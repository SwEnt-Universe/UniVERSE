package com.android.universe.model.user

/**
 * Fake implementation of [UserRepository] for testing and UI development purposes.
 *
 * Stores user profiles in memory and does not persist data between app launches. This allows screen
 * and ViewModels to work independently of the real backend.
 */
class FakeUserRepository : UserRepository {

  /** Internal in-memory storage for user profiles */
  private val users = mutableListOf<UserProfile>()

  /**
   * Retrieves all users currently stored in the repository.
   *
   * @return a list of [UserProfile] objects. Returns a copy to prevent external modification.
   */
  override suspend fun getAllUsers(): List<UserProfile> {
    return users.toList()
  }

  /**
   * Retrieves a user by their username.
   *
   * @param username the unique username of the user.
   * @return the [UserProfile] associated with the given username.
   * @throws NoSuchElementException if no user with the given [username] exists.
   */
  override suspend fun getUser(username: String): UserProfile {
    return users.firstOrNull { it.username == username }
        ?: throw NoSuchElementException("No user found with username: $username")
  }

  /**
   * Adds a new user to the repository.
   *
   * @param userProfile the [UserProfile] to add.
   */
  override suspend fun addUser(userProfile: UserProfile) {
    users.add(userProfile)
  }

  /**
   * Updates an existing user profile identified by username.
   *
   * @param username the username of the user to update.
   * @param newUserProfile the new [UserProfile] to replace the old one.
   * @throws NoSuchElementException if no user with the given [username] exists.
   */
  override suspend fun updateUser(username: String, newUserProfile: UserProfile) {
    val index = users.indexOfFirst { it.username == username }
    if (index != -1) {
      users[index] = newUserProfile
    } else {
      throw NoSuchElementException("No user found with username: $username")
    }
  }

  /**
   * Deletes a user by username.
   *
   * @param username the username of the user to remove. If no such user exists
   * @throws NoSuchElementException if no user with the given [username] exists.
   */
  override suspend fun deleteUser(username: String) {
    val removed = users.removeIf { it.username == username }
    if (!removed) {
      throw NoSuchElementException("No user found with username: $username")
    }
  }

  /**
   * Checks if a given username is unique in the repository.
   *
   * @param username the username to check.
   * @return true if no user with this username exists; false otherwise.
   */
  override suspend fun isUsernameUnique(username: String): Boolean {
    return users.none { it.username == username }
  }
}
