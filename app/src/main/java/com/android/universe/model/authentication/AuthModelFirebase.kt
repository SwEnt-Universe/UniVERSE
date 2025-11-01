package com.android.universe.model.authentication

/**
 * Part of the code in this file is copy-pasted from the Bootcamp solution provided by the SwEnt
 * staff.
 */
import android.util.Log
import androidx.credentials.Credential
import com.android.universe.ui.common.InputLimits.EMAIL_MAX_LENGTH
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.tasks.await

/**
 * Firebase implementation of the [AuthModel] interface. Handles authentication with Firebase.
 *
 * @property auth The FirebaseAuth instance.
 * @property helper The helper for Google Sign-In.
 * @property emailRegex The regex to validate the email address.
 */
class AuthModelFirebase(
    private val auth: FirebaseAuth = Firebase.auth,
    private val helper: GoogleSignInHelper = DefaultGoogleSignInHelper(),
    private val emailRegex: Regex = Regex("^[a-zA-Z0-9._%+-]+@epfl\\.ch$"),
) : AuthModel {

  companion object {
    private const val TAG = "AuthModelFirebase"
  }

  /**
   * Signs in the user with Google. This function will check if the credential is a Google ID token
   * and if the email address is from EPFL. If the checks are successful, it will sign in with
   * Firebase.
   *
   * @param credential The credential to sign in with.
   * @param onSuccess The callback to call when the sign-in is successful.
   * @param onFailure The callback to call when the sign-in fails.
   */
  override suspend fun signInWithGoogle(
      credential: Credential,
      onSuccess: (FirebaseUser) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    try {
      // Check if the credential is a Google ID token
      if (credential.type != TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        val exception = IllegalStateException("Credential type not supported: ${credential.type}")
        Log.w(TAG, exception.localizedMessage, exception)
        onFailure(exception)
        return
      }

      // Check if the email address match the REGEX
      val googleCredential = helper.extractIdTokenCredential(credential.data)
      val email = googleCredential.id
      if (!emailRegex.matches(email)) {
        val exception = IllegalStateException("Email address is not from EPFL: $email")
        Log.w(TAG, exception.localizedMessage, exception)
        onFailure(exception)
        return
      }

      // Sign in with Firebase
      val idToken = googleCredential.idToken
      val firebaseCredential = helper.toFirebaseCredential(idToken)
      val user = helper.signInWithFirebase(auth, firebaseCredential).user
      if (user == null) {
        val exception = IllegalStateException("Could not retrieve user information")
        Log.w(TAG, exception.localizedMessage, exception)
        onFailure(exception)
        return
      }

      // Login is successful, return the user via callback
      Log.d(TAG, "Login successful")
      onSuccess(user)
    } catch (e: Exception) {
      Log.e(TAG, "Login failed: ${e.localizedMessage ?: "Unexpected error"}")
      onFailure(e)
    }
  }

  private fun validateEmailForAuth(email: String): String? {
    return when {
      email.isBlank() -> "Email cannot be empty"
      email.length > EMAIL_MAX_LENGTH -> "Email is too long (max $EMAIL_MAX_LENGTH)"
      !emailRegex.matches(email) -> "Not a valid @epfl.ch email address"
      else -> null
    }
  }

  /**
   * Signs in a user with an email and password.
   *
   * This function first validates the email format. If the format is invalid, it throws an
   * [InvalidEmailException]. It then attempts to create a new user with the provided credentials.
   * If an account with that email already exists ([FirebaseAuthUserCollisionException]), it
   * proceeds to sign in the existing user instead.
   *
   * @param email The user's email address.
   * @param password The user's password.
   * @return A [Result] object containing the [FirebaseUser] on success, or an [Exception] on
   *   failure.
   * @throws InvalidEmailException if the email format is not valid.
   * @throws SignInFailedException if the user is not available after a successful authentication
   *   attempt.
   */
  override suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> =
      runCatching {
        validateEmailForAuth(email)?.let { throw InvalidEmailException(it) }
        val authResult =
            try {
              auth.createUserWithEmailAndPassword(email, password).await()
            } catch (_: FirebaseAuthUserCollisionException) {
              auth.signInWithEmailAndPassword(email, password).await()
            }
        authResult?.user ?: throw SignInFailedException()
      }

  /**
   * Signs out the current user.
   *
   * @param onSuccess The callback to call when the sign-out is successful.
   * @param onFailure The callback to call when the sign-out fails.
   * @see FirebaseAuth.signOut
   */
  override suspend fun signOut(onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    try {
      // Firebase sign out
      auth.signOut()
      onSuccess()
    } catch (e: Exception) {
      Log.e(TAG, "Logout failed: ${e.localizedMessage ?: "Unexpected error"}")
      onFailure(e)
    }
  }
}

const val SIGN_IN_FAILED_EXCEPTION_MESSAGE = "Sign in Failed"

class InvalidEmailException(message: String) : Exception(message)

class SignInFailedException() : Exception(SIGN_IN_FAILED_EXCEPTION_MESSAGE)
