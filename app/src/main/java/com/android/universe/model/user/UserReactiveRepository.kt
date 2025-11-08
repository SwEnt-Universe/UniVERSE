package com.android.universe.model.user

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.shareIn

class UserReactiveRepository(private val db: FirebaseFirestore) {
  // Cache of user flows so we don't re-subscribe for the same uid
  private val userFlows = mutableMapOf<String, SharedFlow<UserProfile?>>()
  private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

  fun getUserFlow(uid: String): Flow<UserProfile?> {
    return userFlows.getOrPut(uid) {
      callbackFlow {
            val listener =
                db.collection(USERS_COLLECTION_PATH).document(uid).addSnapshotListener {
                    snapshot,
                    error ->
                  if (error != null) {
                    close(error)
                    return@addSnapshotListener
                  }
                  /**
                   * This function is called without reason sometimes as such bad data can be
                   * received and notably the date of birth will throw an exception which needs to
                   * be caught but not resent
                   */
                  try {
                    val user = documentToUserProfile(snapshot!!)
                    trySend(user)
                  } catch (e: Exception) {
                    Log.e("UserReactiveRepository", "Error converting document to UserProfile", e)
                  }
                }
            awaitClose { listener.remove() }
          }
          // shareIn makes sure all collectors share the same Firestore listener
          .shareIn(scope = scope, started = SharingStarted.Eagerly, replay = 1)
    }
  }
}
