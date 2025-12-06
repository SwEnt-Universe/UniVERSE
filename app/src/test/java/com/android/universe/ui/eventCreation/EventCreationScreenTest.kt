package com.android.universe.ui.eventCreation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.di.DispatcherProvider
import com.android.universe.model.ai.gemini.EventProposal
import com.android.universe.model.ai.gemini.FakeGeminiEventAssistant
import com.android.universe.model.event.FakeEventRepository
import com.android.universe.model.image.ImageBitmapManager
import com.android.universe.utils.nextMonth
import com.android.universe.utils.pressOKDate
import com.android.universe.utils.selectDay
import com.android.universe.utils.selectYear
import com.android.universe.utils.setContentWithStubBackdrop
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class EventCreationScreenTest {
  private lateinit var viewModel: EventCreationViewModel
  private lateinit var fakeGemini: FakeGeminiEventAssistant
  @get:Rule val composeTestRule = createComposeRule()

  companion object {
    const val SAMPLE_TEXT_INPUT = "Text"
    const val SAMPLE_MINUTE = 25
    const val SAMPLE_HOUR = 17
    const val FORMATTED_TIME = "$SAMPLE_HOUR:$SAMPLE_MINUTE"
    const val SAMPLE_YEAR = 2027
    const val SAMPLE_DAY = 17
    val SAMPLE_DATE: LocalDate = LocalDate.of(SAMPLE_YEAR, LocalDate.now().month, SAMPLE_DAY)
  }

  @Before
  fun setUp() {
    val context = ApplicationProvider.getApplicationContext<android.content.Context>()

    val testDispatcher = UnconfinedTestDispatcher()
    val testDispatcherProvider =
        object : DispatcherProvider {
          override val main: CoroutineDispatcher = testDispatcher
          override val default: CoroutineDispatcher = testDispatcher
          override val io: CoroutineDispatcher = testDispatcher
          override val unconfined: CoroutineDispatcher = testDispatcher
        }

    val imageManager = ImageBitmapManager(context)
    fakeGemini = FakeGeminiEventAssistant()

    viewModel =
        EventCreationViewModel(
            imageManager = imageManager,
            eventRepository = FakeEventRepository(),
            dispatcherProvider = testDispatcherProvider,
            gemini = fakeGemini)

    composeTestRule.setContentWithStubBackdrop {
      EventCreationScreen(eventCreationViewModel = viewModel, onSelectLocation = {}, onSave = {})
    }
  }

  @Test
  fun eventCreationScreen_displayedCorrectly() {
    composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_PICTURE_PICKER).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventCreationTestTags.CREATION_EVENT_TITLE).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.SET_LOCATION_BUTTON, useUnmergedTree = true)
        .assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_TITLE_TEXT_FIELD).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_DESCRIPTION_TEXT_FIELD)
        .assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_DATE_TEXT_FIELD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_DATE_PICKER).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_TIME_TEXT_FIELD).assertIsDisplayed()
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

    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_TIME_TEXT_FIELD)
        .performTextInput(FORMATTED_TIME)
    composeTestRule.waitForIdle()
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_TIME_TEXT_FIELD)
        .assertTextContains(FORMATTED_TIME)
  }

  @Test
  fun eventCreationScreen_canEnterDate() {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_DATE_TEXT_FIELD).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_DATE_PICKER).assertIsDisplayed()
    selectYear(composeTestRule, SAMPLE_YEAR)
    nextMonth(composeTestRule)
    composeTestRule.waitForIdle()
    selectDay(composeTestRule, SAMPLE_DATE)
    pressOKDate(composeTestRule)
    // composeTestRule.onNodeWithText(formatter.format(SAMPLE_DATE)).assertIsDisplayed()
  }

  @Test
  fun eventCreationViewModel_deleteImage_clearsState() {
    viewModel.deleteImage()

    assert(viewModel.uiStateEventCreation.value.eventPicture == null)
  }

  @Test
  fun eventCreationScreen_aiAssistFlow() {
    // 1. Setup Fake AI response
    val aiTitle = "AI Generated Event"
    val aiDesc = "AI Generated Description"
    fakeGemini.predefinedProposal = EventProposal(aiTitle, aiDesc)

    // 2. Click AI Button
    composeTestRule.onNodeWithTag(EventCreationTestTags.AI_ASSIST_BUTTON).performClick()
    composeTestRule.waitForIdle()

    // 3. Verify AI Prompt Box Displayed
    composeTestRule.onNodeWithTag(EventCreationTestTags.AI_PROMPT_TEXT_FIELD).assertIsDisplayed()

    // 4. Enter Prompt
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.AI_PROMPT_TEXT_FIELD)
        .performTextInput("Fun party")

    // 5. Click Generate (Assuming button text is "Generate")
    composeTestRule.onNodeWithText("Generate").performClick()
    composeTestRule.waitForIdle()

    // 6. Verify Review Box Fields matches fake data
    composeTestRule.onNodeWithTag(EventCreationTestTags.AI_REVIEW_TITLE_FIELD).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.AI_REVIEW_TITLE_FIELD)
        .assertTextContains(aiTitle)
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.AI_REVIEW_DESCRIPTION_FIELD)
        .assertTextContains(aiDesc)

    // 7. Confirm Proposal
    composeTestRule.onNodeWithText("Confirm").performClick()
    composeTestRule.waitForIdle()

    // 8. Verify Standard Form is back and populated
    composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_TITLE_TEXT_FIELD).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_TITLE_TEXT_FIELD)
        .assertTextContains(aiTitle)
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_DESCRIPTION_TEXT_FIELD)
        .assertTextContains(aiDesc)
  }
}
