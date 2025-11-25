package com.android.universe.ui.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.util.Log
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.universe.BuildConfig
import com.android.universe.R
import com.android.universe.di.DefaultDP
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
import com.tomtom.sdk.map.display.image.ImageFactory
import com.tomtom.sdk.map.display.location.LocationMarkerOptions
import com.tomtom.sdk.map.display.marker.MarkerOptions
import com.tomtom.sdk.map.display.style.StyleDescriptor
import com.tomtom.sdk.map.display.ui.MapView
import com.tomtom.sdk.map.display.ui.currentlocation.CurrentLocationButton
import com.tomtom.sdk.map.display.ui.logo.LogoView
import kotlinx.coroutines.withContext

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
      bottomBar = { NavigationBottomMenu(selectedTab = Tab.Map, onTabSelected = onTabSelected) }) {
          padding ->
        Box(
            modifier =
                Modifier.fillMaxSize()
                    .then(
                        if (uiState.isMapInteractive)
                            Modifier.testTag(MapScreenTestTags.INTERACTABLE)
                        else Modifier)) {
              TomTomMapComposable(
                  modifier = Modifier.fillMaxSize().layerBackdrop(layerBackdrop),
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
                    onToggleEventParticipation = { viewModel.toggleEventParticipation(event) })
              }
            }
      }
}

// --- HELPER COMPOSABLES & EXTENSIONS ---

@Composable
fun TomTomMapComposable(modifier: Modifier = Modifier, onMapReady: (TomTomMap) -> Unit) {
  val mapView = rememberMapViewWithLifecycle(onMapReady)

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

  DisposableEffect(lifecycleOwner) {
    val lifecycle = lifecycleOwner.lifecycle
    val observer = LifecycleEventObserver { _, event ->
      when (event) {
        Lifecycle.Event.ON_CREATE -> mapView.onCreate(Bundle())
        Lifecycle.Event.ON_START -> mapView.onStart()
        Lifecycle.Event.ON_RESUME -> mapView.onResume()
        Lifecycle.Event.ON_PAUSE -> mapView.onPause()
        Lifecycle.Event.ON_STOP -> mapView.onStop()
        else -> { /* DO NOTHING */ }
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

  this.addMapClickListener {
      onMapClick()
      true
  }

    this.addMapLongClickListener { geoPoint ->
        onMapLongClick(geoPoint)
        true
    }

    this.addMarkerClickListener { clickedMarker -> onMarkerClick(clickedMarker.id) }

    this.addCameraSteadyListener {
        onCameraChange(this.cameraPosition.position, this.cameraPosition.zoom)
    }
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

private suspend fun TomTomMap.syncEventMarkers(
    markers: List<MapMarkerUiModel>,
    markerMap: MutableMap<UniqueId, Event>
) {
  val pin = ImageFactory.fromResource(R.drawable.ic_marker_icon)
  val optionsAndEvents = withContext(DefaultDP.default) {
      markers.map {
          val eventPicture = it.event.eventPicture
          val image = if (eventPicture != null) ImageFactory.fromBitmap(BitmapFactory.decodeByteArray(eventPicture, 0, eventPicture.size)) else ImageFactory.fromResource(it.iconResId)
          Triple(it, image, it.event)
      }
  }
  this@syncEventMarkers.removeMarkers("event")
  markerMap.clear()

  optionsAndEvents.forEach { (markerModel, image, event) ->
    val markerOptions =
        MarkerOptions(
            tag = "event",
            coordinate = markerModel.position,
            pinImage = pin,
            pinIconImage = image
            )
    val addedMarker = this@syncEventMarkers.addMarker(markerOptions)
    markerMap[addedMarker.id] = event
  }
}

private suspend fun TomTomMap.syncSelectedLocationMarker(location: GeoPoint?) {
  this.removeMarkers("selected_location")
  val image = withContext(DefaultDP.default) { ImageFactory.fromResource(R.drawable.ic_marker_icon)}
  location?.let { geoPoint ->
    this.addMarker(
        MarkerOptions(
            tag = "selected_location",
            coordinate = geoPoint,
            pinImage = image))
  }
}

@Preview
@Composable
fun mapPreview() {
  Box(modifier = Modifier.fillMaxSize()) {}
}
