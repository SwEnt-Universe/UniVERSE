package com.android.universe.ui.selectTag

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.universe.model.Tag
import com.android.universe.model.user.UserProfile
import com.android.universe.model.user.UserRepository
import com.android.universe.model.user.UserRepositoryProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SelectTagViewModel(
    private val userRepository: UserRepository = UserRepositoryProvider.repository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {
  private val selectedTags = MutableStateFlow<List<String>>(emptyList())
  val uiStateTags = selectedTags.asStateFlow()

  fun addTag(name: String) {
    if (selectedTags.value.contains(name)) {
      Log.e("SelectTagViewModel", "Cannot add tag '$name' because it was already in the list")
      throw IllegalArgumentException("Tag '$name' not found in selectedTags")
    } else {
      selectedTags.value = selectedTags.value + name
    }
  }

  fun deleteTag(name: String) {
    if (!selectedTags.value.contains(name)) {
      Log.e("SelectTagViewModel", "Cannot delete tag '$name' because it is not in the list")
      throw IllegalArgumentException("Tag '$name' not found in selectedTags")
    } else {
      selectedTags.value = selectedTags.value - name
    }
  }

  fun loadTags(username: String) {
    viewModelScope.launch(dispatcher) {
      val userProfil = userRepository.getUser(username)
      selectedTags.value = userProfil.tags.map { tag -> tag.name }
    }
  }

  fun saveTags(username: String) {
    viewModelScope.launch(dispatcher) {
      val userProfil = userRepository.getUser(username)
      val newUserProfil =
          UserProfile(
              userProfil.username,
              userProfil.firstName,
              userProfil.lastName,
              userProfil.country,
              userProfil.description,
              userProfil.dateOfBirth,
              selectedTags.value.map { tagName -> Tag(tagName) })
      userRepository.updateUser(username, newUserProfil)
    }
  }
}
