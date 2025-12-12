package com.android.universe.ui.eventCreation

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.universe.di.DefaultDP
import com.android.universe.model.ai.gemini.EventProposal
import com.android.universe.model.ai.gemini.GeminiEventAssistant
import com.android.universe.model.event.EventRepository
import com.android.universe.model.event.EventRepositoryProvider
import com.android.universe.model.event.EventTemporaryRepository
import com.android.universe.model.event.EventTemporaryRepositoryProvider
import com.android.universe.model.image.ImageBitmapManager
import com.android.universe.model.location.Location
import com.android.universe.ui.common.InputLimits
import com.android.universe.ui.common.ValidationState
import com.android.universe.ui.common.validateDateTime
import com.android.universe.ui.common.validateDescription
import com.android.universe.ui.common.validateEventDate
import com.android.universe.ui.common.validateEventTitle
import com.android.universe.ui.common.validateTime
import com.android.universe.ui.eventCreation.EventCreationViewModel.Companion.AiErrors.DESCRIPTION_TOO_LONG_FMT
import com.android.universe.ui.eventCreation.EventCreationViewModel.Companion.AiErrors.TITLE_TOO_LONG_FMT
import com.android.universe.ui.utils.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

enum class OnboardingState {
  ENTER_EVENT_TITLE,
  ENTER_DESCRIPTION,
  ENTER_TIME
}

/**
 * Represent the UiSate of the EventCreationScreen.
 *
 * @param name the name of the event.
 * @param description the description of the event.
 * @param date the date of the event.
 * @param time the time of the event.
 * @param titleError the error message for the title.
 * @param dateError the error message for the date.
 * @param timeError the error message for the time.
 * @param eventPicture the picture of the event.
 * @param onboardingState the map that give a boolean for each OnboardingState depending if the text
 *   field has already been changed.
 * @param isAiAssistVisible true if the AI assistant UI should be shown.
 * @param isGenerating true if the AI is currently generating a proposal.
 * @param generationError error message if AI generation failed.
 * @param proposal the AI generated event proposal (title and description).
 * @param aiPrompt the user's input prompt for the AI.
 * @param aiPromptError validation error for the AI prompt.
 */
data class EventCreationUIState(
    val name: String = "",
    val description: String? = null,
    val date: LocalDate? = null,
    val time: String = "",
    val titleError: String? = null,
    val dateError: String? = null,
    val timeError: String? = null,
    val eventPicture: ByteArray? = null,
    val isPrivate: Boolean = false,
    val onboardingState: MutableMap<OnboardingState, Boolean> =
        mutableMapOf(
            OnboardingState.ENTER_EVENT_TITLE to false,
            OnboardingState.ENTER_DESCRIPTION to false,
            OnboardingState.ENTER_TIME to false),
    val isAiAssistVisible: Boolean = false,
    val isGenerating: Boolean = false,
    val generationError: String? = null,
    val proposal: EventProposal? = null,
    val aiPrompt: String = "",
    val aiPromptError: String? = null
) {
  /** Keep the ValidationState of the event title. */
  val eventTitleValid: ValidationState
    get() = validateEventTitle(name)

  /** Keep the ValidationState of the event title. */
  val eventDescriptionValid: ValidationState
    get() =
        if (description == null) {
          ValidationState.Valid
        } else {
          validateDescription(description)
        }

  /** Keep the ValidationState of the event date. */
  val eventDateValid: ValidationState
    get() = validateEventDate(date)

  /** Keep the ValidationState of the event time. */
  val eventTimeValid: ValidationState
    get() = validateTime(time)

  /** Keep the ValidationState of the combination of the date and the time of the event */
  val eventDateTimeValid: ValidationState
    get() = validateDateTime(date, time)

  /**
   * Computed validation state for the AI Prompt.
   *
   * It handles the Neutral state: if the prompt is blank but no error is set (initial state), it is
   * Neutral.
   */
  val aiPromptValid: ValidationState
    get() {
      if (aiPromptError != null) return ValidationState.Invalid(aiPromptError)
      if (aiPrompt.isBlank()) return ValidationState.Neutral
      return ValidationState.Valid
    }

  /** Computed validation for the AI Proposal Title length. Checks against [InputLimits]. */
  val aiProposalTitleValid: ValidationState
    get() {
      val p = proposal ?: return ValidationState.Neutral
      return if (p.title.length <= InputLimits.TITLE_EVENT_MAX_LENGTH) {
        ValidationState.Valid
      } else {
        ValidationState.Invalid(
            TITLE_TOO_LONG_FMT.format(p.title.length, InputLimits.TITLE_EVENT_MAX_LENGTH))
      }
    }

  /** Computed validation for the AI Proposal Description length. Checks against [InputLimits]. */
  val aiProposalDescriptionValid: ValidationState
    get() {
      val p = proposal ?: return ValidationState.Neutral
      return if (p.description.length <= InputLimits.DESCRIPTION) {
        ValidationState.Valid
      } else {
        ValidationState.Invalid(
            DESCRIPTION_TOO_LONG_FMT.format(p.description.length, InputLimits.DESCRIPTION))
      }
    }

  /** Helper to check if the proposal is valid overall. */
  val isAiProposalValid: Boolean
    get() =
        aiProposalTitleValid is ValidationState.Valid &&
            aiProposalDescriptionValid is ValidationState.Valid
}

