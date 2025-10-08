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
    val state = viewModel.uiStateTags.value
    assertEquals(emptyList<String>(), state)
  }

  @Test
  fun addTag_addsInterestTagSuccessfully() = runTest {
    val tag = "Music"
    viewModel.addTag(tag)
    val state = viewModel.uiStateTags.value
    assertEquals(listOf("Music"), state)
  }

  @Test
  fun addTag_addsSportTagSuccessfully() = runTest {
    val tag = "Judo"
    viewModel.addTag(tag)
    val state = viewModel.uiStateTags.value
    assertEquals(listOf("Judo"), state)
  }

  @Test
  fun addTag_addsMusicTagSuccessfully() = runTest {
    val tag = "Metal"
    viewModel.addTag(tag)
    val state = viewModel.uiStateTags.value
    assertEquals(listOf("Metal"), state)
  }

  @Test
  fun addTag_addsTransportTagSuccessfully() = runTest {
    val tag = "Boat"
    viewModel.addTag(tag)
    val state = viewModel.uiStateTags.value
    assertEquals(listOf("Boat"), state)
  }

  @Test
  fun addTag_addsCantonTagSuccessfully() = runTest {
    val tag = "Geneva"
    viewModel.addTag(tag)
    val state = viewModel.uiStateTags.value
    assertEquals(listOf("Geneva"), state)
  }

  @Test
  fun addTag_addsMultipleTagsSuccessfully() {
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
    val tag = "Music"
    viewModel.addTag(tag)
    viewModel.deleteTag(tag)
    val state = viewModel.uiStateTags.value
    assertEquals(emptyList<String>(), state)
  }

  @Test
  fun deleteTag_removesSportTagSuccessfully() = runTest {
    val tag = "Judo"
    viewModel.addTag(tag)
    viewModel.deleteTag(tag)
    val state = viewModel.uiStateTags.value
    assertEquals(emptyList<String>(), state)
  }

  @Test
  fun deleteTag_removesMusicTagSuccessfully() = runTest {
    val tag = "Metal"
    viewModel.addTag(tag)
    viewModel.deleteTag(tag)
    val state = viewModel.uiStateTags.value
    assertEquals(emptyList<String>(), state)
  }

  @Test
  fun deleteTag_removesTransportTagSuccessfully() = runTest {
    val tag = "Boat"
    viewModel.addTag(tag)
    viewModel.deleteTag(tag)
    val state = viewModel.uiStateTags.value
    assertEquals(emptyList<String>(), state)
  }

  @Test
  fun deleteTag_removesCantonTagSuccessfully() = runTest {
    val tag = "Geneva"
    viewModel.addTag(tag)
    viewModel.deleteTag(tag)
    val state = viewModel.uiStateTags.value
    assertEquals(emptyList<String>(), state)
  }

  @Test
  fun deleteTag_throwsExceptionWhenTagNotSelected() = runTest {
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
    testDispatcher.scheduler.advanceUntilIdle()
    assertEquals(listOf("Metal", "Car"), viewModel.uiStateTags.value)
  }

  @Test
  fun deleteTag_maintainsCorrectOrder() = runTest {
    viewModel.addTag("Metal")
    viewModel.addTag("Handball")
    viewModel.addTag("Car")
    viewModel.deleteTag("Handball")

    assertEquals(listOf("Metal", "Car"), viewModel.uiStateTags.value)
  }

  @Test
  fun saveTags_savesAllSelectedTagsToUserProfile() = runTest {
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
    testDispatcher.scheduler.advanceUntilIdle()
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
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.addTag("Handball")
    viewModel.saveTags("Jacquie")
    testDispatcher.scheduler.advanceUntilIdle()

    val updatedUser = repository.getUser("Jacquie")
    assertEquals(listOf(Tag("Metal"), Tag("Car"), Tag("Handball")), updatedUser.tags)
  }
}
