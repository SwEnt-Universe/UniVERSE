package com.android.universe.ui.eventCreation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.di.DefaultDP
import com.android.universe.model.ai.gemini.FakeGeminiEventAssistant
import com.android.universe.model.event.EventRepository
import com.android.universe.model.event.FakeEventRepository
import com.android.universe.model.image.ImageBitmapManager
import com.android.universe.model.location.Location
import com.android.universe.ui.navigation.FlowBottomMenuTestTags
import com.android.universe.utils.EventTestData
import com.android.universe.utils.setContentWithStubBackdrop
import io.mockk.every
import io.mockk.mockkObject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class EventEditionScreenTest {
  private lateinit var viewModel: EventCreationViewModel
  private lateinit var fakeGemini: FakeGeminiEventAssistant
  private lateinit var eventRepository: EventRepository
  @get:Rule val composeTestRule = createComposeRule()

  companion object {
    val SAMPLE_EVENT = EventTestData.dummyEvent1
  }

  @Before
  fun setUp() {
    runBlocking {
      val context = ApplicationProvider.getApplicationContext<android.content.Context>()

      mockkObject(DefaultDP)
      val testDispatcher = UnconfinedTestDispatcher()
      every { DefaultDP.default } returns testDispatcher
      every { DefaultDP.io } returns testDispatcher
      every { DefaultDP.main } returns testDispatcher

      val imageManager = ImageBitmapManager(context)
      fakeGemini = FakeGeminiEventAssistant()

      eventRepository = FakeEventRepository()
      eventRepository.addEvent(SAMPLE_EVENT)
      viewModel =
          EventCreationViewModel(
              imageManager = imageManager, eventRepository = eventRepository, gemini = fakeGemini)

      composeTestRule.setContentWithStubBackdrop {
        EventCreationScreen(
            eventCreationViewModel = viewModel,
            location = Location(0.0, 0.0),
            onSave = {},
            uidEvent = SAMPLE_EVENT.id)
      }
    }
  }

  @Test
  fun eventEditionScreen_displayedCorrectly() = runTest {
    composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_PICTURE_PICKER).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventCreationTestTags.CREATION_EVENT_TITLE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_TITLE_TEXT_FIELD).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_DESCRIPTION_TEXT_FIELD)
        .assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_DATE_TEXT_FIELD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_DATE_PICKER).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_TIME_TEXT_FIELD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventCreationTestTags.PRIVACY_TOGGLE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventCreationTestTags.SET_LOCATION_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(FlowBottomMenuTestTags.DELETE_BUTTON).assertIsDisplayed()
  }
}
