package com.android.universe.ui.profileSettings

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.universe.di.DefaultDP
import com.android.universe.di.DispatcherProvider
import com.android.universe.model.CountryData.countryToIsoCode
import com.android.universe.model.authentication.AuthModel
import com.android.universe.model.authentication.AuthModelFirebase
import com.android.universe.model.image.ImageBitmapManager
import com.android.universe.model.isoToCountryName
import com.android.universe.model.tag.Tag
import com.android.universe.model.tag.TagTemporaryRepository
import com.android.universe.model.tag.TagTemporaryRepositoryProvider
import com.android.universe.model.user.UserProfile
import com.android.universe.model.user.UserRepository
import com.android.universe.model.user.UserRepositoryProvider
import com.android.universe.ui.common.ErrorMessages
import com.android.universe.ui.common.InputLimits
import com.android.universe.ui.common.ValidationState
import com.android.universe.ui.common.sanitize
import com.android.universe.ui.common.toTitleCase
import com.android.universe.ui.common.validateDescription
import com.android.universe.ui.common.validateEmail
import com.android.universe.ui.common.validateFirstName
import com.android.universe.ui.common.validateLastName
import com.android.universe.ui.common.validatePassword
import com.android.universe.ui.common.validateUsername
import com.android.universe.ui.signIn.SignInMethod
import com.android.universe.ui.utils.viewModelFactory
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Represents all UI-related state for the user settings screen.
 *
 * It includes both persistent data (e.g., `email`, `firstName`, `tags`) and temporary modal values,
 * as well as modal validation errors and modal visibility
 */
data class SettingsUiState(
    val username: String = "",
    val email: String = "preview@epfl.ch",
    val password: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val country: String = "",
    val description: String = "",
    val selectedTags: List<Tag> = emptyList(),
    val profilePicture: ByteArray? = null,
    val showModal: Boolean = false,
    val errorMsg: String? = null,
    val isLoading: Boolean = false,
    val modalType: ModalType? = null,
    val modalText: String? = null,
    val modalValState: ValidationState = ValidationState.Valid,
    val passwordEnabled: Boolean? = null,
    val date: LocalDate? = null,
    val formattedDate: String? = null,
    val dateValidation: ValidationState = ValidationState.Neutral
)

/**
 * Represents the type of modal used for the text fields
 *
 * @param fieldName the name of the field to be shown to the user
 */
