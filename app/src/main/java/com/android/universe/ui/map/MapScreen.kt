package com.android.universe.ui.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.universe.model.event.EventRepositoryProvider
import com.android.universe.model.location.Location
import com.android.universe.model.location.TomTomLocationRepository
import com.android.universe.model.user.UserRepositoryProvider
import com.android.universe.ui.components.LiquidButton
import com.android.universe.ui.components.ScreenLayout
import com.android.universe.ui.navigation.NavigationBottomMenu
import com.android.universe.ui.navigation.NavigationTestTags
import com.android.universe.ui.navigation.Tab
import com.tomtom.sdk.location.GeoPoint

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
          context,
          context.getSharedPreferences("map_pref", Context.MODE_PRIVATE),
          TomTomLocationRepository(context),
          EventRepositoryProvider.repository,
          UserRepositoryProvider.repository)
    }
) {
  val uiState by viewModel.uiState.collectAsState()
  val selectedEvent by viewModel.selectedEvent.collectAsState()

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
  LaunchedEffect(uiState.markers, uiState.isMapInteractive) { viewModel.syncEventMarkers(uiState.markers) }

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
  ScreenLayout(
      modifier = Modifier.testTag(NavigationTestTags.MAP_SCREEN),
      bottomBar = {
        NavigationBottomMenu(selectedTab = Tab.Map, onTabSelected = { tab -> onTabSelected(tab) })
      }) { padding ->
        MapBox(uiState = uiState) {
          // Create Event Button
          uiState.selectedLocation?.let { CreateEventButton(padding, createEvent, uiState) }

          // Overlays
          if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier =
                    Modifier.align(Alignment.Center).testTag(MapScreenTestTags.LOADING_INDICATOR))
          }

          uiState.error?.let { errorMessage ->
            Snackbar(modifier = Modifier.align(Alignment.BottomCenter).padding(padding)) {
              Text(errorMessage)
            }
          }

          selectedEvent?.let { event ->
            EventInfoPopup(
                modifier = Modifier.padding(bottom = padding.calculateBottomPadding()),
                event = event,
                isUserParticipant = viewModel.isUserParticipant(event),
                onDismiss = { viewModel.selectEvent(null) },
                onChatNavigate = onChatNavigate,
                onToggleEventParticipation = { viewModel.toggleEventParticipation(event) })
          }
        }
      }
}

@Composable
private fun CreateEventButton(
    padding: PaddingValues,
    createEvent: (latitude: Double, longitude: Double) -> Unit,
    uiState: MapUiState
) {
  Box(
      modifier = Modifier.fillMaxSize().padding(padding),
      contentAlignment = Alignment.BottomCenter) {
        LiquidButton(
            onClick = {
              createEvent(uiState.selectedLocation!!.latitude, uiState.selectedLocation.longitude)
            },
            modifier =
                Modifier.padding(bottom = 96.dp).testTag(MapScreenTestTags.CREATE_EVENT_BUTTON)) {
              Text("Create your Event !", color = MaterialTheme.colorScheme.onBackground)
            }
      }
}

@Composable
private fun MapBox(
    modifier: Modifier = Modifier,
    contentAlignement: Alignment = Alignment.TopStart,
    propagateMinConstraints: Boolean = false,
    uiState: MapUiState,
    content: @Composable (BoxScope.() -> Unit)
) {
  Box(
      modifier =
          modifier
              .fillMaxSize()
              .then(
                  if (uiState.isMapInteractive) Modifier.testTag(MapScreenTestTags.INTERACTABLE)
                  else Modifier),
      contentAlignment = contentAlignement,
      propagateMinConstraints = propagateMinConstraints,
      content = content)
}

@Preview
@Composable
fun mapPreview() {
  Box(modifier = Modifier.fillMaxSize()) {}
}
