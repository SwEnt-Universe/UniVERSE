package com.android.universe.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.ui.profileCreation.AddProfileScreen
import com.android.universe.ui.profileCreation.AddProfileScreenTestTags
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AddProfileNavigationTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun clickingBackButton_navigatesToSignIn() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController, startDestination = NavigationScreens.AddProfile.route) {
        composable(NavigationScreens.AddProfile.route) {
          AddProfileScreen(
              uid = "test_uid",
              onBack = {
                navController.navigate(NavigationScreens.SignIn.route) {
                  popUpTo(NavigationScreens.AddProfile.route) { inclusive = true }
                }
              })
        }
        composable(NavigationScreens.SignIn.route) {
          Box(Modifier.testTag(NavigationTestTags.SIGN_IN_SCREEN)) { Text("Sign In Screen") }
        }
      }
    }

    // Assert AddProfileScreen is visible (TopAppBar)
    composeTestRule.onNodeWithTag(NavigationTestTags.ADD_PROFILE_SCREEN).assertIsDisplayed()

    // Click back
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.BACK_BUTTON).performClick()

    // Verify SignInScreen is now visible
    composeTestRule.onNodeWithTag(NavigationTestTags.SIGN_IN_SCREEN).assertIsDisplayed()
  }
}
