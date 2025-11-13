package com.android.universe.ui.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.utils.setContentWithStubBackdrop
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationPlaceholderScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun displaysTitle() {
    val title = "Test Screen"

    composeTestRule.setContentWithStubBackdrop {
      NavigationPlaceholderScreen(
          title = title, selectedTab = Tab.Map, onTabSelected = {}, testTag = null)
    }

    composeTestRule.onNodeWithText(title).assertIsDisplayed()
  }

  @Test
  fun hasTestTag_whenProvided() {
    val testTag = "example_test_tag"

    composeTestRule.setContentWithStubBackdrop {
      NavigationPlaceholderScreen(
          title = "Title", selectedTab = Tab.Map, onTabSelected = {}, testTag = testTag)
    }

    composeTestRule.onNodeWithTag(testTag).assertExists().assertIsDisplayed()
  }

  @Test
  fun bottomBar_isVisible_whenEnabled() {

    composeTestRule.setContentWithStubBackdrop {
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

    composeTestRule.setContentWithStubBackdrop {
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

    composeTestRule.setContentWithStubBackdrop {
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
