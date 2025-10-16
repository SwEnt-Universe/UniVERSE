package com.android.universe.ui.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.android.universe.UniverseApp
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UniverseAppNavigationTest : TestCase() {

  @get:Rule val composeTestRule = createComposeRule()
  @get:Rule
  val permissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION)

  // This is a placeholder setup for the test to correctly launch while the FirebaseEmulator is in
  // development
  @Before
  fun setup() {
    runTest { Firebase.auth.signInAnonymously().await() }
    composeTestRule.setContent { UniverseApp() }
  }

  @After
  fun tearDown() {
    runTest {
      FirebaseAuth.getInstance().currentUser?.delete()
      Firebase.auth.signOut()
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
}
