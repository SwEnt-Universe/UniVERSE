package com.android.universe.model.chat

import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

private const val COLLECTION_NAME = "chats"

class FirestoreChatRepository(private val db: FirebaseFirestore = Firebase.firestore) :
    ChatRepository {
  private val listeners = mutableMapOf<String, ListenerRegistration>()

  override suspend fun loadChat(chatID: String): Chat {
    val doc = db.collection(COLLECTION_NAME).document(chatID).get().await()
    return doc.toObject(Chat::class.java)?.copy(chatID = doc.id)
        ?: throw Exception("Chat not found")
  }

  override suspend fun sendMessage(chatID: String, message: Message) {
    val batch = db.batch()

    // Reference for new message
    val messageRef =
        db.collection(COLLECTION_NAME)
            .document(chatID)
            .collection("messages")
            .document() // generate a new ID

    // Add the message
    batch.set(messageRef, message)

    // Update lastUpdated on the chat document
    val chatRef = db.collection(COLLECTION_NAME).document(chatID)
    batch.update(chatRef, "lastUpdated", message.timestamp)

    // Commit batch
    batch.commit().await()
  }

  override fun listenForMessages(chatID: String, onMessagesUpdated: (List<Message>) -> Unit) {
    // Remove previous listener if exists
    listeners[chatID]?.remove()

    val listener =
        db.collection(COLLECTION_NAME)
            .document(chatID)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshots, e ->
              if (e != null || snapshots == null) return@addSnapshotListener
              val messages = snapshots.documents.mapNotNull { it.toObject(Message::class.java) }
              onMessagesUpdated(messages)
            }

    listeners[chatID] = listener
  }

  override suspend fun createChat(chatID: String, admin: String): Chat {
    val chat =
        Chat(
            chatID = chatID,
            admin = admin,
            members = mutableListOf(admin),
            messages = mutableListOf(),
            lastMessage = null)
    val docRef = db.collection(COLLECTION_NAME).add(chatID).await()
    return chat.copy(chatID = docRef.id)
  }

  override suspend fun addMember(chatID: String, userId: String) {
    db.collection(COLLECTION_NAME)
        .document(chatID)
        .update("members", com.google.firebase.firestore.FieldValue.arrayUnion(userId))
        .await()
  }

  override suspend fun removeMember(chatID: String, userId: String) {
    db.collection(COLLECTION_NAME)
        .document(chatID)
        .update("members", com.google.firebase.firestore.FieldValue.arrayRemove(userId))
        .await()
  }
}
