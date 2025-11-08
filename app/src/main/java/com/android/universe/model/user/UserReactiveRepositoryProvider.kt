package com.android.universe.model.user

import com.google.firebase.firestore.FirebaseFirestore

/**
 * Singleton provider for a [UserReactiveRepository] instance.
 *
 * Supplies a shared instance used across the app to provide reactive user data streams.
 */
object UserReactiveRepositoryProvider {
  private val _repository: UserReactiveRepository by lazy {
    UserReactiveRepository(FirebaseFirestore.getInstance())
  }

  /** Public repository instance (read-only) */
  val repository: UserReactiveRepository = _repository

  /**
   * Closes and disposes the shared repository.
   *
   * Cancels all active Firestore listeners and releases internal resources. Should be called on app
   * shutdown (e.g. in Application.onTerminate or tests).
   */
  fun close() {
    _repository.close()
  }
}
