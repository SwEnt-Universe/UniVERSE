package com.android.universe.model.chat

import com.android.universe.di.DefaultDP
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

const val COLLECTION_NAME = "chats"

/**
 * An implementation of [ChatRepository] that uses Google's Cloud Firestore as the data source. This
 * class handles all Firestore-specific operations for chat-related data, including creating chats,
 * sending messages, and listening for real-time message updates.
 *
 * @property db An instance of [FirebaseFirestore] used to interact with the database. Defaults to
 *   the default `Firebase.firestore` instance.
 */
class FirestoreChatRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) :
    ChatRepository {
  private val listeners = ConcurrentHashMap<String, ListenerRegistration>()

  /**
   * Asynchronously loads a chat from the Firestore database.
   *
   * @param chatID The unique identifier of the chat to load.
   * @return The [Chat] object corresponding to the given ID.
   * @throws NoSuchElementException if the chat with the specified ID is not found.
   */
  override suspend fun loadChat(chatID: String): Chat {
    val doc = withContext(DefaultDP.io) {db.collection(COLLECTION_NAME).document(chatID).get().await()}
    return doc.toObject(ChatDTO::class.java)?.toChat()
        ?: throw NoSuchElementException("Chat not found")
  }

  /**
   * Sends a message to a specific chat.
   *
   * This function generates a new unique ID for the message, updates the message object with this
   * ID, and then saves it to the "messages" sub-collection of the specified chat document in
   * Firestore.
   *
   * @param chatID The ID of the chat to send the message to.
   * @param message The [Message] object to be sent. The `messageID` field will be ignored and
   *   overwritten with a newly generated ID.
   */
  override suspend fun sendMessage(chatID: String, message: Message) {
    val chatRef = db.collection(COLLECTION_NAME).document(chatID)
    val messageRef = chatRef.collection("messages").document() // generate new message ID
    val msg = message.copy(messageID = messageRef.id)

    val batch = db.batch()

    // Add the message write
    batch.set(messageRef, msg)

    // Add the lastMessage update
    batch.update(chatRef, "lastMessage", msg)

    // Commit the batch
    withContext(DefaultDP.io) {batch.commit().await()}
  }

  /**
   * Sets up a real-time listener for messages in a specific chat.
   *
   * This function attaches a snapshot listener to the "messages" sub-collection of a given chat
   * document in Firestore. The listener will be triggered whenever a message is added, modified, or
   * removed. It automatically handles removing any previous listener for the same `chatID` to
   * prevent duplicate listeners and memory leaks. The messages are ordered by their timestamp.
   *
   * @param chatID The unique identifier of the chat to listen to.
   * @param onMessageAdded A callback function that is invoked when a new message is added. It
   *   receives the new [Message] object.
   * @param onMessageUpdated A callback function that is invoked when an existing message is
   *   modified. It receives the updated [Message] object.
   * @param onMessageDeleted A callback function that is invoked when a message is removed. It
   *   receives the deleted [Message] object.
   */
  override fun setMessageListener(
      chatID: String,
      onMessageAdded: (Message) -> Unit,
      onMessageUpdated: (Message) -> Unit,
      onMessageDeleted: (Message) -> Unit
  ) {

    // Remove previous listener if exists
    listeners[chatID]?.remove()
    listeners[chatID] =
        db.collection(COLLECTION_NAME)
            .document(chatID)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshots, e ->
              if (e != null || snapshots == null) return@addSnapshotListener
              for (change in snapshots.documentChanges) {
                handleDocumentChange(change, onMessageAdded, onMessageUpdated, onMessageDeleted)
              }
            }
  }

  /**
   * Removes the real-time message listener for a specific chat.
   *
   * This function finds the active [ListenerRegistration] associated with the given `chatID` and
   * detaches it from the Firestore database. This stops the app from receiving further real-time
   * updates for that chat, which is crucial for managing resources and preventing memory leaks when
   * a chat view is no longer active.
   *
   * @param chatID The unique identifier of the chat whose listener should be removed.
   */
  override fun removeMessageListener(chatID: String) {
    listeners.remove(chatID)?.remove()
  }

  /**
   * Creates a new chat document in the Firestore database with initial values.
   *
   * This function creates a new document in the "chats" collection using the provided `chatID`. It
   * sets the initial fields for the chat, including the `chatID`, the `admin`'s user ID, and sets
   * the `lastMessage` to null.
   *
   * @param chatID The unique identifier for the new chat. This will be used as the document ID.
   * @param admin The user ID of the person creating the chat, who will be designated as the admin.
   * @return A [Chat] object representing the newly created chat.
   */
  override suspend fun createChat(chatID: String, admin: String): Chat {
    val chat = ChatDTO(chatID = chatID, admin = admin, lastMessage = null)
    withContext(DefaultDP.io) {db.collection(COLLECTION_NAME).document(chatID).set(chat).await()}
    return Chat(chatID, admin)
  }

  /**
   * A Data Transfer Object (DTO) for representing a chat as stored in Firestore. This class is used
   * for serialization and deserialization of chat data from Firestore documents. It includes all
   * potential fields that might be present in the document.
   *
   * The [toChat] method is provided to convert this DTO into a domain-specific [Chat] object, which
   * is used within the application's business logic. This separation ensures that the data layer's
   * representation is decoupled from the domain model.
   *
   * @property chatID The unique identifier for the chat.
   * @property admin The ID of the user who is the administrator of the chat.
   * @property messages A list of messages within the chat. This is typically handled as a
   *   sub-collection in Firestore and might be null in the main chat document DTO.
   * @property lastMessage The most recent message sent in the chat, used for display in chat lists.
   */
  data class ChatDTO(
      val chatID: String = "",
      val admin: String = "",
      val lastMessage: Message? = null
  ) {
    fun toChat(): Chat {
      return Chat(chatID = chatID, admin = admin, initialLastMessage = lastMessage)
    }
  }

  /**
   * Processes a single [DocumentChange] from a Firestore snapshot listener.
   *
   * This function is called for each document change within the snapshot. It deserializes the
   * document data into a [Message] object and then invokes the appropriate callback
   * (`onMessageAdded`, `onMessageUpdated`, or `onMessageDeleted`) based on the type of change
   * (ADDED, MODIFIED, or REMOVED).
   *
   * @param change The [DocumentChange] object representing the change in the Firestore collection.
   * @param onMessageAdded Callback to execute when a document is added.
   * @param onMessageUpdated Callback to execute when a document is modified.
   * @param onMessageDeleted Callback to execute when a document is removed.
   */
  private fun handleDocumentChange(
      change: DocumentChange,
      onMessageAdded: (Message) -> Unit,
      onMessageUpdated: (Message) -> Unit,
      onMessageDeleted: (Message) -> Unit
  ) {
    val msg = change.document.toObject(Message::class.java)
    when (change.type) {
      DocumentChange.Type.ADDED -> onMessageAdded(msg)
      DocumentChange.Type.MODIFIED -> onMessageUpdated(msg)
      DocumentChange.Type.REMOVED -> onMessageDeleted(msg)
    }
  }
}
