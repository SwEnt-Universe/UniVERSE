package com.android.universe.ui.emailVerification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.jetbrains.annotations.VisibleForTesting

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
class EmailVerificationViewModel() : ViewModel() {
  private val _uiState = MutableStateFlow(EmailVerificationUIState())
  val uiState: StateFlow<EmailVerificationUIState> = _uiState.asStateFlow()
  private var awaitEmailJob: Job? = null

  /**
   * Decrements the countdown timer by one second if it's greater than zero. This is used to control
   * the cooldown period for resending a verification email.
   */
  @VisibleForTesting
  fun countDown() {
    if (_uiState.value.countDown > 0) _uiState.update { it.copy(countDown = it.countDown - 1) }
  }

  /**
   * Sends a verification email to the current user and starts polling for verification status.
   *
   * This function first cancels any existing email verification polling jobs. It updates the UI
   * state with the user's email address. If the user's email is already verified, it updates the
   * state and returns immediately.
   *
   * Otherwise, it attempts to send a verification email using Firebase Authentication.
   * - If the email is sent successfully, it calls `awaitEmailVerification()` to begin polling.
   * - If the sending fails with a [FirebaseTooManyRequestsException], it's treated as a success
   *   (since an email was recently sent), and it proceeds to call `awaitEmailVerification()`.
   * - For any other failure, it updates the UI to indicate the failure and resets the resend
   *   cooldown, allowing the user to try again immediately.
   *
   * @param user The current [FirebaseUser] to whom the verification email will be sent.
   */
  fun sendEmailVerification(user: FirebaseUser) {
    awaitEmailJob?.cancel()
    _uiState.update { it.copy(email = user.email ?: "") }
    if (user.isEmailVerified) {
      _uiState.update { it.copy(emailVerified = true) }
      return
    }
    viewModelScope.launch {
      runCatching { user.sendEmailVerification().await() }
          .onSuccess { awaitEmailVerification(user) }
          .onFailure { th ->
            if (th is FirebaseTooManyRequestsException)
                awaitEmailVerification(
                    user) // Suppress this error, since a validation email was send in the last
            // minute.
            else _uiState.update { it.copy(sendEmailFailed = true, countDown = 0) }
          }
    }
  }

  /**
   * Starts a coroutine to periodically check if the user's email has been verified.
   *
   * This function initiates a polling mechanism. It first resets the `sendEmailFailed` flag and
   * sets the resend cooldown timer to its initial value. It then launches a coroutine that checks
   * the user's email verification status every second.
   *
   * In each iteration, it decrements the cooldown timer and attempts to reload the user's Firebase
   * profile to get the latest `isEmailVerified` status. The loop continues as long as the coroutine
   * is active and the email remains unverified.
   * - If `user.reload()` fails (e.g., due to network issues), the UI state is updated to indicate a
   *   failure, the cooldown is reset to allow an immediate resend, and the polling job is
   *   cancelled.
   * - If the email is successfully verified, the `emailVerified` flag in the UI state is set to
   *   `true`, and the loop terminates.
   *
   * @param user The [FirebaseUser] whose email verification status is being monitored.
   */
  private fun awaitEmailVerification(user: FirebaseUser) {
    _uiState.update { it.copy(countDown = COOLDOWN, sendEmailFailed = false) }
    awaitEmailJob =
        viewModelScope.launch {
          while (isActive && !user.isEmailVerified) {
            delay(ONE_SECOND)
            countDown()
            runCatching { user.reload().await() }
                .onFailure {
                  _uiState.update { it.copy(sendEmailFailed = true, countDown = 0) }
                  cancel()
                }
          }
          if (user.isEmailVerified) _uiState.update { it.copy(emailVerified = true) }
        }
  }
}
