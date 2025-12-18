package com.android.universe.ui.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.universe.model.event.Event
import com.android.universe.model.event.EventRepository
import com.android.universe.model.event.EventRepositoryProvider
import com.android.universe.model.user.UserProfile
import com.android.universe.model.user.UserRepository
import com.android.universe.model.user.UserRepositoryProvider
import com.android.universe.ui.event.EventUIState
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI state for user profiles.
 *
 * @param userProfile The user's profile to hold the state of.
 * @param age The calculated age of the user.
 * @property incomingEvents List of events the user has joined that are in the future.
 * @property historyEvents List of events the user has joined that are in the past.
 * @property errorMsg An error message to display, if any.
 * @property follower if there is an observer of this profile and the observer follows the observed
 *   user
 */
data class UserProfileUIState(
    val userProfile: UserProfile =
        UserProfile(
            uid = "",
            username = "",
            firstName = "",
            lastName = "",
            country = "",
            dateOfBirth = LocalDate.now(),
            tags = emptySet(),
            profilePicture = null),
    val age: Int = 0,
    val incomingEvents: List<EventUIState> = emptyList(),
    val historyEvents: List<EventUIState> = emptyList(),
    val errorMsg: String? = null,
    val follower: Boolean? = null
)

/**
 * ViewModel for managing user profiles. Notably loading a user's profile from the repository.
 *
 * @param uid The unique identifier of the user to load.
 * @param observerUid The unique identifier of the user observing another's profile, empty if no
 *   observer (default state).
 * @param userRepository The repository to fetch user profiles from.
 * @param eventRepository The repository to fetch user events from.
 */
class UserProfileViewModel(
    private val uid: String,
    private val observerUid: String = "",
    private val userRepository: UserRepository = UserRepositoryProvider.repository,
    private val eventRepository: EventRepository = EventRepositoryProvider.repository
) : ViewModel() {
  private val _userState = MutableStateFlow(UserProfileUIState())
  val userState: StateFlow<UserProfileUIState> = _userState.asStateFlow()

  init {
    loadUser()
  }

  /** Loads a user's profile from the repository. */
  fun loadUser() {
    viewModelScope.launch {
      try {
        val userProfile = userRepository.getUser(uid)

        _userState.value =
            _userState.value.copy(
                userProfile = userProfile, age = calculateAge(userProfile.dateOfBirth))

        loadUserEvents()
      } catch (e: Exception) {
        if (e is CancellationException) {
          this.ensureActive()
          throw e
        }
        Log.e("UserProfileViewModel", "User $uid not found", e)
        setErrorMsg("Username not Found")
      }
    }
  }

  /**
   * Fetches events, splits them into History/Incoming based on current time, and sorts them.
   *
   * @param uid The unique identifier of the user.
   */
  private suspend fun loadUserEvents() {
    try {
      val isFollower =
          if (observerUid.isNotEmpty()) _userState.value.userProfile.followers.contains(observerUid)
          else null
      val rawEvents =
          if (isFollower == null) eventRepository.getUserInvolvedEvents(uid)
          else
              eventRepository.getUserInvolvedEvents(uid).filter { e ->
                e.isPrivate.not() || (e.isPrivate && isFollower)
              }
      val now = LocalDateTime.now()

      val (incoming, history) = rawEvents.partition { event -> event.date.isAfter(now) }

      suspend fun mapToUIState(event: Event): EventUIState {
        val creatorName =
            try {
              userRepository.getUser(event.creator).username
            } catch (e: Exception) {
              if (e is NoSuchElementException) "Deleted" else throw e
            }

        return EventUIState(
            id = event.id,
            title = event.title,
            description = event.description ?: "",
            date = event.date,
            tags = event.tags.toList(),
            creator = creatorName,
            creatorId = event.creator,
            participants = event.participants.size,
            location = event.location,
            locationAsText = event.locationAsText,
            isPrivate = event.isPrivate,
            index = event.id.hashCode(),
            joined = true,
            eventPicture = event.eventPicture)
      }

      _userState.value =
          _userState.value.copy(
              incomingEvents = incoming.sortedBy { it.date }.map { mapToUIState(it) },
              historyEvents = history.sortedByDescending { it.date }.map { mapToUIState(it) },
              follower = isFollower)
    } catch (e: Exception) {
      Log.e("UserProfileViewModel", "Error loading events", e)
    }
  }

  /**
   * Calculates the age of a user based on their date of birth.
   *
   * @param dateOfBirth The user's date of birth.
   * @param today The current date to calculate the age.
   */
  fun calculateAge(dateOfBirth: LocalDate, today: LocalDate = LocalDate.now()): Int {
    // Number of whole years between dob and today
    return Period.between(dateOfBirth, today).years.coerceAtLeast(0)
  }

  /**
   * Joins or leaves an event based on current participation status.
   *
   * @param eventId The unique identifier of the event to join or leave.
   */
  fun joinOrLeaveEvent(eventId: String) {
    viewModelScope.launch {
      try {
        eventRepository.toggleEventParticipation(eventId, uid)
        loadUserEvents()
      } catch (_: Exception) {
        setErrorMsg("Error updating event participation")
      }
    }
  }

  /** Clears the current error message from the UI state. */
  fun clearErrorMsg() {
    _userState.value = _userState.value.copy(errorMsg = null)
  }

  /**
   * Updates the current error message in the UI state.
   *
   * @param errorMsg The message to display to the user.
   */
  fun setErrorMsg(errorMsg: String) {
    _userState.value = _userState.value.copy(errorMsg = errorMsg)
  }
}
