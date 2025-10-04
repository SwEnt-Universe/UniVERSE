package com.android.universe.ui.map

import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.map.display.camera.CameraOptions
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MapViewModelTest {

    @Test
    fun centerOn_emitsCameraOptions_withProvidedPointAndZoom() = runTest {
        val vm = MapViewModel()
        val p = GeoPoint(10.0, 20.0)
        val zoom = 12.5

        // Because we used extraBufferCapacity=1 and tryEmit, the latest value is available immediately.
        vm.centerOn(p, zoom)

        val emitted: CameraOptions = vm.cameraCommands.first()

        assertEquals(p, emitted.position)
        assertEquals(zoom, emitted.zoom!!, 1e-9)
        assertEquals(0.0, emitted.tilt ?: 0.0, 1e-9)
        assertEquals(0.0, emitted.rotation ?: 0.0, 1e-9)
    }

    @Test
    fun centerOnLausanne_emitsExpectedDefaults() = runTest {
        val vm = MapViewModel()

        vm.centerOnLausanne()

        val emitted: CameraOptions = vm.cameraCommands.first()

        // From your VM
        val expected = GeoPoint(46.5196, 6.5685)
        assertEquals(expected, emitted.position)
        assertEquals(10.0, emitted.zoom ?: 0.0, 1e-9)
        assertEquals(0.0, emitted.tilt ?: 0.0, 1e-9)
        assertEquals(0.0, emitted.rotation ?: 0.0, 1e-9)
    }
}
