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
 * @param date the date of the event.
 * @param time the time of the event.
 * @param titleError the error message for the title.
 * @param dateError the error message for the date.
 * @param timeError the error message for the time.
 * @param eventPicture the picture of the event.
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
    val time: LocalTime? = null,
    val titleError: String? = "Title cannot be empty",
    val dateError: String? = null,
    val timeError: String? = null,
    val eventPicture: ByteArray? = null
)

/**
 * Object that contains the different limit of character for the textFields in the Event creation
 * screen.
 */
object EventInputLimits {
  const val TITLE_MAX_LENGTH = 40
  const val DESCRIPTION_MAX_LENGTH = 200
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

  companion object {
    const val FUTURETIMETEXT = "Future time only"
    const val FUTUREDATETEXT = "Future dates only"
    const val MISSINGDATETEXT = "Please select a date"
    const val MISSINGTIMETEXT = "Please select a time"
  }

  private val eventCreationUiState = MutableStateFlow(EventCreationUIState())
  val uiStateEventCreation = eventCreationUiState.asStateFlow()
  private val _eventTags = MutableStateFlow(emptySet<Tag>())
  val eventTags = _eventTags.asStateFlow()
  val noDateText = "No date selected"
  val noTimeText = "No time selected"

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
   * Check that all the parameters enter in the textFields are not empty and are well written. as
   * well as checking that there is a date a no error
   */
  fun validateAll(): Boolean {
    return (eventCreationUiState.value.titleError == null &&
        eventCreationUiState.value.date != null &&
        eventCreationUiState.value.dateError == null &&
        eventCreationUiState.value.time != null &&
        eventCreationUiState.value.timeError == null)
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
   * Update the date of the event.
   *
   * @param date the new event's date.
   */
  fun setDate(date: LocalDate?) {
    if (date == null) {
      eventCreationUiState.value = eventCreationUiState.value.copy(dateError = MISSINGDATETEXT)
    } else if (date.isBefore(LocalDate.now())) {
      eventCreationUiState.value =
          eventCreationUiState.value.copy(date = date, dateError = FUTUREDATETEXT)
    } else if (eventCreationUiState.value.time != null &&
        LocalDateTime.of(date, eventCreationUiState.value.time).isBefore(LocalDateTime.now())) {
      eventCreationUiState.value =
          eventCreationUiState.value.copy(
              date = date, dateError = FUTUREDATETEXT, timeError = FUTURETIMETEXT)
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
  fun setTime(time: LocalTime?) {
    if (time == null) {
      eventCreationUiState.value = eventCreationUiState.value.copy(timeError = MISSINGTIMETEXT)
    } else if (eventCreationUiState.value.date != null &&
        LocalDateTime.of(eventCreationUiState.value.date, time).isBefore(LocalDateTime.now())) {
      eventCreationUiState.value =
          eventCreationUiState.value.copy(
              time = time, dateError = FUTUREDATETEXT, timeError = FUTURETIMETEXT)
    } else {
      eventCreationUiState.value =
          eventCreationUiState.value.copy(time = time, timeError = null, dateError = null)
    }
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
   * Format the time to a string.
   *
   * @param time the time to format.
   */
  fun formatTime(time: LocalTime?): String {
    return if (time == null) "Select time" else time.format(timeFormatter)
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

          val internalDate = uiStateEventCreation.value.date
          val internalTime = uiStateEventCreation.value.time

          val eventDateTime = LocalDateTime.of(internalDate, internalTime)

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
