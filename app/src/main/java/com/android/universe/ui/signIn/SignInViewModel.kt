package com.android.universe.ui.signIn

/**
 * Part of the code in this file is copy-pasted from the Bootcamp solution provided by the SwEnt
 * staff.
 */
import android.content.Context
import android.util.Log
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.universe.R
import com.android.universe.di.DefaultDP
import com.android.universe.model.authentication.AuthModel
import com.android.universe.model.authentication.AuthModelFirebase
import com.android.universe.model.authentication.InvalidEmailException
import com.android.universe.model.authentication.SIGN_IN_FAILED_EXCEPTION_MESSAGE
import com.android.universe.ui.common.ValidationState
import com.android.universe.ui.common.validateEmail
import com.android.universe.ui.common.validatePassword
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Represents the UI state for the Sign In screen.
 *
 * @param errorMsg An optional error message to be displayed.
 * @param isLoading A boolean indicating if a loading process is active.
 * @param user The currently signed-in [FirebaseUser], or null if not signed in.
 * @param signedOut A boolean indicating if the user has signed out.
 */
data class SignInUIState(
    val errorMsg: String? = null,
    val isLoading: Boolean = false,
    val user: FirebaseUser? = null,
    val signedOut: Boolean = false,
    val email: String = "",
    val emailErrorMsg: String? = null,
    val password: String = "",
    val passwordErrorMsg: String? = null,
) {
  val signInEnabled: Boolean
    get() =
        !isLoading &&
            validateEmail(email) is ValidationState.Valid &&
            validatePassword(password) is ValidationState.Valid
}

/**
 * ViewModel for the Sign In screen, handling user authentication logic.
 *
 * @param authModel The authentication model responsible for handling authentication logic.
 */
