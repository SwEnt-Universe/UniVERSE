package com.android.universe.ui.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.universe.BuildConfig
import com.android.universe.model.event.EventRepositoryProvider
import com.android.universe.model.location.Location
import com.android.universe.model.location.TomTomLocationRepository
import com.android.universe.model.user.UserRepositoryProvider
import com.android.universe.ui.components.LiquidButton
import com.android.universe.ui.navigation.NavigationBottomMenu
import com.android.universe.ui.navigation.NavigationTestTags
import com.android.universe.ui.navigation.Tab
import com.tomtom.sdk.common.Uri
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.map.display.MapOptions
import com.tomtom.sdk.map.display.TomTomMap
import com.tomtom.sdk.map.display.style.StyleDescriptor
import com.tomtom.sdk.map.display.ui.MapView
import com.tomtom.sdk.map.display.ui.currentlocation.CurrentLocationButton
import com.tomtom.sdk.map.display.ui.logo.LogoView

object MapScreenTestTags {
  const val MAP_VIEW = "map_view"
  const val INTERACTABLE = "interactable"
  const val LOADING_INDICATOR = "loading_indicator"
  const val CREATE_EVENT_BUTTON = "create_event_button"
  const val EVENT_INFO_POPUP = "event_info_popup"
  const val EVENT_JOIN_LEAVE_BUTTON = "event_join_leave_button"
}

/**
 * The main screen composable for displaying a map with event markers.
 *
 * This screen handles location permissions, initializes the map, displays event markers, and
 * manages user interactions such as selecting events and creating new ones.
 *
 * @param uid The unique identifier for the current user.
 * @param onTabSelected A callback function invoked when a tab in the bottom navigation menu is
 *   selected.
 * @param context The Android context, defaulting to the current LocalContext.
 * @param preselectedEventId An optional event ID to preselect and focus on when the map loads.
 * @param preselectedLocation An optional location to preselect and focus on when the map loads.
 * @param onChatNavigate A callback function invoked when navigating to a chat, with event ID and
 *   title as parameters.
 * @param createEvent A callback function invoked when creating a new event at specified latitude
 *   and longitude.
 * @param viewModel The [MapViewModel] that provides the state for the screen. Defaults to a
 *   ViewModel instance initialized with necessary repositories.
 */