enum class ModalType(val fieldName: String) {
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
 * - Loading user data from [UserRepository].
 * - Managing UI state through [SettingsUiState].
 * - Validating and sanitizing user input.
 * - Persisting updates to Firebase Authentication and the local repository.
 * - Logging out the user through [signOut] from [AuthModelFirebase].
 *
 * @param uid the user's unique identifier.
 * @param userRepository the repository to fetch user data from.
 * @param authModel the authentication model to sign out the user.
 * @param tagRepository the repository to change the tags
 */
class SettingsViewModel(
    private val uid: String,
    private val userRepository: UserRepository = UserRepositoryProvider.repository,
    private val authModel: AuthModel = AuthModelFirebase(),
    private val tagRepository: TagTemporaryRepository = TagTemporaryRepositoryProvider.repository,
    private val imageManager: ImageBitmapManager,
    private val dispatcherProvider: DispatcherProvider = DefaultDP
) : ViewModel() {

  companion object {
    /**
     * Factory to create an instance of [SettingsViewModel].
     *
     * This factory is required to inject the [Context] needed for [ImageBitmapManager] and the
     * repositories into the ViewModel.
     *
     * @param context The context used to initialize the image manager.
     * @param uid The user's unique identifier.
     */
    fun provideFactory(context: Context, uid: String): ViewModelProvider.Factory =
        viewModelFactory {
          SettingsViewModel(
              uid = uid,
              userRepository = UserRepositoryProvider.repository,
              authModel = AuthModelFirebase(),
              tagRepository = TagTemporaryRepositoryProvider.repository,
              imageManager = ImageBitmapManager(context.applicationContext),
              dispatcherProvider = DefaultDP)
        }
  }

  private val _uiState = MutableStateFlow(SettingsUiState())
  val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

  private val _userTags = MutableStateFlow(emptySet<Tag>())
  val userTags = _userTags.asStateFlow()

  val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

  init {
    loadUser(uid)
    FirebaseAuth.getInstance().currentUser?.email?.let { email ->
      _uiState.value = _uiState.value.copy(email = email)
    }
    viewModelScope.launch {
      try {
        if (uiState.value.passwordEnabled == null) {
          val mail = FirebaseAuth.getInstance().currentUser?.email
          if (mail != null) {
            val methods = authModel.fetchSignInMethodsForEmail(mail).signInMethods
            val enabled = methods?.contains(SignInMethod.EMAIL) ?: false
            _uiState.value = _uiState.value.copy(passwordEnabled = enabled)
          } else _uiState.value = _uiState.value.copy(passwordEnabled = false)
        }
      } catch (_: FirebaseNetworkException) {
        _uiState.update { it.copy(errorMsg = "No network connection") }
      } catch (_: NoSuchElementException) {
        _uiState.update { it.copy(errorMsg = "No profile found") }
      }
    }
  }

  /**
   * Loads the full [UserProfile] for the given [uid] from the repository and populates the state
   * fields accordingly.
   *
   * If loading fails, [SettingsUiState.errorMsg] is updated with an error message.
   */
  fun loadUser(uid: String) {
    viewModelScope.launch {
      try {
        val userProfile = userRepository.getUser(uid)
        _uiState.value =
            _uiState.value.copy(
                username = userProfile.username,
                firstName = userProfile.firstName,
                lastName = userProfile.lastName,
                country = isoToCountryName[userProfile.country] ?: userProfile.country,
                description = userProfile.description ?: "",
                date = userProfile.dateOfBirth,
                formattedDate = formatter.format(userProfile.dateOfBirth),
                selectedTags = userProfile.tags.toList(),
                profilePicture = userProfile.profilePicture)
        _userTags.value = _userTags.value.plus(userProfile.tags)
        tagRepository.updateTags(_userTags.value)
        tagRepository.tagsFlow.collect { newTags -> _userTags.value = newTags }
      } catch (e: Exception) {
        _uiState.value = _uiState.value.copy(errorMsg = "Failed to load user: ${e.message}")
      }
    }
  }

  /** Deletes the profile picture */
  fun deleteImage() {
    _uiState.value = _uiState.value.copy(profilePicture = null)
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
          _uiState.value = uiState.value.copy(profilePicture = byteArray)
        }
      }
    }
  }

  /**
   * Sets the date of birth of the user if not null
   *
   * @param date the date of birth
   */
  fun setDate(date: LocalDate?) {
    if (date != null) {
      val age = Period.between(date, LocalDate.now()).years
      if (age < InputLimits.MIN_AGE) {
        _uiState.value =
            _uiState.value.copy(
                date = date,
                formattedDate = formatter.format(date),
                dateValidation =
                    ValidationState.Invalid(
                        ErrorMessages.DATE_TOO_YOUNG.format(InputLimits.MIN_AGE)))
      } else
          _uiState.value =
              _uiState.value.copy(
                  date = date,
                  formattedDate = formatter.format(date),
                  dateValidation = ValidationState.Valid)
    }
  }

  /** Saves the temporary modal to the main state */
  fun saveTempModal() {
    when (_uiState.value.modalType) {
      ModalType.EMAIL ->
          _uiState.value =
              _uiState.value.copy(
                  email = _uiState.value.modalText!!,
                  modalType = null,
                  showModal = false,
                  modalText = null,
                  modalValState = ValidationState.Neutral)
      ModalType.PASSWORD ->
          _uiState.value =
              _uiState.value.copy(
                  password = _uiState.value.modalText!!,
                  modalType = null,
                  showModal = false,
                  modalText = null,
                  modalValState = ValidationState.Neutral)
      ModalType.USERNAME ->
          _uiState.value =
              _uiState.value.copy(
                  username = _uiState.value.modalText!!,
                  modalType = null,
                  showModal = false,
                  modalText = null,
                  modalValState = ValidationState.Neutral)
      ModalType.FIRSTNAME ->
          _uiState.value =
              _uiState.value.copy(
                  firstName = _uiState.value.modalText!!,
                  modalType = null,
                  showModal = false,
                  modalText = null,
                  modalValState = ValidationState.Neutral)
      ModalType.LASTNAME ->
          _uiState.value =
              _uiState.value.copy(
                  lastName = _uiState.value.modalText!!,
                  modalType = null,
                  showModal = false,
                  modalText = null,
                  modalValState = ValidationState.Neutral)
      ModalType.DESCRIPTION ->
          _uiState.value =
              _uiState.value.copy(
                  description = _uiState.value.modalText!!,
                  modalType = null,
                  showModal = false,
                  modalText = null,
                  modalValState = ValidationState.Neutral)
      null -> {}
    }
  }

  /** Clears any active global error message shown via toast or snackbar. */
  fun clearErrorMsg() {
    _uiState.value = _uiState.value.copy(errorMsg = null)
  }

  /**
   * Sets the modal type and shows its associated text
   *
   * @param type the type of modal i.e a basic string field
   */
  fun setModalType(type: ModalType) {
    val text =
        when (type) {
          ModalType.EMAIL -> _uiState.value.email
          ModalType.PASSWORD -> _uiState.value.password
          ModalType.USERNAME -> _uiState.value.username
          ModalType.FIRSTNAME -> _uiState.value.firstName
          ModalType.LASTNAME -> _uiState.value.lastName
          ModalType.DESCRIPTION -> _uiState.value.description
        }
    _uiState.value = _uiState.value.copy(modalType = type, showModal = true, modalText = text)
  }

  /** Stops the modal and thus informs the UI */
  fun stopModal() {
    _uiState.value =
        _uiState.value.copy(
            modalType = null,
            showModal = false,
            modalText = null,
            modalValState = ValidationState.Valid)
  }

  /**
   * A helper to simplify similar logic in the modal
   *
   * @param limit the possible input limit
   * @param string the current input
   * @param validate the validation function
   */
  fun setterHelper(limit: Int?, string: String, validate: (String) -> ValidationState) {
    if (limit != null && string.length <= limit) {
      _uiState.value = _uiState.value.copy(modalText = string, modalValState = validate(string))
    } else
        _uiState.value = _uiState.value.copy(modalText = string, modalValState = validate(string))
  }

  /**
   * Sets the text of the modal to handle the temporary changes and validates them to tell if there
   * are errors
   *
   * @param string the current input
   */
  fun setModalText(string: String) {
    when (_uiState.value.modalType) {
      ModalType.EMAIL -> setterHelper(InputLimits.EMAIL_MAX_LENGTH, string) { validateEmail(it) }
      ModalType.PASSWORD -> setterHelper(null, string) { validatePassword(it) }
      ModalType.USERNAME -> setterHelper(InputLimits.USERNAME, string) { validateUsername(it) }
      ModalType.FIRSTNAME -> setterHelper(InputLimits.FIRST_NAME, string) { validateFirstName(it) }
      ModalType.LASTNAME -> setterHelper(InputLimits.LAST_NAME, string) { validateLastName(it) }
      ModalType.DESCRIPTION ->
          setterHelper(InputLimits.DESCRIPTION, string) { validateDescription(it) }
      null -> {}
    }
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
  fun saveProfile(uid: String, onConfirm: () -> Unit = {}) {
    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true) }
      val state = _uiState.value
      // ─── 1. Validation is already done! ────────────────────────────
      // We no longer need the `validateAll` check here.

      // ─── 2. Attempt to update the user profile ─────────────────────
      try {
        val latestProfile: UserProfile = userRepository.getUser(uid)
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
                tags = _userTags.value,
                profilePicture = state.profilePicture,
                followers = latestProfile.followers,
                following = latestProfile.following)

        userRepository.updateUser(uid, updatedProfile)

        // ─── 3. Firebase Email update ──────────────────────────────
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (state.email != currentUser?.email) {
          // TODO Mail update needs to be looked at
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

        _uiState.update { it.copy(isLoading = false) }
        onConfirm()
      } catch (e: Exception) {
        _uiState.update {
          it.copy(errorMsg = "Failed to save profile: ${e.message}", isLoading = false)
        }
        if (e is CancellationException) {
          ensureActive()
          throw e
        }
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
