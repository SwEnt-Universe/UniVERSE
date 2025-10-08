// MapViewModel.kt
package com.android.universe.ui.map

import androidx.lifecycle.ViewModel
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.map.display.camera.CameraOptions
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * ViewModel for map-related UI logic.
 *
 * Exposes a shared flow of camera commands (`CameraOptions`) that the UI observes to update the map
 * (position, zoom, tilt, rotation).
 *
 * The initial camera position is emitted from init to keep unidirectional flow: ViewModel ->
 * Activity -> UI.
 */
class MapViewModel : ViewModel() {

  // Use replay=1 so new collectors immediately get the latest camera command.
  private val _cameraCommands =
      MutableSharedFlow<CameraOptions>(replay = 1, extraBufferCapacity = 1)
  val cameraCommands = _cameraCommands.asSharedFlow()

  init {
    // Emit initial camera once ViewModel is created (e.g., app entry to the screen).
    // Replace with user location or injected defaults as needed.
    centerOnLausanne()
  }

  /**
   * Emits a camera command to center the map on the given [point] with the specified [zoom] level.
   * The camera will have no tilt or rotation.
   */
  fun centerOn(point: GeoPoint, zoom: Double = 10.0) {
    _cameraCommands.tryEmit(
        CameraOptions(position = point, zoom = zoom, tilt = 0.0, rotation = 0.0))
  }

  /**
   * Convenience method for development, to center the map on Lausanne, Switzerland. Coordinates:
   * 46.5196° N, 6.5685° E Default zoom level is set to 10.0.
   */
  fun centerOnLausanne() = centerOn(GeoPoint(46.5196, 6.5685), zoom = 10.0)
}
