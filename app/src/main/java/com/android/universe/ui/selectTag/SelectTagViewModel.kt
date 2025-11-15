package com.android.universe.ui.selectTag

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.universe.model.tag.Tag
import com.android.universe.model.tag.TagTemporaryRepository
import com.android.universe.model.tag.TagTemporaryRepositoryProvider
import com.android.universe.model.user.UserRepository
import com.android.universe.model.user.UserRepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Mode to know if the viewModel should save tag for a userProfile or for an event. */
enum class SelectTagMode {
  USER_PROFILE,
  EVENT_CREATION
}

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
 *     @param selectTagMode The mode the viewModel should run on.
 *     @param tagRepository The repository for the tags. Used only if the mode is EVENT_CREATION.
 */
class SelectTagViewModel(
    private val userRepository: UserRepository = UserRepositoryProvider.repository,
    private val selectTagMode: SelectTagMode = SelectTagMode.USER_PROFILE,
    private val tagRepository: TagTemporaryRepository = TagTemporaryRepositoryProvider.repository
) : ViewModel() {
  /** Backing field for [uiStateTags]. Mutable within the ViewModel only. */
  private val _selectedTags = MutableStateFlow<List<Tag>>(emptyList())
  var mode = selectTagMode
  /** Publicly exposed state of the selected tags. */
  val selectedTags = _selectedTags.asStateFlow()

  /**
   * We launch a coroutine that will update the set of tag each time the tag repository change. This
   * allow the user to see the tag he already selected if he returns to the screen.
   */
  init {
    if (mode == SelectTagMode.EVENT_CREATION) {
      viewModelScope.launch {
        tagRepository.tagsFlow.collect { newTags -> _selectedTags.value = newTags.toList() }
      }
    }
  }

  /**
   * Adds a new tag to the list of selected tags. Throws an IllegalArgumentException and logs an
   * error in Logcat if the tag is already selected.
   *
   * @param tag the tag that is selected.
   */
  fun addTag(tag: Tag) {
    if (_selectedTags.value.contains(tag)) {
      Log.e(
          "SelectTagViewModel",
          "Cannot add tag '${tag.displayName}' because it was already in the list")
      throw IllegalArgumentException("Tag '${tag.displayName}' is already selected")
    } else {
      _selectedTags.value = _selectedTags.value + tag
    }
  }

  /**
   * Removes a tag from the list of selected tags. Throws an IllegalArgumentException and logs an
   * error in Logcat if the tag is not already selected.
   *
   * @param tag the tag that is deselected.
   */
  fun deleteTag(tag: Tag) {
    if (!_selectedTags.value.contains(tag)) {
      Log.e(
          "SelectTagViewModel",
          "Cannot delete tag '${tag.displayName}' because it is not in the list")
      throw IllegalArgumentException("Tag '${tag.displayName}' is not currently selected")
    } else {
      _selectedTags.value = _selectedTags.value - tag
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
      when (mode) {
        SelectTagMode.USER_PROFILE -> {
          val userProfile = userRepository.getUser(uid)
          _selectedTags.value = userProfile.tags.toList()
        }
        SelectTagMode.EVENT_CREATION -> _selectedTags.value = tagRepository.getTags().toList()
      }
    }
  }

  /**
   * Saves the selected tags to the userProfile of the current User.
   *
   * @param uid the uid of the current user.
   */
  fun saveTags(uid: String) {
    viewModelScope.launch {
      when (mode) {
        SelectTagMode.USER_PROFILE -> {
          val userProfile = userRepository.getUser(uid)
          val newUserProfile = userProfile.copy(tags = _selectedTags.value.toSet())
          userRepository.updateUser(uid, newUserProfile)
        }
        SelectTagMode.EVENT_CREATION -> tagRepository.updateTags(_selectedTags.value.toSet())
      }
    }
  }
}
