package com.android.universe.model.event

import com.android.universe.model.Tag
import com.android.universe.model.location.Location
import com.android.universe.model.user.UserProfile
import com.android.universe.utils.EventTestData
import com.android.universe.utils.UserTestData
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

class FakeEventRepositoryTest {
  companion object {
    private val userA = UserTestData.Alice
    private val userB = UserTestData.Bob
  }

  private lateinit var repository: FakeEventRepository

  @Before
  fun setup() {
    repository = FakeEventRepository()
  }

  @Test
  fun getEvent_throwsException_forNonExistentEvent() = runTest {
    try {
      repository.getEvent("nonexistent")
      fail("Expected NoSuchElementException to be thrown")
    } catch (e: NoSuchElementException) {
      assertEquals(e.message, "No event found with id: nonexistent")
    }
  }

  @Test
  fun getSuggestedEvents_returnsEventsMatchingUserTags() = runTest {
    val user = UserTestData.SomeTagsUser
    val eventA = EventTestData.dummyEvent1.copy(tags = setOf(Tag.ROCK, Tag.POP))
    val eventB = EventTestData.dummyEvent2.copy(tags = setOf(Tag.METAL, Tag.JAZZ))
    val eventC = EventTestData.dummyEvent3.copy(tags = setOf(Tag.TENNIS))

    repository.addEvent(eventA)
    repository.addEvent(eventB)
    repository.addEvent(eventC)

    val suggestedEvents = repository.getSuggestedEventsForUser(user)

    assertTrue(suggestedEvents.contains(eventA))
    assertTrue(suggestedEvents.contains(eventB))
    assertFalse(suggestedEvents.contains(eventC))
  }

  @Test
  fun addEvent_storesEvent_andCanBeRetrieved() = runTest {
    val event =
        Event(
            id = "event-001",
            title = "Morning Run at the Lake",
            description = "A casual 5km run around the lake followed by coffee at the café nearby.",
            date = LocalDateTime.of(2025, 10, 15, 7, 30),
            tags = setOf(Tag.RUNNING, Tag.FITNESS),
            participants = setOf(userA.uid, userB.uid),
            creator = userA.uid,
            location = Location(latitude = 46.5196535, longitude = 6.6322734))
    repository.addEvent(event)

    val result = repository.getEvent("event-001")
    assertNotNull(result)
    assertEquals("event-001", result.id)
    assertEquals("Morning Run at the Lake", result.title)
    assertEquals(
        "A casual 5km run around the lake followed by coffee at the café nearby.",
        result.description)
    assertEquals(LocalDateTime.of(2025, 10, 15, 7, 30), result.date)
    assertEquals(setOf(Tag.RUNNING, Tag.FITNESS), result.tags)
    assertEquals(2, result.participants.size)
    assert(result.participants.contains(userA.uid))
    assert(result.participants.contains(userB.uid))
    assertEquals(result.creator, userA.uid)
  }

  @Test
  fun addEvent_storesEventWithNoDescription_andCanBeRetrieved() = runTest {
    val event =
        Event(
            id = "event-002",
            title = "Evening Cycling Tour",
            date = LocalDateTime.of(2025, 11, 5, 18, 0),
            tags = setOf(Tag.CYCLING, Tag.COUNTRY),
            participants = setOf(userA.uid),
            creator = userA.uid,
            location = Location(latitude = 46.5196535, longitude = 6.6322734))
    repository.addEvent(event)

    val result = repository.getEvent("event-002")
    assertNotNull(result)
    assertEquals("event-002", result.id)
    assertEquals("Evening Cycling Tour", result.title)
    assertNull(result.description)
    assertEquals(LocalDateTime.of(2025, 11, 5, 18, 0), result.date)
    assertEquals(setOf(Tag.CYCLING, Tag.COUNTRY), result.tags)
    assertEquals(1, result.participants.size)
    assert(result.participants.contains(userA.uid))
    assertEquals(result.creator, userA.uid)
  }

