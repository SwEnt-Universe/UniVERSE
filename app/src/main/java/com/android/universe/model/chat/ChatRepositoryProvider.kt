package com.android.universe.model.chat

object ChatRepositoryProvider {
  val chatRepository: ChatRepository = FirestoreChatRepository()
}
