package com.android.universe.model.user

import android.util.Log
import com.android.universe.model.Tag
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
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

  /** Check if a List is of type T and safely casts it, returning an empty list if not. */
  private inline fun <reified T> Any?.safeCastList(): List<T> {
    return if (this is List<*>) {
      this.filterIsInstance<T>()
    } else emptyList()
  }

  /**
   * Converts a Firestore DocumentSnapshot to a UserProfile object.
   *
   * @param doc the DocumentSnapshot to convert.
   * @return the corresponding UserProfile object.
   * @throws Exception if any required field is missing or has an invalid format.
   */
  private fun documentToUserProfile(doc: DocumentSnapshot): UserProfile {
    return try {
      UserProfile(
          uid = doc.getString("uid") ?: "",
          username = doc.getString("username") ?: "",
          firstName = doc.getString("firstName") ?: "",
          lastName = doc.getString("lastName") ?: "",
          country = doc.getString("country") ?: "",
          description = doc.getString("description"),
          dateOfBirth = LocalDate.parse(doc.getString("dateOfBirth")),
          tags =
              (doc.get("tags").safeCastList<Number>())
                  .map { ordinal -> Tag.entries[ordinal.toInt()] }
                  .toSet())
    } catch (e: Exception) {
      Log.e(
          "UserReactiveRepository.documentToUserProfile",
          "Error converting document to UserProfile",
          e)
      throw e
    }
  }
}
