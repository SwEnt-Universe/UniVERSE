package com.android.universe.model.event

import androidx.test.ext.junit.runners.AndroidJUnit4
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

    val result = eventRepository.getAllEvents().toSet()

    assertEquals(3, result.size)

    val expectedSet = setOf(event1, event2, event3)

    assertEquals(expectedSet, result)
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
    val result = eventRepository.getAllEvents().toSet()
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
    val result = eventRepository.getAllEvents()
    assertEquals(0, result.size)
  }

  @Test
  fun deleteEventWhenMultipleEventsExist() = runTest {
    eventRepository.addEvent(event1)
    eventRepository.addEvent(event2)
    eventRepository.addEvent(event3)

    eventRepository.deleteEvent(event2.id)
    val result = eventRepository.getAllEvents().toSet()
    assertEquals(2, result.size)

    val expectedSet = setOf(event1, event3)
    assertEquals(expectedSet, result)
  }

  @Test(expected = NoSuchElementException::class)
  fun deleteNonExistentEventThrowsException() = runTest {
    eventRepository.deleteEvent("NonExistentEvent")
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
}