/**
 * ViewModel of the EventCreationScreen. Manage the data of the Screen and save the Event in the
 * repository.
 *
 * @param eventRepository The repository for the event.
 * @param eventTemporaryRepository The temporary repository fot the event.
 * @param eventTemporaryRepository The temporary repository for the event.
 * @param gemini The AI assistant used for generating event proposals.
 */
class EventCreationViewModel(
    private val imageManager: ImageBitmapManager,
    private val eventRepository: EventRepository = EventRepositoryProvider.repository,
    private val eventTemporaryRepository: EventTemporaryRepository =
        EventTemporaryRepositoryProvider.repository,
    private val gemini: GeminiEventAssistant = GeminiEventAssistant()
) : ViewModel() {

  companion object {
    const val MISSING_DATE_TEXT = "Please select a date"

    /** Centralized error messages for AI features. */
    object AiErrors {
      const val PROMPT_EMPTY = "Prompt cannot be empty"
      const val GENERATION_FAILED = "AI could not generate a proposal. Please try again."
      const val CONTENT_TOO_LONG_UI =
          "Sorry, the AI generated content that is too long. Please reduce or regenerate."
      const val TITLE_TOO_LONG_FMT = "Title too long (%d/%d)"
      const val DESCRIPTION_TOO_LONG_FMT = "Description too long (%d/%d)"
    }

    /**
     * Factory to create an instance of [EventCreationViewModel].
     *
     * This factory is required to inject the [Context] needed for [ImageBitmapManager] and the
     * repositories into the ViewModel.
     *
     * @param context The context used to initialize the image manager.
     */
    fun provideFactory(context: Context): ViewModelProvider.Factory = viewModelFactory {
      EventCreationViewModel(
          imageManager = ImageBitmapManager(context.applicationContext),
          eventRepository = EventRepositoryProvider.repository,
          eventTemporaryRepository = EventTemporaryRepositoryProvider.repository)
    }
  }

  /**
   * Loads an existing event from its unique identifier and updates the UI state of the event
   * creation/editing screen accordingly.
   *
   * This function is typically used when entering the event edition flow. It retrieves the event
   * corresponding to the given `uid`, extracts its fields, and populates the `eventCreationUiState`
   * so that the UI is pre-filled with the event's current data.
   *
   * The event is loaded asynchronously using a ViewModel coroutine on the I/O dispatcher.
   *
   * @param uid The unique identifier of the event to load. If null, nothing is loaded.
   */
  fun loadUid(uid: String?) {
    viewModelScope.launch(DefaultDP.io) {
      if (uid != null) {
        val event = eventRepository.getEvent(uid)
        val eventDate = event.date.toLocalDate()
        val eventTime = event.date.toLocalTime()
        eventCreationUiState.value =
            eventCreationUiState.value.copy(
                name = event.title,
                description = event.description ?: "",
                date = eventDate,
                time = formatTime(eventTime),
                isPrivate = event.isPrivate)
      }
    }
  }

  private val eventCreationUiState = MutableStateFlow(EventCreationUIState())
  val uiStateEventCreation = eventCreationUiState.asStateFlow()

  /**
   * Updates the onboarding state for a specific step in event creation.
   *
   * @param state The [OnboardingState] to update.
   * @param value True if the step is active or completed, false otherwise.
   */
  fun setOnboardingState(state: OnboardingState, value: Boolean) {
    eventCreationUiState.value.onboardingState[state] = value
  }

  /** Check that all the parameters require to create an event are well input by the user. */
  fun validateAll(): Boolean {
    val uiStateValue = eventCreationUiState.value
    return (uiStateValue.eventTitleValid is ValidationState.Valid &&
        (uiStateValue.eventDescriptionValid is ValidationState.Valid ||
            uiStateValue.eventDescriptionValid is ValidationState.Neutral) &&
        uiStateValue.eventDateValid is ValidationState.Valid &&
        uiStateValue.eventTimeValid is ValidationState.Valid &&
        uiStateValue.eventDateTimeValid is ValidationState.Valid)
  }

  /**
   * Update the name of the event.
   *
   * @param name the new event's name.
   */
  fun setEventName(name: String) {
    val finalName = name.take(InputLimits.TITLE_EVENT_MAX_LENGTH + 1)
    eventCreationUiState.value = eventCreationUiState.value.copy(name = finalName)
  }

  /**
   * Update the description of the event.
   *
   * @param description the new event's description.
   */
  fun setEventDescription(description: String) {
    val finalDescription = description.take(InputLimits.DESCRIPTION + 1)
    eventCreationUiState.value = eventCreationUiState.value.copy(description = finalDescription)
  }

  /**
   * Update the privacy setting of the event.
   *
   * @param isPrivate true if the event should be private (followers only).
   */
  fun setPrivacy(isPrivate: Boolean) {
    eventCreationUiState.value = eventCreationUiState.value.copy(isPrivate = isPrivate)
  }

  /**
   * Updates the event image state with the provided URI.
   *
   * @param uri the URI of the image selected by the user.
   */
  fun setImage(uri: Uri?) {
    if (uri == null) {
      eventCreationUiState.value = eventCreationUiState.value.copy(eventPicture = null)
    } else {
      viewModelScope.launch(DefaultDP.io) {
        val byteArray = imageManager.resizeAndCompressImage(uri)
        eventCreationUiState.value = eventCreationUiState.value.copy(eventPicture = byteArray)
      }
    }
  }

  /** Removes the currently selected event image. */
  fun deleteImage() {
    eventCreationUiState.value = eventCreationUiState.value.copy(eventPicture = null)
  }

  /**
   * Update the date of the event.
   *
   * @param date the new event's date.
   */
  fun setDate(date: LocalDate?) {
    if (date == null) {
      eventCreationUiState.value = eventCreationUiState.value.copy(dateError = MISSING_DATE_TEXT)
    } else {
      eventCreationUiState.value =
          eventCreationUiState.value.copy(date = date, dateError = null, timeError = null)
    }
  }

  /**
   * Update the time of the event.
   *
   * @param time the new event's time.
   */
  fun setTime(time: String) {
    val finalTime = time.take(InputLimits.TIME_MAX_LENGTH)
    eventCreationUiState.value = eventCreationUiState.value.copy(time = finalTime)
  }

  private val formatter: DateTimeFormatter? = DateTimeFormatter.ofPattern("dd/MM/yyyy")
  private val timeFormatter: DateTimeFormatter? = DateTimeFormatter.ofPattern("HH:mm")

  /**
   * Format the date to a string.
   *
   * @param date the date to format.
   */
  fun formatDate(date: LocalDate?): String {
    return if (date == null) "Select date" else date.format(formatter)
  }

  /**
   * Format the date to a string.
   *
   * @param date the date to format.
   */
  fun formatTime(time: LocalTime?): String {
    return if (time == null) "Select time" else time.format(timeFormatter)
  }

  /**
   * Save the event with all the parameters selected by the user in the event repository.
   *
   * @param uidUser the uid of the Current User.
   * @param location the location of the event.
   */
  fun saveEvent(uidUser: String, uidEvent: String?, location: Location) {
    if (validateAll()) {
      viewModelScope.launch {
        try {
          val id = uidEvent ?: eventRepository.getNewID()

          val internalDate = uiStateEventCreation.value.date
          val internalTime = LocalTime.parse(uiStateEventCreation.value.time, timeFormatter)

          val eventDateTime = LocalDateTime.of(internalDate, internalTime)

          eventTemporaryRepository.updateEvent(
              id = id,
              title = eventCreationUiState.value.name,
              description = eventCreationUiState.value.description,
              dateTime = eventDateTime,
              creator = uidUser,
              participants = setOf(uidUser),
              location = location,
              isPrivate = eventCreationUiState.value.isPrivate,
              eventPicture = eventCreationUiState.value.eventPicture)
        } catch (e: Exception) {
          Log.e("EventCreationViewModel", "Error saving event: ${e.message}")
        }
      }
    }
  }

  /**
   * Updates the AI prompt text in the state.
   *
   * Performs real-time validation: if the prompt is blank (and not in initial state), an error is
   * set.
   *
   * @param prompt The new prompt text input by the user.
   */
  fun setAiPrompt(prompt: String) {
    val error = if (prompt.isBlank()) AiErrors.PROMPT_EMPTY else null
    eventCreationUiState.value =
        eventCreationUiState.value.copy(aiPrompt = prompt, aiPromptError = error)
  }

  /**
   * Initializes and shows the AI assistance view.
   *
   * Resets any previous AI state (proposal, errors, prompt) to ensure a clean slate.
   */
  fun showAiAssist() {
    eventCreationUiState.value =
        eventCreationUiState.value.copy(
            isAiAssistVisible = true,
            proposal = null,
            generationError = null,
            aiPrompt = "",
            aiPromptError = null)
  }

  /** Hides the AI assistance view and returns to the standard event creation form. */
  fun hideAiAssist() {
    eventCreationUiState.value = eventCreationUiState.value.copy(isAiAssistVisible = false)
  }

  /**
   * Triggers the AI generation process based on the current prompt.
   *
   * If the prompt is valid, it sets the loading state and launches a coroutine to fetch the
   * proposal from [GeminiEventAssistant]. Updates the state with either the result or an error.
   */
  fun generateProposal(geoPoint: Location) {
    val prompt = eventCreationUiState.value.aiPrompt
    if (prompt.isBlank()) {
      eventCreationUiState.value =
          eventCreationUiState.value.copy(aiPromptError = AiErrors.PROMPT_EMPTY)
      return
    }

    eventCreationUiState.value =
        eventCreationUiState.value.copy(isGenerating = true, generationError = null)

    viewModelScope.launch {
      val result = gemini.generateProposal(prompt, geoPoint)
      if (result != null) {
        eventCreationUiState.value =
            eventCreationUiState.value.copy(isGenerating = false, proposal = result)
      } else {
        eventCreationUiState.value =
            eventCreationUiState.value.copy(
                isGenerating = false, generationError = AiErrors.GENERATION_FAILED)
      }
    }
  }

  /**
   * Update the title of the AI proposal.
   *
   * @param title the new title.
   */
  fun updateProposalTitle(title: String) {
    val currentProposal = eventCreationUiState.value.proposal ?: return
    eventCreationUiState.value =
        eventCreationUiState.value.copy(proposal = currentProposal.copy(title = title))
  }

  /**
   * Update the description of the AI proposal.
   *
   * @param description the new description.
   */
  fun updateProposalDescription(description: String) {
    val currentProposal = eventCreationUiState.value.proposal ?: return
    eventCreationUiState.value =
        eventCreationUiState.value.copy(proposal = currentProposal.copy(description = description))
  }

  /**
   * Accepts the AI generated proposal.
   *
   * If the proposal is valid (meets length constraints), it populates the main event title and
   * description fields, marks the onboarding steps as active, and closes the AI assistant.
   */
  fun acceptProposal() {
    val p = eventCreationUiState.value.proposal ?: return

    if (!eventCreationUiState.value.isAiProposalValid) return

    setEventName(p.title)
    setEventDescription(p.description)

    setOnboardingState(OnboardingState.ENTER_EVENT_TITLE, true)
    setOnboardingState(OnboardingState.ENTER_DESCRIPTION, true)

    hideAiAssist()
  }
}
