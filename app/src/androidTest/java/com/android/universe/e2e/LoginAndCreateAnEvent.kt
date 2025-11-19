package com.android.universe.e2e

import android.Manifest.permission.ACCESS_FINE_LOCATION
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.printToLog
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.android.universe.UniverseApp
import com.android.universe.di.DefaultDP
import com.android.universe.ui.common.FormTestTags
import com.android.universe.ui.common.GeneralDatePopUpTestTags
import com.android.universe.ui.event.EventScreenTestTags
import com.android.universe.ui.eventCreation.EventCreationTestTags
import com.android.universe.ui.map.MapScreenTestTags
import com.android.universe.ui.navigation.NavigationTestTags
import com.android.universe.ui.signIn.SignInScreenTestTags
import com.android.universe.ui.theme.UniverseTheme
import com.android.universe.utils.EventTestData
import com.android.universe.utils.FirebaseAuthUserTest
import com.android.universe.utils.UserTestData
import com.android.universe.utils.setContentWithStubBackdrop
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import io.mockk.every
import io.mockk.mockkObject
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class LoginAndCreateAnEvent : FirebaseAuthUserTest(isRobolectric = false) {

  companion object {
    var fakeUser = UserTestData.Bob
    const val FAKE_EMAIL = UserTestData.bobEmail
    const val FAKE_PASS = UserTestData.bobPassword
    val FAKE_EVENT = EventTestData.futureEventNoTags
  }

  @get:Rule val composeTestRule = createComposeRule()

  @get:Rule
  val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(ACCESS_FINE_LOCATION)

  @Before
  override fun setUp() {
    super.setUp()
    mockkObject(DefaultDP)
    every { DefaultDP.io } returns UnconfinedTestDispatcher()
    every { DefaultDP.default } returns UnconfinedTestDispatcher()
    every { DefaultDP.main } returns Dispatchers.Main
    runTest {
      val uid = createTestUser(fakeUser, FAKE_EMAIL, FAKE_PASS)
      fakeUser = fakeUser.copy(uid = uid)
      Firebase.auth.signOut()
    }
    composeTestRule.setContentWithStubBackdrop { UniverseTheme { UniverseApp() } }
    composeTestRule.waitForIdle()
  }

  @Test
  fun `Login and Create an Event`() {
    composeTestRule.waitUntil(5_000L) {
      composeTestRule.onNodeWithTag(SignInScreenTestTags.EMAIL_SIGN_IN_BUTTON).isDisplayed()
    }
    loginAndWait()
    zoomOnMapAndWait()
    clickOnMapAndCreateEvent()
    seeAddedEventInEventList()
  }

  private fun loginAndWait() {
    composeTestRule
        .onNodeWithTag(FormTestTags.EMAIL_FIELD)
        .performClick()
        .performTextInput(FAKE_EMAIL)

    composeTestRule
        .onNodeWithTag(FormTestTags.PASSWORD_FIELD)
        .performClick()
        .performTextInput(FAKE_PASS)

    composeTestRule.onNodeWithTag(SignInScreenTestTags.EMAIL_SIGN_IN_BUTTON).performClick()

    // Wait max 30 seconds, we should arrive on the MapScreen
    composeTestRule.waitUntil(30_000L) {
      composeTestRule.onNodeWithTag(NavigationTestTags.MAP_SCREEN).isDisplayed()
    }

    composeTestRule.waitUntil(15_000L) {
      composeTestRule
          .onAllNodesWithTag(MapScreenTestTags.INTERACTABLE)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
  }

  private fun zoomOnMapAndWait() {
    val startOffset1 = Offset(x = 400f, y = 400f) // Finger 1 start (e.g., bottom-left of center)
    val endOffset1 = Offset(x = 200f, y = 200f) // Finger 1 end (moves toward top-left)

    val startOffset2 = Offset(x = 600f, y = 600f) // Finger 2 start (e.g., top-right of center)
    val endOffset2 = Offset(x = 800f, y = 800f) // Finger 2 end (moves toward bottom-right)

    composeTestRule.onNodeWithTag(MapScreenTestTags.MAP_VIEW).performTouchInput {
      down(pointerId = 0, startOffset1)
      down(pointerId = 1, startOffset2)

      moveTo(pointerId = 0, position = endOffset1, 1000)
      moveTo(pointerId = 1, position = endOffset2, 1000)

      up(pointerId = 0)
      up(pointerId = 1)
    }
    composeTestRule.waitForIdle()
  }

  private fun clickOnMapAndCreateEvent() {
    composeTestRule.onNodeWithTag(MapScreenTestTags.MAP_VIEW).performTouchInput { click(center) }
    composeTestRule.waitUntil(5_000L) {
      composeTestRule.onNodeWithTag(MapScreenTestTags.CREATE_EVENT_BUTTON).isDisplayed()
    }
    composeTestRule.onNodeWithTag(MapScreenTestTags.CREATE_EVENT_BUTTON).performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(EventCreationTestTags.SAVE_EVENT_BUTTON).assertIsDisplayed()

    composeTestRule
        .onNodeWithTag(EventCreationTestTags.DATE_BUTTON)
        .assertIsDisplayed()
        .performClick()
    nextMonth()
    composeTestRule
        .onNodeWithTag(GeneralDatePopUpTestTags.DATE_PICKER, useUnmergedTree = true)
        .printToLog("DATE_PICKER_TREE")
    composeTestRule.waitForIdle()
    selectSampleDay()
    composeTestRule.waitForIdle()
    pressOK()
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.TIME_BUTTON)
        .assertIsDisplayed()
        .performClick()
    selectHour(FAKE_EVENT.date.hour)
    selectMinute(FAKE_EVENT.date.minute)
    pressOK()
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_TITLE_TEXT_FIELD)
        .performTextInput(FAKE_EVENT.title)
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_DESCRIPTION_TEXT_FIELD)
        .performTextInput(FAKE_EVENT.description!!)

    composeTestRule.onNodeWithTag(EventCreationTestTags.SAVE_EVENT_BUTTON).performClick()
    composeTestRule.waitForIdle()
  }

  private fun seeAddedEventInEventList() {
    composeTestRule.waitUntil(5_000L) {
      composeTestRule.onNodeWithTag(NavigationTestTags.EVENT_TAB).isDisplayed()
    }
    composeTestRule.onNodeWithTag(NavigationTestTags.EVENT_TAB).performClick()
    composeTestRule.waitUntil(5_000L) {
      composeTestRule
          .onAllNodesWithTag(EventScreenTestTags.EVENT_CARD)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    // There should be a single event therefore we can check using the onFirst() function

    composeTestRule
        .onAllNodesWithTag(EventScreenTestTags.EVENT_TITLE)
        .onFirst()
        .assertTextEquals(FAKE_EVENT.title)
    composeTestRule
        .onAllNodesWithTag(EventScreenTestTags.EVENT_DESCRIPTION)
        .onFirst()
        .assertTextEquals(FAKE_EVENT.description!!)
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
        .onNodeWithText(
            FAKE_EVENT.date.format(DateTimeFormatter.ofPattern("MMMM dd", Locale.ENGLISH)),
            substring = true,
            useUnmergedTree = true)
        .assertIsDisplayed()
        .performClick()
    composeTestRule.waitForIdle()
  }

  fun pressOK() {
    composeTestRule.onNodeWithText("OK").performClick()
    composeTestRule.waitForIdle()
  }
}