  @Test
  fun addEvent_storesEventWithNoParticipants_andCanBeRetrieved() = runTest {
    val event =
        Event(
            id = "event-003",
            title = "Tech Talk on AI",
            description =
                "An insightful talk on the latest advancements in artificial intelligence.",
            date = LocalDateTime.of(2025, 12, 1, 14, 0),
            tags = setOf(Tag.PROGRAMMING, Tag.ARTIFICIAL_INTELLIGENCE),
            creator = userA.uid,
            location = Location(latitude = 37.423021, longitude = -122.086808))
    repository.addEvent(event)

    val result = repository.getEvent("event-003")
    assertNotNull(result)
    assertEquals("event-003", result.id)
    assertEquals("Tech Talk on AI", result.title)
    assertEquals(
        "An insightful talk on the latest advancements in artificial intelligence.",
        result.description)
    assertEquals(LocalDateTime.of(2025, 12, 1, 14, 0), result.date)
    assertEquals(setOf(Tag.PROGRAMMING, Tag.ARTIFICIAL_INTELLIGENCE), result.tags)
    assertEquals(0, result.participants.size)
    assertEquals(result.creator, userA.uid)
  }

  @Test
  fun addEvent_storesMultipleEvents_andAllCanBeRetrieved() = runTest {
    val event1 =
        Event(
            id = "event-001",
            title = "Morning Run at the Lake",
            description = "A casual 5km run around the lake followed by coffee at the café nearby.",
            date = LocalDateTime.of(2025, 10, 15, 7, 30),
            tags = setOf(Tag.RUNNING, Tag.FITNESS),
            participants = setOf(userA.uid, userB.uid),
            creator = userA.uid,
            location = Location(latitude = 46.5196535, longitude = 6.6322734))
    val event2 =
        Event(
            id = "event-002",
            title = "Evening Cycling Tour",
            date = LocalDateTime.of(2025, 11, 5, 18, 0),
            tags = setOf(Tag.CYCLING, Tag.COUNTRY),
            creator = userB.uid,
            location = Location(latitude = 46.5196535, longitude = 6.6322734))
    repository.addEvent(event1)
    repository.addEvent(event2)

    val result = repository.getAllEvents()
    assertEquals(2, result.size)
    assertEquals("event-001", result[0].id)
    assertEquals("event-002", result[1].id)
    assertEquals("Morning Run at the Lake", result[0].title)
    assertEquals("Evening Cycling Tour", result[1].title)
    assertEquals(
        "A casual 5km run around the lake followed by coffee at the café nearby.",
        result[0].description)
    assertNull(result[1].description)
    assertEquals(LocalDateTime.of(2025, 10, 15, 7, 30), result[0].date)
    assertEquals(LocalDateTime.of(2025, 11, 5, 18, 0), result[1].date)
    assertEquals(setOf(Tag.RUNNING, Tag.FITNESS), result[0].tags)
    assertEquals(setOf(Tag.CYCLING, Tag.COUNTRY), result[1].tags)
    assertEquals(2, result[0].participants.size)
    assert(result[0].participants.contains(userA.uid))
    assert(result[0].participants.contains(userB.uid))
    assertEquals(0, result[1].participants.size)
    assertEquals(result[0].creator, userA.uid)
    assertEquals(result[1].creator, userB.uid)
  }

