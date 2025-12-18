package com.android.universe.ui.profileCreation

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.universe.di.DefaultDP
import com.android.universe.di.DispatcherProvider
import com.android.universe.model.image.ImageBitmapManager
import com.android.universe.model.user.UserProfile
import com.android.universe.model.user.UserRepository
import com.android.universe.model.user.UserRepositoryProvider
import com.android.universe.ui.common.InputLimits
import com.android.universe.ui.common.ValidationState
import com.android.universe.ui.common.sanitize
import com.android.universe.ui.common.sanitizeLead
import com.android.universe.ui.common.toTitleCase
import com.android.universe.ui.common.validateBirthDate
import com.android.universe.ui.common.validateDescription
import com.android.universe.ui.common.validateFirstName
import com.android.universe.ui.common.validateLastName
import com.android.universe.ui.common.validateUsername
import com.android.universe.ui.utils.viewModelFactory
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class OnboardingState {
  WELCOME,
  ENTER_USERNAME,
  ENTER_FIRSTNAME,
  ENTER_LASTNAME,
  ENTER_DATE_OF_BIRTH,
  ENTER_DESCRIPTION
}

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
 * @property day The day of birth as a string.
 * @property month The month of a birth as a string.
 * @property year The year of birth as a string.
 */
data class AddProfileUIState(
    val username: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val description: String? = null,
    val day: String = "",
    val month: String = "",
    val year: String = "",
    val profilePicture: ByteArray? = null,
    val onboardingState: MutableMap<OnboardingState, Boolean> =
        mutableMapOf(
            OnboardingState.WELCOME to true,
            OnboardingState.ENTER_USERNAME to false,
            OnboardingState.ENTER_FIRSTNAME to false,
            OnboardingState.ENTER_LASTNAME to false,
            OnboardingState.ENTER_DATE_OF_BIRTH to false,
            OnboardingState.ENTER_DESCRIPTION to false)
) {
  val canSave: Boolean
    get() =
        userNameValid is ValidationState.Valid &&
            firstNameValid is ValidationState.Valid &&
            lastNameValid is ValidationState.Valid &&
            (descriptionValid is ValidationState.Valid ||
                descriptionValid is ValidationState.Neutral) &&
            dateOfBirthValid is ValidationState.Valid

  val userNameValid: ValidationState
    get() = validateUsername(username)

  val firstNameValid: ValidationState
    get() = validateFirstName(firstName)

  val lastNameValid: ValidationState
    get() = validateLastName(lastName)

  val descriptionValid: ValidationState
    get() = validateDescription(description ?: "")

  val dateOfBirthValid: ValidationState
    get() = validateBirthDate(day, month, year)
}

/**
 * ViewModel responsible for managing the Add Profile screen Logic.
 *
 * It validates user input, handles error messages, and communicates with the [UserRepository] to
 * persist the new [UserProfile].
 *
 * @param repository The data source handling user-related operations.
 * @param imageManager The manager used to resize and compress images.
 * @param dispatcherProvider The provider for coroutine dispatchers.
 */
