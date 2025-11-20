package com.android.universe.ui.chat.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.android.universe.model.chat.Message
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

object MessageListTestTags {
  const val LIST = "MESSAGE_LIST"
  const val ITEM_PREFIX = "MESSAGE_ITEM_"
}

/**
 * A composable that displays a list of messages in a chat interface.
 *
 * This composable uses a `LazyColumn` to efficiently display a potentially long list of messages.
 * It automatically scrolls to the bottom of the list when new messages are added, if the user is
 * already at the end, or if the new message is from the current user. This ensures a natural chat
 * experience.
 *
 * @param userID The ID of the current user, used to determine if a message is sent by them.
 * @param messages The list of [Message] objects to be displayed.
 * @param modifier The [Modifier] to be applied to the `LazyColumn`.
 * @param vm The [ChatUIViewModel] instance, passed down to child composables like [MessageItem] to
 *   handle user interactions or access user data.
 */
@Composable
fun MessageList(userID: String, messages: List<Message>, modifier: Modifier, vm: ChatUIViewModel) {
  val listState = rememberLazyListState()
  var isFirstLoad by remember { mutableStateOf(true) }
  val isScrolledToEnd by remember { derivedStateOf { listState.isScrolledToEnd() } }

  LaunchedEffect(messages.size) {
    val lastMessageIsFromUser = messages.lastOrNull()?.senderID == userID
    if (messages.isNotEmpty() && (isScrolledToEnd || isFirstLoad || lastMessageIsFromUser)) {
      listState.animateScrollToItem(index = messages.size - 1)
      isFirstLoad = false
    }
  }

  LazyColumn(
      modifier = modifier.testTag(MessageListTestTags.LIST),
      state = listState,
      verticalArrangement = Arrangement.Bottom) {
        itemsIndexed(messages, key = { _, message -> message.messageID }) { _, message ->
          MessageItem(
              senderID = message.senderID,
              message = message.message,
              time = timeStampToDisplayTime(timestamp = message.timestamp),
              isUserMe = message.senderID == userID,
              modifier = Modifier.testTag(MessageListTestTags.ITEM_PREFIX + message.messageID),
              vm = vm)
        }
      }
}

/**
 * Converts a Firebase Timestamp into a display-friendly time string.
 *
 * @param timestamp The Firebase Timestamp to be formatted.
 * @return A string representing the time in "HH:mm" format based on the default locale.
 */
fun timeStampToDisplayTime(timestamp: Timestamp): String {
  val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
  return formatter.format(timestamp.toDate())
}

/**
 * Extension function on [LazyListState] to check if the user has scrolled to the end of the list.
 * It determines this by checking if the last visible item's index is at or near the total number of
 * items, with a small threshold to account for layout variations and provide a smoother user
 * experience.
 *
 * @return `true` if the list is scrolled to the end, `false` otherwise.
 */
private fun LazyListState.isScrolledToEnd(): Boolean {
  val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
  val totalItems = layoutInfo.totalItemsCount
  return lastVisible >= totalItems - 2 // threshold of 1-2 items
}
