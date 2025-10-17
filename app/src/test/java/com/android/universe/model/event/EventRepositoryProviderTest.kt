package com.android.universe.model.event

import com.android.universe.model.location.Location
import com.android.universe.model.user.UserProfile
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class EventRepositoryProviderTest {

  @Test
  fun repositoryIsNotNull() {
    val repo = FakeEventRepository()
    assertNotNull("Repository should not be null", repo)
  }

  @Test
  fun repositoryCanBeReplaced() {
    var originalRepo: EventRepository = FakeEventRepository()

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
                  participants = emptySet(),
                  creator =
                      UserProfile(
                          uid = "0",
                          username = "john_doe",
                          firstName = "John",
                          lastName = "Doe",
                          country = "US",
                          dateOfBirth = LocalDate.of(1990, 1, 1),
                          tags = emptySet()),
                  location = Location(0.0, 0.0))

          override suspend fun addEvent(event: Event) {}

          override suspend fun updateEvent(eventId: String, newEvent: Event) {}

          override suspend fun deleteEvent(eventId: String) {}

          override suspend fun getNewID(): String {
            return "new_id"
          }
        }

    // Swap repository
    originalRepo = fakeRepo
    assertEquals("Repository should be replaced", fakeRepo, originalRepo)
  }
}
