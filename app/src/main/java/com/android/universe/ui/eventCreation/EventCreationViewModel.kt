package com.android.universe.ui.eventCreation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.universe.di.DefaultDP
import com.android.universe.model.event.EventRepository
import com.android.universe.model.event.EventRepositoryProvider
import com.android.universe.model.event.EventTemporaryRepository
import com.android.universe.model.event.EventTemporaryRepositoryProvider
import com.android.universe.model.location.Location
import com.android.universe.ui.common.InputLimits
import com.android.universe.ui.common.ValidationState
import com.android.universe.ui.common.validateDateTime
import com.android.universe.ui.common.validateDescription
import com.android.universe.ui.common.validateEventDate
import com.android.universe.ui.common.validateEventTitle
import com.android.universe.ui.common.validateLocation
import com.android.universe.ui.common.validateTime
import com.android.universe.ui.theme.Dimensions
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
 * @param day the day of the event.
 * @param month the month of the event.
 * @param year the year of the event.
 * @param hour the hour of the event.
 * @param minute the minute of the event.
 * @param date the date of the event.
 * @param time the time of the event.
 * @param titleError the error message for the title.
 * @param dateError the error message for the date.
 * @param timeError the error message for the time.
 * @param eventPicture the picture of the event.
 * @param onboardingState the map that give a boolean for each OnboardingState depending if the text
 *   field has already been changed.
 */
data class EventCreationUIState(
    val name: String = "",
    val description: String? = null,
    val day: String = "",
    val month: String = "",
    val year: String = "",
    val hour: String = "",
    val minute: String = "",
    val date: LocalDate? = null,
    val time: String = "",
    val titleError: String? = null,
    val dateError: String? = null,
    val timeError: String? = null,
    val eventPicture: ByteArray? = null,
    val location: Location? = null,
    val onboardingState: MutableMap<OnboardingState, Boolean> =
        mutableMapOf(
            OnboardingState.ENTER_EVENT_TITLE to false,
            OnboardingState.ENTER_DESCRIPTION to false,
            OnboardingState.ENTER_TIME to false)
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

  val eventLocationValid: ValidationState
    get() = validateLocation(location)
}

/**
 * ViewModel of the EventCreationScreen. Manage the data of the Screen and save the Event in the
 * repository.
 *
 * @param eventRepository The repository for the event.
 * @param eventTemporaryRepository The temporary repository fot the event.
 */
class EventCreationViewModel(
    private val eventRepository: EventRepository = EventRepositoryProvider.repository,
    private val eventTemporaryRepository: EventTemporaryRepository =
        EventTemporaryRepositoryProvider.repository
) : ViewModel() {

  companion object {
    const val MISSING_DATE_TEXT = "Please select a date"
  }

  fun setLocation(lat: Double, lon: Double) {
    eventCreationUiState.value = eventCreationUiState.value.copy(location = Location(lat, lon))
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
        uiStateValue.eventDateTimeValid is ValidationState.Valid &&
        uiStateValue.eventLocationValid is ValidationState.Valid)
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
   * Takes the uri as argument and decode the content of the image, resize it to a 256*256 image,
   * transform it to a byteArray and modify the eventPicture argument of the eventCreationUiState.
   *
   * @param context the context of the UI.
   * @param uri the temporary url that give access to the image that the user selected.
   */
  fun setImage(context: Context, uri: Uri?) {
    if (uri == null) {
      eventCreationUiState.value = eventCreationUiState.value.copy(eventPicture = null)
    } else {
      viewModelScope.launch(DefaultDP.io) {
        // We redimension the image to have a 256*256 image to reduce the space of the
        // image.
        val maxSize = Dimensions.ProfilePictureSize

        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }

        context.contentResolver.openInputStream(uri)?.use { input ->
          BitmapFactory.decodeStream(input, null, options)
        }

        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1
        if (height > maxSize || width > maxSize) {
          val halfHeight = height / 2
          val halfWidth = width / 2
          while ((halfHeight / inSampleSize) >= maxSize && (halfWidth / inSampleSize) >= maxSize) {
            inSampleSize *= 2
          }
        }

        options.inSampleSize = inSampleSize
        options.inJustDecodeBounds = false

        val bitmap =
            context.contentResolver.openInputStream(uri)?.use { input ->
              BitmapFactory.decodeStream(input, null, options)
            }

        if (bitmap == null) {
          Log.e("ImageError", "Failed to decode bitmap from URI $uri")
        } else {
          val stream = ByteArrayOutputStream()
          // We compress the image with a low quality to reduce the space of the image.
          bitmap.compress(Bitmap.CompressFormat.JPEG, 45, stream)
          val byteArray = stream.toByteArray()
          withContext(DefaultDP.main) {
            eventCreationUiState.value = eventCreationUiState.value.copy(eventPicture = byteArray)
          }
        }
      }
    }
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

  val formatter: DateTimeFormatter? = DateTimeFormatter.ofPattern("dd/MM/yyyy")
  val timeFormatter: DateTimeFormatter? = DateTimeFormatter.ofPattern("HH:mm")

  /**
   * Format the date to a string.
   *
   * @param date the date to format.
   */
  fun formatDate(date: LocalDate?): String {
    return if (date == null) "Select date" else date.format(formatter)
  }

  /**
   * Save the event with all the parameters selected by the user in the event repository.
   *
   * @param uid the uid of the Current User.
   */
  fun saveEvent(uid: String) {
    if (validateAll()) {
      viewModelScope.launch {
        try {
          val id = eventRepository.getNewID()

          val internalDate = uiStateEventCreation.value.date
          val internalTime = LocalTime.parse(uiStateEventCreation.value.time, timeFormatter)

          val eventDateTime = LocalDateTime.of(internalDate, internalTime)
          val loc =
              requireNotNull(eventCreationUiState.value.location) {
                "Location must be set before saving the event"
              }

          eventTemporaryRepository.updateEvent(
              id = id,
              title = eventCreationUiState.value.name,
              description = eventCreationUiState.value.description,
              dateTime = eventDateTime,
              creator = uid,
              participants = setOf(uid),
              location = loc,
              eventPicture = eventCreationUiState.value.eventPicture)
        } catch (e: Exception) {
          Log.e("EventCreationViewModel", "Error saving event: ${e.message}")
        }
      }
    }
  }
}
