package com.android.universe.ui.common

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.utils.UserTestData
import com.android.universe.utils.setContentWithStubBackdrop
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileContentLayoutTest {
  @get:Rule val composeTestRule = createComposeRule()

  private val testUserProfile = UserTestData.SomeTagsUser

  private fun setProfileContent(
      isFollowing: Boolean = true,
      onToggleFollowing: () -> Unit = {},
      onSettingsClick: (() -> Unit)? = null
  ) {
    composeTestRule.setContentWithStubBackdrop {
      ProfileContentLayout(
          modifier = Modifier.fillMaxSize(),
          userProfile = testUserProfile,
          isFollowing = isFollowing,
          onToggleFollowing = onToggleFollowing,
          onSettingsClick = onSettingsClick)
    }
  }

  @Test
  fun profileContent_displaysUserInfoCorrectly() {
    setProfileContent()
    composeTestRule
        .onNodeWithTag("${ProfileContentTestTags.FULL_NAME}_${testUserProfile.uid}")
        .assertExists()
        .assertTextEquals("${testUserProfile.firstName} ${testUserProfile.lastName}")

    composeTestRule
        .onNodeWithTag("${ProfileContentTestTags.DESCRIPTION}_${testUserProfile.uid}")
        .assertExists()
        .assertTextEquals(testUserProfile.description ?: "No description available")

    composeTestRule
        .onNodeWithTag("${ProfileContentTestTags.PROFILE_IMAGE_CONTAINER}_${testUserProfile.uid}")
        .assertExists()
  }

  @Test
  fun profileContent_settingsButton_isDisplayed_whenOnSettingsProvided() {
    val settingsClicked = mutableStateOf(false)

    setProfileContent(onSettingsClick = { settingsClicked.value = true })

    composeTestRule
        .onNodeWithTag("${ProfileContentTestTags.SETTINGS_BUTTON}_${testUserProfile.uid}")
        .assertExists()
        .performClick()

    assert(settingsClicked.value)
  }

  @Test
  fun profileContent_settingsButton_isNotDisplayed_whenOnSettingsIsNull() {
    setProfileContent(onSettingsClick = null)

    composeTestRule
        .onNodeWithTag("${ProfileContentTestTags.SETTINGS_BUTTON}_${testUserProfile.uid}")
        .assertDoesNotExist()
  }

  @Test
  fun profileContent_tagsAreDisplayed() {
    setProfileContent()

    composeTestRule
        .onNodeWithTag("${ProfileContentTestTags.TAGS_COLUMN}_${testUserProfile.uid}")
        .assertExists()
  }

  @Test
  fun profileContent_displaysFollowersAndFollowingCounts() {
    setProfileContent()
    composeTestRule
        .onNodeWithTag("${ProfileContentTestTags.FOLLOWERS_COUNT}_${testUserProfile.uid}")
        .assertExists()

    composeTestRule.onNodeWithText("3").assertExists()

    composeTestRule.onNodeWithText("followers").assertExists()

    composeTestRule
        .onNodeWithTag("${ProfileContentTestTags.FOLLOWING_COUNT}_${testUserProfile.uid}")
        .assertExists()

    composeTestRule.onNodeWithText("2").assertExists()

    composeTestRule.onNodeWithText("following").assertExists()
  }

  @Test
  fun profileContent_followButton_displaysFollow_whenNotFollowing_andCallsCallback() {
    var toggleCalled = false

    setProfileContent(isFollowing = false, onToggleFollowing = { toggleCalled = true })

    val tag = "${ProfileContentTestTags.ADD_BUTTON}_${testUserProfile.uid}"

    composeTestRule.onNodeWithTag(tag).assertExists()

    composeTestRule.onNodeWithText("Follow").assertExists()

    composeTestRule.onNodeWithTag(tag).performClick()

    assert(toggleCalled)
  }

  @Test
  fun profileContent_followButton_displaysUnfollow_whenFollowing_andCallsCallback() {
    var toggleCalled = false

    setProfileContent(isFollowing = true, onToggleFollowing = { toggleCalled = true })

    val tag = "${ProfileContentTestTags.ADD_BUTTON}_${testUserProfile.uid}"

    composeTestRule.onNodeWithTag(tag).assertExists()

    composeTestRule.onNodeWithText("Unfollow").assertExists()

    composeTestRule.onNodeWithTag(tag).performClick()

    assert(toggleCalled)
  }
}
