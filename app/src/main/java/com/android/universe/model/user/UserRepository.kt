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
   * @param uid the unique uid of the user.
   * @return the UserProfile associated with the given username
   * @throws java.lang.IllegalArgumentException() if no user with this uid is found.
   */
  suspend fun getUser(uid: String): UserProfile

  /**
   * Adds a new user to the repository.
   *
   * @param userProfile the UserProfile to add.
   */
  suspend fun addUser(userProfile: UserProfile)

  /**
   * Updates an existing user's profile.
   *
   * @param uid the uid of the user to update.
   * @param newUserProfile the updated UserProfile.
   */
  suspend fun updateUser(uid: String, newUserProfile: UserProfile)

  /**
   * Deletes a user from the repository.
   *
   * @param uid the uid of the user to delete.
   */
  suspend fun deleteUser(uid: String)

  /**
   * Checks if a username is unique (i.e., not already taken by another user).
   *
   * @param username the username to check.
   * @return true if the username is not used by any other user; false otherwise.
   */
  suspend fun isUsernameUnique(username: String): Boolean

  /**
   * Add the targetUserId to the currentUser following list.
   * Add the currentUserId to the targetUser follower list.
   *
   * @param currentUserId the uid of the user who wants to follow the target user.
   * @param targetUserId the uid of the user who is being followed by the current user.
   */
  suspend fun followUser(currentUserId: String, targetUserId: String)

  /**
   * Remove the targetUserId to the currentUser following list.
   * Remove the currentUserId to the targetUser follower list.
   *
   * @param currentUserId the uid of the user who wants to unfollow the target user.
   * @param targetUserId the uid of the user who is being unfollowed by the current user.
   */
  suspend fun unfollowUser(currentUserId: String, targetUserId: String)
}
