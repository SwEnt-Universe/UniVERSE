package com.android.universe.model.event

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.utils.EventTestData
import com.android.universe.utils.FirestoreEventTest
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
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

    val result = eventRepository.getAllEvents()

    assertEquals(3, result.size)

    assertEquals(event1, result[0])
    assertEquals(event2, result[1])
    assertEquals(event3, result[2])
  }

  @Test
  fun getEventThrowsExceptionWhenEventNotFound() = runTest {
    try {
      eventRepository.getEvent("NonExistentEvent")
      assert(false) { "Expected NoSuchElementException was not thrown" }
    } catch (e: NoSuchElementException) {
      assert(true)
    } catch (e: Exception) {
      assert(false) { "Unexpected exception type: ${e::class.java}" }
    }
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
    val result = eventRepository.getAllEvents()
    assertEquals(2, result.size)

    assertEquals(event3.copy(id = event1.id), result[0])
    assertEquals(event2, result[1])
  }

  @Test
  fun updateNonExistentEventThrowsException() = runTest {
    try {
      eventRepository.updateEvent("NonExistentEvent", event1)
      assert(false) { "Expected NoSuchElementException was not thrown" }
    } catch (e: NoSuchElementException) {
      assert(true)
    } catch (e: Exception) {
      assert(false) { "Unexpected exception type: ${e::class.java}" }
    }
  }

  @Test
  fun deleteEvent() = runTest {
    eventRepository.addEvent(event1)
    eventRepository.deleteEvent("1")
    val result = eventRepository.getAllEvents()
    assertEquals(0, result.size)
  }

  @Test
  fun deleteEventWhenMultipleEventsExist() = runTest {
    eventRepository.addEvent(event1)
    eventRepository.addEvent(event2)
    eventRepository.addEvent(event3)

    eventRepository.deleteEvent(event2.id)
    val result = eventRepository.getAllEvents()
    assertEquals(2, result.size)

    assertEquals(event1, result[0])
    assertEquals(event3, result[1])
  }

  @Test
  fun deleteNonExistentEventThrowsException() = runTest {
    try {
      eventRepository.deleteEvent("NonExistentEvent")
      assert(false) { "Expected NoSuchElementException was not thrown" }
    } catch (e: NoSuchElementException) {
      assert(true)
    } catch (e: Exception) {
      assert(false) { "Unexpected exception type: ${e::class.java}" }
    }
  }

  @Test
  fun getNewID_returnsAString() = runTest {
    val id = eventRepository.getNewID()
    assertNotNull(id)
    assert(id.isNotEmpty())
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
