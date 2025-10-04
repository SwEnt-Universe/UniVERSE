package com.android.universe.ui.map

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.map.display.camera.CameraOptions

/**
 * Simple VM that emits camera commands.
 */
class MapViewModel : ViewModel() {

	// Camera commands the Activity can react to
	private val _cameraCommands = MutableSharedFlow<CameraOptions>(extraBufferCapacity = 1)
	val cameraCommands = _cameraCommands.asSharedFlow()

	fun centerOn(point: GeoPoint, zoom: Double = 10.0) {
		_cameraCommands.tryEmit(
			CameraOptions(
				position = point,
				zoom = zoom,
				tilt = 0.0,
				rotation = 0.0
			)
		)
	}

	// Convenience
	fun centerOnLausanne() = centerOn(
		GeoPoint(46.5196, 6.5685), zoom = 10.0)
}
