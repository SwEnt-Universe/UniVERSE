package com.android.universe.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.universe.ui.chat.ChatListScreenTestTags.CHAT_ITEM_PREFIX
import com.android.universe.ui.chat.ChatListScreenTestTags.CHAT_LIST_COLUMN
import com.android.universe.ui.chat.ChatListScreenTestTags.CHAT_NAME
import com.android.universe.ui.chat.ChatListScreenTestTags.DISPLAY_TIME_TEXT
import com.android.universe.ui.chat.ChatListScreenTestTags.LAST_MESSAGE_TEXT
import com.android.universe.ui.chat.ChatListScreenTestTags.NO_CHAT_PREVIEW
import com.android.universe.ui.navigation.NavigationBottomMenu
import com.android.universe.ui.navigation.NavigationTestTags
import com.android.universe.ui.navigation.Tab
import com.android.universe.ui.theme.Dimensions

object ChatListScreenTestTags {
  const val CHAT_LIST_COLUMN = "chatListColumn"
  const val CHAT_ITEM_PREFIX = "chatPreviewItem_"
  const val CHAT_NAME = "chatName"
  const val LAST_MESSAGE_TEXT = "lastMessageText"
  const val DISPLAY_TIME_TEXT = "displayTimeText"
  const val NO_CHAT_PREVIEW = "noChatPreview"
}

/**
 * A composable function that displays the main screen for the chat list.
 *
 * This screen shows a list of chat previews. Each preview item is clickable and navigates to the
 * corresponding chat conversation. The screen also includes a bottom navigation bar. The chat data
 * is fetched and managed by the [ChatListViewModel].
 *
 * @param userID The ID of the current user, used to initialize the ViewModel.
 * @param onTabSelected A callback function invoked when a tab in the bottom navigation menu is
 *   selected.
 * @param onChatSelected A callback function invoked with the chat ID when a chat preview item is
 *   clicked.
 * @param vm An instance of [ChatListViewModel] that provides the state for this screen. By default,
 *   it's created using `viewModel()` with the provided `userID`.
 */
@Composable
fun ChatListScreen(
    userID: String,
    onTabSelected: (Tab) -> Unit,
    onChatSelected: (chatID: String, chatName: String) -> Unit,
    vm: ChatListViewModel = viewModel { ChatListViewModel(userID = userID) }
) {
  val chatPreviews by vm.chatPreviews.collectAsState()
  Scaffold(
      bottomBar = { NavigationBottomMenu(Tab.Chat, onTabSelected) },
      modifier = Modifier.testTag(NavigationTestTags.CHAT_SCREEN)) { paddingValues ->
        if (chatPreviews.isNotEmpty()) {
          LazyColumn(
              horizontalAlignment = CenterHorizontally,
              modifier = Modifier.fillMaxSize().padding(paddingValues).testTag(CHAT_LIST_COLUMN),
          ) {
            items(items = chatPreviews, key = { it.chatID }) { chatPreview ->
              ChatPreviewItem(chatPreview, onChatSelected)
            }
          }
        } else {
          Column(
              horizontalAlignment = CenterHorizontally,
              verticalArrangement = Arrangement.Center,
              modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                Text(
                    text = "Join some events to start chatting with others",
                    modifier = Modifier.testTag(NO_CHAT_PREVIEW))
              }
        }
      }
}

/**
 * A composable that displays a preview of a single chat conversation. It shows the chat name, the
 * last message, and the time the last message was sent. The entire item is clickable, triggering a
 * navigation event to the full chat screen.
 *
 * @param chatPreview The data object containing information about the chat to display.
 * @param onChatSelected A callback function that is invoked with the chat's ID when the item is
 *   clicked.
 */
@Composable
fun ChatPreviewItem(
    chatPreview: ChatPreview,
    onChatSelected: (chatID: String, chatName: String) -> Unit
) {
  val nodeTestTag = CHAT_ITEM_PREFIX + chatPreview.chatID
  Row(
      horizontalArrangement = Arrangement.SpaceBetween,
      modifier =
          Modifier.fillMaxWidth()
              .padding(horizontal = Dimensions.PaddingLarge, vertical = Dimensions.PaddingSmall)
              .background(
                  MaterialTheme.colorScheme.surface, RoundedCornerShape(Dimensions.RoundedCorner))
              .padding(horizontal = Dimensions.PaddingSmall, vertical = Dimensions.PaddingSmall)
              .clickable { onChatSelected(chatPreview.chatID, chatPreview.chatName) }
              // Add the test tag to the clickable Row, using chatID for uniqueness
              .testTag(nodeTestTag)) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
              text = chatPreview.chatName,
              style = MaterialTheme.typography.titleMedium,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.testTag(nodeTestTag + CHAT_NAME))
          Spacer(modifier = Modifier.height(Dimensions.SpacerMedium))
          Text(
              text = chatPreview.lastMessage.value?.message ?: "Empty in here ...",
              style = MaterialTheme.typography.bodyMedium,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.testTag(nodeTestTag + LAST_MESSAGE_TEXT))
        }
        chatPreview.lastMessage.value?.let {
          Text(
              text = it.getDisplayTime(),
              style = MaterialTheme.typography.labelMedium,
              modifier =
                  Modifier.padding(top = Dimensions.PaddingSmall, start = Dimensions.PaddingLarge)
                      .testTag(nodeTestTag + DISPLAY_TIME_TEXT))
        }
      }
}
