package com.android.universe.ui.chat.composable

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android.universe.ui.components.LiquidBox
import com.android.universe.ui.theme.Dimensions

object SendMessageInputTestTags {
  const val TEXT_FIELD = "SEND_MESSAGE_TEXT_FIELD"
  const val SEND_BUTTON = "SEND_BUTTON"
}

/**
 * A composable function that provides a text input field and a send button for a chat interface. It
 * observes the state from the provided ViewModel to manage the input text and trigger send actions.
 *
 * @param vm The [ChatUIViewModel] instance that provides the state and handles user interactions
 *   for the message input.
 */
@Composable
fun SendMessageInput(vm: ChatUIViewModel) {
  val messageText by vm.messageText.collectAsState()
  val cornerRadius: Dp = Dimensions.RoundedCorner * 2
  val shape =
      RoundedCornerShape(
          topStart = cornerRadius,
          topEnd = cornerRadius,
          bottomStart = cornerRadius,
          bottomEnd = 0.dp)
  LiquidBox(shape = shape) {
    TextField(
        value = messageText,
        onValueChange = { vm.onInput(it) },
        placeholder = { Text("Type a message...") },
        modifier = Modifier.fillMaxWidth().testTag(SendMessageInputTestTags.TEXT_FIELD),
        singleLine = false,
        maxLines = 8,
        colors =
            TextFieldDefaults.colors()
                .copy(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    errorContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    focusedTrailingIconColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurface,
                    cursorColor = MaterialTheme.colorScheme.primary),
        trailingIcon = {
          IconButton(
              enabled = messageText.isNotBlank(),
              onClick = { vm.sendMessage() },
              modifier = Modifier.testTag(SendMessageInputTestTags.SEND_BUTTON)) {
                Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
              }
        })
  }
}
