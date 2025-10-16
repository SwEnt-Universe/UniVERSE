package com.android.universe.model.user

import com.android.universe.model.Tag
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

/**
 * Singleton provider for a [UserRepository] instance.
 *
 * This provider supplies a pre-populated [FakeUserRepository] that can be used for UI development
 * and testing purposes. It is intended to be a single shared repository instance across the
 * application.
 */
object UserRepositoryProvider {
  private val _repository: UserRepository by lazy { UserRepositoryFirestore(Firebase.firestore) }

  /** Public repository instance (read-only) */
  var repository: UserRepository = _repository
}
