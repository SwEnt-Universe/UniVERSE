package com.android.universe.ui.signIn

import android.content.Context
import android.credentials.Credential
import android.credentials.CredentialManager
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SignInUIState(
  val errorMsg: String? = null,
  val isLoading: Boolean = false,
  val isLoginSuccess: Boolean = false
)

class SignInViewModel : ViewModel() {

  companion object {
    private const val TAG = "SignIn"
  }

  private val _uiState = MutableStateFlow(SignInUIState())
  val uiState: StateFlow<SignInUIState> = _uiState.asStateFlow()

 val emailRegex: Regex = Regex("^[a-zA-Z0-9._%+-]+@epfl\\.ch$")


  /** Sets the loading status in the UI state. */
  private fun setLoading(isLoading: Boolean) {
    _uiState.value = _uiState.value.copy(isLoading = isLoading)
  }

  /** Sets the login success status in the UI state. */
  private fun setLoginSuccess(isLoginSuccess: Boolean) {
    _uiState.value = _uiState.value.copy(isLoginSuccess = isLoginSuccess)
  }

  /** Sets an error message in the UI state. */
  private fun setErrorMsg(errorMsg: String) {
    _uiState.value = _uiState.value.copy(errorMsg = errorMsg)
  }

  /** Clears the error message in the UI state. */
  fun clearErrorMsg() {
    _uiState.value = _uiState.value.copy(errorMsg = null)
  }

  private suspend fun fireBaseAuth(idToken: String) {
    val credential = GoogleAuthProvider.getCredential(idToken, null)
    //TODO: waiting firebase auth
  }

  private suspend fun handleSignIn(credential: Credential){
    if (credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
      val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
      val emailAddress = googleIdTokenCredential.id
      if(!emailRegex.matches(emailAddress)) {
        setErrorMsg("Email address is not from EPFL")
        Log.e(TAG, "Email address is not from EPFL")
        setLoginSuccess(false)
        return
      }
      //TODO: waiting firebase auth

    }
  }

  fun signIn(context: Context, credentialManager: CredentialManager) {
    viewModelScope.launch {
      setLoading(true)

    }
  }

}
