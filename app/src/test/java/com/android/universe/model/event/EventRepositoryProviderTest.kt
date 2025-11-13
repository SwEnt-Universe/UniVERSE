package com.android.universe.model.event

import com.android.universe.model.user.UserProfile
import com.android.universe.utils.EventTestData
import com.google.firebase.firestore.Source
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

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
          override suspend fun getAllEvents(source: Source) = emptyList<Event>()

          override suspend fun getEvent(eventId: String, source: Source) = event1

          override suspend fun getSuggestedEventsForUser(user: UserProfile, source: Source) =
              emptyList<Event>()

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
