package com.android.universe.ui.selectTag

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.model.event.Event
import com.android.universe.model.event.EventLocalTemporaryRepository
import com.android.universe.model.event.EventRepository
import com.android.universe.model.event.EventTemporaryRepository
import com.android.universe.model.event.FakeEventRepository
import com.android.universe.model.location.Location
import com.android.universe.model.tag.Tag
import com.android.universe.model.tag.TagLocalTemporaryRepository
import com.android.universe.model.tag.TagTemporaryRepository
import com.android.universe.model.user.FakeUserRepository
import com.android.universe.model.user.UserProfile
import com.android.universe.utils.MainCoroutineRule
import com.android.universe.utils.UserTestData
import java.time.LocalDate
import java.time.LocalDateTime
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

  private lateinit var userRepository: FakeUserRepository
  private lateinit var tagRepository: TagTemporaryRepository
  private lateinit var eventTemporaryRepository: EventTemporaryRepository
  private lateinit var eventRepository: EventRepository
  private lateinit var viewModel: SelectTagViewModel
  private lateinit var defaultUser: UserProfile

  val tags = setOf(Tag.HANDBALL, Tag.METAL, Tag.DND)

  object SelectTagViewModelTestValues {
    val temporaryEvent =
        Event(
            id = "test",
            title = "Title",
            description = "description",
            date = LocalDateTime.now(),
            tags = emptySet(),
            creator = "Test user",
            participants = setOf("Test user"),
            location = Location(0.0, 0.0),
            eventPicture = null)
  }

  fun setUpViewmodel(uid: String, mode: SelectTagMode) {
    viewModel =
        SelectTagViewModel(
            uid = uid,
            mode = mode,
            userRepository = userRepository,
            tagRepository = tagRepository,
            eventTemporaryRepository = eventTemporaryRepository,
            eventRepository = eventRepository)
  }

  @Before
  fun setup() = runTest {
    userRepository = FakeUserRepository()
    tagRepository = TagLocalTemporaryRepository()
    eventTemporaryRepository = EventLocalTemporaryRepository()
    eventRepository = FakeEventRepository()
    defaultUser = UserTestData.NoTagsUser
    userRepository.addUser(defaultUser)
    setUpViewmodel(defaultUser.uid, SelectTagMode.USER_PROFILE)
  }

  @Test
  fun uiStateTags_initiallyEmpty() = runTest {
    // Check that the selected tags are initially empty.
    val state = viewModel.selectedTags.value
    assertEquals(emptyList<Tag>(), state)
  }

  @Test
  fun addTag_addsMusicTagSuccessfully() = runTest {
    // Check that the uiStateTag changes when we add a tag.
    val tag = Tag.MUSIC
    viewModel.addTag(tag)
    val state = viewModel.selectedTags.value
    assertEquals(listOf(Tag.MUSIC), state)
  }

  @Test
  fun addTag_addsSportTagSuccessfully() = runTest {
    // Check that the uiStateTag changes when we add a tag.
    val tag = Tag.JUDO
    viewModel.addTag(tag)
    val state = viewModel.selectedTags.value
    assertEquals(listOf(Tag.JUDO), state)
  }

  @Test
  fun addTag_addsFoodTagSuccessfully() = runTest {
    // Check that the uiStateTag changes when we add a tag.
    val tag = Tag.BAKING
    viewModel.addTag(tag)
    val state = viewModel.selectedTags.value
    assertEquals(listOf(Tag.BAKING), state)
  }

  @Test
  fun addTag_addsArtTagSuccessfully() = runTest {
    // Check that the uiStateTag changes when we add a tag.
    val tag = Tag.LITERATURE
    viewModel.addTag(tag)
    val state = viewModel.selectedTags.value
    assertEquals(listOf(Tag.LITERATURE), state)
  }

  @Test
  fun addTag_addsTravelTagSuccessfully() = runTest {
    // Check that the uiStateTag changes when we add a tag.
    val tag = Tag.GROUP_TRAVEL
    viewModel.addTag(tag)
    val state = viewModel.selectedTags.value
    assertEquals(listOf(Tag.GROUP_TRAVEL), state)
  }

  @Test
  fun addTag_addsGamesTagSuccessfully() = runTest {
    // Check that the uiStateTag changes when we add a tag.
    val tag = Tag.DND
    viewModel.addTag(tag)
    val state = viewModel.selectedTags.value
    assertEquals(listOf(Tag.DND), state)
  }

  @Test
  fun addTag_addsTechnologyTagSuccessfully() = runTest {
    // Check that the uiStateTag changes when we add a tag.
    val tag = Tag.MACHINE_LEARNING
    viewModel.addTag(tag)
    val state = viewModel.selectedTags.value
    assertEquals(listOf(Tag.MACHINE_LEARNING), state)
  }

  @Test
  fun addTag_addsTopicTagSuccessfully() = runTest {
    // Check that the uiStateTag changes when we add a tag.
    val tag = Tag.PHYSICS
    viewModel.addTag(tag)
    val state = viewModel.selectedTags.value
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
      viewModel.addTag(tag)
    }
    val state = viewModel.selectedTags.value
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
  fun deleteTag_removesMusicTagSuccessfully() = runTest {
    // Check that the uiStateTag changes when we remove a tag.
    val tag = Tag.METAL
    viewModel.addTag(tag)
    viewModel.deleteTag(tag)
    val state = viewModel.selectedTags.value
    assertEquals(emptyList<Tag>(), state)
  }

  @Test
  fun deleteTag_removesSportTagSuccessfully() = runTest {
    // Check that the uiStateTag changes when we remove a tag.
    val tag = Tag.JUDO
    viewModel.addTag(tag)
    viewModel.deleteTag(tag)
    val state = viewModel.selectedTags.value
    assertEquals(emptyList<Tag>(), state)
  }

  @Test
  fun deleteTag_removesFoodTagSuccessfully() = runTest {
    // Check that the uiStateTag changes when we remove a tag.
    val tag = Tag.BAKING
    viewModel.addTag(tag)
    viewModel.deleteTag(tag)
    val state = viewModel.selectedTags.value
    assertEquals(emptyList<Tag>(), state)
  }

  @Test
  fun deleteTag_removesArtTagSuccessfully() = runTest {
    // Check that the uiStateTag changes when we remove a tag.
    val tag = Tag.LITERATURE
    viewModel.addTag(tag)
    viewModel.deleteTag(tag)
    val state = viewModel.selectedTags.value
    assertEquals(emptyList<Tag>(), state)
  }

  @Test
  fun deleteTag_removesTravelTagSuccessfully() = runTest {
    // Check that the uiStateTag changes when we remove a tag.
    val tag = Tag.SAFARI
    viewModel.addTag(tag)
    viewModel.deleteTag(tag)
    val state = viewModel.selectedTags.value
    assertEquals(emptyList<Tag>(), state)
  }

  @Test
  fun deleteTag_removesGamesTagSuccessfully() = runTest {
    // Check that the uiStateTag changes when we remove a tag.
    val tag = Tag.DND
    viewModel.addTag(tag)
    viewModel.deleteTag(tag)
    val state = viewModel.selectedTags.value
    assertEquals(emptyList<Tag>(), state)
  }

  @Test
  fun deleteTag_removesTechnologyTagSuccessfully() = runTest {
    // Check that the uiStateTag changes when we remove a tag.
    val tag = Tag.MACHINE_LEARNING
    viewModel.addTag(tag)
    viewModel.deleteTag(tag)
    val state = viewModel.selectedTags.value
    assertEquals(emptyList<Tag>(), state)
  }

  @Test
  fun deleteTag_removesTopicTagSuccessfully() = runTest {
    // Check that the uiStateTag changes when we remove a tag.
    val tag = Tag.PHYSICS
    viewModel.addTag(tag)
    viewModel.deleteTag(tag)
    val state = viewModel.selectedTags.value
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
      viewModel.addTag(tag)
    }
    for (tag in tags) {
      viewModel.deleteTag(tag)
    }
    val state = viewModel.selectedTags.value
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
      viewModel.addTag(tag)
    }
    viewModel.deleteTag(Tag.BAKING)
    viewModel.deleteTag(Tag.CYCLING)
    viewModel.deleteTag(Tag.KARATE)

    val expectedTags =
        listOf(Tag.SAFARI, Tag.METAL, Tag.ROCK, Tag.JUDO, Tag.HANDBALL, Tag.MUSIC, Tag.DND)
    val state = viewModel.selectedTags.value
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
    userRepository.addUser(userProfile)
    advanceUntilIdle()
    setUpViewmodel(userProfile.uid, SelectTagMode.USER_PROFILE)
    advanceUntilIdle()
    assertEquals(listOf(Tag.METAL, Tag.KARATE), viewModel.selectedTags.value)
  }

  @Test
  fun deleteTag_maintainsCorrectOrder() = runTest {
    // Check that if we select tags and deselect them, the selected tags remain in the correct
    // order, matching the sequence they
    // were clicked.
    viewModel.addTag(Tag.METAL)
    viewModel.addTag(Tag.HANDBALL)
    viewModel.addTag(Tag.KARATE)
    viewModel.deleteTag(Tag.HANDBALL)

    assertEquals(listOf(Tag.METAL, Tag.KARATE), viewModel.selectedTags.value)
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
    userRepository.addUser(userProfile)
    advanceUntilIdle()
    setUpViewmodel(userProfile.uid, SelectTagMode.USER_PROFILE)
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
      viewModel.addTag(tag)
    }

    viewModel.saveTags()
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
    val actual = userRepository.getUser("0")
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
    userRepository.addUser(userProfile)
    advanceUntilIdle()
    setUpViewmodel(userProfile.uid, SelectTagMode.USER_PROFILE)
    advanceUntilIdle()
    viewModel.addTag(Tag.HANDBALL)
    viewModel.saveTags()
    advanceUntilIdle()
    val updatedUser = userRepository.getUser("0")
    advanceUntilIdle()
    assertEquals(setOf(Tag.METAL, Tag.KARATE, Tag.HANDBALL), updatedUser.tags)
  }

  @Test
  fun loadTagsEventCreation() = runTest {
    tagRepository.updateTags(tags)
    advanceUntilIdle()
    setUpViewmodel(defaultUser.uid, SelectTagMode.SETTINGS)
    advanceUntilIdle()
    val resultTags = viewModel.selectedTags.value.toSet()
    assertEquals(tags, resultTags)
  }

  @Test
  fun loadTagsWhenRepositoryChange() = runTest {
    tagRepository.updateTags(tags)
    advanceUntilIdle()
    setUpViewmodel(defaultUser.uid, SelectTagMode.SETTINGS)
    advanceUntilIdle()
    val resultTags = viewModel.selectedTags.value.toSet()
    assertEquals(tags, resultTags)
  }

  @Test
  fun saveTagsEventCreation() = runTest {
    setUpViewmodel(defaultUser.uid, SelectTagMode.EVENT_CREATION)
    advanceUntilIdle()
    tags.forEach { tag -> viewModel.addTag(tag) }
    advanceUntilIdle()
    val fakeEvent = SelectTagViewModelTestValues.temporaryEvent
    eventTemporaryRepository.updateEvent(
        id = fakeEvent.id,
        title = fakeEvent.title,
        description = fakeEvent.description,
        dateTime = fakeEvent.date,
        creator = fakeEvent.creator,
        participants = fakeEvent.participants,
        location = fakeEvent.location,
        isPrivate = fakeEvent.isPrivate,
        eventPicture = fakeEvent.eventPicture)
    viewModel.saveTags()
    advanceUntilIdle()
    val resultEventTags = eventRepository.getEvent("test")
    assertEquals(fakeEvent.copy(tags = tags), resultEventTags)
  }
}