@Composable
fun MapScreen(
    uid: String,
    onTabSelected: (Tab) -> Unit,
    context: Context = LocalContext.current,
    preselectedEventId: String? = null,
    preselectedLocation: Location? = null,
    onChatNavigate: (eventId: String, eventTitle: String) -> Unit = { _, _ -> },
    createEvent: (latitude: Double, longitude: Double) -> Unit = { _, _ -> },
    viewModel: MapViewModel = viewModel {
      MapViewModel(
          context.getSharedPreferences("map_pref", Context.MODE_PRIVATE),
          TomTomLocationRepository(context),
          EventRepositoryProvider.repository,
          UserRepositoryProvider.repository)
    }
) {
  val uiState by viewModel.uiState.collectAsState()
  val selectedEvent by viewModel.selectedEvent.collectAsState()

  // Local cache for marker click handling (ID -> Event)

  // --- 1. Permissions & Initialization ---

  val permissionLauncher =
      rememberLauncherForActivityResult(
          contract = ActivityResultContracts.RequestMultiplePermissions(),
          onResult = { permissions ->
            val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val coarseLocationGranted =
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
            if (fineLocationGranted && coarseLocationGranted) {
              viewModel.onPermissionGranted()
            }
          })

  LaunchedEffect(Unit) {
    viewModel.initData(uid) // Start polling, etc.

    val hasFine =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
    val hasCoarse =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    if (hasFine && hasCoarse) {
      viewModel.onPermissionGranted()
    } else {
      permissionLauncher.launch(
          arrayOf(
              Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }
  }

  // --- 2. Reactive Updates (Side Effects) ---

  // Sync Markers
  LaunchedEffect(uiState.markers) { viewModel.syncEventMarkers(uiState.markers) }

  // Sync Selection
  LaunchedEffect(uiState.selectedLocation) {
    viewModel.syncSelectedLocationMarker(uiState.selectedLocation)
  }

  // Handle direct event link: auto-focus and open popup
  LaunchedEffect(preselectedEventId, preselectedLocation) {
    if (preselectedEventId != null && preselectedLocation != null) {
      val target = GeoPoint(preselectedLocation.latitude, preselectedLocation.longitude)
      viewModel.onCameraMoveRequest(target, uiState.zoomLevel)

      // --- Select event to show popup ---
      val matched = uiState.markers.firstOrNull { it.event.id == preselectedEventId }?.event
      if (matched != null) {
        viewModel.selectEvent(matched)
      }
    }
  }

  // --- 3. UI Structure ---
  Scaffold(
      containerColor = Color.Transparent,
      modifier = Modifier.testTag(NavigationTestTags.MAP_SCREEN),
      bottomBar = {
        NavigationBottomMenu(selectedTab = Tab.Map, onTabSelected = { tab -> onTabSelected(tab) })
      }) { padding ->
        Box(
            modifier =
                Modifier.fillMaxSize()
                    .then(
                        if (uiState.isMapInteractive)
                            Modifier.testTag(MapScreenTestTags.INTERACTABLE)
                        else Modifier)) {
              if (uiState.selectedLocation != null) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.BottomCenter) {
                      LiquidButton(
                          onClick = {
                            createEvent(
                                uiState.selectedLocation!!.latitude,
                                uiState.selectedLocation!!.longitude)
                          },
                          modifier =
                              Modifier.padding(bottom = 96.dp)
                                  .testTag(MapScreenTestTags.CREATE_EVENT_BUTTON)) {
                            Text(
                                "Create your Event !",
                                color = MaterialTheme.colorScheme.onBackground)
                          }
                    }
              }
              // Overlays
              if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier =
                        Modifier.align(Alignment.Center)
                            .testTag(MapScreenTestTags.LOADING_INDICATOR))
              }

              uiState.error?.let { errorMessage ->
                Snackbar(modifier = Modifier.align(Alignment.BottomCenter).padding(padding)) {
                  Text(errorMessage)
                }
              }

              selectedEvent?.let { event ->
                EventInfoPopup(
                    modifier = Modifier.padding(padding),
                    event = event,
                    isUserParticipant = viewModel.isUserParticipant(event),
                    onDismiss = { viewModel.selectEvent(null) },
                    onChatNavigate = onChatNavigate,
                    onToggleEventParticipation = { viewModel.toggleEventParticipation(event) })
              }
            }
      }
}

// --- HELPER COMPOSABLES & EXTENSIONS ---

@Composable
fun TomTomMapComposable(
    modifier: Modifier = Modifier,
    onMapViewReady: (MapView) -> Unit,
    onMapReady: (TomTomMap) -> Unit
) {
  val mapView = rememberMapViewWithLifecycle(onMapReady)
  onMapViewReady(mapView)

  AndroidView(
      factory = { mapView.apply { configureUiSettings() } },
      modifier = modifier.testTag(MapScreenTestTags.MAP_VIEW))
}

@Composable
fun rememberMapViewWithLifecycle(onMapReady: (TomTomMap) -> Unit): MapView {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current

  val mapOptions = remember {
    MapOptions(
        mapKey = BuildConfig.TOMTOM_API_KEY,
        mapStyle =
            StyleDescriptor(
                Uri.parse(
                    "https://api.tomtom.com/style/2/custom/style/dG9tdG9tQEBAZUJrOHdFRXJIM0oySEUydTsd6ZOYVIJPYKLNwZiNGdLE/drafts/0.json?key=oICGv96tZpkxbJRieRSfAKcW8fmNuUWx")),
        renderToTexture = true)
  }

  val mapView = remember { MapView(context, mapOptions) }
  return mapView
}

// --- MapView Extension Functions ---

private fun MapView.configureUiSettings() {
  this.currentLocationButton.visibilityPolicy = CurrentLocationButton.VisibilityPolicy.Invisible
  this.logoView.visibilityPolicy = LogoView.VisibilityPolicy.Invisible
  this.scaleView.isVisible = false
}

// --- TomTomMap Extension Functions ---

@Preview
@Composable
fun mapPreview() {
  Box(modifier = Modifier.fillMaxSize()) {}
}
