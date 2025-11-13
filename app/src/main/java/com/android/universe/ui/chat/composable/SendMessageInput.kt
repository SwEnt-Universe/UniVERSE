package com.android.universe.ui.chat.composable

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

object SendMessageInputTestTags {
  const val TEXT_FIELD = "SEND_MESSAGE_TEXT_FIELD"
  const val SEND_BUTTON = "SEND_BUTTON"
}

@Composable
fun SendMessageInput(vm: ChatUIViewModel) {
  val messageText by vm.messageText.collectAsState()

  TextField(
      value = messageText,
      onValueChange = { vm.onInput(it) },
      placeholder = { Text("Type a message...") },
      modifier = Modifier.fillMaxWidth().testTag(SendMessageInputTestTags.TEXT_FIELD),
      singleLine = false,
      maxLines = 8,
      trailingIcon = {
        IconButton(
            enabled = messageText.isNotBlank(),
            onClick = { vm.sendMessage() },
            modifier = Modifier.testTag(SendMessageInputTestTags.SEND_BUTTON)) {
              Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
            }
      })
}
