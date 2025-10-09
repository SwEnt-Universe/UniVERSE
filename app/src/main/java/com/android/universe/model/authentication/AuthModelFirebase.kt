package com.android.universe.model.authentication

import android.util.Log
import androidx.credentials.Credential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.tasks.await

class AuthModelFirebase(
    private val auth: FirebaseAuth = Firebase.auth,
    private val helper: GoogleSignInHelper = DefaultGoogleSignInHelper(),
    private val emailRegex: Regex = Regex("^[a-zA-Z0-9._%+-]+@epfl\\.ch$"),
) : AuthModel {

  companion object {
    private const val TAG = "AuthModelFirebase"
  }

  override suspend fun signInWithGoogle(
      credential: Credential,
      onSuccess: (FirebaseUser) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    try {
      // Check if the credential is a Google ID token
      if (credential.type != TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        Log.w(TAG, "Credential type not supported: ${credential.type}")
        onFailure(IllegalStateException("Credential type not supported: ${credential.type}"))
        return
      }

      // Check if the email address match the REGEX
      val credential = helper.extractIdTokenCredential(credential.data)
      val email = credential.id
      if (!emailRegex.matches(email)) {
        Log.w(TAG, "Email address is not from EPFL: $email")
        onFailure(IllegalStateException("Email address is not from EPFL: $email"))
        return
      }

      // Sign in with Firebase
      val idToken = credential.idToken
      val firebaseCredential = helper.toFirebaseCredential(idToken)
      val user = auth.signInWithCredential(firebaseCredential).await().user
      if (user == null) {
        Log.w(TAG, "Could not retrieve user information")
        onFailure(IllegalStateException("Could not retrieve user information"))
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
