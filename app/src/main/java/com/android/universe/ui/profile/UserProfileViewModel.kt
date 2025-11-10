package com.android.universe.ui.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.universe.model.user.UserProfile
import com.android.universe.model.user.UserRepository
import com.android.universe.model.user.UserRepositoryProvider
import java.time.LocalDate
import java.time.Period
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI state for user profiles.
 *
 * @param userProfile The user's profile to hold the state of.
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
            profileImageUri = null),
    val age: Int = 0,
    val errorMsg: String? = null
)

/**
 * ViewModel for managing user profiles. Notably loading a user's profile from the repository.
 *
 * @param userRepository The repository to fetch user profiles from.
 */
class UserProfileViewModel(
    private val userRepository: UserRepository = UserRepositoryProvider.repository
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
      } catch (e: Exception) {
        Log.e("UserProfileViewModel", "User $uid not found")
        setErrorMsg("Username not Found")
      }
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
