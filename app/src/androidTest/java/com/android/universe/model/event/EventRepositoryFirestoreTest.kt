package com.android.universe.model.event

import com.android.universe.model.Tag
import com.android.universe.model.location.Location
import com.android.universe.model.user.UserProfile
import com.android.universe.utils.FirestoreEventTest
import java.time.LocalDateTime
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class EventRepositoryFirestoreTest : FirestoreEventTest() {
  var eventRepository = createInitializedRepository()

  @Before override fun setUp() = runBlocking { super.setUp() }

  private val userProfile1 =
      UserProfile(
          uid = "0",
          username = "Bobbb",
          firstName = "Test",
          lastName = "User",
          country = "Switzerland",
          description = "Just a test user",
          dateOfBirth = java.time.LocalDate.of(1990, 1, 1),
          tags = setOf(Tag.MUSIC, Tag.METAL))

  private val userProfile2 =
      UserProfile(
          uid = "1",
          username = "Al",
          firstName = "second",
          lastName = "User2",
          country = "France",
          description = "a second user",
          dateOfBirth = java.time.LocalDate.of(2005, 12, 15),
          tags = setOf(Tag.TENNIS))

  private val userProfile3 =
      UserProfile(
          uid = "2",
          username = "Rocky",
          firstName = "third",
          lastName = "User3",
          country = "Portugal",
          description = "a third user",
          dateOfBirth = java.time.LocalDate.of(2012, 9, 12),
          tags = setOf(Tag.ROLE_PLAYING_GAMES, Tag.ARTIFICIAL_INTELLIGENCE))

  private val event1 =
      Event(
          id = "1",
          title = "Morning Run at the Lake",
          description = "Join us for a casual 5km run around the lake followed by coffee.",
          date = LocalDateTime.of(2025, 10, 15, 7, 30),
          tags = setOf(Tag.JAZZ, Tag.COUNTRY),
          participants = setOf(userProfile1, userProfile2),
          creator = userProfile1,
          location = Location(latitude = 46.5196535, longitude = 6.6322734))

  private val event2 =
      Event(
          id = "2",
          title = "Tech Hackathon 2025",
          date = LocalDateTime.of(2025, 11, 3, 9, 0),
          tags = setOf(Tag.PROGRAMMING, Tag.ARTIFICIAL_INTELLIGENCE, Tag.BOAT),
          participants = setOf(userProfile3),
          creator = userProfile3,
          location = Location(latitude = 46.5196535, longitude = 6.6322734))

  private val event3 =
      Event(
          id = "3",
          title = "Art & Wine Evening",
          description = "Relaxed evening mixing painting, wine, and music.",
          date = LocalDateTime.of(2025, 10, 22, 19, 0),
          tags = setOf(Tag.SCULPTURE, Tag.MUSIC),
          participants = setOf(userProfile2),
          creator = userProfile2,
          location = Location(latitude = 46.5196535, longitude = 6.6322734))

  private fun userProfileEquals(userA: UserProfile?, userB: UserProfile?): Boolean {
    if (userA == null || userB == null) return false
    return userA.uid == userB.uid &&
        userA.username == userB.username &&
        userA.firstName == userB.firstName &&
        userA.lastName == userB.lastName &&
        userA.country == userB.country &&
        userA.description == userB.description &&
        userA.dateOfBirth == userB.dateOfBirth &&
        userA.tags == userB.tags
  }

  private fun participantsEqual(setA: Set<UserProfile>?, setB: Set<UserProfile>?): Boolean {
    if (setA == null || setB == null) return false
    if (setA.size != setB.size) return false
    return setA.all { a -> setB.any { b -> userProfileEquals(a, b) } }
  }

  private fun eventEquals(eventA: Event?, eventB: Event?): Boolean {
    if (eventA == null || eventB == null) return false
    return eventA.id == eventB.id &&
        eventA.title == eventB.title &&
        eventA.description == eventB.description &&
        eventA.date == eventB.date &&
        eventA.tags == eventB.tags &&
        userProfileEquals(eventA.creator, eventB.creator) &&
        participantsEqual(eventA.participants, eventB.participants)
  }

  @Test
  fun canAddEventAndRetrieve() = runTest {
    eventRepository.addEvent(event1)
    val resultEvent = eventRepository.getEvent("1")
    assertTrue(eventEquals(event1, resultEvent))
  }

  @Test
  fun canAddMultipleEventAndRetrieveAll() = runTest {
    eventRepository.addEvent(event1)
    eventRepository.addEvent(event2)
    eventRepository.addEvent(event3)

    val resultEvent1 = eventRepository.getEvent("1")
    val resultEvent2 = eventRepository.getEvent("2")
    val resultEvent3 = eventRepository.getEvent("3")

    assertTrue(eventEquals(event1, resultEvent1))
    assertTrue(eventEquals(event2, resultEvent2))
    assertTrue(eventEquals(event3, resultEvent3))
  }

  @Test
  fun canRetrieveAllTheEventWithGetAll() = runTest {
    eventRepository.addEvent(event1)
    eventRepository.addEvent(event2)
    eventRepository.addEvent(event3)

    val result = eventRepository.getAllEvents()

    assertEquals(3, result.size)

    assertTrue(eventEquals(event1, result[0]))
    assertTrue(eventEquals(event2, result[1]))
    assertTrue(eventEquals(event3, result[2]))
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
    eventRepository.updateEvent("1", event2)
    val resultUser = eventRepository.getEvent("1")
    assertTrue(eventEquals(event2, resultUser))
  }

  @Test
  fun updateEventWhenMultipleEventsExist() = runTest {
    eventRepository.addEvent(event1)
    eventRepository.addEvent(event2)

    eventRepository.updateEvent("1", event3)
    val result = eventRepository.getAllEvents()
    assertEquals(2, result.size)

    assertTrue(eventEquals(event3, result[0]))
    assertTrue(eventEquals(event2, result[1]))
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

    eventRepository.deleteEvent("2")
    val result = eventRepository.getAllEvents()
    assertEquals(2, result.size)

    assertTrue(eventEquals(event1, result[0]))
    assertTrue(eventEquals(event3, result[1]))
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
      assert(id.isNotEmpty())
      assert(!ids.contains(id)) // Ensure uniqueness
      ids.add(id)
    }
    assertEquals(100, ids.size) // Ensure all IDs are unique
  }
}
