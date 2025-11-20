package com.android.universe.ui.selectTag

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.model.tag.Tag
import com.android.universe.model.tag.TagLocalTemporaryRepository
import com.android.universe.model.tag.TagTemporaryRepository
import com.android.universe.model.user.FakeUserRepository
import com.android.universe.model.user.UserProfile
import com.android.universe.utils.MainCoroutineRule
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class SelectTagViewModelTest {
  // Define the parameters for the tests.
  @get:Rule val mainCoroutineRule = MainCoroutineRule()

  private lateinit var repository: FakeUserRepository
  private lateinit var tagRepository: TagTemporaryRepository
  private lateinit var viewModelUser: SelectTagViewModel
  private lateinit var viewModelEvent: SelectTagViewModel

  val tags = setOf(Tag.HANDBALL, Tag.METAL, Tag.DND)

  @Before
  fun setup() {
    repository = FakeUserRepository()
    tagRepository = TagLocalTemporaryRepository()
    viewModelUser = SelectTagViewModel(repository)
    viewModelEvent = SelectTagViewModel(repository, tagRepository)
    viewModelEvent.mode = SelectTagMode.EVENT_CREATION
    viewModelEvent.eventTagRepositoryObserving()
  }

  @Test
  fun uiStateTags_initiallyEmpty() = runTest {
    // Check that the selected tags are initially empty.
    val state = viewModelUser.selectedTags.value
    assertEquals(emptyList<Tag>(), state)
  }

  @Test
  fun addTag_addsMusicTagSuccessfully() = runTest {
    // Check that the uiStateTag changes when we add a tag.
    val tag = Tag.MUSIC
    viewModelUser.addTag(tag)
    val state = viewModelUser.selectedTags.value
    assertEquals(listOf(Tag.MUSIC), state)
  }

  @Test
  fun addTag_addsSportTagSuccessfully() = runTest {
    // Check that the uiStateTag changes when we add a tag.
    val tag = Tag.JUDO
    viewModelUser.addTag(tag)
    val state = viewModelUser.selectedTags.value
    assertEquals(listOf(Tag.JUDO), state)
  }

  @Test
  fun addTag_addsFoodTagSuccessfully() = runTest {
    // Check that the uiStateTag changes when we add a tag.
    val tag = Tag.BAKING
    viewModelUser.addTag(tag)
    val state = viewModelUser.selectedTags.value
    assertEquals(listOf(Tag.BAKING), state)
  }

  @Test
  fun addTag_addsArtTagSuccessfully() = runTest {
    // Check that the uiStateTag changes when we add a tag.
    val tag = Tag.LITERATURE
    viewModelUser.addTag(tag)
    val state = viewModelUser.selectedTags.value
    assertEquals(listOf(Tag.LITERATURE), state)
  }

  @Test
  fun addTag_addsTravelTagSuccessfully() = runTest {
    // Check that the uiStateTag changes when we add a tag.
    val tag = Tag.GROUP_TRAVEL
    viewModelUser.addTag(tag)
    val state = viewModelUser.selectedTags.value
    assertEquals(listOf(Tag.GROUP_TRAVEL), state)
  }

  @Test
  fun addTag_addsGamesTagSuccessfully() = runTest {
    // Check that the uiStateTag changes when we add a tag.
    val tag = Tag.DND
    viewModelUser.addTag(tag)
    val state = viewModelUser.selectedTags.value
    assertEquals(listOf(Tag.DND), state)
  }

  @Test
  fun addTag_addsTechnologyTagSuccessfully() = runTest {
    // Check that the uiStateTag changes when we add a tag.
    val tag = Tag.MACHINE_LEARNING
    viewModelUser.addTag(tag)
    val state = viewModelUser.selectedTags.value
    assertEquals(listOf(Tag.MACHINE_LEARNING), state)
  }

  @Test
  fun addTag_addsTopicTagSuccessfully() = runTest {
    // Check that the uiStateTag changes when we add a tag.
    val tag = Tag.PHYSICS
    viewModelUser.addTag(tag)
    val state = viewModelUser.selectedTags.value
    assertEquals(listOf(Tag.PHYSICS), state)
  }

  @Test
  fun addTag_addsMultipleTagsSuccessfully() {
    // Check that the uiStateTag changes when we add multiple tags.
    val tags =
        listOf(
            Tag.SAFARI,
            Tag.BAKING,
            Tag.KARATE,
            Tag.CYCLING,
            Tag.METAL,
            Tag.ROCK,
            Tag.JUDO,
            Tag.HANDBALL,
            Tag.MUSIC,
            Tag.DND)
    for (tag in tags) {
      viewModelUser.addTag(tag)
    }
    val state = viewModelUser.selectedTags.value
    assertEquals(tags, state)
  }

  @Test
  fun addTag_throwsExceptionWhenTagAlreadySelected() = runTest {
    // Check that the function addTag throws an exception when we select a tag that is already
    // selected.
    val tag = Tag.METAL
    viewModelUser.addTag(tag)
    try {
      viewModelUser.addTag(tag)
      assert(false) { "Expected an exception" }
    } catch (e: IllegalArgumentException) {
      assert(true)
    } catch (e: Exception) {
      assert(false) { "Expected another exception type" }
    }
  }

  @Test
  fun deleteTag_removesMusicTagSuccessfully() = runTest {
    // Check that the uiStateTag changes when we remove a tag.
    val tag = Tag.METAL
    viewModelUser.addTag(tag)
    viewModelUser.deleteTag(tag)
    val state = viewModelUser.selectedTags.value
    assertEquals(emptyList<Tag>(), state)
  }

  @Test
  fun deleteTag_removesSportTagSuccessfully() = runTest {
    // Check that the uiStateTag changes when we remove a tag.
    val tag = Tag.JUDO
    viewModelUser.addTag(tag)
    viewModelUser.deleteTag(tag)
    val state = viewModelUser.selectedTags.value
    assertEquals(emptyList<Tag>(), state)
  }

  @Test
  fun deleteTag_removesFoodTagSuccessfully() = runTest {
    // Check that the uiStateTag changes when we remove a tag.
    val tag = Tag.BAKING
    viewModelUser.addTag(tag)
    viewModelUser.deleteTag(tag)
    val state = viewModelUser.selectedTags.value
    assertEquals(emptyList<Tag>(), state)
  }

  @Test
  fun deleteTag_removesArtTagSuccessfully() = runTest {
    // Check that the uiStateTag changes when we remove a tag.
    val tag = Tag.LITERATURE
    viewModelUser.addTag(tag)
    viewModelUser.deleteTag(tag)
    val state = viewModelUser.selectedTags.value
    assertEquals(emptyList<Tag>(), state)
  }

  @Test
  fun deleteTag_removesTravelTagSuccessfully() = runTest {
    // Check that the uiStateTag changes when we remove a tag.
    val tag = Tag.SAFARI
    viewModelUser.addTag(tag)
    viewModelUser.deleteTag(tag)
    val state = viewModelUser.selectedTags.value
    assertEquals(emptyList<Tag>(), state)
  }

  @Test
  fun deleteTag_removesGamesTagSuccessfully() = runTest {
    // Check that the uiStateTag changes when we remove a tag.
    val tag = Tag.DND
    viewModelUser.addTag(tag)
    viewModelUser.deleteTag(tag)
    val state = viewModelUser.selectedTags.value
    assertEquals(emptyList<Tag>(), state)
  }

  @Test
  fun deleteTag_removesTechnologyTagSuccessfully() = runTest {
    // Check that the uiStateTag changes when we remove a tag.
    val tag = Tag.MACHINE_LEARNING
    viewModelUser.addTag(tag)
    viewModelUser.deleteTag(tag)
    val state = viewModelUser.selectedTags.value
    assertEquals(emptyList<Tag>(), state)
  }

  @Test
  fun deleteTag_removesTopicTagSuccessfully() = runTest {
    // Check that the uiStateTag changes when we remove a tag.
    val tag = Tag.PHYSICS
    viewModelUser.addTag(tag)
    viewModelUser.deleteTag(tag)
    val state = viewModelUser.selectedTags.value
    assertEquals(emptyList<Tag>(), state)
  }

  @Test
  fun deleteTag_throwsExceptionWhenTagNotSelected() = runTest {
    // Check that the function deleteTag throws an exception when we deselect a tag that is not
    // already selected.
    val tag = Tag.METAL
    try {
      viewModelUser.deleteTag(tag)
      assert(false) { "Expected an exception" }
    } catch (e: IllegalArgumentException) {
      assert(true)
    } catch (e: Exception) {
      assert(false) { "Expected another exception type" }
    }
  }

  @Test
  fun deleteTag_removesMultipleTagsSuccessfully() = runTest {
    // Check that the uiStateTag change when we remove multiple tag.
    val tags =
        listOf(
            Tag.SAFARI,
            Tag.BAKING,
            Tag.KARATE,
            Tag.CYCLING,
            Tag.METAL,
            Tag.ROCK,
            Tag.JUDO,
            Tag.HANDBALL,
            Tag.MUSIC,
            Tag.DND)
    for (tag in tags) {
      viewModelUser.addTag(tag)
    }
    for (tag in tags) {
      viewModelUser.deleteTag(tag)
    }
    val state = viewModelUser.selectedTags.value
    assertEquals(emptyList<Tag>(), state)
  }

  @Test
  fun addTagAndDeleteTag_maintainsCorrectOrder() = runTest {
    // Check that if we select tags and deselect them, the selected tags remain in the correct
    // order, matching the sequence they
    // were clicked.
    val tags =
        listOf(
            Tag.SAFARI,
            Tag.BAKING,
            Tag.KARATE,
            Tag.CYCLING,
            Tag.METAL,
            Tag.ROCK,
            Tag.JUDO,
            Tag.HANDBALL,
            Tag.MUSIC,
            Tag.DND)
    for (tag in tags) {
      viewModelUser.addTag(tag)
    }
    viewModelUser.deleteTag(Tag.BAKING)
    viewModelUser.deleteTag(Tag.CYCLING)
    viewModelUser.deleteTag(Tag.KARATE)

    val expectedTags =
        listOf(Tag.SAFARI, Tag.METAL, Tag.ROCK, Tag.JUDO, Tag.HANDBALL, Tag.MUSIC, Tag.DND)
    val state = viewModelUser.selectedTags.value
    assertEquals(expectedTags, state)
  }

  @Test
  fun loadTags_loadsUserTagsCorrectly() = runTest {
    // Check that loading user tags, update the uiStateTags value.
    val userProfile =
        UserProfile(
            uid = "0",
            username = "Jacquie",
            firstName = "Bob",
            lastName = "Maurice",
            country = "France",
            description = null,
            dateOfBirth = LocalDate.of(2000, 8, 11),
            tags = setOf(Tag.METAL, Tag.KARATE))
    repository.addUser(userProfile)
    advanceUntilIdle()
    viewModelUser.loadTags("0")
    advanceUntilIdle()
    assertEquals(listOf(Tag.METAL, Tag.KARATE), viewModelUser.selectedTags.value)
  }

  @Test
  fun deleteTag_maintainsCorrectOrder() = runTest {
    // Check that if we select tags and deselect them, the selected tags remain in the correct
    // order, matching the sequence they
    // were clicked.
    viewModelUser.addTag(Tag.METAL)
    viewModelUser.addTag(Tag.HANDBALL)
    viewModelUser.addTag(Tag.KARATE)
    viewModelUser.deleteTag(Tag.HANDBALL)

    assertEquals(listOf(Tag.METAL, Tag.KARATE), viewModelUser.selectedTags.value)
  }

  @Test
  fun saveTags_savesAllSelectedTagsToUserProfile() = runTest {
    // Check that the saving of the tags, change the tags of the userProfile.
    val userProfile =
        UserProfile(
            uid = "0",
            username = "Jacquie",
            firstName = "Bob",
            lastName = "Maurice",
            country = "France",
            description = null,
            dateOfBirth = LocalDate.of(2000, 8, 11),
            tags = emptySet())
    repository.addUser(userProfile)
    advanceUntilIdle()
    val tags =
        listOf(
            Tag.SAFARI,
            Tag.BAKING,
            Tag.KARATE,
            Tag.CYCLING,
            Tag.METAL,
            Tag.ROCK,
            Tag.JUDO,
            Tag.HANDBALL,
            Tag.MUSIC,
            Tag.DND)
    for (tag in tags) {
      viewModelUser.addTag(tag)
    }
    viewModelUser.saveTags("0")
    advanceUntilIdle()
    val expectedTags =
        setOf(
            Tag.SAFARI,
            Tag.BAKING,
            Tag.KARATE,
            Tag.CYCLING,
            Tag.METAL,
            Tag.ROCK,
            Tag.JUDO,
            Tag.HANDBALL,
            Tag.MUSIC,
            Tag.DND)
    val expectedUserProfile =
        UserProfile(
            uid = "0",
            username = "Jacquie",
            firstName = "Bob",
            lastName = "Maurice",
            country = "France",
            description = null,
            dateOfBirth = LocalDate.of(2000, 8, 11),
            tags = expectedTags)
    val actual = repository.getUser("0")
    advanceUntilIdle()
    assertEquals(expectedUserProfile, actual)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun loadThenAddThenSave_preservesExistingTagsAndAddsNewTag() = runTest {
    // Check that if we save, the tags that were already in the userProfile remains.
    val userProfile =
        UserProfile(
            uid = "0",
            username = "Jacquie",
            firstName = "Bob",
            lastName = "Maurice",
            country = "France",
            description = null,
            dateOfBirth = LocalDate.of(2000, 8, 11),
            tags = setOf(Tag.METAL, Tag.KARATE))
    repository.addUser(userProfile)
    advanceUntilIdle()
    viewModelUser.loadTags("0")
    advanceUntilIdle()
    viewModelUser.addTag(Tag.HANDBALL)
    viewModelUser.saveTags("0")
    advanceUntilIdle()
    val updatedUser = repository.getUser("0")
    advanceUntilIdle()
    assertEquals(setOf(Tag.METAL, Tag.KARATE, Tag.HANDBALL), updatedUser.tags)
  }

  @Test
  fun loadTagsEventCreation() = runTest {
    tagRepository.updateTags(tags)
    advanceUntilIdle()
    viewModelEvent.loadTags("0")
    val resultTags = viewModelEvent.selectedTags.value.toSet()
    assertEquals(tags, resultTags)
  }

  @Test
  fun loadTagsWhenRepositoryChange() = runTest {
    tagRepository.updateTags(tags)
    advanceUntilIdle()
    val resultTags = viewModelEvent.selectedTags.value.toSet()
    assertEquals(tags, resultTags)
  }

  @Test
  fun saveTagsEventCreation() = runTest {
    tags.forEach { tag -> viewModelEvent.addTag(tag) }
    viewModelEvent.saveTags("0")
    advanceUntilIdle()
    val resultTags = tagRepository.getTags()
    assertEquals(tags, resultTags)
  }
}
