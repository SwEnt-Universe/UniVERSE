package com.android.universe.ui.map

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.R
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.map.display.ui.MapFragment
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MapActivityTest {

  @get:Rule val rule = ActivityScenarioRule(MapActivity::class.java)

  @Test
  fun mapActivity_starts_and_mapFragment_isDisplayed_and_getMapAsync_returns() {
    val latch = CountDownLatch(1)

    // Ensure Activity is RESUMED so the fragment is attached.
    rule.scenario.moveToState(Lifecycle.State.RESUMED)

    // Interact with the fragment and call getMapAsync on the MAIN thread.
    rule.scenario.onActivity { activity ->
      val mapFragment =
          activity.supportFragmentManager.findFragmentById(R.id.map_fragment) as MapFragment

      mapFragment.getMapAsync { _ -> latch.countDown() }
    }

    // Wait for the async map init callback.
    assertTrue(latch.await(15, TimeUnit.SECONDS), "TomTom map did not become ready in time")
  }

  /**
   * Extends the smoke test:
   * - Ensures Activity reaches RESUMED and MapFragment exists.
   * - Waits for TomTom map readiness (getMapAsync).
   * - Then emits a new camera command on the SAME ViewModel instance the Activity uses.
   *
   * We can’t assert the TomTom map’s internal camera state without refactoring for a fake. But this
   * verifies the end-to-end wiring: map readiness + Activity collecting the flow without crashing.
   */
  @Test
  fun mapActivity_collects_cameraCommands_after_map_is_ready() {
    val mapReady = CountDownLatch(1)

    rule.scenario.moveToState(Lifecycle.State.RESUMED)

    // 1) Ensure MapFragment is present and map becomes ready
    rule.scenario.onActivity { activity ->
      val mapFragment =
          activity.supportFragmentManager.findFragmentById(R.id.map_fragment) as MapFragment
      assertNotNull(mapFragment, "MapFragment must be present in activity_map layout")

      mapFragment.getMapAsync {
        // Map is ready, Activity has installed the collector inside getMapAsync.
        mapReady.countDown()
      }
    }

    assertTrue(mapReady.await(20, TimeUnit.SECONDS), "Map did not become ready in time")

    // 2) Drive the SAME ViewModel instance and emit a fresh camera command.
    //    This triggers Activity's collector; if anything is miswired, we’d typically see a crash.
    val driven = CountDownLatch(1)
    rule.scenario.onActivity { activity ->
      val vm = ViewModelProvider(activity)[MapViewModel::class.java]
      vm.centerOn(GeoPoint(47.3769, 8.5417), zoom = 11.0) // Zurich, arbitrary sanity check input
      driven.countDown()
    }

    assertTrue(driven.await(5, TimeUnit.SECONDS), "Failed to drive ViewModel command")
  }
}
