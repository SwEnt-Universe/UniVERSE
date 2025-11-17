package com.android.universe.ui.chat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.universe.ui.chat.composable.ChatListViewModel
import com.android.universe.ui.chat.composable.ChatPreview

@Composable
fun ChatListScreen(
    userID: String,
    vm: ChatListViewModel = viewModel { ChatListViewModel(userID = userID) }
) {
  val chatPreviews by vm.chatPreviews.collectAsState()
}

@Composable fun ChatPreviewItem(chatPreview: ChatPreview) {}
