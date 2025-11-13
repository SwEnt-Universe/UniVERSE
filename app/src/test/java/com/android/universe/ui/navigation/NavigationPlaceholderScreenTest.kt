package com.android.universe.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.ui.utils.LocalLayerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationPlaceholderScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private fun setContentWithStubBackdrop(content: @Composable () -> Unit) {
    composeTestRule.setContent {
      val stubBackdrop = rememberLayerBackdrop { drawRect(Color.Transparent) }

      CompositionLocalProvider(LocalLayerBackdrop provides stubBackdrop) { content() }
    }
  }

  @Test
  fun displaysTitle() {
    val title = "Test Screen"

    setContentWithStubBackdrop {
      NavigationPlaceholderScreen(
          title = title, selectedTab = Tab.Map, onTabSelected = {}, testTag = null)
    }

    composeTestRule.onNodeWithText(title).assertIsDisplayed()
  }

  @Test
  fun hasTestTag_whenProvided() {
    val testTag = "example_test_tag"

    setContentWithStubBackdrop {
      NavigationPlaceholderScreen(
          title = "Title", selectedTab = Tab.Map, onTabSelected = {}, testTag = testTag)
    }

    composeTestRule.onNodeWithTag(testTag).assertExists().assertIsDisplayed()
  }

  @Test
  fun bottomBar_isVisible_whenEnabled() {

    setContentWithStubBackdrop {
      NavigationPlaceholderScreen(
          title = "With Bottom Bar",
          selectedTab = Tab.Map,
          onTabSelected = {},
          enableBottomBar = true)
    }

    composeTestRule.onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
  }

  @Test
  fun bottomBar_isHidden_whenDisabled() {

    setContentWithStubBackdrop {
      NavigationPlaceholderScreen(
          title = "No Bottom Bar",
          selectedTab = Tab.Map,
          onTabSelected = {},
          enableBottomBar = false)
    }

    composeTestRule.onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertDoesNotExist()
  }

  @Test
  fun clickingTab_invokesCallback() {
    var selectedTab: Tab? = null

    setContentWithStubBackdrop {
      NavigationPlaceholderScreen(
          title = "Tabs",
          selectedTab = Tab.Map,
          onTabSelected = { selectedTab = it },
      )
    }

    composeTestRule.onNodeWithTag(NavigationTestTags.CHAT_TAB).performClick()
    composeTestRule.runOnIdle { assertEquals(Tab.Chat, selectedTab) }
  }
}
