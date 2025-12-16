package com.android.universe.model.event

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.model.tag.Tag
import com.android.universe.utils.EventTestData
import com.android.universe.utils.FirestoreEventTest
import com.android.universe.utils.UserTestData
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EventRepositoryFirestoreTest : FirestoreEventTest() {
  private lateinit var eventRepository: EventRepository

  @Before
  override fun setUp() = runTest {
    super.setUp()
    eventRepository = createInitializedRepository()
  }

  companion object {
    private val event1 = EventTestData.dummyEvent1
    private val event2 = EventTestData.dummyEvent2
    private val event3 = EventTestData.dummyEvent3
  }

  @Test
  fun canAddEventAndRetrieve() = runTest {
    eventRepository.addEvent(event1)
    val resultEvent = eventRepository.getEvent(event1.id)
    assertEquals(event1, resultEvent)
  }

  @Test
  fun canAddMultipleEventAndRetrieveAll() = runTest {
    eventRepository.addEvent(event1)
    eventRepository.addEvent(event2)
    eventRepository.addEvent(event3)

    val resultEvent1 = eventRepository.getEvent(event1.id)
    val resultEvent2 = eventRepository.getEvent(event2.id)
    val resultEvent3 = eventRepository.getEvent(event3.id)

    assertEquals(event1, resultEvent1)
    assertEquals(event2, resultEvent2)
    assertEquals(event3, resultEvent3)
  }

  @Test
  fun canRetrieveAllTheEventWithGetAll() = runTest {
    eventRepository.addEvent(event1)
    eventRepository.addEvent(event2)
    eventRepository.addEvent(event3)

    val result = eventRepository.getAllEvents("anyUser", emptySet()).toSet()

    assertEquals(3, result.size)

    val expectedSet = setOf(event1, event2, event3)

    assertEquals(expectedSet, result)
  }

  @Test
  fun getAllEvents_respectsVisibilityLogic() = runTest {
    val creatorId = "creator-123"
    val strangerId = "stranger-999"

    val publicEvent = event1.copy(id = "pub", creator = creatorId, isPrivate = false)
    val privateEvent = event2.copy(id = "priv", creator = creatorId, isPrivate = true)

    eventRepository.addEvent(publicEvent)
    eventRepository.addEvent(privateEvent)

    val resultStranger = eventRepository.getAllEvents(strangerId, emptySet())
    assertEquals(1, resultStranger.size)
    assertEquals("pub", resultStranger[0].id)

    val resultFollower = eventRepository.getAllEvents(strangerId, setOf(creatorId))
    assertEquals(2, resultFollower.size)

    val resultCreator = eventRepository.getAllEvents(creatorId, emptySet())
    assertEquals(2, resultCreator.size)
  }

  @Test(expected = NoSuchElementException::class)
  fun getEventThrowsExceptionWhenEventNotFound() = runTest {
    eventRepository.getEvent("NonExistentEvent")
  }

  @Test
  fun getSuggestedEvents_returnsEventsWithMatchingTags() = runTest {
    val user = UserTestData.SomeTagsUser

    eventRepository.addEvent(EventTestData.SomeTagsEvent)
    eventRepository.addEvent(EventTestData.NoTagsEvent)

    val suggestedEvents = eventRepository.getSuggestedEventsForUser(user)

    assertEquals(1, suggestedEvents.size)
    assertTrue(suggestedEvents.contains(EventTestData.SomeTagsEvent))
    assertFalse(suggestedEvents.contains(EventTestData.NoTagsEvent))
  }

  @Test
  fun getSuggestedEvents_returnsEmptyListWhenNoTagsMatch() = runTest {
    val user = UserTestData.NoTagsUser

    eventRepository.addEvent(EventTestData.SomeTagsEvent)
    eventRepository.addEvent(EventTestData.NoTagsEvent)

    val suggestedEvents = eventRepository.getSuggestedEventsForUser(user)

    assertTrue(suggestedEvents.isEmpty())
  }

  @Test
  fun getSuggestedEvents_respectsVisibilityLogic() = runTest {
    val user = UserTestData.SomeTagsUser.copy(tags = setOf(Tag.ROCK))
    val creatorId = "creator-001"

    val publicEvent =
        event1.copy(
            id = "public-event", creator = creatorId, isPrivate = false, tags = setOf(Tag.ROCK))

    val privateEvent =
        event2.copy(
            id = "private-event", creator = creatorId, isPrivate = true, tags = setOf(Tag.ROCK))

    eventRepository.addEvent(publicEvent)
    eventRepository.addEvent(privateEvent)

    val resultNotFollowing = eventRepository.getSuggestedEventsForUser(user)
    assertEquals(1, resultNotFollowing.size)
    assertEquals("public-event", resultNotFollowing[0].id)

    val userFollowing = user.copy(following = setOf(creatorId))
    val resultFollowing = eventRepository.getSuggestedEventsForUser(userFollowing)

    assertEquals(2, resultFollowing.size)
    assertTrue(resultFollowing.any { it.id == "private-event" })
  }

  @Test
  fun getEventsForUser_returnsEventsWhereUserInvolvedEventsIsParticipantOrCreator() = runTest {
    val userId = "user-123"
    val otherUser = "user-456"

    val createdEvent =
        event1.copy(id = "created-event", creator = userId, participants = emptySet())
    val participatingEvent =
        event2.copy(id = "part-event", creator = otherUser, participants = setOf(userId))
    val unrelatedEvent =
        event3.copy(id = "unrelated-event", creator = otherUser, participants = emptySet())

    eventRepository.addEvent(createdEvent)
    eventRepository.addEvent(participatingEvent)
    eventRepository.addEvent(unrelatedEvent)

    val result = eventRepository.getUserInvolvedEvents(userId)

    assertEquals(2, result.size)
    assertTrue("Should contain event created by user", result.contains(createdEvent))
    assertTrue(
        "Should contain event where user is participant", result.contains(participatingEvent))
    assertFalse("Should not contain unrelated event", result.contains(unrelatedEvent))
  }

  @Test
  fun getEventsForUser_returnsEmptyList_whenUserHasNoAssociatedEventsInvolvedEvents() = runTest {
    val userId = "lonely-user"
    val otherUser = "popular-user"

    val event = event1.copy(creator = otherUser, participants = setOf(otherUser))

    eventRepository.addEvent(event)

    val result = eventRepository.getUserInvolvedEvents(userId)

    assertTrue(result.isEmpty())
  }

  @Test
  fun updateEventReplacesExistingEventCompletely() = runTest {
    eventRepository.addEvent(event1)
    eventRepository.updateEvent(event1.id, event2)
    val resultEvent = eventRepository.getEvent(event1.id)
    assertEquals(event2.copy(id = event1.id), resultEvent)
  }

  @Test
  fun updateEventWhenMultipleEventsExist() = runTest {
    eventRepository.addEvent(event1)
    eventRepository.addEvent(event2)

    eventRepository.updateEvent(event1.id, event3)
    val result = eventRepository.getAllEvents("any", emptySet()).toSet()
    assertEquals(2, result.size)

    val expectedSet = setOf(event3.copy(id = event1.id), event2)
    assertEquals(expectedSet, result)
  }

  @Test(expected = NoSuchElementException::class)
  fun updateNonExistentEventThrowsException() = runTest {
    eventRepository.updateEvent("NonExistentEvent", event1)
  }

  @Test
  fun deleteEvent() = runTest {
    eventRepository.addEvent(event1)
    eventRepository.deleteEvent(event1.id)
    val result = eventRepository.getAllEvents("any", emptySet())
    assertEquals(0, result.size)
  }

  @Test
  fun deleteEventWhenMultipleEventsExist() = runTest {
    eventRepository.addEvent(event1)
    eventRepository.addEvent(event2)
    eventRepository.addEvent(event3)

    eventRepository.deleteEvent(event2.id)
    val result = eventRepository.getAllEvents("any", emptySet()).toSet()
    assertEquals(2, result.size)

    val expectedSet = setOf(event1, event3)
    assertEquals(expectedSet, result)
  }

  @Test(expected = NoSuchElementException::class)
  fun deleteNonExistentEventThrowsException() = runTest {
    eventRepository.deleteEvent("NonExistentEvent")
  }

  @Test
  fun toggleEventParticipation_addsUserIfNotParticipant() = runTest {
    eventRepository.addEvent(event1.copy(participants = emptySet()))
    eventRepository.toggleEventParticipation(event1.id, "user-123")
    val updatedEvent = eventRepository.getEvent(event1.id)
    assertTrue(updatedEvent.participants.contains("user-123"))
  }

  @Test
  fun toggleEventParticipation_removesUserIfParticipant() = runTest {
    eventRepository.addEvent(event1.copy(participants = setOf("user-123")))
    eventRepository.toggleEventParticipation(event1.id, "user-123")
    val updatedEvent = eventRepository.getEvent(event1.id)
    assertFalse(updatedEvent.participants.contains("user-123"))
  }

  @Test
  fun getNewID_returnsAString() = runTest {
    val id = eventRepository.getNewID()
    assertNotNull(id)
    assertTrue(id.isNotEmpty())
  }

  @Test
  fun getNewID_returnsUniqueStrings_onMultipleCalls() = runTest {
    val ids = mutableSetOf<String>()
    repeat(100) {
      val id = eventRepository.getNewID()
      assertNotNull(id)
      assertTrue(id.isNotEmpty())
      assertTrue(!ids.contains(id)) // Ensure uniqueness
      ids.add(id)
    }
    assertEquals(100, ids.size) // Ensure all IDs are unique
  }

  @Test
  fun persistAIEvents_persistsEventsWithNewIDsAndReturnsStoredEvents() = runTest {
    // Given: two dummy events WITHOUT correct IDs (AI events usually come ID-less or dummy IDs)
    val aiEvents = listOf(event1.copy(id = ""), event2.copy(id = ""))

    // When: calling persistAIEvents
    val stored = eventRepository.persistAIEvents(aiEvents)

    // Then: returned list must:
    // 1. Have same size
    assertEquals(2, stored.size)

    // 2. Have newly assigned IDs (NOT the original ones)
    assertTrue(stored[0].id != "")
    assertTrue(stored[1].id != "")

    // 3. IDs should be non-empty + unique
    assertTrue(stored[0].id.isNotBlank())
    assertTrue(stored[1].id.isNotBlank())
    assertTrue(stored[0].id != stored[1].id)

    // 4. Events should be persisted in Firestore
    val fetched1 = eventRepository.getEvent(stored[0].id)
    val fetched2 = eventRepository.getEvent(stored[1].id)

    assertEquals(stored[0], fetched1)
    assertEquals(stored[1], fetched2)
  }
}
