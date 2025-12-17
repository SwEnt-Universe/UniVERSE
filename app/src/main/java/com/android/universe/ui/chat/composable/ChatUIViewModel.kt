package com.android.universe.ui.chat.composable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.universe.model.chat.Chat
import com.android.universe.model.chat.ChatManager
import com.android.universe.model.chat.Message
import com.android.universe.model.user.UserRepository
import com.android.universe.model.user.UserRepositoryProvider
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

const val MAX_MESSAGE_LENGTH = 256
const val MAX_CACHE_SIZE = 100

/**
 * ViewModel for the chat screen.
 *
 * This ViewModel is responsible for managing the UI state of a specific chat session. It loads chat
 * messages, handles user input for sending new messages, and fetches user details like usernames,
 * with a caching mechanism to improve performance.
 *
 * The UI state is exposed via a [StateFlow] of [ChatUiState], which can represent loading, success,
 * or error states.
 *
 * @param chatID The unique identifier for the chat to be displayed.
 * @param userID The unique identifier of the current user.
 * @param userRepository The repository for fetching user data. Defaults to the singleton
 *   [UserRepositoryProvider.repository].
 */
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

  private val _userNameCache =
      object : LinkedHashMap<String, String>(MAX_CACHE_SIZE, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, String>?): Boolean {
          return size > MAX_CACHE_SIZE
        }
      }
  private val _loadingMutex = Mutex()
  private val _messageText = MutableStateFlow("")
  val messageText = _messageText.asStateFlow()

  private val _uiState = MutableStateFlow<ChatUiState>(ChatUiState.Loading)
  val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

  init {
    loadChat()
  }

  /**
   * Asynchronously loads the chat data for the `chatID` provided during ViewModel initialization.
   *
   * This function is launched in the `viewModelScope`. It attempts to load the chat using
   * `ChatManager.loadChat`. On success, it updates the `_uiState` to `ChatUiState.Success` with the
   * loaded chat data. If an exception occurs during loading, it updates the `_uiState` to
   * `ChatUiState.Error`. This function is called once when the ViewModel is initialized.
   */
  private fun loadChat() {
    viewModelScope.launch {
      try {
        val chat = ChatManager.loadChat(chatID)
        _uiState.value = ChatUiState.Success(chat)
      } catch (_: NoSuchElementException) {
        _uiState.value = ChatUiState.Error()
      }
    }
  }

  /**
   * Updates the message input text state.
   *
   * This function is called when the user types in the message input field. It updates the
   * `_messageText` state flow with the new input. If the input exceeds the `MAX_MESSAGE_LENGTH`, it
   * truncates the input to the maximum allowed length.
   *
   * @param input The latest text from the message input field.
   */
  fun onInput(input: String) {
    if (input.length <= MAX_MESSAGE_LENGTH) _messageText.value = input
    else _messageText.value = input.substring(0, MAX_MESSAGE_LENGTH)
  }

  /**
   * Sends the current message text to the chat.
   *
   * This function takes the text from the `messageText` state, trims any leading or trailing
   * whitespace, and if the resulting message is not blank, it creates a [Message] object and sends
   * it through the current chat instance. After sending, it clears the message input field. This
   * operation is performed within a coroutine scope. It only proceeds if the UI state is
   * [ChatUiState.Success].
   */
  fun sendMessage() {
    val message = _messageText.value.trim()
    if (message.isBlank()) return

    val currentState = _uiState.value
    if (currentState is ChatUiState.Success) {
      viewModelScope.launch {
        val message = Message(senderID = userID, message = message)
        currentState.chat.sendMessage(message)
      }
      _messageText.value = ""
    }
  }

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
    val cachedName = _userNameCache[senderID]
    if (cachedName != null) {
      return MutableStateFlow(cachedName).asStateFlow()
    }

    val flow = MutableStateFlow("...")
    loadUserName(senderID, flow)
    return flow.asStateFlow()
  }

  /**
   * Loads the username for the given [userID] and updates the provided [flow] with the result. Uses
   * a [Mutex] and the cache to ensure the network fetch is only attempted once.
   *
   * @param userID The ID of the user whose name is to be loaded.
   * @param flow The [MutableStateFlow] that will be updated with the loaded username.
   */
  private fun loadUserName(userID: String, flow: MutableStateFlow<String>) {
    viewModelScope.launch {
      _loadingMutex.withLock {
        if (_userNameCache.containsKey(userID)) {
          flow.value = _userNameCache.getValue(userID)
          return@launch
        }

        val name =
            try {
              userRepository.getUser(userID).username
            } catch (_: NoSuchElementException) {
              "deleted"
            } catch (_: FirebaseFirestoreException) {
              "..."
            }

        _userNameCache[userID] = name
        flow.value = name
      }
    }
  }
}

/**
 * Factory for creating instances of [ChatUIViewModel]. This factory is necessary because
 * [ChatUIViewModel] has constructor parameters ([chatID] and [userID]) that are not empty, and the
 * ViewModel framework needs a way to instantiate it.
 *
 * @property chatID The unique identifier for the chat.
 * @property userID The unique identifier for the current user.
 */
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
