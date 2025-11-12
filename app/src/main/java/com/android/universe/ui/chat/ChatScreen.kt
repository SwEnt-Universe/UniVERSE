package com.android.universe.ui.chat

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.universe.model.chat.Chat
import com.android.universe.model.chat.ChatManager
import com.android.universe.ui.chat.composable.MessageItemViewModel
import com.android.universe.ui.chat.composable.MessageList
import com.android.universe.ui.chat.composable.SendMessageInput
import com.android.universe.ui.navigation.NavigationBottomMenu
import com.android.universe.ui.navigation.NavigationTestTags
import com.android.universe.ui.navigation.Tab

/**
 * A composable function that displays the main screen for a chat conversation. It includes the list
 * of messages, an input field to send new messages, and the app's navigation bottom bar.
 *
 * This screen is responsible for loading the chat data based on the provided [chatID] and
 * displaying a loading state until the data is available. Once loaded, it renders the `MessageList`
 * and `SendMessageInput` composable.
 *
 * @param chatID The unique identifier for the chat to be displayed.
 * @param userID The unique identifier for the current user. Defaults to an empty string.
 * @param onTabSelected A callback function invoked when a tab in the bottom navigation menu is
 *   selected.
 * @param messageItemViewModel The ViewModel responsible for handling logic related to individual
 *   message items, such as reactions.
 */
@Composable
fun ChatScreen(
    chatID: String,
    userID: String = "",
    onTabSelected: (Tab) -> Unit,
    messageItemViewModel: MessageItemViewModel = viewModel()
) {

  // Collect the chat as Compose state
  val chat by
      produceState<Chat?>(initialValue = null, chatID) {
        value =
            try {
              ChatManager.loadChat(chatID)
            } catch (e: Exception) {
              Log.w("ChatScreen", "Failed to load chat", e)
              null
            }
      }

  Scaffold(bottomBar = { NavigationBottomMenu(Tab.Chat, onTabSelected) }) { paddingValues ->
    Column(
        modifier =
            Modifier.fillMaxSize().padding(paddingValues).testTag(NavigationTestTags.CHAT_SCREEN)) {
          chat?.let { chat ->
            MessageList(
                userID = userID,
                messages = chat.messages,
                modifier = Modifier.weight(weight = 1f),
                messageItemViewModel = messageItemViewModel)
            SendMessageInput(userID = userID, chat = chat)
          } ?: run { Text("Loading chat...") }
        }
  }
}
