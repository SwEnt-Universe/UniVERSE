package com.android.universe.ui.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.universe.BuildConfig
import com.android.universe.R
import com.android.universe.model.event.Event
import com.android.universe.model.event.EventRepositoryProvider
import com.android.universe.model.location.TomTomLocationRepository
import com.android.universe.model.user.UserRepositoryProvider
import com.android.universe.ui.components.LiquidButton
import com.android.universe.ui.navigation.NavigationBottomMenu
import com.android.universe.ui.navigation.NavigationTestTags
import com.android.universe.ui.navigation.Tab
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.map.display.MapOptions
import com.tomtom.sdk.map.display.TomTomMap
import com.tomtom.sdk.map.display.image.ImageFactory
import com.tomtom.sdk.map.display.location.LocationMarkerOptions
import com.tomtom.sdk.map.display.marker.MarkerOptions
import com.tomtom.sdk.map.display.ui.MapView

object MapScreenTestTags {
  const val MAP_VIEW = "map_view"
  const val INTERACTABLE = "interactable"
  const val LOADING_INDICATOR = "loading_indicator"
  const val CREATE_EVENT_BUTTON = "create_event_button"
  const val EVENT_INFO_POPUP = "event_info_popup"
  const val EVENT_JOIN_LEAVE_BUTTON = "event_join_leave_button"
}

/**
 * Composable for displaying a map screen with location tracking and permissions.
 *
 * This screen handles location permissions, manages the MapViewModel, and displays the map along
 * with appropriate UI states.
 *
 * @param uid The user ID for loading user-specific data.
 * @param onTabSelected Lambda to handle bottom navigation tab selection.
 */
@Composable
fun MapScreen(
    uid: String,
    onTabSelected: (Tab) -> Unit,
    context: Context = LocalContext.current,
    createEvent: (latitude: Double, longitude: Double) -> Unit = { lat, lng -> },
    viewModel: MapViewModel = viewModel {
      MapViewModel(
          uid,
          TomTomLocationRepository(context),
          EventRepositoryProvider.repository,
          UserRepositoryProvider.repository)
    }
) {

  val uiState by viewModel.uiState.collectAsState()
  val selectedEvent by viewModel.selectedEvent.collectAsState()

  val hasPermission =
      ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
          PackageManager.PERMISSION_GRANTED

  val permissionLauncher =
      rememberLauncherForActivityResult(
          contract = ActivityResultContracts.RequestPermission(),
          onResult = { granted ->
            if (granted) {
              viewModel.loadLastKnownLocation()
              viewModel.startLocationTracking()
            }
          })

  LaunchedEffect(Unit) {
    if (hasPermission) {
      viewModel.loadLastKnownLocation()
      viewModel.startLocationTracking()
    } else {
      permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }
  }

  DisposableEffect(Unit) { onDispose { viewModel.stopLocationTracking() } }

  Scaffold(
      modifier = Modifier.testTag(NavigationTestTags.MAP_SCREEN),
      bottomBar = { NavigationBottomMenu(Tab.Map, onTabSelected) }) { paddingValues ->
        Box(
            modifier =
                Modifier.fillMaxSize()
                    .then(
                        if (uiState.isMapInteractive)
                            Modifier.testTag(MapScreenTestTags.INTERACTABLE)
                        else Modifier)) {
              TomTomMapView(
                  viewModel = viewModel,
                  modifier = Modifier.fillMaxSize(),
                  createEvent = createEvent)

              if (uiState.selectedLat != null && uiState.selectedLng != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                  LiquidButton(
                      onClick = {
                        createEvent(uiState.selectedLat!!, uiState.selectedLng!!)
                        viewModel.selectLocation(null, null)
                      },
                      modifier =
                          Modifier.padding(bottom = 96.dp)
                              .testTag(MapScreenTestTags.CREATE_EVENT_BUTTON)) {
                        Text("Create your Event !", color = MaterialTheme.colorScheme.onBackground)
                      }
                }
              }
              if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier =
                        Modifier.align(Alignment.Center)
                            .testTag(MapScreenTestTags.LOADING_INDICATOR))
              }

              uiState.error?.let { errorMessage ->
                Snackbar(modifier = Modifier.padding(paddingValues)) { Text(errorMessage) }
              }

              if (uiState.isPermissionRequired) {
                Snackbar(modifier = Modifier.padding(16.dp)) {
                  Text("Location permission required")
                }
              }

              selectedEvent?.let { event ->
                EventInfoPopup(
                    event = event,
                    isUserParticipant = viewModel.isUserParticipant(event),
                    onDismiss = { viewModel.selectEvent(null) },
                    onToggleEventParticipation = { viewModel.toggleEventParticipation(event) })
              }
            }
      }
}

