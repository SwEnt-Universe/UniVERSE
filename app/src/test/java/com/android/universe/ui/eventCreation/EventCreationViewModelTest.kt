package com.android.universe.ui.eventCreation

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.di.DispatcherProvider
import com.android.universe.model.event.EventLocalTemporaryRepository
import com.android.universe.model.event.EventTemporaryRepository
import com.android.universe.model.event.FakeEventRepository
import com.android.universe.model.image.ImageBitmapManager
import com.android.universe.model.location.Location
import com.android.universe.model.tag.Tag
import com.android.universe.ui.common.ErrorMessages
import com.android.universe.ui.common.InputLimits
import com.android.universe.ui.common.ValidationState
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EventCreationViewModelTest {
  private lateinit var eventRepository: FakeEventRepository
  private lateinit var viewModel: EventCreationViewModel
  private lateinit var eventTemporaryRepository: EventTemporaryRepository
  private val testDispatcher = StandardTestDispatcher()

  /** Companion object to provides values for the tests. */
  companion object {
    const val EMPTY_TEXT_INPUT = ""
    const val SAMPLE_TITLE = "Sample Title"
    val BAD_SAMPLE_TITLE = "a".repeat(50)
    const val SAMPLE_DESCRIPTION = "Sample Description"
    val BAD_SAMPLE_DESCRIPTION = "a".repeat(150)
    const val SAMPLE_DATE_FORMAT = "12/12/2030"
    val SAMPLE_DATE: LocalDate = LocalDate.of(2030, 12, 12)
    val BAD_SAMPLE_DATE: LocalDate = LocalDate.of(1900, 12, 12)
    const val SAMPLE_TIME = "12:12"
    const val BAD_SAMPLE_TIME = "23:78"
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)

    val context = ApplicationProvider.getApplicationContext<Context>()
    val imageManager = ImageBitmapManager(context)

    val dispatcherProvider =
        object : DispatcherProvider {
          override val main: CoroutineDispatcher = testDispatcher
          override val default: CoroutineDispatcher = testDispatcher
          override val io: CoroutineDispatcher = testDispatcher
          override val unconfined: CoroutineDispatcher = testDispatcher
        }

    eventRepository = FakeEventRepository()
    eventTemporaryRepository = EventLocalTemporaryRepository()
    viewModel =
        EventCreationViewModel(
            imageManager = imageManager,
            eventRepository = eventRepository,
            eventTemporaryRepository = eventTemporaryRepository,
            dispatcherProvider = dispatcherProvider)
  }

  @Test
  fun testSetEventName() {
    viewModel.setEventName(SAMPLE_TITLE)
    val state = viewModel.uiStateEventCreation.value
    assert(state.name == SAMPLE_TITLE)
    assert(state.eventTitleValid == ValidationState.Valid)
  }

  @Test
  fun testSetBadEventName() {
    viewModel.setEventName(BAD_SAMPLE_TITLE)
    val state = viewModel.uiStateEventCreation.value
    assert(state.name == BAD_SAMPLE_TITLE.take(InputLimits.TITLE_EVENT_MAX_LENGTH + 1))
    assert(
        state.eventTitleValid ==
            ValidationState.Invalid(
                ErrorMessages.TITLE_EVENT_TOO_LONG.format(InputLimits.TITLE_EVENT_MAX_LENGTH)))
  }

  @Test
  fun testSetEmptyEventName() {
    viewModel.setEventName(EMPTY_TEXT_INPUT)
    val state = viewModel.uiStateEventCreation.value
    assert(state.name == EMPTY_TEXT_INPUT)
    assert(state.eventTitleValid == ValidationState.Invalid(ErrorMessages.TITLE_EVENT_EMPTY))
  }

  @Test
  fun testSetEventDescription() {
    viewModel.setEventDescription(SAMPLE_DESCRIPTION)
    val state = viewModel.uiStateEventCreation.value
    assert(state.description == SAMPLE_DESCRIPTION)
    assert(state.eventDescriptionValid == ValidationState.Valid)
  }

  @Test
  fun testSetBadEventDescription() {
    viewModel.setEventDescription(BAD_SAMPLE_DESCRIPTION)
    val state = viewModel.uiStateEventCreation.value
    assert(state.description == BAD_SAMPLE_DESCRIPTION.take(InputLimits.DESCRIPTION + 1))
    assert(
        state.eventDescriptionValid ==
            ValidationState.Invalid(
                ErrorMessages.DESCRIPTION_TOO_LONG.format(InputLimits.DESCRIPTION)))
  }

  @Test
  fun testSetEventDate() {
    viewModel.setDate(SAMPLE_DATE)
    val state = viewModel.uiStateEventCreation.value
    assert(state.date == SAMPLE_DATE)
    assert(state.eventDateValid == ValidationState.Valid)
  }

  @Test
  fun testSetPastEventDate() {
    viewModel.setDate(BAD_SAMPLE_DATE)
    val state = viewModel.uiStateEventCreation.value
    assert(state.date == BAD_SAMPLE_DATE)
    assert(state.eventDateValid == ValidationState.Invalid(ErrorMessages.EVENT_DATE_IN_PAST))
  }

  @Test
  fun testSetEventTime() {
    viewModel.setTime(SAMPLE_TIME)
    val state = viewModel.uiStateEventCreation.value
    assert(state.time == SAMPLE_TIME)
    assert(state.eventTimeValid == ValidationState.Valid)
  }

  @Test
  fun testSetBadEventTime() {
    viewModel.setTime(BAD_SAMPLE_TIME)
    val state = viewModel.uiStateEventCreation.value
    assert(state.time == BAD_SAMPLE_TIME)
    assert(state.eventTimeValid == ValidationState.Invalid(ErrorMessages.TIME_INVALID_LOGICAL))
  }

  @Test
  fun testSetEmptyEventTime() {
    viewModel.setTime(EMPTY_TEXT_INPUT)
    val state = viewModel.uiStateEventCreation.value
    assert(state.time == EMPTY_TEXT_INPUT)
    assert(state.eventTimeValid == ValidationState.Invalid(ErrorMessages.TIME_EMPTY))
  }

  @Test
  fun setImageWithNullUriRemoveImagePicture() {
    viewModel.setImage(null)
    assertEquals(null, viewModel.uiStateEventCreation.value.eventPicture)
  }

  @Test
  fun testDeleteImage() {
    viewModel.deleteImage()
    assertNull(viewModel.uiStateEventCreation.value.eventPicture)
  }

  @Test
  fun setOnboardingState() {
    viewModel.setOnboardingState(OnboardingState.ENTER_EVENT_TITLE, true)
    assertTrue(
        viewModel.uiStateEventCreation.value.onboardingState[OnboardingState.ENTER_EVENT_TITLE] ==
            true)

    viewModel.setOnboardingState(OnboardingState.ENTER_EVENT_TITLE, false)
    assertTrue(
        viewModel.uiStateEventCreation.value.onboardingState[OnboardingState.ENTER_EVENT_TITLE] ==
            false)

    viewModel.setOnboardingState(OnboardingState.ENTER_DESCRIPTION, true)
    assertTrue(
        viewModel.uiStateEventCreation.value.onboardingState[OnboardingState.ENTER_DESCRIPTION] ==
            true)

    viewModel.setOnboardingState(OnboardingState.ENTER_TIME, true)
    assertTrue(
        viewModel.uiStateEventCreation.value.onboardingState[OnboardingState.ENTER_TIME] == true)
  }

  @Test
  fun validateAllWithGoodInputs() {
    viewModel.setEventName(SAMPLE_TITLE)

    viewModel.setEventDescription(SAMPLE_DESCRIPTION)

    viewModel.setDate(SAMPLE_DATE)
    viewModel.setTime(SAMPLE_TIME)
    viewModel.setLocation(0.0, 0.0)

    assertTrue(viewModel.validateAll())
  }

  @Test
  fun validateAllWithBadTitle() {
    viewModel.setEventName(BAD_SAMPLE_TITLE)

    viewModel.setEventDescription(SAMPLE_DESCRIPTION)

    viewModel.setDate(SAMPLE_DATE)
    viewModel.setTime(SAMPLE_TIME)

    assertFalse(viewModel.validateAll())
  }

  @Test
  fun validateAllWithBadDescription() {
    viewModel.setEventName(SAMPLE_TITLE)

    viewModel.setEventDescription(BAD_SAMPLE_DESCRIPTION)

    viewModel.setDate(SAMPLE_DATE)
    viewModel.setTime(SAMPLE_TIME)

    assertFalse(viewModel.validateAll())
  }

  @Test
  fun validateAllWithBadDate() {
    viewModel.setEventName(SAMPLE_TITLE)

    viewModel.setEventDescription(SAMPLE_DESCRIPTION)

    viewModel.setDate(BAD_SAMPLE_DATE)
    viewModel.setTime(SAMPLE_TIME)

    assertFalse(viewModel.validateAll())
  }

  @Test
  fun validateAllWithBadTime() {
    viewModel.setEventName(SAMPLE_TITLE)

    viewModel.setEventDescription(SAMPLE_DESCRIPTION)

    viewModel.setDate(SAMPLE_DATE)
    viewModel.setTime(BAD_SAMPLE_TIME)

    assertFalse(viewModel.validateAll())
  }

  @Test
  fun validateAllWithBadInputs() {
    viewModel.setEventName(BAD_SAMPLE_TITLE)

    viewModel.setEventDescription(BAD_SAMPLE_DESCRIPTION)

    viewModel.setDate(BAD_SAMPLE_DATE)
    viewModel.setTime(BAD_SAMPLE_TIME)

    assertFalse(viewModel.validateAll())
  }

  @Test
  fun validateAllFailsWithoutLocation() {
    viewModel.setEventName(SAMPLE_TITLE)
    viewModel.setEventDescription(SAMPLE_DESCRIPTION)
    viewModel.setDate(SAMPLE_DATE)
    viewModel.setTime(SAMPLE_TIME)

    // No location set here

    assertFalse(viewModel.validateAll())
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun testSaveEvent() = runTest {
    viewModel.setEventName(SAMPLE_TITLE)

    viewModel.setEventDescription(SAMPLE_DESCRIPTION)

    viewModel.setDate(SAMPLE_DATE)
    viewModel.setTime(SAMPLE_TIME)
    viewModel.setLocation(0.0, 0.0)

    assertEquals(
        viewModel.formatDate(viewModel.uiStateEventCreation.value.date), SAMPLE_DATE_FORMAT)

    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.saveEvent(uid = "user123")
    testDispatcher.scheduler.advanceUntilIdle()
    val stockedEvent = eventTemporaryRepository.getEvent()
    assert(stockedEvent.title == SAMPLE_TITLE)
    assert(stockedEvent.description == SAMPLE_DESCRIPTION)
    assert(stockedEvent.creator == "user123")
    assert(stockedEvent.participants == setOf("user123"))
    assert(stockedEvent.location == Location(0.0, 0.0))
    assert(stockedEvent.tags == emptySet<Tag>())

    val expectedDate = LocalDateTime.of(2030, 12, 12, 12, 12)

    assert(stockedEvent.date == expectedDate)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }
}
