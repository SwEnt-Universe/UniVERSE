package com.android.universe.ui.eventCreation

import androidx.compose.ui.test.assertAny
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextContains
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
import com.android.universe.utils.nextMonth
import com.android.universe.utils.pressOKDate
import com.android.universe.utils.pressOKTime
import com.android.universe.utils.previousMonth
import com.android.universe.utils.selectDay
import com.android.universe.utils.selectHour
import com.android.universe.utils.selectMinute
import com.android.universe.utils.selectYear
import java.time.LocalDate
import java.time.format.DateTimeFormatter
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
    const val SAMPLE_DAY = 17
    val SAMPLE_DATE = LocalDate.of(SAMPLE_YEAR, LocalDate.now().month, SAMPLE_DAY)
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
                    Tag.DND,
                    Tag.TABLE_TENNIS,
                    Tag.AI,
                    Tag.METAL,
                    Tag.COUNTRY,
                    Tag.PROGRAMMING,
                    Tag.HANDBALL,
                    Tag.RUNNING,
                    Tag.CYCLING,
                    Tag.CHESS,
                    Tag.VIDEO_GAMES,
                    Tag.FITNESS,
                    Tag.YOGA,
                    Tag.MEDITATION,
                    Tag.RAP,
                    Tag.BOARD_GAMES,
                    Tag.BASKETBALL,
                    Tag.RNB,
                    Tag.BEACH,
                    Tag.GRAFFITI,
                    Tag.GROUP_TRAVEL,
                    Tag.HIKING,
                    Tag.REGGAE,
                    Tag.KARATE,
                    Tag.MACHINE_LEARNING))
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

    selectHour(composeTestRule, SAMPLE_HOUR)

    selectMinute(composeTestRule, SAMPLE_MINUTE)
    pressOKTime(composeTestRule)
    composeTestRule.onNodeWithText(FORMATTED_TIME).assertIsDisplayed()
  }

  @Test
  fun eventCreationScreen_canEnterDate() {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    composeTestRule.onNodeWithTag(EventCreationTestTags.DATE_BUTTON).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(EventCreationTestTags.DATE_DIALOG).assertIsDisplayed()
    selectYear(composeTestRule, SAMPLE_YEAR)
    nextMonth(composeTestRule)
    composeTestRule.waitForIdle()
    selectDay(composeTestRule, SAMPLE_DATE)
    pressOKDate(composeTestRule)
    composeTestRule.onNodeWithText(formatter.format(SAMPLE_DATE)).assertIsDisplayed()
  }

  @Test
  fun pastDatePutsError() {
    composeTestRule.onNodeWithTag(EventCreationTestTags.DATE_BUTTON).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(EventCreationTestTags.DATE_DIALOG).assertIsDisplayed()
    previousMonth(composeTestRule)
    composeTestRule.waitForIdle()
    selectDay(composeTestRule, SAMPLE_DATE.minusMonths(1))

    pressOKDate(composeTestRule)
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
