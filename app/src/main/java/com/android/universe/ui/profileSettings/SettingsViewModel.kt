package com.android.universe.ui.profileSettings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.universe.model.CountryData.countryToIsoCode
import com.android.universe.model.authentication.AuthModel
import com.android.universe.model.authentication.AuthModelFirebase
import com.android.universe.model.isoToCountryName
import com.android.universe.model.tag.Tag
import com.android.universe.model.user.UserProfile
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
import com.android.universe.ui.common.validateEmail
import com.android.universe.ui.common.validateFirstName
import com.android.universe.ui.common.validateLastName
import com.android.universe.ui.common.validateMonth
import com.android.universe.ui.common.validatePassword
import com.android.universe.ui.common.validateYear
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Represents all UI-related state for the user settings screen.
 *
 * It includes both persistent data (e.g., `email`, `firstName`, `tags`) and temporary modal values
 * (`tempValue`, `tempDay`, etc.), as well as field-specific validation errors and modal visibility
 * flags.
 */
data class SettingsUiState(
    val username: String = "",
    val email: String = "preview@epfl.ch",
    val password: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val country: String = "",
    val description: String = "",
    val day: String = "",
    val month: String = "",
    val year: String = "",
    val selectedTags: List<Tag> = emptyList(),
    val tempSelectedTags: List<Tag> = emptyList(),
    val tempValue: String = "",
    val tempDay: String = "",
    val tempMonth: String = "",
    val tempYear: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val firstNameError: String? = null,
    val lastNameError: String? = null,
    val descriptionError: String? = null,
    val dayError: String? = null,
    val monthError: String? = null,
    val yearError: String? = null,
    val tempDayError: String? = null,
    val tempMonthError: String? = null,
    val tempYearError: String? = null,
    val modalError: String? = null,
    val showModal: Boolean = false,
    val currentField: String = "",
    val showCountryDropdown: Boolean = false,
    val errorMsg: String? = null,
    val isLoading: Boolean = false
)

/**
 * ViewModel that manages all state and business logic for the profile settings screen.
 *
 * Responsibilities:
 * - Loading user data from [UserRepositoryProvider].
 * - Managing UI state through [SettingsUiState].
 * - Validating and sanitizing user input.
 * - Persisting updates to Firebase Authentication and the local repository.
 * - Logging out the user through [signOut] from [AuthModelFirebase].
 */
