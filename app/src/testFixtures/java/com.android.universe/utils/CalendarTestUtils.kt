package com.android.universe.utils

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.android.universe.ui.common.GeneralDatePopUpTestTags
import com.android.universe.ui.common.GeneralTimePopUpTestTags
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Selecting an already selected hour will give an error as 2 nodes in the node tree will have the
 * same content description.
 *
 * @param rule the compose test rule of this test
 * @param hour the hour to select.
 */
fun selectHour(rule: ComposeTestRule, hour: Int) {
  require(hour in 0..23)

  rule
      .onNode(hasContentDescription("Select hour", ignoreCase = true))
      .assertIsDisplayed()
      .performClick()

  rule.waitForIdle()

  rule
      .onNode(hasContentDescription("$hour hours"), useUnmergedTree = true)
      .assertIsDisplayed()
      .performClick()

  rule.waitForIdle()
}

/**
 * Selecting an already selected minute will give an error as 2 nodes in the node tree will have the
 * same content description.
 *
 * @param rule the compose test rule of this test class
 * @param minute5 the minute to select, a multiple of 5.
 */
fun selectMinute(rule: ComposeTestRule, minute5: Int) {
  require(minute5 in 0..59)
  require(minute5 % 5 == 0)

  rule
      .onNode(hasContentDescription("Select minutes", ignoreCase = true))
      .assertIsDisplayed()
      .performClick()

  rule.waitForIdle()

  rule
      .onNode(hasContentDescription("$minute5 minutes"), useUnmergedTree = true)
      .assertIsDisplayed()
      .performClick()

  rule.waitForIdle()
}

/**
 * Selects a year in the calendar
 *
 * @param rule the compose test rule of this test class
 * @param year the year to select.
 */
fun selectYear(rule: ComposeTestRule, year: Int) {
  rule
      .onNode(hasContentDescription("Switch to selecting a year"), useUnmergedTree = true)
      .assertIsDisplayed()
      .performClick()

  rule
      .onNodeWithText("Navigate to year $year", useUnmergedTree = true)
      .assertIsDisplayed()
      .performClick()

  rule.waitForIdle()
}

/**
 * Presses on the next month button
 *
 * @param rule the compose test rule of this test class
 */
fun nextMonth(rule: ComposeTestRule) {
  rule
      .onNode(hasContentDescription("Change to next month"), useUnmergedTree = true)
      .assertIsDisplayed()
      .performClick()

  rule.waitForIdle()
}

/**
 * Presses on the previous month button
 *
 * @param rule the compose test rule of this test class
 */
fun previousMonth(rule: ComposeTestRule) {
  rule
      .onNode(hasContentDescription("Change to previous month"), useUnmergedTree = true)
      .assertIsDisplayed()
      .performClick()

  rule.waitForIdle()
}

/**
 * Selects a day in the calendar
 *
 * @param rule the compose test rule of this test class
 * @param monthDay the date containing the current month's page and the day to select
 */
fun selectDay(rule: ComposeTestRule, monthDay: LocalDate) {
  val formatMonth = monthDay.format(DateTimeFormatter.ofPattern("MMMM", Locale.ENGLISH))

  rule
      .onNodeWithText(
          "$formatMonth ${monthDay.dayOfMonth}", substring = true, useUnmergedTree = true)
      .assertIsDisplayed()
      .performClick()
  rule.waitForIdle()
}

/**
 * Presses the confirm button for a date picker dialog
 *
 * @param rule the compose test rule of this test class
 */
fun pressOKDate(rule: ComposeTestRule) {
  rule.onNodeWithTag(GeneralDatePopUpTestTags.CONFIRM_BUTTON).assertIsDisplayed().performClick()
  rule.waitForIdle()
}

/**
 * Presses the confirm button for a time picker dialog
 *
 * @param rule the compose test rule of this test class
 */
fun pressOKTime(rule: ComposeTestRule) {
  rule.onNodeWithTag(GeneralTimePopUpTestTags.CONFIRM_BUTTON).assertIsDisplayed().performClick()
  rule.waitForIdle()
}
