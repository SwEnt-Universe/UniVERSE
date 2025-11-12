package com.android.universe.ui.chat.composable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.universe.model.user.UserRepository
import com.android.universe.model.user.UserRepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MessageItemViewModel(
    private val userRepository: UserRepository = UserRepositoryProvider.repository
) : ViewModel() {

  // Map of senderID -> username
  // Map of senderID -> MutableStateFlow(username)
  private val _userFlows = mutableMapOf<String, MutableStateFlow<String>>()

  fun getUserName(userID: String): StateFlow<String> {
    return _userFlows
        .getOrPut(userID) { MutableStateFlow("...").also { flow -> loadUserName(userID, flow) } }
        .asStateFlow()
  }

  private fun loadUserName(userID: String, flow: MutableStateFlow<String>) {
    viewModelScope.launch {
      val name =
          try {
            userRepository.getUser(userID).username
          } catch (_: IllegalArgumentException) {
            "deleted"
          }
      flow.value = name
    }
  }
}
