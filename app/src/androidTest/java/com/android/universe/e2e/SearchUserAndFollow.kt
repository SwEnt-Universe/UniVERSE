package com.android.universe.e2e

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
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
import com.android.universe.ui.common.FormTestTags
import com.android.universe.ui.common.ProfileContentTestTags
import com.android.universe.ui.common.UniverseBackgroundContainer
import com.android.universe.ui.map.MapScreenTestTags
import com.android.universe.ui.map.MapViewModel
import com.android.universe.ui.map.MapViewModelFactory
import com.android.universe.ui.navigation.FlowBottomMenuTestTags
import com.android.universe.ui.navigation.NavigationTestTags
import com.android.universe.ui.searchProfile.ProfileCardTestTags
import com.android.universe.ui.searchProfile.SearchProfileScreenTestTags
import com.android.universe.ui.signIn.SignInScreenTestTags
import com.android.universe.ui.theme.UniverseTheme
import com.android.universe.utils.FirebaseAuthUserTest
import com.android.universe.utils.UserTestData
import com.android.universe.utils.onNodeWithTagWithUnmergedTree
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
class SearchUserAndFollow : FirebaseAuthUserTest(isRobolectric = false) {

  companion object {
    var bobUser = UserTestData.Bob
    var aliceUser = UserTestData.Alice
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
  fun searchFollowAndVerify() {
    // Search for Bob and Follow him
    loginAndWait(aliceEmail, PASSWORD)
    followBobAsAlice()
    logoutAndWait()

    // Login and Check follower count
    loginAndWait(bobEmail, PASSWORD)
    verifyFollowerAsBob()
  }

  private fun followBobAsAlice() = runTest {
    // Navigate to Community/Search Tab
    composeTestRule.waitUntil(10_000L) {
      composeTestRule.onNodeWithTag(NavigationTestTags.COMMUNITY_TAB).isDisplayed()
    }
    composeTestRule.onNodeWithTag(NavigationTestTags.COMMUNITY_TAB).performClick()

    composeTestRule.waitUntil(5_000L) {
      composeTestRule.onNodeWithTag(SearchProfileScreenTestTags.HEADER).isDisplayed()
    }

    // Wait for Bob's Profile Card to appear in the list
    composeTestRule.waitUntil(10_000L) {
      composeTestRule
          .onAllNodesWithTag("${ProfileCardTestTags.PROFILE_CARD}_$bobUid")
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    // Identify the "Follow" button
    val followButtonTag = "${ProfileContentTestTags.ADD_BUTTON}_$bobUid"

    composeTestRule.waitUntil(5_000L) {
      composeTestRule.onNodeWithTag(followButtonTag).isDisplayed()
    }

    // Perform Click using Touch Input (Same as joinEventAsAlice)
    composeTestRule.onNodeWithTag(followButtonTag).performTouchInput {
      click(center)
      advanceEventTime(1_000L)
    }

    // Wait for text to change to "Unfollow"
    composeTestRule.waitUntil(10_000L) {
      runCatching { composeTestRule.onNodeWithTag(followButtonTag).assert(hasText("Unfollow")) }
          .isSuccess
    }

    advanceUntilIdle()
  }

  private fun verifyFollowerAsBob() = runTest {
    // Navigate to Profile Tab
    composeTestRule.waitUntil(5_000L) {
      composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).isDisplayed()
    }
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()

    composeTestRule.waitUntil(5_000L) {
      composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_SCREEN).isDisplayed()
    }

    // Check Followers Count
    val followersCountTag = "${ProfileContentTestTags.FOLLOWERS_COUNT}_$bobUid"

    composeTestRule.waitUntil(5_000L) {
      composeTestRule.onNodeWithTag(followersCountTag).isDisplayed()
    }

    // Assert the count is "1"
    composeTestRule.waitUntil(5_000L) {
      composeTestRule.onAllNodes(hasText("1")).fetchSemanticsNodes().isNotEmpty()
    }

    composeTestRule
        .onNodeWithTag(followersCountTag, useUnmergedTree = true)
        .assert(hasAnyDescendant(hasText("1")))
  }

  private fun logoutAndWait() = runTest {
    // Navigate to Profile
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()

    composeTestRule.waitUntil(5_000L) {
      composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_SCREEN).isDisplayed()
    }

    // Open Settings
    val uid = Firebase.auth.currentUser!!.uid
    composeTestRule.onNodeWithTag("${ProfileContentTestTags.SETTINGS_BUTTON}_$uid").performClick()

    // Click Logout in Bottom Menu
    composeTestRule.onNodeWithTag(FlowBottomMenuTestTags.LOGOUT_BUTTON).performClick()

    // Wait for the Dialog to appear
    composeTestRule.waitUntil(5_000L) {
      composeTestRule
          .onAllNodes(hasText("Are you sure you want to log out?"))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    // Click "Logout" in the Dialog
    composeTestRule.onAllNodes(hasText("Logout")).onLast().performClick()

    // Wait for the Welcome Screen
    composeTestRule.waitUntil(10_000L) {
      composeTestRule.onNodeWithTagWithUnmergedTree(SignInScreenTestTags.WELCOME_BOX).isDisplayed()
    }
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

    composeTestRule.waitUntil(30_000L) {
      composeTestRule.onNodeWithTag(NavigationTestTags.MAP_SCREEN).isDisplayed()
    }
    advanceUntilIdle()
    composeTestRule.waitUntil(15_000L) {
      composeTestRule.onNodeWithTagWithUnmergedTree(MapScreenTestTags.INTERACTABLE).isDisplayed()
    }
  }
}
