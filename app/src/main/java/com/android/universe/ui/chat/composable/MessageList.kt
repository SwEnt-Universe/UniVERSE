package com.android.universe.ui.chat.composable

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.android.universe.model.chat.Message
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun MessageList(
    userID: String,
    messages: List<Message>,
    modifier: Modifier,
) {

  val listState = rememberLazyListState()
  val init = remember { mutableStateOf(true) }

  LaunchedEffect(messages.size) {
    val lastMessageIsFromUser = messages.lastOrNull()?.senderID == userID
    if (messages.isNotEmpty() &&
        (listState.isScrolledToEnd() || init.value || lastMessageIsFromUser)) {
      listState.animateScrollToItem(index = messages.size - 1)
      init.value = false
    }
  }

  LazyColumn(modifier = modifier, state = listState, verticalArrangement = Arrangement.Bottom) {
    itemsIndexed(messages, key = { _, message -> message.messageID }) { _, message ->
      MessageItem(
          senderID = message.senderID,
          message = message.message,
          time = timeStampToDisplayTime(timestamp = message.timestamp),
          isUserMe = message.senderID == userID)
    }
  }
}

fun timeStampToDisplayTime(timestamp: Timestamp): String {
  val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
  return formatter.format(timestamp.toDate())
}

fun LazyListState.isScrolledToEnd(): Boolean {
  val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
  val totalItems = layoutInfo.totalItemsCount
  Log.w("isScrolledToEnd", "lastVisible: $lastVisible, totalItems: $totalItems")
  return lastVisible >= totalItems - 2 // threshold of 1-2 items
}
