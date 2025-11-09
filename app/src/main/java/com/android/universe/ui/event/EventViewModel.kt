package com.android.universe.ui.event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.universe.model.event.Event
import com.android.universe.model.event.EventRepository
import com.android.universe.model.event.EventRepositoryProvider
import com.android.universe.model.user.UserProfile
import com.android.universe.model.user.UserRepository
import com.android.universe.model.user.UserRepositoryProvider
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
    val participants: Int = 0,
    val index: Int = 0,
    val joined: Boolean = true
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
    private val eventRepository: EventRepository = EventRepositoryProvider.repository,
    private val userRepository: UserRepository = UserRepositoryProvider.repository
) : ViewModel() {

  /** Backing property for the list of event UI states. */
  private val _eventsState = MutableStateFlow<List<EventUIState>>(emptyList())
    private var localList = emptyList<Event>()

  /** Publicly exposed StateFlow of event UI states. */
  val eventsState: StateFlow<List<EventUIState>> = _eventsState.asStateFlow()
  var thisuid = ""
  /** Initializes the ViewModel by loading events. */
  init {
    //loadEvents()
  }

  /**
   * Loads events from the repository, transforms them into UI states, and updates the state flow.
   */
  fun loadEvents() {
      viewModelScope.launch {
          val events = eventRepository.getAllEvents()
          localList = events

          val uiStates = events.mapIndexed { index, event ->
              val isJoined = event.participants.any { it.uid == thisuid }
              EventUIState(
                  title = event.title,
                  description = event.description ?: "",
                  date = formatEventDate(event.date),
                  tags = event.tags.map { it.displayName }.take(3),
                  creator = formatCreator(event.creator),
                  participants = event.participants.size,
                  index = index,
                  joined = isJoined
              )
          }
          _eventsState.value = uiStates
      }
  }

    fun joinOrLeaveEvent(index: Int) {
        viewModelScope.launch {
            val currentState = _eventsState.value.getOrNull(index) ?: return@launch
            val currentEvent = localList.getOrNull(index) ?: return@launch

            val isJoined = currentState.joined
            val updatedParticipants = if (isJoined) {
                currentEvent.participants.filterNot { it.uid == thisuid }.toSet()
            } else {
                currentEvent.participants + userRepository.getUser(thisuid)
            }

            val updatedEvent = currentEvent.copy(participants = updatedParticipants)
            eventRepository.updateEvent(currentEvent.id, updatedEvent)

            localList = localList.toMutableList().also { it[index] = updatedEvent }
            _eventsState.value = _eventsState.value.toMutableList().also {
                it[index] = it[index].copy(
                    joined = !isJoined,
                    participants = updatedParticipants.size
                )
            }
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
