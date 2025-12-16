package com.android.universe.e2e

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.android.universe.UniverseApp
import com.android.universe.di.DefaultDP
import com.android.universe.model.user.UserRepositoryProvider
import com.android.universe.ui.chat.ChatListScreenTestTags
import com.android.universe.ui.chat.composable.SendMessageInputTestTags
import com.android.universe.ui.common.EventContentTestTags
import com.android.universe.ui.common.FormTestTags
import com.android.universe.ui.common.ProfileContentTestTags
import com.android.universe.ui.common.UniverseBackgroundContainer
import com.android.universe.ui.event.EventCardTestTags
import com.android.universe.ui.eventCreation.EventCreationTestTags
import com.android.universe.ui.map.MapCreateEventModalTestTags
import com.android.universe.ui.map.MapScreenTestTags
import com.android.universe.ui.map.MapViewModel
import com.android.universe.ui.map.MapViewModelFactory
import com.android.universe.ui.navigation.FlowBottomMenuTestTags
import com.android.universe.ui.navigation.NavigationTestTags
import com.android.universe.ui.signIn.SignInScreenTestTags
import com.android.universe.ui.theme.UniverseTheme
import com.android.universe.utils.CustomComposeSemantics.hasTestTagPrefix
import com.android.universe.utils.CustomComposeSemantics.hasText
import com.android.universe.utils.EventTestData
import com.android.universe.utils.FirebaseAuthUserTest
import com.android.universe.utils.UserTestData
import com.android.universe.utils.nextMonth
import com.android.universe.utils.onNodeWithTagWithUnmergedTree
import com.android.universe.utils.pressOKDate
import com.android.universe.utils.selectDayWithMonth
import com.android.universe.utils.setContentWithStubBackdrop
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import io.mockk.every
import io.mockk.mockkObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class JoinAndChatTest : FirebaseAuthUserTest(isRobolectric = false) {

  companion object {
    // User 1: Bob
    var bobUser = UserTestData.Bob
    const val BOB_MESSAGE = "Hello from Bob!"

    // User 2: Alice
    var aliceUser = UserTestData.Alice
    const val ALICE_MESSAGE = "Hello Bob, Alice here!"

    val FAKE_EVENT = EventTestData.futureEventNoTags
    const val TIME_INPUT = "13:25"
  }

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @get:Rule
  val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(ACCESS_FINE_LOCATION)

  private lateinit var mapViewModel: MapViewModel
  private lateinit var context: Context

  private lateinit var bobEmail: String
  private lateinit var bobUid: String
  private lateinit var aliceEmail: String
  private lateinit var aliceUid: String

  @Before
  override fun setUp() {
    super.setUp()
    context = ApplicationProvider.getApplicationContext()
    mapViewModel = MapViewModelFactory(context).create(MapViewModel::class.java)
    mockkObject(DefaultDP)
    every { DefaultDP.io } returns UnconfinedTestDispatcher()
    every { DefaultDP.default } returns UnconfinedTestDispatcher()
    every { DefaultDP.main } returns Dispatchers.Main

    runTest {
      // Create independent e-mail address
      createRandomTestUser(bobUser).let {
        bobEmail = it.first
        bobUid = it.second
      }
      bobUser = bobUser.copy(uid = bobUid)
      UserRepositoryProvider.repository.addUser(bobUser)

      createRandomTestUser(aliceUser).let {
        aliceEmail = it.first
        aliceUid = it.second
      }
      aliceUser = aliceUser.copy(uid = aliceUid)
      UserRepositoryProvider.repository.addUser(aliceUser)
      advanceUntilIdle()

      Firebase.auth.signOut()
      advanceUntilIdle()

      composeTestRule.setContentWithStubBackdrop {
        UniverseTheme {
          UniverseBackgroundContainer(mapViewModel) { UniverseApp(mapViewModel = mapViewModel) }
        }
      }
      advanceUntilIdle()
    }
  }

  @Test
  fun createEventAndSendChatMessage() {
    // Bob
    // Login -> Create Event -> Send Message -> Log out
    loginAndWait(bobEmail, PASSWORD)
    createEvent()
    openChatAndSendMessage(BOB_MESSAGE)
    logoutAndWait()

    // Alice
    // Login -> Join Event -> Reply in Chat
    loginAndWait(aliceEmail, PASSWORD)
    joinEventAsAlice()
    openChatVerifyAndReply()
  }

  private fun joinEventAsAlice() = runTest {
    // 1. Navigate to Event Tab
    composeTestRule.waitUntil(10_000L) {
      composeTestRule.onNodeWithTag(NavigationTestTags.EVENT_TAB).isDisplayed()
    }
    composeTestRule.onNodeWithTag(NavigationTestTags.EVENT_TAB).performClick()

    composeTestRule.waitUntil(5_001L) {
      composeTestRule
          .onAllNodesWithTag("${EventCardTestTags.EVENT_CARD}_0", useUnmergedTree = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    // 2. Click the Card
    composeTestRule
        .onAllNodesWithTag("${EventCardTestTags.EVENT_CARD}_0", useUnmergedTree = true)
        .onFirst()
        .performClick()

    // 3. Wait for Popup
    composeTestRule.waitUntil(23_333L) {
      composeTestRule
          .onAllNodesWithTag(MapScreenTestTags.EVENT_INFO_POPUP)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    // 4. Click "Join" inside the Popup
    composeTestRule.waitUntil(5_002L) {
      composeTestRule
          .onNode(hasTestTagPrefix(EventContentTestTags.PARTICIPATION_BUTTON))
          .isDisplayed()
    }

    composeTestRule
        .onNode(hasTestTagPrefix(EventContentTestTags.PARTICIPATION_BUTTON))
        .performTouchInput {
          click(center)
          advanceEventTime(1_000L)
        }

    composeTestRule.waitUntil(5_003L) {
      composeTestRule
          .onNode(hasTestTagPrefix(EventContentTestTags.PARTICIPATION_BUTTON))
          .hasText("Leave")
    }
    // 5. Dismiss Popup
    composeTestRule.onNodeWithTag(MapScreenTestTags.EVENT_INFO_POPUP).performTouchInput {
      click(topRight)
    }

    // 6. Wait for Dismissal
    composeTestRule.waitUntil(5_004L) {
      composeTestRule
          .onAllNodesWithTag(MapScreenTestTags.EVENT_INFO_POPUP)
          .fetchSemanticsNodes()
          .isEmpty()
    }

    composeTestRule.waitUntil(5_005L) {
      composeTestRule.onNodeWithTag(NavigationTestTags.CHAT_TAB).isDisplayed()
    }

    advanceUntilIdle()
  }

  private fun openChatVerifyAndReply() = runTest {
    // 1. Navigate to Chat Tab
    composeTestRule.onNodeWithTag(NavigationTestTags.CHAT_TAB).performClick()

    composeTestRule.waitUntil(5_006L) {
      composeTestRule.onNodeWithTag(ChatListScreenTestTags.CHAT_LIST_COLUMN).isDisplayed()
    }

    // 2. Enter the Event Chat
    composeTestRule.waitUntil(5_007L) {
      composeTestRule.onAllNodes(hasText(FAKE_EVENT.title)).fetchSemanticsNodes().isNotEmpty()
    }
    composeTestRule.onAllNodes(hasText(FAKE_EVENT.title)).onFirst().performClick()

    // 3. Verify Bob's Message exists
    composeTestRule.waitUntil(5_008L) {
      composeTestRule.onAllNodes(hasText(BOB_MESSAGE)).fetchSemanticsNodes().isNotEmpty()
    }

    // 4. Send Alice's Reply
    composeTestRule.waitUntil(5_009L) {
      composeTestRule.onNodeWithTag(SendMessageInputTestTags.TEXT_FIELD).isDisplayed()
    }

    composeTestRule
        .onNodeWithTag(SendMessageInputTestTags.TEXT_FIELD)
        .performClick()
        .performTextInput(ALICE_MESSAGE)

    composeTestRule.onNodeWithTag(SendMessageInputTestTags.SEND_BUTTON).performClick()

    // 5. Verify Alice's message appears
    composeTestRule.waitUntil(2_000L) {
      composeTestRule.onAllNodes(hasText(ALICE_MESSAGE)).fetchSemanticsNodes().isNotEmpty()
    }
  }

  private fun logoutAndWait() = runTest {
    // 1. Navigate to Profile
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()

    composeTestRule.waitUntil(5_010L) {
      composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_SCREEN).isDisplayed()
    }

    // 2. Open Settings
    val uid = Firebase.auth.currentUser!!.uid
    composeTestRule.onNodeWithTag("${ProfileContentTestTags.SETTINGS_BUTTON}_$uid").performClick()

    // 3. Click Logout in Bottom Menu
    composeTestRule.onNodeWithTag(FlowBottomMenuTestTags.LOGOUT_BUTTON).performClick()

    // 4. Wait for the Dialog to appear
    composeTestRule.waitUntil(5_011L) {
      composeTestRule
          .onAllNodes(hasText("Are you sure you want to log out?"))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    // 5. Click "Logout" in the Dialog
    composeTestRule.onAllNodes(hasText("Logout")).onLast().performClick()

    // 6. Wait for the Welcome Screen
    composeTestRule.waitUntil(10_000L) {
      composeTestRule.onNodeWithTagWithUnmergedTree(SignInScreenTestTags.WELCOME_BOX).isDisplayed()
    }
    advanceUntilIdle()
  }

  private fun openChatAndSendMessage(msg: String) = runTest {
    composeTestRule.waitUntil(5_012L) {
      composeTestRule.onNodeWithTag(NavigationTestTags.CHAT_TAB).isDisplayed()
    }
    composeTestRule.mainClock.advanceTimeBy(5_013L)

    composeTestRule.onNodeWithTag(NavigationTestTags.CHAT_TAB).performClick()

    composeTestRule.waitUntil(5_014L) {
      composeTestRule.onNodeWithTag(ChatListScreenTestTags.CHAT_LIST_COLUMN).isDisplayed()
    }
    advanceUntilIdle()
    composeTestRule.waitUntil(60_015L) {
      composeTestRule.onAllNodes(hasText(FAKE_EVENT.title)).fetchSemanticsNodes().isNotEmpty()
    }
    composeTestRule.onAllNodes(hasText(FAKE_EVENT.title)).onFirst().performClick()

    composeTestRule.waitUntil(5_016L) {
      composeTestRule.onNodeWithTag(SendMessageInputTestTags.TEXT_FIELD).isDisplayed()
    }

    composeTestRule
        .onNodeWithTag(SendMessageInputTestTags.TEXT_FIELD)
        .performClick()
        .performTextInput(msg)

    composeTestRule.onNodeWithTag(SendMessageInputTestTags.SEND_BUTTON).performClick()

    advanceUntilIdle()
  }

  private fun loginAndWait(email: String, pass: String) = runTest {
    composeTestRule.waitUntil(10_000L) {
      composeTestRule.onNodeWithTagWithUnmergedTree(SignInScreenTestTags.WELCOME_BOX).isDisplayed()
    }
    composeTestRule.onNodeWithTagWithUnmergedTree(SignInScreenTestTags.JOIN_BUTTON).performClick()
    composeTestRule.onNodeWithTagWithUnmergedTree(FormTestTags.EMAIL_FIELD).performTextInput(email)
    composeTestRule.onNodeWithTag(FlowBottomMenuTestTags.CONFIRM_BUTTON).performClick()

    composeTestRule.waitUntil(60_000L) {
      composeTestRule.onNodeWithTagWithUnmergedTree(SignInScreenTestTags.PASSWORD_BOX).isDisplayed()
    }
    composeTestRule
        .onNodeWithTagWithUnmergedTree(FormTestTags.PASSWORD_FIELD)
        .performTextInput(pass)
    composeTestRule.onNodeWithTag(FlowBottomMenuTestTags.CONFIRM_BUTTON).performClick()
    advanceUntilIdle()
    composeTestRule.waitForIdle()
    composeTestRule.waitUntil(60_000L) {
      composeTestRule.onNodeWithTag(NavigationTestTags.MAP_SCREEN).isDisplayed()
    }
    advanceUntilIdle()
    composeTestRule.waitUntil(15_000L) {
      composeTestRule.onNodeWithTagWithUnmergedTree(MapScreenTestTags.INTERACTABLE).isDisplayed()
    }
  }

  private fun createEvent() = runTest {
    composeTestRule.waitUntil(10_000L) {
      composeTestRule.onNodeWithTag(MapScreenTestTags.CREATE_EVENT_BUTTON).isDisplayed()
    }
    composeTestRule.onNodeWithTag(MapScreenTestTags.CREATE_EVENT_BUTTON).performClick()
    composeTestRule
        .onAllNodesWithTag(
            MapCreateEventModalTestTags.MANUAL_CREATE_EVENT_BUTTON, useUnmergedTree = true)
        .onFirst()
        .performClick()
    composeTestRule.waitUntil(10_000L) {
      composeTestRule
          .onNodeWithTagWithUnmergedTree(MapScreenTestTags.SELECT_LOCATION_TEXT)
          .isDisplayed()
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(MapScreenTestTags.INTERACTABLE).performTouchInput {
      down(center)
      advanceEventTime(1000L)
    }

    composeTestRule.onNodeWithTag(MapScreenTestTags.INTERACTABLE).performTouchInput { up() }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(FlowBottomMenuTestTags.CONFIRM_BUTTON).performClick()
    composeTestRule.waitForIdle()

    composeTestRule.waitUntil(9_000L) {
      composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_DATE_TEXT_FIELD).isDisplayed()
    }

    composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_DATE_TEXT_FIELD).performClick()
    composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_DATE_PICKER).performClick()

    nextMonth(composeTestRule)
    selectDayWithMonth(composeTestRule, FAKE_EVENT.date.toLocalDate())
    pressOKDate(composeTestRule)
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_TIME_TEXT_FIELD)
        .performTextInput(TIME_INPUT)
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_TITLE_TEXT_FIELD)
        .performTextInput(FAKE_EVENT.title)
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_DESCRIPTION_TEXT_FIELD)
        .performTextInput(FAKE_EVENT.description!!)

    composeTestRule.onNodeWithTag(FlowBottomMenuTestTags.CONFIRM_BUTTON).performClick()
    composeTestRule.onNodeWithTag(FlowBottomMenuTestTags.CONFIRM_BUTTON).performClick()

    composeTestRule.waitUntil(10_000L) {
      composeTestRule.onNodeWithTag(MapScreenTestTags.CREATE_EVENT_BUTTON).isDisplayed()
    }
    advanceUntilIdle()
  }
}
