package com.android.universe.ui.eventCreation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.universe.di.DefaultDP
import com.android.universe.model.event.Event
import com.android.universe.model.event.EventRepository
import com.android.universe.model.event.EventRepositoryProvider
import com.android.universe.model.location.Location
import com.android.universe.model.tag.Tag
import com.android.universe.model.tag.TagTemporaryRepository
import com.android.universe.model.tag.TagTemporaryRepositoryProvider
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
 * @param titleError the error message for the title.
 * @param dayError the error message for the day.
 * @param monthError the error message for the month.
 * @param yearError the error message for the year.
 * @param hourError the error message for the hour.
 * @param minuteError the error message for the minute.
 */
data class EventCreationUIState(
    val name: String = "",
    val description: String? = null,
    val day: String = "",
    val month: String = "",
    val year: String = "",
    val hour: String = "",
    val minute: String = "",
    val titleError: String? = "Title cannot be empty",
    val dayError: String? = "Day cannot be empty",
    val monthError: String? = "Month cannot be empty",
    val yearError: String? = "Year cannot be empty",
    val hourError: String? = "Hour cannot be empty",
    val minuteError: String? = "Minute cannot be empty",
    val eventPicture: ByteArray? = null
)

/**
 * Object that contains the different limit of character for the textFields in the Event creation
 * screen.
 */
object EventInputLimits {
  const val TITLE_MAX_LENGTH = 40
  const val DESCRIPTION_MAX_LENGTH = 200
  const val DAY_MAX_LENGTH = 2
  const val MONTH_MAX_LENGTH = 2
  const val YEAR_MAX_LENGTH = 4
  const val HOUR_MAX_LENGTH = 2
  const val MINUTE_MAX_LENGTH = 2
}

/**
 * ViewModel of the EventCreationScreen. Manage the data of the Screen and save the Event in the
 * repository.
 *
 * @param eventRepository the repository for the event.
 * @param tagRepository The repository for the tags.
 */
