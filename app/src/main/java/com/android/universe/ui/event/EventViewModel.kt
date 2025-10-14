package com.android.universe.ui.event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.universe.model.event.EventRepository
import com.android.universe.model.event.EventRepositoryProvider
import com.android.universe.model.user.UserProfile
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI state for an event item.
 *
 * @property title The title of the event.
 * @property description A brief description of the event.
 * @property date The formatted date of the event.
 * @property tags A list of tags associated with the event.
 * @property creator The name of the event creator.
 * @property participants The number of participants in the event.
 */
data class EventUIState(
    val title: String = "",
    val description: String = "",
    val date: String = "",
    val tags: List<String> = emptyList(),
    val creator: String = "",
    val participants: Int = 0
)

/**
 * ViewModel for managing event data and state.
 *
 * It fetches events from the [EventRepository], transforms them into [EventUIState], and exposes
 * them via a [StateFlow] for the UI to observe.
 *
 * @param eventRepository The repository used to fetch event data. Defaults to the singleton
 *   instance provided by [EventRepositoryProvider].
 */
class EventViewModel(
    private val eventRepository: EventRepository = EventRepositoryProvider.repository
) : ViewModel() {

  /** Backing property for the list of event UI states. */
  private val _eventsState = MutableStateFlow<List<EventUIState>>(emptyList())

  /** Publicly exposed StateFlow of event UI states. */
  val eventsState: StateFlow<List<EventUIState>> = _eventsState.asStateFlow()

  /** Initializes the ViewModel by loading events. */
  init {
    loadEvents()
  }

  /**
   * Loads events from the repository, transforms them into UI states, and updates the state flow.
   */
  fun loadEvents() {
    viewModelScope.launch {
      val events = eventRepository.getAllEvents()
      val uiStates =
          events.map { event ->
            EventUIState(
                title = event.title,
                description = event.description ?: "",
                date = formatEventDate(event.date),
                tags = event.tags.map { it.name }.take(3),
                creator = formatCreator(event.creator),
                participants = event.participants.count())
          }
      _eventsState.value = uiStates
    }
  }

  /**
   * Formats a [LocalDateTime] into a user-friendly string.
   *
   * @param date The [LocalDateTime] to format.
   * @return A formatted string representing the date and time.
   */
  private fun formatEventDate(date: LocalDateTime): String {
    val formatter = DateTimeFormatter.ofPattern("d MMM hh:mm a", Locale.ENGLISH)
    return date.format(formatter)
  }

  /**
   * Formats a [UserProfile] into a full name string.
   *
   * @param user The [UserProfile] of the event creator.
   * @return A string combining the first and last name.
   */
  private fun formatCreator(user: UserProfile): String {
    return "${user.firstName} ${user.lastName}"
  }
}
