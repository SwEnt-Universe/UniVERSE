package com.android.universe.model.chat

import androidx.annotation.VisibleForTesting

/**
 * A singleton object that provides access to the chat repository.
 *
 * This provider ensures that the same instance of [ChatRepository] is used throughout the
 * application, following the singleton pattern. It abstracts the specific implementation of the
 * repository, allowing for easier testing and maintenance.
 */
object ChatRepositoryProvider {

  private val _chatRepository: ChatRepository by lazy { FirestoreChatRepository() }
  private var testChatRepository: ChatRepository? = null

  /**
   * The singleton instance of [ChatRepository] used throughout the application. This is the main
   * entry point for interacting with chat data.
   */
  val chatRepository: ChatRepository
    get() = testChatRepository ?: _chatRepository

  /**
   * Allows tests to replace the repository with a mock. Marked internal so production code outside
   * the module cannot access it.
   */
  @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
  internal fun setTestChatRepository(repository: ChatRepository) {
    testChatRepository = repository
  }
}
