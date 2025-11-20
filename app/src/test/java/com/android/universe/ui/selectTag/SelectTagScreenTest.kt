package com.android.universe.ui.selectTag

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assertAll
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.model.tag.Tag
import com.android.universe.model.user.FakeUserRepository
import com.android.universe.model.user.UserRepository
import com.android.universe.utils.UserTestData
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SelectTagScreenTest {
  /**
   * Private function scrollAndClick that is used in the tests to perform a scroll to an element and
   * click on it
   */
  private fun scrollAndClick(clickName: String) {
    composeTestRule.onNodeWithTag(LAZY_COLUMN_TAGS).performScrollToNode(hasTestTag(clickName))
    composeTestRule.onNodeWithTag(clickName).performClick()
  }

  // Define the parameters for the tests.
  @get:Rule val composeTestRule = createComposeRule()
  private lateinit var userRepository: UserRepository
  private lateinit var viewModel: SelectTagViewModel

  companion object {
    private val dummyUser = UserTestData.NoTagsUser
    private val BUTTON_METAL = SelectTagsScreenTestTags.unselectedTag(Tag.METAL)
    private val BUTTON_HANDBALL = SelectTagsScreenTestTags.unselectedTag(Tag.HANDBALL)
    private val BUTTON_BAKING = SelectTagsScreenTestTags.unselectedTag(Tag.BAKING)
    private val BUTTON_DOCUMENTARIES = SelectTagsScreenTestTags.unselectedTag(Tag.DOCUMENTARIES)
    private val BUTTON_SAFARI = SelectTagsScreenTestTags.unselectedTag(Tag.SAFARI)
    private val BUTTON_DND = SelectTagsScreenTestTags.unselectedTag(Tag.DND)
    private val BUTTON_PROGRAMMING = SelectTagsScreenTestTags.unselectedTag(Tag.PROGRAMMING)
    private val BUTTON_PHYSICS = SelectTagsScreenTestTags.unselectedTag(Tag.PHYSICS)

    private val BUTTON_VIDEO_GAMES = SelectTagsScreenTestTags.unselectedTag(Tag.VIDEO_GAMES)
    private val BUTTON_BOARD_GAMES = SelectTagsScreenTestTags.unselectedTag(Tag.BOARD_GAMES)
    private val BUTTON_CARD_GAMES = SelectTagsScreenTestTags.unselectedTag(Tag.CARD_GAMES)
    private val BUTTON_PUZZLE = SelectTagsScreenTestTags.unselectedTag(Tag.PUZZLE)
    private val BUTTON_BRAIN_GAMES = SelectTagsScreenTestTags.unselectedTag(Tag.BRAIN_GAMES)
    private val BUTTON_ONLINE_GAMES = SelectTagsScreenTestTags.unselectedTag(Tag.ONLINE_GAMES)
    private val BUTTON_CO_OP_GAMES = SelectTagsScreenTestTags.unselectedTag(Tag.CO_OP_GAMES)
    private val BUTTON_CHESS = SelectTagsScreenTestTags.unselectedTag(Tag.CHESS)
    private const val LAZY_COLUMN_TAGS = "LazyColumnTags"
  }

  private fun launchDefaultScreen() {
    composeTestRule.setContent {
      SelectTagScreen(selectedTagOverview = viewModel, uid = dummyUser.uid)
    }
  }

  @Before
  fun setUp() {
    // Set up a fake repository for testing
    userRepository = FakeUserRepository()
    runTest { userRepository.addUser(dummyUser) }
    viewModel = SelectTagViewModel(userRepository)
  }

  @Test
  fun allTagGroupsAreDisplayed() {
    launchDefaultScreen()
    // Check that all types of tags are displayed.
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.MUSIC_TAGS).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(LAZY_COLUMN_TAGS)
        .performScrollToNode(hasTestTag(SelectTagsScreenTestTags.SPORT_TAGS))
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(LAZY_COLUMN_TAGS)
        .performScrollToNode(hasTestTag(SelectTagsScreenTestTags.FOOD_TAGS))
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(LAZY_COLUMN_TAGS)
        .performScrollToNode(hasTestTag(SelectTagsScreenTestTags.ART_TAGS))
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(LAZY_COLUMN_TAGS)
        .performScrollToNode(hasTestTag(SelectTagsScreenTestTags.TRAVEL_TAGS))
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(LAZY_COLUMN_TAGS)
        .performScrollToNode(hasTestTag(SelectTagsScreenTestTags.GAMES_TAGS))
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(LAZY_COLUMN_TAGS)
        .performScrollToNode(hasTestTag(SelectTagsScreenTestTags.TECHNOLOGY_TAGS))
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(LAZY_COLUMN_TAGS)
        .performScrollToNode(hasTestTag(SelectTagsScreenTestTags.TOPIC_TAGS))
        .assertIsDisplayed()
  }

  @Test
  fun saveButtonIsDisplayed() {
    launchDefaultScreen()
    // Check that the save button is displayed.
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.SAVE_BUTTON).assertIsDisplayed()
  }

  @Test
  fun selectedTagsSectionIsHiddenInitially() {
    launchDefaultScreen()
    // Check that the selectedTags and their trash icons are not displayed because the user didn't
    // select anything.
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.SELECTED_TAGS).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.DELETE_ICON).assertIsNotDisplayed()
  }

  @Test
  fun selectedTagsShownWhenMusicTagClicked() {
    launchDefaultScreen()
    // Check that when the user selects a tag, it appears in the selected section with its trash
    // icon.
    scrollAndClick(BUTTON_METAL)
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.SELECTED_TAGS).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.DELETE_ICON).assertIsDisplayed()
  }

  @Test
  fun selectedTagsShownWhenSportTagClicked() {
    launchDefaultScreen()
    // Check that when the user selects a tag, it appears in the selected section with its trash
    // icon.
    scrollAndClick(BUTTON_HANDBALL)
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.SELECTED_TAGS).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.DELETE_ICON).assertIsDisplayed()
  }

  @Test
  fun selectedTagsShownWhenFoodTagClicked() {
    launchDefaultScreen()
    // Check that when the user selects a tag, it appears in the selected section with its trash
    // icon.
    scrollAndClick(BUTTON_BAKING)
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.SELECTED_TAGS).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.DELETE_ICON).assertIsDisplayed()
  }

  @Test
  fun selectedTagsShownWhenArtTagClicked() {
    launchDefaultScreen()
    // Check that when the user selects a tag, it appears in the selected section with its trash
    // icon.
    scrollAndClick(BUTTON_DOCUMENTARIES)
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.SELECTED_TAGS).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.DELETE_ICON).assertIsDisplayed()
  }

  @Test
  fun selectedTagsShownWhenTravelTagClicked() {
    launchDefaultScreen()
    // Check that when the user selects a tag, it appears in the selected section with its trash
    // icon.
    scrollAndClick(BUTTON_SAFARI)
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.SELECTED_TAGS).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.DELETE_ICON).assertIsDisplayed()
  }

  @Test
  fun selectedTagsShownWhenGamesTagClicked() {
    launchDefaultScreen()
    // Check that when the user selects a tag, it appears in the selected section with its trash
    // icon.
    scrollAndClick(BUTTON_DND)
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.SELECTED_TAGS).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.DELETE_ICON).assertIsDisplayed()
  }

  @Test
  fun selectedTagsShownWhenTechnologyTagClicked() {
    launchDefaultScreen()
    // Check that when the user selects a tag, it appears in the selected section with its trash
    // icon.
    scrollAndClick(BUTTON_PROGRAMMING)
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.SELECTED_TAGS).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.DELETE_ICON).assertIsDisplayed()
  }

  @Test
  fun selectedTagsShownWhenTopicTagClicked() {
    launchDefaultScreen()
    // Check that when the user selects a tag, it appears in the selected section with its trash
    // icon.
    scrollAndClick(BUTTON_PHYSICS)
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.SELECTED_TAGS).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.DELETE_ICON).assertIsDisplayed()
  }

  @Test
  fun selectedTagsShownWhenMultipleTagsClicked() {
    launchDefaultScreen()
    // Check that when the user selects multiple tags, they appear in the selected section with
    // their trash icons.
    scrollAndClick(BUTTON_PHYSICS)
    scrollAndClick(BUTTON_SAFARI)
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.SELECTED_TAGS).assertIsDisplayed()
    val deleteIcons = composeTestRule.onAllNodesWithTag(SelectTagsScreenTestTags.DELETE_ICON)
    deleteIcons.assertAll(hasClickAction())
  }

  @Test
  fun selectedTagsHiddenAfterDeleteClicked() {
    launchDefaultScreen()
    // Check that if we click on the trash icon, the tag is deselected and does not appear in the
    // selected tag section.
    scrollAndClick(BUTTON_SAFARI)
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.DELETE_ICON).performClick()
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.SELECTED_TAGS).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.DELETE_ICON).assertIsNotDisplayed()
  }

  @Test
  fun selectedTagsHiddenWhenTagDeselected() {
    launchDefaultScreen()
    // Check that if we click again on the tag, it is deselected and does not appear in the selected
    // tag section.
    scrollAndClick(BUTTON_SAFARI)
    scrollAndClick(BUTTON_SAFARI)
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.SELECTED_TAGS).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.DELETE_ICON).assertIsNotDisplayed()

    scrollAndClick(BUTTON_SAFARI)
    scrollAndClick(BUTTON_DND)
    scrollAndClick(BUTTON_SAFARI)
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.SELECTED_TAGS).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.DELETE_ICON).assertIsDisplayed()
  }

  @Test
  fun selectedTagsDisplayedInCorrectOrder() {
    launchDefaultScreen()
    // Check that the selected tags are displayed in the correct order, matching the sequence they
    // were clicked.
    scrollAndClick(BUTTON_SAFARI)
    scrollAndClick(BUTTON_HANDBALL)
    scrollAndClick(BUTTON_METAL)

    val selectedTagNodes =
        composeTestRule
            .onAllNodes(hasParent(hasTestTag(SelectTagsScreenTestTags.SELECTED_TAGS)))
            .fetchSemanticsNodes()
    val displayedTags =
        selectedTagNodes.mapNotNull {
          it.config.getOrNull(SemanticsProperties.Text)?.firstOrNull()?.text
        }
    assertEquals(
        listOf(Tag.SAFARI.displayName, Tag.HANDBALL.displayName, Tag.METAL.displayName),
        displayedTags)
  }

  @Test
  fun selectedTagsMaintainOrderAfterDeselection() {
    launchDefaultScreen()
    // Check that the selected tags are displayed in the correct order when we deselect one tag.
    scrollAndClick(BUTTON_SAFARI)
    scrollAndClick(BUTTON_HANDBALL)
    scrollAndClick(BUTTON_METAL)
    scrollAndClick(BUTTON_DND)
    scrollAndClick(BUTTON_METAL)

    val selectedTagNodes =
        composeTestRule
            .onAllNodes(hasParent(hasTestTag(SelectTagsScreenTestTags.SELECTED_TAGS)))
            .fetchSemanticsNodes()
    val displayedTags =
        selectedTagNodes.mapNotNull {
          it.config.getOrNull(SemanticsProperties.Text)?.firstOrNull()?.text
        }
    assertEquals(
        listOf(Tag.SAFARI.displayName, Tag.HANDBALL.displayName, Tag.DND.displayName),
        displayedTags)
  }

  @Test
  fun selectedTagsRemainStableAfterRapidClicks() {
    launchDefaultScreen()
    // Check that rapid repeated clicks on a tag do not break selection behavior.
    scrollAndClick(BUTTON_SAFARI)
    scrollAndClick(BUTTON_SAFARI)
    scrollAndClick(BUTTON_SAFARI)
    scrollAndClick(BUTTON_SAFARI)
    scrollAndClick(BUTTON_SAFARI)
    scrollAndClick(BUTTON_SAFARI)
    scrollAndClick(BUTTON_SAFARI)
    scrollAndClick(BUTTON_SAFARI)
    scrollAndClick(BUTTON_SAFARI)
    scrollAndClick(BUTTON_SAFARI)
    scrollAndClick(BUTTON_SAFARI)

    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.SELECTED_TAGS).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.DELETE_ICON).assertIsDisplayed()
  }

  private fun selectLotsOfTags() {
    scrollAndClick(BUTTON_VIDEO_GAMES)
    scrollAndClick(BUTTON_BOARD_GAMES)
    scrollAndClick(BUTTON_CARD_GAMES)
    scrollAndClick(BUTTON_DND)
    scrollAndClick(BUTTON_PUZZLE)
    scrollAndClick(BUTTON_BRAIN_GAMES)
    scrollAndClick(BUTTON_ONLINE_GAMES)
    scrollAndClick(BUTTON_CO_OP_GAMES)
    scrollAndClick(BUTTON_CHESS)
  }

  @Test
  fun tagStillVisibleWhenAllSelected() {
    launchDefaultScreen()
    // Check that selecting all tags of one type still displays them on the screen.
    selectLotsOfTags()

    composeTestRule
        .onNodeWithTag(LAZY_COLUMN_TAGS)
        .performScrollToNode(hasTestTag(SelectTagsScreenTestTags.GAMES_TAGS))
        .assertIsDisplayed()
  }

  @Test
  fun selectedTagsSectionIsScrollable() {
    launchDefaultScreen()
    // Check that the selected tags section is scrollable.
    selectLotsOfTags()

    composeTestRule
        .onNodeWithTag(SelectTagsScreenTestTags.SELECTED_TAGS)
        .performScrollToNode(hasTestTag(SelectTagsScreenTestTags.selectedTag(Tag.CHESS)))
        .assertIsDisplayed()
  }

  @Test
  fun selectedTagsModeChange() {
    val modeViewModel = SelectTagViewModel(userRepository)

    composeTestRule.setContent {
      SelectTagScreen(
          selectTagMode = SelectTagMode.EVENT_CREATION,
          selectedTagOverview = modeViewModel,
          uid = dummyUser.uid)
    }
    composeTestRule.waitForIdle()

    assertEquals(modeViewModel.mode, SelectTagMode.EVENT_CREATION)
  }
}
