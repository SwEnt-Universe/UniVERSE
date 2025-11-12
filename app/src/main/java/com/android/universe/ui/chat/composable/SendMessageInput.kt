package com.android.universe.ui.chat.composable

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.android.universe.model.chat.Chat
import com.android.universe.model.chat.Message
import kotlinx.coroutines.launch

const val MAX_MESSAGE_LENGTH = 256

object SendMessageInputTestTags {
  const val TEXT_FIELD = "SEND_MESSAGE_TEXT_FIELD"
  const val SEND_BUTTON = "SEND_BUTTON"
}

@Composable
fun SendMessageInput(chat: Chat, userID: String) {
  var messageText by remember { mutableStateOf("") }
  val coroutineScope = rememberCoroutineScope()

  TextField(
      value = messageText,
      onValueChange = {
        messageText =
            if (it.length <= MAX_MESSAGE_LENGTH) it else it.substring(0, MAX_MESSAGE_LENGTH)
      },
      placeholder = { Text("Type a message...") },
      modifier = Modifier.fillMaxWidth().testTag(SendMessageInputTestTags.TEXT_FIELD),
      singleLine = false,
      maxLines = 8,
      trailingIcon = {
        IconButton(
            enabled = messageText.isNotBlank(),
            onClick = {
              val text = messageText.trim()
              if (text.isNotBlank()) {
                coroutineScope.launch {
                  chat.sendMessage(Message(senderID = userID, message = text))
                }
                messageText = ""
              }
            },
            modifier = Modifier.testTag(SendMessageInputTestTags.SEND_BUTTON)) {
              androidx.compose.material3.Icon(
                  imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
            }
      })
}
