package com.android.universe.ui.selectTag

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.universe.model.Tag
import com.android.universe.model.user.UserRepository
import com.android.universe.model.user.UserRepositoryProvider
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
 */
class SelectTagViewModel(
    private val userRepository: UserRepository = UserRepositoryProvider.repository,
) : ViewModel() {
  /** Backing field for [uiStateTags]. Mutable within the ViewModel only. */
  private val selectedTags = MutableStateFlow<List<Tag>>(emptyList())

  /** Publicly exposed state of the selected tags. */
  val uiStateTags = selectedTags.asStateFlow()

  /**
   * Adds a new tag to the list of selected tags. Throws an IllegalArgumentException and logs an
   * error in Logcat if the tag is already selected.
   *
   * @param tag the tag that is selected.
   */
  fun addTag(tag: Tag) {
    if (selectedTags.value.contains(tag)) {
      Log.e(
          "SelectTagViewModel",
          "Cannot add tag '${tag.displayName}' because it was already in the list")
      throw IllegalArgumentException("Tag '${tag.displayName}' is already selected")
    } else {
      selectedTags.value = selectedTags.value + tag
    }
  }

  /**
   * Removes a tag from the list of selected tags. Throws an IllegalArgumentException and logs an
   * error in Logcat if the tag is not already selected.
   *
   * @param tag the tag that is deselected.
   */
  fun deleteTag(tag: Tag) {
    if (!selectedTags.value.contains(tag)) {
      Log.e(
          "SelectTagViewModel",
          "Cannot delete tag '${tag.displayName}' because it is not in the list")
      throw IllegalArgumentException("Tag '${tag.displayName}' is not currently selected")
    } else {
      selectedTags.value = selectedTags.value - tag
    }
  }

  /**
   * Updates the selectedTags value by replacing it with the tags already selected in the
   * userProfile.
   *
   * @param uid the uid of the current user.
   */
  fun loadTags(uid: String) {
    viewModelScope.launch {
      val userProfile = userRepository.getUser(uid)
      selectedTags.value = userProfile.tags.toList()
    }
  }

  /**
   * Saves the selected tags to the userProfile of the current User.
   *
   * @param uid the uid of the current user.
   */
  fun saveTags(uid: String) {
    viewModelScope.launch {
      val userProfile = userRepository.getUser(uid)
      val newUserProfile = userProfile.copy(tags = selectedTags.value.toSet())
      userRepository.updateUser(uid, newUserProfile)
    }
  }
}
