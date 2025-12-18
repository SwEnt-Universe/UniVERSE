package com.android.universe.model.event

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.model.location.Location
import com.android.universe.ui.map.ReverseGeocoderSingleton
import io.mockk.coEvery
import io.mockk.mockkObject
import java.time.LocalDateTime
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

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
            isPrivate = false,
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
            isPrivate = true,
            eventPicture = null)
    val sampleLocation = Location(10.0, 10.0)
  }

  @Before
  fun setUp() {
    mockkObject(ReverseGeocoderSingleton)
    coEvery { ReverseGeocoderSingleton.getSmartAddress(any()) } returns "Example"
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
        isPrivate = fakeEvent.isPrivate,
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
        isPrivate = fakeEvent.isPrivate,
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
        isPrivate = fakeEvent2.isPrivate,
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
        isPrivate = fakeEvent.isPrivate,
        eventPicture = fakeEvent.eventPicture)
    repository.deleteEvent()
    try {
      repository.getEvent()
      assertTrue(false)
    } catch (e: Exception) {
      assertTrue(true)
    }
  }

  @Test
  fun isLocationNullWhenLocationIsNull() = runTest {
    repository.deleteEvent()
    val isNull = repository.isLocationNull()
    assertTrue(isNull)
  }

  @Test
  fun isLocationNullWhenLocationIsNotNull() = runTest {
    val fakeEvent = EventLocalTemporaryTestValues.temporaryEvent1
    repository.updateEventAsObject(fakeEvent)
    val isNull = repository.isLocationNull()
    assertTrue(!isNull)
  }

  @Test
  fun updateLocationChangesOnlyLocation() = runTest {
    repository.updateLocation(EventLocalTemporaryTestValues.sampleLocation)
    val updatedEvent = repository.getEvent()
    assertEquals(EventLocalTemporaryTestValues.sampleLocation, updatedEvent.location)
    assertEquals("", updatedEvent.id)
    assertEquals("", updatedEvent.title)
    assertEquals(null, updatedEvent.description)
    assertEquals(setOf<String>(), updatedEvent.participants)
    assertEquals("", updatedEvent.creator)
    assertEquals(false, updatedEvent.isPrivate)
    assertEquals(null, updatedEvent.eventPicture)
  }
}
