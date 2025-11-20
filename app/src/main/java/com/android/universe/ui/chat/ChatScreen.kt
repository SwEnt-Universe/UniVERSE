package com.android.universe.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.universe.ui.chat.ChatScreenTestTags.ERROR
import com.android.universe.ui.chat.ChatScreenTestTags.LOADING
import com.android.universe.ui.chat.composable.ChatUIViewModel
import com.android.universe.ui.chat.composable.ChatUIViewModelFactory
import com.android.universe.ui.chat.composable.MessageList
import com.android.universe.ui.chat.composable.SendMessageInput
import com.android.universe.ui.components.LiquidTopBar
import com.android.universe.ui.components.ScreenLayout
import com.android.universe.ui.components.TopBarBackButton
import com.android.universe.ui.components.TopBarTitle
import com.android.universe.ui.navigation.NavigationBottomMenu
import com.android.universe.ui.navigation.Tab
import com.android.universe.ui.theme.Dimensions

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
    chatName: String = "",
    userID: String = "",
    onTabSelected: (Tab) -> Unit,
    onBack: () -> Unit = {},
    vm: ChatUIViewModel = viewModel { ChatUIViewModel(chatID, userID) }
) {

  val uiState by vm.uiState.collectAsState()
  ScreenLayout(
      topBar = {
        LiquidTopBar(
            title = { TopBarTitle(text = chatName) }, navigationIcon = { TopBarBackButton(onBack) })
      },
      bottomBar = { NavigationBottomMenu(Tab.Chat, onTabSelected) }) { paddingValues ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    // TODO: replace with real background once we have one.
                    .background(
                        brush =
                            Brush.linearGradient(
                                colorStops =
                                    arrayOf(
                                        0.0f to Color(0xFFFF0000), // Red
                                        0.125f to Color(0xFFFF7F00), // Orange
                                        0.25f to Color(0xFFFFFF00), // Yellow
                                        0.375f to Color(0xFF00FF00), // Green
                                        0.5f to Color(0xFF0000FF), // Blue
                                        0.625f to Color(0xFF4B0082), // Indigo
                                        0.75f to Color(0xFF8B00FF), // Violet
                                        0.875f to Color.Black, // Black
                                        1.0f to Color.White // White
                                        ),
                                start = Offset.Zero,
                                end = Offset(2000f, 2000f) // strong diagonal
                                ))
                    .padding(paddingValues)
                    .padding(horizontal = Dimensions.PaddingMedium)) {
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
