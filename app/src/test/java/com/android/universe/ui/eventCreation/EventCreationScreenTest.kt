package com.android.universe.ui.eventCreation

import androidx.compose.ui.test.assertAny
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import com.android.universe.model.Tag
import com.android.universe.model.event.FakeEventRepository
import com.android.universe.model.location.Location
import com.android.universe.model.user.FakeUserRepository
import org.junit.Rule

@RunWith(AndroidJUnit4::class)
class EventCreationScreenTest {
    private lateinit var viewModel: EventCreationViewModel
    @get:Rule val composeTestRule = createComposeRule()

    @Before
    fun setUp() {
        viewModel = EventCreationViewModel(eventRepository = FakeEventRepository(), userRepository = FakeUserRepository())
        composeTestRule.setContent {
            EventCreationScreen(eventCreationViewModel = viewModel, location = Location(0.0, 0.0), onSave = {},
                onAddTag = { tags -> viewModel.setEventTags(tags + Tag.ROLE_PLAYING_GAMES + Tag.TABLE_TENNIS + Tag.ARTIFICIAL_INTELLIGENCE + Tag.METAL
                        + Tag.COUNTRY + Tag.PROGRAMMING + Tag.HANDBALL + Tag.RUNNING + Tag.BICYCLE + Tag.AARGAU + Tag.GENEVA + Tag.FITNESS + Tag.YOGA
                        + Tag.MEDITATION + Tag.RAP + Tag.BOARD_GAMES + Tag.BASKETBALL + Tag.RNB + Tag.BASEL_LANDSCHAFT + Tag.APPENZELL_INNERRHODEN + Tag.JURA
                        + Tag.HIKING + Tag.REGGAE + Tag.KARATE + Tag.TRAIN) })
        }
    }

