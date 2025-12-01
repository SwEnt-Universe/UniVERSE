package com.android.universe.ui.profileSettings

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.universe.di.DefaultDP
import com.android.universe.model.CountryData.countryToIsoCode
import com.android.universe.model.authentication.AuthModel
import com.android.universe.model.authentication.AuthModelFirebase
import com.android.universe.model.isoToCountryName
import com.android.universe.model.tag.Tag
import com.android.universe.model.user.UserProfile
import com.android.universe.model.user.UserRepositoryProvider
import com.android.universe.ui.common.ErrorMessages
import com.android.universe.ui.common.InputLimits
import com.android.universe.ui.common.ValidationState
import com.android.universe.ui.common.sanitize
import com.android.universe.ui.common.sanitizeLead
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
import com.android.universe.ui.common.validateUsername
import com.android.universe.ui.common.validateYear
import com.android.universe.ui.theme.Dimensions
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.time.Period
import java.time.format.DateTimeFormatter

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
    val password: String = "asdasdas",
    val firstName: String = "",
    val lastName: String = "",
    val country: String = "",
    val description: String = "",
    val day: String = "",
    val month: String = "",
    val year: String = "",
    val selectedTags: List<Tag> = emptyList(),
    val profilePicture: ByteArray? = null,
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
    val isLoading: Boolean = false,
    val modalType: ModalType? = null,
    val modalText : String? = null,
    val modalValState : ValidationState = ValidationState.Neutral,
    val passwordEnabled : Boolean = false,
    val date : LocalDate? = null,
    val formattedDate : String? = null,
    val dateValidation : ValidationState = ValidationState.Neutral
)

/**
 * For textfields TODO desc
 */
enum class ModalType (val fieldName: String){
    EMAIL(fieldName = "Email"),
    PASSWORD(fieldName = "Password"),
    USERNAME(fieldName = "Username"),
    FIRSTNAME(fieldName = "First Name"),
    LASTNAME(fieldName = "Last Name"),
    DESCRIPTION(fieldName = "Description")
}
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
/**
 * TODO uid
 */
