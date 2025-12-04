package com.android.universe.model.event

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.model.location.Location
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime

@RunWith(AndroidJUnit4::class)
class EventLocalTemporaryRepositoryTest {

  private lateinit var repository: EventTemporaryRepository

  object EventLocalTemporaryTestValues {
    val temporaryEvent1 =
        Event(
            id = "test",
            title = "Title",
            description = "description",
            date = LocalDateTime.now(),
            tags = emptySet(),
            creator = "Test user",
            participants = setOf("Test user"),
            location = Location(0.0, 0.0),
            eventPicture = null)
    val temporaryEvent2 =
        Event(
            id = "test 2 ",
            title = "Title 2",
            description = "description 2",
            date = LocalDateTime.now(),
            tags = emptySet(),
            creator = "Test user 2",
            participants = setOf("Test user 2"),
            location = Location(0.1, 0.1),
            eventPicture = null)
  }

  @Before
  fun setUp() {
    repository = EventLocalTemporaryRepository()
  }

  @Test
  fun updateEventChangeEventInEmptyRepo() = runTest {
    val fakeEvent = EventLocalTemporaryTestValues.temporaryEvent1
    repository.updateEvent(
        id = fakeEvent.id,
        title = fakeEvent.title,
        description = fakeEvent.description,
        dateTime = fakeEvent.date,
        creator = fakeEvent.creator,
        participants = fakeEvent.participants,
        location = fakeEvent.location,
        eventPicture = fakeEvent.eventPicture)

    assertEquals(fakeEvent, repository.getEvent())
  }

  @Test
  fun updateEventChangeEventInNonEmptyRepo() = runTest {
    val fakeEvent = EventLocalTemporaryTestValues.temporaryEvent1
    repository.updateEvent(
        id = fakeEvent.id,
        title = fakeEvent.title,
        description = fakeEvent.description,
        dateTime = fakeEvent.date,
        creator = fakeEvent.creator,
        participants = fakeEvent.participants,
        location = fakeEvent.location,
        eventPicture = fakeEvent.eventPicture)
    val fakeEvent2 = EventLocalTemporaryTestValues.temporaryEvent2
    repository.updateEvent(
        id = fakeEvent2.id,
        title = fakeEvent2.title,
        description = fakeEvent2.description,
        dateTime = fakeEvent2.date,
        creator = fakeEvent2.creator,
        participants = fakeEvent2.participants,
        location = fakeEvent2.location,
        eventPicture = fakeEvent2.eventPicture)

    assertEquals(fakeEvent2, repository.getEvent())
  }

  @Test
  fun getNullEventTriggerException() = runTest {
    try {
      repository.getEvent()
      assertTrue(false)
    } catch (e: Exception) {
      assertTrue(true)
    }
  }

  @Test
  fun deleteEventModifyTheEvent() = runTest {
    val fakeEvent = EventLocalTemporaryTestValues.temporaryEvent1
    repository.updateEvent(
        id = fakeEvent.id,
        title = fakeEvent.title,
        description = fakeEvent.description,
        dateTime = fakeEvent.date,
        creator = fakeEvent.creator,
        participants = fakeEvent.participants,
        location = fakeEvent.location,
        eventPicture = fakeEvent.eventPicture)
    repository.deleteEvent()
    try {
      repository.getEvent()
      assertTrue(false)
    } catch (e: Exception) {
      assertTrue(true)
    }
  }
}