  @Test
  fun addEvent_storesEvent_thenUpdateEvent_editsAndCanBeRetrieved() = runTest {
    val user1 =
        UserProfile(
            uid = "0",
            username = "alice",
            firstName = "Alice",
            lastName = "Smith",
            country = "CH",
            description = "Bio",
            dateOfBirth = LocalDate.of(1990, 1, 1),
            tags = setOf(Tag.MUSIC, Tag.RUNNING))
    val user2 =
        UserProfile(
            uid = "1",
            username = "bob",
            firstName = "Bob",
            lastName = "Jones",
            country = "FR",
            dateOfBirth = LocalDate.of(2000, 8, 11),
            tags = setOf(Tag.COOKING, Tag.FITNESS))
    val event =
        Event(
            id = "event-001",
            title = "Morning Run at the Lake",
            description = "A casual 5km run around the lake followed by coffee at the café nearby.",
            date = LocalDateTime.of(2025, 10, 15, 7, 30),
            tags = setOf(Tag.RUNNING, Tag.FITNESS),
            participants = setOf(userA.uid, userB.uid),
            creator = userA.uid,
            location = Location(latitude = 46.5196535, longitude = 6.6322734))
    repository.addEvent(event)

    val newEvent =
        Event(
            id = "event-001",
            title = "Morning Run and Yoga at the Lake",
            description =
                "A casual 5km run around the lake followed by a relaxing yoga session and coffee at the café nearby.",
            date = LocalDateTime.of(2025, 10, 15, 8, 0),
            tags = setOf(Tag.RUNNING, Tag.FITNESS, Tag.YOGA),
            participants = setOf(userA.uid),
            creator = userA.uid,
            location = Location(latitude = 46.5196535, longitude = 6.6322734))
    repository.updateEvent("event-001", newEvent)

    val result1 = repository.getAllEvents()
    assertEquals(1, result1.size)
    val result2 = repository.getEvent("event-001")
    assertNotNull(result2)
    assertEquals("event-001", result2.id)
    assertEquals("Morning Run and Yoga at the Lake", result2.title)
    assertEquals(
        "A casual 5km run around the lake followed by a relaxing yoga session and coffee at the café nearby.",
        result2.description)
    assertEquals(LocalDateTime.of(2025, 10, 15, 8, 0), result2.date)
    assertEquals(setOf(Tag.RUNNING, Tag.FITNESS, Tag.YOGA), result2.tags)
    assertEquals(1, result2.participants.size)
    assert(result2.participants.contains(userA.uid))
    assertEquals(result2.creator, userA.uid)
  }

  @Test
  fun addEvent_storesEventWithNoDescription_thenUpdateEvent_editsAndCanBeRetrieved() = runTest {
    val user =
        UserProfile(
            uid = "3",
            username = "charlie",
            firstName = "Charlie",
            lastName = "Brown",
            country = "US",
            dateOfBirth = LocalDate.of(1985, 5, 20),
            tags = setOf(Tag.CYCLING, Tag.PHOTOGRAPHY))
    val event =
        Event(
            id = "event-002",
            title = "Evening Cycling Tour",
            date = LocalDateTime.of(2025, 11, 5, 18, 0),
            tags = setOf(Tag.CYCLING, Tag.COUNTRY),
            participants = setOf(userA.uid),
            creator = userA.uid,
            location = Location(latitude = 46.5196535, longitude = 6.6322734))
    repository.addEvent(event)

    val newEvent =
        Event(
            id = "event-002",
            title = "Evening Cycling and Photography Tour",
            description = "An evening cycling tour with stops for photography at scenic spots.",
            date = LocalDateTime.of(2025, 11, 5, 18, 30),
            tags = setOf(Tag.CYCLING, Tag.COUNTRY, Tag.PHOTOGRAPHY),
            participants = setOf(userA.uid),
            creator = userA.uid,
            location = Location(latitude = 46.5196535, longitude = 6.6322734))
    repository.updateEvent("event-002", newEvent)

    val result1 = repository.getAllEvents()
    assertEquals(1, result1.size)
    val result2 = repository.getEvent("event-002")
    assertNotNull(result2)
    assertEquals("event-002", result2.id)
    assertEquals("Evening Cycling and Photography Tour", result2.title)
    assertEquals(
        "An evening cycling tour with stops for photography at scenic spots.", result2.description)
    assertEquals(LocalDateTime.of(2025, 11, 5, 18, 30), result2.date)
    assertEquals(setOf(Tag.CYCLING, Tag.COUNTRY, Tag.PHOTOGRAPHY), result2.tags)
    assertEquals(1, result2.participants.size)
    assert(result2.participants.contains(userA.uid))
    assertEquals(result2.creator, userA.uid)
  }

