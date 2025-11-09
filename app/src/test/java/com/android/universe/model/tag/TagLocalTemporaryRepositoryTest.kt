package com.android.universe.model.tag

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertEquals


class TagLocalTemporaryRepositoryTest {
    private lateinit var repository: TagTemporaryRepository
    private val tags1 = setOf(Tag.ROLE_PLAYING_GAMES, Tag.METAL, Tag.HANDBALL)
    private val tags2 = setOf(Tag.KARATE, Tag.REGGAE)

    @Before
    fun setup() {
        repository = TagLocalTemporaryRepository()
    }

    @Test
    fun updateTagsChangeTagsInEmptyRepo() = runTest{
        repository.updateTags(tags1)
        val resultTags = repository.getTags()
        assertEquals(tags1, resultTags)
    }

    @Test
    fun updateTagsChangeTagsInNonEmptyRepo() = runTest{
        repository.updateTags(tags1)
        repository.updateTags(tags2)
        val resultTags = repository.getTags()
        assertEquals(tags2, resultTags)
    }

    @Test
    fun deleteTagsChangeTagsInRepo() = runTest{
        repository.updateTags(tags1)
        repository.deleteAllTags()
        val resultTags = repository.getTags()
        assertEquals(emptySet<Tag>(), resultTags)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun tagsFlowEmitsUpdates() = runTest {
        val expected = listOf(emptySet<Tag>(), tags1, tags2, emptySet<Tag>())
        val emissions = mutableListOf<Set<Tag>>()

        val job = launch {
            repository.tagsFlow
                .take(expected.size)
                .toList(emissions)
        }

        runCurrent()

        repository.updateTags(tags1)
        runCurrent()

        repository.updateTags(tags2)
        runCurrent()

        repository.deleteAllTags()
        runCurrent()

        job.join()

        assertEquals(expected, emissions)
    }
}