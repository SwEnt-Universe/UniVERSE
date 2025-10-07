package com.android.universe.ui.selectedTagScreen

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.android.universe.model.Tag
import com.android.universe.model.user.FakeUserRepository
import com.android.universe.model.user.UserProfile
import com.android.universe.ui.SelectTagScreen
import com.android.universe.ui.SelectTagViewModel
import com.android.universe.ui.SelectTagsScreenTestTags
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SelectTagScreenTest {
  fun scrollAndClick(scrollName: String, clickName: String) {
    composeTestRule.onNodeWithTag(scrollName).performScrollToNode(hasTestTag(clickName))
    composeTestRule.onNodeWithTag(clickName).performClick()
  }

  @get:Rule val composeTestRule = createComposeRule()
  private lateinit var userRepository: FakeUserRepository
  private lateinit var viewModel: SelectTagViewModel

  @Before
  fun setUp() {
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
    viewModel = SelectTagViewModel(userRepository)
    composeTestRule.setContent { SelectTagScreen(viewModel, username = "bob") }
  }

  @Test
  fun allTagGroupsAreDisplayed() {
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.INTERESTTAGS).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("LazyColumnTags")
        .performScrollToNode(hasTestTag(SelectTagsScreenTestTags.SPORTTAGS))
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("LazyColumnTags")
        .performScrollToNode(hasTestTag(SelectTagsScreenTestTags.MUSICTAGS))
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("LazyColumnTags")
        .performScrollToNode(hasTestTag(SelectTagsScreenTestTags.TRANSPORTTAGS))
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("LazyColumnTags")
        .performScrollToNode(hasTestTag(SelectTagsScreenTestTags.CANTONTAGS))
        .assertIsDisplayed()
  }

  @Test
  fun saveButtonIsDisplayed() {
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.SAVEBUTTON).assertIsDisplayed()
  }

  @Test
  fun selectedTagsSectionIsHiddenInitially() {
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.SELECTEDTAGS).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.DELETEICON).assertIsNotDisplayed()
  }

  @Test
  fun selectedTagsShownWhenInterestTagClicked() {
    composeTestRule.onNodeWithTag("Button_Reading").performClick()
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.SELECTEDTAGS).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.DELETEICON).assertIsDisplayed()
  }

  @Test
  fun selectedTagsShownWhenSportTagClicked() {
    scrollAndClick("LazyColumnTags", "Button_Handball")
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.SELECTEDTAGS).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.DELETEICON).assertIsDisplayed()
  }

  @Test
  fun selectedTagsShownWhenMusicTagClicked() {
    scrollAndClick("LazyColumnTags", "Button_Metal")
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.SELECTEDTAGS).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.DELETEICON).assertIsDisplayed()
  }

  @Test
  fun selectedTagsShownWhenTransportTagClicked() {
    scrollAndClick("LazyColumnTags", "Button_Car")
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.SELECTEDTAGS).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.DELETEICON).assertIsDisplayed()
  }

  @Test
  fun selectedTagsShownWhenCantonTagClicked() {
    scrollAndClick("LazyColumnTags", "Button_Bern")
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.SELECTEDTAGS).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.DELETEICON).assertIsDisplayed()
  }

  @Test
  fun selectedTagsShownWhenMultipleTagsClicked() {
    scrollAndClick("LazyColumnTags", "Button_Bern")
    scrollAndClick("LazyColumnTags", "Button_Surfing")
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.SELECTEDTAGS).assertIsDisplayed()
    val deleteIcons = composeTestRule.onAllNodesWithTag(SelectTagsScreenTestTags.DELETEICON)
    deleteIcons.assertAny(hasClickAction())
  }

  @Test
  fun selectedTagsHiddenAfterDeleteClicked() {
    scrollAndClick("LazyColumnTags", "Button_Bern")
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.DELETEICON).performClick()
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.SELECTEDTAGS).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.DELETEICON).assertIsNotDisplayed()
  }

  @Test
  fun selectedTagsHiddenWhenTagDeselected() {
    scrollAndClick("LazyColumnTags", "Button_Bern")
    composeTestRule.onNodeWithTag("Button_Bern").performClick()
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.SELECTEDTAGS).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.DELETEICON).assertIsNotDisplayed()

    scrollAndClick("LazyColumnTags", "Button_Bern")
    scrollAndClick("LazyColumnTags", "Button_Surfing")
    scrollAndClick("LazyColumnTags", "Button_Bern")
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.SELECTEDTAGS).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.DELETEICON).assertIsDisplayed()
  }

  @Test
  fun selectedTagsDisplayedInCorrectOrder() {
    scrollAndClick("LazyColumnTags", "Button_Bern")
    scrollAndClick("LazyColumnTags", "Button_Handball")
    scrollAndClick("LazyColumnTags", "Button_Metal")

    val selectedTagNodes =
        composeTestRule
            .onAllNodes(hasParent(hasTestTag(SelectTagsScreenTestTags.SELECTEDTAGS)))
            .fetchSemanticsNodes()
    val displayedTags =
        selectedTagNodes.mapNotNull {
          it.config.getOrNull(SemanticsProperties.Text)?.firstOrNull()?.text
        }
    assertEquals(listOf("Bern", "Handball", "Metal"), displayedTags)
  }

  @Test
  fun selectedTagsMaintainOrderAfterDeselection() {
    scrollAndClick("LazyColumnTags", "Button_Bern")
    scrollAndClick("LazyColumnTags", "Button_Handball")
    scrollAndClick("LazyColumnTags", "Button_Metal")
    scrollAndClick("LazyColumnTags", "Button_Car")
    scrollAndClick("LazyColumnTags", "Button_Metal")

    val selectedTagNodes =
        composeTestRule
            .onAllNodes(hasParent(hasTestTag(SelectTagsScreenTestTags.SELECTEDTAGS)))
            .fetchSemanticsNodes()
    val displayedTags =
        selectedTagNodes.mapNotNull {
          it.config.getOrNull(SemanticsProperties.Text)?.firstOrNull()?.text
        }
    assertEquals(listOf("Bern", "Handball", "Car"), displayedTags)
  }

  @Test
  fun selectedTagsRemainStableAfterRapidClicks() {
    scrollAndClick("LazyColumnTags", "Button_Bern")
    scrollAndClick("LazyColumnTags", "Button_Bern")
    scrollAndClick("LazyColumnTags", "Button_Bern")
    scrollAndClick("LazyColumnTags", "Button_Bern")
    scrollAndClick("LazyColumnTags", "Button_Bern")
    scrollAndClick("LazyColumnTags", "Button_Bern")
    scrollAndClick("LazyColumnTags", "Button_Bern")
    scrollAndClick("LazyColumnTags", "Button_Bern")
    scrollAndClick("LazyColumnTags", "Button_Bern")
    scrollAndClick("LazyColumnTags", "Button_Bern")
    scrollAndClick("LazyColumnTags", "Button_Bern")

    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.SELECTEDTAGS).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.DELETEICON).assertIsDisplayed()
  }

  @Test
  fun tagStillVisibleWhenAllSelected() {
    scrollAndClick("LazyColumnTags", "Button_Car")
    scrollAndClick("LazyColumnTags", "Button_Train")
    scrollAndClick("LazyColumnTags", "Button_Boat")
    scrollAndClick("LazyColumnTags", "Button_Bus")
    scrollAndClick("LazyColumnTags", "Button_Bicycle")
    scrollAndClick("LazyColumnTags", "Button_Foot")
    scrollAndClick("LazyColumnTags", "Button_Plane")

    composeTestRule
        .onNodeWithTag("LazyColumnTags")
        .performScrollToNode(hasTestTag(SelectTagsScreenTestTags.TRANSPORTTAGS))
        .assertIsDisplayed()
  }

  @Test
  fun selectedTagsSectionIsScrollable() {
    scrollAndClick("LazyColumnTags", "Button_Car")
    scrollAndClick("LazyColumnTags", "Button_Train")
    scrollAndClick("LazyColumnTags", "Button_Boat")
    scrollAndClick("LazyColumnTags", "Button_Bus")
    scrollAndClick("LazyColumnTags", "Button_Bicycle")
    scrollAndClick("LazyColumnTags", "Button_Foot")
    scrollAndClick("LazyColumnTags", "Button_Plane")

    composeTestRule
        .onNodeWithTag(SelectTagsScreenTestTags.SELECTEDTAGS)
        .performScrollToNode(hasTestTag("Button_Selected_Plane"))
        .assertIsDisplayed()
  }

  @Test
  fun selectedTagsAreSavedForUser() = runBlocking {
    scrollAndClick("LazyColumnTags", "Button_Car")
    scrollAndClick("LazyColumnTags", "Button_Handball")
    scrollAndClick("LazyColumnTags", "Button_Metal")

    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.SAVEBUTTON).performClick()
    assertEquals(
        userRepository.getUser("bob").tags, listOf<Tag>(Tag("Car"), Tag("Handball"), Tag("Metal")))
  }
}
