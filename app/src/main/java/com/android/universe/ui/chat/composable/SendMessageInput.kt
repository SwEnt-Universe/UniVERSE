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
import com.android.universe.model.chat.Chat
import com.android.universe.model.chat.Message
import kotlinx.coroutines.launch

@Composable
fun SendMessageInput(chat: Chat, userID: String) {
  var messageText by remember { mutableStateOf("") }
  val coroutineScope = rememberCoroutineScope()

  TextField(
      value = messageText,
      onValueChange = { messageText = it },
      placeholder = { Text("Type a message...") },
      modifier = Modifier.fillMaxWidth(),
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
            }) {
              androidx.compose.material3.Icon(
                  imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
            }
      })
}
