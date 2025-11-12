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
import com.android.universe.model.chat.Chat
import com.android.universe.model.chat.ChatManager
import com.android.universe.ui.chat.composable.MessageList
import com.android.universe.ui.chat.composable.SendMessageInput
import com.android.universe.ui.navigation.NavigationBottomMenu
import com.android.universe.ui.navigation.Tab

@Composable
fun ChatScreen(chatID: String, userID: String = "", onTabSelected: (Tab) -> Unit) {

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
    Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
      chat?.let { chat ->
        MessageList(
            userID = userID, messages = chat.messages, modifier = Modifier.weight(weight = 1f))
        SendMessageInput(userID = userID, chat = chat)
      } ?: run { Text("Loading chat...") }
    }
  }
}
