package com.android.universe.model.user

import android.util.Log
import com.android.universe.model.Tag
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import kotlinx.coroutines.tasks.await

// Firestore collection path for user profiles.
const val USERS_COLLECTION_PATH = "users"

/** Check if a List is of type T and safely casts it, returning an empty list if not. */
private inline fun <reified T> Any?.safeCastList(): List<T> {
  return if (this is List<*>) {
    this.filterIsInstance<T>()
  } else emptyList()
}

/**
 * Firestore implementation of [UserRepository] to store user profiles in the Firestore database.
 *
 * Stores user profiles in the database and persists data between app launches.
 *
 * @param db the Firestore database instance.
 */
class UserRepositoryFirestore(private val db: FirebaseFirestore) : UserRepository {
  /**
   * Converts a UserProfile object to a Map<String, Any?>.
   *
   * @param user the UserProfile to convert.
   * @return a map representation of the UserProfile.
   */
  private fun userProfileToMap(user: UserProfile): Map<String, Any?> {
    return mapOf(
        "uid" to user.uid,
        "username" to user.username,
        "firstName" to user.firstName,
        "lastName" to user.lastName,
        "country" to user.country,
        "description" to user.description,
        "dateOfBirth" to user.dateOfBirth.toString(),
        "tags" to user.tags.map { it.ordinal })
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
          "UserRepositoryFirestore.documentToUserProfile",
          "Error converting document to UserProfile",
          e)
      throw e
    }
  }

  /**
   * Retrieves all users currently stored in the database.
   *
   * @return a list of [UserProfile] objects.
   */
  override suspend fun getAllUsers(): List<UserProfile> {
    val users = ArrayList<UserProfile>()
    val querySnapshot = db.collection(USERS_COLLECTION_PATH).get().await()

    for (document in querySnapshot.documents) {
      val user = documentToUserProfile(document)
      users.add(user)
    }
    return users
  }

  /**
   * Retrieves a user by their username.
   *
   * @param uid the unique uid of the user.
   * @return the [UserProfile] associated with the given username.
   * @throws NoSuchElementException if no user with the given [uid] exists.
   */
  override suspend fun getUser(uid: String): UserProfile {
    val user = db.collection(USERS_COLLECTION_PATH).document(uid).get().await()
    if (user.exists()) {
      return documentToUserProfile(user)
    } else {
      throw NoSuchElementException("No user with username $uid found")
    }
  }

  /**
   * Adds a new user to the repository.
   *
   * @param userProfile the [UserProfile] to add.
   */
  override suspend fun addUser(userProfile: UserProfile) {
    db.collection(USERS_COLLECTION_PATH)
        .document(userProfile.uid)
        .set(userProfileToMap(userProfile))
        .await()
  }

  /**
   * Updates an existing user profile identified by username.
   *
   * @param uid the uid of the user to update.
   * @param newUserProfile the new [UserProfile] to replace the old one.
   * @throws NoSuchElementException if no user with the given [uid] exists.
   */
  override suspend fun updateUser(uid: String, newUserProfile: UserProfile) {
    val user = db.collection(USERS_COLLECTION_PATH).document(uid).get().await()
    if (user.exists()) {
      db.collection(USERS_COLLECTION_PATH)
          .document(uid)
          .set(userProfileToMap(newUserProfile.copy(uid = uid)))
          .await()
    } else {
      throw NoSuchElementException("No user with username $uid found")
    }
  }

  /**
   * Deletes a user by username.
   *
   * @param uid the uid of the user to delete. If no such user exists
   * @throws NoSuchElementException if no user with the given [uid] exists.
   */
  override suspend fun deleteUser(uid: String) {
    val user = db.collection(USERS_COLLECTION_PATH).document(uid).get().await()
    if (user.exists()) {
      db.collection(USERS_COLLECTION_PATH).document(uid).delete().await()
    } else {
      throw NoSuchElementException("No user with username $uid found")
    }
  }

  /**
   * Checks if a given username is unique in the database.
   *
   * @param username the username to check.
   * @return true if no user with this username exists; false otherwise.
   */
  override suspend fun isUsernameUnique(username: String): Boolean {
    val querySnapshot =
        db.collection(USERS_COLLECTION_PATH).whereEqualTo("username", username).get().await()
    return querySnapshot.isEmpty
  }
}
