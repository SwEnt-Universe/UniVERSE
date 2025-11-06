package com.android.universe.ui.emailVerification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

const val ONE_SECOND = 1000L
const val COOLDOWN = 60

/**
 * Represents the UI state for the email verification screen.
 *
 * @property email The email address of the user to be verified.
 * @property countDown The remaining time in seconds before the user can request another
 *   verification email.
 * @property emailVerified A flag indicating whether the user's email has been successfully
 *   verified.
 * @property sendEmailFailed A flag indicating if the last attempt to send a verification email
 *   failed.
 * @property resendEnabled A computed property that is true when the countdown is zero, allowing the
 *   user to resend the email.
 */
data class EmailVerificationUIState(
    val email: String = "",
    val countDown: Int = COOLDOWN,
    val emailVerified: Boolean = false,
    val sendEmailFailed: Boolean = false,
) {
  val resendEnabled: Boolean
    get() = countDown == 0
}

/**
 * ViewModel for the email verification screen.
 *
 * This ViewModel handles the logic for sending a verification email, polling for the user's email
 * verification status, and managing the UI state, including a cooldown timer for resending the
 * email.
 *
 * @param user The current [FirebaseUser] whose email needs to be verified.
 */
class EmailVerificationViewModel(private val user: FirebaseUser) : ViewModel() {
  private val _uiState = MutableStateFlow(EmailVerificationUIState(email = user.email ?: ""))
  val uiState: StateFlow<EmailVerificationUIState> = _uiState.asStateFlow()
  private var awaitEmailJob: Job? = null

  init {
    sendEmailVerification()
  }

  /**
   * Decrements the countdown timer by one second if it's greater than zero. This is used to control
   * the cooldown period for resending a verification email.
   */
  private fun countDown() {
    if (_uiState.value.countDown > 0) _uiState.update { it.copy(countDown = it.countDown - 1) }
  }

  /**
   * Sends a verification email to the current user.
   *
   * First, it cancels any ongoing email verification checks. If the user's email is already
   * verified, it updates the UI state to reflect this. Otherwise, it attempts to send a
   * verification email using Firebase Auth. If the email is sent successfully, it starts polling to
   * check for email verification status. If sending the email fails, it updates the UI state to
   * show an error and resets the cooldown.
   */
  fun sendEmailVerification() {
    awaitEmailJob?.cancel()
    if (user.isEmailVerified) _uiState.update { it.copy(emailVerified = true) }
    else
        user.sendEmailVerification().addOnCompleteListener { sendEmail ->
          if (sendEmail.isSuccessful) awaitEmailVerification()
          else _uiState.update { it.copy(sendEmailFailed = true, countDown = 0) }
        }
  }

  /**
   * Starts a coroutine to periodically check if the user's email has been verified. It resets the
   * resend cooldown timer and enters a loop that runs every second. Inside the loop, it decrements
   * the cooldown timer, reloads the user's Firebase profile to get the latest email verification
   * status, and continues until the email is verified. Once verified, it updates the UI state to
   * reflect this.
   */
  private fun awaitEmailVerification() {
    _uiState.update { it.copy(countDown = COOLDOWN, sendEmailFailed = false) }
    awaitEmailJob =
        viewModelScope.launch {
          while (!user.isEmailVerified) {
            delay(ONE_SECOND)
            countDown()
            user.reload().await()
          }
          _uiState.update { it.copy(emailVerified = true) }
        }
  }
}
