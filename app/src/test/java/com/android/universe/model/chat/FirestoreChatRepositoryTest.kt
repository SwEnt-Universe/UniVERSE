package com.android.universe.model.chat

import android.os.Looper
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.model.chat.Utils.getNewSampleChatID
import com.android.universe.model.chat.Utils.getNewSampleMessage
import com.android.universe.utils.FirestoreChatTest
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf

@RunWith(AndroidJUnit4::class)
class FirestoreChatRepositoryTest : FirestoreChatTest() {

  private lateinit var chatRepository: ChatRepository

  @Before
  override fun setUp() = runTest {
    super.setUp()
    chatRepository = FirestoreChatRepository(db = emulator.firestore)
  }

  @Test
  fun `createChat stores chat in Firestore`() = runTest {
    val chatID = getNewSampleChatID()
    val admin = "adminUser"

    val chat = chatRepository.createChat(chatID, admin)

    assertEquals(chatID, chat.chatID)
    assertEquals(admin, chat.admin)

    // Verify it exists in Firestore
    val snapshot = emulator.firestore.collection(COLLECTION_NAME).document(chatID).get().await()

    assertTrue(snapshot.exists())
    assertEquals(admin, snapshot.getString("admin"))
  }

  @Test
  fun `loadChat returns stored chat`() = runTest {
    val chatID = getNewSampleChatID()
    val admin = "adminUser2"

    chatRepository.createChat(chatID, admin)

    val loaded = chatRepository.loadChat(chatID)

    assertEquals(chatID, loaded.chatID)
    assertEquals(admin, loaded.admin)
  }

  @Test
  fun `sendMessage adds a message to chat subcollection`() = runTest {
    val chatID = getNewSampleChatID()
    val admin = "adminUser3"

    chatRepository.createChat(chatID, admin)

    val msg = Message(senderID = "sender", message = "Hello!")

    chatRepository.sendMessage(chatID, msg)

    val messages =
        emulator.firestore
            .collection(COLLECTION_NAME)
            .document(chatID)
            .collection("messages")
            .get()
            .await()

    assertEquals(1, messages.size())

    val stored = messages.documents.first().toObject(Message::class.java)
    assertNotNull(stored)
    assertEquals("Hello!", stored!!.message)
    assertEquals("sender", stored.senderID)
  }

  @Test
  fun `setMessageListener receives added, updated, and removed events`() = runBlocking {
    val chatID = getNewSampleChatID()
    val admin = "adminUser"

    chatRepository.createChat(chatID, admin)

    val addedDeferred = CompletableDeferred<Message>()
    val updatedDeferred = CompletableDeferred<Message>()
    val deletedDeferred = CompletableDeferred<Message>()

    chatRepository.setMessageListener(
        chatID,
        onMessageAdded = { addedDeferred.complete(it) },
        onMessageUpdated = { updatedDeferred.complete(it) },
        onMessageDeleted = { deletedDeferred.complete(it) })

    val messageRef =
        emulator.firestore
            .collection(COLLECTION_NAME)
            .document(chatID)
            .collection("messages")
            .document()
    val message = Message(messageID = messageRef.id, senderID = "user1", message = "Hello")
    messageRef.set(message).await()

    shadowOf(Looper.getMainLooper()).idle()
    val addedMessage = withTimeout(5000) { addedDeferred.await() }
    assertEquals("Hello", addedMessage.message)

    val updatedMessageData = message.copy(message = "Updated text")
    messageRef.set(updatedMessageData).await()

    shadowOf(Looper.getMainLooper()).idle()
    val updatedMessage = withTimeout(5000) { updatedDeferred.await() }
    assertEquals("Updated text", updatedMessage.message)

    messageRef.delete().await()
    shadowOf(Looper.getMainLooper()).idle()
    val deletedMessage = withTimeout(5000) { deletedDeferred.await() }
    assertEquals("Updated text", deletedMessage.message)

    chatRepository.removeMessageListener(chatID)
  }

  @Test
  fun `loadChat throws NoSuchElementException for non-existent chat`() = runTest {
    val chatID = getNewSampleChatID()
    try {
      chatRepository.loadChat(chatID)
      fail("Expected NoSuchElementException")
    } catch (e: NoSuchElementException) {
      assertEquals("Chat not found", e.message)
    }
  }

  @Test
  fun `setLastMessageListener receives updates when lastMessage field changes`() = runBlocking {
    val chatID = getNewSampleChatID()
    val admin = "adminUser"
    val message = getNewSampleMessage()

    chatRepository.createChat(chatID, admin)

    var lastMessage: Message? = null

    chatRepository.setLastMessageListener(chatID, onLastMessageUpdated = { lastMessage = it })

    shadowOf(Looper.getMainLooper()).idle()
    chatRepository.sendMessage(chatID, message)
    shadowOf(Looper.getMainLooper()).idle()

    assertNotNull(lastMessage)
    assertEquals(message.message, lastMessage?.message)
    assertEquals(message.senderID, lastMessage?.senderID)

    chatRepository.removeLastMessageListener(chatID)
  }

  @Test
  fun `removeLastMessageListener stops receiving lastMessage updates`() = runBlocking {
    val chatID = getNewSampleChatID()
    val admin = "adminUserForRemoval"
    val initialMessage = Message(senderID = "system", message = "Chat created")
    val firstUpdate = Message(senderID = "user5", message = "Update 1")
    val secondUpdate = Message(senderID = "user5", message = "Update 2 - Should be ignored")

    // 1. Manually create the chat with an initial lastMessage field
    emulator.firestore
        .collection(COLLECTION_NAME)
        .document(chatID)
        .set(mapOf("admin" to admin, "lastMessage" to initialMessage))
        .await()

    val receivedUpdates = mutableListOf<Message>()

    // 2. Set the listener
    chatRepository.setLastMessageListener(
        chatID, onLastMessageUpdated = { receivedUpdates.add(it) })

    // 3. First update: should be received
    emulator.firestore
        .collection(COLLECTION_NAME)
        .document(chatID)
        .update("lastMessage", firstUpdate)
        .await()
    shadowOf(Looper.getMainLooper()).idle()

    // Give it a short moment to ensure the first update is processed
    withTimeout(1000) {
      while (receivedUpdates.size <
          2) { // Should receive initial (once the listener is set) + first update
        kotlinx.coroutines.delay(100)
      }
    }

    // 4. Remove the listener
    chatRepository.removeLastMessageListener(chatID)

    // 5. Second update: should NOT be received
    emulator.firestore
        .collection(COLLECTION_NAME)
        .document(chatID)
        .update("lastMessage", secondUpdate)
        .await()
    shadowOf(Looper.getMainLooper()).idle() // Process looper again

    // Wait a short duration to be sure no unexpected update came through
    kotlinx.coroutines.delay(500)

    // Initial state + First update = 2 calls
    assertEquals(2, receivedUpdates.size)

    // Verify the *last* received message is the first update
    assertEquals(firstUpdate.message, receivedUpdates.last().message)
  }
}
