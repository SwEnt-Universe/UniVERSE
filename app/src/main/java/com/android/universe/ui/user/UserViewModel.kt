package com.android.universe.ui.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.universe.model.user.UserProfile
import com.android.universe.model.user.UserRepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for managing user-related data and operations.
 *
 * Acts as a bridge between the UI and the [com.android.universe.model.user.UserRepository],
 * exposing user data in a UI-observable form and handling repository operations safely in
 * coroutines.
 *
 * Uses [StateFlow] to expose the current list of users, which can be collected by the UI to
 * reactively update when data changes.
 *
 * All repository calls are launched in [ViewModelScope] to ensure they are tied to the ViewModel's
 * lifecycle and run asynchronously.
 */
class UserViewModel : ViewModel() {

  /** Repository instance used to access user data. */
  private val repository = UserRepositoryProvider.repository

  /** Internal mutable state of all users */
  private val _users = MutableStateFlow<List<UserProfile>>(emptyList())

  /** Public read-only state flow of all users for UI observation */
  val users: StateFlow<List<UserProfile>> = _users

  init {
    // Load initial users when ViewModel is created
    loadUsers()
  }

  /**
   * Loads all users from the repository and updates [_users] state.
   *
   * This triggers UI observers to receive the latest list of users.
   */
  fun loadUsers() {
    viewModelScope.launch {
      val allUsers = repository.getAllUsers()
      _users.value = allUsers
    }
  }

  /**
   * Retrieves a single user by username.
   *
   * @param username the username of the user to retrieve
   * @param callback invoked with the [UserProfile] if found, or null otherwise.
   */
  fun getUser(username: String, callback: (UserProfile?) -> Unit) {
    viewModelScope.launch { callback(repository.getUser(username)) }
  }

  /**
   * Adds a new user to the repository and refreshes [_users]
   *
   * @param user the [UserProfile] to add.
   */
  fun addUser(user: UserProfile) {
    viewModelScope.launch {
      repository.addUser(user)
      loadUsers()
    }
  }

  /**
   * Updates an existing user's profile and refreshes [_users].
   *
   * @param username the username of the user to update.
   * @param newUserProfile the updated [UserProfile].
   */
  fun editUser(username: String, newUserProfile: UserProfile) {
    viewModelScope.launch {
      repository.editUser(username, newUserProfile)
      loadUsers()
    }
  }

  /**
   * Deletes a user from the reposiroty and refreshes [_users]
   *
   * @param username the username of the user to delete.
   */
  fun deleteUser(username: String) {
    viewModelScope.launch {
      repository.deleteUser(username)
      loadUsers()
    }
  }

  /**
   * Checks if a username is unique (not already taken).
   *
   * @param username the username to check.
   * @param callback invoked with true if the username is unique, false otherwise.
   */
  fun isUsernameUnique(username: String, callback: (Boolean) -> Unit) {
    viewModelScope.launch { callback(repository.isUsernameUnique(username)) }
  }
}
