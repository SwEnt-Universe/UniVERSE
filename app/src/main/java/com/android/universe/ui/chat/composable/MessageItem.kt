package com.android.universe.ui.chat.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.universe.ui.theme.Dimensions

object MessageItemTestTags {
  const val USERNAME = "USERNAME"
  const val MESSAGE_TEXT = "MESSAGE_TEXT"
  const val TIME = "TIME"
  const val MESSAGE_CONTAINER = "MESSAGE_CONTAINER"
}

/**
 * A composable that displays a single message in a chat conversation. It handles the visual
 * distinction between messages sent by the current user ("me") and messages sent by others.
 *
 * @param senderID The unique identifier of the message sender. Used to fetch the user's name.
 * @param message The text content of the message.
 * @param time The timestamp of when the message was sent, formatted as a string.
 * @param modifier The modifier to be applied to the message item container.
 * @param isUserMe A boolean flag indicating if the message was sent by the current user. This
 *   controls the alignment and color scheme of the message bubble.
 * @param vm The [MessageItemViewModel] instance used to fetch sender details, typically provided by
 *   `viewModel()`.
 */
@Composable
fun MessageItem(
    senderID: String,
    message: String,
    time: String,
    modifier: Modifier = Modifier,
    isUserMe: Boolean = false,
    vm: MessageItemViewModel = viewModel(),
) {
  val userName by vm.getUserName(senderID).collectAsState()

  val messageCornerRadius: Dp = Dimensions.RoundedCorner * 2
  val arrangement = if (isUserMe) Arrangement.End else Arrangement.Start

  Row(
      modifier =
          modifier
              .fillMaxWidth()
              .padding(Dimensions.PaddingMedium)
              .testTag(MessageItemTestTags.MESSAGE_CONTAINER),
      horizontalArrangement = arrangement) {
        Row(modifier = Modifier.fillMaxWidth(0.9f), horizontalArrangement = arrangement) {
          Column(
              modifier =
                  Modifier.wrapContentWidth()
                      .background(
                          color =
                              if (isUserMe) MaterialTheme.colorScheme.primary
                              else MaterialTheme.colorScheme.surfaceVariant,
                          shape =
                              RoundedCornerShape(
                                  topStart = messageCornerRadius,
                                  topEnd = messageCornerRadius,
                                  bottomStart = if (isUserMe) messageCornerRadius else 0.dp,
                                  bottomEnd = if (isUserMe) 0.dp else messageCornerRadius))
                      .padding(Dimensions.PaddingMedium),
              horizontalAlignment = Alignment.End) {
                Column {
                  if (!isUserMe) {
                    Text(
                        text = userName,
                        style =
                            MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.testTag(MessageItemTestTags.USERNAME))
                  }

                  Text(
                      text = message,
                      style = MaterialTheme.typography.bodyMedium,
                      color =
                          if (isUserMe) MaterialTheme.colorScheme.onPrimary
                          else MaterialTheme.colorScheme.onSurface,
                      modifier =
                          Modifier.padding(Dimensions.PaddingSmall)
                              .testTag(MessageItemTestTags.MESSAGE_TEXT))
                }

                Text(
                    text = time,
                    style = MaterialTheme.typography.labelSmall,
                    color =
                        if (isUserMe) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag(MessageItemTestTags.TIME))
              }
        }
      }
}
