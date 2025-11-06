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

  /** Companion object to provides values for the tests. */
  companion object {
    const val SAMPLE_TITLE = "Sample Title"
    const val SAMPLE_DESCRIPTION = "Sample Description"
    const val SAMPLE_DAY = "12"
    const val SAMPLE_MONTH = "12"
    const val SAMPLE_YEAR = "2025"
    const val SAMPLE_HOUR = "12"
    const val SAMPLE_MINUTE = "12"
  }

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
    viewModel.setEventName(SAMPLE_TITLE)
    val state = viewModel.uiStateEventCreation.value
    assert(state.name == SAMPLE_TITLE)
  }

  @Test
  fun testSetEventDescription() {
    viewModel.setEventDescription(SAMPLE_DESCRIPTION)
    val state = viewModel.uiStateEventCreation.value
    assert(state.description == SAMPLE_DESCRIPTION)
  }

  @Test
  fun testSetEventDay() {
    viewModel.setEventDay(SAMPLE_DAY)
    val state = viewModel.uiStateEventCreation.value
    assert(state.day == SAMPLE_DAY)
  }

  @Test
  fun testSetEventMonth() {
    viewModel.setEventMonth(SAMPLE_MONTH)
    val state = viewModel.uiStateEventCreation.value
    assert(state.month == SAMPLE_MONTH)
  }

  @Test
  fun testSetEventYear() {
    viewModel.setEventYear(SAMPLE_YEAR)
    val state = viewModel.uiStateEventCreation.value
    assert(state.year == SAMPLE_YEAR)
  }

  @Test
  fun testSetEventHour() {
    viewModel.setEventHour(SAMPLE_HOUR)
    val state = viewModel.uiStateEventCreation.value
    assert(state.hour == SAMPLE_HOUR)
  }

  @Test
  fun testSetEventMinute() {
    viewModel.setEventMinute(SAMPLE_MINUTE)
    val state = viewModel.uiStateEventCreation.value
    assert(state.minute == SAMPLE_MINUTE)
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
    viewModel.setEventName(SAMPLE_TITLE)

    viewModel.setEventDescription(SAMPLE_DESCRIPTION)

    viewModel.setEventDay(SAMPLE_DAY)

    viewModel.setEventMonth(SAMPLE_MONTH)

    viewModel.setEventYear(SAMPLE_YEAR)

    viewModel.setEventHour(SAMPLE_HOUR)

    viewModel.setEventMinute(SAMPLE_MINUTE)

    val eventTags = setOf(Tag.METAL, Tag.CAR)
    viewModel.setEventTags(eventTags)

    viewModel.saveEvent(location = Location(0.0, 0.0), uid = "user123")
    testDispatcher.scheduler.advanceUntilIdle()
    val savedEvent = eventRepository.getAllEvents()
    assert(savedEvent.size == 1)
    val event = savedEvent[0]
    assert(event.title == SAMPLE_TITLE)
    assert(event.description == SAMPLE_DESCRIPTION)
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