open class AddProfileViewModel(
    private val repository: UserRepository = UserRepositoryProvider.repository,
    private val imageManager: ImageBitmapManager,
    private val dispatcherProvider: DispatcherProvider = DefaultDP
) : ViewModel() {

  companion object {
    /**
     * Factory to create an instance of [AddProfileViewModel].
     *
     * This factory is required to inject the [Context] needed for [ImageBitmapManager] and the
     * repositories into the ViewModel.
     *
     * @param context The context used to initialize the image manager.
     * @param repository The repository for user storage.
     */
    fun provideFactory(context: Context): ViewModelProvider.Factory = viewModelFactory {
      AddProfileViewModel(
          repository = UserRepositoryProvider.repository,
          imageManager = ImageBitmapManager(context.applicationContext),
          dispatcherProvider = DefaultDP)
    }
  }

  /** Backing field for [uiState]. Mutable within the ViewModel only. */
  private val _uiState = MutableStateFlow(AddProfileUIState())

  /** Publicly exposed state of the Add Profile UI. */
  val uiState: StateFlow<AddProfileUIState> = _uiState.asStateFlow()

  fun setOnboardingState(state: OnboardingState, value: Boolean) {
    _uiState.value.onboardingState[state] = value
  }

  /**
   * Validates all inputs and attempts to create and save a new user profile.
   *
   * If validation succeeds, it constructs a [UserProfile] object from the current UI state,
   * sanitizing text fields and converting the country name to its ISO code. The new profile is then
   * saved to the repository.
   *
   * On successful creation, it invokes the [onSuccess] callback.
   *
   * @param uid The unique identifier for the user.
   * @param onSuccess A callback function to be executed on the main thread after the profile is
   *   successfully created.
   */
  fun addProfile(uid: String, onSuccess: () -> Unit = {}) {
    viewModelScope.launch {
      if (!uiState.value.canSave) {
        return@launch
      }

      val state = _uiState.value
      val dateOfBirth = LocalDate.of(state.year.toInt(), state.month.toInt(), state.day.toInt())

      val userProfile =
          UserProfile(
              uid = uid,
              username = sanitize(state.username),
              firstName = sanitize(state.firstName).toTitleCase(),
              lastName = sanitize(state.lastName).toTitleCase(),
              description = state.description?.takeIf { it.isNotBlank() },
              dateOfBirth = dateOfBirth,
              country = "",
              tags = emptySet(),
              profilePicture = state.profilePicture)

      repository.addUser(userProfile)
      onSuccess()
    }
  }

  /**
   * Updates the username in the UI state and validates it. Input is truncated to slightly above the
   * allowed limit to ensure the length error is shown.
   *
   * @param username The new username string from the UI.
   */
  fun setUsername(username: String) {
    val finalUsername = username.take(InputLimits.USERNAME + 1)
    _uiState.update { it.copy(username = finalUsername) }
  }

  /**
   * Updates the first name in the UI state and validates it. Input is sanitized, then truncated to
   * slightly above the allowed limit to ensure the length error is shown.
   *
   * @param firstName The new first name string from the UI.
   */
  fun setFirstName(firstName: String) {
    val cleaned = sanitizeLead(firstName)
    val finalName = cleaned.take(InputLimits.FIRST_NAME + 1)
    _uiState.update { it.copy(firstName = finalName) }
  }

  /**
   * Updates the last name in the UI state and validates it. Input is sanitized, then truncated to
   * slightly above the allowed limit to ensure the length error is shown.
   *
   * @param lastName The new last name string from the UI.
   */
  fun setLastName(lastName: String) {
    val cleaned = sanitizeLead(lastName)
    val finalName = cleaned.take(InputLimits.LAST_NAME + 1)
    _uiState.update { it.copy(lastName = finalName) }
  }

  /**
   * Updates the description in the UI state and validates its length.
   *
   * @param description The new description string from the UI.
   */
  fun setDescription(description: String) {
    _uiState.update { it.copy(description = description) }
  }

  /**
   * Updates the day of birth in the UI state and validates it. Input is filtered to only allow
   * digits and truncated to the maximum character length.
   *
   * @param day The day of birth as a string from the UI.
   */
  fun setDay(day: String) {
    val finalDay = day.filter { it.isDigit() }.take(InputLimits.DAY)
    _uiState.update { it.copy(day = finalDay) }
  }

  /**
   * Updates the month of birth in the UI state and validates it. Input is filtered to only allow
   * digits and truncated to the maximum character length.
   *
   * @param month The month of birth as a string from the UI.
   */
  fun setMonth(month: String) {
    val finalMonth = month.filter { it.isDigit() }.take(InputLimits.MONTH)
    _uiState.update { it.copy(month = finalMonth) }
  }

  /**
   * Updates the year of birth in the UI state and validates it. Input is filtered to only allow
   * digits and truncated to the maximum character length.
   *
   * @param year The year of birth as a string from the UI.
   */
  fun setYear(year: String) {
    val finalYear = year.filter { it.isDigit() }.take(InputLimits.YEAR)
    _uiState.update { it.copy(year = finalYear) }
  }

  /**
   * Updates the user's profile picture in the UI state.
   *
   * If a non-null [uri] is provided, this method launches a coroutine on the IO dispatcher to
   * resize and compress the image via [ImageBitmapManager]. The resulting [ByteArray] is then
   * updated in the state on the main thread.
   *
   * If [uri] is `null`, the current profile picture is removed from the state immediately.
   *
   * @param uri The [Uri] of the selected image, or `null` to clear the current picture.
   */
  fun setProfilePicture(uri: Uri?) {
    if (uri == null) {
      _uiState.update { it.copy(profilePicture = null) }
    } else {
      viewModelScope.launch(dispatcherProvider.io) {
        val byteArray = imageManager.resizeAndCompressImage(uri)
        withContext(dispatcherProvider.main) {
          _uiState.update { it.copy(profilePicture = byteArray) }
        }
      }
    }
  }

  /** Removes the current profile picture. */
  fun deleteProfilePicture() {
    _uiState.update { it.copy(profilePicture = null) }
  }
}
