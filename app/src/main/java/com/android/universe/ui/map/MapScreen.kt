package com.android.universe.ui.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.PixelCopy
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.drawToBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
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
import com.android.universe.ui.utils.LocalLayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.tomtom.sdk.common.Bundle
import com.tomtom.sdk.common.UniqueId
import com.tomtom.sdk.common.Uri
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.location.LocationProvider
import com.tomtom.sdk.map.display.MapOptions
import com.tomtom.sdk.map.display.TomTomMap
import com.tomtom.sdk.map.display.camera.CameraOptions
import com.tomtom.sdk.map.display.camera.CameraSteadyListener
import com.tomtom.sdk.map.display.gesture.MapClickListener
import com.tomtom.sdk.map.display.gesture.MapLongClickListener
import com.tomtom.sdk.map.display.image.ImageFactory
import com.tomtom.sdk.map.display.location.LocationMarkerOptions
import com.tomtom.sdk.map.display.marker.MarkerClickListener
import com.tomtom.sdk.map.display.marker.MarkerOptions
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

@Composable
fun MapScreen(
    uid: String,
    onTabSelected: (Tab) -> Unit,
    context: Context = LocalContext.current,
    createEvent: (latitude: Double, longitude: Double) -> Unit = { _, _ -> },
    viewModel: MapViewModel = viewModel {
      MapViewModel(
          context.getSharedPreferences("map_pref", Context.MODE_PRIVATE),
          uid,
          TomTomLocationRepository(context),
          EventRepositoryProvider.repository,
          UserRepositoryProvider.repository)
    }
) {
  val uiState by viewModel.uiState.collectAsState()
  val selectedEvent by viewModel.selectedEvent.collectAsState()
  val layerBackdrop = LocalLayerBackdrop.current
  var mapViewInstance by remember { mutableStateOf<MapView?>(null) }
  var tomTomMap by remember { mutableStateOf<TomTomMap?>(null) }

  // Local cache for marker click handling (ID -> Event)
  val markerToEvent = remember { mutableMapOf<UniqueId, Event>() }

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
    viewModel.initData() // Start polling, etc.

    val hasFineLocation =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
    val hasCoarseLocation =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    if (hasFineLocation && hasCoarseLocation) {
      viewModel.onPermissionGranted()
    } else {
      permissionLauncher.launch(
          arrayOf(
              Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }
  }

  // --- 2. Reactive Updates (Side Effects) ---

  // Sync Markers
  LaunchedEffect(uiState.markers, tomTomMap) {
    val map = tomTomMap ?: return@LaunchedEffect
    map.syncEventMarkers(uiState.markers, markerToEvent)
  }

  // Sync Selection
  LaunchedEffect(uiState.selectedLocation, tomTomMap) {
    val map = tomTomMap ?: return@LaunchedEffect
    map.syncSelectedLocationMarker(uiState.selectedLocation)
  }

  LaunchedEffect(uiState.cameraPosition) {
    Log.e("MapScreen", "cameraPosition: ${uiState.cameraPosition}")
  }

  // Sync Camera Actions
  LaunchedEffect(viewModel) {
    viewModel.mapActions.collect { action -> tomTomMap?.executeMapAction(action) }
  }

  // --- 3. UI Structure ---
  Scaffold(
      modifier = Modifier.testTag(NavigationTestTags.MAP_SCREEN),
      bottomBar = {
        NavigationBottomMenu(
            selectedTab = Tab.Map,
            onTabSelected = { tab ->
              val view = mapViewInstance
              if (!uiState.isLoading &&
                  uiState.isMapInteractive &&
                  view != null &&
                  tab != Tab.Map) {
                view.takeSnapshot { bmp ->
                  if (bmp != null) {
                    viewModel.onSnapshotAvailable(bmp)
                  }
                }
              }
              onTabSelected(tab)
            })
      }) { padding ->
        Box(
            modifier =
                Modifier.fillMaxSize()
                    .then(
                        if (uiState.isMapInteractive)
                            Modifier.testTag(MapScreenTestTags.INTERACTABLE)
                        else Modifier)) {
              TomTomMapComposable(
                  modifier = Modifier.fillMaxSize().layerBackdrop(layerBackdrop),
                  onMapViewReady = { mapViewInstance = it },
                  onMapReady = { map ->
                    tomTomMap = map

                    // --- 4. Map Initialization Sequence ---
                    map.initLocationProvider(viewModel.locationProvider)

                    map.setUpMapListeners(
                        onMapClick = { viewModel.onMapClick() },
                        onMapLongClick = { pos ->
                          viewModel.onMapLongClick(pos.latitude, pos.longitude)
                        },
                        onMarkerClick = { id ->
                          markerToEvent[id]?.let { event ->
                            viewModel.onMarkerClick(event)
                            true
                          } ?: false
                        },
                        onCameraChange = { pos, zoom -> viewModel.onCameraStateChange(pos, zoom) })

                    map.setInitialCamera(uiState.cameraPosition, uiState.zoomLevel)
                    viewModel.nowInteractable()
                  })

              if (uiState.selectedLocation != null) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.BottomCenter) {
                      LiquidButton(
                          onClick = {
                            val view = mapViewInstance
                            if (!uiState.isLoading && uiState.isMapInteractive && view != null) {
                              view.takeSnapshot { bmp ->
                                if (bmp != null) {
                                  viewModel.onSnapshotAvailable(bmp)
                                }
                              }
                            }
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
                Snackbar(modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)) {
                  Text(errorMessage)
                }
              }

              selectedEvent?.let { event ->
                EventInfoPopup(
                    modifier = Modifier.padding(padding),
                    event = event,
                    isUserParticipant = viewModel.isUserParticipant(event),
                    onDismiss = { viewModel.selectEvent(null) },
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

  AndroidView(
      factory = { mapView.apply { configureUiSettings() } },
      modifier = modifier.testTag(MapScreenTestTags.MAP_VIEW),
      update = { onMapViewReady(mapView) })
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

  DisposableEffect(lifecycleOwner) {
    val lifecycle = lifecycleOwner.lifecycle
    val observer = LifecycleEventObserver { _, event ->
      when (event) {
        Lifecycle.Event.ON_CREATE -> mapView.onCreate(Bundle())
        Lifecycle.Event.ON_START -> mapView.onStart()
        Lifecycle.Event.ON_RESUME -> mapView.onResume()
        Lifecycle.Event.ON_PAUSE -> mapView.onPause()
        Lifecycle.Event.ON_STOP -> mapView.onStop()
        else -> {}
      }
    }
    lifecycle.addObserver(observer)

    if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
      mapView.onStart()
    }
    // onResume means the user can use it so it needs to be called to enable the listeners
    if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
      mapView.onResume()
    }

    mapView.getMapAsync(onMapReady)

    onDispose {
      lifecycle.removeObserver(observer)
      // Manually cleanup if the composable is disposed (e.g. tab switch)
      mapView.onDestroy()
    }
  }
  return mapView
}

// --- MapView Extension Functions ---

private fun MapView.configureUiSettings() {
  this.currentLocationButton.visibilityPolicy = CurrentLocationButton.VisibilityPolicy.Invisible
  this.logoView.visibilityPolicy = LogoView.VisibilityPolicy.Invisible
  this.scaleView.isVisible = false
}

// --- TomTomMap Extension Functions ---

private fun TomTomMap.initLocationProvider(provider: LocationProvider?) {
  provider?.let {
    this.setLocationProvider(it)
    val locationMarkerOptions = LocationMarkerOptions(type = LocationMarkerOptions.Type.Pointer)
    this.enableLocationMarker(locationMarkerOptions)
    it.enable()
  }
}

private fun TomTomMap.setUpMapListeners(
    onMapClick: () -> Unit,
    onMapLongClick: (GeoPoint) -> Unit,
    onMarkerClick: (UniqueId) -> Boolean,
    onCameraChange: (GeoPoint, Double) -> Unit
) {

  this.addMapClickListener(
      MapClickListener {
        onMapClick()
        true
      })

  this.addMapLongClickListener(
      MapLongClickListener { geoPoint ->
        onMapLongClick(geoPoint)
        true
      })

  this.addMarkerClickListener(
      MarkerClickListener { clickedMarker -> onMarkerClick(clickedMarker.id) })

  this.addCameraSteadyListener(
      CameraSteadyListener {
        onCameraChange(this.cameraPosition.position, this.cameraPosition.zoom)
      })
}

private fun TomTomMap.setInitialCamera(position: GeoPoint, zoom: Double) {
  this.moveCamera(CameraOptions(position = position, zoom = zoom))
}

private fun TomTomMap.executeMapAction(action: MapAction) {
  when (action) {
    is MapAction.MoveCamera -> {
      this.moveCamera(CameraOptions(position = action.target, zoom = this.cameraPosition.zoom))
    }
    is MapAction.ZoomIn -> {
      val newZoom = (this.cameraPosition.zoom + 1.0).coerceAtMost(22.0)
      this.animateCamera(CameraOptions(zoom = newZoom))
    }
  }
}

/**
 * Recursively searches this view hierarchy for the TextureView used to render the TomTom map.
 *
 * TomTom's MapView internally contains multiple nested views. Depending on configuration (e.g.,
 * `renderToTexture = true`), the actual rendered map surface may be hosted inside a `TextureView`.
 * This method walks the view tree in depth-first order and returns the first `TextureView`
 * encountered.
 *
 * @return The first discovered [TextureView] instance, or `null` if none exists.
 */
fun View.findRenderingView(): View? {
  if (this is TextureView) return this

  if (this is ViewGroup) {
    for (child in children) {
      val result = child.findRenderingView()
      if (result != null) return result
    }
  }
  return null
}

/**
 * Retrieves the internal rendering view used by this [MapView].
 *
 * This is a convenience wrapper around [findRenderingView], which performs a recursive traversal of
 * the MapView's internal view hierarchy to locate the map rendering surface (normally a
 * [TextureView] when `renderToTexture = true`).
 *
 * @return The underlying rendering view, or `null` if none is found.
 */
fun MapView.getRendererView(): View? {
  return this.findRenderingView()
}

/**
 * Captures a bitmap snapshot of the visible contents rendered by this [MapView].
 *
 * The function attempts to locate the internal rendering surface (usually a [TextureView]) via
 * [getRendererView]. If successful, a bitmap of equal size is created and populated using
 * [PixelCopy]. PixelCopy ensures accurate, GPU-correct rendering even when the map is drawn on a
 * separate hardware layer.
 *
 * Because snapshotting is asynchronous, the result is delivered via a callback. If the rendering
 * view cannot be found, has invalid size, or PixelCopy fails, the callback receives `null`.
 *
 * @param onResult Callback invoked with the resulting [Bitmap], or `null` if snapshot acquisition
 *   failed.
 */
fun MapView.takeSnapshot(onResult: (Bitmap?) -> Unit) {

  val renderer = getRendererView() ?: return onResult(null)

  val width = renderer.width
  val height = renderer.height

  if (width == 0 || height == 0) {
    onResult(null)
    return
  }

  val bitmap = renderer.drawToBitmap()
  val handler = Handler(Looper.getMainLooper())

  when (renderer) {
    is TextureView -> {
      val surface = Surface(renderer.surfaceTexture)
      PixelCopy.request(
          surface,
          bitmap,
          { result -> onResult(if (result == PixelCopy.SUCCESS) bitmap else null) },
          handler)
    }

    else -> onResult(null)
  }
}

private fun TomTomMap.syncEventMarkers(
    markers: List<MapMarkerUiModel>,
    markerMap: MutableMap<UniqueId, Event>
) {
  this.removeMarkers("event")
  markerMap.clear()

  markers.forEach { markerModel ->
    val markerOptions =
        MarkerOptions(
            tag = "event",
            coordinate = markerModel.position,
            pinImage = ImageFactory.fromResource(markerModel.iconResId))
    val addedMarker = this.addMarker(markerOptions)
    markerMap[addedMarker.id] = markerModel.event
  }
}

private fun TomTomMap.syncSelectedLocationMarker(location: GeoPoint?) {
  this.removeMarkers("selected_location")
  location?.let { geoPoint ->
    this.addMarker(
        MarkerOptions(
            tag = "selected_location",
            coordinate = geoPoint,
            pinImage = ImageFactory.fromResource(R.drawable.ic_marker_icon)))
  }
}

@Preview
@Composable
fun mapPreview() {
  Box(modifier = Modifier.fillMaxSize()) {}
}
