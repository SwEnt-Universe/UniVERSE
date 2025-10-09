package com.android.universe.ui.signIn

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.ui.navigation.NavigationTestTags
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Instrumented test, which will execute on an Android device. */
@RunWith(AndroidJUnit4::class)
class SignInScreenTest : TestCase() {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun signInScreen_is_visible() {
    composeTestRule.setContent { SignInScreen() }
    signInScreenIsVisible()
  }

  fun signInScreenIsVisible() {
    isVisible(NavigationTestTags.SIGN_IN_SCREEN)
  }

  fun isVisible(testTag: String) {
    composeTestRule.onNodeWithTag(testTag).assertIsDisplayed()
  }
}
