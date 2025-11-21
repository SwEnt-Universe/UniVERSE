package com.android.universe.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
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

  @Test
  fun liquidTopBar_andChildren_respectInjectedModifiers() {
    var clicked = false

    val backButtonTag = "BackButtonTag"
    val titleTag = "TitleTag"

    composeTestRule.setContentWithStubBackdrop {
      MaterialTheme {
        LiquidTopBar(
            navigationIcon = {
              TopBarBackButton(
                  onClick = { clicked = true }, modifier = Modifier.testTag(backButtonTag))
            },
            title = { TopBarTitle(text = "Hello Liquid", modifier = Modifier.testTag(titleTag)) })
      }
    }

    // Verify children inside LiquidBox
    composeTestRule.onNodeWithTag(backButtonTag).assertIsDisplayed().performClick()
    assertTrue(clicked)

    composeTestRule.onNodeWithTag(titleTag).assertIsDisplayed()
  }
}
