package com.android.universe.ui.selectTag

import com.android.universe.model.Tag
import com.android.universe.model.user.FakeUserRepository
import com.android.universe.model.user.UserProfile
import java.time.LocalDate
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class SelectTagViewModelTest {
  // Define the parameters for the tests.
  private lateinit var repository: FakeUserRepository
  private lateinit var viewModel: SelectTagViewModel
  private val testDispatcher = StandardTestDispatcher()

  @Before
  fun setup() {
    repository = FakeUserRepository()
    viewModel = SelectTagViewModel(repository, testDispatcher)
  }

  @Test
  fun uiStateTags_initiallyEmpty() = runTest {
    // Check that the selected tags are initially empty.
    val state = viewModel.uiStateTags.value
    assertEquals(emptyList<String>(), state)
  }

  @Test
  fun addTag_addsInterestTagSuccessfully() = runTest {
    // Check that the uiStateTag changes when we add a tag.
    val tag = "Music"
    viewModel.addTag(tag)
    val state = viewModel.uiStateTags.value
    assertEquals(listOf("Music"), state)
  }

  @Test
  fun addTag_addsSportTagSuccessfully() = runTest {
    // Check that the uiStateTag changes when we add a tag.
    val tag = "Judo"
    viewModel.addTag(tag)
    val state = viewModel.uiStateTags.value
    assertEquals(listOf("Judo"), state)
  }

  @Test
  fun addTag_addsMusicTagSuccessfully() = runTest {
    // Check that the uiStateTag changes when we add a tag.
    val tag = "Metal"
    viewModel.addTag(tag)
    val state = viewModel.uiStateTags.value
    assertEquals(listOf("Metal"), state)
  }

  @Test
  fun addTag_addsTransportTagSuccessfully() = runTest {
    // Check that the uiStateTag changes when we add a tag.
    val tag = "Boat"
    viewModel.addTag(tag)
    val state = viewModel.uiStateTags.value
    assertEquals(listOf("Boat"), state)
  }

  @Test
  fun addTag_addsCantonTagSuccessfully() = runTest {
    // Check that the uiStateTag changes when we add a tag.
    val tag = "Geneva"
    viewModel.addTag(tag)
    val state = viewModel.uiStateTags.value
    assertEquals(listOf("Geneva"), state)
  }

  @Test
  fun addTag_addsMultipleTagsSuccessfully() {
    // Check that the uiStateTag changes when we add multiple tags.
    val tags =
        listOf(
            "Geneva",
            "Aargau",
            "Car",
            "Bicycle",
            "Metal",
            "Rock",
            "Judo",
            "Handball",
            "Music",
            "Role-playing games")
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
    val tag = "Metal"
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
    val tag = "Music"
    viewModel.addTag(tag)
    viewModel.deleteTag(tag)
    val state = viewModel.uiStateTags.value
    assertEquals(emptyList<String>(), state)
  }

  @Test
  fun deleteTag_removesSportTagSuccessfully() = runTest {
    // Check that the uiStateTag changes when we remove a tag.
    val tag = "Judo"
    viewModel.addTag(tag)
    viewModel.deleteTag(tag)
    val state = viewModel.uiStateTags.value
    assertEquals(emptyList<String>(), state)
  }

  @Test
  fun deleteTag_removesMusicTagSuccessfully() = runTest {
    // Check that the uiStateTag changes when we remove a tag.
    val tag = "Metal"
    viewModel.addTag(tag)
    viewModel.deleteTag(tag)
    val state = viewModel.uiStateTags.value
    assertEquals(emptyList<String>(), state)
  }

  @Test
  fun deleteTag_removesTransportTagSuccessfully() = runTest {
    // Check that the uiStateTag changes when we remove a tag.
    val tag = "Boat"
    viewModel.addTag(tag)
    viewModel.deleteTag(tag)
    val state = viewModel.uiStateTags.value
    assertEquals(emptyList<String>(), state)
  }

  @Test
  fun deleteTag_removesCantonTagSuccessfully() = runTest {
    // Check that the uiStateTag changes when we remove a tag.
    val tag = "Geneva"
    viewModel.addTag(tag)
    viewModel.deleteTag(tag)
    val state = viewModel.uiStateTags.value
    assertEquals(emptyList<String>(), state)
  }

  @Test
  fun deleteTag_throwsExceptionWhenTagNotSelected() = runTest {
    // Check that the function deleteTag throws an exception when we deselect a tag that is not
    // already selected.
    val tag = "Metal"
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
            "Geneva",
            "Aargau",
            "Car",
            "Bicycle",
            "Metal",
            "Rock",
            "Judo",
            "Handball",
            "Music",
            "Role-playing games")
    for (tag in tags) {
      viewModel.addTag(tag)
    }
    for (tag in tags) {
      viewModel.deleteTag(tag)
    }
    val state = viewModel.uiStateTags.value
    assertEquals(emptyList<String>(), state)
  }

  @Test
  fun addTagAndDeleteTag_maintainsCorrectOrder() = runTest {
    // Check that if we select tags and deselect them, the selected tags remain in the correct
    // order, matching the sequence they
    // were clicked.
    val tags =
        listOf(
            "Geneva",
            "Aargau",
            "Car",
            "Bicycle",
            "Metal",
            "Rock",
            "Judo",
            "Handball",
            "Music",
            "Role-playing games")
    for (tag in tags) {
      viewModel.addTag(tag)
    }
    viewModel.deleteTag("Aargau")
    viewModel.deleteTag("Bicycle")
    viewModel.deleteTag("Car")

    val expectedTags =
        listOf("Geneva", "Metal", "Rock", "Judo", "Handball", "Music", "Role-playing games")
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
            tags = listOf(Tag("Metal"), Tag("Car")))
    repository.addUser(userProfile)

    viewModel.loadTags("Jacquie")
    testDispatcher.scheduler.advanceUntilIdle() // Wait for the loading.
    assertEquals(listOf("Metal", "Car"), viewModel.uiStateTags.value)
  }

  @Test
  fun deleteTag_maintainsCorrectOrder() = runTest {
    // Check that if we select tags and deselect them, the selected tags remain in the correct
    // order, matching the sequence they
    // were clicked.
    viewModel.addTag("Metal")
    viewModel.addTag("Handball")
    viewModel.addTag("Car")
    viewModel.deleteTag("Handball")

    assertEquals(listOf("Metal", "Car"), viewModel.uiStateTags.value)
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
            "Geneva",
            "Aargau",
            "Car",
            "Bicycle",
            "Metal",
            "Rock",
            "Judo",
            "Handball",
            "Music",
            "Role-playing games")
    for (tag in tags) {
      viewModel.addTag(tag)
    }
    viewModel.saveTags("Jacquie")
    testDispatcher.scheduler.advanceUntilIdle() // Wait for the saving.
    val expectedTags =
        listOf(
            Tag("Geneva"),
            Tag("Aargau"),
            Tag("Car"),
            Tag("Bicycle"),
            Tag("Metal"),
            Tag("Rock"),
            Tag("Judo"),
            Tag("Handball"),
            Tag("Music"),
            Tag("Role-playing games"))
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
            tags = listOf(Tag("Metal"), Tag("Car")))
    repository.addUser(userProfile)
    viewModel.loadTags("Jacquie")
    testDispatcher.scheduler.advanceUntilIdle() // Wait for the loading.

    viewModel.addTag("Handball")
    viewModel.saveTags("Jacquie")
    testDispatcher.scheduler.advanceUntilIdle() // Wait for the saving.

    val updatedUser = repository.getUser("Jacquie")
    assertEquals(listOf(Tag("Metal"), Tag("Car"), Tag("Handball")), updatedUser.tags)
  }
}