  @Test
  fun addEvent_storesEventWithNoParticipants_thenUpdateEvent_editsAndCanBeRetrieved() = runTest {
    val user =
        UserProfile(
            uid = "3",
            username = "charlie",
            firstName = "Charlie",
            lastName = "Brown",
            country = "US",
            dateOfBirth = LocalDate.of(1985, 5, 20),
            tags = setOf(Tag.CYCLING, Tag.PHOTOGRAPHY))
    val event =
        Event(
            id = "event-003",
            title = "Tech Talk on AI",
            description =
                "An insightful talk on the latest advancements in artificial intelligence.",
            date = LocalDateTime.of(2025, 12, 1, 14, 0),
            tags = setOf(Tag.PROGRAMMING, Tag.ARTIFICIAL_INTELLIGENCE),
            creator = userA.uid,
            location = Location(latitude = 37.423021, longitude = -122.086808))
    repository.addEvent(event)

    val newEvent =
        Event(
            id = "event-003",
            title = "Advanced Tech Talk on AI",
            description =
                "A deep dive into the latest advancements and future trends in artificial intelligence.",
            date = LocalDateTime.of(2025, 12, 1, 15, 0),
            tags = setOf(Tag.PROGRAMMING, Tag.ARTIFICIAL_INTELLIGENCE, Tag.RUNNING),
            participants = setOf(userA.uid),
            creator = userA.uid,
            location = Location(latitude = 37.423021, longitude = -122.086808))
    repository.updateEvent("event-003", newEvent)

    val result1 = repository.getAllEvents()
    assertEquals(1, result1.size)
    val result2 = repository.getEvent("event-003")
    assertNotNull(result2)
    assertEquals("event-003", result2.id)
    assertEquals("Advanced Tech Talk on AI", result2.title)
    assertEquals(
        "A deep dive into the latest advancements and future trends in artificial intelligence.",
        result2.description)
    assertEquals(LocalDateTime.of(2025, 12, 1, 15, 0), result2.date)
    assertEquals(setOf(Tag.PROGRAMMING, Tag.ARTIFICIAL_INTELLIGENCE, Tag.RUNNING), result2.tags)
    assertEquals(1, result2.participants.size)
    assert(result2.participants.contains(userA.uid))
    assertEquals(result2.creator, userA.uid)
  }

  @Test
  fun updateEvent_throwsException_forNonExistentEvent() = runTest {
    val user =
        UserProfile(
            uid = "3",
            username = "charlie",
            firstName = "Charlie",
            lastName = "Brown",
            country = "US",
            dateOfBirth = LocalDate.of(1985, 5, 20),
            tags = setOf(Tag.CYCLING, Tag.PHOTOGRAPHY))
    val newEvent =
        Event(
            id = "event-001",
            title = "Morning Run at the Lake",
            description = "A casual 5km run around the lake followed by coffee at the café nearby.",
            date = LocalDateTime.of(2025, 10, 15, 7, 30),
            tags = setOf(Tag.RUNNING, Tag.FITNESS),
            creator = userA.uid,
            location = Location(latitude = 46.5196535, longitude = 6.6322734))
    try {
      repository.updateEvent("nonexistent", newEvent)
      fail("Expected NoSuchElementException to be thrown")
    } catch (e: NoSuchElementException) {
      assertEquals(e.message, "No event found with id: nonexistent")
    }
  }