class SettingsViewModel(
    private val userRepository: UserRepositoryProvider = UserRepositoryProvider,
    private val authModel: AuthModel = AuthModelFirebase()
) : ViewModel() {
  private val _uiState = MutableStateFlow(SettingsUiState())
  val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

  private fun ValidationResult.toStringOrNull(): String? {
    return when (this) {
      is ValidationResult.Valid -> null
      // Assumes ValidationResult.Invalid now has: val errorMessage: String
      is ValidationResult.Invalid -> this.errorMessage
    }
  }

  init {
    FirebaseAuth.getInstance().currentUser?.email?.let { email ->
      _uiState.value = _uiState.value.copy(email = email)
    }
  }

  /**
   * Loads the full [UserProfile] for the given [username] from the repository and populates the
   * state fields accordingly.
   *
   * If loading fails, [SettingsUiState.errorMsg] is updated with an error message.
   */
  fun loadUser(uid: String) {
    viewModelScope.launch {
      try {
        val userProfile = userRepository.repository.getUser(uid)
        _uiState.value =
            _uiState.value.copy(
                username = userProfile.username,
                firstName = userProfile.firstName,
                lastName = userProfile.lastName,
                country = isoToCountryName[userProfile.country] ?: userProfile.country,
                description = userProfile.description ?: "",
                day = userProfile.dateOfBirth.dayOfMonth.toString(),
                month = userProfile.dateOfBirth.monthValue.toString(),
                year = userProfile.dateOfBirth.year.toString(),
                selectedTags = userProfile.tags.toList())
      } catch (e: Exception) {
        _uiState.value = _uiState.value.copy(errorMsg = "Failed to load user: ${e.message}")
      }
    }
  }

  /** Clears any active global error message shown via toast or snackbar. */
  fun clearErrorMsg() {
    _uiState.value = _uiState.value.copy(errorMsg = null)
  }

  /**
   * Updates a temporary modal field (e.g., `tempValue`, `tempDay`, etc.) based on the given [key].
   *
   * Also clears any existing validation errors for that temporary field.
   */
  fun updateTemp(key: String, value: String) {
    val state = _uiState.value
    var validationResult: ValidationResult = ValidationResult.Valid

    // Handle date fields separately for complex validation
    if (key == "tempDay" || key == "tempMonth" || key == "tempYear") {
      val newDay =
          if (key == "tempDay") value.filter { it.isDigit() }.take(InputLimits.DAY)
          else state.tempDay
      val newMonth =
          if (key == "tempMonth") value.filter { it.isDigit() }.take(InputLimits.MONTH)
          else state.tempMonth
      val newYear =
          if (key == "tempYear") value.filter { it.isDigit() }.take(InputLimits.YEAR)
          else state.tempYear

      _validateAndSetDate(newDay, newMonth, newYear)
      return
    }

    // Handle single-value text fields
    if (key == "tempValue") {
      var finalValue = value
      when (state.currentField) {
        "email" -> {
          // Truncate at LIMIT + 1 to match AddProfileViewModel behavior
          finalValue = value.take(InputLimits.EMAIL_MAX_LENGTH + 1)
          validationResult = validateEmail(finalValue)
        }
        "password" -> {
          // No hard limit for password, just validation (soft limit)
          finalValue = value
          validationResult = validatePassword(finalValue)
        }
        "firstName" -> {
          finalValue = value.take(InputLimits.FIRST_NAME + 1)
          validationResult = ValidationResult.Valid // no real-time validation
        }
        "lastName" -> {
          finalValue = value.take(InputLimits.LAST_NAME + 1)
          validationResult = ValidationResult.Valid // no real-time validation
        }
        "description" -> {
          // No truncation, (soft limit), matches AddProfileViewModel
          finalValue = value
          validationResult = validateDescription(finalValue)
        }
        "country" -> {
          // No truncation
          finalValue = value
          validationResult = validateCountry(finalValue, countryToIsoCode)
        }
      }
      _uiState.update {
        it.copy(tempValue = finalValue, modalError = validationResult.toStringOrNull())
      }
    }
  }

  /**
   * Validates temporary date components and updates the UI state with new values and errors.
   *
   * This function is called when any part of the date is modified in the settings modal. It
   * performs three levels of validation:
   * 1. Individual validation of the day, month, and year fields (`validateDay`, `validateMonth`,
   *    `validateYear`).
   * 2. Logical validation of the complete date (e.g., checking for "April 31st") and age
   *    constraints via `validateBirthDate`, but only if the individual fields are valid.
   * 3. Derives the final, most relevant error message for each field using `deriveDateErrors`.
   *
   * The results are then used to update the `tempDay`, `tempMonth`, `tempYear` and their
   * corresponding error fields in the UI state.
   *
   * @param day The temporary day string to validate.
   * @param month The temporary month string to validate.
   * @param year The temporary year string to validate.
   */
  private fun _validateAndSetDate(day: String, month: String, year: String) {
    val dayResult = validateDay(day)
    val monthResult = validateMonth(month)
    val yearResult = validateYear(year)

    var logicalDateResult: ValidationResult = ValidationResult.Valid
    if (dayResult is ValidationResult.Valid &&
        monthResult is ValidationResult.Valid &&
        yearResult is ValidationResult.Valid) {
      logicalDateResult = validateBirthDate(day.toInt(), month.toInt(), year.toInt())
    }

    // Use the same error derivation logic as AddProfileViewModel
    val (finalDayError, finalMonthError, finalYearError) =
        deriveDateErrors(dayResult, monthResult, yearResult, logicalDateResult)

    _uiState.update {
      it.copy(
          tempDay = day,
          tempMonth = month,
          tempYear = year,
          tempDayError = finalDayError.toStringOrNull(),
          tempMonthError = finalMonthError.toStringOrNull(),
          tempYearError = finalYearError.toStringOrNull())
    }
  }

  private fun deriveDateErrors(
      dayResult: ValidationResult,
      monthResult: ValidationResult,
      yearResult: ValidationResult,
      logicalDateResult: ValidationResult
  ): Triple<ValidationResult, ValidationResult, ValidationResult> {
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

    return Triple(finalDayError, finalMonthError, finalYearError)
  }

  /**
   * Opens a modal bottom sheet for editing a specific field (e.g. `"email"`, `"country"`,
   * `"date"`).
   *
   * Initializes all relevant temporary values (`tempValue`, `tempDay`, etc.) and resets modal error
   * fields.
   */
  fun openModal(field: String) {
    val state = _uiState.value
    _uiState.value =
        _uiState.value.copy(
            showModal = true,
            currentField = field,
            modalError = null,
            tempDayError = null,
            tempMonthError = null,
            tempYearError = null,
            tempValue =
                when (field) {
                  "email" -> state.email
                  "password" -> ""
                  "firstName" -> state.firstName
                  "lastName" -> state.lastName
                  "description" -> state.description
                  "country" -> state.country
                  else -> ""
                },
            tempDay = if (field == "date") state.day else "",
            tempMonth = if (field == "date") state.month else "",
            tempYear = if (field == "date") state.year else "",
            tempSelectedTags =
                Tag.Category.entries
                    .find { it.fieldName == field }
                    ?.let { category -> Tag.filterByCategory(state.selectedTags, category) }
                    ?: emptyList())
  }

  /** Closes the currently open modal and resets all temporary modal-related fields and errors. */
  fun closeModal() {
    _uiState.value =
        _uiState.value.copy(
            showModal = false,
            currentField = "",
            modalError = null,
            tempDayError = null,
            tempMonthError = null,
            tempYearError = null)
  }

  /** Toggles the visibility of the country dropdown within the modal. */
  fun toggleCountryDropdown(show: Boolean) {
    _uiState.value = _uiState.value.copy(showCountryDropdown = show)
  }

  /** Adds a [Tag] to the temporary selection list if it is not already selected. */
  fun addTag(tag: Tag) {
    if (!_uiState.value.tempSelectedTags.contains(tag)) {
      _uiState.value = _uiState.value.copy(tempSelectedTags = _uiState.value.tempSelectedTags + tag)
    } else {
      Log.e("SettingsViewModel", "Tag '${tag.displayName}' is already selected")
    }
  }

  /** Removes a [Tag] from the temporary selection list if it exists. */
  fun removeTag(tag: Tag) {
    if (_uiState.value.tempSelectedTags.contains(tag)) {
      _uiState.value = _uiState.value.copy(tempSelectedTags = _uiState.value.tempSelectedTags - tag)
    } else {
      Log.e("SettingsViewModel", "Tag '${tag.displayName}' is not selected")
    }
  }

  /**
   * Validates and applies changes from the modal to the main state.
   *
   * Performs field-specific validation based on [SettingsUiState.currentField]. If validation
   * passes, updates the corresponding state field and closes the modal.
   *
   * Finally, triggers [saveProfile] to persist all updates.
   */
  fun saveModal(uid: String) {
    val state = _uiState.value
    var newState = state

    // Sanitize & validate only when saving
    val cleanedValue = sanitize(state.tempValue)
    val result =
        when (state.currentField) {
          "firstName" -> validateFirstName(cleanedValue)
          "lastName" -> validateLastName(cleanedValue)
          "email" -> validateEmail(cleanedValue)
          "password" -> validatePassword(cleanedValue)
          "description" -> validateDescription(cleanedValue)
          "country" -> validateCountry(cleanedValue, countryToIsoCode)
          else -> ValidationResult.Valid
        }

    if (result is ValidationResult.Invalid) {
      _uiState.update { it.copy(modalError = result.errorMessage) }
      return
    }

    // Apply normalization only for names
    val finalValue =
        when (state.currentField) {
          "firstName",
          "lastName" -> sanitize(cleanedValue).toTitleCase()
          else -> cleanedValue
        }

    when (state.currentField) {
      "firstName" -> newState = newState.copy(firstName = finalValue)
      "lastName" -> newState = newState.copy(lastName = finalValue)
      "email" -> newState = newState.copy(email = finalValue)
      "password" -> newState = newState.copy(password = finalValue)
      "description" -> newState = newState.copy(description = finalValue)
      "country" -> newState = newState.copy(country = finalValue)

      else -> {
        // Tag category saving logic
        Tag.Category.entries.find { it.fieldName == state.currentField }?.let { category ->
          val tagList = Tag.getTagsForCategory(category)
          newState = newState.copy(
            selectedTags = state.selectedTags.filter { it !in tagList } + state.tempSelectedTags
          )
        }
      }
    }

    // Close modal
    _uiState.value =
        newState.copy(
            showModal = false,
            currentField = "",
            modalError = null,
            tempDayError = null,
            tempMonthError = null,
            tempYearError = null)

    saveProfile(uid)

  }

  /**
   * Performs full form validation and persists the user profile.
   *
   * Steps:
   * 1. Runs all validation checks via [validateAll].
   * 2. Sanitizes text fields.
   * 3. Updates user data in the repository.
   * 4. Updates Firebase email/password if changed.
   *
   * Errors are reflected in the [SettingsUiState] fields.
   */
  fun saveProfile(uid: String) {
    viewModelScope.launch {
      val state = _uiState.value

      // ─── 1. Validation is already done! ────────────────────────────
      // We no longer need the `validateAll` check here.

      // ─── 2. Attempt to update the user profile ─────────────────────
      try {
        val cleanedFirstName = sanitize(state.firstName).toTitleCase()
        val cleanedLastName = sanitize(state.lastName).toTitleCase()
        val cleanedDescription = sanitize(state.description).takeIf { it.isNotBlank() }

        val updatedProfile =
            UserProfile(
                uid = uid,
                username = state.username, // Username is read-only here
                firstName = cleanedFirstName,
                lastName = cleanedLastName,
                country = countryToIsoCode[state.country] ?: state.country,
                description = cleanedDescription,
                dateOfBirth =
                    LocalDate.of(state.year.toInt(), state.month.toInt(), state.day.toInt()),
                tags = state.selectedTags.toSet())

        userRepository.repository.updateUser(uid, updatedProfile)

        // ─── 3. Firebase Email update ──────────────────────────────
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (state.email != currentUser?.email) {
          currentUser?.updateEmail(state.email)?.addOnFailureListener { e ->
            _uiState.update { it.copy(errorMsg = "Failed to update email: ${e.message}") }
          }
        }

        // ─── 4. Firebase Password update ───────────────────────────
        if (state.password.isNotEmpty()) {
          currentUser?.updatePassword(state.password)?.addOnFailureListener { e ->
            _uiState.update { it.copy(errorMsg = "Failed to update password: ${e.message}") }
          }
        }
      } catch (e: Exception) {
        _uiState.update { it.copy(errorMsg = "Failed to save profile: ${e.message}") }
      }
    }
  }

  /**
   * Signs out the user, clears the credential state and navigates to the login screen.
   *
   * @param clear the credential state
   * @param navigate to the login screen
   */
  fun signOut(clear: suspend () -> Unit, navigate: () -> Unit) {
    _uiState.value = _uiState.value.copy(isLoading = true)
    viewModelScope.launch {
      authModel.signOut(
          onSuccess = {},
          onFailure = { e ->
            Log.e("SettingsViewModel", "Failed to sign out: ${e.message}")
            _uiState.value = _uiState.value.copy(isLoading = false)
            return@signOut
          })
      clear()
      navigate()
    }
  }
}
