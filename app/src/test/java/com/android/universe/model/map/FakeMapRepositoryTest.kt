package com.android.universe.model.map

import com.android.universe.model.event.Event
import com.android.universe.model.location.Location
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class FakeMapRepositoryTest {

  private lateinit var repository: FakeMapRepository

  @Before
  fun setup() {
    repository = FakeMapRepository()
  }

  @Test
  fun createMarker_addsMarker() = runBlocking {
    val initialCount = repository.getMarkers().size

    repository.createMarker(Location(47.0, 8.0), "Concert", "event1", null)

    val markers = repository.getMarkers()
    assertEquals(initialCount + 1, markers.size)
    val marker = markers.last()
    assertEquals("event1", marker.eventId)
    assertEquals("Concert", marker.eventTitle)
    assertEquals(47.0, marker.location.latitude)
    assertEquals(8.0, marker.location.longitude)
  }

  @Test
  fun createMarkers_addsMultipleMarkers() = runBlocking {
    val events =
        listOf(
            Event(id = "event1", title = "Concert", location = Location(47.0, 8.0)),
            Event(id = "event2", title = "Market", location = Location(47.1, 8.1)))
    repository.createMarkers(events)

    val markers = repository.getMarkers()
    assertEquals(2, markers.size)
    assertEquals("event1", markers[0].eventId)
    assertEquals("event2", markers[1].eventId)
  }

  @Test
  fun updateMarker_replacesExistingMarker() = runBlocking {
    repository.createMarker(Location(47.0, 8.0), "Old", "event1", null)

    repository.updateMarker("event1", Location(48.0, 9.0), "New")

    val markers = repository.getMarkers()
    assertEquals(1, markers.size)
    val updated = markers.first()
    assertEquals("event1", updated.eventId)
    assertEquals("New", updated.eventTitle)
    assertEquals(48.0, updated.location.latitude)
    assertEquals(9.0, updated.location.longitude)
  }

  @Test
  fun deleteMarker_removesMarker() = runBlocking {
    repository.createMarker(Location(47.0, 8.0), "Concert", "event1", null)
    repository.createMarker(Location(47.1, 8.1), "Market", "event2", null)

    repository.deleteMarker("event1")

    val markers = repository.getMarkers()
    assertEquals(1, markers.size)
    assertEquals("event2", markers[0].eventId)
  }

  @Test
  fun deleteMarker_nonExistentMarker_doesNothing() = runBlocking {
    repository.createMarker(Location(47.0, 8.0), "Concert", "event1", null)

    repository.deleteMarker("nonexistent")

    val markers = repository.getMarkers()
    assertEquals(1, markers.size)
    assertEquals("event1", markers[0].eventId)
  }

  @Test
  fun getMarkers_returnsCopy_notMutable() = runBlocking {
    repository.createMarker(Location(47.0, 8.0), "Concert", "event1", null)
    val markers = repository.getMarkers()
    markers.toMutableList().clear()

    val original = repository.getMarkers()
    assertEquals(1, original.size)
  }

  @Test
  fun updateMarker_nonExistent_createsNewMarker() = runBlocking {
    repository.updateMarker("eventX", Location(49.0, 10.0), "New Event")

    val markers = repository.getMarkers()
    assertEquals(1, markers.size)
    val marker = markers.first()
    assertEquals("eventX", marker.eventId)
    assertEquals("New Event", marker.eventTitle)
  }
}
