package com.android.universe.model.chat

import androidx.annotation.Keep
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
@Keep
data class Message(
    val messageID: String = "",
    val senderID: String = "",
    val message: String = "",
    val timestamp: Timestamp = Timestamp.now()
) {

  /**
   * Formats the message's timestamp into a human-readable time string.
   *
   * This function takes the `timestamp` property of the message, which is a Firebase [Timestamp],
   * and converts it into a string representing the time in "HH:mm" (24-hour) format, respecting the
   * user's default locale for formatting conventions.
   *
   * @return A string representation of the time the message was sent, e.g., "14:32".
   */
  fun getDisplayTime(): String {
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    return formatter.format(timestamp.toDate())
  }
}
