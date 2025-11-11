package com.android.universe.model.chat

interface ChatRepository {
  /**
   * Asynchronously loads a chat object from the repository.
   *
   * This function retrieves all the data associated with a specific chat, such as its members,
   * messages, and metadata.
   *
   * @param chatID The unique identifier of the chat to load.
   * @return The [Chat] object corresponding to the given ID.
   * @throws NoSuchElementException if no chat with the given ID is found.
   */
  suspend fun loadChat(chatID: String): Chat

  /**
   * Sends a message to a specific chat.
   *
   * This is a one-off operation to push a new message to the chat's history. For real-time updates,
   * use [setMessageListener].
   *
   * @param chatID The unique identifier of the chat to send the message to.
   * @param message The [Message] object to be sent. The `messageID` field will be ignored and a new
   *   one will be generated.
   */
  suspend fun sendMessage(chatID: String, message: Message)

  /**
   * Listens for real-time updates to messages in a specific chat.
   *
   * This function attaches listeners to a chat conversation to handle new, updated, or deleted
   * messages as they occur. It provides callbacks to process these events.
   *
   * @param chatID The unique identifier of the chat to listen to.
   * @param onMessageAdded A callback function that is invoked when a new message is added. It
   *   receives the newly added [Message] as a parameter.
   * @param onMessageUpdated A callback function that is invoked when an existing message is
   *   modified. It receives the updated [Message] as a parameter.
   * @param onMessageDeleted A callback function that is invoked when a message is deleted. It
   *   receives the deleted [Message] as a parameter.
   */
  fun setMessageListener(
      chatID: String,
      onMessageAdded: (Message) -> Unit,
      onMessageUpdated: (Message) -> Unit,
      onMessageDeleted: (Message) -> Unit
  )

  /**
   * Removes the real-time message listeners from a specific chat.
   *
   * This function should be called to detach the listeners previously set up with
   * [setMessageListener] to stop receiving updates and prevent memory leaks. This is typically done
   * when the user navigates away from the chat screen.
   *
   * @param chatID The unique identifier of the chat to stop listening to.
   */
  fun removeMessageListener(chatID: String)

  /**
   * Creates a new chat.
   *
   * This function is responsible for creating a new chat entity in the repository. It requires a
   * unique identifier for the chat and the ID of the user who will be the administrator.
   *
   * @param chatID The unique identifier for the new chat.
   * @param admin The user ID of the person creating the chat, who will be designated as the admin.
   * @return The newly created [Chat] object.
   * @throws Exception if the chat creation fails (e.g., if a chat with the same ID already exists).
   */
  suspend fun createChat(chatID: String, admin: String): Chat
}