  @Test
  fun deleteEvent_existingEvent_shouldBeRemoved() = runTest {
    val user =
        UserProfile(
            uid = "3",
            username = "charlie",
            firstName = "Charlie",
            lastName = "Brown",
            country = "US",
            dateOfBirth = LocalDate.of(1985, 5, 20),
            tags = setOf(Tag.CYCLING, Tag.PHOTOGRAPHY))
    val event1 =
        Event(
            id = "event-001",
            title = "Morning Run at the Lake",
            description = "A casual 5km run around the lake followed by coffee at the café nearby.",
            date = LocalDateTime.of(2025, 10, 15, 7, 30),
            tags = setOf(Tag.RUNNING, Tag.FITNESS),
            creator = userA.uid,
            location = Location(latitude = 46.5196535, longitude = 6.6322734))
    repository.addEvent(event1)
    val event2 =
        Event(
            id = "event-002",
            title = "Evening Cycling Tour",
            date = LocalDateTime.of(2025, 11, 5, 18, 0),
            tags = setOf(Tag.CYCLING, Tag.COUNTRY),
            creator = userA.uid,
            location = Location(latitude = 46.5196535, longitude = 6.6322734))
    repository.addEvent(event2)
    val result1 = repository.getAllEvents()
    assertEquals(result1.size, 2)

    repository.deleteEvent("event-001")
    val result2 = repository.getAllEvents()
    assertEquals(1, result2.size)
    assertEquals("event-002", result2[0].id)
    assertEquals("Evening Cycling Tour", result2[0].title)
    assertNull(result2[0].description)
    assertEquals(LocalDateTime.of(2025, 11, 5, 18, 0), result2[0].date)
    assertEquals(setOf(Tag.CYCLING, Tag.COUNTRY), result2[0].tags)
    assertEquals(0, result2[0].participants.size)
    assertEquals(result2[0].creator, userA.uid)
  }

  @Test
  fun deleteEvent_nonExistingEvent_shouldThrowException() = runTest {
    val user =
        UserProfile(
            uid = "3",
            username = "charlie",
            firstName = "Charlie",
            lastName = "Brown",
            country = "US",
            dateOfBirth = LocalDate.of(1985, 5, 20),
            tags = setOf(Tag.CYCLING, Tag.PHOTOGRAPHY))
    val event =
        Event(
            id = "event-001",
            title = "Morning Run at the Lake",
            description = "A casual 5km run around the lake followed by coffee at the café nearby.",
            date = LocalDateTime.of(2025, 10, 15, 7, 30),
            tags = setOf(Tag.RUNNING, Tag.FITNESS),
            creator = userA.uid,
            location = Location(latitude = 46.5196535, longitude = 6.6322734))
    repository.addEvent(event)

    // verify initial state
    val result1 = repository.getAllEvents()
    assertEquals(1, result1.size)

    // Attempt to delete a non-existent event
    try {
      repository.deleteEvent("nonexistent")
      fail("Expected NoSuchElementException to be thrown")
    } catch (e: NoSuchElementException) {
      assertEquals(e.message, "No event found with id: nonexistent")
    }

    // Verify the existing event is still intact
    val result2 = repository.getAllEvents()
    assertEquals(1, result2.size)
    assertEquals("event-001", result2[0].id)
    assertEquals("Morning Run at the Lake", result2[0].title)
    assertEquals(
        "A casual 5km run around the lake followed by coffee at the café nearby.",
        result2[0].description)
    assertEquals(LocalDateTime.of(2025, 10, 15, 7, 30), result2[0].date)
    assertEquals(setOf(Tag.RUNNING, Tag.FITNESS), result2[0].tags)
    assertEquals(0, result2[0].participants.size)
    assertEquals(result2[0].creator, userA.uid)
  }

  @Test
  fun getNewID_returnsAString() = runTest {
    val id = repository.getNewID()
    assertNotNull(id)
    assert(id.isNotEmpty())
  }

  @Test
  fun getNewID_returnsUniqueStrings_onMultipleCalls() = runTest {
    val ids = mutableSetOf<String>()
    repeat(100) {
      val id = repository.getNewID()
      assertNotNull(id)
      assert(id.isNotEmpty())
      assert(!ids.contains(id)) // Ensure uniqueness
      ids.add(id)
    }
    assertEquals(100, ids.size) // Ensure all IDs are unique
  }
}
