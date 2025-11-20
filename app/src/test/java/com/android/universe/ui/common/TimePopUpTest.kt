package com.android.universe.ui.common

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.time.LocalTime
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TimePopUpTest {
  companion object {
    val time = LocalTime.of(12, 0)
  }

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun dialog_is_displayed_when_visible() {
    composeTestRule.setContent {
      UniversalTimePickerDialog(visible = true, initialTime = time, onDismiss = {}, onConfirm = {})
    }

    // Check dialog & picker
    composeTestRule.onNodeWithTag(GeneralTimePopUpTestTags.TIME_PICKER_DIALOG).assertIsDisplayed()
    composeTestRule.onNodeWithTag(GeneralTimePopUpTestTags.TIME_PICKER).assertIsDisplayed()
  }

  @Test
  fun confirm_button_calls_onConfirm() {
    var confirmCalled = false

    composeTestRule.setContent {
      UniversalTimePickerDialog(
          visible = true, initialTime = time, onDismiss = {}, onConfirm = { confirmCalled = true })
    }

    composeTestRule.onNodeWithTag(GeneralTimePopUpTestTags.CONFIRM_BUTTON).performClick()

    assertTrue(confirmCalled)
  }

  @Test
  fun cancel_button_calls_onDismiss() {
    var dismissCalled = false

    composeTestRule.setContent {
      UniversalTimePickerDialog(
          visible = true, initialTime = time, onDismiss = { dismissCalled = true }, onConfirm = {})
    }

    composeTestRule.onNodeWithTag(GeneralTimePopUpTestTags.CANCEL_BUTTON).performClick()

    assertTrue(dismissCalled)
  }
}
