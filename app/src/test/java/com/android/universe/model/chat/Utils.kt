package com.android.universe.model.chat

object Utils {
  private var sampleNumber = 1

  /**
   * Returns a new sample chat ID. The sample chat ID is a string that is unique for each call. The
   * format is "chat" followed by a number that increments on each call. For example: "chat1",
   * "chat2", "chat3", etc.
   *
   * @return A new sample chat ID.
   */
  fun getNewSampleChatID(): String = "chat${sampleNumber++}"

  /**
   * Creates a new sample [Chat] instance.
   *
   * @param chatID The unique identifier for the chat.
   * @param repository The repository to be associated with the chat.
   * @return A new [Chat] object with a generated admin name based on the chatID.
   */
  fun getNewSampleChat(chatID: String, repository: ChatRepository): Chat {
    val adminName = "user-$chatID"
    return Chat(chatID = chatID, admin = adminName, chatRepository = repository)
  }
}
