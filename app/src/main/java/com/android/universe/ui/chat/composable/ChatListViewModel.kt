package com.android.universe.ui.chat.composable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.universe.model.chat.ChatManager
import com.android.universe.model.event.EventRepository
import com.android.universe.model.event.EventRepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatPreview(val chatName: String, val lastMessage: String)

class ChatListViewModel(
    private val userID: String,
    private val eventRepository: EventRepository = EventRepositoryProvider.repository,
) : ViewModel() {

  private val _chatPreviews = MutableStateFlow<List<ChatPreview>>(emptyList())
  val chatPreviews = _chatPreviews.asStateFlow()

  init {
    loadEvents()
  }

  private fun loadEvents() {
    viewModelScope.launch {
      // Since users don't have a list of event they participate in, we filter the events.
      // TODO: Update this once users have a list of events.
      val events = eventRepository.getAllEvents().filter { it.participants.contains(userID) }
      val chats =
          events.map {
            try {
              ChatManager.loadChat(chatID = it.id)
            } catch (_: NoSuchElementException) {
              // Since we have created events before chats existed we create them here,
              // if an event doesn't have an associated chat.
              // TODO: This should be moved to event creation.
              ChatManager.createChat(chatID = it.id, admin = it.creator)
            }
          }
    }
  }
}
