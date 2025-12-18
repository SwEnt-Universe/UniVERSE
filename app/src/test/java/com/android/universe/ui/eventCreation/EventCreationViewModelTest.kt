package com.android.universe.ui.eventCreation

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.di.DefaultDP
import com.android.universe.model.ai.gemini.EventProposal
import com.android.universe.model.ai.gemini.FakeGeminiEventAssistant
import com.android.universe.model.event.EventLocalTemporaryRepository
import com.android.universe.model.event.EventTemporaryRepository
import com.android.universe.model.event.FakeEventRepository
import com.android.universe.model.image.ImageBitmapManager
import com.android.universe.model.location.Location
import com.android.universe.model.tag.Tag
import com.android.universe.ui.common.ErrorMessages
import com.android.universe.ui.common.InputLimits
import com.android.universe.ui.common.ValidationState
import com.android.universe.ui.map.ReverseGeocoderSingleton
import com.android.universe.utils.EventTestData
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
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
  private lateinit var fakeGemini: FakeGeminiEventAssistant
  private val testDispatcher = StandardTestDispatcher()

  /** Companion object to provides values for the tests. */
  companion object {
    const val EMPTY_TEXT_INPUT = ""
    const val SAMPLE_TITLE = "Sample Title"
    val BAD_SAMPLE_TITLE = "a".repeat(50)
    const val SAMPLE_DESCRIPTION = "Sample Description"
    val BAD_SAMPLE_DESCRIPTION = "a".repeat(InputLimits.DESCRIPTION + 1)
    const val SAMPLE_DATE_FORMAT = "12/12/2030"
    val SAMPLE_DATE: LocalDate = LocalDate.of(2030, 12, 12)
    val BAD_SAMPLE_DATE: LocalDate = LocalDate.of(1900, 12, 12)
    const val SAMPLE_TIME = "12:12"
    const val BAD_SAMPLE_TIME = "23:78"
    val SAMPLE_LOCATION = Location(0.0, 0.0)
    val SAMPLE_EVENT = EventTestData.dummyEvent1
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)

    val context = ApplicationProvider.getApplicationContext<Context>()
    mockkObject(ReverseGeocoderSingleton)
    coEvery { ReverseGeocoderSingleton.getSmartAddress(any()) } returns "Example"
    val imageManager = ImageBitmapManager(context)

    mockkObject(DefaultDP)
    every { DefaultDP.default } returns testDispatcher
    every { DefaultDP.io } returns testDispatcher
    every { DefaultDP.main } returns testDispatcher
    every { DefaultDP.unconfined } returns testDispatcher

    eventRepository = FakeEventRepository()
    eventTemporaryRepository = EventLocalTemporaryRepository()
    fakeGemini = FakeGeminiEventAssistant()

    viewModel =
        EventCreationViewModel(
            imageManager = imageManager,
            eventRepository = eventRepository,
            eventTemporaryRepository = eventTemporaryRepository,
            gemini = fakeGemini)
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
  fun testSetPrivacy() {
    assertFalse(viewModel.uiStateEventCreation.value.isPrivate)

    viewModel.setPrivacy(true)
    assertTrue(viewModel.uiStateEventCreation.value.isPrivate)

    viewModel.setPrivacy(false)
    assertFalse(viewModel.uiStateEventCreation.value.isPrivate)
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
  fun testSaveEvent() = runTest {
    viewModel.setEventName(SAMPLE_TITLE)

    viewModel.setEventDescription(SAMPLE_DESCRIPTION)

    viewModel.setDate(SAMPLE_DATE)
    viewModel.setTime(SAMPLE_TIME)

    assertEquals(
        viewModel.formatDate(viewModel.uiStateEventCreation.value.date), SAMPLE_DATE_FORMAT)

    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.saveEvent(uidUser = "user123", uidEvent = null, location = SAMPLE_LOCATION)
    testDispatcher.scheduler.advanceUntilIdle()
    val stockedEvent = eventTemporaryRepository.getEvent()
    assert(stockedEvent.title == SAMPLE_TITLE)
    assert(stockedEvent.description == SAMPLE_DESCRIPTION)
    assert(stockedEvent.creator == "user123")
    assert(stockedEvent.participants == setOf("user123"))
    assert(stockedEvent.location == Location(0.0, 0.0))
    assert(stockedEvent.tags == emptySet<Tag>())
    assertFalse(stockedEvent.isPrivate)

    val expectedDate = LocalDateTime.of(2030, 12, 12, 12, 12)

    assert(stockedEvent.date == expectedDate)
  }

  @Test
  fun testSavePrivateEvent() = runTest {
    viewModel.setEventName(SAMPLE_TITLE)
    viewModel.setEventDescription(SAMPLE_DESCRIPTION)
    viewModel.setDate(SAMPLE_DATE)
    viewModel.setTime(SAMPLE_TIME)
    viewModel.setPrivacy(true)

    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.saveEvent(uidUser = "user123", uidEvent = null, location = SAMPLE_LOCATION)
    testDispatcher.scheduler.advanceUntilIdle()
    val stockedEvent = eventTemporaryRepository.getEvent()

    assertTrue(stockedEvent.isPrivate)
  }

  @Test
  fun testShowAiAssistResetsState() {
    viewModel.setAiPrompt("Old Prompt")

    viewModel.showAiAssist()

    val state = viewModel.uiStateEventCreation.value
    assertTrue(state.isAiAssistVisible)
    assertEquals("", state.aiPrompt)
    assertNull(state.aiPromptError)
    assertNull(state.proposal)
    assertNull(state.generationError)

    assertEquals(ValidationState.Neutral, state.aiPromptValid)
  }

  @Test
  fun testHideAiAssist() {
    viewModel.showAiAssist()
    assertTrue(viewModel.uiStateEventCreation.value.isAiAssistVisible)

    viewModel.hideAiAssist()
    assertFalse(viewModel.uiStateEventCreation.value.isAiAssistVisible)
  }

  @Test
  fun testAiPromptValidationLogic() {
    viewModel.showAiAssist()

    assertEquals(ValidationState.Neutral, viewModel.uiStateEventCreation.value.aiPromptValid)

    viewModel.setAiPrompt("Music Festival")
    assertEquals(ValidationState.Valid, viewModel.uiStateEventCreation.value.aiPromptValid)

    viewModel.setAiPrompt("")
    val state = viewModel.uiStateEventCreation.value
    assertEquals(EventCreationViewModel.Companion.AiErrors.PROMPT_EMPTY, state.aiPromptError)
    assertTrue(state.aiPromptValid is ValidationState.Invalid)
  }

  @Test
  fun testGenerateProposalSuccess() = runTest {
    viewModel.showAiAssist()
    viewModel.setAiPrompt("Music Festival")

    fakeGemini.predefinedProposal = EventProposal("Jazz Night", "A smooth evening.")

    viewModel.generateProposal(SAMPLE_LOCATION)

    testDispatcher.scheduler.advanceUntilIdle()

    val state = viewModel.uiStateEventCreation.value
    assertFalse(state.isGenerating)
    assertNotNull(state.proposal)
    assertEquals("Jazz Night", state.proposal?.title)
    assertEquals("A smooth evening.", state.proposal?.description)
  }

  @Test
  fun testGenerateProposalFailure() = runTest {
    viewModel.showAiAssist()
    viewModel.setAiPrompt("Chaos")

    fakeGemini.shouldFail = true

    viewModel.generateProposal(SAMPLE_LOCATION)
    testDispatcher.scheduler.advanceUntilIdle()

    val state = viewModel.uiStateEventCreation.value
    assertFalse(state.isGenerating)
    assertNull(state.proposal)
    assertEquals(EventCreationViewModel.Companion.AiErrors.GENERATION_FAILED, state.generationError)
  }

  @Test
  fun testGenerateProposalEmptyPrompt() {
    viewModel.showAiAssist()
    viewModel.setAiPrompt("")

    viewModel.generateProposal(SAMPLE_LOCATION)

    val state = viewModel.uiStateEventCreation.value
    assertEquals(EventCreationViewModel.Companion.AiErrors.PROMPT_EMPTY, state.aiPromptError)
  }

  @Test
  fun testAcceptProposalValid() {
    viewModel.showAiAssist()
    viewModel.setAiPrompt("Valid Prompt")
    val validProposal = EventProposal("Cool Title", "Cool Description")

    fakeGemini.predefinedProposal = validProposal
    viewModel.generateProposal(SAMPLE_LOCATION)
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.acceptProposal()

    val state = viewModel.uiStateEventCreation.value
    assertEquals("Cool Title", state.name)
    assertEquals("Cool Description", state.description)
    assertFalse(state.isAiAssistVisible)

    assertTrue(state.onboardingState[OnboardingState.ENTER_EVENT_TITLE] == true)
    assertTrue(state.onboardingState[OnboardingState.ENTER_DESCRIPTION] == true)
  }

  @Test
  fun testAcceptProposalInvalidLength() {
    viewModel.showAiAssist()
    viewModel.setAiPrompt("Long Prompt")

    val longTitle = "A".repeat(InputLimits.TITLE_EVENT_MAX_LENGTH + 5)
    val validDesc = "Desc"

    fakeGemini.predefinedProposal = EventProposal(longTitle, validDesc)
    viewModel.generateProposal(SAMPLE_LOCATION)
    testDispatcher.scheduler.advanceUntilIdle()

    val stateBeforeAccept = viewModel.uiStateEventCreation.value
    assertFalse(stateBeforeAccept.isAiProposalValid)
    assertTrue(stateBeforeAccept.aiProposalTitleValid is ValidationState.Invalid)

    viewModel.acceptProposal()

    val stateAfter = viewModel.uiStateEventCreation.value
    assertTrue(stateAfter.isAiAssistVisible)
    assertEquals("", stateAfter.name)
  }

  @Test
  fun loadFunctionLoadEventParameters() = runTest {
    val sampleEvent = EventTestData.futureEventNoTags
    eventRepository.addEvent(sampleEvent)
    viewModel.loadUid(sampleEvent.id)
    testDispatcher.scheduler.advanceUntilIdle()
    val uiState = viewModel.uiStateEventCreation
    assertEquals(sampleEvent.title, uiState.value.name)
    assertEquals(sampleEvent.description, uiState.value.description)
    assertEquals(sampleEvent.date.toLocalDate(), uiState.value.date)
    assertEquals(viewModel.formatTime(sampleEvent.date.toLocalTime()), uiState.value.time)
    assertEquals(sampleEvent.isPrivate, uiState.value.isPrivate)
  }

  @Test
  fun loadFunctionLoadEventParametersWhenNull() = runTest {
    val sampleEvent = EventTestData.futureEventNoTags
    eventRepository.addEvent(sampleEvent)
    viewModel.loadUid(null)
    testDispatcher.scheduler.advanceUntilIdle()
    val uiState = viewModel.uiStateEventCreation
    assertEquals("", uiState.value.name)
    assertEquals(null, uiState.value.description)
    assertEquals(null, uiState.value.date)
    assertEquals("", uiState.value.time)
    assertEquals(false, uiState.value.isPrivate)
  }

  @Test
  fun testFormatTime() {
    val time = LocalTime.of(14, 30)
    val formatted = viewModel.formatTime(time)

    assertEquals("14:30", formatted)
  }

  @Test
  fun testFormatTimeWithNull() {
    val formatted = viewModel.formatTime(null)

    assertEquals("Select time", formatted)
  }

  @Test
  fun deleteEventChangeRepository() = runTest {
    eventRepository.addEvent(SAMPLE_EVENT)
    viewModel.deleteEvent(SAMPLE_EVENT.id)
    testDispatcher.scheduler.advanceUntilIdle()
    assertEquals(0, eventRepository.getAllEvents(SAMPLE_EVENT.creator, emptySet()).size)
  }

  @Test
  fun deleteNonExistingEvent() = runTest {
    viewModel.deleteEvent(SAMPLE_EVENT.id)
    testDispatcher.scheduler.advanceUntilIdle()
    assertEquals(0, eventRepository.getAllEvents(SAMPLE_EVENT.creator, emptySet()).size)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @After
  fun tearDown() {
    Dispatchers.resetMain()
    unmockkObject(DefaultDP)
  }
}
