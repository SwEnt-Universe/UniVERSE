package com.android.universe.ui.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.utils.setContentWithStubBackdrop
import junit.framework.TestCase.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FlowBottomMenuTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun flowBottomMenu_displaysAllButtons() {
    composeTestRule.setContentWithStubBackdrop {
      FlowBottomMenu(
          listOf(FlowTab.Back(onClick = {}), FlowTab.Confirm(onClick = {}, enabled = true)))
    }

    composeTestRule.onNodeWithTag(FlowBottomMenuTestTags.MENU).assertIsDisplayed()
    composeTestRule.onNodeWithTag(FlowBottomMenuTestTags.BACK_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(FlowBottomMenuTestTags.CONFIRM_BUTTON).assertIsDisplayed()
  }

  @Test
  fun flowBottomMenu_invokesCallbacks_whenClicked() {
    var backClicked = false
    var continueClicked = false

    composeTestRule.setContentWithStubBackdrop {
      FlowBottomMenu(
          listOf(
              FlowTab.Back(onClick = { backClicked = true }),
              FlowTab.Confirm(onClick = { continueClicked = true }, enabled = true)))
    }

    composeTestRule.onNodeWithTag(FlowBottomMenuTestTags.BACK_BUTTON).performClick()
    assertTrue("Back callback should be invoked", backClicked)

    composeTestRule.onNodeWithTag(FlowBottomMenuTestTags.CONFIRM_BUTTON).performClick()
    assertTrue("Continue callback should be invoked", continueClicked)
  }
}
