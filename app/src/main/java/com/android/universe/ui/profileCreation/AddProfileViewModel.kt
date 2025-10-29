package com.android.universe.ui.profileCreation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.universe.model.CountryData.countryToIsoCode
import com.android.universe.model.user.UserProfile
import com.android.universe.model.user.UserRepository
import com.android.universe.model.user.UserRepositoryProvider
import com.android.universe.ui.common.sanitize
import com.android.universe.ui.common.validateCountry
import com.android.universe.ui.common.validateDateTriple
import com.android.universe.ui.common.validateDescription
import com.android.universe.ui.common.validateName
import java.time.LocalDate
import kotlinx.coroutines.CoroutineDispatcher
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
 * @property usernameError Optional error message for the username field. Note that it is
 *   initialized to "Username cannot be empty" to allow a UI recomposition as the state wouldn't
 *   change if the user didn't enter anything and clicked away from the field.
 * @property firstNameError Optional error message for the first name field. Note that it is
 *   initialized to "Username cannot be empty" to allow a UI recomposition as the state wouldn't
 *   change if the user didn't enter anything and clicked away from the field.
 * @property lastNameError Optional error message for the last name field. Note that it is
 *   initialized to "Username cannot be empty" to allow a UI recomposition as the state wouldn't
 *   change if the user didn't enter anything and clicked away from the field.
 * @property descriptionError Optional error message for the description field.
 * @property yearError Optional error message for the year field. Note that it is initialized to
 *   "Username cannot be empty" to allow a UI recomposition as the state wouldn't change if the user
 *   didn't enter anything and clicked away from the field.
 * @property monthError Optional error message for the month field. Note that it is initialized to
 *   "Username cannot be empty" to allow a UI recomposition as the state wouldn't change if the user
 *   didn't enter anything and clicked away from the field.
 * @property dayError Optional error message for the day field. Note that it is initialized to
 *   "Username cannot be empty" to allow a UI recomposition as the state wouldn't change if the user
 *   didn't enter anything and clicked away from the field.
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
    val errorMsg: String? = null,
    val usernameError: String? = "Username cannot be empty",
    val firstNameError: String? = "First name cannot be empty",
    val lastNameError: String? = "Last name cannot be empty",
    val descriptionError: String? = null,
    val yearError: String? = "Year cannot be empty",
    val monthError: String? = "Month cannot be empty",
    val dayError: String? = "Day cannot be empty"
)

/**
 * @property USERNAME The maximum length of a username.
 * @property FIRST_NAME The maximum length of a user's first name.
 * @property LAST_NAME The maximum length of a user's last name.
 * @property DESCRIPTION The maximum length of a user's description.
 */
object InputLimits {
  const val USERNAME = 25
  const val FIRST_NAME = 25
  const val LAST_NAME = 25
  const val DESCRIPTION = 100
  const val DAY = 2
  const val MONTH = 2
  const val YEAR = 4
}

/**
 * ViewModel responsible for managing the Add Profile screen Logic.
 *
 * It validates user input, handles error messages, and communicates with the [UserRepository] to
 * persist the new [UserProfile].
 *
 * UI should collect [uiState] to observe changes in real time.
 *
 * @param repository The data source handling user-related operations.
 * @param dispatcher The [CoroutineDispatcher] used for launching coroutines in this ViewModel.
 *   Defaults to [Dispatchers.Default].
 * @param repositoryDispatcher The [CoroutineDispatcher] used for executing repository operations.
 *   Defaults to [Dispatchers.IO].
 * @constructor Creates a new instance with an injected [UserRepository].
 */
