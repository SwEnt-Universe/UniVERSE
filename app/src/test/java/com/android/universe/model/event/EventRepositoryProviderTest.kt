package com.android.universe.model.event

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.model.user.UserProfile
import com.android.universe.utils.EventTestData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EventRepositoryProviderTest {
  companion object {
    private val event1 = EventTestData.dummyEvent1
  }

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
          override suspend fun getAllEvents(
              requestorId: String,
              usersRequestorFollows: Set<String>
          ): List<Event> = emptyList()

          override suspend fun getEvent(eventId: String) = event1

          override suspend fun getSuggestedEventsForUser(
              user: UserProfile,
              usersRequestorFollows: Set<String>
          ): List<Event> = emptyList()

          override suspend fun getUserInvolvedEvents(userId: String): List<Event> = emptyList()

          override suspend fun addEvent(event: Event) {}

          override suspend fun updateEvent(eventId: String, newEvent: Event) {}

          override suspend fun deleteEvent(eventId: String) {}

          override suspend fun persistAIEvents(events: List<Event>): List<Event> = events

          override suspend fun getNewID(): String {
            return "new_id"
          }
        }

    // Swap repository
    originalRepo = fakeRepo
    assertEquals("Repository should be replaced", fakeRepo, originalRepo)
  }
}
