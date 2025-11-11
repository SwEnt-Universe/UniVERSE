package com.android.universe.model.chat

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class ChatManagerTest {

  private lateinit var mockRepository: ChatRepository

  @Before
  fun setup() {
    mockRepository = mockk<ChatRepository>(relaxed = true)
    ChatRepositoryProvider.setTestChatRepository(mockRepository)
    ChatManager.clear()
    every { mockRepository.setMessageListener(any(), any(), any(), any()) } returns Unit
    every { mockRepository.removeMessageListener(any()) } returns Unit
  }

  @After
  fun tearDown() {
    ChatManager.clear()
    unmockkAll()
  }

  @Test
  fun `loadChat returns chat from repository if not cached`() = runBlocking {
    val chatID = "chat1"
    val expectedChat = Chat(chatID, admin = "user1")

    coEvery { mockRepository.loadChat(chatID) } returns expectedChat

    val result = ChatManager.loadChat(chatID)

    coVerify(exactly = 1) { mockRepository.loadChat(chatID) }
    assertEquals(expectedChat, result)
  }

  @Test
  fun `loadChat returns cached chat without calling repository`() = runBlocking {
    val chatID = "chat2"
    val cachedChat = Chat(chatID, admin = "user2")

    // Prime the cache
    coEvery { mockRepository.createChat(chatID, "user2") } returns cachedChat
    ChatManager.createChat(chatID, "user2")

    // Repository returns something else if called
    coEvery { mockRepository.loadChat(chatID) } returns Chat(chatID, admin = "different")

    val result = ChatManager.loadChat(chatID)
    print(result)
    // Repository should NOT be called because cached
    coVerify(exactly = 0) { mockRepository.loadChat(chatID) }
    assertEquals("user2", result.admin)
  }

  @Test
  fun `createChat calls repository and caches chat`() = runBlocking {
    val chatID = "chat3"
    val admin = "adminUser"
    val createdChat = Chat(chatID, admin)

    coEvery { mockRepository.createChat(chatID, admin) } returns createdChat

    val result = ChatManager.createChat(chatID, admin)

    coVerify(exactly = 1) { mockRepository.createChat(chatID, admin) }
    assertEquals(createdChat, result)

    val cached = ChatManager.loadChat(chatID)
    assertEquals(createdChat, cached)
  }
}
