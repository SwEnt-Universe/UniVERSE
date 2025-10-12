package com.android.universe.ui.profileCreation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.universe.model.CountryData.countryToIsoCode
import com.android.universe.model.user.UserProfile
import com.android.universe.model.user.UserRepository
import com.android.universe.model.user.UserRepositoryProvider
import java.time.DateTimeException
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
}

/**
 * ViewModel responsible for managing the Add Profile screen Logic.
 *
 * It validates user input, handles error messages, and communicates with the [UserRepository] to
 * persist the new [UserProfile].
 *
 * UI should collet [uiState] to observe changes in real time.
 *
 * @param repository The data source handling user-related operations.
 * @param dispatcher The [CoroutineDispatcher] used for launching coroutines in this ViewModel.
 *   Defaults to [Dispatchers.Default].
 * @param repositoryDispatcher The [CoroutineDispatcher] used for executing repository operations.
 *   Defaults to [Dispatchers.IO].
 * @constructor Creates a new instance with an injected [UserRepository].
 */
class AddProfileViewModel(
    private val repository: UserRepository = UserRepositoryProvider.repository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val repositoryDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

  /** Backing field for [uiState]. Mutable within the ViewModel only. */
  private val _uiState = MutableStateFlow(AddProfileUIState())

  /** Publicly exposed state of the Add Profile UI. */
  val uiState: StateFlow<AddProfileUIState> = _uiState.asStateFlow()

  private val nameRegex = "^[\\p{L}\\p{M}' -]*$".toRegex()

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
   * If any check fails, [errorMsg] is updated with a user-friendly message. Otherwise, a
   * [UserProfile] is constructed and persisted via [repository]
   */
  fun addProfile() {
    viewModelScope.launch(dispatcher) {
      val state = _uiState.value

      val firstName = state.firstName
      if (firstName.isBlank()) {
        setErrorMsg("First name cannot be empty")
        return@launch
      }

      if (!nameRegex.matches(firstName)) {
        setErrorMsg("Invalid first name format")
        return@launch
      }

      if (firstName.length > InputLimits.FIRST_NAME) {
        setErrorMsg("First name is too long")
        return@launch
      }

      val cleanedFirstName = sanitize(firstName)

      val lastName = state.lastName
      if (state.lastName.isBlank()) {
        setErrorMsg("Last name cannot be empty")
        return@launch
      }

      if (!nameRegex.matches(lastName)) {
        setErrorMsg("Invalid last name format")
        return@launch
      }

      if (lastName.length > InputLimits.LAST_NAME) {
        setErrorMsg("Last name is too long")
        return@launch
      }

      val cleanedLastName = sanitize(lastName)

      val description = state.description
      if (description != null && description.length > InputLimits.DESCRIPTION) {
        setErrorMsg("Description is too long")
        return@launch
      }

      val cleanedDescription = description?.let { sanitize(it) }
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

      if (!usernameRegex.matches(username)) {
        setErrorMsg("Invalid username format")
        return@launch
      }

      if (username.length > InputLimits.USERNAME) {
        setErrorMsg("Username is too long")
        return@launch
      }

      if (!repository.isUsernameUnique(username)) {
        setErrorMsg("Username already exists")
        return@launch
      }

      val userProfile =
          UserProfile(
              username = username,
              firstName = cleanedFirstName,
              lastName = cleanedLastName,
              description = cleanedDescription?.takeIf { it.isNotBlank() },
              country = isoCode,
              dateOfBirth = LocalDate.of(year.toInt(), month.toInt(), day.toInt()),
              tags = emptyList())

      withContext(repositoryDispatcher) { repository.addUser(userProfile) }
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

  /**
   * Updates the username field. Also truncates the input to more than the specified limit to allow
   * an error popup to be displayed
   */
  fun setUsername(username: String) {
    val trimmed = username.take(InputLimits.USERNAME + 1)
    validUsername(trimmed)
    _uiState.value = _uiState.value.copy(username = trimmed)
  }

  /**
   * Updates the first name field. Also truncates the input to more than the specified limit to
   * allow an error popup to be displayed and removes double spaces
   */
  fun setFirstName(firstName: String) {
    val cleaned = trimAndCleanInput(firstName, InputLimits.FIRST_NAME + 1)
    validateName(
        cleaned, "First name", InputLimits.FIRST_NAME, setError = ::setOrClearFirstNameError)
    _uiState.value = _uiState.value.copy(firstName = cleaned)
  }

  /**
   * Updates the last name field. Also truncates the input to more than the specified limit to allow
   * an error popup to be displayed and removes double spaces
   */
  fun setLastName(lastName: String) {
    val cleaned = trimAndCleanInput(lastName, InputLimits.LAST_NAME + 1)
    validateName(cleaned, "Last name", InputLimits.LAST_NAME, setError = ::setOrClearLastNameError)
    _uiState.value = _uiState.value.copy(lastName = cleaned)
  }

  /**
   * Updates the description field. Also truncates the input to more than the specified limit to
   * allow an error popup to be displayed and removes double spaces
   */
  fun setDescription(description: String) {
    val cleaned = trimAndCleanInput(description, InputLimits.DESCRIPTION + 1)
    validDescription(cleaned)
    _uiState.value = _uiState.value.copy(description = cleaned)
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

  /**
   * Sanitizes a name string by:
   * - Replacing multiple spaces with a single one
   * - Trimming leading and trailing spaces Note this function should not be used in live user input
   *   handling as trimming user input prevents users from entering spaces.
   *
   * @param input The string to sanitize.
   * @return The sanitized string.
   */
  private fun sanitize(input: String): String {
    val singleSpaced = input.replace(Regex("\\s+"), " ")
    val trimmed = singleSpaced.trim()
    return trimmed
  }

  /**
   * Sets the error message for a username for the UI.
   *
   * @param username The username to set a possible error message for.
   */
  private fun validUsername(username: String) {
    if (username.isBlank()) {
      setOrClearUsernameError("Username cannot be empty")
    } else if (username.length > InputLimits.USERNAME) {
      setOrClearUsernameError("Username is too long")
    } else if (!usernameRegex.matches(username)) {
      setOrClearUsernameError(
          "Invalid username format, allowed characters are letters, numbers, dots, underscores, or dashes")
    } else {
      setOrClearUsernameError(null)
    }
  }

  /**
   * Sets the error message for a username for the UI.
   *
   * @param usernameError The username to set a possible error message for.
   */
  private fun setOrClearUsernameError(usernameError: String?) {
    _uiState.value = _uiState.value.copy(usernameError = usernameError)
  }

  /**
   * Validates a name for the UI by setting the possible error message.
   *
   * @param name The name to validate.
   * @param label The label for the name.
   * @param limit The maximum length of the name.
   * @param setError A function to set the error message to the appropriate name field.
   */
  private fun validateName(name: String, label: String, limit: Int, setError: (String?) -> Unit) {
    when {
      name.isBlank() -> setError("$label cannot be empty")
      name.length > limit -> setError("$label is too long")
      !nameRegex.matches(name) ->
          setError(
              "Invalid name format, allowed characters are letters, apostrophes, hyphens, and spaces")
      else -> setError(null)
    }
  }

  /**
   * Sets the error message for a first name for the UI.
   *
   * @param firstNameError The first name to set a possible error message for.
   */
  private fun setOrClearFirstNameError(firstNameError: String?) {
    _uiState.value = _uiState.value.copy(firstNameError = firstNameError)
  }

  /**
   * Sets the error message for a last name for the UI.
   *
   * @param lastNameError The last name to set a possible error message for.
   */
  private fun setOrClearLastNameError(lastNameError: String?) {
    _uiState.value = _uiState.value.copy(lastNameError = lastNameError)
  }

  /**
   * Sets the error message for a description for the UI.
   *
   * @param description The description to set a possible error message for.
   */
  private fun validDescription(description: String?) {
    if (description == null) setOrClearDescriptionError(null)
    else if (description.length > InputLimits.DESCRIPTION) {
      setOrClearDescriptionError("Description is too long")
    } else {
      setOrClearDescriptionError(null)
    }
  }

  /**
   * Sets the error message for a description for the UI.
   *
   * @param descriptionError The description to set a possible error message for.
   */
  private fun setOrClearDescriptionError(descriptionError: String?) {
    _uiState.value = _uiState.value.copy(descriptionError = descriptionError)
  }
}
