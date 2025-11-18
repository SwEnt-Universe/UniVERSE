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
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.universe.ui.navigation.NavigationBottomMenu
import com.android.universe.ui.navigation.Tab
import com.android.universe.ui.theme.Dimensions

@Composable
fun ChatListScreen(
    userID: String,
    onTabSelected: (Tab) -> Unit,
    onChatSelected: (String) -> Unit,
    vm: ChatListViewModel = viewModel { ChatListViewModel(userID = userID) }
) {
  val chatPreviews by vm.chatPreviews.collectAsState()
  Scaffold(bottomBar = { NavigationBottomMenu(Tab.Chat, onTabSelected) }) { paddingValues ->
    LazyColumn(
        horizontalAlignment = CenterHorizontally,
        modifier = Modifier.fillMaxSize().padding(paddingValues),
    ) {
      items(items = chatPreviews, key = { it.chatID }) { chatPreview ->
        ChatPreviewItem(chatPreview, onChatSelected)
      }
    }
  }
}

@Composable
fun ChatPreviewItem(chatPreview: ChatPreview, onChatSelected: (String) -> Unit) {
  Row(
      horizontalArrangement = Arrangement.SpaceBetween,
      modifier =
          Modifier.fillMaxWidth()
              .padding(horizontal = Dimensions.PaddingLarge, vertical = Dimensions.PaddingSmall)
              .background(
                  MaterialTheme.colorScheme.surface, RoundedCornerShape(Dimensions.RoundedCorner))
              .padding(horizontal = Dimensions.PaddingSmall, vertical = Dimensions.PaddingSmall)
              .clickable { onChatSelected(chatPreview.chatID) }) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
              text = chatPreview.chatName,
              style = MaterialTheme.typography.titleMedium,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis)
          Spacer(modifier = Modifier.height(Dimensions.SpacerMedium))
          Text(
              text = chatPreview.lastMessage.value?.message ?: "Empty in here ...",
              style = MaterialTheme.typography.bodyMedium,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis)
        }
        chatPreview.lastMessage.value?.displayTime?.let {
          Text(
              text = it,
              style = MaterialTheme.typography.labelMedium,
              modifier =
                  Modifier.padding(top = Dimensions.PaddingSmall, start = Dimensions.PaddingLarge))
        }
      }
}
