package com.android.universe.ui.common

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
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
      onChatClick: () -> Unit = {},
      onAddClick: () -> Unit = {},
      onSettingsClick: (() -> Unit)? = null,
      actionRowEnabled: Boolean = true,
      followers: Int? = 0,
      following: Int? = 0
  ) {
    composeTestRule.setContentWithStubBackdrop {
      ProfileContentLayout(
          modifier = Modifier.fillMaxSize(),
          userProfile = testUserProfile,
          followers = followers,
          following = following,
          heightTagList = 260.dp,
          actionRowEnabled = actionRowEnabled,
          onChatClick = onChatClick,
          onAddClick = onAddClick,
          onSettingsClick = onSettingsClick)
    }
  }

  @Test
  fun profileContent_displaysUserInfoCorrectly() {
    setProfileContent(followers = 10, following = 5)

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
  fun profileContent_actionsRow_isNotDisplayed_whenActionRowEnabledFalse() {
    setProfileContent(actionRowEnabled = false)

    composeTestRule
        .onNodeWithTag("${ProfileContentTestTags.CHAT_BUTTON}_${testUserProfile.uid}")
        .assertDoesNotExist()

    composeTestRule
        .onNodeWithTag("${ProfileContentTestTags.ADD_BUTTON}_${testUserProfile.uid}")
        .assertDoesNotExist()
  }

  @Test
  fun profileContent_actionsRow_isDisplayed_whenActionRowEnabledTrue() {
    var chatClicked = false
    var addClicked = false

    setProfileContent(onChatClick = { chatClicked = true }, onAddClick = { addClicked = true })

    composeTestRule
        .onNodeWithTag("${ProfileContentTestTags.CHAT_BUTTON}_${testUserProfile.uid}")
        .performClick()
    assert(chatClicked)

    composeTestRule
        .onNodeWithTag("${ProfileContentTestTags.ADD_BUTTON}_${testUserProfile.uid}")
        .performClick()
    assert(addClicked)
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
    setProfileContent(followers = 100, following = 50)

    composeTestRule
        .onNodeWithTag("${ProfileContentTestTags.FOLLOWERS_COUNT}_${testUserProfile.uid}")
        .assertExists()

    composeTestRule.onNodeWithText("100").assertExists()

    composeTestRule.onNodeWithText("followers").assertExists()

    composeTestRule
        .onNodeWithTag("${ProfileContentTestTags.FOLLOWING_COUNT}_${testUserProfile.uid}")
        .assertExists()

    composeTestRule.onNodeWithText("50").assertExists()

    composeTestRule.onNodeWithText("following").assertExists()
  }
}
