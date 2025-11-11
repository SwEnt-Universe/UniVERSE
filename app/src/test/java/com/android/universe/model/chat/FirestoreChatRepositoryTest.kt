package com.android.universe.model.chat

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.utils.FirestoreChatTest
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

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
    val chatID = "testChat1"
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
    val chatID = "testChat2"
    val admin = "adminUser2"

    chatRepository.createChat(chatID, admin)

    val loaded = chatRepository.loadChat(chatID)

    assertEquals(chatID, loaded.chatID)
    assertEquals(admin, loaded.admin)
  }

  @Test
  fun `sendMessage adds a message to chat subcollection`() = runTest {
    val chatID = "testChat3"
    val admin = "adminUser3"

    chatRepository.createChat(chatID, admin)

    val msg = Message(messageID = "", senderID = "sender", message = "Hello!")

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
  /*
  @Test
  fun `setMessageListener receives added, updated, and removed events`() = runBlocking {
    val chatID = "testChat4"
    val admin = "adminUser4"

    // Create the chat document
    chatRepository.createChat(chatID, admin)

    // CompletableDeferred to capture listener events
    val addedDeferred = CompletableDeferred<Message>()
    val updatedDeferred = CompletableDeferred<Message>()
    val deletedDeferred = CompletableDeferred<Message>()

    // Attach listener
    chatRepository.setMessageListener(
      chatID,
      onMessageAdded = { addedDeferred.complete(it) },
      onMessageUpdated = { updatedDeferred.complete(it) },
      onMessageDeleted = { deletedDeferred.complete(it) }
    )

    // --- Add message ---
    val msgRef = emulator.firestore
      .collection(COLLECTION_NAME)
      .document(chatID)
      .collection("messages")
      .document()

    val message = Message(messageID = msgRef.id, senderID = "user", message = "Hello")
    msgRef.set(message).await()

    // Wait for listener to fire
    val addedMessage = withTimeout(5000) { addedDeferred.await() }
    assertEquals("Hello", addedMessage.message)

    // --- Update message ---
    val updatedMessage = addedMessage.copy(message = "Updated text")
    msgRef.set(updatedMessage).await()
    val updatedResult = withTimeout(5000) { updatedDeferred.await() }
    assertEquals("Updated text", updatedResult.message)

    // --- Delete message ---
    msgRef.delete().await()
    val deletedResult = withTimeout(5000) { deletedDeferred.await() }
    assertEquals("Updated text", deletedResult.message)
  }
  */
}
