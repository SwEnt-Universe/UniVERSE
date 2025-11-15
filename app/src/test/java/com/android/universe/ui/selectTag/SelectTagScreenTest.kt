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
    private val BUTTON_READING = SelectTagsScreenTestTags.unselectedTag(Tag.READING)
    private val BUTTON_BERN = SelectTagsScreenTestTags.unselectedTag(Tag.BERN)
    private val BUTTON_HANDBALL = SelectTagsScreenTestTags.unselectedTag(Tag.HANDBALL)
    private val BUTTON_SURFING = SelectTagsScreenTestTags.unselectedTag(Tag.SURFING)
    private val BUTTON_METAL = SelectTagsScreenTestTags.unselectedTag(Tag.METAL)
    private val BUTTON_CAR = SelectTagsScreenTestTags.unselectedTag(Tag.CAR)
    private val BUTTON_TRAIN = SelectTagsScreenTestTags.unselectedTag(Tag.TRAIN)
    private val BUTTON_BOAT = SelectTagsScreenTestTags.unselectedTag(Tag.BOAT)
    private val BUTTON_BUS = SelectTagsScreenTestTags.unselectedTag(Tag.BUS)
    private val BUTTON_BICYCLE = SelectTagsScreenTestTags.unselectedTag(Tag.BICYCLE)
    private val BUTTON_FOOT = SelectTagsScreenTestTags.unselectedTag(Tag.FOOT)
    private val BUTTON_PLANE = SelectTagsScreenTestTags.unselectedTag(Tag.PLANE)
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
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.INTEREST_TAGS).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(LAZY_COLUMN_TAGS)
        .performScrollToNode(hasTestTag(SelectTagsScreenTestTags.SPORT_TAGS))
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(LAZY_COLUMN_TAGS)
        .performScrollToNode(hasTestTag(SelectTagsScreenTestTags.MUSIC_TAGS))
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(LAZY_COLUMN_TAGS)
        .performScrollToNode(hasTestTag(SelectTagsScreenTestTags.TRANSPORT_TAGS))
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(LAZY_COLUMN_TAGS)
        .performScrollToNode(hasTestTag(SelectTagsScreenTestTags.CANTON_TAGS))
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
  fun selectedTagsShownWhenInterestTagClicked() {
    launchDefaultScreen()
    // Check that when the user selects a tag, it appears in the selected section with its trash
    // icon.
    composeTestRule.onNodeWithTag(BUTTON_READING).performClick()
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
  fun selectedTagsShownWhenMusicTagClicked() {
    launchDefaultScreen()
    // Check that when the user selects a tag, it appears in the selected section with its trash
    // icon.
    scrollAndClick(BUTTON_METAL)
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.SELECTED_TAGS).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.DELETE_ICON).assertIsDisplayed()
  }

  @Test
  fun selectedTagsShownWhenTransportTagClicked() {
    launchDefaultScreen()
    // Check that when the user selects a tag, it appears in the selected section with its trash
    // icon.
    scrollAndClick(BUTTON_CAR)
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.SELECTED_TAGS).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.DELETE_ICON).assertIsDisplayed()
  }

  @Test
  fun selectedTagsShownWhenCantonTagClicked() {
    launchDefaultScreen()
    // Check that when the user selects a tag, it appears in the selected section with its trash
    // icon.
    scrollAndClick(BUTTON_BERN)
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.SELECTED_TAGS).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.DELETE_ICON).assertIsDisplayed()
  }

  @Test
  fun selectedTagsShownWhenMultipleTagsClicked() {
    launchDefaultScreen()
    // Check that when the user selects multiple tags, they appear in the selected section with
    // their trash icons.
    scrollAndClick(BUTTON_BERN)
    scrollAndClick(BUTTON_SURFING)
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.SELECTED_TAGS).assertIsDisplayed()
    val deleteIcons = composeTestRule.onAllNodesWithTag(SelectTagsScreenTestTags.DELETE_ICON)
    deleteIcons.assertAll(hasClickAction())
  }

  @Test
  fun selectedTagsHiddenAfterDeleteClicked() {
    launchDefaultScreen()
    // Check that if we click on the trash icon, the tag is deselected and does not appear in the
    // selected tag section.
    scrollAndClick(BUTTON_BERN)
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.DELETE_ICON).performClick()
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.SELECTED_TAGS).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.DELETE_ICON).assertIsNotDisplayed()
  }

  @Test
  fun selectedTagsHiddenWhenTagDeselected() {
    launchDefaultScreen()
    // Check that if we click again on the tag, it is deselected and does not appear in the selected
    // tag section.
    scrollAndClick(BUTTON_BERN)
    scrollAndClick(BUTTON_BERN)
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.SELECTED_TAGS).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.DELETE_ICON).assertIsNotDisplayed()

    scrollAndClick(BUTTON_BERN)
    scrollAndClick(BUTTON_SURFING)
    scrollAndClick(BUTTON_BERN)
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.SELECTED_TAGS).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.DELETE_ICON).assertIsDisplayed()
  }

  @Test
  fun selectedTagsDisplayedInCorrectOrder() {
    launchDefaultScreen()
    // Check that the selected tags are displayed in the correct order, matching the sequence they
    // were clicked.
    scrollAndClick(BUTTON_BERN)
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
        listOf(Tag.BERN.displayName, Tag.HANDBALL.displayName, Tag.METAL.displayName),
        displayedTags)
  }

  @Test
  fun selectedTagsMaintainOrderAfterDeselection() {
    launchDefaultScreen()
    // Check that the selected tags are displayed in the correct order when we deselect one tag.
    scrollAndClick(BUTTON_BERN)
    scrollAndClick(BUTTON_HANDBALL)
    scrollAndClick(BUTTON_METAL)
    scrollAndClick(BUTTON_CAR)
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
        listOf(Tag.BERN.displayName, Tag.HANDBALL.displayName, Tag.CAR.displayName), displayedTags)
  }

  @Test
  fun selectedTagsRemainStableAfterRapidClicks() {
    launchDefaultScreen()
    // Check that rapid repeated clicks on a tag do not break selection behavior.
    scrollAndClick(BUTTON_BERN)
    scrollAndClick(BUTTON_BERN)
    scrollAndClick(BUTTON_BERN)
    scrollAndClick(BUTTON_BERN)
    scrollAndClick(BUTTON_BERN)
    scrollAndClick(BUTTON_BERN)
    scrollAndClick(BUTTON_BERN)
    scrollAndClick(BUTTON_BERN)
    scrollAndClick(BUTTON_BERN)
    scrollAndClick(BUTTON_BERN)
    scrollAndClick(BUTTON_BERN)

    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.SELECTED_TAGS).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.DELETE_ICON).assertIsDisplayed()
  }

  private fun selectLotsOfTags() {
    scrollAndClick(BUTTON_CAR)
    scrollAndClick(BUTTON_TRAIN)
    scrollAndClick(BUTTON_BOAT)
    scrollAndClick(BUTTON_BUS)
    scrollAndClick(BUTTON_BICYCLE)
    scrollAndClick(BUTTON_FOOT)
    scrollAndClick(BUTTON_PLANE)
  }

  @Test
  fun tagStillVisibleWhenAllSelected() {
    launchDefaultScreen()
    // Check that selecting all tags of one type still displays them on the screen.
    selectLotsOfTags()

    composeTestRule
        .onNodeWithTag(LAZY_COLUMN_TAGS)
        .performScrollToNode(hasTestTag(SelectTagsScreenTestTags.TRANSPORT_TAGS))
        .assertIsDisplayed()
  }

  @Test
  fun selectedTagsSectionIsScrollable() {
    launchDefaultScreen()
    // Check that the selected tags section is scrollable.
    selectLotsOfTags()

    composeTestRule
        .onNodeWithTag(SelectTagsScreenTestTags.SELECTED_TAGS)
        .performScrollToNode(hasTestTag(SelectTagsScreenTestTags.selectedTag(Tag.PLANE)))
        .assertIsDisplayed()
  }

  @Test
  fun selectedTagsModeChange() {
    val modeViewModel =
        SelectTagViewModel(userRepository, selectTagMode = SelectTagMode.USER_PROFILE)

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
