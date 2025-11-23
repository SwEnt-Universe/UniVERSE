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
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
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
    val incomingEvents: List<Event> = emptyList(),
    val historyEvents: List<Event> = emptyList(),
    val errorMsg: String? = null
)

/**
 * ViewModel for managing user profiles. Notably loading a user's profile from the repository.
 *
 * @param userRepository The repository to fetch user profiles from.
 * @param eventRepository The repository to fetch user events from.
 */
class UserProfileViewModel(
    private val userRepository: UserRepository = UserRepositoryProvider.repository,
    private val eventRepository: EventRepository = EventRepositoryProvider.repository
) : ViewModel() {
  private val _userState = MutableStateFlow(UserProfileUIState())
  val userState: StateFlow<UserProfileUIState> = _userState.asStateFlow()

  /**
   * Loads a user's profile from the repository.
   *
   * @param uid The uid of the user to load. Silently fails if the user is not found which should
   *   never happen.
   */
  fun loadUser(uid: String) {
    viewModelScope.launch {
      try {
        val userProfile = userRepository.getUser(uid)

        _userState.value =
            _userState.value.copy(
                userProfile = userProfile, age = calculateAge(userProfile.dateOfBirth))

        loadUserEvents(uid)
      } catch (e: Exception) {
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
  private suspend fun loadUserEvents(uid: String) {
    try {
      val rawEvents = eventRepository.getEventsForUser(uid)
      val now = LocalDateTime.now()

      val (incoming, history) = rawEvents.partition { event -> event.date.isAfter(now) }

      _userState.value =
          _userState.value.copy(
              incomingEvents = incoming.sortedBy { it.date },
              historyEvents = history.sortedByDescending { it.date })
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
