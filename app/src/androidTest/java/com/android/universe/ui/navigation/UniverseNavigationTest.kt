package com.android.universe.ui.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.android.universe.UniverseApp
import com.android.universe.model.Tag
import com.android.universe.model.user.UserProfile
import com.android.universe.model.user.UserRepositoryProvider
import com.android.universe.ui.profile.UserProfileScreenTestTags
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

object Emulator {
  val auth = Firebase.auth

  init {
    auth.useEmulator("10.0.2.2", 9099)
  }
}

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class UniverseAppNavigationTest {

  private val dispatcher = UnconfinedTestDispatcher()
  val emulator = Emulator

  @get:Rule val composeTestRule = createComposeRule()
  @get:Rule
  val permissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION)

  // This is a placeholder setup for the test to correctly launch while the FirebaseEmulator is in
  // development
  @Before
  fun setup() {
    Dispatchers.setMain(dispatcher)
    runTest {
      emulator.auth.signInAnonymously().await()

      UserRepositoryProvider.repository.addUser(
          UserProfile(
              uid = emulator.auth.currentUser!!.uid,
              username = "tester",
              firstName = "testa",
              lastName = "testo",
              country = "testastan",
              dateOfBirth = LocalDate.of(2003, 12, 3),
              tags = setOf(Tag.TABLE_TENNIS),
          ))
    }
    composeTestRule.setContent { UniverseApp() }
  }

  @After
  fun tearDown() {
    runTest {
      emulator.auth.currentUser?.delete()
      emulator.auth.signOut()
    }
  }

  @Test
  fun app_startsAtMapScreen() {
    // Verify that Map screen is displayed
    composeTestRule.onNodeWithTag(NavigationTestTags.MAP_SCREEN).assertIsDisplayed()
  }

  @Test
  fun navigation_toChatScreen() {

    // Click on Chat tab (simulate tab selection)
    composeTestRule.onNodeWithTag(NavigationTestTags.CHAT_TAB).performClick()

    // Verify that Chat screen is displayed
    composeTestRule.onNodeWithTag(NavigationTestTags.CHAT_SCREEN).assertIsDisplayed()
  }

  @Test
  fun navigation_toEventScreen() {

    // Click on Event tab (simulate tab selection)
    composeTestRule.onNodeWithTag(NavigationTestTags.EVENT_TAB).performClick()

    // Verify that Event screen is displayed
    composeTestRule.onNodeWithTag(NavigationTestTags.EVENT_SCREEN).assertIsDisplayed()
  }

  @Test
  fun navigation_toProfileScreen() {

    // Click on Profile tab (simulate tab selection)
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()

    // Verify that Profile screen is displayed
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_SCREEN).assertIsDisplayed()
  }

  @Test
  fun navigation_toAllTabs() {
    // Verify that Map screen is displayed
    composeTestRule.onNodeWithTag(NavigationTestTags.MAP_SCREEN).assertIsDisplayed()
    // Navigate to all tabs
    tabs.forEach { tab ->
      composeTestRule.onNodeWithTag(NavigationTestTags.getTabTestTag(tab)).performClick()
      composeTestRule
          .onNodeWithTag(NavigationTestTags.getScreenTestTag(tab.destination))
          .assertIsDisplayed()
    }
  }

  @Test
  fun navigation_toSettingsScreen() {
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_SCREEN).assertIsDisplayed()
    composeTestRule.onNodeWithTag(UserProfileScreenTestTags.EDIT_BUTTON).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.SETTINGS_SCREEN).assertIsDisplayed()
  }
}
