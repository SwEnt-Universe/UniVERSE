package com.android.universe.ui.profileCreation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.universe.model.CountryData.countryToIsoCode
import com.android.universe.model.user.UserProfile
import com.android.universe.model.user.UserRepository
import com.android.universe.model.user.UserRepositoryProvider
import com.android.universe.ui.common.ErrorMessages
import com.android.universe.ui.common.InputLimits
import com.android.universe.ui.common.ValidationResult
import com.android.universe.ui.common.sanitize
import com.android.universe.ui.common.toTitleCase
import com.android.universe.ui.common.validateBirthDate
import com.android.universe.ui.common.validateCountry
import com.android.universe.ui.common.validateDay
import com.android.universe.ui.common.validateDescription
import com.android.universe.ui.common.validateFirstName
import com.android.universe.ui.common.validateLastName
import com.android.universe.ui.common.validateMonth
import com.android.universe.ui.common.validateUsername
import com.android.universe.ui.common.validateYear
import java.time.LocalDate
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Represents the UI state for the Add Profile screen.
 *
 * This data class centralizes all user-input fields and their corresponding validation error
 * states. It is exposed to the UI via a [StateFlow] in [AddProfileViewModel].
 *
 * @property username The username entered by the user.
 * @property firstName The user's first name.
 * @property lastName The user's last name.
 * @property description Optional description or bio text.
 * @property country The user's selected country.
 * @property day The day of birth as a string.
 * @property month The month of a birth as a string.
 * @property year The year of birth as a string.
 * @property usernameError Optional error message for the username field.
 * @property firstNameError Optional error message for the first name field.
 * @property lastNameError Optional error message for the last name field.
 * @property descriptionError Optional error message for the description field.
 * @property countryError Optional error message for the country field.
 * @property yearError Optional error message for the year field.
 * @property monthError Optional error message for the month field.
 * @property dayError Optional error message for the day field.
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
    val usernameError: String? = null,
    val firstNameError: String? = null,
    val lastNameError: String? = null,
    val descriptionError: String? = null,
    val countryError: String? = null,
    val yearError: String? = null,
    val monthError: String? = null,
    val dayError: String? = null
)

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

  /**
   * Converts a [ValidationResult] to a nullable String.
   *
   * @return The error message if the result is [ValidationResult.Invalid], otherwise null.
   */
  private fun ValidationResult.toStringOrNull(): String? {
    return when (this) {
      is ValidationResult.Valid -> null
      is ValidationResult.Invalid -> this.errorMessage
    }
  }

  /**
   * Validates all inputs and attempts to create and save a new user profile.
   *
   * This method first triggers a comprehensive validation of all fields by calling
   * [validateAllInputs]. If validation succeeds, it constructs a [UserProfile] object from the
   * current UI state, sanitizing text fields and converting the country name to its ISO code. The
   * new profile is then saved to the repository.
   *
   * On successful creation, it invokes the [onSuccess] callback.
   *
   * @param uid The unique identifier for the user.
   * @param onSuccess A callback function to be executed on the main thread after the profile is
   *   successfully created.
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
        _uiState.update { it.copy(countryError = ErrorMessages.COUNTRY_INVALID) }
        return@launch
      }

      val userProfile =
          UserProfile(
              uid = uid,
              username = sanitize(state.username),
              firstName = sanitize(state.firstName).toTitleCase(),
              lastName = sanitize(state.lastName).toTitleCase(),
              description = state.description?.takeIf { it.isNotBlank() },
              country = isoCode,
              dateOfBirth = dateOfBirth,
              tags = emptySet())

      withContext(repositoryDispatcher) { repository.addUser(userProfile) }
      withContext(mainDispatcher) { onSuccess() }
    }
  }

  /**
   * Validates all user inputs from the UI state and updates the [uiState] with any error messages.
   * This includes checking for unique username, correct formats, and valid dates.
   *
   * @return `true` if all inputs are valid, `false` otherwise.
   */
  private suspend fun validateAllInputs(): Boolean {
    val state = _uiState.value

    var usernameResult = validateUsername(state.username)
    val firstNameResult = validateFirstName(state.firstName)
    val lastNameResult = validateLastName(state.lastName)
    val descriptionResult = validateDescription(state.description ?: "")
    val countryResult = validateCountry(state.country, countryToIsoCode)
    val dayResult = validateDay(state.day)
    val monthResult = validateMonth(state.month)
    val yearResult = validateYear(state.year)

    if (usernameResult is ValidationResult.Valid) {
      val isUnique =
          withContext(repositoryDispatcher) { repository.isUsernameUnique(state.username) }
      if (!isUnique) {
        usernameResult = ValidationResult.Invalid(ErrorMessages.USERNAME_TAKEN)
      }
    }

    val allDateFieldsValid =
        dayResult is ValidationResult.Valid &&
            monthResult is ValidationResult.Valid &&
            yearResult is ValidationResult.Valid

    val logicalDateResult =
        if (allDateFieldsValid) {
          validateBirthDate(state.day.toInt(), state.month.toInt(), state.year.toInt())
        } else {
          ValidationResult.Valid
        }

    val finalDayError =
        if (logicalDateResult is ValidationResult.Invalid &&
            logicalDateResult.errorMessage == ErrorMessages.DATE_INVALID_LOGICAL) {
          logicalDateResult
        } else {
          dayResult
        }

    val finalMonthError = monthResult

    val finalYearError =
        if (logicalDateResult is ValidationResult.Invalid &&
            logicalDateResult.errorMessage != ErrorMessages.DATE_INVALID_LOGICAL) {
          logicalDateResult
        } else {
          yearResult
        }

    _uiState.update {
      it.copy(
          usernameError = usernameResult.toStringOrNull(),
          firstNameError = firstNameResult.toStringOrNull(),
          lastNameError = lastNameResult.toStringOrNull(),
          descriptionError = descriptionResult.toStringOrNull(),
          countryError = countryResult.toStringOrNull(),
          dayError = finalDayError.toStringOrNull(),
          monthError = finalMonthError.toStringOrNull(),
          yearError = finalYearError.toStringOrNull())
    }

    // Return true if all results are valid
    return usernameResult is ValidationResult.Valid &&
        firstNameResult is ValidationResult.Valid &&
        lastNameResult is ValidationResult.Valid &&
        descriptionResult is ValidationResult.Valid &&
        countryResult is ValidationResult.Valid &&
        finalDayError is ValidationResult.Valid &&
        finalMonthError is ValidationResult.Valid &&
        finalYearError is ValidationResult.Valid
  }

  /**
   * Updates the username in the UI state and validates it. Input is truncated to slightly above the
   * allowed limit to ensure the length error is shown.
   *
   * @param username The new username string from the UI.
   */
  fun setUsername(username: String) {
    val finalUsername = username.take(InputLimits.USERNAME + 1)
    val validationResult = validateUsername(finalUsername)
    _uiState.update {
      it.copy(username = finalUsername, usernameError = validationResult.toStringOrNull())
    }
  }

  /**
   * Updates the first name in the UI state and validates it. Input is sanitized, then truncated to
   * slightly above the allowed limit to ensure the length error is shown.
   *
   * @param firstName The new first name string from the UI.
   */
  fun setFirstName(firstName: String) {
    val finalName = firstName.take(InputLimits.FIRST_NAME + 1)
    val validationResult = validateFirstName(finalName)
    _uiState.update {
      it.copy(firstName = finalName, firstNameError = validationResult.toStringOrNull())
    }
  }

  /**
   * Updates the last name in the UI state and validates it. Input is sanitized, then truncated to
   * slightly above the allowed limit to ensure the length error is shown.
   *
   * @param lastName The new last name string from the UI.
   */
  fun setLastName(lastName: String) {
    val finalName = lastName.take(InputLimits.LAST_NAME + 1)
    val validationResult = validateLastName(finalName)
    _uiState.update {
      it.copy(lastName = finalName, lastNameError = validationResult.toStringOrNull())
    }
  }

  /**
   * Updates the description in the UI state and validates its length.
   *
   * @param description The new description string from the UI.
   */
  fun setDescription(description: String) {
    val validationResult = validateDescription(description)
    _uiState.update {
      it.copy(description = description, descriptionError = validationResult.toStringOrNull())
    }
  }

  /**
   * Updates the selected country in the UI state and validates it.
   *
   * @param country The new country string from the UI.
   */
  fun setCountry(country: String) {
    val validationResult = validateCountry(country, countryToIsoCode)
    _uiState.update { it.copy(country = country, countryError = validationResult.toStringOrNull()) }
  }

  /**
   * Updates the day of birth in the UI state and validates it. Input is filtered to only allow
   * digits and truncated to the maximum character length.
   *
   * @param day The day of birth as a string from the UI.
   */
  fun setDay(day: String) {
    val finalDay = day.filter { it.isDigit() }.take(InputLimits.DAY)
    val dayResult = validateDay(finalDay)
    _uiState.update { it.copy(day = finalDay, dayError = dayResult.toStringOrNull()) }
  }

  /**
   * Updates the month of birth in the UI state and validates it. Input is filtered to only allow
   * digits and truncated to the maximum character length.
   *
   * @param month The month of birth as a string from the UI.
   */
  fun setMonth(month: String) {
    val finalMonth = month.filter { it.isDigit() }.take(InputLimits.MONTH)
    val monthResult = validateMonth(finalMonth)

    val currentDayError = _uiState.value.dayError
    val newDayError =
        if (currentDayError == ErrorMessages.DATE_INVALID_LOGICAL) {
          null
        } else {
          currentDayError
        }

    _uiState.update {
      it.copy(month = finalMonth, monthError = monthResult.toStringOrNull(), dayError = newDayError)
    }
  }

  /**
   * Updates the year of birth in the UI state and validates it. Input is filtered to only allow
   * digits and truncated to the maximum character length.
   *
   * @param year The year of birth as a string from the UI.
   */
  fun setYear(year: String) {
    val finalYear = year.filter { it.isDigit() }.take(InputLimits.YEAR)
    val yearResult = validateYear(finalYear)

    val currentDayError = _uiState.value.dayError
    val newDayError =
        if (currentDayError == ErrorMessages.DATE_INVALID_LOGICAL) {
          null
        } else {
          currentDayError
        }

    _uiState.update {
      it.copy(year = finalYear, yearError = yearResult.toStringOrNull(), dayError = newDayError)
    }
  }
}
