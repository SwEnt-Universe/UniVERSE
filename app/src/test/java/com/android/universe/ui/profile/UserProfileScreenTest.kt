package com.android.universe.ui.profile

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.model.event.FakeEventRepository
import com.android.universe.model.user.FakeUserRepository
import com.android.universe.ui.common.EventContentTestTags
import com.android.universe.ui.common.ProfileContentTestTags
import com.android.universe.ui.event.EventViewModel
import com.android.universe.utils.EventTestData
import com.android.universe.utils.MainCoroutineRule
import com.android.universe.utils.UserTestData
import com.android.universe.utils.setContentWithStubBackdrop
import java.time.LocalDateTime
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class UserProfileScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @get:Rule val mainCoroutineRule = MainCoroutineRule()

  private lateinit var fakeEventRepository: FakeEventRepository
  private lateinit var fakeUserRepository: FakeUserRepository
  private lateinit var userProfileViewModel: UserProfileViewModel
  private lateinit var eventViewModel: EventViewModel

  private val testUser = UserTestData.Alice

  private val pastEvent =
      EventTestData.dummyEvent1.copy(
          id = "past_event_id",
          title = "Ancient History Run",
          date = LocalDateTime.now().minusYears(1),
          participants = setOf(testUser.uid))

  private val futureEvent =
      EventTestData.dummyEvent2.copy(
          id = "future_event_id",
          title = "Future Space Party",
          date = LocalDateTime.now().plusYears(1),
          participants = setOf(testUser.uid))

  @Before
  fun setUp() {
    fakeEventRepository = FakeEventRepository()
    fakeUserRepository = FakeUserRepository()

    userProfileViewModel = UserProfileViewModel(fakeUserRepository, fakeEventRepository)
    eventViewModel = EventViewModel(fakeEventRepository, null, fakeUserRepository)

    runTest {
      fakeUserRepository.addUser(testUser)
      fakeEventRepository.addEvent(pastEvent)
      fakeEventRepository.addEvent(futureEvent)
    }
  }

  @Test
  fun displayUserProfileInformation() = runTest {
    setupScreen()

    val fullNameTag = "${ProfileContentTestTags.FULL_NAME}_${testUser.uid}"

    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule.onAllNodesWithTag(fullNameTag).fetchSemanticsNodes().isNotEmpty()
    }

    composeTestRule
        .onNodeWithTag(fullNameTag)
        .assertIsDisplayed()
        .assertTextEquals("${testUser.firstName} ${testUser.lastName}")

    composeTestRule
        .onNodeWithTag("${ProfileContentTestTags.DESCRIPTION}_${testUser.uid}")
        .assertExists()

    composeTestRule
        .onNodeWithTag("${ProfileContentTestTags.SETTINGS_BUTTON}_${testUser.uid}")
        .assertIsDisplayed()
  }

  @Test
  fun displayTabsAndHistoryListByDefault() = runTest {
    setupScreen()

    composeTestRule.onNodeWithText("History").assertIsDisplayed()
    composeTestRule.onNodeWithText("Incoming").assertIsDisplayed()

    assertEventDisplayed(pastEvent.title, pastEvent.id)
  }

  @Test
  fun navigateToIncomingTab_viaClick() = runTest {
    setupScreen()

    composeTestRule.onNodeWithText("Incoming").performClick()
    composeTestRule.waitForIdle()

    assertEventDisplayed(futureEvent.title, futureEvent.id)
  }

  @Test
  fun navigateToIncomingTab_viaSwipe() = runTest {
    setupScreen()

    composeTestRule.onNodeWithTag(UserProfileScreenTestTags.PROFILE_EVENT_LIST).performTouchInput {
      swipeLeft()
    }

    composeTestRule.waitForIdle()

    assertEventDisplayed(futureEvent.title, futureEvent.id)
  }

  @Test
  fun clickSettingsButton_triggersCallback() = runTest {
    var capturedUid: String? = null

    composeTestRule.setContentWithStubBackdrop {
      UserProfileScreen(
          uid = testUser.uid,
          onEditProfileClick = { uid -> capturedUid = uid },
          userProfileViewModel = userProfileViewModel,
          eventViewModel = eventViewModel)
    }
    advanceUntilIdle()
    composeTestRule
        .onNodeWithTag("${ProfileContentTestTags.SETTINGS_BUTTON}_${testUser.uid}")
        .performClick()

    assertEquals(testUser.uid, capturedUid)
  }

  private suspend fun TestScope.setupScreen() {
    composeTestRule.setContentWithStubBackdrop {
      UserProfileScreen(
          uid = testUser.uid,
          userProfileViewModel = userProfileViewModel,
          eventViewModel = eventViewModel)
    }

    advanceUntilIdle()

    composeTestRule.waitForIdle()
  }

  private fun assertEventDisplayed(eventTitle: String, eventId: String) {
    val eventTag = "${EventContentTestTags.EVENT_TITLE}_${eventId.hashCode()}"

    composeTestRule
        .onNodeWithTag(eventTag)
        .performScrollTo()
        .assertIsDisplayed()
        .assertTextEquals(eventTitle)
  }
}
