package com.android.universe.ui.common

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.core.graphics.createBitmap
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
  val testBitmap = createBitmap(100, 100, android.graphics.Bitmap.Config.ARGB_8888)
  val testImage: ImageBitmap = testBitmap.asImageBitmap()

  private fun setProfileContent(
      onChatClick: () -> Unit = {},
      onAddClick: () -> Unit = {},
      onSettingsClick: (() -> Unit)? = null,
      followers: Int? = 0,
      following: Int? = 0
  ) {
    composeTestRule.setContentWithStubBackdrop {
      ProfileContentLayout(
          modifier = Modifier.fillMaxSize(),
          userProfile = testUserProfile,
          userProfileImage = testImage,
          followers = followers,
          following = following,
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
        .onNodeWithTag(ProfileContentTestTags.DESCRIPTION)
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
        .onNodeWithTag(ProfileContentTestTags.SETTINGS_BUTTON)
        .assertExists()
        .performClick()

    assert(settingsClicked.value)
  }

  @Test
  fun profileContent_actionsRow_isDisplayed_whenNoSettings() {
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

    composeTestRule.onNodeWithTag(ProfileContentTestTags.TAGS_COLUMN).assertExists()
  }
}