/**
 * A composable function that wraps the TomTom [MapView] in an [AndroidView].
 *
 * This function creates and manages a MapView instance, handling its lifecycle (onCreate, onStart,
 * onStop, onDestroy) and integrating with the MapViewModel for location tracking and camera
 * updates.
 *
 * @param viewModel The MapViewModel that manages map state and location data.
 * @param modifier The modifier to be applied to the layout.
 */
@Composable
fun TomTomMapView(
    viewModel: MapViewModel,
    modifier: Modifier = Modifier,
    createEvent: (latitude: Double, longitude: Double) -> Unit = { lat, lng -> }
) {
  val context = LocalContext.current
  val state = viewModel.uiState.collectAsState()
  LaunchedEffect(state.value.eventCount) { viewModel.loadAllEvents() }
  LaunchedEffect(Unit) {
    // Some polling so that we don't need to create a lot of listeners
    viewModel.startEventPolling(1)
  }
  DisposableEffect(Unit) { onDispose { viewModel.stopEventPolling() } }
  val mapView =
      remember(context) { MapView(context, MapOptions(mapKey = BuildConfig.TOMTOM_API_KEY)) }
  var tomtomMap by remember { mutableStateOf<TomTomMap?>(null) }
  var isInitialized by remember { mutableStateOf(false) }
  var isLocationProviderSet by remember { mutableStateOf(false) }
  val eventMarkers by viewModel.eventMarkers.collectAsState()

  AndroidView(
      modifier = modifier.testTag(MapScreenTestTags.MAP_VIEW),
      factory = { ctx ->
        mapView.apply {
          if (!isInitialized) {
            onCreate(null)
            isInitialized = true
          }
          onStart()

          getMapAsync { map ->
            val coordinateEventMap = mutableMapOf<Pair<Double, Double>, Event>()
            eventMarkers.forEach { event ->
              event.location?.let { loc ->
                val coordinate = GeoPoint(loc.latitude, loc.longitude)
                map.addMarker(
                    MarkerOptions(
                        coordinate = GeoPoint(loc.latitude, loc.longitude),
                        pinImage = ImageFactory.fromResource(R.drawable.ic_marker_icon),
                        pinIconImage = ImageFactory.fromResource(R.drawable.ic_marker_icon)))
                coordinateEventMap[Pair(loc.latitude, loc.longitude)] = event
              }
            }
            tomtomMap = map

            if (!isLocationProviderSet && viewModel.locationProvider != null) {
              map.setLocationProvider(viewModel.locationProvider)
              isLocationProviderSet = true

              val locationMarkerOptions =
                  LocationMarkerOptions(type = LocationMarkerOptions.Type.Pointer)
              map.enableLocationMarker(locationMarkerOptions)
            }

            map.addMapClickListener { geoPoint ->
              map.removeMarkers(tag = "coordinate")
              val latitude = geoPoint.latitude
              val longitude = geoPoint.longitude
              viewModel.selectLocation(latitude, longitude)

              map.addMarker(
                  MarkerOptions(
                      tag = "coordinate",
                      coordinate = GeoPoint(latitude, longitude),
                      pinImage = ImageFactory.fromResource(R.drawable.ic_marker_icon)))
              true
            }

            map.addMarkerClickListener { clickedMarker ->
              val clickedCoordinate =
                  Pair(clickedMarker.coordinate.latitude, clickedMarker.coordinate.longitude)

              coordinateEventMap[clickedCoordinate]?.let { clickedEvent ->
                viewModel.selectEvent(clickedEvent)
                true
              } ?: false
            }

            viewModel.nowInteractable()
          }
        }
      },
      update = { view -> view.onStart() },
      onReset = { mapView.onStop() })

  LaunchedEffect(eventMarkers) {
    tomtomMap?.let { map ->
      map.clear()
      eventMarkers.forEach { event ->
        event.location?.let { loc ->
          val coordinate = Pair(loc.latitude, loc.longitude)
          map.addMarker(
              MarkerOptions(
                  coordinate = GeoPoint(loc.latitude, loc.longitude),
                  pinImage = ImageFactory.fromResource(R.drawable.ic_marker_icon),
                  pinIconImage = ImageFactory.fromResource(R.drawable.ic_marker_icon)))
        }
      }
    }
  }

  Button(
      onClick = { viewModel.loadAllEvents() },
      modifier = Modifier.padding(top = 32.dp).padding(horizontal = 16.dp)) {
        Text("Reload events")
      }

  LaunchedEffect(Unit) {
    viewModel.cameraCommands.collect { camera -> tomtomMap?.moveCamera(camera) }
  }

  DisposableEffect(Unit) { onDispose { mapView.onStop() } }
}
