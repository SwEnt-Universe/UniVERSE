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
import com.android.universe.ui.event.EventInfoPopup
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

  Box(modifier = Modifier.fillMaxSize()) {
    Scaffold(
        modifier = Modifier.testTag(NavigationTestTags.MAP_SCREEN),
        bottomBar = { NavigationBottomMenu(Tab.Map, onTabSelected) }) { padding ->
          Box(
              modifier =
                  Modifier.fillMaxSize()
                      .padding(padding)
                      .then(
                          if (uiState.isMapInteractive)
                              Modifier.testTag(MapScreenTestTags.INTERACTABLE)
                          else Modifier)) {
                TomTomMapView(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize(),
                    createEvent = createEvent)

                if (uiState.isLoading) {
                  CircularProgressIndicator(
                      modifier =
                          Modifier.align(Alignment.Center)
                              .testTag(MapScreenTestTags.LOADING_INDICATOR))
                }

                uiState.error?.let { errorMessage ->
                  Snackbar(modifier = Modifier.padding(16.dp)) { Text(errorMessage) }
                }

                if (uiState.isPermissionRequired) {
                  Snackbar(modifier = Modifier.padding(16.dp)) {
                    Text("Location permission required")
                  }
                }
              }
        }

    if (selectedEvent != null) {
      EventInfoPopup(event = selectedEvent!!, onDismiss = { viewModel.selectEvent(null) })
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

  val mapView =
      remember(context) { MapView(context, MapOptions(mapKey = BuildConfig.TOMTOM_API_KEY)) }
  var tomtomMap by remember { mutableStateOf<TomTomMap?>(null) }
  var isInitialized by remember { mutableStateOf(false) }
  var isLocationProviderSet by remember { mutableStateOf(false) }
  val state = viewModel.uiState.collectAsState()
  val eventMarkers by viewModel.eventMarkers.collectAsState()

  // Use coordinates as key instead of Marker objects because they may not have proper equality
  val coordinateEventMap = remember { mutableMapOf<Pair<Double, Double>, Event>() }

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
            tomtomMap = map

            if (!isLocationProviderSet && viewModel.locationProvider != null) {
              map.setLocationProvider(viewModel.locationProvider)
              isLocationProviderSet = true

              val locationMarkerOptions =
                  LocationMarkerOptions(type = LocationMarkerOptions.Type.Pointer)
              map.enableLocationMarker(locationMarkerOptions)
            }
            map.addMapClickListener { geoPoint ->
              val latitude = geoPoint.latitude
              val longitude = geoPoint.longitude
              viewModel.selectLocation(latitude, longitude)
              true
            }
            viewModel.nowInteractable()
          }
        }
      },
      update = { view -> view.onStart() },
      onReset = { mapView.onStop() })

  // Update markers whenever eventMarkers changes
  LaunchedEffect(eventMarkers) {
    tomtomMap?.let { map ->
      map.clear()
      coordinateEventMap.clear()

      eventMarkers.forEach { event ->
        event.location?.let { loc ->
          val coordinate = Pair(loc.latitude, loc.longitude)
          map.addMarker(
              MarkerOptions(
                  coordinate = GeoPoint(loc.latitude, loc.longitude),
                  pinImage = ImageFactory.fromResource(R.drawable.ic_marker_icon),
                  pinIconImage = ImageFactory.fromResource(R.drawable.ic_marker_icon)))
          coordinateEventMap[coordinate] = event
        }
      }
    }
  }

  // Register click listener only once when map is ready
  LaunchedEffect(tomtomMap) {
    tomtomMap?.let { map ->
      map.addMarkerClickListener { clickedMarker ->
        val clickedCoordinate =
            Pair(clickedMarker.coordinate.latitude, clickedMarker.coordinate.longitude)

        coordinateEventMap[clickedCoordinate]?.let { clickedEvent ->
          viewModel.selectEvent(clickedEvent)
        } ?: true
      }
    }
  }

  if (state.value.selectedLat != null && state.value.selectedLng != null) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
      Button(
          onClick = {
            createEvent(state.value.selectedLat!!, state.value.selectedLng!!)
            viewModel.selectLocation(null, null)
          },
          modifier = Modifier.testTag(MapScreenTestTags.CREATE_EVENT_BUTTON)) {
            Text("Create your Event !")
          }
    }
  }
  Button(onClick = { viewModel.loadAllEvents() }) { Text("Reload events") }

  LaunchedEffect(Unit) {
    viewModel.cameraCommands.collect { camera -> tomtomMap?.moveCamera(camera) }
  }

  DisposableEffect(Unit) { onDispose { mapView.onStop() } }
}
