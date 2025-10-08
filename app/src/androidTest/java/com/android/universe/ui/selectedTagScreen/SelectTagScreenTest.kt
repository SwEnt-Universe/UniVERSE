package com.android.universe.ui.selectedTagScreen

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.android.universe.model.user.FakeUserRepository
import com.android.universe.model.user.UserProfile
import com.android.universe.ui.selectTag.SelectTagScreen
import com.android.universe.ui.selectTag.SelectTagViewModel
import com.android.universe.ui.selectTag.SelectTagsScreenTestTags
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SelectTagScreenTest {
  /**
   * Private function scrollAndClick that is used in the tests to perform a scroll to an element and
   * click on it
   */
  private fun scrollAndClick(clickName: String) {
    composeTestRule.onNodeWithTag("LazyColumnTags").performScrollToNode(hasTestTag(clickName))
    composeTestRule.onNodeWithTag(clickName).performClick()
  }

  // Define the parameters for the tests.
  @get:Rule val composeTestRule = createComposeRule()
  private lateinit var userRepository: FakeUserRepository
  private lateinit var viewModel: SelectTagViewModel
  private val testDispatcher = StandardTestDispatcher()

  @Before
  fun setUp() {
    // Set up a fake repository for testing
    userRepository =
        FakeUserRepository().apply {
          runBlocking {
            addUser(
                UserProfile(
                    username = "bob",
                    firstName = "Bob",
                    lastName = "Jones",
                    country = "FR",
                    description = "Hello, I'm Bob.",
                    dateOfBirth = LocalDate.of(2000, 8, 11),
                    tags = emptyList()))
          }
        }
    viewModel = SelectTagViewModel(userRepository, testDispatcher)
    composeTestRule.setContent { SelectTagScreen(viewModel, username = "bob") }
  }

  @Test
  fun allTagGroupsAreDisplayed() {
    // Check that all types of tags are displayed.
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.INTEREST_TAGS).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("LazyColumnTags")
        .performScrollToNode(hasTestTag(SelectTagsScreenTestTags.SPORT_TAGS))
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("LazyColumnTags")
        .performScrollToNode(hasTestTag(SelectTagsScreenTestTags.MUSIC_TAGS))
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("LazyColumnTags")
        .performScrollToNode(hasTestTag(SelectTagsScreenTestTags.TRANSPORT_TAGS))
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("LazyColumnTags")
        .performScrollToNode(hasTestTag(SelectTagsScreenTestTags.CANTON_TAGS))
        .assertIsDisplayed()
  }

  @Test
  fun saveButtonIsDisplayed() {
    // Check that the save button is displayed.
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.SAVE_BUTTON).assertIsDisplayed()
  }

  @Test
  fun selectedTagsSectionIsHiddenInitially() {
    // Check that the selectedTags and their trash icons are not displayed because the user didn't
    // select anything.
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.SELECTED_TAGS).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.DELETE_ICON).assertIsNotDisplayed()
  }

  @Test
  fun selectedTagsShownWhenInterestTagClicked() {
    // Check that when the user selects a tag, it appears in the selected section with its trash
    // icon.
    composeTestRule.onNodeWithTag("Button_Reading").performClick()
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.SELECTED_TAGS).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.DELETE_ICON).assertIsDisplayed()
  }

  @Test
  fun selectedTagsShownWhenSportTagClicked() {
    // Check that when the user selects a tag, it appears in the selected section with its trash
    // icon.
    scrollAndClick("Button_Handball")
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.SELECTED_TAGS).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.DELETE_ICON).assertIsDisplayed()
  }

  @Test
  fun selectedTagsShownWhenMusicTagClicked() {
    // Check that when the user selects a tag, it appears in the selected section with its trash
    // icon.
    scrollAndClick("Button_Metal")
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.SELECTED_TAGS).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.DELETE_ICON).assertIsDisplayed()
  }

  @Test
  fun selectedTagsShownWhenTransportTagClicked() {
    // Check that when the user selects a tag, it appears in the selected section with its trash
    // icon.
    scrollAndClick("Button_Car")
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.SELECTED_TAGS).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.DELETE_ICON).assertIsDisplayed()
  }

  @Test
  fun selectedTagsShownWhenCantonTagClicked() {
    // Check that when the user selects a tag, it appears in the selected section with its trash
    // icon.
    scrollAndClick("Button_Bern")
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.SELECTED_TAGS).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.DELETE_ICON).assertIsDisplayed()
  }

  @Test
  fun selectedTagsShownWhenMultipleTagsClicked() {
    // Check that when the user selects multiple tags, they appear in the selected section with
    // their trash icons.
    scrollAndClick("Button_Bern")
    scrollAndClick("Button_Surfing")
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.SELECTED_TAGS).assertIsDisplayed()
    val deleteIcons = composeTestRule.onAllNodesWithTag(SelectTagsScreenTestTags.DELETE_ICON)
    deleteIcons.assertAny(hasClickAction())
  }

  @Test
  fun selectedTagsHiddenAfterDeleteClicked() {
    // Check that if we click on the trash icon, the tag is deselected and does not appear in the
    // selected tag section.
    scrollAndClick("Button_Bern")
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.DELETE_ICON).performClick()
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.SELECTED_TAGS).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.DELETE_ICON).assertIsNotDisplayed()
  }

  @Test
  fun selectedTagsHiddenWhenTagDeselected() {
    // Check that if we click again on the tag, it is deselected and does not appear in the selected
    // tag section.
    scrollAndClick("Button_Bern")
    composeTestRule.onNodeWithTag("Button_Bern").performClick()
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.SELECTED_TAGS).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.DELETE_ICON).assertIsNotDisplayed()

    scrollAndClick("Button_Bern")
    scrollAndClick("Button_Surfing")
    scrollAndClick("Button_Bern")
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.SELECTED_TAGS).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.DELETE_ICON).assertIsDisplayed()
  }

  @Test
  fun selectedTagsDisplayedInCorrectOrder() {
    // Check that the selected tags are displayed in the correct order, matching the sequence they
    // were clicked.
    scrollAndClick("Button_Bern")
    scrollAndClick("Button_Handball")
    scrollAndClick("Button_Metal")

    val selectedTagNodes =
        composeTestRule
            .onAllNodes(hasParent(hasTestTag(SelectTagsScreenTestTags.SELECTED_TAGS)))
            .fetchSemanticsNodes()
    val displayedTags =
        selectedTagNodes.mapNotNull {
          it.config.getOrNull(SemanticsProperties.Text)?.firstOrNull()?.text
        }
    assertEquals(listOf("Bern", "Handball", "Metal"), displayedTags)
  }

  @Test
  fun selectedTagsMaintainOrderAfterDeselection() {
    // Check that the selected tags are displayed in the correct order when we deselect one tag.
    scrollAndClick("Button_Bern")
    scrollAndClick("Button_Handball")
    scrollAndClick("Button_Metal")
    scrollAndClick("Button_Car")
    scrollAndClick("Button_Metal")

    val selectedTagNodes =
        composeTestRule
            .onAllNodes(hasParent(hasTestTag(SelectTagsScreenTestTags.SELECTED_TAGS)))
            .fetchSemanticsNodes()
    val displayedTags =
        selectedTagNodes.mapNotNull {
          it.config.getOrNull(SemanticsProperties.Text)?.firstOrNull()?.text
        }
    assertEquals(listOf("Bern", "Handball", "Car"), displayedTags)
  }

  @Test
  fun selectedTagsRemainStableAfterRapidClicks() {
    // Check that rapid repeated clicks on a tag do not break selection behavior.
    scrollAndClick("Button_Bern")
    scrollAndClick("Button_Bern")
    scrollAndClick("Button_Bern")
    scrollAndClick("Button_Bern")
    scrollAndClick("Button_Bern")
    scrollAndClick("Button_Bern")
    scrollAndClick("Button_Bern")
    scrollAndClick("Button_Bern")
    scrollAndClick("Button_Bern")
    scrollAndClick("Button_Bern")
    scrollAndClick("Button_Bern")

    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.SELECTED_TAGS).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.DELETE_ICON).assertIsDisplayed()
  }

  @Test
  fun tagStillVisibleWhenAllSelected() {
    // Check that selecting all tags of one type still displays them on the screen.
    scrollAndClick("Button_Car")
    scrollAndClick("Button_Train")
    scrollAndClick("Button_Boat")
    scrollAndClick("Button_Bus")
    scrollAndClick("Button_Bicycle")
    scrollAndClick("Button_Foot")
    scrollAndClick("Button_Plane")

    composeTestRule
        .onNodeWithTag("LazyColumnTags")
        .performScrollToNode(hasTestTag(SelectTagsScreenTestTags.TRANSPORT_TAGS))
        .assertIsDisplayed()
  }

  @Test
  fun selectedTagsSectionIsScrollable() {
    // Check that the selected tags section is scrollable.
    scrollAndClick("Button_Car")
    scrollAndClick("Button_Train")
    scrollAndClick("Button_Boat")
    scrollAndClick("Button_Bus")
    scrollAndClick("Button_Bicycle")
    scrollAndClick("Button_Foot")
    scrollAndClick("Button_Plane")

    composeTestRule
        .onNodeWithTag(SelectTagsScreenTestTags.SELECTED_TAGS)
        .performScrollToNode(hasTestTag("Button_Selected_Plane"))
        .assertIsDisplayed()
  }
}