open class AddProfileViewModel(
    private val repository: UserRepository = UserRepositoryProvider.repository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val repositoryDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main
) : ViewModel() {

  /** Backing field for [uiState]. Mutable within the ViewModel only. */
  private val _uiState = MutableStateFlow(AddProfileUIState())

  /** Publicly exposed state of the Add Profile UI. */
  val uiState: StateFlow<AddProfileUIState> = _uiState.asStateFlow()

  private val usernameRegex = "^[A-Za-z0-9._-]+$".toRegex()

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
   * - Checks invalid input formats in the name fields.
   * - Enforces a maximum length for each field.
   * - Removes any leading or trailing spaces from the input before adding the profile.
   *
   * If any check fails, errorMsg is updated with a user-friendly message. Otherwise, a
   * [UserProfile] is constructed and persisted via [repository]
   *
   * @param uid The user's unique identifier.
   */
  fun addProfile(uid: String, onSuccess: () -> Unit = {}) {
    viewModelScope.launch(dispatcher) {
      if (!validateAllInputs()) {
        return@launch
      }

      val state = _uiState.value
      val dateOfBirth = LocalDate.of(state.year.toInt(), state.month.toInt(), state.day.toInt())

      val isoCode = countryToIsoCode[state.country]
      if (isoCode == null) {
        setErrorMsg("Invalid country")
        return@launch
      }

      val userProfile =
          UserProfile(
              uid = uid,
              username = sanitize(state.username),
              firstName = sanitize(state.firstName),
              lastName = sanitize(state.lastName),
              description = state.description?.let { sanitize(it) }?.takeIf { it.isNotBlank() },
              country = isoCode,
              dateOfBirth = dateOfBirth,
              tags = emptySet())

      withContext(repositoryDispatcher) { repository.addUser(userProfile) }
      withContext(mainDispatcher) { onSuccess() }
    }
  }

  /**
   * Validates all user inputs from the UI state. Sets errorMsg on the first failure.
   *
   * @return `true` if all inputs are valid, `false` otherwise.
   */
  private suspend fun validateAllInputs(): Boolean {
    val state = _uiState.value

    // Validate Name
    val firstNameError = validateName("First name", state.firstName, InputLimits.FIRST_NAME)
    if (firstNameError != null) {
      setErrorMsg(firstNameError)
      return false
    }
    val lastNameError = validateName("Last name", state.lastName, InputLimits.LAST_NAME)
    if (lastNameError != null) {
      setErrorMsg(lastNameError)
      return false
    }

    // Validate Description
    val descriptionError = validateDescription(state.description ?: "", InputLimits.DESCRIPTION)
    if (descriptionError != null) {
      setErrorMsg(descriptionError)
      return false
    }

    // Validate Date
    val (dayErr, monthErr, yearErr) = validateDateTriple(state.day, state.month, state.year)
    if (dayErr != null || monthErr != null || yearErr != null) {
      setErrorMsg(dayErr ?: monthErr ?: yearErr ?: "Invalid date")
      return false
    }

    // Validate Country
    val countryError = validateCountry(state.country)
    if (countryError != null) {
      setErrorMsg(countryError)
      return false
    }

    // Validate Username (sets its own error)
    if (!validateUsername(state.username)) {
      return false
    }

    return true
  }

  /**
   * Validates the username for format, length, and uniqueness.
   *
   * @return `true` if valid, `false` otherwise, setting an error message on failure.
   */
  private suspend fun validateUsername(username: String): Boolean {
    when {
      username.isBlank() -> {
        setErrorMsg("Username cannot be empty")
        return false
      }
      username.length > InputLimits.USERNAME -> {
        setErrorMsg("Username is too long")
        return false
      }
      !usernameRegex.matches(username) -> {
        setErrorMsg("Invalid Username format")
        return false
      }
    }
    if (!withContext(repositoryDispatcher) { repository.isUsernameUnique(username) }) {
      setErrorMsg("Username already exists")
      return false
    }
    return true
  }

  /**
   * Updates the username field and its associated error state if any. Also truncates the input to
   * more than the specified limit to allow an error popup to be displayed
   */
  fun setUsername(username: String) {
    val trimmed = username.take(InputLimits.USERNAME + 1)

    val error =
        when {
          trimmed.isBlank() -> "Username cannot be empty"
          trimmed.length > InputLimits.USERNAME -> "Username is too long"
          !usernameRegex.matches(trimmed) ->
              "Invalid username format, allowed characters are letters, numbers, dots, underscores, or dashes"
          else -> null
        }
    _uiState.value = _uiState.value.copy(username = trimmed, usernameError = error)
  }

  /**
   * Updates the first name field and its associated error state if any. Also truncates the input to
   * more than the specified limit to allow an error popup to be displayed and removes double spaces
   */
  fun setFirstName(firstName: String) {
    val cleaned = trimAndCleanInput(firstName, InputLimits.FIRST_NAME + 1)
    val error = validateName(label = "First name", s = cleaned, maxLength = InputLimits.FIRST_NAME)
    _uiState.value = _uiState.value.copy(firstName = cleaned, firstNameError = error)
  }

  /**
   * Updates the last name field and its associated error state if any. Also truncates the input to
   * more than the specified limit to allow an error popup to be displayed and removes double spaces
   */
  fun setLastName(lastName: String) {
    val cleaned = trimAndCleanInput(lastName, InputLimits.LAST_NAME + 1)
    val error = validateName(label = "Last name", s = cleaned, maxLength = InputLimits.LAST_NAME)
    _uiState.value = _uiState.value.copy(lastName = cleaned, lastNameError = error)
  }

  /**
   * Updates the description field and its associated error state if any. Also truncates the input
   * to more than the specified limit to allow an error popup to be displayed and removes double
   * spaces
   */
  fun setDescription(description: String) {
    val cleaned = trimAndCleanInput(description, InputLimits.DESCRIPTION + 1)
    val error = validateDescription(s = cleaned, maxLength = InputLimits.DESCRIPTION)
    _uiState.value = _uiState.value.copy(description = cleaned, descriptionError = error)
  }

  /** Updates the country field. */
  fun setCountry(country: String) {
    _uiState.value = _uiState.value.copy(country = country)
  }

  /**
   * Updates the day field and re-validates the entire date (day, month, year) including logical
   * checks (e.g. Feb 30) and age constraints.
   */
  fun setDay(day: String) {
    val number = day.filter { it.isDigit() }
    val trimmedDay = number.take(InputLimits.DAY)
    val state = _uiState.value

    val (dayErr, monthErr, yearErr) =
        validateDateTriple(day = trimmedDay, month = state.month, year = state.year)

    _uiState.value =
        state.copy(day = trimmedDay, dayError = dayErr, monthError = monthErr, yearError = yearErr)
  }

  /**
   * Updates the month field and re-validates the entire date (day, month, year) including logical
   * checks (e.g. Feb 30) and age constraints.
   */
  fun setMonth(month: String) {
    val number = month.filter { it.isDigit() }
    val trimmedMonth = number.take(InputLimits.MONTH)
    val state = _uiState.value

    val (dayErr, monthErr, yearErr) =
        validateDateTriple(day = state.day, month = trimmedMonth, year = state.year)

    _uiState.value =
        state.copy(
            month = trimmedMonth, dayError = dayErr, monthError = monthErr, yearError = yearErr)
  }

  /**
   * Updates the year field and re-validates the entire date (day, month, year) including logical
   * checks (e.g. Feb 30) and age constraints.
   */
  fun setYear(year: String) {
    val number = year.filter { it.isDigit() }
    val trimmedYear = number.take(InputLimits.YEAR)
    val state = _uiState.value

    val (dayErr, monthErr, yearErr) =
        validateDateTriple(day = state.day, month = state.month, year = trimmedYear)

    _uiState.value =
        state.copy(
            year = trimmedYear, dayError = dayErr, monthError = monthErr, yearError = yearErr)
  }

  /**
   * Updates the current error message in the UI state.
   *
   * @param errorMsg The message to display to the user.
   */
  fun setErrorMsg(errorMsg: String) {
    _uiState.value = _uiState.value.copy(errorMsg = errorMsg)
  }

  /**
   * Replaces multiple spaces with a single one and truncates the input if it exceeds the specified
   * limit.
   *
   * @param input The input string.
   * @param limit The maximum length of the output string.
   */
  private fun trimAndCleanInput(input: String, limit: Int): String {
    val singleSpaced = input.replace(Regex("\\s+"), " ")
    val trimmed = singleSpaced.take(limit)
    return trimmed
  }
}
