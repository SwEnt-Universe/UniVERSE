package com.android.universe.ui.map

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.MainActivity
import com.android.universe.ui.navigation.NavigationTestTags
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MapScreenTest : TestCase() {

  @get:Rule val composeRule = createAndroidComposeRule<MainActivity>()

  @Test
  fun mapScreen_displaysContainerAndBottomMenu() {
    // Start destination is Map, so the map screen scaffold should be visible
    composeRule.onNodeWithTag(NavigationTestTags.MAP_SCREEN).assertIsDisplayed()
    // Bottom navigation menu is visible
    composeRule.onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
  }
}
