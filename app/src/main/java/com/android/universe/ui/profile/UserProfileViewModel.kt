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

data class UserProfileUIState(
    val userProfile: UserProfile =
        UserProfile(
            username = "",
            firstName = "",
            lastName = "",
            country = "",
            dateOfBirth = LocalDate.now())
)

class UserProfileViewModel(
    private val userRepository: UserRepository = UserRepositoryProvider.repository
) : ViewModel() {
  private val _userState = MutableStateFlow(UserProfileUIState())
  val userState: StateFlow<UserProfileUIState> = _userState.asStateFlow()

  fun loadUser(username: String) {
    viewModelScope.launch {
      val userProfile = userRepository.getUser(username)
      if (userProfile == null) {
        Log.e("UserProfileViewModel", "User $username not found")
        return@launch
      }
      _userState.value = UserProfileUIState(userProfile)
    }
  }

  fun calculateAge(dateOfBirth: LocalDate, today: LocalDate = LocalDate.now()): Int {
    // Number of whole years between dob and today
    return Period.between(dateOfBirth, today).years.coerceAtLeast(0)
  }
}