class EventCreationViewModel(
    private val eventRepository: EventRepository = EventRepositoryProvider.repository,
    private val tagRepository: TagTemporaryRepository = TagTemporaryRepositoryProvider.repository,
) : ViewModel() {
  private val eventCreationUiState = MutableStateFlow(EventCreationUIState())
  val uiStateEventCreation = eventCreationUiState.asStateFlow()
  private val _eventTags = MutableStateFlow(emptySet<Tag>())
  val eventTags = _eventTags.asStateFlow()

  /** We launch a coroutine that will update the set of tag each time the tag repository change. */
  init {
    viewModelScope.launch {
      tagRepository.tagsFlow.collect { newTags -> _eventTags.value = newTags }
    }
  }

  /**
   * Update the title error message of the uiState.
   *
   * @param errorMessage the new message to display for the title textField.
   */
  private fun setTitleError(errorMessage: String?) {
    eventCreationUiState.value = eventCreationUiState.value.copy(titleError = errorMessage)
  }

  /**
   * Update the day error message of the uiState.
   *
   * @param errorMessage the new message to display for the day textField.
   */
  private fun setDayError(errorMessage: String?) {
    eventCreationUiState.value = eventCreationUiState.value.copy(dayError = errorMessage)
  }

  /**
   * Update the month error message of the uiState.
   *
   * @param errorMessage the new message to display for the month textField.
   */
  private fun setMonthError(errorMessage: String?) {
    eventCreationUiState.value = eventCreationUiState.value.copy(monthError = errorMessage)
  }

  /**
   * Update the year error message of the uiState.
   *
   * @param errorMessage the new message to display for the year textField.
   */
  private fun setYearError(errorMessage: String?) {
    eventCreationUiState.value = eventCreationUiState.value.copy(yearError = errorMessage)
  }

  /**
   * Update the hour error message of the uiState.
   *
   * @param errorMessage the new message to display for the hour textField.
   */
  private fun setHourError(errorMessage: String?) {
    eventCreationUiState.value = eventCreationUiState.value.copy(hourError = errorMessage)
  }

  /**
   * Update the minute error message of the uiState.
   *
   * @param errorMessage the new message to display for the minute textField.
   */
  private fun setMinuteError(errorMessage: String?) {
    eventCreationUiState.value = eventCreationUiState.value.copy(minuteError = errorMessage)
  }

  /**
   * Check that the new title input is not empty and respect a certain format.
   *
   * @param title the new title input.
   */
  private fun validateTitle(title: String): Boolean {
    if (title.isEmpty()) {
      setTitleError("Title cannot be empty")
      return false
    } else {
      setTitleError(null)
      return true
    }
  }

  /**
   * Check that the new day input is not empty and respect a certain format.
   *
   * @param day the new day input.
   */
  private fun validateDay(day: String): Boolean {
    if (day.isEmpty()) {
      setDayError("Day cannot be empty")
      return false
    } else if (day.toIntOrNull() == null) {
      setDayError("Day should be a valid number")
      return false
    } else if (day.toInt() !in 1..31) {
      setDayError("Day should be between 1 and 31")
      return false
    } else {
      setDayError(null)
      return true
    }
  }

  /**
   * Check that the new month input is not empty and respect a certain format.
   *
   * @param month the new month input.
   */
  private fun validateMonth(month: String): Boolean {
    if (month.isEmpty()) {
      setMonthError("Month cannot be empty")
      return false
    } else if (month.toIntOrNull() == null) {
      setMonthError("Month should be a valid number")
      return false
    } else if (month.toInt() !in 1..12) {
      setMonthError("Month should be between 1 and 12")
      return false
    } else {
      setMonthError(null)
      return true
    }
  }

  /**
   * Check that the new year input is not empty and respect a certain format.
   *
   * @param year the new year input.
   */
  private fun validateYear(year: String): Boolean {
    if (year.isEmpty()) {
      setYearError("Year cannot be empty")
      return false
    } else if (year.toIntOrNull() == null) {
      setYearError("Year should be a valid number")
      return false
    } else if (year.toInt() < 2025 || year.length < 4) {
      setYearError("Enter a valid 4-digit year (2025 or later)")
      return false
    } else {
      setYearError(null)
      return true
    }
  }

  /**
   * Check that the new hour input is not empty and respect a certain format.
   *
   * @param hour the new hour input.
   */
  private fun validateHour(hour: String): Boolean {
    if (hour.isEmpty()) {
      setHourError("Hour cannot be empty")
      return false
    } else if (hour.toIntOrNull() == null) {
      setHourError("Hour should be a valid number")
      return false
    } else if (hour.toInt() !in 0..23) {
      setHourError("Hour should be between 0 and 23")
      return false
    } else {
      setHourError(null)
      return true
    }
  }

  /**
   * Check that the new minute input is not empty and respect a certain format.
   *
   * @param minute the new minute input.
   */
  private fun validateMinute(minute: String): Boolean {
    if (minute.isEmpty()) {
      setMinuteError("Minute cannot be empty")
      return false
    } else if (minute.toIntOrNull() == null) {
      setMinuteError("Minute should be a valid number")
      return false
    } else if (minute.toInt() !in 0..59) {
      setMinuteError("Minute should be between 0 and 59")
      return false
    } else {
      setMinuteError(null)
      return true
    }
  }

  /** Check that all the parameters enter in the textFields are not empty and are well written. */
  fun validateAll(): Boolean {
    return (eventCreationUiState.value.titleError == null &&
        eventCreationUiState.value.dayError == null &&
        eventCreationUiState.value.monthError == null &&
        eventCreationUiState.value.yearError == null &&
        eventCreationUiState.value.hourError == null &&
        eventCreationUiState.value.minuteError == null)
  }

  /**
   * Update the name of the event.
   *
   * @param name the new event's name.
   */
  fun setEventName(name: String) {
    if (name.length <= EventInputLimits.TITLE_MAX_LENGTH) {
      eventCreationUiState.value = eventCreationUiState.value.copy(name = name)
      validateTitle(name)
    }
  }

  /**
   * Update the description of the event.
   *
   * @param description the new event's description.
   */
  fun setEventDescription(description: String) {
    if (description.length <= EventInputLimits.DESCRIPTION_MAX_LENGTH) {
      eventCreationUiState.value = eventCreationUiState.value.copy(description = description)
    }
  }

  /**
   * Update the day of the event.
   *
   * @param day the new event's day.
   */
  fun setEventDay(day: String) {
    if (day.length <= EventInputLimits.DAY_MAX_LENGTH) {
      eventCreationUiState.value = eventCreationUiState.value.copy(day = day)
      validateDay(day)
    }
  }

  /**
   * Update the month of the event.
   *
   * @param month the new event's month.
   */
  fun setEventMonth(month: String) {
    if (month.length <= EventInputLimits.MONTH_MAX_LENGTH) {
      eventCreationUiState.value = eventCreationUiState.value.copy(month = month)
      validateMonth(month)
    }
  }

  /**
   * Update the year of the event.
   *
   * @param year the new event's year.
   */
  fun setEventYear(year: String) {
    if (year.length <= EventInputLimits.YEAR_MAX_LENGTH) {
      eventCreationUiState.value = eventCreationUiState.value.copy(year = year)
      validateYear(year)
    }
  }

  /**
   * Update the hour of the event.
   *
   * @param hour the new event's hour.
   */
  fun setEventHour(hour: String) {
    if (hour.length <= EventInputLimits.HOUR_MAX_LENGTH) {
      eventCreationUiState.value = eventCreationUiState.value.copy(hour = hour)
      validateHour(hour)
    }
  }

  /**
   * Update the minute of the event.
   *
   * @param minute the new event's minute.
   */
  fun setEventMinute(minute: String) {
    if (minute.length <= EventInputLimits.MINUTE_MAX_LENGTH) {
      eventCreationUiState.value = eventCreationUiState.value.copy(minute = minute)
      validateMinute(minute)
    }
  }

  /**
   * Update the tags of the event. Only for test purposes.
   *
   * @param tags the new event's tags.
   */
  fun setEventTags(tags: Set<Tag>) {
    viewModelScope.launch { tagRepository.updateTags(tags) }
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
   * TODO
   */
  fun setDate(date: LocalDate) {
    eventCreationUiState.value = eventCreationUiState.value.copy(
      day = date.dayOfMonth.toString(),
      month = date.monthValue.toString(),
      year = date.year.toString(),
      dayError = null,
      monthError = null,
      yearError = null
    )
  }
  /**
   * Save the event with all the parameters selected by the user in the event repository.
   *
   * @param location the location of the event.
   * @param uid the uid of the Current User.
   */
  fun saveEvent(location: Location, uid: String) {
    if (validateAll()) {
      viewModelScope.launch {
        try {
          val id = eventRepository.getNewID()

          val realDay = eventCreationUiState.value.day.padStart(2, '0')

          val realMonth = eventCreationUiState.value.month.padStart(2, '0')

          val realHour = eventCreationUiState.value.hour.padStart(2, '0')

          val realMinute = eventCreationUiState.value.minute.padStart(2, '0')

          val date = realDay + "/" + realMonth + "/" + eventCreationUiState.value.year

          val time = "$realHour:$realMinute"

          val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
          val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

          val localDate =
              try {
                LocalDate.parse(date, dateFormatter)
              } catch (e: Exception) {
                setDayError("Please enter a valid date (e.g. no 30th of February)")
                return@launch
              }
          val localTime = LocalTime.parse(time, timeFormatter)

          val eventDateTime = LocalDateTime.of(localDate, localTime)

          val event =
              Event(
                  id = id,
                  title = eventCreationUiState.value.name,
                  description = eventCreationUiState.value.description,
                  date = eventDateTime,
                  tags = _eventTags.value,
                  creator = uid,
                  participants = setOf(uid),
                  location = location,
                  eventPicture = eventCreationUiState.value.eventPicture)
          eventRepository.addEvent(event)
          // The event is saved, we can now delete the current tag Set for the event.
          tagRepository.deleteAllTags()
        } catch (e: Exception) {
          Log.e("EventCreationViewModel", "Error saving event: ${e.message}")
        }
      }
    }
  }
}
