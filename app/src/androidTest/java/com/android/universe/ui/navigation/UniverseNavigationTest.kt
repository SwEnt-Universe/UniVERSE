package com.android.universe.ui.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.android.universe.UniverseApp
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UniverseAppNavigationTest : TestCase() {

  @get:Rule val composeTestRule = createComposeRule()
  @get:Rule
  val permissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION)

  @Test
  fun app_startsAtMapScreen() {
    composeTestRule.setContent { UniverseApp() }

    // Verify that Map screen is displayed
    composeTestRule.onNodeWithTag(NavigationTestTags.MAP_SCREEN).assertIsDisplayed()
  }

  @Test
  fun navigation_toChatScreen() {
    composeTestRule.setContent { UniverseApp() }

    // Click on Chat tab (simulate tab selection)
    composeTestRule.onNodeWithTag(NavigationTestTags.CHAT_TAB).performClick()

    // Verify that Chat screen is displayed
    composeTestRule.onNodeWithTag(NavigationTestTags.CHAT_SCREEN).assertIsDisplayed()
  }

  @Test
  fun navigation_toEventScreen() {
    composeTestRule.setContent { UniverseApp() }

    // Click on Event tab (simulate tab selection)
    composeTestRule.onNodeWithTag(NavigationTestTags.EVENT_TAB).performClick()

    // Verify Event Chat screen is displayed
    composeTestRule.onNodeWithTag(NavigationTestTags.EVENT_SCREEN).assertIsDisplayed()
  }

  @Test
  fun navigation_toProfileScreen() {
    composeTestRule.setContent { UniverseApp() }

    // Click on Profile tab (simulate tab selection)
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()

    // Verify that Profile screen is displayed
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_SCREEN).assertIsDisplayed()
  }

  @Test
  fun navigation_toAllTabs() {
    composeTestRule.setContent { UniverseApp() }
    // Verify that Map screen is displayed
    composeTestRule.onNodeWithTag(NavigationTestTags.MAP_SCREEN).assertIsDisplayed()
    // Navigate to all tabs
    tabs.forEach { tab ->
      composeTestRule.onNodeWithTag(NavigationTestTags.getTabTestTag(tab)).performClick()
      composeTestRule
          .onNodeWithTag(NavigationTestTags.getScreenTestTag(tab.destination))
          .assertIsDisplayed()
    }
  }
}
