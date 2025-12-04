package com.android.universe.ui.event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.universe.model.event.Event
import com.android.universe.model.event.EventRepository
import com.android.universe.model.event.EventRepositoryProvider
import com.android.universe.model.location.Location
import com.android.universe.model.tag.Tag
import com.android.universe.model.user.UserProfile
import com.android.universe.model.user.UserReactiveRepository
import com.android.universe.model.user.UserReactiveRepositoryProvider
import com.android.universe.model.user.UserRepository
import com.android.universe.model.user.UserRepositoryProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime

/**
 * UI state for an event item.
 *
 * @property id The unique identifier of the event.
 * @property title The title of the event.
 * @property description A brief description of the event.
 * @property date The formatted date of the event.
 * @property tags A list of tags associated with the event.
 * @property creator The name of the event creator.
 * @property participants The number of participants in the event.
 * @property location The location of the event.
 * @property index The index of the event in the list.
 * @property joined Whether the current user has joined the event.
 */
data class EventUIState(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val date: LocalDateTime = LocalDateTime.now(),
    val tags: List<Tag> = emptyList(),
    val creator: String = "",
    val participants: Int = 0,
    val location: Location = Location(0.0, 0.0),
    val index: Int = 0,
    val joined: Boolean = false,
    val eventPicture: ByteArray? = null
)

data class UiState(val errormsg: String? = null)

/**
 * [EventViewModel] orchestrates the retrieval and transformation of event and user data into
 * reactive, UI-friendly state for Compose or other reactive consumers.
 *
 * ## Overview
 * This ViewModel bridges the Firestore data layer (via [EventRepository] and
 * [UserReactiveRepository]) and the UI layer by exposing a [StateFlow] of [EventUIState] items.
 * Each [EventUIState] contains event details plus the up-to-date name of its creator.
 *
 * The ViewModel is designed to:
 * - Fetch all events from Firestore once (via [EventRepository]).
 * - Subscribe to **real-time updates** of each event creator's user profile (via
 *   [UserReactiveRepository]).
 * - Merge both streams to expose a single, continuously updated state for the UI.
 *
 * ## Reactive pipeline
 * 1. Events are loaded from [EventRepository].
 * 2. The distinct set of creator UIDs is extracted from those events.
 * 3. For each UID, [UserReactiveRepository.getUserFlow] is called to get a *shared* [Flow] that
 *    emits a [UserProfile] whenever that user's Firestore document changes.
 * 4. All creator flows are combined via [combine], producing a map of `uid → UserProfile`.
 * 5. For each event, the matching [UserProfile] is used to create an [EventUIState].
 * 6. The final list of [EventUIState] is exposed as a [StateFlow] through [eventsState].
 *
 * ### Result
 * - **Live updates**: If a user changes their first or last name, all events created by them
 *   automatically recompose in the UI — no manual refresh needed.
 * - **Minimal Firestore reads**: Each distinct user has **one shared snapshot listener** regardless
 *   of how many events or UI components depend on it.
 * - **Offline resilience**: Firestore's local cache ensures that users’ data is available even
 *   without network connectivity.
 *
 * ## Design rationale
 * - Uses Kotlin [Flow] and [combine] for reactive data merging.
 * - Uses [StateFlow] to provide replayable, observable state to Jetpack Compose.
 * - Separates concerns cleanly:
 *     - [EventRepository] handles event documents.
 *     - [UserReactiveRepository] handles live user documents.
 *     - [EventViewModel] merges and formats them for UI.
 *
 * ## Threading & lifecycle
 * - All Firestore listeners and Flow operations run in [viewModelScope], which is tied to the
 *   ViewModel lifecycle. When the ViewModel is cleared, collection stops automatically.
 * - No manual listener cleanup is needed here; it is handled by [UserReactiveRepository].
 *
 * ## Fallback mode
 * If [UserReactiveRepository] is unavailable (e.g., dependency injection disabled or running in
 * offline test mode), the ViewModel falls back to a static fetch via [UserRepository], ensuring
 * that event display still works without real-time updates.
 *
 * ## Example usage (Compose)
 *
 * ```kotlin
 * @Composable
 * fun EventListScreen(viewModel: EventViewModel) {
 *     val events by viewModel.eventsState.collectAsState()
 *     LazyColumn {
 *         items(events) { event ->
 *             EventCard(title = event.title, creator = event.creator, date = event.date)
 *         }
 *     }
 * }
 * ```
 *
 * @property eventRepository Source of event documents (Firestore).
 * @property userReactiveRepository Reactive Firestore listener source for user profiles.
 * @property userRepository Non-reactive fallback for one-time user fetches.
 */
