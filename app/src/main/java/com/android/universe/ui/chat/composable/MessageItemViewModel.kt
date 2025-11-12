package com.android.universe.ui.chat.composable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.universe.model.user.UserRepository
import com.android.universe.model.user.UserRepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for a single message item.
 *
 * This ViewModel is responsible for fetching and exposing the username of a message sender. It
 * caches the results to avoid redundant network calls for the same user ID.
 *
 * @param userRepository The repository to fetch user data from.
 */
class MessageItemViewModel(
    private val userRepository: UserRepository = UserRepositoryProvider.repository
) : ViewModel() {

  // Map of senderID -> MutableStateFlow(username)
  private val _userFlows = mutableMapOf<String, MutableStateFlow<String>>()

  /**
   * Retrieves a [StateFlow] that emits the username for a given [userID].
   *
   * This function is designed to be called from a composable. It manages a cache of user data; if
   * the username for the given [userID] is not already loaded or being loaded, it will initiate a
   * new asynchronous request. The returned [StateFlow] will initially emit a placeholder value
   * ("...") and will later be updated with the fetched username. If the user has been deleted or is
   * not found, it will emit "deleted".
   *
   * @param userID The unique identifier of the user whose name is to be fetched.
   * @return A [StateFlow] that will emit the username.
   */
  fun getUserName(userID: String): StateFlow<String> {
    return _userFlows
        .getOrPut(userID) { MutableStateFlow("...").also { flow -> loadUserName(userID, flow) } }
        .asStateFlow()
  }

  /**
   * Loads the username for the given [userID] and updates the provided [flow] with the result. It
   * launches a coroutine in the viewModelScope to fetch the user data asynchronously. If the user
   * is found, their username is emitted to the flow. If the user cannot be found (e.g., has been
   * deleted), the flow is updated with the string "deleted".
   *
   * @param userID The ID of the user whose name is to be loaded.
   * @param flow The [MutableStateFlow] that will be updated with the loaded username.
   */
  private fun loadUserName(userID: String, flow: MutableStateFlow<String>) {
    viewModelScope.launch {
      val name =
          try {
            userRepository.getUser(userID).username
          } catch (_: IllegalArgumentException) {
            "deleted"
          }
      flow.value = name
    }
  }
}
