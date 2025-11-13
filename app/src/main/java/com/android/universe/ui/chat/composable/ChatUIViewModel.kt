package com.android.universe.ui.chat.composable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.universe.model.chat.Chat
import com.android.universe.model.chat.ChatManager
import com.android.universe.model.chat.Message
import com.android.universe.model.user.UserRepository
import com.android.universe.model.user.UserRepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

const val MAX_MESSAGE_LENGTH = 256

class ChatUIViewModel(
    private val chatID: String,
    private val userID: String,
    private val userRepository: UserRepository = UserRepositoryProvider.repository
) : ViewModel() {

  // Define the various states for the chat screen data
  sealed interface ChatUiState {
    data object Loading : ChatUiState

    data class Success(val chat: Chat) : ChatUiState

    data class Error(val errorMsg: String = "Failed to load chat") : ChatUiState
  }

  private val _messageText = MutableStateFlow<String>("")
  val messageText = _messageText.asStateFlow()

  private val _uiState = MutableStateFlow<ChatUiState>(ChatUiState.Loading)
  val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

  init {
    loadChat()
  }

  private fun loadChat() {
    viewModelScope.launch {
      try {
        val chat = ChatManager.loadChat(chatID)
        _uiState.value = ChatUiState.Success(chat)
      } catch (e: Exception) {
        _uiState.value = ChatUiState.Error()
      }
    }
  }

  fun onInput(input: String) {
    if (input.length <= MAX_MESSAGE_LENGTH) _messageText.value = input
    else _messageText.value = input.substring(0, MAX_MESSAGE_LENGTH)
  }

  fun sendMessage() {
    val message = _messageText.value.trim()
    if (message.isBlank()) return

    val currentState = _uiState.value
    if (currentState is ChatUiState.Success) {
      viewModelScope.launch {
        val message = Message(senderID = userID, message = message)
        currentState.chat.sendMessage(message)
      }
    }
  }

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
  fun getUserName(senderID: String): StateFlow<String> {
    return _userFlows
        .getOrPut(senderID) {
          MutableStateFlow("...").also { flow -> loadUserName(senderID, flow) }
        }
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

class ChatUIViewModelFactory(private val chatID: String, private val userID: String) :
    ViewModelProvider.Factory {

  @Suppress("UNCHECKED_CAST")
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    // Check if the requested class is the one we want to build
    if (modelClass.isAssignableFrom(ChatUIViewModel::class.java)) {
      return ChatUIViewModel(chatID, userID) as T
    }
    throw IllegalArgumentException("Unknown ViewModel class")
  }
}
