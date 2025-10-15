package com.android.universe.model.user

import android.util.Log
import com.android.universe.model.Tag
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

// Firestore collection path for user profiles.
const val USERS_COLLECTION_PATH = "users"

/**
 * Firestore implementation of [UserRepository] to store user profiles in the Firestore database.
 *
 * Stores user profiles in the database and persists data between app launches.
 * @param db the Firestore database instance.
 */
class UserRepositoryFirestore(private val db: FirebaseFirestore) : UserRepository {
    /**
     * Converts a UserProfile object to a Map<String, Any?>.
     * @param user the UserProfile to convert.
     * @return a map representation of the UserProfile.
     */
    private fun userProfileToMap(user: UserProfile): Map<String, Any?> {
        return mapOf(
            "username" to user.username,
            "firstName" to user.firstName,
            "lastName" to user.lastName,
            "country" to user.country,
            "description" to user.description,
            "dateOfBirth" to user.dateOfBirth.toString(),
            "tags" to user.tags.map { it.ordinal }
        )
    }

    /**
     * Converts a Firestore DocumentSnapshot to a UserProfile object.
     *
     * @param doc the DocumentSnapshot to convert.
     * @return the corresponding UserProfile object.
     * @throws Exception if any required field is missing or has an invalid format.
     */
    private fun documentToUserProfile(doc: DocumentSnapshot): UserProfile {
        return try{
            UserProfile(
                username = doc.getString("username") ?: "",
                firstName = doc.getString("firstName") ?: "",
                lastName = doc.getString("lastName") ?: "",
                country = doc.getString("country") ?: "",
                description = doc.getString("description"),
                dateOfBirth = LocalDate.parse(doc.getString("dateOfBirth")),
                tags = (doc.get("tags") as? List<Number>)?.map { ordinal -> Tag.entries[ordinal.toInt()] }?.toSet() ?: emptySet()
            )
        } catch (e: Exception) {
            Log.e("UserRepositoryFirestore.documentToUserProfile", "Error converting document to UserProfile", e)
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
        val querySnapshot =
            db.collection(USERS_COLLECTION_PATH).get().await()

        for (document in querySnapshot.documents) {
            val user = documentToUserProfile(document)
            users.add(user)
        }
        return users
    }

    /**
     * Retrieves a user by their username.
     *
     * @param username the unique username of the user.
     * @return the [UserProfile] associated with the given username.
     * @throws NoSuchElementException if no user with the given [username] exists.
     */
    override suspend fun getUser(username: String): UserProfile {
        val user = db.collection(USERS_COLLECTION_PATH).document(username).get().await()
        if (user.exists()) {
            return documentToUserProfile(user)
        } else {
            throw NoSuchElementException("No user with username $username found")
        }
    }

    /**
     * Adds a new user to the repository.
     *
     * @param userProfile the [UserProfile] to add.
     */
    override suspend fun addUser(userProfile: UserProfile) {
        db.collection(USERS_COLLECTION_PATH).document(userProfile.username)
            .set(userProfileToMap(userProfile)).await()
    }

    /**
     * Updates an existing user profile identified by username.
     *
     * @param username the username of the user to update.
     * @param newUserProfile the new [UserProfile] to replace the old one.
     * @throws NoSuchElementException if no user with the given [username] exists.
     */
    override suspend fun updateUser(
        username: String,
        newUserProfile: UserProfile
    ) {
        val user = db.collection(USERS_COLLECTION_PATH).document(username).get().await()
        if (user.exists()) {
            db.collection(USERS_COLLECTION_PATH).document(username).set(userProfileToMap(newUserProfile)).await()
        } else {
            throw NoSuchElementException("No user with username $username found")
        }
    }

    /**
     * Deletes a user by username.
     *
     * @param username the username of the user to remove. If no such user exists
     * @throws NoSuchElementException if no user with the given [username] exists.
     */
    override suspend fun deleteUser(username: String) {
        val user = db.collection(USERS_COLLECTION_PATH).document(username).get().await()
        if (user.exists()) {
            db.collection(USERS_COLLECTION_PATH).document(username).delete().await()
        }else{
            throw NoSuchElementException("No user with username $username found")
        }
    }

    /**
     * Checks if a given username is unique in the database.
     *
     * @param username the username to check.
     * @return true if no user with this username exists; false otherwise.
     */
    override suspend fun isUsernameUnique(username: String): Boolean {
        val querySnapshot = db.collection(USERS_COLLECTION_PATH)
            .whereEqualTo("username", username)
            .get()
            .await()
        return querySnapshot.isEmpty
    }
}