class SignInViewModel(
    private val authModel: AuthModel = AuthModelFirebase(),
    private val iODispatcher: CoroutineDispatcher = DefaultDP.io
) : ViewModel() {

  companion object {
    private const val TAG = "SignInViewModel"
  }

  private val _uiState = MutableStateFlow(SignInUIState())
  /** The UI state for the Sign In screen. */
  val uiState: StateFlow<SignInUIState> = _uiState.asStateFlow()

  /**
   * Helper method to set the UI state, this is more useful than setter since you update the whole
   * UI state and don't have to worry about previous UI state
   *
   * @param isLoading A boolean indicating if the signing is in progress.
   * @param user The currently signed-in [FirebaseUser], or null if not signed in.
   * @param signedOut A boolean indicating if the user has signed out.
   * @param errorMsg An optional error message to be displayed.
   */
  private fun updateUiState(
      isLoading: Boolean = false,
      user: FirebaseUser?,
      signedOut: Boolean,
      errorMsg: String?
  ) {
    _uiState.update {
      it.copy(isLoading = isLoading, user = user, signedOut = signedOut, errorMsg = errorMsg)
    }
  }

  /** Clears the error message in the UI state. */
  fun clearErrorMsg() {
    _uiState.value = _uiState.value.copy(errorMsg = null)
  }

  /** Function to call before starting the signing process. */
  fun nowLoading() {
    _uiState.value = _uiState.value.copy(isLoading = true)
  }

  /**
   * Creates the sign-in options for Google Sign-In.
   *
   * @param context The application context.
   */
  private fun getGoogleOptions(context: Context) =
      GetSignInWithGoogleOption.Builder(
              serverClientId = context.getString(R.string.default_web_client_id))
          .build()

  /**
   * Creates a credential request for Google Sign-In.
   *
   * @param signInOptions The sign-in options for Google Sign-In.
   */
  private fun googleSignInRequest(signInOptions: GetSignInWithGoogleOption) =
      GetCredentialRequest.Builder().addCredentialOption(signInOptions).build()

  /**
   * Asynchronously retrieves a credential using the [CredentialManager].
   *
   * @param context The application context.
   * @param request The credential request.
   * @param credentialManager The [CredentialManager] to use for the sign-in process.
   * @return The retrieved [Credential]
   */
  private suspend fun getCredential(
      context: Context,
      request: GetCredentialRequest,
      credentialManager: CredentialManager
  ) = credentialManager.getCredential(context, request).credential

  /**
   * Handles failures during the credential retrieval process.
   *
   * @param e The [Exception] that occurred during the credential retrieval process.
   */
  private fun handleCredentialFailure(e: Exception) {
    val errorMsg =
        when (e) {
          is GetCredentialCancellationException -> "Sign in cancelled"
          is GetCredentialException -> "Failed to get credentials: ${e.localizedMessage}"
          else -> "Unexpected error: ${e.localizedMessage}"
        }

    updateUiState(isLoading = false, user = null, signedOut = true, errorMsg = errorMsg)
  }

  /**
   * Initiates the Google Sign In flow.
   *
   * @param context The application context.
   * @param credentialManager The [CredentialManager] to use for the sign-in process.
   * @param onSuccess A callback to be invoked upon successful sign-in.
   * @param onFailure A callback to be invoked upon a failed sign-in attempt.
   */
  fun signIn(
      context: Context,
      credentialManager: CredentialManager,
      onSuccess: () -> Unit = { Log.d(TAG, "signIn success callback") },
      onFailure: (Exception) -> Unit = {
        Log.e(TAG, "signIn failure callback: ${it.localizedMessage}")
      }
  ) {
    if (_uiState.value.isLoading) return

    viewModelScope.launch {
      nowLoading()
      clearErrorMsg()

      val signInOptions = getGoogleOptions(context)
      val signInRequest = googleSignInRequest(signInOptions)

      try {
        val credential =
            withContext(iODispatcher) { getCredential(context, signInRequest, credentialManager) }

        authModel.signInWithGoogle(
            credential,
            onSuccess = {
              updateUiState(isLoading = false, user = it, signedOut = false, errorMsg = null)
              onSuccess()
            },
            onFailure = {
              handleCredentialFailure(it)
              onFailure(it)
            })

        // This handle the case where getCredential throw an error
      } catch (e: Exception) {
        handleCredentialFailure(e)
        onFailure(e)
      }
    }
  }

  /**
   * Updates the email in the UI state and validates it.
   *
   * This function updates the `email` field in the `SignInUIState`. It also validates the provided
   * email using the `validateEmail` utility function. If the email is invalid, the `emailErrorMsg`
   * in the UI state is updated with the corresponding error message; otherwise, it is cleared.
   *
   * @param email The new email string to set.
   */
  fun setEmail(email: String) {
    if (_uiState.value.isLoading) return
    val ValidationState = validateEmail(email)

    val errorMsg =
        when (ValidationState) {
          is ValidationState.Valid -> null
          is ValidationState.Neutral -> null
          is ValidationState.Invalid -> ValidationState.errorMessage
        }

    _uiState.update { it.copy(email = email, emailErrorMsg = errorMsg) }
  }

  /**
   * Updates the password in the UI state and validates it.
   *
   * @param password The new password string.
   */
  fun setPassword(password: String) {
    if (_uiState.value.isLoading) return
    val ValidationState = validatePassword(password)

    val errorMsg =
        when (ValidationState) {
          is ValidationState.Valid -> null
          is ValidationState.Neutral -> null
          is ValidationState.Invalid -> ValidationState.errorMessage
        }

    _uiState.update { it.copy(password = password, passwordErrorMsg = errorMsg) }
  }

  /**
   * Attempts to sign in the user using the email and password provided in the UI state.
   *
   * It first checks if a sign-in process is already in progress or if the sign-in button should be
   * disabled. If not, it clears any existing error messages, sets the UI state to loading, and then
   * launches a coroutine to call the authentication model.
   *
   * On successful sign-in, it calls [onSignInSuccess]. On failure, it calls [onSignInFailure] to
   * handle and display the appropriate error.
   */
  fun signInWithEmail() {
    if (_uiState.value.isLoading or !_uiState.value.signInEnabled) return
    clearErrorMsg()
    nowLoading()
    viewModelScope.launch {
      val signIn =
          authModel.signInWithEmail(email = uiState.value.email, password = uiState.value.password)
      signIn.onSuccess { user: FirebaseUser -> onSignInSuccess(user) }
      signIn.onFailure { tr: Throwable -> onSignInFailure(tr) }
    }
  }

  /**
   * Updates the UI state upon successful sign-in. It sets the user in the state and stops the
   * loading indicator.
   *
   * @param user The [FirebaseUser] object representing the successfully signed-in user.
   */
  private fun onSignInSuccess(user: FirebaseUser) {
    _uiState.update { it.copy(user = user, isLoading = false) }
  }

  /**
   * Handles failures that occur during the email/password sign-in process. It updates the UI state
   * with an appropriate error message based on the type of exception.
   *
   * @param tr The [Throwable] that occurred during the sign-in attempt.
   */
  private fun onSignInFailure(tr: Throwable) {
    when (tr) {
      is FirebaseAuthWeakPasswordException -> {
        _uiState.update { it.copy(errorMsg = tr.reason, isLoading = false) }
      }
      is FirebaseAuthInvalidCredentialsException -> {
        _uiState.update { it.copy(errorMsg = "Invalid password", isLoading = false) }
      }
      is InvalidEmailException -> {
        _uiState.update { it.copy(emailErrorMsg = tr.message, isLoading = false) }
      }
      is FirebaseNetworkException -> {
        _uiState.update { it.copy(errorMsg = "No internet connection", isLoading = false) }
      }
      else -> {
        Log.e(TAG, tr.localizedMessage, tr)
        _uiState.update { it.copy(errorMsg = SIGN_IN_FAILED_EXCEPTION_MESSAGE, isLoading = false) }
      }
    }
  }
}
