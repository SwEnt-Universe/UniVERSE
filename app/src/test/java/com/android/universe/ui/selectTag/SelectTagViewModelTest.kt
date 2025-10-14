package com.android.universe.ui.selectTag

import com.android.universe.model.Tag
import com.android.universe.model.user.FakeUserRepository
import com.android.universe.model.user.UserProfile
import java.time.LocalDate
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class SelectTagViewModelTest {
  // Define the parameters for the tests.
  private lateinit var repository: FakeUserRepository
  private lateinit var viewModel: SelectTagViewModel

  @Before
  fun setup() {
    repository = FakeUserRepository()
    viewModel = SelectTagViewModel(repository)
  }

  @Test
  fun uiStateTags_initiallyEmpty() = runTest {
    // Check that the selected tags are initially empty.
    val state = viewModel.uiStateTags.value
    assertEquals(emptyList<Tag>(), state)
  }

  @Test
  fun addTag_addsInterestTagSuccessfully() = runTest {
    // Check that the uiStateTag changes when we add a tag.
    val tag = Tag.MUSIC
    viewModel.addTag(tag)
    val state = viewModel.uiStateTags.value
    assertEquals(listOf(Tag.MUSIC), state)
  }

  @Test
  fun addTag_addsSportTagSuccessfully() = runTest {
    // Check that the uiStateTag changes when we add a tag.
    val tag = Tag.JUDO
    viewModel.addTag(tag)
    val state = viewModel.uiStateTags.value
    assertEquals(listOf(Tag.JUDO), state)
  }

  @Test
  fun addTag_addsMusicTagSuccessfully() = runTest {
    // Check that the uiStateTag changes when we add a tag.
    val tag = Tag.METAL
    viewModel.addTag(tag)
    val state = viewModel.uiStateTags.value
    assertEquals(listOf(Tag.METAL), state)
  }

  @Test
  fun addTag_addsTransportTagSuccessfully() = runTest {
    // Check that the uiStateTag changes when we add a tag.
    val tag = Tag.BOAT
    viewModel.addTag(tag)
    val state = viewModel.uiStateTags.value
    assertEquals(listOf(Tag.BOAT), state)
  }

  @Test
  fun addTag_addsCantonTagSuccessfully() = runTest {
    // Check that the uiStateTag changes when we add a tag.
    val tag = Tag.GENEVA
    viewModel.addTag(tag)
    val state = viewModel.uiStateTags.value
    assertEquals(listOf(Tag.GENEVA), state)
  }

  @Test
  fun addTag_addsMultipleTagsSuccessfully() {
    // Check that the uiStateTag changes when we add multiple tags.
    val tags =
        listOf(
            Tag.GENEVA,
            Tag.AARGAU,
            Tag.CAR,
            Tag.BICYCLE,
            Tag.METAL,
            Tag.ROCK,
            Tag.JUDO,
            Tag.HANDBALL,
            Tag.MUSIC,
            Tag.ROLE_PLAYING_GAMES)
    for (tag in tags) {
      viewModel.addTag(tag)
    }
    val state = viewModel.uiStateTags.value
    assertEquals(tags, state)
  }

  @Test
  fun addTag_throwsExceptionWhenTagAlreadySelected() = runTest {
    // Check that the function addTag throws an exception when we select a tag that is already
    // selected.
    val tag = Tag.METAL
    viewModel.addTag(tag)
    try {
      viewModel.addTag(tag)
      assert(false) { "Expected an exception" }
    } catch (e: IllegalArgumentException) {
      assert(true)
    } catch (e: Exception) {
      assert(false) { "Expected another exception type" }
    }
  }

  @Test
  fun deleteTag_removesInterestTagSuccessfully() = runTest {
    // Check that the uiStateTag changes when we remove a tag.
    val tag = Tag.MUSIC
    viewModel.addTag(tag)
    viewModel.deleteTag(tag)
    val state = viewModel.uiStateTags.value
    assertEquals(emptyList<Tag>(), state)
  }

  @Test
  fun deleteTag_removesSportTagSuccessfully() = runTest {
    // Check that the uiStateTag changes when we remove a tag.
    val tag = Tag.JUDO
    viewModel.addTag(tag)
    viewModel.deleteTag(tag)
    val state = viewModel.uiStateTags.value
    assertEquals(emptyList<Tag>(), state)
  }

  @Test
  fun deleteTag_removesMusicTagSuccessfully() = runTest {
    // Check that the uiStateTag changes when we remove a tag.
    val tag = Tag.METAL
    viewModel.addTag(tag)
    viewModel.deleteTag(tag)
    val state = viewModel.uiStateTags.value
    assertEquals(emptyList<Tag>(), state)
  }

  @Test
  fun deleteTag_removesTransportTagSuccessfully() = runTest {
    // Check that the uiStateTag changes when we remove a tag.
    val tag = Tag.BOAT
    viewModel.addTag(tag)
    viewModel.deleteTag(tag)
    val state = viewModel.uiStateTags.value
    assertEquals(emptyList<Tag>(), state)
  }

  @Test
  fun deleteTag_removesCantonTagSuccessfully() = runTest {
    // Check that the uiStateTag changes when we remove a tag.
    val tag = Tag.GENEVA
    viewModel.addTag(tag)
    viewModel.deleteTag(tag)
    val state = viewModel.uiStateTags.value
    assertEquals(emptyList<Tag>(), state)
  }

  @Test
  fun deleteTag_throwsExceptionWhenTagNotSelected() = runTest {
    // Check that the function deleteTag throws an exception when we deselect a tag that is not
    // already selected.
    val tag = Tag.METAL
    try {
      viewModel.deleteTag(tag)
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
            Tag.GENEVA,
            Tag.AARGAU,
            Tag.CAR,
            Tag.BICYCLE,
            Tag.METAL,
            Tag.ROCK,
            Tag.JUDO,
            Tag.HANDBALL,
            Tag.MUSIC,
            Tag.ROLE_PLAYING_GAMES)
    for (tag in tags) {
      viewModel.addTag(tag)
    }
    for (tag in tags) {
      viewModel.deleteTag(tag)
    }
    val state = viewModel.uiStateTags.value
    assertEquals(emptyList<Tag>(), state)
  }

  @Test
  fun addTagAndDeleteTag_maintainsCorrectOrder() = runTest {
    // Check that if we select tags and deselect them, the selected tags remain in the correct
    // order, matching the sequence they
    // were clicked.
    val tags =
        listOf(
            Tag.GENEVA,
            Tag.AARGAU,
            Tag.CAR,
            Tag.BICYCLE,
            Tag.METAL,
            Tag.ROCK,
            Tag.JUDO,
            Tag.HANDBALL,
            Tag.MUSIC,
            Tag.ROLE_PLAYING_GAMES)
    for (tag in tags) {
      viewModel.addTag(tag)
    }
    viewModel.deleteTag(Tag.AARGAU)
    viewModel.deleteTag(Tag.BICYCLE)
    viewModel.deleteTag(Tag.CAR)

    val expectedTags =
        listOf(
            Tag.GENEVA,
            Tag.METAL,
            Tag.ROCK,
            Tag.JUDO,
            Tag.HANDBALL,
            Tag.MUSIC,
            Tag.ROLE_PLAYING_GAMES)
    val state = viewModel.uiStateTags.value
    assertEquals(expectedTags, state)
  }

  @Test
  fun loadTags_loadsUserTagsCorrectly() = runTest {
    // Check that loading user tags, update the uiStateTags value.
    val userProfile =
        UserProfile(
            username = "Jacquie",
            firstName = "Bob",
            lastName = "Maurice",
            country = "France",
            description = null,
            dateOfBirth = LocalDate.of(2000, 8, 11),
            tags = listOf(Tag.METAL, Tag.CAR))
    repository.addUser(userProfile)

    viewModel.loadTags("Jacquie")
    assertEquals(listOf(Tag.METAL, Tag.CAR), viewModel.uiStateTags.value)
  }

  @Test
  fun deleteTag_maintainsCorrectOrder() = runTest {
    // Check that if we select tags and deselect them, the selected tags remain in the correct
    // order, matching the sequence they
    // were clicked.
    viewModel.addTag(Tag.METAL)
    viewModel.addTag(Tag.HANDBALL)
    viewModel.addTag(Tag.CAR)
    viewModel.deleteTag(Tag.HANDBALL)

    assertEquals(listOf(Tag.METAL, Tag.CAR), viewModel.uiStateTags.value)
  }

  @Test
  fun saveTags_savesAllSelectedTagsToUserProfile() = runTest {
    // Check that the saving of the tags, change the tags of the userProfile.
    val userProfile =
        UserProfile(
            username = "Jacquie",
            firstName = "Bob",
            lastName = "Maurice",
            country = "France",
            description = null,
            dateOfBirth = LocalDate.of(2000, 8, 11),
            tags = emptyList())
    repository.addUser(userProfile)
    val tags =
        listOf(
            Tag.GENEVA,
            Tag.AARGAU,
            Tag.CAR,
            Tag.BICYCLE,
            Tag.METAL,
            Tag.ROCK,
            Tag.JUDO,
            Tag.HANDBALL,
            Tag.MUSIC,
            Tag.ROLE_PLAYING_GAMES)
    for (tag in tags) {
      viewModel.addTag(tag)
    }
    viewModel.saveTags("Jacquie")
    val expectedTags =
        listOf(
            Tag.GENEVA,
            Tag.AARGAU,
            Tag.CAR,
            Tag.BICYCLE,
            Tag.METAL,
            Tag.ROCK,
            Tag.JUDO,
            Tag.HANDBALL,
            Tag.MUSIC,
            Tag.ROLE_PLAYING_GAMES)
    val expectedUserProfile =
        UserProfile(
            username = "Jacquie",
            firstName = "Bob",
            lastName = "Maurice",
            country = "France",
            description = null,
            dateOfBirth = LocalDate.of(2000, 8, 11),
            tags = expectedTags)
    assertEquals(expectedUserProfile, repository.getUser("Jacquie"))
  }

  @Test
  fun loadThenAddThenSave_preservesExistingTagsAndAddsNewTag() = runTest {
    // Check that if we save, the tags that were already in the userProfile remains.
    val userProfile =
        UserProfile(
            username = "Jacquie",
            firstName = "Bob",
            lastName = "Maurice",
            country = "France",
            description = null,
            dateOfBirth = LocalDate.of(2000, 8, 11),
            tags = listOf(Tag.METAL, Tag.CAR))
    repository.addUser(userProfile)
    viewModel.loadTags("Jacquie")

    viewModel.addTag(Tag.HANDBALL)
    viewModel.saveTags("Jacquie")

    val updatedUser = repository.getUser("Jacquie")
    assertEquals(listOf(Tag.METAL, Tag.CAR, Tag.HANDBALL), updatedUser.tags)
  }
}
