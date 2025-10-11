package com.android.universe.ui.signIn

/**
 * Part of the code in this file is copy-pasted from the Bootcamp solution provided by the SwEnt
 * staff.
 */
import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.universe.R
import com.android.universe.model.authentication.AuthModel
import com.android.universe.model.authentication.AuthModelFirebase
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
    val signedOut: Boolean = false
)

/**
 * ViewModel for the Sign In screen, handling user authentication logic.
 *
 * @param authModel The authentication model responsible for handling authentication logic.
 */
class SignInViewModel(private val authModel: AuthModel = AuthModelFirebase()) : ViewModel() {

  companion object {
    private const val TAG = "SignInViewModel"
  }

  private val _uiState = MutableStateFlow(SignInUIState())
  /** The UI state for the Sign In screen. */
  val uiState: StateFlow<SignInUIState> = _uiState.asStateFlow()

  private fun updateUiState(
      isLoading: Boolean,
      user: FirebaseUser?,
      signedOut: Boolean,
      errorMsg: String?
  ) {
    _uiState.update {
      it.copy(isLoading = isLoading, user = user, signedOut = signedOut, errorMsg = errorMsg)
    }
  }

  /** Sets an error message in the UI state. */
  private fun setErrorMsg(errorMsg: String) {
    _uiState.value = _uiState.value.copy(errorMsg = errorMsg)
  }

  /** Clears the error message in the UI state. */
  fun clearErrorMsg() {
    _uiState.value = _uiState.value.copy(errorMsg = null)
  }

  /** Sets the loading state in the UI. */
  private fun setLoading(isLoading: Boolean) {
    _uiState.value = _uiState.value.copy(isLoading = isLoading)
  }

  /** Creates the sign-in options for Google Sign-In. */
  private fun getGoogleOptions(context: Context) =
      GetSignInWithGoogleOption.Builder(
              serverClientId = context.getString(R.string.default_web_client_id))
          .build()

  /** Creates a credential request for Google Sign-In. */
  private fun googleSignInRequest(signInOptions: GetSignInWithGoogleOption) =
      GetCredentialRequest.Builder().addCredentialOption(signInOptions).build()

  /** Asynchronously retrieves a credential using the [CredentialManager]. */
  private suspend fun getCredential(
      context: Context,
      request: GetCredentialRequest,
      credentialManager: CredentialManager
  ) = credentialManager.getCredential(context, request).credential

  /** Handles failures during the credential retrieval process. */
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
      setLoading(true)
      clearErrorMsg()

      val signInOptions = getGoogleOptions(context)
      val signInRequest = googleSignInRequest(signInOptions)

      try {
        val credential = getCredential(context, signInRequest, credentialManager)

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
}
