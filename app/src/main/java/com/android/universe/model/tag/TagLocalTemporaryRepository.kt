package com.android.universe.model.tag

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Implementation of the TagTemporaryRepository Manage the tags data during the event creation
 * process. Keep a set of tags and update it accordingly to the action of the user.
 */
class TagLocalTemporaryRepository() : TagTemporaryRepository {
  private val _tagsTemporary = MutableStateFlow<Set<Tag>>(emptySet())
  override val tagsFlow: Flow<Set<Tag>> = _tagsTemporary.asStateFlow()

  /**
   * Update the current set of tags.
   *
   * @param tags the new set of tags that should replace the old one.
   */
  override suspend fun updateTags(tags: Set<Tag>) {
    _tagsTemporary.value = tags
  }

  /** Return the current set of tags. */
  override suspend fun getTags(): Set<Tag> {
    return _tagsTemporary.value
  }

  /** Clear the current set of tags. */
  override suspend fun deleteAllTags() {
    _tagsTemporary.value = emptySet()
  }
}
