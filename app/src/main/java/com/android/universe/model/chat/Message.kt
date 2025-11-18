package com.android.universe.model.chat

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Represents a single chat message within the application.
 *
 * This data class models the essential components of a message, including its unique identifier,
 * the sender's ID, the actual text content, and the time it was sent.
 *
 * @property messageID A unique identifier for the message.
 * @property senderID The unique identifier of the user who sent the message.
 * @property message The text content of the message.
 * @property timestamp The time when the message was sent, represented as a Firebase [Timestamp].
 *   Defaults to the current time.
 */
data class Message(
    val messageID: String = "",
    val senderID: String = "",
    val message: String = "",
    val timestamp: Timestamp = Timestamp.now()
) {
  val displayTime: String
    get() {
      val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
      return formatter.format(timestamp.toDate())
    }
}
