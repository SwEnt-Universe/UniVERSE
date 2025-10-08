package com.android.universe.model.map

import com.android.universe.model.location.Location
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.runBlocking
import org.junit.Test

class MapRepositoryProviderTest {

  @Test
  fun repositoryIsNotNull() {
    val repo = MapRepositoryProvider.repository
    assertNotNull("Repository should not be null", repo)
  }

  @Test
  fun repositoryIsPrePopulated() = runBlocking {
    val repo = MapRepositoryProvider.repository as FakeMapRepository
    val markers = repo.getMarkers()
    assert(markers.size >= 2) { "Repository should contain at least 2 pre-populated events" }
  }

  @Test
  fun createMarkerAddsMarker() = runBlocking {
    val repo = MapRepositoryProvider.repository as FakeMapRepository
    val initialSize = repo.getMarkers().size

    repo.createMarker(Location(47.2, 8.2), "Test Event", "test_event", null)

    val markers = repo.getMarkers()
    assertEquals("Repository should have one more marker", initialSize + 1, markers.size)
    assertEquals("New marker tag should be 'test_event'", "test_event", markers.last().eventId)
  }

  @Test
  fun deleteMarkerRemovesMarker() = runBlocking {
    val repo = MapRepositoryProvider.repository as FakeMapRepository
    repo.createMarker(Location(47.3, 8.3), "ToDelete", "delete_me", null)
    val sizeAfterAdd = repo.getMarkers().size

    repo.deleteMarker("delete_me")

    val markers = repo.getMarkers()
    assertEquals("Repository should remove the marker", sizeAfterAdd - 1, markers.size)
    assert(markers.none { it.eventId == "delete_me" })
  }

  @Test
  fun updateMarkerReplacesMarker() = runBlocking {
    val repo = MapRepositoryProvider.repository as FakeMapRepository

    // Add marker
    repo.createMarker(Location(47.4, 8.4), "OldTitle", "update_me", null)
    repo.updateMarker("update_me", Location(47.5, 8.5), "NewTitle")

    val markers = repo.getMarkers()
    val updated = markers.find { it.eventId == "update_me" }
    assertNotNull("Updated marker should exist", updated)
    assertEquals("Updated marker should have new location", 47.5, updated!!.location.latitude)
    assertEquals("Updated marker should have new title", "NewTitle", updated.eventTitle)
  }
}
