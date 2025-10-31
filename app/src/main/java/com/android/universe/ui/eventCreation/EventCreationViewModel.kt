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

class EventCreationViewModel(
    private val eventRepository: EventRepository = EventRepositoryProvider.repository,
    private val userRepository: UserRepository = UserRepositoryProvider.repository
) : ViewModel() {
  private val eventCreationUiState = MutableStateFlow(EventCreationUIState())
  val uiStateEventCreation = eventCreationUiState.asStateFlow()

  fun setEventName(name: String) {
    eventCreationUiState.value = eventCreationUiState.value.copy(name = name)
  }

  fun setEventDescription(description: String) {
    eventCreationUiState.value = eventCreationUiState.value.copy(description = description)
  }

  fun setEventDay(day: String) {
    eventCreationUiState.value = eventCreationUiState.value.copy(day = day)
  }

  fun setEventMonth(month: String) {
    eventCreationUiState.value = eventCreationUiState.value.copy(month = month)
  }

  fun setEventYear(year: String) {
    eventCreationUiState.value = eventCreationUiState.value.copy(year = year)
  }

  fun setEventHour(hour: String) {
    eventCreationUiState.value = eventCreationUiState.value.copy(hour = hour)
  }

  fun setEventMinute(minute: String) {
    eventCreationUiState.value = eventCreationUiState.value.copy(minute = minute)
  }

  fun setEventTags(tags: Set<Tag>) {
    eventCreationUiState.value = eventCreationUiState.value.copy(tags = tags)
  }

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
