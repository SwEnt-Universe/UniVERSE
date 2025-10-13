package com.android.universe.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.universe.model.location.LocationRepository
import com.android.universe.model.map.MapUiState
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.location.LocationProvider
import com.tomtom.sdk.map.display.camera.CameraOptions
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * ViewModel for map-related UI logic.
 *
 * Exposes a shared flow of camera commands (`CameraOptions`) that the UI observes to update the map
 * (position, zoom, tilt, rotation).
 *
 * The initial camera position is emitted from init.
 */
class MapViewModel(private val locationRepository: LocationRepository) : ViewModel() {

  // Use replay=1 so new collectors immediately get the latest camera command.
  private val _cameraCommands =
      MutableSharedFlow<CameraOptions>(replay = 1, extraBufferCapacity = 1)
  val cameraCommands = _cameraCommands.asSharedFlow()

  private val _uiState = MutableStateFlow<MapUiState>(MapUiState.Idle)
  val uiState = _uiState.asStateFlow()

  // Expose the underlying location provider for the map integration
  val locationProvider: LocationProvider?
    get() = locationRepository.getLocationProvider()

  private var trackingJob: Job? = null

  init {
    loadLastKnownLocation()
  }

  /** Loads the last known location from the repository. */
  fun loadLastKnownLocation() {
    if (!locationRepository.hasLocationPermission()) {
      _uiState.value = MapUiState.PermissionRequired
      return
    }

    _uiState.value = MapUiState.Loading

    locationRepository.getLastKnownLocation(
        onSuccess = { location -> _uiState.value = MapUiState.Success(location) },
        onFailure = { _uiState.value = MapUiState.Error("Unable to get location") })
  }

  /**
   * Centers the map on [point] at the given [zoom].
   *
   * Emits a camera command with zero tilt and rotation (top-down, north-up).
   */
  fun centerOn(point: GeoPoint, zoom: Double = 10.0) {
    _cameraCommands.tryEmit(
        CameraOptions(position = point, zoom = zoom, tilt = 0.0, rotation = 0.0))
  }

  /** Starts tracking the user's location. Emits location updates to userLocation StateFlow. */
  fun startLocationTracking() {
    if (!locationRepository.hasLocationPermission()) {
      _uiState.value = MapUiState.PermissionRequired
      return
    }

    if (_uiState.value is MapUiState.Tracking) return

    trackingJob =
        viewModelScope.launch {
          _uiState.value = MapUiState.Tracking
          try {
            locationRepository
                .startLocationTracking()
                .catch { e ->
                  _uiState.value = MapUiState.Error(e.message ?: "Location tracking failed")
                }
                .collect { location -> _uiState.value = MapUiState.Success(location) }
          } finally {}
        }
  }

  /** Stops location tracking. */
  fun stopLocationTracking() {
    trackingJob?.cancel()
    trackingJob = null
    _uiState.value = MapUiState.Idle
  }

  /**
   * Convenience method for development, to center the map on Lausanne, Switzerland. Coordinates:
   * 46.5196° N, 6.5685° E Default zoom level is set to 10.0.
   */
  fun centerOnLausanne() = centerOn(GeoPoint(46.5196, 6.5685), zoom = 10.0)

  override fun onCleared() {
    super.onCleared()
    stopLocationTracking()
  }
}
