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

/**
 * ViewModel responsible for managing the SelectTag screen.
 *
 * It keeps track of the selected tags for the user, adds new ones, and saves them when the user
 * finishes.
 *
 * UI should collect [uiStateTags] to observe changes in real time.
 *
 * @param userRepository The data source handling user-related operations. Defaults to
 *   UserRepositoryProvider.repository
 * @param dispatcher The [CoroutineDispatcher] used for launching coroutines in this ViewModel.
 *   Defaults to [Dispatchers.Default].
 */
class SelectTagViewModel(
    private val userRepository: UserRepository = UserRepositoryProvider.repository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {
  /** Backing field for [uiStateTags]. Mutable within the ViewModel only. */
  private val selectedTags = MutableStateFlow<List<String>>(emptyList())

  /** Publicly exposed state of the selected tags. */
  val uiStateTags = selectedTags.asStateFlow()

  /**
   * Adds a new tag to the list of selected tags. Throws an IllegalArgumentException and logs an
   * error in Logcat if the tag is already selected.
   *
   * @param name the name of the tag that is selected.
   */
  fun addTag(name: String) {
    if (selectedTags.value.contains(name)) {
      Log.e("SelectTagViewModel", "Cannot add tag '$name' because it was already in the list")
      throw IllegalArgumentException("Tag '$name' is already selected")
    } else {
      selectedTags.value = selectedTags.value + name
    }
  }

  /**
   * Removes a tag from the list of selected tags. Throws an IllegalArgumentException and logs an
   * error in Logcat if the tag is not already selected.
   *
   * @param name the name of the tag that is deselected.
   */
  fun deleteTag(name: String) {
    if (!selectedTags.value.contains(name)) {
      Log.e("SelectTagViewModel", "Cannot delete tag '$name' because it is not in the list")
      throw IllegalArgumentException("Tag '$name' is not currently selected")
    } else {
      selectedTags.value = selectedTags.value - name
    }
  }

  /**
   * Updates the selectedTags value by replacing it with the tags already selected in the
   * userProfile.
   *
   * @param username the username of the current user.
   */
  fun loadTags(username: String) {
    viewModelScope.launch(dispatcher) {
      val userProfile = userRepository.getUser(username)
      selectedTags.value = userProfile.tags.map { tag -> tag.name }
    }
  }

  /**
   * Saves the selected tags to the userProfile of the current User.
   *
   * @param username the username of the current user.
   */
  fun saveTags(username: String) {
    viewModelScope.launch(dispatcher) {
      val userProfile = userRepository.getUser(username)
      val newUserProfile =
          UserProfile(
              userProfile.username,
              userProfile.firstName,
              userProfile.lastName,
              userProfile.country,
              userProfile.description,
              userProfile.dateOfBirth,
              selectedTags.value.map { tagName -> Tag(tagName) })
      userRepository.updateUser(username, newUserProfile)
    }
  }
}
