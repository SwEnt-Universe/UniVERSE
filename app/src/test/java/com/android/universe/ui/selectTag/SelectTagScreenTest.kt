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
import com.android.universe.model.user.UserRepositoryFirestore
import com.android.universe.utils.FirestoreUserTest
import com.android.universe.utils.UserTestData
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SelectTagScreenTest : FirestoreUserTest() {
  /**
   * Private function scrollAndClick that is used in the tests to perform a scroll to an element and
   * click on it
   */
  private fun scrollAndClick(clickName: String) {
    composeTestRule.onNodeWithTag(LAZY_COLUMN_TAGS).performScrollToNode(hasTestTag(clickName))
    composeTestRule.onNodeWithTag(clickName).performClick()
  }

  // Define the parameters for the tests.
  @get:Rule
  val composeTestRule = createComposeRule()
  private lateinit var userRepository: UserRepositoryFirestore
  private lateinit var viewModel: SelectTagViewModel

  companion object {
    private val dummyUser = UserTestData.NoTagsUser
    private const val BUTTON_BERN = "Button_Bern"
    private const val BUTTON_HANDBALL = "Button_Handball"
    private const val BUTTON_SURFING = "Button_Surfing"
    private const val BUTTON_METAL = "Button_Metal"
    private const val BUTTON_CAR = "Button_Car"
    private const val BUTTON_TRAIN = "Button_Train"
    private const val BUTTON_BOAT = "Button_Boat"
    private const val BUTTON_BUS = "Button_Bus"
    private const val BUTTON_BICYCLE = "Button_Bicycle"
    private const val BUTTON_FOOT = "Button_Foot"
    private const val BUTTON_PLANE = "Button_Plane"
    private const val LAZY_COLUMN_TAGS = "LazyColumnTags"
  }

  @Before
  override fun setUp() {
    super.setUp()
    // Set up a fake repository for testing
      runTest {
          userRepository = UserRepositoryFirestore(emulator.firestore)
          userRepository.addUser(dummyUser)
      }
    viewModel = SelectTagViewModel(userRepository)
    composeTestRule.setContent { SelectTagScreen(viewModel, uid = dummyUser.uid) }
  }

  @Test
  fun allTagGroupsAreDisplayed() {
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
    scrollAndClick(BUTTON_HANDBALL)
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.SELECTED_TAGS).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.DELETE_ICON).assertIsDisplayed()
  }

  @Test
  fun selectedTagsShownWhenMusicTagClicked() {
    // Check that when the user selects a tag, it appears in the selected section with its trash
    // icon.
    scrollAndClick(BUTTON_METAL)
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.SELECTED_TAGS).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.DELETE_ICON).assertIsDisplayed()
  }

  @Test
  fun selectedTagsShownWhenTransportTagClicked() {
    // Check that when the user selects a tag, it appears in the selected section with its trash
    // icon.
    scrollAndClick(BUTTON_CAR)
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.SELECTED_TAGS).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.DELETE_ICON).assertIsDisplayed()
  }

  @Test
  fun selectedTagsShownWhenCantonTagClicked() {
    // Check that when the user selects a tag, it appears in the selected section with its trash
    // icon.
    scrollAndClick(BUTTON_BERN)
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.SELECTED_TAGS).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.DELETE_ICON).assertIsDisplayed()
  }

  @Test
  fun selectedTagsShownWhenMultipleTagsClicked() {
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
    // Check that if we click on the trash icon, the tag is deselected and does not appear in the
    // selected tag section.
    scrollAndClick(BUTTON_BERN)
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.DELETE_ICON).performClick()
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.SELECTED_TAGS).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.DELETE_ICON).assertIsNotDisplayed()
  }

  @Test
  fun selectedTagsHiddenWhenTagDeselected() {
    // Check that if we click again on the tag, it is deselected and does not appear in the selected
    // tag section.
    scrollAndClick(BUTTON_BERN)
    composeTestRule.onNodeWithTag(BUTTON_BERN).performClick()
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
      Assert.assertEquals(listOf("Bern", "Handball", "Metal"), displayedTags)
  }

  @Test
  fun selectedTagsMaintainOrderAfterDeselection() {
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
      Assert.assertEquals(listOf("Bern", "Handball", "Car"), displayedTags)
  }

  @Test
  fun selectedTagsRemainStableAfterRapidClicks() {
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
    // Check that selecting all tags of one type still displays them on the screen.
    selectLotsOfTags()

    composeTestRule
        .onNodeWithTag(LAZY_COLUMN_TAGS)
        .performScrollToNode(hasTestTag(SelectTagsScreenTestTags.TRANSPORT_TAGS))
        .assertIsDisplayed()
  }

  @Test
  fun selectedTagsSectionIsScrollable() {
    // Check that the selected tags section is scrollable.
    selectLotsOfTags()

    composeTestRule
        .onNodeWithTag(SelectTagsScreenTestTags.SELECTED_TAGS)
        .performScrollToNode(hasTestTag("Button_Selected_Plane"))
        .assertIsDisplayed()
  }
}