package com.android.universe.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.android.universe.MainActivity
import com.android.universe.resources.C
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityUITest {

  @get:Rule
  val permissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(
          android.Manifest.permission.ACCESS_FINE_LOCATION,
          android.Manifest.permission.ACCESS_COARSE_LOCATION)

  @get:Rule val activityRule = ActivityScenarioRule(MainActivity::class.java)

  @get:Rule val composeRule = createAndroidComposeRule<MainActivity>()

  @Test
  fun verifyMainScreenContainerExists() {
    composeRule.waitForIdle()
    composeRule.onNodeWithTag(C.Tag.main_screen_container).assertIsDisplayed()
  }
}
