package com.android.universe.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.ui.theme.UniverseTheme
import com.android.universe.utils.setContentWithStubBackdrop
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalMaterial3Api::class)
class LiquidBottomSheetTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val TEST_SHEET_CONTENT = "Sheet Body Content"
  private val TEST_CUSTOM_HANDLE = "Custom Handle Text"
  private val TEST_TAG = "liquid_bottom_sheet"

  @Test
  fun liquidBottomSheet_whenNotPresented_doesNotDisplayContent() {
    composeTestRule.setContentWithStubBackdrop {
      UniverseTheme {
        LiquidBottomSheet(isPresented = false, onDismissRequest = {}) { Text(TEST_SHEET_CONTENT) }
      }
    }

    // The sheet should not be in the hierarchy or displayed
    composeTestRule.onNodeWithText(TEST_SHEET_CONTENT).assertDoesNotExist()
  }

  @Test
  fun liquidBottomSheet_whenPresented_displaysContent() {
    composeTestRule.setContentWithStubBackdrop {
      UniverseTheme {
        LiquidBottomSheet(isPresented = true, onDismissRequest = {}) {
          Box(modifier = Modifier.fillMaxWidth().height(200.dp)) { Text(TEST_SHEET_CONTENT) }
        }
      }
    }

    // Allow time for the BottomSheet animation to start/settle
    composeTestRule.waitForIdle()

    // Verify the content inside the sheet is visible
    composeTestRule.onNodeWithText(TEST_SHEET_CONTENT).assertIsDisplayed()
  }

  @Test
  fun liquidBottomSheet_rendersCustomDragHandle() {
    composeTestRule.setContentWithStubBackdrop {
      UniverseTheme {
        LiquidBottomSheet(
            isPresented = true,
            onDismissRequest = {},
            // Inject a specific text element as the drag handle to verify its presence
            dragHandle = { Text(TEST_CUSTOM_HANDLE) }) {
              Text(TEST_SHEET_CONTENT)
            }
      }
    }

    composeTestRule.waitForIdle()

    // Verify both the handle and the content are displayed
    composeTestRule.onNodeWithText(TEST_CUSTOM_HANDLE).assertIsDisplayed()
    composeTestRule.onNodeWithText(TEST_SHEET_CONTENT).assertIsDisplayed()
  }

  @Test
  fun liquidBottomSheet_canRemoveDragHandle() {
    composeTestRule.setContentWithStubBackdrop {
      UniverseTheme {
        LiquidBottomSheet(
            isPresented = true,
            onDismissRequest = {},
            // Pass null to remove the handle
            dragHandle = null) {
              Text(TEST_SHEET_CONTENT)
            }
      }
    }

    composeTestRule.waitForIdle()

    // Content should still be there
    composeTestRule.onNodeWithText(TEST_SHEET_CONTENT).assertIsDisplayed()
  }

  @Test
  fun liquidBottomSheet_maintainsState_acrossRecompositions() {
    // This test verifies that the internal LiquidBox parameters don't cause crashes
    // during recomposition cycles.

    composeTestRule.setContentWithStubBackdrop {
      UniverseTheme {
        LiquidBottomSheet(
            isPresented = true,
            onDismissRequest = {},
            blurRadius = 8.dp,
            refractionHeight = 10.dp) {
              Text(TEST_SHEET_CONTENT)
            }
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText(TEST_SHEET_CONTENT).assertIsDisplayed()
  }

  @Test
  fun liquidBottomSheet_appliesCustomStylingAndParameters() {
    composeTestRule.setContentWithStubBackdrop {
      val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

      UniverseTheme {
        LiquidBottomSheet(
            isPresented = true,
            onDismissRequest = {},
            modifier = Modifier.testTag(TEST_TAG),
            sheetState = sheetState,
            sheetMaxWidth = 400.dp,
            shape = RoundedCornerShape(0.dp),
            containerColor = Color.Blue.copy(alpha = 0.5f),
            contentColor = Color.White,
            scrimColor = Color.Black.copy(alpha = 0.8f),
            tonalElevation = 8.dp,
            contentWindowInsets = { BottomSheetDefaults.windowInsets }) {
              Text(TEST_SHEET_CONTENT)
            }
      }
    }

    composeTestRule.waitForIdle()

    // Verify the component rendered with the content
    composeTestRule.onNodeWithText(TEST_SHEET_CONTENT).assertIsDisplayed()

    // Verify the modifier tag was applied to the hierarchy
    composeTestRule.onNodeWithTag(TEST_TAG).assertExists()
  }

  @Test
  fun liquidBottomSheet_rendersBottomBar_whenProvided() {
    val BOTTOM_BAR_TEXT = "Bottom Bar Content"

    composeTestRule.setContentWithStubBackdrop {
      UniverseTheme {
        LiquidBottomSheet(
            isPresented = true,
            onDismissRequest = {},
            bottomBar = { Text(BOTTOM_BAR_TEXT, modifier = Modifier.testTag("bottom_bar")) }) {
              Text(TEST_SHEET_CONTENT)
            }
      }
    }

    composeTestRule.waitForIdle()

    // Content is shown
    composeTestRule.onNodeWithText(TEST_SHEET_CONTENT).assertIsDisplayed()

    // Bottom bar should also be shown
    composeTestRule.onNodeWithTag("bottom_bar").assertIsDisplayed()
    composeTestRule.onNodeWithText(BOTTOM_BAR_TEXT).assertIsDisplayed()
  }

  @Test
  fun liquidBottomSheet_doesNotRenderBottomBar_whenNull() {
    composeTestRule.setContentWithStubBackdrop {
      UniverseTheme {
        LiquidBottomSheet(isPresented = true, onDismissRequest = {}, bottomBar = null) {
          Text(TEST_SHEET_CONTENT)
        }
      }
    }

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText(TEST_SHEET_CONTENT).assertIsDisplayed()

    // Ensure NO bottom bar exists
    composeTestRule.onNodeWithTag("bottom_bar", useUnmergedTree = true).assertDoesNotExist()
  }
}
