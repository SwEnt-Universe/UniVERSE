package com.android.universe.ui.chat

import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.universe.model.chat.ChatManager
import com.android.universe.model.chat.Message
import com.android.universe.model.event.EventRepository
import com.android.universe.model.event.EventRepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatPreview(val chatName: String, val chatID: String, val lastMessage: State<Message?>)

class ChatListViewModel(
    private val userID: String,
    private val eventRepository: EventRepository = EventRepositoryProvider.repository,
) : ViewModel() {

  private val _chatPreviews = MutableStateFlow(emptyList<ChatPreview>())
  val chatPreviews: StateFlow<List<ChatPreview>> = _chatPreviews.asStateFlow()

  init {
    loadEvents()
  }

  private fun loadEvents() {
    viewModelScope.launch {
      // Since users don't have a list of event they participate in, we filter the events.
      // TODO: Update this once users have a list of events.
      val events = eventRepository.getAllEvents().filter { it.participants.contains(userID) }
      events.forEach { event ->
        val chat =
            try {
              ChatManager.loadChat(chatID = event.id)
            } catch (_: NoSuchElementException) {
              // Since we have created events before chats existed we create them here,
              // if an event doesn't have an associated chat.
              // TODO: This should be moved to event creation.
              ChatManager.createChat(chatID = event.id, admin = event.creator)
            }

        _chatPreviews.value =
            _chatPreviews.value +
                ChatPreview(
                    chatName = event.title, chatID = chat.chatID, lastMessage = chat.lastMessage)
      }
    }
  }
}
