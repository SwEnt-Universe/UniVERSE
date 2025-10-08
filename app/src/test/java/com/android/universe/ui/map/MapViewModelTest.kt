// app/src/test/java/com/android/universe/ui/map/MapViewModelTest.kt
package com.android.universe.ui.map

import app.cash.turbine.test
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MapViewModelTest {

  @Test
  fun `initial camera is emitted via replay and is Lausanne with zoom 10`() = runTest {
    val vm = MapViewModel()

    vm.cameraCommands.test {
      // Because replay=1, the first awaitItem() gives us the latest value immediately.
      val initial = awaitItem()
      assertApprox(46.5196, initial.position!!.latitude)
      assertApprox(6.5685, initial.position!!.longitude)
      assertApprox(10.0, initial.zoom!!)
      assertApprox(0.0, initial.tilt!!)
      assertApprox(0.0, initial.rotation!!)
      cancelAndIgnoreRemainingEvents()
    }
  }

  private fun assertApprox(expected: Double, actual: Double, eps: Double = 1e-6) {
    assertTrue(
        kotlin.math.abs(expected - actual) <= eps, "expected≈$expected but was $actual (±$eps)")
  }
}
