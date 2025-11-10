package com.android.universe.model.chat

object ChatManager {
  private val chats = mutableMapOf<String, Chat>()
  private val chatRepository = ChatRepositoryProvider.chatRepository

  suspend fun loadChat(chatID: String): Chat {
    chats[chatID]?.let {
      return it
    }
    val chat = chatRepository.loadChat(chatID)
    chats[chatID] = chat
    return chat
  }

  suspend fun createChat(chatID: String, admin: String): Chat {
    val chat = chatRepository.createChat(chatID, admin)
    chats[chatID] = chat
    return chat
  }
}
