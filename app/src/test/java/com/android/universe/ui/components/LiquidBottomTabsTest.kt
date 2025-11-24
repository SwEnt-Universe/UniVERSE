package com.android.universe.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.utils.setContentWithStubBackdrop
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LiquidBottomTabsTest {

  @get:Rule val composeTestRule = createComposeRule()

  private companion object {
    const val TAG_LIQUID_TABS = "LiquidTabs"
    const val TAG_TAB_0 = "Tab0"
    const val TAG_TAB_1 = "Tab1"
  }

  @Test
  fun liquidBottomTabs_rendersContent() {
    composeTestRule.setContentWithStubBackdrop {
      MaterialTheme {
        LiquidBottomTabs(selectedTabIndex = { 0 }, onTabSelected = {}, tabsCount = 2) {
          Text("Home")
          Text("Settings")
        }
      }
    }

    // Assert that both content items are rendered
    composeTestRule.onNodeWithText("Home").assertExists()
    composeTestRule.onNodeWithText("Settings").assertExists()
  }

  @Test
  fun liquidBottomTabs_reactsToStateChangeAndCallsCallback() {
    var selectedIndex by mutableStateOf(0)
    var onTabSelectedCalledWith: Int? = null

    composeTestRule.setContentWithStubBackdrop {
      MaterialTheme {
        LiquidBottomTabs(
            selectedTabIndex = { selectedIndex },
            onTabSelected = { onTabSelectedCalledWith = it },
            tabsCount = 2) {
              Text("Home")
              Text("Settings")
            }
      }
    }

    // Act: Change the state externally, simulating a user tap on a tab item
    composeTestRule.runOnIdle { selectedIndex = 1 }

    // Assert: Wait for compose to recompose and animations to settle.
    // The LaunchedEffect in LiquidBottomTabs should fire and call onTabSelected.
    // We use runOnIdle to execute the assertion block after the UI has processed
    // the state change and become idle.
    composeTestRule.runOnIdle {
      assertEquals("onTabSelected should be called with the new index", 1, onTabSelectedCalledWith)
    }
  }

  @Test
  fun liquidBottomTabs_providesLocalScaleToContent() {
    composeTestRule.setContentWithStubBackdrop {
      MaterialTheme {
        LiquidBottomTabs(selectedTabIndex = { 0 }, onTabSelected = {}, tabsCount = 1) {
          // Consume the provided local and display its value
          val scale = LocalLiquidBottomTabScale.current()
          Text("ScaleValue:${scale}")
        }
      }
    }

    // At rest, the pressProgress is 0f.
    // lerp(1f, 1.2f, 0f) results in 1.0f.
    composeTestRule.onNodeWithText("ScaleValue:1.0").assertExists()
  }

  @Test
  fun liquidBottomTabs_initialStateMinusOne_doesNotCrash() {
    composeTestRule.setContentWithStubBackdrop {
      MaterialTheme {
        LiquidBottomTabs(
            selectedTabIndex = { -1 },
            onTabSelected = {},
            tabsCount = 2,
            modifier = Modifier.testTag(TAG_LIQUID_TABS)) {
              Box(Modifier.size(50.dp))
              Box(Modifier.size(50.dp))
            }
      }
    }

    composeTestRule.onNodeWithTag(TAG_LIQUID_TABS).assertIsDisplayed()
  }

  @Test
  fun liquidBottomTabs_transitionFromNoneToSelected_updateState() {
    composeTestRule.setContentWithStubBackdrop {
      MaterialTheme {
        val selectedIndex = remember { mutableIntStateOf(-1) }

        LiquidBottomTabs(
            selectedTabIndex = { selectedIndex.intValue },
            onTabSelected = { selectedIndex.intValue = it },
            tabsCount = 2,
            modifier = Modifier.testTag(TAG_LIQUID_TABS)) {
              LiquidBottomTab(
                  onClick = { selectedIndex.intValue = 0 },
                  modifier = Modifier.testTag(TAG_TAB_0)) {
                    Box(Modifier.size(50.dp))
                  }

              LiquidBottomTab(
                  onClick = { selectedIndex.intValue = 1 },
                  modifier = Modifier.testTag(TAG_TAB_1)) {
                    Box(Modifier.size(50.dp))
                  }
            }
      }
    }

    composeTestRule.onNodeWithTag(TAG_LIQUID_TABS).assertIsDisplayed()
    composeTestRule.onNodeWithTag(TAG_TAB_0).performClick()
    composeTestRule.onNodeWithTag(TAG_TAB_1).assertIsDisplayed()
  }
}
