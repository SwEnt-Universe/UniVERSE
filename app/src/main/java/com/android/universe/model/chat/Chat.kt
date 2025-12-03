package com.android.universe.model.chat

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf

/**
 * Represents a single chat conversation.
 *
 * This class manages the state of a chat, including its messages, and handles interactions with the
 * underlying data source through a [ChatRepository]. It listens for real-time updates (additions,
 * modifications, deletions) to messages within the chat.
 *
 * @param chatID The unique identifier for this chat.
 * @param admin The identifier for the administrator or creator of the chat.
 * @param initialLastMessage An optional initial value for the last message in the chat, which can
 *   be used for display purposes before the full message list is loaded.
 */
class Chat(
    val chatID: String = "",
    val admin: String = "",
    initialLastMessage: Message? = null,
    private val chatRepository: ChatRepository = ChatRepositoryProvider.chatRepository
) {

  private val _messages = mutableStateListOf<Message>()
  private val _lastMessage = mutableStateOf(initialLastMessage)

  init {
    chatRepository.setMessageListener(
        chatID,
        onMessageAdded = { onMessageAdded(it) },
        onMessageUpdated = { onMessageUpdated(it) },
        onMessageDeleted = { onMessageDeleted(it) })
    chatRepository.setLastMessageListener(
        chatID, onLastMessageUpdated = { onLastMessageUpdated(it) })
  }

  /**
   * A read-only, observable list of [Message] objects in this chat.
   *
   * This list is backed by a private mutable state list (`_messages`) and is updated in real-time
   * as messages are added, updated, or deleted from the underlying data source. Changes to this
   * list will automatically trigger recomposition in Jetpack Compose UI components that observe it.
   */
  val messages: List<Message>
    get() = _messages

  /**
   * Represents the most recent message in the chat conversation.
   *
   * This property holds a reference to the last `Message` object added to the chat. It is
   * initialized with `initialLastMessage` and subsequently updated by the `onMessageAdded` function
   * whenever a new message arrives. This is useful for displaying a preview of the chat's latest
   * activity, such as in a chat list.
   */
  val lastMessage: State<Message?>
    get() = _lastMessage

  /**
   * Handles the addition of a new message to the chat.
   *
   * This function is called when the underlying data source signals that a new message has been
   * added. It appends the new message to the internal `_messages` list, which triggers UI updates
   * for observers of the public `messages` property. It also updates the `lastMessage` reference to
   * reflect the most recent message in the conversation.
   *
   * @param message The new [Message] object that was added.
   */
  private fun onMessageAdded(message: Message) {
    _messages.add(0, message)
    _lastMessage.value = message
  }

  /**
   * Handles the update of an existing message in the chat.
   *
   * This function is called when a message's data has been modified in the underlying data source.
   * It finds the message in the local `_messages` list by its `messageID` and replaces it with the
   * updated [Message] object. If the message is not found in the list, no action is taken.
   *
   * @param message The updated [Message] object containing the new data.
   */
  private fun onMessageUpdated(message: Message) {
    val index = _messages.indexOfFirst { it.messageID == message.messageID }
    if (index != -1) _messages[index] = message
  }

  /**
   * Handles the deletion of a message from the local state.
   *
   * This function is called when a message deletion event is received from the data source (via the
   * `setMessageListener`). It removes the specified [Message] object from the internal `_messages`
   * list, which in turn updates the public `messages` property and triggers UI recomposition.
   *
   * @param message The [Message] object that has been deleted and should be removed.
   */
  private fun onMessageDeleted(message: Message) {
    val index = _messages.indexOfFirst { it.messageID == message.messageID }
    if (index != -1) _messages.removeAt(index)
  }

  /**
   * Updates the state of the last message in the chat.
   *
   * This function is invoked by a listener (`setLastMessageListener`) that specifically monitors
   * changes to the chat's last message in the repository. It updates the local `_lastMessage` state
   * with the new message, but only if the incoming message is different from the current one. This
   * helps optimize UI recompositions by avoiding unnecessary state changes.
   *
   * @param message The latest [Message] object from the data source.
   */
  private fun onLastMessageUpdated(message: Message) {
    if (_lastMessage.value != message) _lastMessage.value = message
  }

  /**
   * Asynchronously sends a new message to this chat.
   *
   * This function delegates the sending operation to the `ChatRepository`, which handles the
   * underlying data persistence and network communication.
   *
   * @param message The [Message] object to be sent.
   */
  suspend fun sendMessage(message: Message) {
    chatRepository.sendMessage(chatID, message)
  }

  /**
   * Removes the real-time listeners for message updates from the underlying data source.
   *
   * This function should be called when the chat is no longer being observed.
   */
  fun clearListeners() {
    chatRepository.removeMessageListener(chatID)
    chatRepository.removeLastMessageListener(chatID)
  }
}
