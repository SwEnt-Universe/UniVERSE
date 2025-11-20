package com.android.universe.model.chat

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.model.chat.Utils.getNewSampleChat
import com.android.universe.model.chat.Utils.getNewSampleChatID
import com.android.universe.model.chat.Utils.getNewSampleMessage
import io.mockk.*
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class ChatTest {
  private lateinit var mockRepository: ChatRepository
  private val message1 = Message("msg1", "sender1", "content1")
  private val message1Updated = Message("msg1", "sender2", "content2")
  private val message3 = Message("msg3", "sender3", "content3")

  @Before
  fun setup() {
    mockRepository = mockk<ChatRepository>(relaxed = true)
    every { mockRepository.setMessageListener(any(), any(), any(), any()) } returns Unit
    every { mockRepository.setLastMessageListener(any(), any()) } returns Unit
    every { mockRepository.removeMessageListener(any()) } returns Unit
  }

  @After
  fun tearDown() {
    unmockkAll()
  }

  @Test
  fun `initialization sets message listener`() {
    val chatID = getNewSampleChatID()
    getNewSampleChat(chatID, mockRepository)
    verify {
      mockRepository.setMessageListener(
          chatID, onMessageAdded = any(), onMessageUpdated = any(), onMessageDeleted = any())
    }
  }

  @Test
  fun `onMessageAdded updates messages and lastMessage`() {
    val addedSlot = slot<(Message) -> Unit>()
    val chatID = getNewSampleChatID()
    every { mockRepository.setMessageListener(chatID, capture(addedSlot), any(), any()) } returns
        Unit

    val chat = getNewSampleChat(chatID, mockRepository)

    addedSlot.captured.invoke(message1)

    assertEquals(1, chat.messages.size)
    assertEquals(message1, chat.messages[0])
    assertEquals(message1, chat.lastMessage.value)
  }

  @Test
  fun `onMessageUpdated updates existing message`() {
    val addedSlot = slot<(Message) -> Unit>()
    val updatedSlot = slot<(Message) -> Unit>()
    val chatID = getNewSampleChatID()
    every {
      mockRepository.setMessageListener(chatID, capture(addedSlot), capture(updatedSlot), any())
    } returns Unit
    val chat = getNewSampleChat(chatID, mockRepository)

    addedSlot.captured.invoke(message1)
    updatedSlot.captured.invoke(message1Updated)

    assertEquals(1, chat.messages.size)
    assertEquals(message1Updated.message, chat.messages[0].message)
  }

  @Test
  fun `onMessageDeleted removes message`() {
    val addedSlot = slot<(Message) -> Unit>()
    val deletedSlot = slot<(Message) -> Unit>()
    val chatID = getNewSampleChatID()
    every {
      mockRepository.setMessageListener(chatID, capture(addedSlot), any(), capture(deletedSlot))
    } returns Unit
    val chat = getNewSampleChat(chatID, mockRepository)

    addedSlot.captured.invoke(message3)
    deletedSlot.captured.invoke(message3)

    assertTrue(chat.messages.isEmpty())
  }

  @Test
  fun `sendMessage delegates to repository`() = runBlocking {
    val chatID = getNewSampleChatID()
    val chat = getNewSampleChat(chatID, mockRepository)
    coEvery { mockRepository.sendMessage(chat.chatID, message1) } returns Unit

    chat.sendMessage(message1)

    coVerify(exactly = 1) { mockRepository.sendMessage(chat.chatID, message1) }
  }

  @Test
  fun `clearListeners calls removeMessageListener`() {
    val chatID = getNewSampleChatID()
    val chat = getNewSampleChat(chatID, mockRepository)
    chat.clearListeners()

    verify { mockRepository.removeMessageListener(chat.chatID) }
  }

  @Test
  fun `onLastMessageUpdated updates lastMessage state and avoids unnecessary updates`() {
    val lastMessageSlot = slot<(Message) -> Unit>()
    val chatID = getNewSampleChatID()
    val initialMessage = getNewSampleMessage()
    val newMessage = getNewSampleMessage()

    every { mockRepository.setLastMessageListener(chatID, capture(lastMessageSlot)) } returns Unit

    val chat = getNewSampleChat(chatID, mockRepository)

    // 3. Manually set the initial state (the repository listener fires the callback)
    lastMessageSlot.captured.invoke(initialMessage)
    assertEquals(initialMessage, chat.lastMessage.value)

    lastMessageSlot.captured.invoke(newMessage)
    assertEquals(newMessage, chat.lastMessage.value)

    lastMessageSlot.captured.invoke(newMessage)
    assertEquals(newMessage, chat.lastMessage.value)
  }
}