    @Test
    fun eventCreationScreen_displayedCorrectly() {
        composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_TITLE_TEXT_FIELD).assertIsDisplayed()
        composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_DESCRIPTION_TEXT_FIELD).assertIsDisplayed()
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
        val title = "Text"
        composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_TITLE_TEXT_FIELD).performTextInput(title)
        composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_TITLE_TEXT_FIELD,).assertTextContains(title)
    }

    @Test
    fun eventCreationScreen_canEnterDescription() {
        val text = "Text"
        composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_DESCRIPTION_TEXT_FIELD).performTextInput(text)
        composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_DESCRIPTION_TEXT_FIELD).assertTextContains(text)
    }

    @Test
    fun eventCreationScreen_canEnterDay() {
        val day = "12"
        composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_DAY_TEXT_FIELD).performTextInput(day)
        composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_DAY_TEXT_FIELD).assertTextContains(day)
    }

    @Test
    fun eventCreationScreen_canEnterMonth() {
        val month = "12"
        composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_MONTH_TEXT_FIELD).performTextInput(month)
        composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_MONTH_TEXT_FIELD).assertTextContains(month)
    }

    @Test
    fun eventCreationScreen_canEnterYear() {
        val year = "2025"
        composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_YEAR_TEXT_FIELD).performTextInput(year)
        composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_YEAR_TEXT_FIELD).assertTextContains(year)
    }

    @Test
    fun eventCreationScreen_canEnterHour() {
        val hour = "18"
        composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_HOUR_TEXT_FIELD).performTextInput(hour)
        composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_HOUR_TEXT_FIELD).assertTextContains(hour)
    }

    @Test
    fun eventCreationScreen_canEnterMinute() {
        val minute = "18"
        composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_MINUTE_TEXT_FIELD).performTextInput(minute)
        composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_MINUTE_TEXT_FIELD).assertTextContains(minute)
    }

    @Test
    fun eventCreationScreen_TitleEmpty_Display_Error() {
        composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_TITLE_TEXT_FIELD).performTextInput("Text")
        composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_TITLE_TEXT_FIELD).performTextReplacement("")
        composeTestRule.onNodeWithTag(EventCreationTestTags.ERROR_TITLE, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun eventCreationScreen_DescriptionEmpty_Display_Error() {
        composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_DESCRIPTION_TEXT_FIELD).performTextInput("Text")
        composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_DESCRIPTION_TEXT_FIELD).performTextReplacement("")
        composeTestRule.onNodeWithTag(EventCreationTestTags.ERROR_DESCRIPTION, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun eventCreationScreen_DayEmpty_Display_Error() {
        composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_DAY_TEXT_FIELD).performTextInput("AB")
        composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_DAY_TEXT_FIELD).performTextReplacement("")
        composeTestRule.onNodeWithTag(EventCreationTestTags.ERROR_DAY, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun eventCreationScreen_MonthEmpty_Display_Error() {
        composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_MONTH_TEXT_FIELD).performTextInput("AB")
        composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_MONTH_TEXT_FIELD).performTextReplacement("")
        composeTestRule.onNodeWithTag(EventCreationTestTags.ERROR_MONTH, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun eventCreationScreen_YearEmpty_Display_Error() {
        composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_YEAR_TEXT_FIELD).performTextInput("AB")
        composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_YEAR_TEXT_FIELD).performTextReplacement("")
        composeTestRule.onNodeWithTag(EventCreationTestTags.ERROR_YEAR, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun eventCreationScreen_HourEmpty_Display_Error() {
        composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_HOUR_TEXT_FIELD).performTextInput("AB")
        composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_HOUR_TEXT_FIELD).performTextReplacement("")
        composeTestRule.onNodeWithTag(EventCreationTestTags.ERROR_HOUR, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun eventCreationScreen_MinuteEmpty_Display_Error() {
        composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_MINUTE_TEXT_FIELD).performTextInput("AB")
        composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_MINUTE_TEXT_FIELD).performTextReplacement("")
        composeTestRule.onNodeWithTag(EventCreationTestTags.ERROR_MINUTE, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun eventCreationScreen_DayNonInt_Display_Error() {
        composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_DAY_TEXT_FIELD).performTextInput("AB")
        composeTestRule.onNodeWithTag(EventCreationTestTags.ERROR_DAY, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun eventCreationScreen_MonthNonInt_Display_Error() {
        composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_MONTH_TEXT_FIELD).performTextInput("AB")
        composeTestRule.onNodeWithTag(EventCreationTestTags.ERROR_MONTH, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun eventCreationScreen_YearNonInt_Display_Error() {
        composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_YEAR_TEXT_FIELD).performTextInput("AB")
        composeTestRule.onNodeWithTag(EventCreationTestTags.ERROR_YEAR, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun eventCreationScreen_HourNonInt_Display_Error() {
        composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_HOUR_TEXT_FIELD).performTextInput("AB")
        composeTestRule.onNodeWithTag(EventCreationTestTags.ERROR_HOUR, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun eventCreationScreen_MinuteNonInt_Display_Error() {
        composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_MINUTE_TEXT_FIELD).performTextInput("AB")
        composeTestRule.onNodeWithTag(EventCreationTestTags.ERROR_MINUTE, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun eventCreationScreen_3LengthDay_Display_Length2() {
        composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_DAY_TEXT_FIELD).performTextInput("1")
        composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_DAY_TEXT_FIELD).performTextInput("2")
        composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_DAY_TEXT_FIELD).performTextInput("3")
        composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_DAY_TEXT_FIELD, useUnmergedTree = true).assertTextEquals("12")
    }

    @Test
    fun eventCreationScreen_3LengthMonth_Display_Length2() {
        composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_MONTH_TEXT_FIELD).performTextInput("1")
        composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_MONTH_TEXT_FIELD).performTextInput("2")
        composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_MONTH_TEXT_FIELD).performTextInput("3")
        composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_MONTH_TEXT_FIELD,useUnmergedTree = true).assertTextEquals("12")
    }

    @Test
    fun eventCreationScreen_5LengthYear_Display_Length4() {
        composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_YEAR_TEXT_FIELD).performTextInput("1")
        composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_YEAR_TEXT_FIELD).performTextInput("2")
        composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_YEAR_TEXT_FIELD).performTextInput("3")
        composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_YEAR_TEXT_FIELD).performTextInput("4")
        composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_YEAR_TEXT_FIELD).performTextInput("5")
        composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_YEAR_TEXT_FIELD).performTextInput("12345")
        composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_YEAR_TEXT_FIELD, useUnmergedTree = true).assertTextEquals("1234")
    }

    @Test
    fun eventCreationScreen_3LengthHour_Display_Length2() {
        composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_HOUR_TEXT_FIELD).performTextInput("1")
        composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_HOUR_TEXT_FIELD).performTextInput("2")
        composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_HOUR_TEXT_FIELD).performTextInput("3")
        composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_HOUR_TEXT_FIELD, useUnmergedTree = true).assertTextEquals("12")
    }

    @Test
    fun eventCreationScreen_3LengthMinute_Display_Length2() {
        composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_MINUTE_TEXT_FIELD).performTextInput("1")
        composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_MINUTE_TEXT_FIELD).performTextInput("2")
        composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_MINUTE_TEXT_FIELD).performTextInput("3")
        composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_MINUTE_TEXT_FIELD, useUnmergedTree = true).assertTextEquals("12")
    }

    @Test
    fun eventCreationScreen_CanAddTags() {
        composeTestRule.onNodeWithTag(EventCreationTestTags.ADD_TAG_BUTTON).performClick()
        composeTestRule.onAllNodesWithTag(EventCreationTestTags.TAG).assertAny(hasTestTag(
            EventCreationTestTags.TAG))
    }

    @Test
    fun eventCreationScreen_AddTags_SaveButtonDisplay() {
        composeTestRule.onNodeWithTag(EventCreationTestTags.ADD_TAG_BUTTON).performClick()
        composeTestRule.onNodeWithTag(EventCreationTestTags.SAVE_EVENT_BUTTON).assertIsDisplayed()
    }
}