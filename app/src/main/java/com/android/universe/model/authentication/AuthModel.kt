package com.android.universe.model.authentication

import androidx.credentials.Credential
import com.google.firebase.auth.FirebaseUser

interface AuthModel {

  suspend fun signInWithGoogle(
      credential: Credential,
      onSuccess: (FirebaseUser) -> Unit,
      onFailure: (Exception) -> Unit
  )

  suspend fun signOut(onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
}
