package com.android.universe.model.user

/**
 * Repository interface for accessing and managing user profiles.
 *
 * Provides basic CRUD operations as well as helper functions commonly used in the authentification
 * and profile flow.
 */
interface UserRepository {

  /**
   * Retrieves all users.
   *
   * @return a list of all UserProfile objects.
   */
  suspend fun getAllUsers(): List<UserProfile>

  /**
   * Retrieves a single user by username.
   *
   * @param username the unique username of the user.
   * @return the UserProfile associated with the given username, or null if not found.
   */
  suspend fun getUser(username: String): UserProfile

  /**
   * Adds a new user to the repository.
   *
   * @param userProfile the UserProfile to add.
   */
  suspend fun addUser(userProfile: UserProfile)

  /**
   * Updates an existing user's profile.
   *
   * @param username the username of the user to update.
   * @param newUserProfile the updated UserProfile.
   */
  suspend fun updateUser(username: String, newUserProfile: UserProfile)

  /**
   * Deletes a user from the repository.
   *
   * @param username the username of the user to delete.
   */
  suspend fun deleteUser(username: String)

  /**
   * Checks if a username is unique (i.e., not already taken by another user).
   *
   * @param username the username to check.
   * @return true if the username is not used by any other user; false otherwise.
   */
  suspend fun isUsernameUnique(username: String): Boolean
}
