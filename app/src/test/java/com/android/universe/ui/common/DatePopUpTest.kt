package com.android.universe.ui.common

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.time.LocalDate
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatePopUpTest {
  companion object {
    val date = LocalDate.of(2025, 1, 1)
    val range = 2000..2030
  }

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun dialog_is_displayed_when_visible() {
    composeTestRule.setContent {
      UniversalDatePickerDialog(
          visible = true, initialDate = date, yearRange = range, onDismiss = {}, onConfirm = {})
    }

    composeTestRule.onNodeWithTag(GeneralDatePopUpTestTags.DATE_PICKER_DIALOG).assertIsDisplayed()
    composeTestRule.onNodeWithTag(GeneralDatePopUpTestTags.DATE_PICKER).assertIsDisplayed()
  }

  @Test
  fun confirm_button_calls_onConfirm() {
    var confirmCalled = false

    composeTestRule.setContent {
      UniversalDatePickerDialog(
          visible = true,
          initialDate = date,
          yearRange = range,
          onDismiss = {},
          onConfirm = { confirmCalled = true })
    }

    composeTestRule.onNodeWithTag(GeneralDatePopUpTestTags.CONFIRM_BUTTON).performClick()

    assertTrue(confirmCalled)
  }

  @Test
  fun cancel_button_calls_onDismiss() {
    var dismissCalled = false

    composeTestRule.setContent {
      UniversalDatePickerDialog(
          visible = true,
          initialDate = date,
          yearRange = range,
          onDismiss = { dismissCalled = true },
          onConfirm = {})
    }

    composeTestRule.onNodeWithTag(GeneralDatePopUpTestTags.CANCEL_BUTTON).performClick()

    assertTrue(dismissCalled)
  }
}
