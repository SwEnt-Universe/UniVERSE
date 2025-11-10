package com.android.universe.model.chat

interface ChatRepository {
  suspend fun loadChat(chatID: String): Chat

  suspend fun sendMessage(chatID: String, message: Message)

  fun listenForMessages(chatID: String, onMessagesUpdated: (List<Message>) -> Unit)

  suspend fun createChat(chatID: String, admin: String): Chat

  suspend fun addMember(chatID: String, userId: String)

  suspend fun removeMember(chatID: String, userId: String)
}
