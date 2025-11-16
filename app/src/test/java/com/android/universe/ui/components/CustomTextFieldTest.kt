package com.android.universe.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.ui.common.ValidationState
import com.android.universe.ui.theme.UniverseTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests for the [CustomTextField] composable.
 * This test suite verifies display logic, icon visibility, password masking,
 * and validation state rendering.
 */
@RunWith(AndroidJUnit4::class)
class CustomTextFieldTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // --- Test Constants ---
    private val LABEL = "Username"
    private val PLACEHOLDER = "Enter username"
    private val VALUE = "MyUsername"
    private val ERROR_MESSAGE = "Username is too short"
    private val TOGGLE_DESCRIPTION = "Toggle visibility"
    private val INVISIBLE_SPACER_TEXT = " " // Used for the error message spacer

    @Test
    fun customTextField_displaysLabel() {
        composeTestRule.setContent {
            UniverseTheme {
                CustomTextField(
                    label = LABEL,
                    placeholder = PLACEHOLDER,
                    value = "",
                    onValueChange = {}
                )
            }
        }
        composeTestRule.onNodeWithText(LABEL).assertIsDisplayed()
    }

    @Test
    fun customTextField_displaysPlaceholder_whenValueIsEmpty() {
        composeTestRule.setContent {
            UniverseTheme {
                CustomTextField(
                    label = LABEL,
                    placeholder = PLACEHOLDER,
                    value = "",
                    onValueChange = {}
                )
            }
        }
        composeTestRule.onNodeWithText(PLACEHOLDER).assertIsDisplayed()
    }

    @Test
    fun customTextField_displaysValue_andHidesPlaceholder() {
        composeTestRule.setContent {
            UniverseTheme {
                CustomTextField(
                    label = LABEL,
                    placeholder = PLACEHOLDER,
                    value = VALUE,
                    onValueChange = {}
                )
            }
        }

        // The value is displayed
        composeTestRule.onNodeWithText(VALUE).assertIsDisplayed()
        // The placeholder is not
        composeTestRule.onNodeWithText(PLACEHOLDER).assertDoesNotExist()
    }

    @Test
    fun customTextField_displaysToggleIcon_whenCallbackIsProvided() {
        composeTestRule.setContent {
            UniverseTheme {
                CustomTextField(
                    label = LABEL,
                    placeholder = PLACEHOLDER,
                    value = VALUE,
                    onValueChange = {},
                    onToggleVisibility = {} // Callback is provided
                )
            }
        }
        composeTestRule.onNodeWithContentDescription(TOGGLE_DESCRIPTION).assertIsDisplayed()
    }

    @Test
    fun customTextField_hidesToggleIcon_whenCallbackIsNull() {
        composeTestRule.setContent {
            UniverseTheme {
                CustomTextField(
                    label = LABEL,
                    placeholder = PLACEHOLDER,
                    value = VALUE,
                    onValueChange = {},
                    onToggleVisibility = null // Callback is null
                )
            }
        }
        composeTestRule.onNodeWithContentDescription(TOGGLE_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun customTextField_onToggleVisibility_isCalled_whenIconIsClicked() {
        var toggled = false
        composeTestRule.setContent {
            UniverseTheme {
                CustomTextField(
                    label = LABEL,
                    placeholder = PLACEHOLDER,
                    value = VALUE,
                    onValueChange = {},
                    onToggleVisibility = { toggled = true } // Set the flag on click
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(TOGGLE_DESCRIPTION).performClick()
        composeTestRule.runOnIdle {
            assert(toggled) { "onToggleVisibility was not called" }
        }
    }

    @Test
    fun customTextField_hidesText_whenIsPasswordTrue() {
        composeTestRule.setContent {
            UniverseTheme {
                CustomTextField(
                    label = LABEL,
                    placeholder = PLACEHOLDER,
                    value = VALUE,
                    onValueChange = {},
                    isPassword = true
                )
            }
        }

        val passwordDots = "\u2022".repeat(VALUE.length)
        composeTestRule.onNodeWithText(passwordDots).assertIsDisplayed()
    }

    @Test
    fun customTextField_showsText_whenIsPasswordFalse() {
        composeTestRule.setContent {
            UniverseTheme {
                CustomTextField(
                    label = LABEL,
                    placeholder = PLACEHOLDER,
                    value = VALUE,
                    onValueChange = {},
                    isPassword = false
                )
            }
        }

        composeTestRule.onNodeWithText(VALUE).assertIsDisplayed()

        val passwordDots = "\u2022".repeat(VALUE.length)
        composeTestRule.onNodeWithText(passwordDots).assertDoesNotExist()
    }

    @Test
    fun customTextField_displaysError_whenStateIsInvalid() {
        composeTestRule.setContent {
            UniverseTheme {
                CustomTextField(
                    label = LABEL,
                    placeholder = PLACEHOLDER,
                    value = VALUE,
                    onValueChange = {},
                    validationState = ValidationState.Invalid(ERROR_MESSAGE)
                )
            }
        }

        // The error message is displayed
        composeTestRule.onNodeWithText(ERROR_MESSAGE).assertIsDisplayed()
        // The invisible spacer is NOT displayed
        composeTestRule.onNodeWithText(INVISIBLE_SPACER_TEXT).assertDoesNotExist()
    }

    @Test
    fun customTextField_hidesErrorAndShowsSpacer_whenStateIsNeutral() {
        composeTestRule.setContent {
            UniverseTheme {
                CustomTextField(
                    label = LABEL,
                    placeholder = PLACEHOLDER,
                    value = VALUE,
                    onValueChange = {},
                    validationState = ValidationState.Neutral
                )
            }
        }

        // The error message is not displayed
        composeTestRule.onNodeWithText(ERROR_MESSAGE).assertDoesNotExist()
        // The invisible spacer *is* displayed to prevent layout shift
        composeTestRule.onNodeWithText(INVISIBLE_SPACER_TEXT).assertIsDisplayed()
    }

    @Test
    fun customTextField_hidesErrorAndShowsSpacer_whenStateIsValid() {
        composeTestRule.setContent {
            UniverseTheme {
                CustomTextField(
                    label = LABEL,
                    placeholder = PLACEHOLDER,
                    value = VALUE,
                    onValueChange = {},
                    validationState = ValidationState.Valid
                )
            }
        }

        // The error message is not displayed
        composeTestRule.onNodeWithText(ERROR_MESSAGE).assertDoesNotExist()
        // The invisible spacer *is* displayed
        composeTestRule.onNodeWithText(INVISIBLE_SPACER_TEXT).assertIsDisplayed()
    }
}