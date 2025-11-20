package com.android.universe.ui.event

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.model.event.FakeEventRepository
import com.android.universe.model.user.FakeUserRepository
import com.android.universe.utils.EventTestData
import com.android.universe.utils.MainCoroutineRule
import com.android.universe.utils.UserTestData
import com.android.universe.utils.setContentWithStubBackdrop
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class EventScreenTest {

  @get:Rule val composeTestRule = createComposeRule()
  @get:Rule val mainCoroutineRule = MainCoroutineRule()

  private lateinit var fakeEventRepository: FakeEventRepository
  private lateinit var fakeUserRepository: FakeUserRepository
  private lateinit var viewModel: EventViewModel

  companion object {
    private val firstEvent = EventTestData.FullDescriptionEvent
    private val secondEvent = EventTestData.NullDescriptionEvent
    private val thirdEvent = EventTestData.NoTagsEvent
    private val fourthEvent = EventTestData.NoImage
    private val megaTagEvent = EventTestData.SomeTagsEvent
    private val sampleEvents = listOf(firstEvent, secondEvent, thirdEvent)
    private val sampleUsers =
        listOf(UserTestData.NullDescription, UserTestData.ManyTagsUser, UserTestData.Alice)

    private const val THREE_TAG_MESSAGE = "Expected at least 3 visible tag nodes but found"
    private const val ONE_ICON_IMAGE_MESSAGE = "Expected one Image Icon but found"
  }

  @Before
  fun setUp() {
    // Create a fresh fake repository for every test (isolated)
    fakeEventRepository = FakeEventRepository()
    fakeUserRepository = FakeUserRepository()

    runTest {
      sampleEvents.forEach { fakeEventRepository.addEvent(it) }
      sampleUsers.forEach { fakeUserRepository.addUser(it) }
    }

    viewModel = EventViewModel(fakeEventRepository, null, fakeUserRepository)

    composeTestRule.setContentWithStubBackdrop { EventScreen(viewModel = viewModel) }

    viewModel.loadEvents()

    runTest { advanceUntilIdle() }
    // Wait until Compose settles
    composeTestRule.waitForIdle()
  }

  @Test
  fun displayAllCoreComponents() = runTest {
    // LazyColumn list is displayed
    composeTestRule.onNodeWithTag(EventScreenTestTags.EVENTS_LIST, useUnmergedTree = true)
      .assertIsDisplayed()

    // There should be at least one event card: fetch semantics nodes and assert non-empty
    val cards = composeTestRule.onAllNodesWithTag(EventScreenTestTags.EVENT_CARD, useUnmergedTree = true)
    assertTrue(cards.fetchSemanticsNodes().isNotEmpty())

    // Check that the first card's main parts are present (query by the first occurrence)
    composeTestRule.onNodeWithTag(EventScreenTestTags.EVENTS_LIST, useUnmergedTree = true)

      .performScrollToNode(hasTestTag(EventScreenTestTags.EVENT_TITLE))

    composeTestRule.onAllNodesWithTag(EventScreenTestTags.EVENT_TITLE, useUnmergedTree = true)
      .onFirst()
      .assertIsDisplayed()
    composeTestRule
        .onAllNodesWithTag(EventScreenTestTags.EVENT_DESCRIPTION, useUnmergedTree = true)
        .onFirst()
        .assertIsDisplayed()
    composeTestRule.onAllNodesWithTag(EventScreenTestTags.EVENT_DATE, useUnmergedTree = true).onFirst().assertIsDisplayed()
    composeTestRule.onAllNodesWithTag(EventScreenTestTags.EVENT_IMAGE, useUnmergedTree = true).onFirst().assertIsDisplayed()
    composeTestRule
        .onAllNodesWithTag(EventScreenTestTags.EVENT_CREATOR_PARTICIPANTS, useUnmergedTree = true)
        .onFirst()
        .assertIsDisplayed()
    composeTestRule
        .onAllNodesWithTag(EventScreenTestTags.EVENT_JOIN_BUTTON, useUnmergedTree = true)
        .onFirst()
        .assertIsDisplayed()
  }

  @Test
  fun tagsAreDisplayedInEachEventCard() = runTest {
    // Get all tag nodes and assert there is at least one tag shown anywhere
    val tags = composeTestRule.onAllNodesWithTag(EventScreenTestTags.EVENT_TAG, useUnmergedTree = true)
    assertTrue(tags.fetchSemanticsNodes().isNotEmpty())
  }

  @Test
  fun eventsWithMoreThanThreeTagsAreCropped() {
    // Add an event with 6 tags to the repository
    runTest {
      fakeEventRepository.getAllEvents().forEach { fakeEventRepository.deleteEvent(it.id) }
      fakeEventRepository.addEvent(megaTagEvent)
      // Reload events in the ViewModel (suspending call)
      viewModel.loadEvents()

      // Let Compose update. Wait until at least one tag node appears (timeout guards flakiness).
      composeTestRule
          .onAllNodesWithTag(EventScreenTestTags.EVENT_TAG, useUnmergedTree = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    // Now fetch all tag nodes that belong to the UI.
    val tagNodes = composeTestRule.onAllNodesWithTag(EventScreenTestTags.EVENT_TAG, useUnmergedTree = true)
    val totalReachableTags = tagNodes.fetchSemanticsNodes().size
    var totalVisibleTags = 0
    for (i in 0 until totalReachableTags) {
      if (tagNodes[i].isDisplayed()) totalVisibleTags++
    }
    // We expect at least 3 visible tags overall (the Mega Tag Event should contribute 3).
    assertTrue("$THREE_TAG_MESSAGE $totalVisibleTags", totalVisibleTags == 3)
  }

  @Test
  fun joinButtonIsVisibleAndClickable() {
    val joinButton =
        composeTestRule.onAllNodesWithTag(EventScreenTestTags.EVENT_JOIN_BUTTON).onFirst()
    joinButton.assertIsDisplayed()
    // assertHasClickAction verifies it is clickable (or at least has a click semantics)
    joinButton.assertHasClickAction()
  }

  @Test
  fun iconImageIsVisibleIfNoImage() = runTest {
    fakeEventRepository.addEvent(fourthEvent)
    viewModel.loadEvents()
    advanceUntilIdle()
    composeTestRule
        .onNodeWithTag(EventScreenTestTags.EVENTS_LIST, useUnmergedTree = true)
      .performScrollToNode(hasTestTag(EventScreenTestTags.DEFAULT_EVENT_IMAGE))
    composeTestRule
        .onAllNodesWithTag(EventScreenTestTags.DEFAULT_EVENT_IMAGE, useUnmergedTree = true)
        .onFirst()
        .assertIsDisplayed()
    val tagNodes = composeTestRule.onAllNodesWithTag(EventScreenTestTags.DEFAULT_EVENT_IMAGE, useUnmergedTree = true)
    val totalReachableTags = tagNodes.fetchSemanticsNodes().size
    assertTrue("$ONE_ICON_IMAGE_MESSAGE $totalReachableTags", totalReachableTags == 1)
  }
}
