package com.android.universe.e2e

import android.Manifest.permission.ACCESS_FINE_LOCATION
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.android.universe.UniverseApp
import com.android.universe.di.DefaultDP
import com.android.universe.e2e.LoginAndCreateAnEvent.Companion.FAKE_EVENT
import com.android.universe.ui.common.EventContentTestTags
import com.android.universe.ui.common.FormTestTags
import com.android.universe.ui.event.EventCardTestTags
import com.android.universe.ui.eventCreation.EventCreationTestTags
import com.android.universe.ui.map.MapCreateEventModalTestTags
import com.android.universe.ui.map.MapScreenTestTags
import com.android.universe.ui.navigation.FlowBottomMenuTestTags
import com.android.universe.ui.navigation.NavigationTestTags
import com.android.universe.ui.signIn.SignInScreenTestTags
import com.android.universe.ui.theme.UniverseTheme
import com.android.universe.utils.EventTestData
import com.android.universe.utils.FirebaseAuthUserTest
import com.android.universe.utils.UserTestData
import com.android.universe.utils.nextMonth
import com.android.universe.utils.pressOKDate
import com.android.universe.utils.pressOKTime
import com.android.universe.utils.selectDayWithMonth
import com.android.universe.utils.selectHour
import com.android.universe.utils.selectMinute
import com.android.universe.utils.setContentWithStubBackdrop
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import io.mockk.every
import io.mockk.mockkObject
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
  fun `Login and Create an Event`() = runTest {
    composeTestRule.waitUntil(5_000L) {
      composeTestRule.onNodeWithTag(SignInScreenTestTags.WELCOME_BOX).isDisplayed()
    }
    loginAndWait()
    createEvent()
    seeAddedEventInEventList()
    clickOnEventInList()
  }

  private fun loginAndWait() = runTest {
    composeTestRule
        .onNodeWithTag(SignInScreenTestTags.JOIN_BUTTON)
        .assertIsDisplayed()
        .performClick()

    composeTestRule.waitForIdle()
    composeTestRule
        .onNodeWithTag(FormTestTags.EMAIL_FIELD)
        .performClick()
        .performTextInput(FAKE_EMAIL)

    composeTestRule.waitForIdle()
    composeTestRule
        .onNodeWithTag(FlowBottomMenuTestTags.CONFIRM_BUTTON)
        .assertIsDisplayed()
        .performClick()

    composeTestRule.waitUntil(60_000L) {
      composeTestRule.onNodeWithTag(SignInScreenTestTags.PASSWORD_BOX).isDisplayed()
    }

    composeTestRule
        .onNodeWithTag(FormTestTags.PASSWORD_FIELD)
        .performClick()
        .performTextInput(FAKE_PASS)

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(FlowBottomMenuTestTags.CONFIRM_BUTTON).performClick()

    // Wait max 30 seconds, we should arrive on the MapScreen
    composeTestRule.waitUntil(50_000L) {
      composeTestRule.onNodeWithTag(NavigationTestTags.MAP_SCREEN).isDisplayed()
    }
  }

  private fun createEvent() = runTest {
    // —————————————————————————————
    // 1. CLICK CREATE EVENT BUTTON -> CLICK MANUAL CREATE BUTTON -> CLICK SET LOCATION
    // —————————————————————————————
    // Wait for + button on map
    composeTestRule.waitUntil(10_000L) {
      runCatching {
            composeTestRule.onNodeWithTag(MapScreenTestTags.CREATE_EVENT_BUTTON).assertExists()
          }
          .isSuccess
    }
    composeTestRule.onNodeWithTag(MapScreenTestTags.CREATE_EVENT_BUTTON).performClick()

    // Wait for Manual Create button inside the popup modal
    composeTestRule.waitUntil(10_000L) {
      runCatching {
            composeTestRule
                .onAllNodesWithTag(
                    MapCreateEventModalTestTags.MANUAL_CREATE_EVENT_BUTTON, useUnmergedTree = true)
                .onFirst()
                .assertExists()
          }
          .isSuccess
    }
    composeTestRule
        .onAllNodesWithTag(
            MapCreateEventModalTestTags.MANUAL_CREATE_EVENT_BUTTON, useUnmergedTree = true)
        .onFirst()
        .performClick()

    // Click “Set location” button in the creation screen
    composeTestRule.waitUntil(10_000L) {
      runCatching {
            composeTestRule.onNodeWithTag(EventCreationTestTags.SET_LOCATION_BUTTON).assertExists()
          }
          .isSuccess
    }
    composeTestRule.onNodeWithTag(EventCreationTestTags.SET_LOCATION_BUTTON).performClick()

    // —————————————————————————————————————
    // 2. SET LOCATION BY CLICKING ON MAP
    // —————————————————————————————————————








    /*
    composeTestRule
        .onNodeWithTag("test_select_location_backdoor", useUnmergedTree = true)
        .performClick()

     */

    // —————————————————————————————
    // 3. OTHER PARAMETERS
    // —————————————————————————————
    composeTestRule.waitUntil(64_000L) {
      composeTestRule.onNodeWithTag(EventCreationTestTags.DATE_BUTTON).isDisplayed()
    }

    composeTestRule
        .onNodeWithTag(EventCreationTestTags.DATE_BUTTON)
        .assertIsDisplayed()
        .performClick()
    nextMonth(composeTestRule)
    composeTestRule.waitForIdle()
    selectDayWithMonth(composeTestRule, FAKE_EVENT.date.toLocalDate())
    composeTestRule.waitForIdle()
    pressOKDate(composeTestRule)
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.TIME_BUTTON)
        .assertIsDisplayed()
        .performClick()
    selectHour(composeTestRule, FAKE_EVENT.date.hour)
    selectMinute(composeTestRule, FAKE_EVENT.date.minute)
    pressOKTime(composeTestRule)
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_TITLE_TEXT_FIELD)
        .performTextInput(FAKE_EVENT.title)
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_DESCRIPTION_TEXT_FIELD)
        .performTextInput(FAKE_EVENT.description!!)

    composeTestRule.waitUntil(69_000L) {
      composeTestRule
          .onNodeWithTag(EventCreationTestTags.SAVE_EVENT_BUTTON)
          .assertIsDisplayed()
          .isDisplayed()
    }
    composeTestRule.onNodeWithTag(EventCreationTestTags.SAVE_EVENT_BUTTON).performClick()
    composeTestRule.waitForIdle()
  }

  private fun seeAddedEventInEventList() = runTest {
    composeTestRule.waitUntil(10_000L) {
      composeTestRule.onNodeWithTag(NavigationTestTags.EVENT_TAB).isDisplayed()
    }
    composeTestRule.onNodeWithTag(NavigationTestTags.EVENT_TAB).performClick()
    composeTestRule.waitUntil(5_000L) {
      composeTestRule
          .onAllNodesWithTag("${EventCardTestTags.EVENT_CARD}_0", useUnmergedTree = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    // There should be a single event therefore we can check using the onFirst() function
    composeTestRule
        .onAllNodesWithTag("${EventContentTestTags.EVENT_TITLE}_0", useUnmergedTree = true)
        .onFirst()
        .assertTextEquals(FAKE_EVENT.title)
    composeTestRule
        .onAllNodesWithTag("${EventContentTestTags.EVENT_DESCRIPTION}_0", useUnmergedTree = true)
        .onFirst()
        .assertTextEquals(FAKE_EVENT.description!!)
  }

  private fun clickOnEventInList() = runTest {
    composeTestRule
        .onAllNodesWithTag("${EventCardTestTags.EVENT_CARD}_0", useUnmergedTree = true)
        .onFirst()
        .performClick()

    // Check that the event can be seen on the map
    composeTestRule.waitUntil(5_000L) {
      composeTestRule
          .onAllNodesWithTag(MapScreenTestTags.EVENT_INFO_POPUP)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
  }
}
