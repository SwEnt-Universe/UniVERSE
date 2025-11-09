package com.android.universe.model.tag

import kotlinx.coroutines.flow.Flow

/**
 * Manage the tags data during the event creation process.
 * Keep a set of tags and update it accordingly to the action of the user.
 */
interface TagTemporaryRepository {
    /**
     * Flow emitting the current set of tags.
     */
    val tagsFlow: Flow<Set<Tag>>

    /**
     * Update the current set of tags.
     * @param tags the new set of tags that should replace the old one.
     */
    suspend fun updateTags(tags: Set<Tag>)

    /**
     * Return the current set of tags.
     */
    suspend fun getTags(): Set<Tag>

    /**
     * Clear the current set of tags.
     */
    suspend fun deleteAllTags()
}