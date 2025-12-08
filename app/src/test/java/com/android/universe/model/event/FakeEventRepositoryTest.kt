package com.android.universe.model.event

import com.android.universe.model.location.Location
import com.android.universe.model.tag.Tag
import com.android.universe.utils.EventTestData
import com.android.universe.utils.MainCoroutineRule
import com.android.universe.utils.UserTestData
import java.time.LocalDateTime
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class FakeEventRepositoryTest {
  companion object {
    private val userA = UserTestData.Alice
    private val userB = UserTestData.Bob
  }

  private lateinit var repository: FakeEventRepository
  @get:Rule val mainCoroutineRule = MainCoroutineRule()

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
  fun getSuggestedEvents_respectsVisibilityLogic() = runTest {
    val user = UserTestData.SomeTagsUser
    val creatorId = "creator-001"

    val publicEvent =
        EventTestData.dummyEvent1.copy(
            id = "public-event", creator = creatorId, isPrivate = false, tags = user.tags)

    val privateEvent =
        EventTestData.dummyEvent2.copy(
            id = "private-event", creator = creatorId, isPrivate = true, tags = user.tags)

    repository.addEvent(publicEvent)
    repository.addEvent(privateEvent)

    val resultNotFollowing = repository.getSuggestedEventsForUser(user)
    assertEquals(1, resultNotFollowing.size)
    assertEquals("public-event", resultNotFollowing[0].id)

    val userFollowing = user.copy(following = setOf(creatorId))
    val resultFollowing = repository.getSuggestedEventsForUser(userFollowing)

    assertEquals(2, resultFollowing.size)
    assertTrue(resultFollowing.any { it.id == "private-event" })

    val creatorUser = user.copy(uid = creatorId)
    val resultAsCreator = repository.getSuggestedEventsForUser(creatorUser)
    assertEquals(2, resultAsCreator.size)
  }

  @Test
  fun getAllEvents_respectsVisibilityLogic() = runTest {
    val creatorId = "creator-123"
    val strangerId = "stranger-999"

    val publicEvent =
        EventTestData.dummyEvent1.copy(id = "pub", creator = creatorId, isPrivate = false)
    val privateEvent =
        EventTestData.dummyEvent2.copy(id = "priv", creator = creatorId, isPrivate = true)

    repository.addEvent(publicEvent)
    repository.addEvent(privateEvent)

    val resultStranger = repository.getAllEvents(strangerId, emptySet())
    assertEquals(1, resultStranger.size)
    assertEquals("pub", resultStranger[0].id)

    val resultFollower = repository.getAllEvents(strangerId, setOf(creatorId))
    assertEquals(2, resultFollower.size)

    val resultCreator = repository.getAllEvents(creatorId, emptySet())
    assertEquals(2, resultCreator.size)
  }

  @Test
  fun getEventsForUser_returnsEventsWhereUserInvolvedEventsIsParticipantOrCreator() = runTest {
    val createdEvent =
        EventTestData.dummyEvent1.copy(
            id = "event-created", creator = userA.uid, participants = emptySet())

    val participatingEvent =
        EventTestData.dummyEvent2.copy(
            id = "event-participating", creator = userB.uid, participants = setOf(userA.uid))

    val unrelatedEvent =
        EventTestData.dummyEvent3.copy(
            id = "event-unrelated", creator = userB.uid, participants = emptySet())

    repository.addEvent(createdEvent)
    repository.addEvent(participatingEvent)
    repository.addEvent(unrelatedEvent)

    val result = repository.getUserInvolvedEvents(userA.uid)

    assertEquals(2, result.size)
    assertTrue("Should contain event created by user", result.contains(createdEvent))
    assertTrue(
        "Should contain event where user is participant", result.contains(participatingEvent))
    assertFalse("Should not contain unrelated event", result.contains(unrelatedEvent))
  }

  @Test
  fun getEventsForUser_returnsEmptyList_whenUserHasNoAssociatedEventsInvolvedEvents() = runTest {
    val event = EventTestData.dummyEvent1.copy(creator = userA.uid, participants = setOf(userB.uid))

    repository.addEvent(event)

    val result = repository.getUserInvolvedEvents("NonExistentUser")

    assertTrue(result.isEmpty())
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
            tags = setOf(Tag.PROGRAMMING, Tag.AI),
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
    assertEquals(setOf(Tag.PROGRAMMING, Tag.AI), result.tags)
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

    val result = repository.getAllEvents(userA.uid, emptySet())
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

    val result1 = repository.getAllEvents(userA.uid, emptySet())
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

    val result1 = repository.getAllEvents(userA.uid, emptySet())
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
    val event =
        Event(
            id = "event-003",
            title = "Tech Talk on AI",
            description =
                "An insightful talk on the latest advancements in artificial intelligence.",
            date = LocalDateTime.of(2025, 12, 1, 14, 0),
            tags = setOf(Tag.PROGRAMMING, Tag.AI),
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
            tags = setOf(Tag.PROGRAMMING, Tag.AI, Tag.RUNNING),
            participants = setOf(userA.uid),
            creator = userA.uid,
            location = Location(latitude = 37.423021, longitude = -122.086808))
    repository.updateEvent("event-003", newEvent)

    val result1 = repository.getAllEvents(userA.uid, emptySet())
    assertEquals(1, result1.size)
    val result2 = repository.getEvent("event-003")
    assertNotNull(result2)
    assertEquals("event-003", result2.id)
    assertEquals("Advanced Tech Talk on AI", result2.title)
    assertEquals(
        "A deep dive into the latest advancements and future trends in artificial intelligence.",
        result2.description)
    assertEquals(LocalDateTime.of(2025, 12, 1, 15, 0), result2.date)
    assertEquals(setOf(Tag.PROGRAMMING, Tag.AI, Tag.RUNNING), result2.tags)
    assertEquals(1, result2.participants.size)
    assert(result2.participants.contains(userA.uid))
    assertEquals(result2.creator, userA.uid)
  }

  @Test
  fun updateEvent_throwsException_forNonExistentEvent() = runTest {
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
    val result1 = repository.getAllEvents(userA.uid, emptySet())
    assertEquals(result1.size, 2)

    repository.deleteEvent("event-001")
    val result2 = repository.getAllEvents(userA.uid, emptySet())
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
    val result1 = repository.getAllEvents(userA.uid, emptySet())
    assertEquals(1, result1.size)

    // Attempt to delete a non-existent event
    try {
      repository.deleteEvent("nonexistent")
      fail("Expected NoSuchElementException to be thrown")
    } catch (e: NoSuchElementException) {
      assertEquals(e.message, "No event found with id: nonexistent")
    }

    // Verify the existing event is still intact
    val result2 = repository.getAllEvents(userA.uid, emptySet())
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

  @Test
  fun persistAIEvents_assignsIds_persistsEvents_andReturnsSavedEvents() = runTest {
    // Arrange
    val event1 = EventTestData.dummyEvent1.copy(id = "", creator = userA.uid)
    val event2 = EventTestData.dummyEvent2.copy(id = "", creator = userB.uid)

    val input = listOf(event1, event2)

    // Act
    val saved = repository.persistAIEvents(input)

    // Assert: size matches
    assertEquals(2, saved.size)

    // Assert: each event got a new ID
    assertTrue(saved[0].id.isNotBlank())
    assertTrue(saved[1].id.isNotBlank())
    assertNotEquals(saved[0].id, saved[1].id)

    // Assert: repository actually persisted them
    val all = repository.getAllEvents(userA.uid, emptySet())
    assertEquals(2, all.size)

    // Assert: stored events match what persistAIEvents returned
    assertTrue(all.any { it.id == saved[0].id })
    assertTrue(all.any { it.id == saved[1].id })

    // Assert: original fields preserved
    assertEquals(event1.title, saved[0].title)
    assertEquals(event2.title, saved[1].title)

    assertEquals(event1.date, saved[0].date)
    assertEquals(event2.date, saved[1].date)

    assertEquals(event1.tags, saved[0].tags)
    assertEquals(event2.tags, saved[1].tags)

    assertEquals(event1.creator, saved[0].creator)
    assertEquals(event2.creator, saved[1].creator)
  }
}
