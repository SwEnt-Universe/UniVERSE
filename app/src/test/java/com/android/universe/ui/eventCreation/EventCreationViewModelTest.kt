package com.android.universe.ui.eventCreation

import com.android.universe.model.Tag
import com.android.universe.model.event.FakeEventRepository
import com.android.universe.model.location.Location
import com.android.universe.model.user.FakeUserRepository
import com.android.universe.model.user.UserProfile
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

class EventCreationViewModelTest {
  private lateinit var eventRepository: FakeEventRepository
  private lateinit var userRepository: FakeUserRepository
  private lateinit var viewModel: EventCreationViewModel
  private val testDispatcher = StandardTestDispatcher()

  @OptIn(ExperimentalCoroutinesApi::class)
  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    eventRepository = FakeEventRepository()
    userRepository = FakeUserRepository()
    viewModel =
        EventCreationViewModel(eventRepository = eventRepository, userRepository = userRepository)
  }

  @Test
  fun testSetEventName() {
    val eventName = "Sample Event"
    viewModel.setEventName(eventName)
    val state = viewModel.uiStateEventCreation.value
    assert(state.name == eventName)
  }

  @Test
  fun testSetEventDescription() {
    val eventDescription = "Sample Description"
    viewModel.setEventDescription(eventDescription)
    val state = viewModel.uiStateEventCreation.value
    assert(state.description == eventDescription)
  }

  @Test
  fun testSetEventDay() {
    val eventDay = "Sample Day"
    viewModel.setEventDay(eventDay)
    val state = viewModel.uiStateEventCreation.value
    assert(state.day == eventDay)
  }

  @Test
  fun testSetEventMonth() {
    val eventMonth = "Sample Month"
    viewModel.setEventMonth(eventMonth)
    val state = viewModel.uiStateEventCreation.value
    assert(state.month == eventMonth)
  }

  @Test
  fun testSetEventYear() {
    val eventYear = "Sample Year"
    viewModel.setEventYear(eventYear)
    val state = viewModel.uiStateEventCreation.value
    assert(state.year == eventYear)
  }

  @Test
  fun testSetEventHour() {
    val eventHour = "Sample Hour"
    viewModel.setEventHour(eventHour)
    val state = viewModel.uiStateEventCreation.value
    assert(state.hour == eventHour)
  }

  @Test
  fun testSetEventMinute() {
    val eventMinute = "Sample Minute"
    viewModel.setEventMinute(eventMinute)
    val state = viewModel.uiStateEventCreation.value
    assert(state.minute == eventMinute)
  }

  @Test
  fun testSetEventTags() {
    val eventTags = setOf(Tag.METAL, Tag.CAR)
    viewModel.setEventTags(eventTags)
    val state = viewModel.uiStateEventCreation.value
    assert(state.tags == eventTags)
  }

  @Test
  fun testSaveEvent() = runTest {
    val userProfile =
        UserProfile(
            uid = "user123",
            username = "testUser",
            firstName = "Test",
            lastName = "User",
            country = "US",
            dateOfBirth = LocalDate.of(1990, 1, 1),
            tags = emptySet())
    userRepository.addUser(userProfile)
    val eventName = "Sample Event"
    viewModel.setEventName(eventName)

    val eventDescription = "Sample Description"
    viewModel.setEventDescription(eventDescription)

    val eventDay = "12"
    viewModel.setEventDay(eventDay)

    val eventMonth = "12"
    viewModel.setEventMonth(eventMonth)

    val eventYear = "2025"
    viewModel.setEventYear(eventYear)

    val eventHour = "12"
    viewModel.setEventHour(eventHour)

    val eventMinute = "12"
    viewModel.setEventMinute(eventMinute)

    val eventTags = setOf(Tag.METAL, Tag.CAR)
    viewModel.setEventTags(eventTags)

    viewModel.saveEvent(location = Location(0.0, 0.0), uid = "user123")
    testDispatcher.scheduler.advanceUntilIdle()
    val savedEvent = eventRepository.getAllEvents()
    assert(savedEvent.size == 1)
    val event = savedEvent[0]
    assert(event.title == eventName)
    assert(event.description == eventDescription)
    assert(event.creator == userProfile)
    assert(event.participants == setOf(userProfile))
    assert(event.location == Location(0.0, 0.0))
    assert(event.tags == eventTags)

    val expectedDate = LocalDateTime.of(2025, 12, 12, 12, 12)

    assert(event.date == expectedDate)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }
}
