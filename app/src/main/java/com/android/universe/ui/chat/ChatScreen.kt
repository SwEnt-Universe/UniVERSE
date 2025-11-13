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
import com.android.universe.ui.chat.composable.ChatUIViewModel
import com.android.universe.ui.chat.composable.ChatUIViewModelFactory
import com.android.universe.ui.chat.composable.MessageList
import com.android.universe.ui.chat.composable.SendMessageInput
import com.android.universe.ui.navigation.NavigationBottomMenu
import com.android.universe.ui.navigation.NavigationTestTags
import com.android.universe.ui.navigation.Tab

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
            is ChatUIViewModel.ChatUiState.Loading -> Text("Loading chat...")
            is ChatUIViewModel.ChatUiState.Error -> Text("Failed to load chat.")
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
