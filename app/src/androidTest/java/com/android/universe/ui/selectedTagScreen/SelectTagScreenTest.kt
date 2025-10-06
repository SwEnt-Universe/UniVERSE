package com.android.universe.ui.selectedTagScreen

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.android.universe.ui.SelectTagScreen
import com.android.universe.ui.SelectTagsScreenTestTags
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

  @Before
  fun setUp() {
    composeTestRule.setContent { SelectTagScreen() }
  }

  @Test
  fun isAllTagDisplayed() {
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
  fun isSaveTagDisplayed() {
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.SAVEBUTTON).assertIsDisplayed()
  }

  @Test
  fun isSelectedTagsNotDisplayed() {
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.SELECTEDTAGS).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.DELETEICON).assertIsNotDisplayed()
  }

  @Test
  fun isSelectedTagsDisplayedOnClickInterest() {
    composeTestRule.onNodeWithTag("Button_Reading").performClick()
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.SELECTEDTAGS).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.DELETEICON).assertIsDisplayed()
  }

  @Test
  fun isSelectedTagsDisplayedOnClickSport() {
    scrollAndClick("LazyColumnTags", "Button_Handball")
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.SELECTEDTAGS).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.DELETEICON).assertIsDisplayed()
  }

  @Test
  fun isSelectedTagsDisplayedOnClickMusic() {
    scrollAndClick("LazyColumnTags", "Button_Metal")
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.SELECTEDTAGS).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.DELETEICON).assertIsDisplayed()
  }

  @Test
  fun isSelectedTagsDisplayedOnClickTransport() {
    scrollAndClick("LazyColumnTags", "Button_Car")
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.SELECTEDTAGS).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.DELETEICON).assertIsDisplayed()
  }

  @Test
  fun isSelectedTagsDisplayedOnClickCanton() {
    scrollAndClick("LazyColumnTags", "Button_Bern")
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.SELECTEDTAGS).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.DELETEICON).assertIsDisplayed()
  }

  @Test
  fun isSelectedTagsDisplayedOnClick2Tags() {
    scrollAndClick("LazyColumnTags", "Button_Bern")
    scrollAndClick("LazyColumnTags", "Button_Surfing")
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.SELECTEDTAGS).assertIsDisplayed()
    val deleteIcons = composeTestRule.onAllNodesWithTag(SelectTagsScreenTestTags.DELETEICON)
    deleteIcons.assertAny(hasClickAction())
  }

  @Test
  fun isSelectedTagsDisplayedWithDelete() {
    scrollAndClick("LazyColumnTags", "Button_Bern")
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.DELETEICON).performClick()
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.SELECTEDTAGS).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.DELETEICON).assertIsNotDisplayed()
  }

  @Test
  fun isSelectedTagsDisplayedWithDeselect() {
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
  fun isSelectedTagsInOrder() {
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
  fun isTagStillDisplayedIfAllSelected() {
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
  fun isSelectedTagsScrollable() {
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
}
