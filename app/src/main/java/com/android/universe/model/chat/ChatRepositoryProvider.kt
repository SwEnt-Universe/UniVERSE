package com.android.universe.model.chat

/**
 * A singleton object that provides access to the chat repository.
 *
 * This provider ensures that the same instance of [ChatRepository] is used throughout the
 * application, following the singleton pattern. It abstracts the specific implementation of the
 * repository, allowing for easier testing and maintenance.
 */
object ChatRepositoryProvider {
  /**
   * The singleton instance of [ChatRepository] used throughout the application. This is the main
   * entry point for interacting with chat data.
   */
  val chatRepository: ChatRepository = FirestoreChatRepository()
}
