package com.android.universe.ui.map

import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.R
import com.tomtom.sdk.map.display.ui.MapFragment
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class MapActivityTest {

	@get:Rule
	val rule = ActivityScenarioRule(MapActivity::class.java)

	@Test
	fun mapActivity_starts_and_mapFragment_isDisplayed_and_getMapAsync_returns() {
		val latch = CountDownLatch(1)

		// Ensure Activity is RESUMED so the fragment is attached.
		rule.scenario.moveToState(Lifecycle.State.RESUMED)

		// Interact with the fragment and call getMapAsync on the MAIN thread.
		rule.scenario.onActivity { activity ->
			val mapFragment = activity.supportFragmentManager
				.findFragmentById(R.id.map_fragment) as MapFragment

			mapFragment.getMapAsync { _ ->
				latch.countDown()
			}
		}

		// Wait for the async map init callback.
		assertTrue(latch.await(15, TimeUnit.SECONDS), "TomTom map did not become ready in time")
	}
}