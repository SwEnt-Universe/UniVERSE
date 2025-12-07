package com.android.universe.e2e

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.android.universe.UniverseApp
import com.android.universe.di.DefaultDP
import com.android.universe.ui.chat.ChatListScreenTestTags
import com.android.universe.ui.chat.composable.SendMessageInputTestTags
import com.android.universe.ui.common.FormTestTags
import com.android.universe.ui.common.UniverseBackgroundContainer
import com.android.universe.ui.eventCreation.EventCreationTestTags
import com.android.universe.ui.map.MapCreateEventModalTestTags
import com.android.universe.ui.map.MapScreenTestTags
import com.android.universe.ui.map.MapViewModel
import com.android.universe.ui.map.MapViewModelFactory
import com.android.universe.ui.navigation.FlowBottomMenuTestTags
import com.android.universe.ui.navigation.NavigationTestTags
import com.android.universe.ui.signIn.SignInScreenTestTags
import com.android.universe.ui.theme.UniverseTheme
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
        var fakeUser = UserTestData.Bob
        const val FAKE_EMAIL = UserTestData.bobEmail
        const val FAKE_PASS = UserTestData.bobPassword
        val FAKE_EVENT = EventTestData.futureEventNoTags
        const val TIME_INPUT = "13:25"
        const val TEST_MESSAGE = "Hello World!"
    }

    @get:Rule
    val composeTestRule = createComposeRule()

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(ACCESS_FINE_LOCATION)

    private lateinit var mapViewModel: MapViewModel
    private lateinit var context: Context

    @Before
    override fun setUp() {
        super.setUp()
        context = ApplicationProvider.getApplicationContext()
        mapViewModel = MapViewModelFactory(context).create(MapViewModel::class.java)
        mockkObject(DefaultDP)
        every { DefaultDP.io } returns UnconfinedTestDispatcher()
        every { DefaultDP.default } returns UnconfinedTestDispatcher()
        every { DefaultDP.main } returns UnconfinedTestDispatcher()

        runTest {
            val uid = createTestUser(fakeUser, FAKE_EMAIL, FAKE_PASS)
            fakeUser = fakeUser.copy(uid = uid)
            Firebase.auth.signOut()
            advanceUntilIdle()
            composeTestRule.setContentWithStubBackdrop {
                UniverseTheme {
                    UniverseBackgroundContainer(mapViewModel) {
                        UniverseApp(mapViewModel = mapViewModel)
                    }
                }
            }
            advanceUntilIdle()
        }
    }

    @Test
    fun createEventAndSendChatMessage() {
        loginAndWait()

        createEvent()

        openChatAndSendMessage()
    }

    private fun openChatAndSendMessage() = runTest {
        // 1. Wait for the Bottom Bar to be visible and stable
        composeTestRule.waitUntil(5_000L) {
            composeTestRule.onNodeWithTag(NavigationTestTags.CHAT_TAB).isDisplayed()
        }

        composeTestRule.mainClock.advanceTimeBy(5_000L)
        advanceUntilIdle()

        // 2. Perform the click
        composeTestRule.onNodeWithTag(NavigationTestTags.CHAT_TAB).performClick()

        // 3. Verify Chat Screen is displayed
        composeTestRule.waitUntil(5_000L) {
            composeTestRule.onNodeWithTag(NavigationTestTags.CHAT_SCREEN).isDisplayed()
        }

        // 4. Wait for the Chat List to populate
        composeTestRule.waitUntil(10_000L) {
            composeTestRule.onNodeWithTag(ChatListScreenTestTags.CHAT_LIST_COLUMN).isDisplayed()
        }

        // 5. Find the chat item by the Event Title and click it
        composeTestRule.waitUntil(5_000L) {
            composeTestRule.onAllNodes(hasText(FAKE_EVENT.title)).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onAllNodes(hasText(FAKE_EVENT.title)).onFirst().performClick()

        // 6. Verify we are in the specific Chat Screen (Input field should be visible)
        composeTestRule.waitUntil(5_000L) {
            composeTestRule.onNodeWithTag(SendMessageInputTestTags.TEXT_FIELD).isDisplayed()
        }

        // 7. Type and Send
        composeTestRule
            .onNodeWithTag(SendMessageInputTestTags.TEXT_FIELD)
            .performClick()
            .performTextInput(TEST_MESSAGE)

        composeTestRule
            .onNodeWithTag(SendMessageInputTestTags.SEND_BUTTON)
            .performClick()

        advanceUntilIdle()
    }

    private fun loginAndWait() = runTest {
        composeTestRule.waitUntil(5_000L) {
            composeTestRule.onNodeWithTagWithUnmergedTree(SignInScreenTestTags.WELCOME_BOX).isDisplayed()
        }

        composeTestRule
            .onNodeWithTagWithUnmergedTree(SignInScreenTestTags.JOIN_BUTTON)
            .assertIsDisplayed()
            .performClick()

        composeTestRule
            .onNodeWithTagWithUnmergedTree(FormTestTags.EMAIL_FIELD)
            .performClick()
            .performTextInput(FAKE_EMAIL)

        composeTestRule
            .onNodeWithTag(FlowBottomMenuTestTags.CONFIRM_BUTTON)
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitUntil(60_000L) {
            composeTestRule.onNodeWithTagWithUnmergedTree(SignInScreenTestTags.PASSWORD_BOX).isDisplayed()
        }

        composeTestRule
            .onNodeWithTagWithUnmergedTree(FormTestTags.PASSWORD_FIELD)
            .performClick()
            .performTextInput(FAKE_PASS)

        composeTestRule.onNodeWithTag(FlowBottomMenuTestTags.CONFIRM_BUTTON).performClick()

        composeTestRule.waitUntil(30_000L) {
            composeTestRule.onNodeWithTag(NavigationTestTags.MAP_SCREEN).isDisplayed()
        }
        advanceUntilIdle()

        composeTestRule.waitUntil(15_000L) {
            composeTestRule.onNodeWithTagWithUnmergedTree(MapScreenTestTags.INTERACTABLE).isDisplayed()
        }
        advanceUntilIdle()
    }

    private fun createEvent() = runTest {
        composeTestRule.waitUntil(10_000L) {
            composeTestRule.onNodeWithTag(MapScreenTestTags.CREATE_EVENT_BUTTON).isDisplayed()
        }
        composeTestRule.onNodeWithTag(MapScreenTestTags.CREATE_EVENT_BUTTON).performClick()

        composeTestRule.waitUntil(10_000L) {
            runCatching {
                composeTestRule
                    .onAllNodesWithTag(
                        MapCreateEventModalTestTags.MANUAL_CREATE_EVENT_BUTTON, useUnmergedTree = true)
                    .onFirst()
                    .assertExists()
            }.isSuccess
        }
        composeTestRule
            .onAllNodesWithTag(
                MapCreateEventModalTestTags.MANUAL_CREATE_EVENT_BUTTON, useUnmergedTree = true)
            .onFirst()
            .performClick()

        composeTestRule.waitUntil(10_000L) {
            runCatching {
                composeTestRule.onNodeWithTag(EventCreationTestTags.SET_LOCATION_BUTTON).assertExists()
            }.isSuccess
        }

        composeTestRule.onNodeWithTag(EventCreationTestTags.SET_LOCATION_BUTTON).performClick()
        composeTestRule.onNodeWithTag(MapScreenTestTags.INTERACTABLE).performTouchInput {
            down(center)
            advanceEventTime(1_000)
        }
        composeTestRule.onNodeWithTag(EventCreationTestTags.CREATION_EVENT_TITLE).performTouchInput {
            up()
        }

        composeTestRule.waitUntil(5_000L) {
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