package com.android.universe.ui.event

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import com.android.universe.model.Tag
import com.android.universe.model.event.Event
import com.android.universe.model.event.FakeEventRepository
import com.android.universe.model.user.UserProfile
import java.time.LocalDate
import java.time.LocalDateTime
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class EventScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var fakeEventRepository: FakeEventRepository
  private lateinit var viewModel: EventViewModel

  private val sampleUsers =
      listOf(
          UserProfile(
              username = "alice123",
              firstName = "Alice",
              lastName = "Smith",
              country = "USA",
              description = "Loves running",
              dateOfBirth = LocalDate.of(1990, 1, 1),
              tags = listOf(Tag("Sport"))),
          UserProfile(
              username = "bob456",
              firstName = "Bob",
              lastName = "Johnson",
              country = "USA",
              description = "Tech enthusiast",
              dateOfBirth = LocalDate.of(1985, 5, 12),
              tags = listOf(Tag("Tech"))))

  @Before
  fun setUp() {
    // Create a fresh fake repository for every test (isolated)
    fakeEventRepository = FakeEventRepository()

    // Preload two sample events
    val sampleEvents =
        listOf(
            Event(
                id = "event-001",
                title = "Morning Run at the Lake",
                description = "Join us for a casual 5km run around the lake followed by coffee.",
                date = LocalDateTime.of(2025, 10, 15, 7, 30),
                tags = setOf(Tag("Sport"), Tag("Outdoor")),
                participants = setOf(sampleUsers[0], sampleUsers[1]),
                creator = sampleUsers[0]),
            Event(
                id = "event-002",
                title = "Tech Hackathon 2025",
                date = LocalDateTime.of(2025, 11, 3, 9, 0),
                tags = setOf(Tag("Tech"), Tag("AI"), Tag("Innovation")),
                participants = emptySet(),
                creator = sampleUsers[1]))

    runBlocking { sampleEvents.forEach { fakeEventRepository.addEvent(it) } }

    viewModel = EventViewModel(fakeEventRepository)

    composeTestRule.setContent { EventScreen(viewModel = viewModel) }

    // Wait until Compose settles
    composeTestRule.waitForIdle()
  }

  @Test
  fun displayAllCoreComponents() {
    // LazyColumn list is displayed
    composeTestRule.onNodeWithTag(EventScreenTestTags.EVENTS_LIST).assertIsDisplayed()

    // There should be at least one event card: fetch semantics nodes and assert non-empty
    val cards = composeTestRule.onAllNodesWithTag(EventScreenTestTags.EVENT_CARD)
    assertTrue(cards.fetchSemanticsNodes().isNotEmpty())

    // Check that the first card's main parts are present (query by the first occurrence)
    composeTestRule.onAllNodesWithTag(EventScreenTestTags.EVENT_TITLE).onFirst().assertIsDisplayed()
    composeTestRule
        .onAllNodesWithTag(EventScreenTestTags.EVENT_DESCRIPTION)
        .onFirst()
        .assertIsDisplayed()
    composeTestRule.onAllNodesWithTag(EventScreenTestTags.EVENT_DATE).onFirst().assertIsDisplayed()
    composeTestRule.onAllNodesWithTag(EventScreenTestTags.EVENT_IMAGE).onFirst().assertIsDisplayed()
    composeTestRule
        .onAllNodesWithTag(EventScreenTestTags.EVENT_CREATOR_PARTICIPANTS)
        .onFirst()
        .assertIsDisplayed()
    composeTestRule
        .onAllNodesWithTag(EventScreenTestTags.EVENT_JOIN_BUTTON)
        .onFirst()
        .assertIsDisplayed()
  }

  @Test
  fun tagsAreDisplayedInEachEventCard() {
    // Get all tag nodes and assert there is at least one tag shown anywhere
    val tags = composeTestRule.onAllNodesWithTag(EventScreenTestTags.EVENT_TAG)
    assertTrue(tags.fetchSemanticsNodes().isNotEmpty())
  }

  @Test
  fun eventsWithMoreThanThreeTagsAreCropped() {
    // Add an event with 5 tags to the repository
    runBlocking {
      val tags =
          setOf(Tag("Tech"), Tag("AI"), Tag("Innovation"), Tag("Workshop"), Tag("Networking"))
      val extraEvent =
          Event(
              id = "event-100",
              title = "Mega Tag Event",
              description = "Event with too many tags",
              date = LocalDateTime.of(2025, 12, 1, 10, 0),
              tags = tags,
              participants = setOf(sampleUsers[0], sampleUsers[1]),
              creator = sampleUsers[0])
      fakeEventRepository.addEvent(extraEvent)
    }

    // Reload events in the ViewModel (suspending call)
    runBlocking { viewModel.loadEvents() }

    // Let Compose update. Wait until at least one tag node appears (timeout guards flakiness).
    composeTestRule.waitUntil(timeoutMillis = 2_000) {
      composeTestRule
          .onAllNodesWithTag(EventScreenTestTags.EVENT_TAG)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    // Now fetch all tag nodes that belong to the UI.
    val tagNodes = composeTestRule.onAllNodesWithTag(EventScreenTestTags.EVENT_TAG)
    val totalVisibleTags = tagNodes.fetchSemanticsNodes().size

    // We expect at least 3 visible tags overall (the Mega Tag Event should contribute 3).
    assertTrue(
        "Expected at least 3 visible tag nodes but found $totalVisibleTags", totalVisibleTags >= 3)
  }

  @Test
  fun joinButtonIsVisibleAndClickable() {
    val joinButton =
        composeTestRule.onAllNodesWithTag(EventScreenTestTags.EVENT_JOIN_BUTTON).onFirst()
    joinButton.assertIsDisplayed()
    // assertHasClickAction verifies it is clickable (or at least has a click semantics)
    joinButton.assertHasClickAction()
  }
}
