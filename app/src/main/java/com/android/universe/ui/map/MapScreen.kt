package com.android.universe.ui.map

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import com.android.universe.model.location.TomTomLocationRepository
import com.android.universe.ui.navigation.NavigationBottomMenu
import com.android.universe.ui.navigation.NavigationTestTags
import com.android.universe.ui.navigation.Tab
import com.tomtom.sdk.map.display.MapOptions
import com.tomtom.sdk.map.display.TomTomMap
import com.tomtom.sdk.map.display.location.LocationMarkerOptions
import com.tomtom.sdk.map.display.ui.MapView

object MapScreenTestTags {
  const val MAP_VIEW = "map_view"
  const val LOADING_INDICATOR = "loading_indicator"
}

/**
 * Composable for displaying a map screen with location tracking and permissions.
 *
 * This screen handles location permissions, manages the MapViewModel, and displays the map along
 * with appropriate UI states.
 *
 * @param onTabSelected Lambda to handle bottom navigation tab selection.
 */
@Composable
fun MapScreen(onTabSelected: (Tab) -> Unit) {
  val context = LocalContext.current
  val viewModel: MapViewModel = viewModel { MapViewModel(TomTomLocationRepository(context)) }
  val uiState by viewModel.uiState.collectAsState()

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
      bottomBar = { NavigationBottomMenu(Tab.Map, onTabSelected) }) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
          TomTomMapView(viewModel = viewModel, modifier = Modifier.fillMaxSize())

          if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier =
                    Modifier.align(Alignment.Center).testTag(MapScreenTestTags.LOADING_INDICATOR))
          }

          uiState.error?.let { errorMessage ->
            Snackbar(modifier = Modifier.padding(16.dp)) { Text(errorMessage) }
          }

          if (uiState.isPermissionRequired) {
            Snackbar(modifier = Modifier.padding(16.dp)) { Text("Location permission required") }
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
fun TomTomMapView(viewModel: MapViewModel, modifier: Modifier = Modifier) {
  val context = LocalContext.current

  val mapView =
      remember(context) { MapView(context, MapOptions(mapKey = BuildConfig.TOMTOM_API_KEY)) }
  var tomtomMap by remember { mutableStateOf<TomTomMap?>(null) }
  var isInitialized by remember { mutableStateOf(false) }
  var isLocationProviderSet by remember { mutableStateOf(false) }

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
          }
        }
      },
      update = { view -> view.onStart() },
      onReset = { mapView.onStop() })

  LaunchedEffect(Unit) {
    viewModel.cameraCommands.collect { camera -> tomtomMap?.moveCamera(camera) }
  }

  DisposableEffect(Unit) { onDispose { mapView.onStop() } }
}
