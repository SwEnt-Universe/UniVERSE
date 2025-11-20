package com.android.universe.utils

import android.util.Log
import androidx.test.core.app.ApplicationProvider
import com.android.universe.model.chat.COLLECTION_NAME
import com.android.universe.model.chat.ChatRepository
import com.android.universe.model.chat.FirestoreChatRepository
import com.google.firebase.FirebaseApp
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before

open class FirestoreChatTest(private val isRobolectric: Boolean = true) {
  val emulator = FirebaseEmulator

  fun createInitializedRepository(): ChatRepository {
    return FirestoreChatRepository(db = emulator.firestore)
  }

  suspend fun getDocumentCount(): Int {
    return emulator.firestore.collection(COLLECTION_NAME).get().await().size()
  }

  private suspend fun clearChatsCollection() {
    val chats = emulator.firestore.collection(COLLECTION_NAME).get().await()
    val batch = emulator.firestore.batch()
    for (chatDoc in chats.documents) {

      // Delete messages subcollection first
      val messagesRef = chatDoc.reference.collection("messages")
      val messages = messagesRef.get().await()
      for (msg in messages.documents) {
        batch.delete(msg.reference)
      }

      // Delete the chat document itself
      batch.delete(chatDoc.reference)
    }

    batch.commit().await()

    // ✅ verify clean
    val count = emulator.firestore.collection(COLLECTION_NAME).get().await().size()
    assert(count == 0) { "Chats not fully cleared, remaining count: $count" }
  }

  @Before
  open fun setUp() {
    FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    emulator.connect(isRobolectric)
    val url = URL("http://10.0.2.2:8080") // Firestore emulator host for Android
    val connection = url.openConnection() as HttpURLConnection
    connection.connectTimeout = 2000
    connection.requestMethod = "GET"
    runTest {
      val count = getDocumentCount()
      if (count > 0) {
        Log.w(
            "FirestoreChatTest", "Warning: Chat collection not empty at test start, count: $count")
        clearChatsCollection()
      }
    }
  }

  @After
  open fun tearDown() {
    runTest { clearChatsCollection() }
  }
}
