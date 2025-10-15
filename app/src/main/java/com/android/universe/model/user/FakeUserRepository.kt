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
   * @param uid the unique uid of the user.
   * @return the [UserProfile] associated with the given uid.
   * @throws NoSuchElementException if no user with the given [uid] exists.
   */
  override suspend fun getUser(uid: String): UserProfile {
    return users.firstOrNull { it.uid == uid }
        ?: throw NoSuchElementException("No user found with username: $uid")
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
   * @param uid the uid of the user to update.
   * @param newUserProfile the new [UserProfile] to replace the old one.
   * @throws NoSuchElementException if no user with the given [uid] exists.
   */
  override suspend fun updateUser(uid: String, newUserProfile: UserProfile) {
    val index = users.indexOfFirst { it.uid == uid }
    if (index != -1) {
      users[index] = newUserProfile
    } else {
      throw NoSuchElementException("No user found with username: $uid")
    }
  }

  /**
   * Deletes a user by uid.
   *
   * @param uid the uid of the user to remove. If no such user exists
   * @throws NoSuchElementException if no user with the given [uid] exists.
   */
  override suspend fun deleteUser(uid: String) {
    val removed = users.removeIf { it.uid == uid }
    if (!removed) {
      throw NoSuchElementException("No user found with username: $uid")
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
