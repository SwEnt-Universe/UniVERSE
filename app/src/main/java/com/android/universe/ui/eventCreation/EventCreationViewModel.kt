package com.android.universe.ui.eventCreation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.universe.model.Tag
import com.android.universe.model.event.Event
import com.android.universe.model.event.EventRepository
import com.android.universe.model.event.EventRepositoryProvider
import com.android.universe.model.location.Location
import com.android.universe.model.user.UserRepository
import com.android.universe.model.user.UserRepositoryProvider
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
 * @param tags the tags of the events.
 */
data class EventCreationUIState(
    val name: String = "",
    val description: String = "",
    val day: String = "",
    val month: String = "",
    val year: String = "",
    val hour: String = "",
    val minute: String = "",
    val tags: Set<Tag> = emptySet(),
)

/**
 * ViewModel of the EventCreationScreen. Manage the data of the Screen and save the Event in the
 * repository.
 *
 * @param eventRepository the repository for the event.
 * @param userRepository the repository for the user.
 */
class EventCreationViewModel(
    private val eventRepository: EventRepository = EventRepositoryProvider.repository,
    private val userRepository: UserRepository = UserRepositoryProvider.repository
) : ViewModel() {
  private val eventCreationUiState = MutableStateFlow(EventCreationUIState())
  val uiStateEventCreation = eventCreationUiState.asStateFlow()

  /**
   * Update the name of the event.
   *
   * @param name the new event's name.
   */
  fun setEventName(name: String) {
    eventCreationUiState.value = eventCreationUiState.value.copy(name = name)
  }

  /**
   * Update the description of the event.
   *
   * @param description the new event's description.
   */
  fun setEventDescription(description: String) {
    eventCreationUiState.value = eventCreationUiState.value.copy(description = description)
  }

  /**
   * Update the day of the event.
   *
   * @param day the new event's day.
   */
  fun setEventDay(day: String) {
    eventCreationUiState.value = eventCreationUiState.value.copy(day = day)
  }

  /**
   * Update the month of the event.
   *
   * @param month the new event's month.
   */
  fun setEventMonth(month: String) {
    eventCreationUiState.value = eventCreationUiState.value.copy(month = month)
  }

  /**
   * Update the year of the event.
   *
   * @param year the new event's year.
   */
  fun setEventYear(year: String) {
    eventCreationUiState.value = eventCreationUiState.value.copy(year = year)
  }

  /**
   * Update the hour of the event.
   *
   * @param hour the new event's hour.
   */
  fun setEventHour(hour: String) {
    eventCreationUiState.value = eventCreationUiState.value.copy(hour = hour)
  }

  /**
   * Update the minute of the event.
   *
   * @param minute the new event's minute.
   */
  fun setEventMinute(minute: String) {
    eventCreationUiState.value = eventCreationUiState.value.copy(minute = minute)
  }

  /**
   * Update the tags of the event.
   *
   * @param tags the new event's tags.
   */
  fun setEventTags(tags: Set<Tag>) {
    eventCreationUiState.value = eventCreationUiState.value.copy(tags = tags)
  }

  /**
   * Save the event with all the parameters selected by the user in the event repository.
   *
   * @param location the location of the event.
   * @param uid the uid of the Current User.
   */
  fun saveEvent(location: Location, uid: String) {
    viewModelScope.launch {
      try {
        val id = eventRepository.getNewID()
        val date =
            eventCreationUiState.value.day +
                "/" +
                eventCreationUiState.value.month +
                "/" +
                eventCreationUiState.value.year
        val time = eventCreationUiState.value.hour + ":" + eventCreationUiState.value.minute
        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        val localDate = LocalDate.parse(date, dateFormatter)
        val localTime = LocalTime.parse(time, timeFormatter)

        val eventDateTime = LocalDateTime.of(localDate, localTime)

        val creator = userRepository.getUser(uid)
        val event =
            Event(
                id = id,
                title = eventCreationUiState.value.name,
                description = eventCreationUiState.value.description,
                date = eventDateTime,
                tags = eventCreationUiState.value.tags,
                creator = creator,
                participants = setOf(creator),
                location = location)
        eventRepository.addEvent(event)
      } catch (e: Exception) {
        Log.e("EventCreationViewModel", "Error saving event: ${e.message}")
      }
    }
  }
}
