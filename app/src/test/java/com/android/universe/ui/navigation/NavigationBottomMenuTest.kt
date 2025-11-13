package com.android.universe.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.ui.utils.LocalLayerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationBottomMenuTest {
  @get:Rule val composeTestRule = createComposeRule()

  private fun setContentWithStubBackdrop(content: @Composable () -> Unit) {
    composeTestRule.setContent {
      val stubBackdrop = rememberLayerBackdrop { drawRect(Color.Transparent) }

      CompositionLocalProvider(LocalLayerBackdrop provides stubBackdrop) { content() }
    }
  }

  @Test
  fun bottomMenu_displaysAllTabs() {
    setContentWithStubBackdrop { NavigationBottomMenuPreview() }
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

    setContentWithStubBackdrop {
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
