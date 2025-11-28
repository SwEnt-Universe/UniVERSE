package com.android.universe.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.android.universe.UniverseApp
import com.android.universe.di.DefaultDP
import com.android.universe.model.isoToCountryName
import com.android.universe.model.user.UserProfile
import com.android.universe.model.user.UserRepositoryProvider
import com.android.universe.ui.common.FormTestTags
import com.android.universe.ui.common.ProfileContentTestTags
import com.android.universe.ui.map.MapScreenTestTags
import com.android.universe.ui.navigation.FlowBottomMenuTestTags
import com.android.universe.ui.navigation.NavigationTestTags
import com.android.universe.ui.profileCreation.AddProfileScreenTestTags
import com.android.universe.ui.profileSettings.SettingsTestTags
import com.android.universe.ui.selectTag.SelectTagsScreenTestTags
import com.android.universe.ui.signIn.SignInScreenTestTags
import com.android.universe.ui.theme.UniverseTheme
import com.android.universe.utils.FirebaseAuthUserTest
import com.android.universe.utils.UserTestData
import com.android.universe.utils.setContentWithStubBackdrop
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import io.mockk.every
import io.mockk.mockkObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class ProfileLoginAndCreationTest : FirebaseAuthUserTest(isRobolectric = false) {
  @get:Rule val composeTestRule = createComposeRule()

  @get:Rule
  val permissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION)

  companion object {
    private const val FAKE_EMAIL = UserTestData.aliceEmail
    private const val FAKE_PASSWORD = UserTestData.alicePassword
    private val userTest = UserTestData.Alice
  }

  @Before
  override fun setUp() {
    super.setUp()
    mockkObject(DefaultDP)
    every { DefaultDP.io } returns UnconfinedTestDispatcher()
    every { DefaultDP.default } returns UnconfinedTestDispatcher()
    every { DefaultDP.main } returns Dispatchers.Main
    runTest {
      composeTestRule.setContentWithStubBackdrop { UniverseTheme { UniverseApp() } }
      composeTestRule.waitForIdle()
    }
  }

  @Test
  fun `profile login and creation works`() {
    println("loginAndWait")
    loginAndWait()

    println("fillProfileDetailAndWait")
    fillProfileDetailAndWait()

    println("selectTagAndWait")
    selectTagAndWait()

    println("compareCreatedProfile")
    compareCreatedProfile()

    println("tabNavigationAndPrepareForEdit")
    tabNavigationAndPrepareForEdit()

    println("changeNameToBobAndVerify")
    changeNameToBobAndVerify()
  }

  private fun changeNameToBobAndVerify() {
    val uid = Firebase.auth.currentUser!!.uid

    composeTestRule.onNodeWithTag("${ProfileContentTestTags.SETTINGS_BUTTON}_$uid").performClick()

    composeTestRule.onNodeWithTag(SettingsTestTags.FIRST_NAME_BUTTON).performClick()
    composeTestRule
        .onNode(hasSetTextAction() and hasTestTag(SettingsTestTags.FIRST_NAME_FIELD))
        .performClick()
        .performTextClearance()
    composeTestRule
        .onNode(hasSetTextAction() and hasTestTag(SettingsTestTags.FIRST_NAME_FIELD))
        .performTextInput("Bob")

    composeTestRule.onNodeWithTag(SettingsTestTags.MODAL_SAVE_BUTTON).performClick()

    composeTestRule.onNodeWithTag(SettingsTestTags.FIRST_NAME_BUTTON).assertTextContains("Bob")
  }

  private fun tabNavigationAndPrepareForEdit() {
    composeTestRule.onNodeWithTag(NavigationTestTags.CHAT_TAB).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.CHAT_SCREEN).assertIsDisplayed()

    composeTestRule.onNodeWithTag(NavigationTestTags.EVENT_TAB).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.EVENT_SCREEN).assertIsDisplayed()

    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_SCREEN).assertIsDisplayed()
    composeTestRule.waitForIdle()
    val uid = Firebase.auth.currentUser!!.uid
    composeTestRule.onNodeWithTag("${ProfileContentTestTags.FULL_NAME}_$uid").assertIsDisplayed()

    composeTestRule.onNodeWithTag("${ProfileContentTestTags.DESCRIPTION}_$uid").assertIsDisplayed()

    composeTestRule
        .onNodeWithTag("${ProfileContentTestTags.SETTINGS_BUTTON}_$uid")
        .assertIsDisplayed()
  }

  private fun compareCreatedProfile() {
    val createdUser = Firebase.auth.currentUser
    var createdUserProfile: UserProfile? = null
    assertNotNull(createdUser)
    runTest {
      // This delay avoid race conditions for the tags. Not the best but work for now
      createdUserProfile = UserRepositoryProvider.repository.getUser(createdUser!!.uid)
    }
    assertEquals(userTest.copy(uid = createdUser!!.uid), createdUserProfile)
  }

  private fun selectTagAndWait() {
    userTest.tags.forEach {
      val tagTestTag = SelectTagsScreenTestTags.TAG_BUTTON_PREFIX + it.displayName
      composeTestRule
          .onNodeWithTag(SelectTagsScreenTestTags.LAZY_COLUMN)
          .performScrollToNode(hasTestTag(tagTestTag))
      composeTestRule.onNodeWithTag(tagTestTag).performClick()
    }

    composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.SAVE_BUTTON).performClick()

    composeTestRule.onNodeWithTag(MapScreenTestTags.MAP_VIEW).assertIsDisplayed()
  }

  private fun fillProfileDetailAndWait() {
    composeTestRule
        .onNodeWithTag(AddProfileScreenTestTags.USERNAME_FIELD)
        .performClick()
        .performTextInput(userTest.username)

    composeTestRule
        .onNodeWithTag(AddProfileScreenTestTags.FIRST_NAME_FIELD)
        .performClick()
        .performTextInput(userTest.firstName)

    composeTestRule
        .onNodeWithTag(AddProfileScreenTestTags.LAST_NAME_FIELD)
        .performClick()
        .performTextInput(userTest.lastName)

    composeTestRule
        .onNodeWithTag(AddProfileScreenTestTags.DESCRIPTION_FIELD)
        .performClick()
        .performTextInput(userTest.description!!)

    // Wait 5 second max for the dropdown menu to appear
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.COUNTRY_FIELD).performClick()

    composeTestRule
        .onNodeWithTag(AddProfileScreenTestTags.DROPDOWN_SCROLLING_MENU)
        .performScrollToNode(
            hasTestTag(
                "${AddProfileScreenTestTags.COUNTRY_DROPDOWN_ITEM_PREFIX}${isoToCountryName[userTest.country]}"))

    composeTestRule
        .onNodeWithTag(
            "${AddProfileScreenTestTags.COUNTRY_DROPDOWN_ITEM_PREFIX}${isoToCountryName[userTest.country]}")
        .performClick()

    composeTestRule
        .onNodeWithTag(AddProfileScreenTestTags.DAY_FIELD)
        .performClick()
        .performTextInput("${userTest.dateOfBirth.dayOfMonth}")

    composeTestRule
        .onNodeWithTag(AddProfileScreenTestTags.MONTH_FIELD)
        .performClick()
        .performTextInput("${userTest.dateOfBirth.monthValue}")

    composeTestRule
        .onNodeWithTag(AddProfileScreenTestTags.YEAR_FIELD)
        .performClick()
        .performTextInput("${userTest.dateOfBirth.year}")

    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.SAVE_BUTTON).performClick()

    composeTestRule.waitUntil(5_000L) {
      composeTestRule.onNodeWithTag(SelectTagsScreenTestTags.SAVE_BUTTON).isDisplayed()
    }
  }

  private fun loginAndWait() {
    composeTestRule.waitUntil(5_000L) {
      composeTestRule.onNodeWithTag(SignInScreenTestTags.WELCOME_BOX).isDisplayed()
    }

    composeTestRule
        .onNodeWithTag(SignInScreenTestTags.JOIN_BUTTON)
        .assertIsDisplayed()
        .performClick()

    composeTestRule.waitForIdle()
    composeTestRule
        .onNodeWithTag(FormTestTags.EMAIL_FIELD)
        .assertIsDisplayed()
        .performClick()
        .performTextInput(FAKE_EMAIL)

    composeTestRule.waitForIdle()
    composeTestRule
        .onNodeWithTag(FlowBottomMenuTestTags.CONFIRM_BUTTON)
        .assertIsDisplayed()
        .performClick()

    composeTestRule.waitUntil(60_000L) {
      composeTestRule.onNodeWithTag(FlowBottomMenuTestTags.PASSWORD_BUTTON).isDisplayed()
    }
    composeTestRule.onNodeWithTag(FlowBottomMenuTestTags.PASSWORD_BUTTON).performClick()

    composeTestRule.waitForIdle()
    composeTestRule
        .onNodeWithTag(FormTestTags.PASSWORD_FIELD)
        .assertIsDisplayed()
        .performClick()
        .performTextInput(FAKE_PASSWORD)

    composeTestRule.waitForIdle()
    composeTestRule
        .onNodeWithTag(FlowBottomMenuTestTags.CONFIRM_BUTTON)
        .assertIsDisplayed()
        .performClick()

    // Wait max 30 second for the email validation to occur, we should arrive on the
    // AddProfileScreen
    composeTestRule.waitUntil(60_000L) {
      composeTestRule.onNodeWithTag(NavigationTestTags.ADD_PROFILE_SCREEN).isDisplayed()
    }
  }
}
