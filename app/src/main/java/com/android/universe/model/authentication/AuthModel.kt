package com.android.universe.model.authentication

import androidx.credentials.Credential
import com.google.firebase.auth.FirebaseUser

/** Handles authentication-related operations. */
interface AuthModel {

  /**
   * Signs in a user with Google using the provided credential.
   *
   * @param credential The credential object obtained from the Credential Manager API.
   * @param onSuccess Callback invoked when the sign-in is successful, providing the FirebaseUser.
   * @param onFailure Callback invoked when the sign-in fails, providing the exception.
   */
  suspend fun signInWithGoogle(
      credential: Credential,
      onSuccess: (FirebaseUser) -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Signs out the current user and clears the credential state
   *
   * @param onSuccess Callback invoked when the sign-out is successful.
   * @param onFailure Callback invoked when the sign-out fails, providing the exception.
   */
  suspend fun signOut(onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
}
