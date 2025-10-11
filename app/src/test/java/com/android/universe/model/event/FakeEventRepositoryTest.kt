package com.android.universe.model.event

import com.android.universe.model.Tag
import com.android.universe.model.user.UserProfile
import java.time.LocalDate
import java.time.LocalDateTime
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.fail
import kotlin.test.assertNotNull
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class FakeEventRepositoryTest {

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
  fun addEvent_storesEvent_andCanBeRetrieved() = runTest {
    val user1 =
        UserProfile(
            username = "alice",
            firstName = "Alice",
            lastName = "Smith",
            country = "CH",
            description = "Bio",
            dateOfBirth = LocalDate.of(1990, 1, 1),
            tags = listOf(Tag(name = "Music"), Tag(name = "Running")))
    val user2 =
        UserProfile(
            username = "bob",
            firstName = "Bob",
            lastName = "Jones",
            country = "FR",
            dateOfBirth = LocalDate.of(2000, 8, 11),
            tags = listOf(Tag(name = "Cooking"), Tag(name = "Fitness")))
    val event =
        Event(
            id = "event-001",
            title = "Morning Run at the Lake",
            description = "A casual 5km run around the lake followed by coffee at the café nearby.",
            date = LocalDateTime.of(2025, 10, 15, 7, 30),
            tags = setOf(Tag(name = "Running"), Tag(name = "Fitness")),
            participants = setOf(user1, user2),
            creator = user1)
    repository.addEvent(event)

    val result = repository.getEvent("event-001")
    assertNotNull(result)
    assertEquals("event-001", result.id)
    assertEquals("Morning Run at the Lake", result.title)
    assertEquals(
        "A casual 5km run around the lake followed by coffee at the café nearby.",
        result.description)
    assertEquals(LocalDateTime.of(2025, 10, 15, 7, 30), result.date)
    assertEquals(setOf(Tag(name = "Running"), Tag(name = "Fitness")), result.tags)
    assertEquals(2, result.participants.size)
    assert(result.participants.contains(user1))
    assert(result.participants.contains(user2))
    assertEquals(result.creator, user1)
  }

  @Test
  fun addEvent_storesEventWithNoDescription_andCanBeRetrieved() = runTest {
    val user =
        UserProfile(
            username = "charlie",
            firstName = "Charlie",
            lastName = "Brown",
            country = "US",
            dateOfBirth = LocalDate.of(1985, 5, 20),
            tags = listOf(Tag(name = "Cycling"), Tag(name = "Photography")))
    val event =
        Event(
            id = "event-002",
            title = "Evening Cycling Tour",
            date = LocalDateTime.of(2025, 11, 5, 18, 0),
            tags = setOf(Tag(name = "Cycling"), Tag(name = "Outdoors")),
            participants = setOf(user),
            creator = user)
    repository.addEvent(event)

    val result = repository.getEvent("event-002")
    assertNotNull(result)
    assertEquals("event-002", result.id)
    assertEquals("Evening Cycling Tour", result.title)
    assertNull(result.description)
    assertEquals(LocalDateTime.of(2025, 11, 5, 18, 0), result.date)
    assertEquals(setOf(Tag(name = "Cycling"), Tag(name = "Outdoors")), result.tags)
    assertEquals(1, result.participants.size)
    assert(result.participants.contains(user))
    assertEquals(result.creator, user)
  }

  @Test
  fun addEvent_storesEventWithNoParticipants_andCanBeRetrieved() = runTest {
    val user =
        UserProfile(
            username = "charlie",
            firstName = "Charlie",
            lastName = "Brown",
            country = "US",
            dateOfBirth = LocalDate.of(1985, 5, 20),
            tags = listOf(Tag(name = "Cycling"), Tag(name = "Photography")))
    val event =
        Event(
            id = "event-003",
            title = "Tech Talk on AI",
            description =
                "An insightful talk on the latest advancements in artificial intelligence.",
            date = LocalDateTime.of(2025, 12, 1, 14, 0),
            tags = setOf(Tag(name = "Programming"), Tag(name = "Artificial intelligence")),
            creator = user)
    repository.addEvent(event)

    val result = repository.getEvent("event-003")
    assertNotNull(result)
    assertEquals("event-003", result.id)
    assertEquals("Tech Talk on AI", result.title)
    assertEquals(
        "An insightful talk on the latest advancements in artificial intelligence.",
        result.description)
    assertEquals(LocalDateTime.of(2025, 12, 1, 14, 0), result.date)
    assertEquals(
        setOf(Tag(name = "Programming"), Tag(name = "Artificial intelligence")), result.tags)
    assertEquals(0, result.participants.size)
    assertEquals(result.creator, user)
  }

  @Test
  fun addEvent_storesMultipleEvents_andAllCanBeRetrieved() = runTest {
    val user1 =
        UserProfile(
            username = "alice",
            firstName = "Alice",
            lastName = "Smith",
            country = "CH",
            description = "Bio",
            dateOfBirth = LocalDate.of(1990, 1, 1),
            tags = listOf(Tag(name = "Music"), Tag(name = "Running")))
    val user2 =
        UserProfile(
            username = "bob",
            firstName = "Bob",
            lastName = "Jones",
            country = "FR",
            dateOfBirth = LocalDate.of(2000, 8, 11),
            tags = listOf(Tag(name = "Cooking"), Tag(name = "Fitness")))
    val event1 =
        Event(
            id = "event-001",
            title = "Morning Run at the Lake",
            description = "A casual 5km run around the lake followed by coffee at the café nearby.",
            date = LocalDateTime.of(2025, 10, 15, 7, 30),
            tags = setOf(Tag(name = "Running"), Tag(name = "Fitness")),
            participants = setOf(user1, user2),
            creator = user1)
    val event2 =
        Event(
            id = "event-002",
            title = "Evening Cycling Tour",
            date = LocalDateTime.of(2025, 11, 5, 18, 0),
            tags = setOf(Tag(name = "Cycling"), Tag(name = "Outdoors")),
            creator = user2)
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
    assertEquals(setOf(Tag(name = "Running"), Tag(name = "Fitness")), result[0].tags)
    assertEquals(setOf(Tag(name = "Cycling"), Tag(name = "Outdoors")), result[1].tags)
    assertEquals(2, result[0].participants.size)
    assert(result[0].participants.contains(user1))
    assert(result[0].participants.contains(user2))
    assertEquals(0, result[1].participants.size)
    assertEquals(result[0].creator, user1)
    assertEquals(result[1].creator, user2)
  }

  @Test
  fun addEvent_storesEvent_thenUpdateEvent_editsAndCanBeRetrieved() = runTest {
    val user1 =
        UserProfile(
            username = "alice",
            firstName = "Alice",
            lastName = "Smith",
            country = "CH",
            description = "Bio",
            dateOfBirth = LocalDate.of(1990, 1, 1),
            tags = listOf(Tag(name = "Music"), Tag(name = "Running")))
    val user2 =
        UserProfile(
            username = "bob",
            firstName = "Bob",
            lastName = "Jones",
            country = "FR",
            dateOfBirth = LocalDate.of(2000, 8, 11),
            tags = listOf(Tag(name = "Cooking"), Tag(name = "Fitness")))
    val event =
        Event(
            id = "event-001",
            title = "Morning Run at the Lake",
            description = "A casual 5km run around the lake followed by coffee at the café nearby.",
            date = LocalDateTime.of(2025, 10, 15, 7, 30),
            tags = setOf(Tag(name = "Running"), Tag(name = "Fitness")),
            participants = setOf(user1, user2),
            creator = user1)
    repository.addEvent(event)

    val newEvent =
        Event(
            id = "event-001",
            title = "Morning Run and Yoga at the Lake",
            description =
                "A casual 5km run around the lake followed by a relaxing yoga session and coffee at the café nearby.",
            date = LocalDateTime.of(2025, 10, 15, 8, 0),
            tags = setOf(Tag(name = "Running"), Tag(name = "Fitness"), Tag(name = "Yoga")),
            participants = setOf(user1),
            creator = user1)
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
    assertEquals(
        setOf(Tag(name = "Running"), Tag(name = "Fitness"), Tag(name = "Yoga")), result2.tags)
    assertEquals(1, result2.participants.size)
    assert(result2.participants.contains(user1))
    assertEquals(result2.creator, user1)
  }

  @Test
  fun addEvent_storesEventWithNoDescription_thenUpdateEvent_editsAndCanBeRetrieved() = runTest {
    val user =
        UserProfile(
            username = "charlie",
            firstName = "Charlie",
            lastName = "Brown",
            country = "US",
            dateOfBirth = LocalDate.of(1985, 5, 20),
            tags = listOf(Tag(name = "Cycling"), Tag(name = "Photography")))
    val event =
        Event(
            id = "event-002",
            title = "Evening Cycling Tour",
            date = LocalDateTime.of(2025, 11, 5, 18, 0),
            tags = setOf(Tag(name = "Cycling"), Tag(name = "Outdoors")),
            participants = setOf(user),
            creator = user)
    repository.addEvent(event)

    val newEvent =
        Event(
            id = "event-002",
            title = "Evening Cycling and Photography Tour",
            description = "An evening cycling tour with stops for photography at scenic spots.",
            date = LocalDateTime.of(2025, 11, 5, 18, 30),
            tags = setOf(Tag(name = "Cycling"), Tag(name = "Outdoors"), Tag(name = "Photography")),
            participants = setOf(user),
            creator = user)
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
    assertEquals(
        setOf(Tag(name = "Cycling"), Tag(name = "Outdoors"), Tag(name = "Photography")),
        result2.tags)
    assertEquals(1, result2.participants.size)
    assert(result2.participants.contains(user))
    assertEquals(result2.creator, user)
  }

  @Test
  fun addEvent_storesEventWithNoParticipants_thenUpdateEvent_editsAndCanBeRetrieved() = runTest {
    val user =
        UserProfile(
            username = "charlie",
            firstName = "Charlie",
            lastName = "Brown",
            country = "US",
            dateOfBirth = LocalDate.of(1985, 5, 20),
            tags = listOf(Tag(name = "Cycling"), Tag(name = "Photography")))
    val event =
        Event(
            id = "event-003",
            title = "Tech Talk on AI",
            description =
                "An insightful talk on the latest advancements in artificial intelligence.",
            date = LocalDateTime.of(2025, 12, 1, 14, 0),
            tags = setOf(Tag(name = "Programming"), Tag(name = "Artificial intelligence")),
            creator = user)
    repository.addEvent(event)

    val newEvent =
        Event(
            id = "event-003",
            title = "Advanced Tech Talk on AI",
            description =
                "A deep dive into the latest advancements and future trends in artificial intelligence.",
            date = LocalDateTime.of(2025, 12, 1, 15, 0),
            tags =
                setOf(
                    Tag(name = "Programming"),
                    Tag(name = "Artificial intelligence"),
                    Tag(name = "Technology")),
            participants = setOf(user),
            creator = user)
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
    assertEquals(
        setOf(
            Tag(name = "Programming"),
            Tag(name = "Artificial intelligence"),
            Tag(name = "Technology")),
        result2.tags)
    assertEquals(1, result2.participants.size)
    assert(result2.participants.contains(user))
    assertEquals(result2.creator, user)
  }

  @Test
  fun updateEvent_throwsException_forNonExistentEvent() = runTest {
    val user =
        UserProfile(
            username = "charlie",
            firstName = "Charlie",
            lastName = "Brown",
            country = "US",
            dateOfBirth = LocalDate.of(1985, 5, 20),
            tags = listOf(Tag(name = "Cycling"), Tag(name = "Photography")))
    val newEvent =
        Event(
            id = "event-001",
            title = "Morning Run at the Lake",
            description = "A casual 5km run around the lake followed by coffee at the café nearby.",
            date = LocalDateTime.of(2025, 10, 15, 7, 30),
            tags = setOf(Tag(name = "Running"), Tag(name = "Fitness")),
            creator = user)
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
            username = "charlie",
            firstName = "Charlie",
            lastName = "Brown",
            country = "US",
            dateOfBirth = LocalDate.of(1985, 5, 20),
            tags = listOf(Tag(name = "Cycling"), Tag(name = "Photography")))
    val event1 =
        Event(
            id = "event-001",
            title = "Morning Run at the Lake",
            description = "A casual 5km run around the lake followed by coffee at the café nearby.",
            date = LocalDateTime.of(2025, 10, 15, 7, 30),
            tags = setOf(Tag(name = "Running"), Tag(name = "Fitness")),
            creator = user)
    repository.addEvent(event1)
    val event2 =
        Event(
            id = "event-002",
            title = "Evening Cycling Tour",
            date = LocalDateTime.of(2025, 11, 5, 18, 0),
            tags = setOf(Tag(name = "Cycling"), Tag(name = "Outdoors")),
            creator = user)
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
    assertEquals(setOf(Tag(name = "Cycling"), Tag(name = "Outdoors")), result2[0].tags)
    assertEquals(0, result2[0].participants.size)
    assertEquals(result2[0].creator, user)
  }

  @Test
  fun deleteEvent_nonExistingEvent_shouldThrowException() = runTest {
    val user =
        UserProfile(
            username = "charlie",
            firstName = "Charlie",
            lastName = "Brown",
            country = "US",
            dateOfBirth = LocalDate.of(1985, 5, 20),
            tags = listOf(Tag(name = "Cycling"), Tag(name = "Photography")))
    val event =
        Event(
            id = "event-001",
            title = "Morning Run at the Lake",
            description = "A casual 5km run around the lake followed by coffee at the café nearby.",
            date = LocalDateTime.of(2025, 10, 15, 7, 30),
            tags = setOf(Tag(name = "Running"), Tag(name = "Fitness")),
            creator = user)
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
    assertEquals(setOf(Tag(name = "Running"), Tag(name = "Fitness")), result2[0].tags)
    assertEquals(0, result2[0].participants.size)
    assertEquals(result2[0].creator, user)
  }
}
