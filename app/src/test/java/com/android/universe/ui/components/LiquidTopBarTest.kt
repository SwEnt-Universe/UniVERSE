package com.android.universe.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.ui.navigation.NavigationTestTags
import com.android.universe.utils.setContentWithStubBackdrop
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LiquidTopBarTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun topBarTitle_isDisplayed() {
    val sampleText = "Hello World"

    composeTestRule.setContentWithStubBackdrop { MaterialTheme { TopBarTitle(text = sampleText) } }

    composeTestRule
        .onNodeWithTag(TopBarTestTags.TOP_BAR_TITLE)
        .assertIsDisplayed()
        .assertTextEquals(sampleText)
  }

  @Test
  fun topBarBackButton_callsOnClick() {
    var clicked = false

    composeTestRule.setContentWithStubBackdrop {
      MaterialTheme { TopBarBackButton(onClick = { clicked = true }) }
    }

    composeTestRule
        .onNodeWithTag(NavigationTestTags.BACK_BUTTON)
        .assertHasClickAction()
        .performClick()

    assertTrue(clicked)
  }

  @Test
  fun liquidTopBar_renders_navigationIcon_and_title() {
    val titleText = "Screen Title"

    composeTestRule.setContentWithStubBackdrop {
      MaterialTheme {
        LiquidTopBar(
            navigationIcon = { TopBarBackButton(onClick = {}) },
            title = { TopBarTitle(text = titleText) })
      }
    }

    // Check back button appears
    composeTestRule.onNodeWithTag(NavigationTestTags.BACK_BUTTON).assertIsDisplayed()

    // Check title appears
    composeTestRule
        .onNodeWithTag(TopBarTestTags.TOP_BAR_TITLE)
        .assertIsDisplayed()
        .assertTextEquals(titleText)
  }
}
