package com.android.universe.ui.navigation

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.utils.setContentWithStubBackdrop
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationBottomMenuTest {
  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun bottomMenu_displaysAllTabs() {
    composeTestRule.setContentWithStubBackdrop { NavigationBottomMenuPreview() }
    // Assert: bottom bar is displayed
    composeTestRule.onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
    // Assert: all tabs is displayed
    tabs.forEach { tab ->
      composeTestRule.onNodeWithTag(NavigationTestTags.getTabTestTag(tab)).assertIsDisplayed()
    }
  }

  @Test
  fun bottomMenu_selectsTab_whenClicked() {
    val selectedTab = mutableStateOf<Tab>(Tab.Chat)

    composeTestRule.setContentWithStubBackdrop {
      NavigationBottomMenu(selectedTab = selectedTab.value) { clickedTab ->
        selectedTab.value = clickedTab
      }
    }

    // Assert: selected tab is the one clicked on
    tabs.forEach { tab ->
      composeTestRule.onNodeWithTag(NavigationTestTags.getTabTestTag(tab)).performClick()
      assertEquals(tab, selectedTab.value)
    }
  }
}
