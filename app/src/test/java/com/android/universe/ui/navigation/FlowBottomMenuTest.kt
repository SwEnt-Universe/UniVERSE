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
      FlowBottomMenu(onBackClicked = {}, onContinueClicked = {})
    }

    composeTestRule.onNodeWithTag(FlowBottomMenuTestTags.MENU).assertIsDisplayed()
    composeTestRule.onNodeWithTag(FlowBottomMenuTestTags.BACK_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(FlowBottomMenuTestTags.CONTINUE_BUTTON).assertIsDisplayed()
  }

  @Test
  fun flowBottomMenu_invokesCallbacks_whenClicked() {
    var backClicked = false
    var continueClicked = false

    composeTestRule.setContentWithStubBackdrop {
      FlowBottomMenu(
          onBackClicked = { backClicked = true }, onContinueClicked = { continueClicked = true })
    }

    composeTestRule.onNodeWithTag(FlowBottomMenuTestTags.BACK_BUTTON).performClick()
    assertTrue("Back callback should be invoked", backClicked)

    composeTestRule.onNodeWithTag(FlowBottomMenuTestTags.CONTINUE_BUTTON).performClick()
    assertTrue("Continue callback should be invoked", continueClicked)
  }
}
