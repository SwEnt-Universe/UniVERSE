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

data class EventUIState(
    val title: String = "",
    val description: String = "",
    val date: String = "",
    val tags: List<String> = emptyList(),
    val creator: String = "",
    val participants: Int = 0
)

class EventViewModel(
    private val eventRepository: EventRepository = EventRepositoryProvider.repository
) : ViewModel() {
  private val _eventsState = MutableStateFlow<List<EventUIState>>(emptyList())
  val eventsState: StateFlow<List<EventUIState>> = _eventsState.asStateFlow()

  init {
    loadEvents()
  }

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

  private fun formatEventDate(date: LocalDateTime): String {
    val formatter = DateTimeFormatter.ofPattern("d MMM hh:mm a", Locale.ENGLISH)
    return date.format(formatter)
  }

  private fun formatCreator(user: UserProfile): String {
    return "${user.firstName} ${user.lastName}"
  }
}
