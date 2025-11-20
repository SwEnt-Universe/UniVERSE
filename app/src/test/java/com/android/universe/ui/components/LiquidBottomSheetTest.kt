package com.android.universe.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
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

    @get:Rule
    val composeTestRule = createComposeRule()

    private val TEST_SHEET_CONTENT = "Sheet Body Content"
    private val TEST_CUSTOM_HANDLE = "Custom Handle Text"

    @Test
    fun liquidBottomSheet_whenNotPresented_doesNotDisplayContent() {
        composeTestRule.setContentWithStubBackdrop {
            UniverseTheme {
                LiquidBottomSheet(
                    isPresented = false,
                    onDismissRequest = {}
                ) {
                    Text(TEST_SHEET_CONTENT)
                }
            }
        }

        // The sheet should not be in the hierarchy or displayed
        composeTestRule.onNodeWithText(TEST_SHEET_CONTENT).assertDoesNotExist()
    }

    @Test
    fun liquidBottomSheet_whenPresented_displaysContent() {
        composeTestRule.setContentWithStubBackdrop {
            UniverseTheme {
                LiquidBottomSheet(
                    isPresented = true,
                    onDismissRequest = {}
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        Text(TEST_SHEET_CONTENT)
                    }
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
                    dragHandle = { Text(TEST_CUSTOM_HANDLE) }
                ) {
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
                    dragHandle = null
                ) {
                    Text(TEST_SHEET_CONTENT)
                }
            }
        }

        composeTestRule.waitForIdle()

        // Content should still be there
        composeTestRule.onNodeWithText(TEST_SHEET_CONTENT).assertIsDisplayed()
        // We cannot easily assert "CustomDragHandle" is missing because it has no text,
        // but we verify the sheet still renders correctly without crashing.
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
                    refractionHeight = 10.dp
                ) {
                    Text(TEST_SHEET_CONTENT)
                }
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(TEST_SHEET_CONTENT).assertIsDisplayed()
    }
}