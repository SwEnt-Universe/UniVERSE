package com.android.universe.ui.profileCreation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.universe.model.CountryData.countryToIsoCode
import com.android.universe.model.user.UserProfile
import com.android.universe.model.user.UserRepository
import com.android.universe.model.user.UserRepositoryProvider
import java.time.DateTimeException
import java.time.LocalDate
import java.time.Period
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

  private val oldestYear = 1900

  private val currentDate = LocalDate.now()

  private val minimumUserAge = 13

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
   *
   * @param uid The user's unique identifier.
   */
  fun addProfile(uid: String, onSuccess: () -> Unit = {}) {
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

      if (!userOldEnough(LocalDate.of(year.toInt(), month.toInt(), day.toInt()))) {
        setErrorMsg("At least 13 years old required")
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
              uid = uid,
              username = username,
              firstName = cleanedFirstName,
              lastName = cleanedLastName,
              description = cleanedDescription?.takeIf { it.isNotBlank() },
              country = isoCode,
              dateOfBirth = LocalDate.of(year.toInt(), month.toInt(), day.toInt()),
              tags = emptySet())

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
   * Tests if a user is old enough to use the app.
   *
   * @param dateOfBirth The user's date of birth.
   */
  private fun userOldEnough(dateOfBirth: LocalDate): Boolean {
    // Number of whole years between dob and today
    return Period.between(dateOfBirth, currentDate).years.coerceAtLeast(0) >= minimumUserAge
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
    val error = validateName(cleaned, "First name", InputLimits.FIRST_NAME)
    _uiState.value = _uiState.value.copy(firstName = cleaned, firstNameError = error)
  }

  /**
   * Updates the last name field and its associated error state if any. Also truncates the input to
   * more than the specified limit to allow an error popup to be displayed and removes double spaces
   */
  fun setLastName(lastName: String) {
    val cleaned = trimAndCleanInput(lastName, InputLimits.LAST_NAME + 1)
    val error = validateName(cleaned, "Last name", InputLimits.LAST_NAME)
    _uiState.value = _uiState.value.copy(lastName = cleaned, lastNameError = error)
  }

  /**
   * Updates the description field and its associated error state if any. Also truncates the input
   * to more than the specified limit to allow an error popup to be displayed and removes double
   * spaces
   */
  fun setDescription(description: String) {
    val cleaned = trimAndCleanInput(description, InputLimits.DESCRIPTION + 1)
    val error =
        when {
          cleaned.length > InputLimits.DESCRIPTION -> "Description is too long"
          else -> null
        }
    _uiState.value = _uiState.value.copy(description = cleaned, descriptionError = error)
  }

  /** Updates the country field. */
  fun setCountry(country: String) {
    _uiState.value = _uiState.value.copy(country = country)
  }

  /** Updates the day field and sets the error if it is invalid */
  fun setDay(day: String) {
    val number = day.filter { it.isDigit() }
    val trimmed = number.take(InputLimits.DAY)
    val error =
        when {
          trimmed.isBlank() -> "Day cannot be empty"
          trimmed.toInt() < 1 -> "Day needs to be after 1"
          trimmed.toInt() > 31 -> "Day needs to be before 31"
          else -> null
        }
    _uiState.value = _uiState.value.copy(day = trimmed, dayError = error)
  }

  /** Updates the month field and sets the error if it is invalid */
  fun setMonth(month: String) {
    val number = month.filter { it.isDigit() }
    val trimmed = number.take(InputLimits.MONTH)
    val error =
        when {
          trimmed.isBlank() -> "Month cannot be empty"
          trimmed.toInt() < 1 -> "Month needs to be after 1"
          trimmed.toInt() > 12 -> "Month needs to be before 12"
          else -> null
        }
    _uiState.value = _uiState.value.copy(month = trimmed, monthError = error)
  }

  /** Updates the year field and sets the error if it is invalid */
  fun setYear(year: String) {
    val number = year.filter { it.isDigit() }
    val trimmed = number.take(InputLimits.YEAR)
    val error =
        when {
          trimmed.isBlank() -> "Year cannot be empty"
          trimmed.toInt() < oldestYear -> "Year needs to be after $oldestYear"
          trimmed.toInt() > currentDate.year -> "Year needs to be before ${currentDate.year}"
          else -> null
        }
    _uiState.value = _uiState.value.copy(year = trimmed, yearError = error)
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
   * Validates a name for the UI by returning the possible error message.
   *
   * @param name The name to validate.
   * @param label The label for the name.
   * @param limit The maximum length of the name.
   * @return The error message if the name is invalid, null otherwise.
   */
  private fun validateName(name: String, label: String, limit: Int): String? {
    return when {
      name.isBlank() -> "$label cannot be empty"
      name.length > limit -> "$label is too long"
      !nameRegex.matches(name) ->
          "Invalid name format, allowed characters are letters, apostrophes, hyphens, and spaces"
      else -> null
    }
  }
}
