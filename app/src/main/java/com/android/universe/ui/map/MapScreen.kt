package com.android.universe.ui.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.android.universe.model.tag.Tag
import com.android.universe.model.user.UserRepositoryProvider
import com.android.universe.ui.common.TagRow
import com.android.universe.ui.components.CategoryItemDefaults
import com.android.universe.ui.components.LiquidButton
import com.android.universe.ui.components.ScreenLayoutWithBox
import com.android.universe.ui.navigation.NavigationBottomMenu
import com.android.universe.ui.navigation.NavigationTestTags
import com.android.universe.ui.navigation.Tab
import com.android.universe.ui.theme.Dimensions
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
 * Defines the interaction mode of the map UI.
 * - `NORMAL`: Standard browsing mode where users can pan/zoom the map and view events.
 * - `SELECT_LOCATION`: Special mode used when creating an event, allowing the user to pick a
 *   specific location by clicking on the map.
 */
enum class MapMode {
  NORMAL,
  SELECT_LOCATION
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
 * @param onNavigateToEventCreation Invoked when the user chooses manual event creation.
 * @param preselectedEventId An optional event ID to preselect and focus on when the map loads.
 * @param preselectedLocation An optional location to preselect and focus on when the map loads.
 * @param onChatNavigate A callback function invoked when navigating to a chat, with event ID and
 *   title as parameters.
 * @param viewModel The [MapViewModel] that provides the state for the screen. Defaults to a
 *   ViewModel instance initialized with necessary repositories.
 */
@Composable
fun MapScreen(
    uid: String,
    onTabSelected: (Tab) -> Unit,
    onNavigateToEventCreation: () -> Unit,
    context: Context = LocalContext.current,
    preselectedEventId: String? = null,
    preselectedLocation: Location? = null,
    onChatNavigate: (eventId: String, eventTitle: String) -> Unit = { _, _ -> },
    onLocationSelected: (Double, Double) -> Unit = { _, _ -> },
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
  var showMapModal by remember { mutableStateOf(false) }
  val categoryList = Tag.tagFromEachCategory.toList()
  val categories by viewModel.categories.collectAsState()
  // --- 1. Permissions & Initialization ---

  val permissionLauncher =
      rememberLauncherForActivityResult(
          contract = ActivityResultContracts.RequestMultiplePermissions(),
          onResult = { permissions ->
            val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val coarseLocationGranted =
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
            if (fineLocationGranted && coarseLocationGranted && uiState.isMapInteractive) {
              viewModel.onPermissionGranted()
            }
          })

  LaunchedEffect(uiState.isMapInteractive) {
    viewModel.init(uid, onLocationSelected) // Start polling, etc.
    val hasFine =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
    val hasCoarse =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    if (hasFine && hasCoarse && uiState.isMapInteractive) {
      viewModel.onPermissionGranted()
    } else {
      permissionLauncher.launch(
          arrayOf(
              Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }
  }

  LaunchedEffect(uiState.mapMode) { viewModel.updateLongClickListener(onLocationSelected) }

  // --- 2. Reactive Updates (Side Effects) ---

  // Sync Markers
  LaunchedEffect(uiState.markers, uiState.isMapInteractive) {
    viewModel.syncEventMarkers(uiState.markers)
  }

  // Sync Selection
  LaunchedEffect(uiState.selectedLocation) {
    viewModel.syncSelectedLocationMarker(uiState.selectedLocation)
  }

  // Handle direct event link: auto-focus and open popup
  LaunchedEffect(preselectedEventId, preselectedLocation) {
    if (preselectedEventId != null && preselectedLocation != null) {
      val target = GeoPoint(preselectedLocation.latitude, preselectedLocation.longitude)
      viewModel.onCameraMoveRequest(target)

      // --- Select event to show popup ---
      val matched = uiState.markers.firstOrNull { it.event.id == preselectedEventId }?.event
      if (matched != null) {
        viewModel.selectEvent(matched)
      }
    }
  }

  // Listen for camera-center requests. Performs centering and clears request.
  LaunchedEffect(uiState.pendingCameraCenter, uiState.isMapInteractive) {
    val target = uiState.pendingCameraCenter
    if (target != null && uiState.isMapInteractive) {
      viewModel.onCameraMoveRequest(target)
      viewModel.clearPendingCameraCenter()
    }
  }

  // --- 3. UI Structure ---
  ScreenLayoutWithBox(
      modifier = Modifier.testTag(NavigationTestTags.MAP_SCREEN),
      bottomBar = {
        NavigationBottomMenu(
            selectedTab = Tab.Map,
            onTabSelected = { tab ->
              viewModel.resetFilter()
              onTabSelected(tab)
            })
      }) { padding ->
        MapBox(uiState = uiState) {
          BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val screenHeight = maxHeight
            val topOffset = screenHeight * 0.05f

            TagRow(
                categoryList,
                modifierBox = Modifier.offset(y = topOffset),
                heightTag = CategoryItemDefaults.HEIGHT_CAT,
                widthTag = CategoryItemDefaults.WIDTH_CAT,
                isSelected = { cat -> categories.contains(cat.category) },
                onTagSelect = { cat -> viewModel.selectCategory(cat.category, true) },
                onTagReSelect = { cat -> viewModel.selectCategory(cat.category, false) },
                fadeWidth = (CategoryItemDefaults.HEIGHT_CAT * 0.5f).dp,
                isCategory = true)
          }

          // Create Event Button
          AddEventButton(onClick = { showMapModal = true }, boxScope = this, padding = padding)
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

          selectedEvent.let {
            if (it is MapViewModel.EventSelectionState.Selected)
                EventInfoPopup(
                    modifier = Modifier.padding(padding),
                    event = it.event,
                    creator = it.creator,
                    onDismiss = { viewModel.selectEvent(null) },
                    onChatNavigate = { eventId, eventTitle ->
                      viewModel.resetFilter()
                      onChatNavigate(eventId, eventTitle)
                    },
                    isUserParticipant = viewModel.isUserParticipant(it.event),
                    onToggleEventParticipation = { viewModel.toggleEventParticipation(it.event) })
          }

          MapCreateEventModal(
              isPresented = showMapModal,
              onDismissRequest = { showMapModal = false },
              onAiCreate = { viewModel.generateAiEventAroundUser() },
              onManualCreate = {
                viewModel.resetFilter()
                onNavigateToEventCreation()
                showMapModal = false
              })
        }
      }
}

@Composable
private fun MapBox(
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopStart,
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
      contentAlignment = contentAlignment,
      propagateMinConstraints = propagateMinConstraints,
      content = content)
}

@Composable
private fun AddEventButton(onClick: () -> Unit, boxScope: BoxScope, padding: PaddingValues) {
  boxScope.apply {
    Box(
        modifier =
            Modifier.align(Alignment.BottomStart)
                .padding(
                    bottom = padding.calculateBottomPadding(),
                    start = Dimensions.PaddingExtraLarge)) {
          LiquidButton(
              onClick = onClick,
              height = 56f,
              width = 56f,
              modifier = Modifier.testTag(MapScreenTestTags.CREATE_EVENT_BUTTON)) {
                Text("+", color = MaterialTheme.colorScheme.onBackground)
              }
        }
  }
}

@Preview
@Composable
fun mapPreview() {
  Box(modifier = Modifier.fillMaxSize()) {}
}
