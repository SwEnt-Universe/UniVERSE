package com.android.universe.ui.event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.universe.model.event.Event
import com.android.universe.model.event.EventRepository
import com.android.universe.model.event.EventRepositoryProvider
import com.android.universe.model.user.UserProfile
import com.android.universe.model.user.UserReactiveRepository
import com.android.universe.model.user.UserReactiveRepositoryProvider
import com.android.universe.model.user.UserRepository
import com.android.universe.model.user.UserRepositoryProvider
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/** UI state for an event item. */
data class EventUIState(
    val title: String = "",
    val description: String = "",
    val date: String = "",
    val tags: List<String> = emptyList(),
    val creator: String = "",
    val participants: Int = 0
)

/** ViewModel for managing event data and reactive creator info. */
class EventViewModel(
    private val eventRepository: EventRepository = EventRepositoryProvider.repository,
    private val userReactiveRepository: UserReactiveRepository? =
        UserReactiveRepositoryProvider.repository,
    private val userRepository: UserRepository = UserRepositoryProvider.repository
) : ViewModel() {

  private val _eventsState = MutableStateFlow<List<EventUIState>>(emptyList())
  val eventsState: StateFlow<List<EventUIState>> = _eventsState.asStateFlow()

  init {
    loadEvents()
  }

  /** Load events and set up reactive creator updates */
  fun loadEvents() {
    viewModelScope.launch {
      val events = eventRepository.getAllEvents()

      if (userReactiveRepository != null) {
        // Convert list of creators to distinct set
        val distinctCreators = events.map { it.creator }.distinct()

        // Combine all event flows
        combine(
                distinctCreators.map { uid ->
                  userReactiveRepository!!.getUserFlow(uid).map { uid to it }
                }) { userPairs ->
                  val usersMap = userPairs.toMap()
                  events.map { event ->
                    val user = usersMap[event.creator]
                    event.toUIState(user)
                  }
                }
            .collect { uiStates -> _eventsState.value = uiStates }
      } else {
        val uiStates =
            events.map { event ->
              EventUIState(
                  title = event.title,
                  description = event.description ?: "",
                  date = formatEventDate(event.date),
                  tags = event.tags.map { it.displayName }.take(3),
                  creator = formatCreator(userRepository.getUser(event.creator)),
                  participants = event.participants.count())
            }
        _eventsState.value = uiStates
      }
    }
  }

  /** Convert event and optional creator to UI model */
  private fun Event.toUIState(user: UserProfile?): EventUIState {
    return EventUIState(
        title = title,
        description = description ?: "",
        date = formatEventDate(date),
        tags = tags.map { it.displayName }.take(3),
        creator = user?.let { "${it.firstName} ${it.lastName}" } ?: "Unknown",
        participants = participants.size)
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
