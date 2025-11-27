package com.android.universe.model.user

import android.util.Log
import com.android.universe.di.DefaultDP
import com.android.universe.model.tag.Tag
import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.format.DateTimeParseException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

// Firestore collection path for user profiles.
const val USERS_COLLECTION_PATH = "users"

/**
 * Converts a Firestore DocumentSnapshot to a UserProfile object.
 *
 * @param doc the DocumentSnapshot to convert.
 * @return the corresponding UserProfile object.
 * @throws Exception if any required field is missing or has an invalid format.
 */
fun documentToUserProfile(doc: DocumentSnapshot): UserProfile {
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
            (doc.get("tags") as? List<*>)
                ?.filterIsInstance<Number>()
                ?.mapNotNull { ordinal -> Tag.entries.getOrNull(ordinal.toInt()) }
                ?.toSet() ?: emptySet(),
        profilePicture = doc.getBlob("profilePicture")?.toBytes(),
        followers =
            (doc.get("followers") as? List<*>)?.filterIsInstance<String>()?.toSet() ?: emptySet(),
        following =
            (doc.get("following") as? List<*>)?.filterIsInstance<String>()?.toSet() ?: emptySet())
  } catch (e: DateTimeParseException) {
    Log.e(
        "UserRepositoryFirestore.documentToUserProfile",
        "Error converting document to UserProfile, invalid date format",
        e)
    throw e
  } catch (e: NullPointerException) {
    Log.e(
        "UserRepositoryFirestore.documentToUserProfile",
        "Error converting document to UserProfile, assigning null to non-nullable field",
        e)
    throw e
  }
}

/**
 * Firestore implementation of [UserRepository] to store user profiles in the Firestore database.
 *
 * Stores user profiles in the database and persists data between app launches.
 *
 * @param db the Firestore database instance.
 */
class UserRepositoryFirestore(
    private val db: FirebaseFirestore,
    private val iODispatcher: CoroutineDispatcher = DefaultDP.io
) : UserRepository {
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
        "tags" to user.tags.map { it.ordinal },
        "profilePicture" to
            (if (user.profilePicture != null) {
              Blob.fromBytes(user.profilePicture)
            } else {
              null
            }),
        "followers" to user.followers.toList(),
        "following" to user.following.toList())
  }

  /**
   * Retrieves all users currently stored in the database.
   *
   * @return a list of [UserProfile] objects.
   */
  override suspend fun getAllUsers(): List<UserProfile> {
    val users = ArrayList<UserProfile>()
    val querySnapshot =
        withContext(iODispatcher) { db.collection(USERS_COLLECTION_PATH).get().await() }

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
    val user =
        withContext(iODispatcher) {
          db.collection(USERS_COLLECTION_PATH).document(uid).get().await()
        }
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
    withContext(iODispatcher) {
      db.collection(USERS_COLLECTION_PATH)
          .document(userProfile.uid)
          .set(userProfileToMap(userProfile))
          .await()
    }
  }

  /**
   * Updates an existing user profile identified by username.
   *
   * @param uid the uid of the user to update.
   * @param newUserProfile the new [UserProfile] to replace the old one.
   * @throws NoSuchElementException if no user with the given [uid] exists.
   */
  override suspend fun updateUser(uid: String, newUserProfile: UserProfile) {
    val user =
        withContext(iODispatcher) {
          db.collection(USERS_COLLECTION_PATH).document(uid).get().await()
        }
    if (user.exists()) {
      val mappedProfile = userProfileToMap(newUserProfile.copy(uid = uid))
      withContext(iODispatcher) {
        db.collection(USERS_COLLECTION_PATH).document(uid).set(mappedProfile).await()
      }
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
    val user =
        withContext(iODispatcher) {
          db.collection(USERS_COLLECTION_PATH).document(uid).get().await()
        }
    if (user.exists()) {
      withContext(iODispatcher) {
        db.collection(USERS_COLLECTION_PATH).document(uid).delete().await()
      }
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
        withContext(iODispatcher) {
          db.collection(USERS_COLLECTION_PATH).whereEqualTo("username", username).get().await()
        }
    return querySnapshot.isEmpty
  }

  /**
   * Add the targetUserId to the currentUser following list if the follow argument is true. Add the
   * currentUserId to the targetUser follower list otherwise.
   *
   * @param currentUserId the uid of the user who wants to follow the target user.
   * @param targetUserId the uid of the user who is being followed by the current user.
   * @param follow the boolean that determine if it's a follow action or an unfollow action.
   */
  private suspend fun modifyFollow(currentUserId: String, targetUserId: String, follow: Boolean) {
    withContext(iODispatcher) {
      db.runTransaction { transaction ->
            val currentUserPath = db.collection(USERS_COLLECTION_PATH).document(currentUserId)
            val targetUserPath = db.collection(USERS_COLLECTION_PATH).document(targetUserId)

            val currentUserDoc = transaction.get(currentUserPath)
            val targetUserDoc = transaction.get(targetUserPath)

            if (!currentUserDoc.exists()) {
              throw NoSuchElementException("No user with current UID $currentUserId found")
            } else if (!targetUserDoc.exists()) {
              throw NoSuchElementException("No user with target UID $targetUserId found")
            }

            if (follow) {
              transaction.update(currentUserPath, "following", FieldValue.arrayUnion(targetUserId))
              transaction.update(targetUserPath, "followers", FieldValue.arrayUnion(currentUserId))
            } else {
              transaction.update(currentUserPath, "following", FieldValue.arrayRemove(targetUserId))
              transaction.update(targetUserPath, "followers", FieldValue.arrayRemove(currentUserId))
            }
          }
          .await()
    }
  }

  /**
   * Add the targetUserId to the currentUser following list. Add the currentUserId to the targetUser
   * follower list.
   *
   * @param currentUserId the uid of the user who wants to follow the target user.
   * @param targetUserId the uid of the user who is being followed by the current user.
   */
  override suspend fun followUser(currentUserId: String, targetUserId: String) {
    modifyFollow(currentUserId, targetUserId, true)
  }

  /**
   * Remove the targetUserId to the currentUser following list. Remove the currentUserId to the
   * targetUser follower list.
   *
   * @param currentUserId the uid of the user who wants to unfollow the target user.
   * @param targetUserId the uid of the user who is being unfollowed by the current user.
   */
  override suspend fun unfollowUser(currentUserId: String, targetUserId: String) {
    modifyFollow(currentUserId, targetUserId, false)
  }
}
