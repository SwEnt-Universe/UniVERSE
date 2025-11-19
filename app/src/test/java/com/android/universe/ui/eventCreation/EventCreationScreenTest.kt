package com.android.universe.ui.eventCreation

import androidx.compose.ui.test.assertAny
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.model.event.FakeEventRepository
import com.android.universe.model.location.Location
import com.android.universe.model.tag.Tag
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
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
    const val SAMPLE_MINUTE = 25
    const val SAMPLE_HOUR = 17
    const val FORMATTED_TIME = "$SAMPLE_HOUR:$SAMPLE_MINUTE"
    const val SAMPLE_YEAR = 2027

    val diffYear = SAMPLE_YEAR - LocalDate.now().plusMonths(1).year
    const val SAMPLE_DAY = 17
    val plusMonthDate = LocalDate.now().plusMonths(1).plusYears(diffYear.toLong())

    val formatMonthPlus = plusMonthDate.format(DateTimeFormatter.ofPattern("MMMM", Locale.ENGLISH))
  }

  /**
   * Selecting an already selected hour will give an error as 2 nodes in the node tree will have the
   * same content description.
   *
   * @param hour the hour to select.
   */
  fun selectHour(hour: Int) {
    require(hour in 0..23)
    composeTestRule
        .onNode(hasContentDescription("Select hour", ignoreCase = true))
        .assertIsDisplayed()
        .performClick()
    composeTestRule.waitForIdle()
    composeTestRule
        .onNode(hasContentDescription("$hour hours"), useUnmergedTree = true)
        .assertIsDisplayed()
        .performClick()
    composeTestRule.waitForIdle()
  }

  /**
   * Selecting an already selected minute will give an error as 2 nodes in the node tree will have
   * the same content description.
   *
   * @param minute5 the minute to select, a multiple of 5.
   */
  fun selectMinute(minute5: Int) {
    require(minute5 in 0..59)
    require(minute5 % 5 == 0)
    composeTestRule
        .onNode(hasContentDescription("Select minutes", ignoreCase = true))
        .assertIsDisplayed()
        .performClick()
    composeTestRule.waitForIdle()

    composeTestRule
        .onNode(hasContentDescription("$minute5 minutes"), useUnmergedTree = true)
        .assertIsDisplayed()
        .performClick()
    composeTestRule.waitForIdle()
  }

  fun nextMonth() {
    composeTestRule
        .onNode(hasContentDescription("Change to next month"), useUnmergedTree = true)
        .assertIsDisplayed()
        .performClick()
    composeTestRule.waitForIdle()
  }

  fun previousMonth() {
    composeTestRule
        .onNode(hasContentDescription("Change to previous month"), useUnmergedTree = true)
        .assertIsDisplayed()
        .performClick()
    composeTestRule.waitForIdle()
  }

  fun selectYear(year: Int) {
    composeTestRule
        .onNode(hasContentDescription("Switch to selecting a year"), useUnmergedTree = true)
        .assertIsDisplayed()
        .performClick()

    composeTestRule
        .onNodeWithText("Navigate to year $year", useUnmergedTree = true)
        .assertIsDisplayed()
        .performClick()
    composeTestRule.waitForIdle()
  }

  fun selectSampleDay() {
    composeTestRule
        .onNodeWithText(SAMPLE_DAY.toString(), substring = true, useUnmergedTree = true)
        .assertIsDisplayed()
        .performClick()
    composeTestRule.waitForIdle()
  }

  @Before
  fun setUp() {
    viewModel = EventCreationViewModel(eventRepository = FakeEventRepository())
    composeTestRule.setContent {
      EventCreationScreen(
          eventCreationViewModel = viewModel,
          location = Location(0.0, 0.0),
          onSave = {},
          onAddTag = {
            viewModel.setEventTags(
                setOf(
                    Tag.ROLE_PLAYING_GAMES,
                    Tag.TABLE_TENNIS,
                    Tag.ARTIFICIAL_INTELLIGENCE,
                    Tag.METAL,
                    Tag.COUNTRY,
                    Tag.PROGRAMMING,
                    Tag.HANDBALL,
                    Tag.RUNNING,
                    Tag.BICYCLE,
                    Tag.AARGAU,
                    Tag.GENEVA,
                    Tag.FITNESS,
                    Tag.YOGA,
                    Tag.MEDITATION,
                    Tag.RAP,
                    Tag.BOARD_GAMES,
                    Tag.BASKETBALL,
                    Tag.RNB,
                    Tag.BASEL_LANDSCHAFT,
                    Tag.APPENZELL_INNERRHODEN,
                    Tag.JURA,
                    Tag.HIKING,
                    Tag.REGGAE,
                    Tag.KARATE,
                    Tag.TRAIN))
          })
    }
  }

  @Test
  fun eventCreationScreen_displayedCorrectly() {
    composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_TITLE_TEXT_FIELD).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_DESCRIPTION_TEXT_FIELD)
        .assertIsDisplayed()

    composeTestRule.onNodeWithTag(EventCreationTestTags.ERROR_TITLE).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(EventCreationTestTags.ERROR_DESCRIPTION).assertIsNotDisplayed()

    composeTestRule.onNodeWithTag(EventCreationTestTags.ADD_TAG_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventCreationTestTags.TAG).assertIsNotDisplayed()

    composeTestRule.onNodeWithTag(EventCreationTestTags.SAVE_EVENT_BUTTON).assertExists()

    composeTestRule.onNodeWithTag(EventCreationTestTags.IMAGE_EVENT).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(EventCreationTestTags.IMAGE_ICON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventCreationTestTags.EDIT_IMAGE_ICON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventCreationTestTags.DELETE_IMAGE_BUTTON).assertIsDisplayed()
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
  fun eventCreationScreen_canEnterTime() {

    composeTestRule.onNodeWithTag(EventCreationTestTags.TIME_BUTTON).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(EventCreationTestTags.TIME_DIALOG).assertIsDisplayed()
    composeTestRule.waitForIdle()

    selectHour(SAMPLE_HOUR)

    selectMinute(SAMPLE_MINUTE)
    composeTestRule.onNodeWithText("OK").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText(FORMATTED_TIME).assertIsDisplayed()
  }

  @Test
  fun eventCreationScreen_canEnterDate() {
    composeTestRule.onNodeWithTag(EventCreationTestTags.DATE_BUTTON).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(EventCreationTestTags.DATE_DIALOG).assertIsDisplayed()
    selectYear(SAMPLE_YEAR)
    nextMonth()
    composeTestRule.waitForIdle()
    composeTestRule
        .onNodeWithText("$formatMonthPlus $SAMPLE_DAY", substring = true, useUnmergedTree = true)
        .assertIsDisplayed()
        .performClick()
    composeTestRule.onNodeWithText("OK").performClick()
    composeTestRule.waitForIdle()
    composeTestRule
        .onNodeWithText("$SAMPLE_DAY/${plusMonthDate.month.value}/${plusMonthDate.year}")
        .assertIsDisplayed()
  }

  @Test
  fun pastDatePutsError() {
    composeTestRule.onNodeWithTag(EventCreationTestTags.DATE_BUTTON).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(EventCreationTestTags.DATE_DIALOG).assertIsDisplayed()
    previousMonth()
    composeTestRule.waitForIdle()
    selectSampleDay()

    composeTestRule.onNodeWithText("OK").performClick()
    composeTestRule.waitForIdle()
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.ERROR_DATE, useUnmergedTree = true)
        .assertIsDisplayed()
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
  fun eventCreationScreen_CanAddTags() {
    composeTestRule.onNodeWithTag(EventCreationTestTags.ADD_TAG_BUTTON).performClick()
    composeTestRule
        .onAllNodesWithTag(EventCreationTestTags.TAG)
        .assertAny(hasTestTag(EventCreationTestTags.TAG))
  }

  @Test
  fun eventCreationScreen_AddTags_SaveButtonDisplay() {
    composeTestRule.onNodeWithTag(EventCreationTestTags.ADD_TAG_BUTTON).performClick()
    composeTestRule.onNodeWithTag(EventCreationTestTags.SAVE_EVENT_BUTTON).assertExists()
  }
}
