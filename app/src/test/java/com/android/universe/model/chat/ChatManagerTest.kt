package com.android.universe.model.chat

import com.android.universe.model.chat.Utils.getNewSampleChat
import com.android.universe.model.chat.Utils.getNewSampleChatID
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
    mockRepository = mockk(relaxed = true)
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
    val chatID = getNewSampleChatID()
    val expectedChat = getNewSampleChat(chatID, mockRepository)

    coEvery { mockRepository.loadChat(chatID) } returns expectedChat

    val result = ChatManager.loadChat(chatID)

    coVerify(exactly = 1) { mockRepository.loadChat(chatID) }
    assertEquals(expectedChat, result)
  }

  @Test
  fun `loadChat returns cached chat without calling repository`() = runBlocking {
    val chatID = getNewSampleChatID()
    val cachedChat = getNewSampleChat(chatID, mockRepository)

    // Prime the cache
    coEvery { mockRepository.createChat(chatID, cachedChat.admin) } returns cachedChat
    ChatManager.createChat(chatID, cachedChat.admin)

    // Repository returns something else if called
    coEvery { mockRepository.loadChat(chatID) } returns getNewSampleChat(chatID, mockRepository)

    val result = ChatManager.loadChat(chatID)

    // Repository should NOT be called because cached
    coVerify(exactly = 0) { mockRepository.loadChat(chatID) }
    assertEquals(cachedChat.admin, result.admin)
  }

  @Test
  fun `createChat calls repository and caches chat`() = runBlocking {
    val chatID = getNewSampleChatID()
    val createdChat = getNewSampleChat(chatID, mockRepository)

    coEvery { mockRepository.createChat(chatID, createdChat.admin) } returns createdChat

    val result = ChatManager.createChat(chatID, createdChat.admin)

    coVerify(exactly = 1) { mockRepository.createChat(chatID, createdChat.admin) }
    assertEquals(createdChat, result)

    val cached = ChatManager.loadChat(chatID)
    assertEquals(createdChat, cached)
  }
}
