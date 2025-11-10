package com.android.universe.model.chat

data class Chat(
    val chatID: String = "",
    val admin: String = "",
    val members: MutableList<String> = mutableListOf(),
    val messages: MutableList<Message> = mutableListOf(),
    val lastMessage: Message? = null
) {
  private val chatRepository = ChatRepositoryProvider.chatRepository

  suspend fun sendMessage(message: Message) {
    messages.add(message)
    chatRepository.sendMessage(chatID, message)
  }
}
