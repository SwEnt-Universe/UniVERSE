package com.android.universe.ui.profileCreation

import android.app.Application
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.android.universe.R
import com.android.universe.model.CountryData.countryToIsoCode
import com.android.universe.model.user.UserProfile
import com.android.universe.model.user.UserRepository
import com.android.universe.model.user.UserRepositoryProvider
import com.android.universe.ui.common.InputLimits
import com.android.universe.ui.common.ValidationResult
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
    application: Application,
    private val repository: UserRepository = UserRepositoryProvider.repository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val repositoryDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main
) : AndroidViewModel(application) {

  /** Backing field for [uiState]. Mutable within the ViewModel only. */
  private val _uiState = MutableStateFlow(AddProfileUIState())

  /** Publicly exposed state of the Add Profile UI. */
  val uiState: StateFlow<AddProfileUIState> = _uiState.asStateFlow()

    private fun ValidationResult.toStringOrNull(): String? {
        return when (this) {
            is ValidationResult.Valid -> null
            is ValidationResult.Invalid -> {
                getApplication<Application>().getString(this.errorResId, *this.formatArgs.toTypedArray())
            }
        }
    }

    private fun getString(@StringRes resId: Int): String {
        return getApplication<Application>().getString(resId)
    }

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

      // Re-validate all fields on submit, in case any were empty/pristine
      val usernameResult = validateUsername(state.username)
      val firstNameResult = validateFirstName(state.firstName)
      val lastNameResult = validateLastName(state.lastName)
      val descriptionResult = validateDescription(state.description ?: "")
      val countryResult = validateCountry(state.country, countryToIsoCode)
      val dayResult = validateDay(state.day)
      val monthResult = validateMonth(state.month)
      val yearResult = validateYear(state.year)

      var logicalDateResult: ValidationResult = ValidationResult.Valid
      if (dayResult is ValidationResult.Valid &&
          monthResult is ValidationResult.Valid &&
          yearResult is ValidationResult.Valid) {
          logicalDateResult =
              validateBirthDate(state.day.toInt(), state.month.toInt(), state.year.toInt())
      }

      // Assign logical date error based on logic from _validateAndSetDate
      val (finalDayError, finalMonthError, finalYearError) =
          deriveDateErrors(dayResult, monthResult, yearResult, logicalDateResult)

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

      val isSyncValid =
          usernameResult is ValidationResult.Valid &&
                  firstNameResult is ValidationResult.Valid &&
                  lastNameResult is ValidationResult.Valid &&
                  descriptionResult is ValidationResult.Valid &&
                  countryResult is ValidationResult.Valid &&
                  finalDayError is ValidationResult.Valid &&
                  finalMonthError is ValidationResult.Valid &&
                  finalYearError is ValidationResult.Valid

      if (!isSyncValid) {
          return false
      }

      // All sync checks passed, now run the async username check
      val isUnique =
          withContext(repositoryDispatcher) { repository.isUsernameUnique(state.username) }
      if (!isUnique) {
          // Note: You'll need to add error_username_taken to your strings.xml
          val takenError = ValidationResult.Invalid(R.string.error_username_taken)
          _uiState.update { it.copy(usernameError = takenError.toStringOrNull()) }
          setErrorMsg(getString(R.string.error_username_taken)) // Example
          return false
      }

      return true
  }

  /**
   * Updates the username field and its associated error state if any. Also truncates the input to
   * more than the specified limit to allow an error popup to be displayed
   */
  fun setUsername(username: String) {
      val finalUsername = username.take(InputLimits.USERNAME)
      val validationResult = validateUsername(finalUsername)
      _uiState.update {
          it.copy(username = finalUsername, usernameError = validationResult.toStringOrNull())
      }
  }

  /**
   * Updates the first name field and its associated error state if any. Also truncates the input to
   * more than the specified limit to allow an error popup to be displayed and removes double spaces
   */
  fun setFirstName(firstName: String) {
      val cleaned = firstName.replace(Regex("\\s+"), " ")
      val finalName = cleaned.take(InputLimits.FIRST_NAME)
      val validationResult = validateFirstName(finalName)
      _uiState.update {
          it.copy(firstName = finalName, firstNameError = validationResult.toStringOrNull())
      }
  }

  /**
   * Updates the last name field and its associated error state if any. Also truncates the input to
   * more than the specified limit to allow an error popup to be displayed and removes double spaces
   */
  fun setLastName(lastName: String) {
      val cleaned = lastName.replace(Regex("\\s+"), " ")
      val finalName = cleaned.take(InputLimits.LAST_NAME)
      val validationResult = validateLastName(finalName)
      _uiState.update {
          it.copy(lastName = finalName, lastNameError = validationResult.toStringOrNull())
      }
  }

  /**
   * Updates the description field and its associated error state if any. Also truncates the input
   * to more than the specified limit to allow an error popup to be displayed and removes double
   * spaces
   */
  fun setDescription(description: String) {
      val cleaned = description.replace(Regex("\\s+"), " ")
      val validationResult = validateDescription(cleaned)
      _uiState.update {
          it.copy(description = cleaned, descriptionError = validationResult.toStringOrNull())
      }
  }

  /** Updates the country field. */
  fun setCountry(country: String) {
      val validationResult = validateCountry(country, countryToIsoCode)
      _uiState.update { it.copy(country = country, countryError = validationResult.toStringOrNull()) }
  }

  /**
   * Updates the day field and re-validates the entire date (day, month, year) including logical
   * checks (e.g. Feb 30) and age constraints.
   */
  fun setDay(day: String) {
      val finalDay = day.filter { it.isDigit() }.take(InputLimits.DAY)
      val dayResult = validateDay(finalDay)
      _validateAndSetDate(
          day = finalDay,
          dayResult = dayResult,
          month = _uiState.value.month,
          monthResult = validateMonth(_uiState.value.month), // Re-validate
          year = _uiState.value.year,
          yearResult = validateYear(_uiState.value.year) // Re-validate
      )
  }

  /**
   * Updates the month field and re-validates the entire date (day, month, year) including logical
   * checks (e.g. Feb 30) and age constraints.
   */
  fun setMonth(month: String) {
      val finalMonth = month.filter { it.isDigit() }.take(InputLimits.MONTH)
      val monthResult = validateMonth(finalMonth)
      _validateAndSetDate(
          day = _uiState.value.day,
          dayResult = validateDay(_uiState.value.day), // Re-validate
          month = finalMonth,
          monthResult = monthResult,
          year = _uiState.value.year,
          yearResult = validateYear(_uiState.value.year) // Re-validate
      )
  }

  /**
   * Updates the year field and re-validates the entire date (day, month, year) including logical
   * checks (e.g. Feb 30) and age constraints.
   */
  fun setYear(year: String) {
      val finalYear = year.filter { it.isDigit() }.take(InputLimits.YEAR)
      val yearResult = validateYear(finalYear)
      _validateAndSetDate(
          day = _uiState.value.day,
          dayResult = validateDay(_uiState.value.day), // Re-validate
          month = _uiState.value.month,
          monthResult = validateMonth(_uiState.value.month), // Re-validate
          year = finalYear,
          yearResult = yearResult
      )
  }

    private fun _validateAndSetDate(
        day: String,
        dayResult: ValidationResult,
        month: String,
        monthResult: ValidationResult,
        year: String,
        yearResult: ValidationResult
    ) {
        var logicalDateResult: ValidationResult = ValidationResult.Valid
        // Only check logical date if all fields are individually valid
        if (dayResult is ValidationResult.Valid &&
            monthResult is ValidationResult.Valid &&
            yearResult is ValidationResult.Valid) {
            logicalDateResult =
                validateBirthDate(day.toInt(), month.toInt(), year.toInt())
        }

        val (finalDayError, finalMonthError, finalYearError) =
            deriveDateErrors(dayResult, monthResult, yearResult, logicalDateResult)

        _uiState.update {
            it.copy(
                day = day,
                dayError = finalDayError.toStringOrNull(),
                month = month,
                monthError = finalMonthError.toStringOrNull(),
                year = year,
                yearError = finalYearError.toStringOrNull()
            )
        }
    }

    private fun deriveDateErrors(
        dayResult: ValidationResult,
        monthResult: ValidationResult,
        yearResult: ValidationResult,
        logicalDateResult: ValidationResult
    ): Triple<ValidationResult, ValidationResult, ValidationResult> {
        var finalDayError = dayResult
        var finalMonthError = monthResult
        var finalYearError = yearResult

        if (logicalDateResult is ValidationResult.Invalid) {
            when (logicalDateResult.errorResId) {
                R.string.error_date_invalid_logical -> {
                    finalDayError = logicalDateResult
                    finalMonthError = logicalDateResult
                    finalYearError = logicalDateResult
                }
                R.string.error_date_too_young,
                R.string.error_date_in_future -> {
                    finalYearError = logicalDateResult
                }
            }
        }
        return Triple(finalDayError, finalMonthError, finalYearError)
    }

  /**
   * Updates the current error message in the UI state.
   *
   * @param errorMsg The message to display to the user.
   */
  fun setErrorMsg(errorMsg: String) {
    _uiState.value = _uiState.value.copy(errorMsg = errorMsg)
  }

    private fun sanitize(s: String): String = s.replace(Regex("\\s+"), " ").trim()
}
