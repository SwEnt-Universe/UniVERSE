package com.android.universe.ui.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.universe.model.CountryData.countryToIsoCode
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
import kotlinx.coroutines.withContext

/**
 * Represents the UI state for the Add Profile screen.
 *
 * This data class centralizes all user-input fields and error states. It is observed by the UI via
 * a [StateFlow] in [AddProfileViewModel].
 *
 * @property username The username entered by the user.
 * @property firstName The user's first name.
 * @property lastName The user's last name.
 * @property description Optional description or bio text.
 * @property country The user's country.
 * @property day The day of birth as a string.
 * @property month The month of a birth as a string.
 * @property year The year of birth as a string.
 * @property errorMsg Optional error message displayed on validation failure.
 */
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

/**
 * ViewModel responsible for managing the Add Profile screen Logic.
 *
 * It validates user input, handles error messages, and communicates with the [UserRepository] to
 * persist the new [UserProfile].
 *
 * UI should collet [uiState] to observe changes in real time.
 *
 * @param repository The data source handling user-related operations.
 * @constructor Creates a new instance with an injected [UserRepository].
 */
class AddProfileViewModel(
    private val repository: UserRepository = UserRepositoryProvider.repository,
) : ViewModel() {

  /** Backing field for [uiState]. Mutable within the ViewModel only. */
  private val _uiState = MutableStateFlow(AddProfileUIState())

  /** Publicly exposed state of the Add Profile UI. */
  val uiState: StateFlow<AddProfileUIState> = _uiState.asStateFlow()

  /** Clears the current error message from the UI state. */
  fun clearErrorMsg() {
    _uiState.value = _uiState.value.copy(errorMsg = null)
  }

  /**
   * Attempts to create and add a new user profile.
   *
   * This method performs a series of validations:
   * - Ensures all required fields are filled.
   * - Validates date components (day, month, year).
   * - Checks for a unique username.
   *
   * If any check fails, [errorMsg] is updated with a user-friendly message. Otherwise, a
   * [UserProfile] is constructed and persisted via [repository]
   */
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

      val country = state.country
      if (country.isBlank()) {
        setErrorMsg("Country cannot be empty")
        return@launch
      }

      val isoCode = countryToIsoCode[state.country]
      if (isoCode == null) {
        setErrorMsg("Invalid country")
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
              description = state.description?.takeIf { it.isNotBlank() },
              country = isoCode,
              dateOfBirth = LocalDate.of(year.toInt(), month.toInt(), day.toInt()))

      withContext(Dispatchers.IO) { repository.addUser(userProfile) }
    }
  }

  /**
   * Validates whether a combination of [day], [month], and [year] forms a valid calendar date.
   *
   * @return true if the date is valid, false otherwise.
   */
  private fun isValidDate(day: Int, month: Int, year: Int): Boolean {
    return try {
      LocalDate.of(year, month, day)
      true
    } catch (_: DateTimeException) {
      false
    }
  }

  /** Updates the username field. */
  fun setUsername(username: String) {
    _uiState.value = _uiState.value.copy(username = username)
  }

  /** Updates the first name field. */
  fun setFirstName(firstName: String) {
    _uiState.value = _uiState.value.copy(firstName = firstName)
  }

  /** Updates the last name field. */
  fun setLastName(lastName: String) {
    _uiState.value = _uiState.value.copy(lastName = lastName)
  }

  /** Updates the description field. */
  fun setDescription(description: String) {
    _uiState.value = _uiState.value.copy(description = description)
  }

  /** Updates the country field. */
  fun setCountry(country: String) {
    _uiState.value = _uiState.value.copy(country = country)
  }

  /** Updates the day field. */
  fun setDay(day: String) {
    _uiState.value = _uiState.value.copy(day = day)
  }

  /** Updates the month field. */
  fun setMonth(month: String) {
    _uiState.value = _uiState.value.copy(month = month)
  }

  /** Updates the year field. */
  fun setYear(year: String) {
    _uiState.value = _uiState.value.copy(year = year)
  }

  /**
   * Updates the current error message in the UI state.
   *
   * @param errorMsg The message to display to the user.
   */
  fun setErrorMsg(errorMsg: String) {
    _uiState.value = _uiState.value.copy(errorMsg = errorMsg)
  }
}
