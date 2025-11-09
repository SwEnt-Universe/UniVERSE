package com.android.universe.ui.eventCreation

import androidx.compose.ui.test.assertAny
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.model.event.FakeEventRepository
import com.android.universe.model.location.Location
import com.android.universe.model.tag.Tag
import com.android.universe.model.user.FakeUserRepository
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EventCreationScreenTest {
  private lateinit var viewModel: EventCreationViewModel
  @get:Rule val composeTestRule = createComposeRule()

  companion object {
    const val SAMPLE_ERASED_TEXT = ""
    const val SAMPLE_TEXT_INPUT = "Text"
    const val SAMPLE_INVALID_DATE_INPUT = "AB"
    const val SAMPLE_VALID_DATE_INPUT = "12"
    const val SAMPLE_DATE_INPUT_STEP1 = "1"
    const val SAMPLE_DATE_INPUT_STEP2 = "2"
    const val SAMPLE_DATE_INPUT_STEP3 = "3"
    const val SAMPLE_DATE_INPUT_STEP4 = "4"
    const val SAMPLE_DATE_INPUT_STEP5 = "5"
    const val SAMPLE_INVALID_YEAR_INPUT = "1234"
    const val SAMPLE_VALID_YEAR_INPUT = "2025"
  }

  @Before
  fun setUp() {
    viewModel =
        EventCreationViewModel(
            eventRepository = FakeEventRepository(), userRepository = FakeUserRepository())
    composeTestRule.setContent {
      EventCreationScreen(
          eventCreationViewModel = viewModel,
          location = Location(0.0, 0.0),
          onSave = {},
          onAddTag = { tags ->
            viewModel.setEventTags(
                tags +
                    Tag.ROLE_PLAYING_GAMES +
                    Tag.TABLE_TENNIS +
                    Tag.ARTIFICIAL_INTELLIGENCE +
                    Tag.METAL +
                    Tag.COUNTRY +
                    Tag.PROGRAMMING +
                    Tag.HANDBALL +
                    Tag.RUNNING +
                    Tag.BICYCLE +
                    Tag.AARGAU +
                    Tag.GENEVA +
                    Tag.FITNESS +
                    Tag.YOGA +
                    Tag.MEDITATION +
                    Tag.RAP +
                    Tag.BOARD_GAMES +
                    Tag.BASKETBALL +
                    Tag.RNB +
                    Tag.BASEL_LANDSCHAFT +
                    Tag.APPENZELL_INNERRHODEN +
                    Tag.JURA +
                    Tag.HIKING +
                    Tag.REGGAE +
                    Tag.KARATE +
                    Tag.TRAIN)
          })
    }
  }

  @Test
  fun eventCreationScreen_displayedCorrectly() {
    composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_TITLE_TEXT_FIELD).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_DESCRIPTION_TEXT_FIELD)
        .assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_DAY_TEXT_FIELD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_MONTH_TEXT_FIELD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_YEAR_TEXT_FIELD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_HOUR_TEXT_FIELD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_MINUTE_TEXT_FIELD).assertIsDisplayed()

    composeTestRule.onNodeWithTag(EventCreationTestTags.ERROR_TITLE).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(EventCreationTestTags.ERROR_DESCRIPTION).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(EventCreationTestTags.ERROR_DAY).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(EventCreationTestTags.ERROR_MONTH).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(EventCreationTestTags.ERROR_YEAR).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(EventCreationTestTags.ERROR_HOUR).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(EventCreationTestTags.ERROR_MINUTE).assertIsNotDisplayed()

    composeTestRule.onNodeWithTag(EventCreationTestTags.ADD_TAG_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventCreationTestTags.TAG).assertIsNotDisplayed()

    composeTestRule.onNodeWithTag(EventCreationTestTags.SAVE_EVENT_BUTTON).assertIsDisplayed()
  }

  @Test
  fun eventCreationScreen_canEnterTitle() {
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_TITLE_TEXT_FIELD)
        .performTextInput(SAMPLE_TEXT_INPUT)
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_TITLE_TEXT_FIELD)
        .assertTextContains(SAMPLE_TEXT_INPUT)
  }

  @Test
  fun eventCreationScreen_canEnterDescription() {
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_DESCRIPTION_TEXT_FIELD)
        .performTextInput(SAMPLE_TEXT_INPUT)
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_DESCRIPTION_TEXT_FIELD)
        .assertTextContains(SAMPLE_TEXT_INPUT)
  }

  @Test
  fun eventCreationScreen_canEnterDay() {
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_DAY_TEXT_FIELD)
        .performTextInput(SAMPLE_VALID_DATE_INPUT)
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_DAY_TEXT_FIELD)
        .assertTextContains(SAMPLE_VALID_DATE_INPUT)
  }

  @Test
  fun eventCreationScreen_canEnterMonth() {
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_MONTH_TEXT_FIELD)
        .performTextInput(SAMPLE_VALID_DATE_INPUT)
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_MONTH_TEXT_FIELD)
        .assertTextContains(SAMPLE_VALID_DATE_INPUT)
  }

  @Test
  fun eventCreationScreen_canEnterYear() {
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_YEAR_TEXT_FIELD)
        .performTextInput(SAMPLE_VALID_YEAR_INPUT)
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_YEAR_TEXT_FIELD)
        .assertTextContains(SAMPLE_VALID_YEAR_INPUT)
  }

  @Test
  fun eventCreationScreen_canEnterHour() {
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_HOUR_TEXT_FIELD)
        .performTextInput(SAMPLE_VALID_DATE_INPUT)
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_HOUR_TEXT_FIELD)
        .assertTextContains(SAMPLE_VALID_DATE_INPUT)
  }

  @Test
  fun eventCreationScreen_canEnterMinute() {
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_MINUTE_TEXT_FIELD)
        .performTextInput(SAMPLE_VALID_DATE_INPUT)
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_MINUTE_TEXT_FIELD)
        .assertTextContains(SAMPLE_VALID_DATE_INPUT)
  }

  @Test
  fun eventCreationScreen_TitleEmpty_Display_Error() {
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_TITLE_TEXT_FIELD)
        .performTextInput(SAMPLE_TEXT_INPUT)
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_TITLE_TEXT_FIELD)
        .performTextReplacement("")
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.ERROR_TITLE, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun eventCreationScreen_DescriptionEmpty_NotDisplay_Error() {
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_DESCRIPTION_TEXT_FIELD)
        .performTextInput(SAMPLE_TEXT_INPUT)
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_DESCRIPTION_TEXT_FIELD)
        .performTextReplacement(SAMPLE_ERASED_TEXT)
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.ERROR_DESCRIPTION, useUnmergedTree = true)
        .assertIsNotDisplayed()
  }

  @Test
  fun eventCreationScreen_DayEmpty_Display_Error() {
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_DAY_TEXT_FIELD)
        .performTextInput(SAMPLE_INVALID_DATE_INPUT)
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_DAY_TEXT_FIELD)
        .performTextReplacement(SAMPLE_ERASED_TEXT)
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.ERROR_DAY, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun eventCreationScreen_MonthEmpty_Display_Error() {
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_MONTH_TEXT_FIELD)
        .performTextInput(SAMPLE_INVALID_DATE_INPUT)
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_MONTH_TEXT_FIELD)
        .performTextReplacement(SAMPLE_ERASED_TEXT)
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.ERROR_MONTH, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun eventCreationScreen_YearEmpty_Display_Error() {
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_YEAR_TEXT_FIELD)
        .performTextInput(SAMPLE_INVALID_DATE_INPUT)
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_YEAR_TEXT_FIELD)
        .performTextReplacement(SAMPLE_ERASED_TEXT)
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.ERROR_YEAR, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun eventCreationScreen_HourEmpty_Display_Error() {
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_HOUR_TEXT_FIELD)
        .performTextInput(SAMPLE_INVALID_DATE_INPUT)
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_HOUR_TEXT_FIELD)
        .performTextReplacement(SAMPLE_ERASED_TEXT)
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.ERROR_HOUR, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun eventCreationScreen_MinuteEmpty_Display_Error() {
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_MINUTE_TEXT_FIELD)
        .performTextInput(SAMPLE_INVALID_DATE_INPUT)
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_MINUTE_TEXT_FIELD)
        .performTextReplacement(SAMPLE_ERASED_TEXT)
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.ERROR_MINUTE, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun eventCreationScreen_DayNonInt_Display_Error() {
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_DAY_TEXT_FIELD)
        .performTextInput(SAMPLE_INVALID_DATE_INPUT)
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.ERROR_DAY, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun eventCreationScreen_MonthNonInt_Display_Error() {
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_MONTH_TEXT_FIELD)
        .performTextInput(SAMPLE_INVALID_DATE_INPUT)
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.ERROR_MONTH, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun eventCreationScreen_YearNonInt_Display_Error() {
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_YEAR_TEXT_FIELD)
        .performTextInput(SAMPLE_INVALID_DATE_INPUT)
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.ERROR_YEAR, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun eventCreationScreen_HourNonInt_Display_Error() {
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_HOUR_TEXT_FIELD)
        .performTextInput(SAMPLE_INVALID_DATE_INPUT)
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.ERROR_HOUR, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun eventCreationScreen_MinuteNonInt_Display_Error() {
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_MINUTE_TEXT_FIELD)
        .performTextInput(SAMPLE_INVALID_DATE_INPUT)
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.ERROR_MINUTE, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun eventCreationScreen_3LengthDay_Display_Length2() {
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_DAY_TEXT_FIELD)
        .performTextInput(SAMPLE_DATE_INPUT_STEP1)
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_DAY_TEXT_FIELD)
        .performTextInput(SAMPLE_DATE_INPUT_STEP2)
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_DAY_TEXT_FIELD)
        .performTextInput(SAMPLE_DATE_INPUT_STEP3)
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_DAY_TEXT_FIELD, useUnmergedTree = true)
        .assertTextEquals(SAMPLE_VALID_DATE_INPUT)
  }

  @Test
  fun eventCreationScreen_3LengthMonth_Display_Length2() {
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_MONTH_TEXT_FIELD)
        .performTextInput(SAMPLE_DATE_INPUT_STEP1)
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_MONTH_TEXT_FIELD)
        .performTextInput(SAMPLE_DATE_INPUT_STEP2)
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_MONTH_TEXT_FIELD)
        .performTextInput(SAMPLE_DATE_INPUT_STEP3)
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_MONTH_TEXT_FIELD, useUnmergedTree = true)
        .assertTextEquals(SAMPLE_VALID_DATE_INPUT)
  }

  @Test
  fun eventCreationScreen_5LengthYear_Display_Length4() {
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_YEAR_TEXT_FIELD)
        .performTextInput(SAMPLE_DATE_INPUT_STEP1)
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_YEAR_TEXT_FIELD)
        .performTextInput(SAMPLE_DATE_INPUT_STEP2)
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_YEAR_TEXT_FIELD)
        .performTextInput(SAMPLE_DATE_INPUT_STEP3)
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_YEAR_TEXT_FIELD)
        .performTextInput(SAMPLE_DATE_INPUT_STEP4)
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_YEAR_TEXT_FIELD)
        .performTextInput(SAMPLE_DATE_INPUT_STEP5)
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_YEAR_TEXT_FIELD, useUnmergedTree = true)
        .assertTextEquals(SAMPLE_INVALID_YEAR_INPUT)
  }

  @Test
  fun eventCreationScreen_3LengthHour_Display_Length2() {
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_HOUR_TEXT_FIELD)
        .performTextInput(SAMPLE_DATE_INPUT_STEP1)
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_HOUR_TEXT_FIELD)
        .performTextInput(SAMPLE_DATE_INPUT_STEP2)
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_HOUR_TEXT_FIELD)
        .performTextInput(SAMPLE_DATE_INPUT_STEP3)
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_HOUR_TEXT_FIELD, useUnmergedTree = true)
        .assertTextEquals(SAMPLE_VALID_DATE_INPUT)
  }

  @Test
  fun eventCreationScreen_3LengthMinute_Display_Length2() {
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_MINUTE_TEXT_FIELD)
        .performTextInput(SAMPLE_DATE_INPUT_STEP1)
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_MINUTE_TEXT_FIELD)
        .performTextInput(SAMPLE_DATE_INPUT_STEP2)
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_MINUTE_TEXT_FIELD)
        .performTextInput(SAMPLE_DATE_INPUT_STEP3)
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_MINUTE_TEXT_FIELD, useUnmergedTree = true)
        .assertTextEquals(SAMPLE_VALID_DATE_INPUT)
  }

  @Test
  fun eventCreationScreen_CanAddTags() {
    composeTestRule.onNodeWithTag(EventCreationTestTags.ADD_TAG_BUTTON).performClick()
    composeTestRule
        .onAllNodesWithTag(EventCreationTestTags.TAG)
        .assertAny(hasTestTag(EventCreationTestTags.TAG))
  }

  @Test
  fun eventCreationScreen_AddTags_SaveButtonDisplay() {
    composeTestRule.onNodeWithTag(EventCreationTestTags.ADD_TAG_BUTTON).performClick()
    composeTestRule.onNodeWithTag(EventCreationTestTags.SAVE_EVENT_BUTTON).assertIsDisplayed()
  }
}
