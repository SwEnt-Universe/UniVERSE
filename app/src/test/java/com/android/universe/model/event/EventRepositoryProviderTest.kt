package com.android.universe.model.event

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class EventRepositoryProviderTest {

  @Test
  fun repositoryIsNotNull() {
    val repo = EventRepositoryProvider.repository
    assertNotNull("Repository should not be null", repo)
  }

  @Test
  fun repositoryCanBeReplaced() {
    val originalRepo = EventRepositoryProvider.repository

    val fakeRepo =
        object : EventRepository {
          override suspend fun getAllEvents() = emptyList<Event>()

          override suspend fun getEvent(eventId: String) =
              Event(
                  id = "",
                  title = "",
                  description = null,
                  date = java.time.LocalDateTime.now(),
                  tags = emptySet(),
                  participants = emptySet())

          override suspend fun addEvent(event: Event) {}

          override suspend fun updateEvent(eventId: String, newEvent: Event) {}

          override suspend fun deleteEvent(eventId: String) {}
        }

    // Swap repository
    EventRepositoryProvider.repository = fakeRepo
    assertEquals("Repository should be replaced", fakeRepo, EventRepositoryProvider.repository)

    // Restore original
    EventRepositoryProvider.repository = originalRepo
  }
}
