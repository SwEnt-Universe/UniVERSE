package com.android.universe.ui.chat

import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.universe.model.chat.ChatManager
import com.android.universe.model.chat.Message
import com.android.universe.model.event.EventRepository
import com.android.universe.model.event.EventRepositoryProvider
import com.google.firebase.firestore.FirebaseFirestoreException
import java.time.LocalDateTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Represents a preview of a chat conversation, used for display in a list.
 *
 * This data class holds the essential information needed to show a chat entry, such as in a chat
 * list screen.
 *
 * @property chatName The display name of the chat, typically the event title.
 * @property chatID The unique identifier for the chat.
 * @property lastMessage A [State] holding the most recent [Message] in the chat, or null if there
 *   are no messages. Using [State] allows the UI to automatically update when a new message
 *   arrives.
 */
data class ChatPreview(val chatName: String, val chatID: String, val lastMessage: State<Message?>)

data class ChatListUiState(
    val chatPreviews: List<ChatPreview>,
    val isLoading: Boolean,
    val displayMessage: String?
)

/**
 * ViewModel for the chat list screen.
 *
 * This ViewModel is responsible for loading and managing the list of chat previews for the current
 * user. It fetches events the user is involved in, and for each event, it loads the corresponding
 * chat information to create a [ChatPreview].
 *
 * @param userID The ID of the current user.
 * @param eventRepository The repository to fetch event data from.
 */
class ChatListViewModel(
    private val userID: String,
    private val eventRepository: EventRepository = EventRepositoryProvider.repository,
) : ViewModel() {

  private val _uiState =
      MutableStateFlow(
          ChatListUiState(chatPreviews = emptyList(), isLoading = true, displayMessage = null))
  val uiState = _uiState.asStateFlow()

  init {
    loadEventChats()
  }

  /**
   * Loads all chats for the events the user is involved in.
   *
   * This function fetches all events the current user is a part of, then for each event, it
   * attempts to load the corresponding chat. If a chat exists, it's added to the `_chatPreviews`
   * list to be displayed. If a chat does not exist for a given event (a legacy case), a new chat is
   * created for that event. Network errors are logged.
   *
   * TODO: This logic should be updated once users have a direct list of their events/chats.
   * TODO: Chat creation for an event should be moved to the event creation logic.
   */
  private fun loadEventChats() {
    viewModelScope.launch {
      // Since users don't have a list of event they participate in, we filter the events.
      // TODO: Update this once users have a list of events.
      val events = eventRepository.getUserInvolvedEvents(userID)
      _uiState.update { it.copy(isLoading = false) }
      if (events.isEmpty()) {
        _uiState.update {
          it.copy(displayMessage = "Join some events to start chatting with others")
        }
      } else {
        val yesterday = LocalDateTime.now().minusDays(1)
        events.forEach { event ->
          if (event.date.isAfter(yesterday)) {
            try {
              val chat = ChatManager.loadChat(chatID = event.id)
              _uiState.update {
                it.copy(
                    chatPreviews =
                        uiState.value.chatPreviews +
                            ChatPreview(
                                chatName = event.title,
                                chatID = chat.chatID,
                                lastMessage = chat.lastMessage))
              }
            } catch (_: FirebaseFirestoreException) {
              _uiState.update { it.copy(displayMessage = "Please check your internet connection") }
            }
          }
        }
      }
    }
  }
}
