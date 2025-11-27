package com.android.universe.ui.map

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.model.location.Location
import com.android.universe.utils.EventTestData
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.map.display.marker.Marker
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MarkerLogicTest {

    // 1. Setup Data Stubs
    // We mock Marker because we can't instantiate the real TomTom Marker easily
    private val markerA = mockk<Marker>(relaxed = true)
    private val eventA = EventTestData.dummyEvent1.copy(id = "A")
    private val eventB = EventTestData.dummyEvent1.copy(id = "B")
    private val markerB = mockk<Marker>(relaxed = true)
    private val eventBBis = EventTestData.dummyEvent1.copy(id = "B", location = Location(0.0, 0.0))

    // We create a UI Model for Event B (the new one coming in)
    private val uiModelB = MapMarkerUiModel(
        event = eventB,
        iconResId = 123,
        position = GeoPoint(0.0, 0.0) // Use real GeoPoint if possible, or mock
    )

    // The click position does not matter when we test this exact logic.
    // Basically event location are took from the click position in the first place
    private val uiModelBBis = MapMarkerUiModel(
        event = eventBBis,
        iconResId = 123,
        position = GeoPoint(0.0, 0.0)
    )

    @Before
    fun setup() {
        // 2. Mock the Static Image Cache
        // This prevents the "MarkerImageCache.get" call from crashing
        mockkObject(MarkerImageCache)
        coEvery { MarkerImageCache.get(any()) } returns mockk() // Return a fake image
    }

    @After
    fun tearDown() {
        // Clean up static mocks to avoid leaking into other tests
        unmockkObject(MarkerImageCache)
    }

    @Test
    fun `markerLogic should remove old events and add new ones`() = runTest {

        // The map currently has Event A
        val currentMap = mutableMapOf(markerA to eventA)

        // The new list only has Event B
        val newMarkers = listOf(uiModelB)

        // WHEN
        val result = markerLogic(currentMap, newMarkers)

        // THEN unpack the Triple
        val (optionsToAdd, markersToRemove, newEvents) = result

        // 1. Verify Removals
        // Event A was in the map, but not in the new list. It should be removed.
        assertEquals(1, markersToRemove.size)
        assertTrue(markersToRemove.contains(markerA))

        // 2. Verify Additions
        // Event B was in the new list, but not in the map. It should be added.
        assertEquals(1, optionsToAdd.size)
        // Check if the 'tag' we set in logic is correct
        assertEquals("event", optionsToAdd.first().tag)

        // 3. Verify Event List tracking
        assertEquals(1, newEvents.size)
        assertTrue(newEvents.contains(eventB))
    }
    @Test
    fun `markerLogic should remove old events and add new ones if location changes`() = runTest {

        // The map currently has Event A
        val currentMap = mutableMapOf(markerB to eventB)

        // The new list only has Event B
        val newMarkers = listOf(uiModelBBis)

        // WHEN
        val result = markerLogic(currentMap, newMarkers)

        // THEN unpack the Triple
        val (optionsToAdd, markersToRemove, newEvents) = result

        // 1. Verify Removals
        // Event A was in the map, but not in the new list. It should be removed.
        assertEquals(1, markersToRemove.size)
        assertTrue(markersToRemove.contains(markerB))

        // 2. Verify Additions
        // Event B was in the new list, but not in the map. It should be added.
        assertEquals(1, optionsToAdd.size)
        // Check if the 'tag' we set in logic is correct
        assertEquals("event", optionsToAdd.first().tag)

        // 3. Verify Event List tracking
        assertEquals(1, newEvents.size)
        assertTrue(newEvents.contains(eventBBis))
    }

    @Test
    fun `markerLogic should do nothing if lists are identical`() = runTest {
        // GIVEN
        val currentMap = mutableMapOf(markerA to eventA)
        // The new list matches the old list exactly
        val newMarkers = listOf(MapMarkerUiModel(event = eventA, iconResId = 111, position = mockk()))

        // WHEN
        val result = markerLogic(currentMap, newMarkers)

        // THEN
        assertTrue(result.first.isEmpty())  // optionsToAdd
        assertTrue(result.second.isEmpty()) // markersToRemove
    }
}