class EventViewModel(
    private val eventRepository: EventRepository = EventRepositoryProvider.repository,
    private val userReactiveRepository: UserReactiveRepository? =
        UserReactiveRepositoryProvider.repository,
    private val userRepository: UserRepository = UserRepositoryProvider.repository,
) : ViewModel() {

  /** Backing property for the list of event UI states. */
  private val _eventsState = MutableStateFlow<List<EventUIState>>(emptyList())
  private var localList = emptyList<Event>()

  private val _uiState = MutableStateFlow(UiState())
  val uiState: StateFlow<UiState> = _uiState.asStateFlow()

  /** Publicly exposed StateFlow of event UI states. */
  val eventsState: StateFlow<List<EventUIState>> = _eventsState.asStateFlow()

  var storedUid = ""

  /**
   * Loads all events and transforms them into [EventUIState]s.
   *
   * If a [UserReactiveRepository] is available, it will:
   * - Subscribe to a [Flow] of each creator’s user document in Firestore.
   * - Combine all user flows into one reactive stream.
   * - Automatically re-emit the event list whenever any user data changes.
   *
   * Otherwise (e.g., in offline or test environments), it will:
   * - Fetch user data once via [UserRepository.getUser].
   * - Produce static, non-reactive UI state.
   *
   * ### Firestore efficiency
   * - Only **one Firestore listener** is maintained per unique creator UID.
   * - Each listener performs **one initial read** and **one read per update**, reducing cost
   *   compared to polling or repeated `.get()` calls.
   *
   * ### Output
   * Emits a list of [EventUIState]s through [eventsState], sorted and ready for display.
   */
  fun loadEvents() {
    viewModelScope.launch {
      val events = eventRepository.getAllEvents()
      localList = events

      if (userReactiveRepository != null) {
        // Convert list of creators to distinct set
        val distinctCreators = events.map { it.creator }.distinct()

        // Combine all event flows
        combine(
                distinctCreators.map { uid ->
                  userReactiveRepository.getUserFlow(uid).map { uid to it }
                }) { userPairs ->
                  val usersMap = userPairs.toMap()
                  events.mapIndexed { index, event ->
                    val user = usersMap[event.creator]
                    event.toUIState(
                        user, index = index, joined = event.participants.contains(storedUid))
                  }
                }
            .collect { uiStates -> _eventsState.value = uiStates }
      } else {
        val uiStates =
            events.mapIndexed { index, event ->
              event.toUIState(
                  userRepository.getUser(event.creator),
                  index = index,
                  joined = event.participants.contains(storedUid))
            }
        _eventsState.value = uiStates
      }
    }
  }

  /**
   * Converts an [Event] into an [EventUIState].
   *
   * @param user The creator of the event.
   * @param index The index of the event in the list.
   * @param joined Whether the current user has joined the event.
   */
  private fun Event.toUIState(
      user: UserProfile?,
      index: Int = 0,
      joined: Boolean = false
  ): EventUIState {
    return EventUIState(
        id = id,
        title = title,
        description = description ?: "",
        date = date,
        tags = tags.toList(),
        creator = user?.let { "${it.firstName} ${it.lastName}" } ?: "Unknown",
        participants = participants.size,
        location = location,
        index = index,
        joined = joined,
        eventPicture = eventPicture)
  }

  /**
   * Makes the user join or leave an event based on the current user's join status.
   *
   * @param index The index of the event in the list for quick finding
   */
  fun joinOrLeaveEvent(index: Int) {
    viewModelScope.launch {
      val currentState = _eventsState.value.getOrNull(index) ?: return@launch
      val currentEvent = localList.getOrNull(index) ?: return@launch

      val isJoined = currentState.joined
      val updatedParticipants =
          if (isJoined) {
            currentEvent.participants.filterNot { it == storedUid }.toSet()
          } else {
            currentEvent.participants + storedUid
          }

      val updatedEvent = currentEvent.copy(participants = updatedParticipants)
      try {
        eventRepository.updateEvent(currentEvent.id, updatedEvent)
      } catch (_: NoSuchElementException) {
        setErrorMsg("No event ${currentEvent.title} found")
        return@launch
      }

      localList = localList.toMutableList().also { it[index] = updatedEvent }
      _eventsState.value =
          _eventsState.value.toMutableList().also {
            it[index] = it[index].copy(joined = !isJoined, participants = updatedParticipants.size)
          }
    }
  }

  fun setErrorMsg(err: String?) {
    _uiState.value = _uiState.value.copy(errormsg = err)
  }

  // private mutable flow
  private val _searchQuery = MutableStateFlow("")

  // public immutable flow
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  /**
   * Updates the current search query used by the event filtering algorithm.
   *
   * This function is called whenever the user types into the `SearchBar` component. It updates the
   * `searchQuery` `MutableStateFlow`, which triggers recomposition and automatically refreshes the
   * filtered event list inside the ViewModel.
   *
   * @param query The latest text input from the search bar.
   */
  fun updateSearchQuery(query: String) {
    _searchQuery.value = query
  }

  val filteredEvents: StateFlow<List<EventUIState>> =
      combine(eventsState, _searchQuery) { events, query -> filterEvents(events, query) }
          .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
