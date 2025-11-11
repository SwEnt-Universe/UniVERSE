package com.android.universe.model.chat

import java.util.concurrent.ConcurrentHashMap

/**
 * Manages chat-related operations, acting as an in-memory cache and a bridge to the data layer.
 *
 * This singleton object is responsible for loading, creating, and caching [Chat] instances. It
 * ensures that chat data is fetched from the [ChatRepository] only when not available in the local
 * cache, improving performance and reducing network requests.
 */
object ChatManager {
  private val chats = ConcurrentHashMap<String, Chat>()
  private val chatRepository = ChatRepositoryProvider.chatRepository

  /**
   * Loads a chat by its ID.
   *
   * This function first checks for a cached version of the chat in memory. If a cached version is
   * found, it is returned immediately. Otherwise, it fetches the chat from the [chatRepository],
   * caches it for future use, and then returns it. This function is a suspend function, as it may
   * perform a network or database operation.
   *
   * @param chatID The unique identifier of the chat to load.
   * @return The [Chat] object corresponding to the given ID.
   */
  suspend fun loadChat(chatID: String): Chat {
    chats[chatID]?.let {
      return it
    }
    val chat = chatRepository.loadChat(chatID)
    chats[chatID] = chat
    return chat
  }

  /**
   * Creates a new chat with the specified ID and initial administrator.
   *
   * This function communicates with the chat repository to create the chat persistence and then
   * caches the newly created chat object in memory for quick access.
   *
   * If the chatID is already in use this function will overwrite the current chat metadata,
   *
   * @param chatID The unique identifier for the new chat.
   * @param admin The user ID of the person who is creating the chat and will be its initial admin.
   * @return The newly created [Chat] object.
   */
  suspend fun createChat(chatID: String, admin: String): Chat {
    val chat = chatRepository.createChat(chatID, admin)
    chats[chatID] = chat
    return chat
  }
}
