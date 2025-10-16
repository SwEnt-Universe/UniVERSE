package com.android.universe.model.event

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

/**
 * Singleton provider for an [EventRepository] instance.
 *
 * This provider supplies a pre-populated [FakeEventRepository] that can be used for UI development
 * and testing purposes. It is intended to be a single shared repository instance across the
 * application.
 */
object EventRepositoryProvider {
  private val _repository: EventRepository by lazy { EventRepositoryFirestore(Firebase.firestore) }

  /** Public repository instance (read-only) */
  var repository: EventRepository = _repository
}