class SettingsViewModel(
    private val uid: String,
    private val userRepository: UserRepositoryProvider = UserRepositoryProvider,
    private val authModel: AuthModel = AuthModelFirebase()
) : ViewModel() {
  private val _uiState = MutableStateFlow(SettingsUiState())
  val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

  private fun ValidationState.toStringOrNull(): String? {
    return when (this) {
      is ValidationState.Valid -> null
      is ValidationState.Neutral -> null
      is ValidationState.Invalid -> this.errorMessage
    }
  }

    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
    init {
      loadUser(uid) //TODO NEW
    FirebaseAuth.getInstance().currentUser?.email?.let { email ->
      _uiState.value = _uiState.value.copy(email = email)
    }
      viewModelScope.launch {
          val methods = authModel.fetchSignInMethodsForEmail(FirebaseAuth.getInstance().currentUser?.email!!).signInMethods
          val enabled = methods?.contains("password") ?: false
          _uiState.value = _uiState.value.copy(passwordEnabled = enabled)
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
                date = userProfile.dateOfBirth,
                formattedDate = formatter.format(userProfile.dateOfBirth),
                day = userProfile.dateOfBirth.dayOfMonth.toString(),
                month = userProfile.dateOfBirth.monthValue.toString(),
                year = userProfile.dateOfBirth.year.toString(),
                selectedTags = userProfile.tags.toList(),
                profilePicture = userProfile.profilePicture)
      } catch (e: Exception) {
        _uiState.value = _uiState.value.copy(errorMsg = "Failed to load user: ${e.message}")
      }
    }
  }

    /**
     * Taken as is from AddProfileViewmodel
     */
    fun setProfilePicture(context: Context, uri: Uri?) {
        if (uri == null) {
            //_uiState.value = uiState.value.copy(profilePicture = null)
        } else {
            viewModelScope.launch(DefaultDP.io) {
                // We redimension the image to have a 256256 image to reduce the space of the
                // image.
                val maxSize = Dimensions.ProfilePictureSize

                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }

                context.contentResolver.openInputStream(uri)?.use { input ->
                    BitmapFactory.decodeStream(input, null, options)
                }

                val (height: Int, width: Int) = options.run { outHeight to outWidth }
                var inSampleSize = 1
                if (height > maxSize || width > maxSize) {
                    val halfHeight = height / 2
                    val halfWidth = width / 2
                    while ((halfHeight / inSampleSize) >= maxSize && (halfWidth / inSampleSize) >= maxSize) {
                        inSampleSize = 2
                    }
                }

                options.inSampleSize = inSampleSize
                options.inJustDecodeBounds = false

                val bitmap =
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        BitmapFactory.decodeStream(input, null, options)
                    }

                if (bitmap == null) {
                    Log.e("ImageError", "Failed to decode bitmap from URI $uri")
                } else {
                    val stream = ByteArrayOutputStream()
                    // We compress the image with a low quality to reduce the space of the image.
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 45, stream)
                    val byteArray = stream.toByteArray()
                    withContext(DefaultDP.main) {
                        _uiState.value = uiState.value.copy(profilePicture = byteArray)
                    }
                }
            }
        }
    }
    /**
     * Sets the date of birth of the user if not null
     * @param date the date of birth
     */
    fun setDate(date: LocalDate?){
        if (date != null) {
            val age = Period.between(date, LocalDate.now()).years
            if (age < InputLimits.MIN_AGE) {
                _uiState.value = _uiState.value.copy(date = date, formattedDate = formatter.format(date), dateValidation = ValidationState.Invalid(ErrorMessages.DATE_TOO_YOUNG.format(InputLimits.MIN_AGE)))
            }
            else _uiState.value = _uiState.value.copy(date = date, formattedDate = formatter.format(date), dateValidation = ValidationState.Valid)
        }
    }

    /**
     * Saves the temporary modal to the main state
     */
    fun saveTempModal(){
        when(_uiState.value.modalType){
            ModalType.EMAIL -> _uiState.value = _uiState.value.copy(email = _uiState.value.modalText!!, modalType = null, showModal = false, modalText = null, modalValState = ValidationState.Neutral)
            ModalType.PASSWORD -> _uiState.value = _uiState.value.copy(password = _uiState.value.modalText!!, modalType = null, showModal = false, modalText = null, modalValState = ValidationState.Neutral)
            ModalType.USERNAME -> _uiState.value = _uiState.value.copy(username = _uiState.value.modalText!!, modalType = null, showModal = false, modalText = null, modalValState = ValidationState.Neutral)
            ModalType.FIRSTNAME -> _uiState.value = _uiState.value.copy(firstName = _uiState.value.modalText!!, modalType = null, showModal = false, modalText = null, modalValState = ValidationState.Neutral)
            ModalType.LASTNAME -> _uiState.value = _uiState.value.copy(lastName = _uiState.value.modalText!!, modalType = null, showModal = false, modalText = null, modalValState = ValidationState.Neutral)
            ModalType.DESCRIPTION -> _uiState.value = _uiState.value.copy(description = _uiState.value.modalText!!, modalType = null, showModal = false, modalText = null, modalValState = ValidationState.Neutral)
            null -> {}
        }
    }

  /** Clears any active global error message shown via toast or snackbar. */
  fun clearErrorMsg() {
    _uiState.value = _uiState.value.copy(errorMsg = null)
  }

    /**
     * TODO
     */
    fun setModalType(type: ModalType){
        val text = when(type){
            ModalType.EMAIL -> _uiState.value.email
            ModalType.PASSWORD -> _uiState.value.password
            ModalType.USERNAME -> _uiState.value.username
            ModalType.FIRSTNAME -> _uiState.value.firstName
            ModalType.LASTNAME -> _uiState.value.lastName
            ModalType.DESCRIPTION -> _uiState.value.description
        }
        _uiState.value = _uiState.value.copy(modalType = type, showModal = true, modalText = text)
    }

    /**
     * Stops the modal and thus informs the UI
     */
    fun stopModal(){
        _uiState.value = _uiState.value.copy(modalType = null, showModal = false, modalText = null, modalValState = ValidationState.Neutral)
    }

    /**
     * A helper to simplify similar logic in the modal
     * @param limit the possible input limit
     * @param string the current input
     * @param validate the validation function
     */
    @VisibleForTesting
    private fun setterHelper(limit: Int?, string: String, validate: (String) -> ValidationState) {
        if(limit != null && string.length <= limit) {
            _uiState.value = _uiState.value.copy(modalText = string, modalValState = validate(string))
        } else _uiState.value = _uiState.value.copy(modalText = string, modalValState = validate(string))
    }
    /**
     * Sets the text of the modal to handle the temporary changes
     * @param string the current input
     */
    fun setModalText(string: String){
        when(_uiState.value.modalType){
            ModalType.EMAIL -> setterHelper(InputLimits.EMAIL_MAX_LENGTH, string) { validateEmail(it) }
            ModalType.PASSWORD -> setterHelper(null, string) { validatePassword(it)}
            ModalType.USERNAME -> setterHelper(InputLimits.USERNAME, string) { validateUsername(it) }
            ModalType.FIRSTNAME -> setterHelper(InputLimits.FIRST_NAME, string) { validateFirstName(it) }
            ModalType.LASTNAME -> setterHelper(InputLimits.LAST_NAME, string) { validateLastName(it)}
            ModalType.DESCRIPTION -> setterHelper(InputLimits.DESCRIPTION, string) { validateDescription(it) }
            null -> {}
        }
    }
  /**
   * Updates a temporary modal field (e.g., `tempValue`, `tempDay`, etc.) based on the given [key].
   *
   * Also clears any existing validation errors for that temporary field.
   */
  fun updateTemp(key: String, value: String) {
    val state = _uiState.value
    var ValidationState: ValidationState = ValidationState.Valid

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
          ValidationState = validateEmail(finalValue)
        }
        "password" -> {
          // No hard limit for password, just validation (soft limit)
          finalValue = value
          ValidationState = validatePassword(finalValue)
        }
        "firstName" -> {
          // Sanitize *then* truncate at LIMIT + 1
          val cleaned = sanitizeLead(value)
          finalValue = cleaned.take(InputLimits.FIRST_NAME + 1)
          ValidationState = validateFirstName(finalValue)
        }
        "lastName" -> {
          // Sanitize *then* truncate at LIMIT + 1
          val cleaned = sanitizeLead(value)
          finalValue = cleaned.take(InputLimits.LAST_NAME + 1)
          ValidationState = validateLastName(finalValue)
        }
        "description" -> {
          // No truncation, (soft limit), matches AddProfileViewModel
          finalValue = value
          ValidationState = validateDescription(finalValue)
        }
        "country" -> {
          // No truncation
          finalValue = value
          ValidationState = validateCountry(finalValue, countryToIsoCode)
        }
      }
      _uiState.update {
        it.copy(tempValue = finalValue, modalError = ValidationState.toStringOrNull())
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

    var logicalDateResult: ValidationState = ValidationState.Valid
    if (dayResult is ValidationState.Valid &&
        monthResult is ValidationState.Valid &&
        yearResult is ValidationState.Valid) {
      logicalDateResult = validateBirthDate(day, month, year)
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
      dayResult: ValidationState,
      monthResult: ValidationState,
      yearResult: ValidationState,
      logicalDateResult: ValidationState
  ): Triple<ValidationState, ValidationState, ValidationState> {
    val finalDayError =
        if (logicalDateResult is ValidationState.Invalid &&
            logicalDateResult.errorMessage == ErrorMessages.DATE_INVALID_LOGICAL) {
          logicalDateResult
        } else {
          dayResult
        }

    val finalMonthError = monthResult

    val finalYearError =
        if (logicalDateResult is ValidationState.Invalid &&
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
   * update the profile picture of the user and automatically save it in his user profile.
   *
   * @param imageId the string that characterise the image.
   */
  fun updateProfilePicture(imageId: ByteArray?, uid: String) {
    _uiState.value = _uiState.value.copy(profilePicture = imageId)
    saveProfile(uid)
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

    // Handle DATE modal explicitly
    if (state.currentField == "date") {
      val d = state.tempDay
      val m = state.tempMonth
      val y = state.tempYear

      val dayRes = validateDay(d)
      val monthRes = validateMonth(m)
      val yearRes = validateYear(y)

      val allValid =
          dayRes is ValidationState.Valid &&
              monthRes is ValidationState.Valid &&
              yearRes is ValidationState.Valid

      val logicalRes =
          if (allValid) {
            validateBirthDate(d, m, y)
          } else ValidationState.Valid

      val (finalDayError, finalMonthError, finalYearError) =
          deriveDateErrors(dayRes, monthRes, yearRes, logicalRes)

      // If any invalid → keep modal open
      if (finalDayError is ValidationState.Invalid ||
          finalMonthError is ValidationState.Invalid ||
          finalYearError is ValidationState.Invalid) {
        _uiState.update {
          it.copy(
              tempDayError = finalDayError.toStringOrNull(),
              tempMonthError = finalMonthError.toStringOrNull(),
              tempYearError = finalYearError.toStringOrNull(),
              modalError = logicalRes.toStringOrNull(),
              showModal = true)
        }
        return
      }

      // All valid → commit values and close modal
      newState =
          newState.copy(
              day = d,
              month = m,
              year = y,
              tempDayError = null,
              tempMonthError = null,
              tempYearError = null,
              modalError = null,
              showModal = false,
              currentField = "")

      _uiState.value = newState
      saveProfile(uid)
      return
    }

    // ─── Regular text/country/tag logic ──────────────────────────────
    val cleanedValue = sanitize(state.tempValue)
    val result =
        when (state.currentField) {
          "firstName" -> validateFirstName(cleanedValue)
          "lastName" -> validateLastName(cleanedValue)
          "email" -> validateEmail(cleanedValue)
          "password" -> validatePassword(cleanedValue)
          "description" -> validateDescription(cleanedValue)
          "country" -> validateCountry(cleanedValue, countryToIsoCode)
          else -> ValidationState.Valid
        }

    if (result is ValidationState.Invalid) {
      _uiState.update { it.copy(modalError = result.errorMessage) }
      return
    }

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
        Tag.Category.entries
            .find { it.fieldName == state.currentField }
            ?.let { category ->
              val tagList = Tag.getTagsForCategory(category)
              newState =
                  newState.copy(
                      selectedTags =
                          state.selectedTags.filter { it !in tagList } + state.tempSelectedTags)
            }
      }
    }

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
                dateOfBirth = state.date!!,
                tags = state.selectedTags.toSet(),
                profilePicture = state.profilePicture)

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
