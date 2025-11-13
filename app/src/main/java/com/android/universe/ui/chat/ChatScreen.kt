package com.android.universe.ui.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.universe.model.chat.Chat
import com.android.universe.ui.chat.ChatScreenTestTags.ERROR
import com.android.universe.ui.chat.ChatScreenTestTags.LOADING
import com.android.universe.ui.chat.composable.ChatUIViewModel
import com.android.universe.ui.chat.composable.ChatUIViewModelFactory
import com.android.universe.ui.chat.composable.MessageList
import com.android.universe.ui.chat.composable.SendMessageInput
import com.android.universe.ui.navigation.NavigationBottomMenu
import com.android.universe.ui.navigation.NavigationTestTags
import com.android.universe.ui.navigation.Tab

object ChatScreenTestTags {
  const val LOADING = "CHAT_LOADING"
  const val ERROR = "CHAT_ERROR"
}

/**
 * A composable function that represents the main screen for a chat. It displays the chat messages,
 * an input field for sending new messages, and the main navigation bottom bar.
 *
 * This screen observes the UI state from [ChatUIViewModel] to display either a loading indicator,
 * an error message, or the chat content itself.
 *
 * @param chatID The unique identifier for the chat to be displayed.
 * @param userID The unique identifier for the current user. Defaults to an empty string. This is
 *   used to distinguish the user's messages from others.
 * @param onTabSelected A callback function invoked when a tab in the [NavigationBottomMenu] is
 *   selected.
 * @param vm An instance of [ChatUIViewModel] used to manage the state and logic of the chat screen.
 *   It is created by default using a [ChatUIViewModelFactory].
 */
@Composable
fun ChatScreen(
    chatID: String,
    userID: String = "",
    onTabSelected: (Tab) -> Unit,
    vm: ChatUIViewModel = viewModel(factory = ChatUIViewModelFactory(chatID, userID))
) {

  val uiState by vm.uiState.collectAsState()

  Scaffold(bottomBar = { NavigationBottomMenu(Tab.Chat, onTabSelected) }) { paddingValues ->
    Column(
        modifier =
            Modifier.fillMaxSize().padding(paddingValues).testTag(NavigationTestTags.CHAT_SCREEN)) {
          when (val state = uiState) {
            is ChatUIViewModel.ChatUiState.Loading ->
                Text("Loading chat...", modifier = Modifier.testTag(LOADING))
            is ChatUIViewModel.ChatUiState.Error ->
                Text("Failed to load chat.", modifier = Modifier.testTag(ERROR))
            is ChatUIViewModel.ChatUiState.Success -> {
              MessageList(
                  userID = userID,
                  messages = state.chat.messages,
                  modifier = Modifier.weight(weight = 1f),
                  vm = vm)

              SendMessageInput(vm = vm)
            }
          }
        }
  }
}
