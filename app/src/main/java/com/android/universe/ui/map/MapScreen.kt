package com.android.universe.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.view.View
import android.widget.FrameLayout
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.universe.BuildConfig
import com.android.universe.model.location.TomTomLocationRepository
import com.android.universe.model.map.MapUiState
import com.android.universe.ui.navigation.NavigationBottomMenu
import com.android.universe.ui.navigation.Tab
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.map.display.MapOptions
import com.tomtom.sdk.map.display.location.LocationMarkerOptions
import com.tomtom.sdk.map.display.ui.MapFragment as TomTomMapFragment
import kotlinx.coroutines.launch

/**
 * Composable for displaying a map using TomTom SDK and handling user location.
 *
 * This screen requests location permissions, tracks the user's location, and updates the map
 * accordingly. It also provides a FloatingActionButton to center the map on the user's current
 * location.
 *
 * @param onTabSelected Lambda to handle bottom navigation tab selection.
 * @param viewModel Optional [MapViewModel] instance for managing map state. If not provided, a
 *   default instance is created using [TomTomLocationRepository].
 */
@Composable
fun MapScreen(onTabSelected: (Tab) -> Unit) {
  val context = LocalContext.current

  val viewModel: MapViewModel = viewModel { MapViewModel(TomTomLocationRepository(context)) }

  // FragmentManager required to attach TomTomMapFragment
  val fragmentManager = (context as FragmentActivity).supportFragmentManager
  // Holds a reference to the TomTomMapFragment
  var mapFragment by remember { mutableStateOf<TomTomMapFragment?>(null) }

  // Map configuration with API key
  val mapOptions = MapOptions(mapKey = BuildConfig.TOMTOM_API_KEY)
  val containerId = remember { View.generateViewId() }

  val uiState by viewModel.uiState.collectAsState()

  var hasPermission by remember {
    mutableStateOf(
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED)
  }

  // Launcher to request location permission at runtime
  val permissionLauncher =
      rememberLauncherForActivityResult(
          contract = ActivityResultContracts.RequestPermission(),
          onResult = { granted -> hasPermission = granted })

  LaunchedEffect(Unit) {
    if (!hasPermission) {
      permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }
  }

  LaunchedEffect(hasPermission) {
    if (hasPermission) {
      viewModel.loadLastKnownLocation()
      viewModel.startLocationTracking()
    }
  }

  // Automatically center map when location updates and tracking is active
  LaunchedEffect(uiState) {
    when (val state = uiState) {
      is MapUiState.Success -> {
        viewModel.centerOn(GeoPoint(state.location.latitude, state.location.longitude), zoom = 15.0)
      }
      is MapUiState.Error -> {
        viewModel.centerOnLausanne()
      }
      is MapUiState.PermissionRequired -> {
        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
      }
      else -> Unit
    }
  }

  Scaffold(bottomBar = { NavigationBottomMenu(Tab.Map, onTabSelected) }) { padding ->
    Box(modifier = Modifier.fillMaxSize().padding(padding)) {
      AndroidView(
          factory = { ctx ->
            FrameLayout(ctx).apply {
              id = containerId
              layoutParams =
                  FrameLayout.LayoutParams(
                      FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)

              post {
                if (fragmentManager.findFragmentByTag("TomTomMap") == null) {
                  val fragment = TomTomMapFragment.newInstance(mapOptions)
                  fragmentManager.beginTransaction().replace(id, fragment, "TomTomMap").commitNow()
                  mapFragment = fragment

                  fragment.getMapAsync { tomtomMap ->
                    tomtomMap.setLocationProvider(viewModel.locationProvider)

                    val locationMarkerOptions =
                        LocationMarkerOptions(type = LocationMarkerOptions.Type.Pointer)
                    tomtomMap.enableLocationMarker(locationMarkerOptions)

                    (ctx as FragmentActivity).lifecycleScope.launch {
                      ctx.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        viewModel.cameraCommands.collect { camera -> tomtomMap.moveCamera(camera) }
                      }
                    }
                  }
                }
              }
            }
          },
          modifier = Modifier.matchParentSize())

      when (val state = uiState) {
        is MapUiState.Loading -> {
          CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
        is MapUiState.Error -> {
          Snackbar(modifier = Modifier.padding(16.dp)) { Text(state.message) }
        }
        is MapUiState.PermissionRequired -> {
          Snackbar(modifier = Modifier.padding(16.dp)) { Text("Location permission required") }
        }
        else -> Unit
      }
    }

    // Cleanup resources
    DisposableEffect(Unit) {
      onDispose {
        viewModel.stopLocationTracking()
        mapFragment?.let {
          fragmentManager.beginTransaction().remove(it).commitNowAllowingStateLoss()
        }
      }
    }
  }
}
