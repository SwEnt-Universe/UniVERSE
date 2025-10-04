package com.android.universe.ui.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.universe.model.user.UserProfile
import com.android.universe.model.user.UserRepository
import com.android.universe.model.user.UserRepositoryProvider
import java.time.DateTimeException
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AddProfileUIState(
    val username: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val description: String? = null,
    val country: String = "",
    val day: String = "",
    val month: String = "",
    val year: String = "",
    val errorMsg: String? = null
)

class AddProfileViewModel(
    private val repository: UserRepository = UserRepositoryProvider.repository,
) : ViewModel() {
  private val _uiState = MutableStateFlow(AddProfileUIState())
  val uiState: StateFlow<AddProfileUIState> = _uiState.asStateFlow()

  fun clearErrorMsg() {
    _uiState.value = _uiState.value.copy(errorMsg = null)
  }

  fun addProfile() {
    viewModelScope.launch(Dispatchers.Default) {
      val state = _uiState.value

      if (state.firstName.isBlank()) {
        setErrorMsg("First name cannot be empty")
        return@launch
      }

      if (state.lastName.isBlank()) {
        setErrorMsg("Last name cannot be empty")
        return@launch
      }

      val day = state.day
      if (day.toIntOrNull() == null) {
        setErrorMsg("Day is not a number")
        return@launch
      }

      val month = state.month
      if (month.toIntOrNull() == null) {
        setErrorMsg("Month is not a number")
        return@launch
      }

      val year = state.year
      if (year.toIntOrNull() == null) {
        setErrorMsg("Year is not a number")
        return@launch
      }

      if (!isValidDate(day.toInt(), month.toInt(), year.toInt())) {
        setErrorMsg("Invalid date")
        return@launch
      }

      val username = state.username
      if (username.isBlank()) {
        setErrorMsg("Username cannot be empty")
        return@launch
      }

      if (!repository.isUsernameUnique(username)) {
        setErrorMsg("Username already exists")
        return@launch
      }

      val userProfile =
          UserProfile(
              username = username,
              firstName = state.firstName,
              lastName = state.lastName,
              description = state.description,
              country = state.country,
              dateOfBirth = LocalDate.of(year.toInt(), month.toInt(), day.toInt()))

      repository.addUser(userProfile)
    }
  }

  private fun isValidDate(day: Int, month: Int, year: Int): Boolean {
    return try {
      LocalDate.of(year, month, day)
      true
    } catch (_: DateTimeException) {
      false
    }
  }

  fun setUsername(username: String) {
    _uiState.value = _uiState.value.copy(username = username)
  }

  fun setFirstName(firstName: String) {
    _uiState.value = _uiState.value.copy(firstName = firstName)
  }

  fun setLastName(lastName: String) {
    _uiState.value = _uiState.value.copy(lastName = lastName)
  }

  fun setDescription(description: String) {
    _uiState.value = _uiState.value.copy(description = description)
  }

  fun setCountry(country: String) {
    _uiState.value = _uiState.value.copy(country = country)
  }

  fun setDay(day: String) {
    _uiState.value = _uiState.value.copy(day = day)
  }

  fun setMonth(month: String) {
    _uiState.value = _uiState.value.copy(month = month)
  }

  fun setYear(year: String) {
    _uiState.value = _uiState.value.copy(year = year)
  }

  fun setErrorMsg(errorMsg: String) {
    _uiState.value = _uiState.value.copy(errorMsg = errorMsg)
  }
}
