package com.android.universe.ui.chat.composable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.universe.model.user.UserRepository
import com.android.universe.model.user.UserRepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MessageItemViewModel(
    private val userRepository: UserRepository = UserRepositoryProvider.repository
) : ViewModel() {
  private val _userName = MutableStateFlow("")
  val userName = _userName.asStateFlow()

  fun loadUserName(userID: String) {
    viewModelScope.launch {
      val userName =
          try {
            userRepository.getUser(userID).username
          } catch (_: IllegalArgumentException) {
            "deleted"
          }
      _userName.value = userName
    }
  }
}
