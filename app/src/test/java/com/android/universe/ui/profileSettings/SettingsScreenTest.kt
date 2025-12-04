package com.android.universe.ui.profileSettings

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.model.image.ImageBitmapManager
import com.android.universe.model.user.FakeUserRepository
import com.android.universe.ui.common.GeneralDatePopUpTestTags
import com.android.universe.ui.common.LogoutTestTags
import com.android.universe.ui.navigation.FlowBottomMenuTestTags
import com.android.universe.ui.theme.UniverseTheme
import com.android.universe.utils.UserTestData
import com.android.universe.utils.selectDay
import com.android.universe.utils.setContentWithStubBackdrop
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import java.time.LocalDate
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {
  companion object {
    val user = UserTestData.Alice
    val newUsername = "newusername"
    val sampleDate =
        LocalDate.of(user.dateOfBirth.year, user.dateOfBirth.month, user.dateOfBirth.dayOfMonth + 1)
  }

  private lateinit var viewmodel: SettingsViewModel
  private lateinit var mockFirebaseUser: FirebaseUser
  private lateinit var mockEmailTask: Task<Void>
  private lateinit var mockPasswordTask: Task<Void>

  private fun resolveTypeButton(type: ModalType): String {
    return when (type) {
      ModalType.EMAIL -> SettingsTestTags.EMAIL_BUTTON
      ModalType.PASSWORD -> SettingsTestTags.PASSWORD_BUTTON
      ModalType.USERNAME -> SettingsTestTags.USERNAME_BUTTON
      ModalType.FIRSTNAME -> SettingsTestTags.FIRST_NAME_BUTTON
      ModalType.LASTNAME -> SettingsTestTags.LAST_NAME_BUTTON
      ModalType.DESCRIPTION -> SettingsTestTags.DESCRIPTION_BUTTON
    }
  }

  @get:Rule val composeTestRule = createComposeRule()

  fun screenSetup(
      onBack: () -> Unit = {},
      onConfirm: () -> Unit = {},
      onLogout: () -> Unit = {},
      onAddTag: () -> Unit = {},
      clear: suspend () -> Unit = {}
  ) {
    composeTestRule.setContentWithStubBackdrop {
      UniverseTheme {
        SettingsScreen(user.uid, onBack, onConfirm, viewmodel, onLogout, onAddTag, clear)
      }
    }
  }

  @Before
  fun setUp() {
    mockkStatic(FirebaseAuth::class)
    val fakeAuth = mockk<FirebaseAuth>(relaxed = true)
    every { FirebaseAuth.getInstance() } returns fakeAuth
    every { fakeAuth.currentUser } returns null

    mockkStatic(FirebaseFirestore::class)
    every { FirebaseFirestore.getInstance() } returns mockk(relaxed = true)
    mockFirebaseUser = mockk()
    mockEmailTask = mockk(relaxed = true)
    mockPasswordTask = mockk(relaxed = true)

    every { mockFirebaseUser.email } returns "old@epfl.ch"
    every { mockFirebaseUser.updateEmail(any()) } returns mockEmailTask
    every { mockFirebaseUser.updatePassword(any()) } returns mockPasswordTask

    val repository = FakeUserRepository()

    val context = ApplicationProvider.getApplicationContext<Context>()
    val imageManager = ImageBitmapManager(context)

    runTest {
      repository.addUser(user)
      viewmodel =
          SettingsViewModel(
              uid = user.uid, userRepository = repository, imageManager = imageManager)
    }
  }

  @Test
  fun mainDisplayed() {
    screenSetup()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(SettingsTestTags.MODAL_POPUP).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(SettingsTestTags.PICTURE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SettingsTestTags.LIQUID_BOX_CONTENT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SettingsTestTags.EMAIL_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SettingsTestTags.PASSWORD_BUTTON).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(SettingsTestTags.USERNAME_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SettingsTestTags.FIRST_NAME_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SettingsTestTags.LAST_NAME_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SettingsTestTags.DESCRIPTION_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SettingsTestTags.DATE_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SettingsTestTags.MODAL_TITLE).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(FlowBottomMenuTestTags.CONFIRM_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(FlowBottomMenuTestTags.BACK_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(FlowBottomMenuTestTags.LOGOUT_BUTTON).assertIsDisplayed()
  }

  @Test
  fun modalDisplayed() {
    screenSetup()
    for (type in ModalType.entries) {
      if (type == ModalType.PASSWORD) continue
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag(resolveTypeButton(type)).performClick()
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag(SettingsTestTags.MODAL_POPUP).assertIsDisplayed()
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag(SettingsTestTags.MODAL_TITLE).assertIsDisplayed()
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag(SettingsTestTags.MODAL_CANCEL_BUTTON).performClick()
    }
  }

  @Test
  fun canSelectDate() {
    screenSetup()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(SettingsTestTags.DATE_BUTTON).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(SettingsTestTags.DATE_DIALOG).assertIsDisplayed()
    selectDay(composeTestRule, sampleDate)
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(GeneralDatePopUpTestTags.CONFIRM_BUTTON).performClick()
    composeTestRule.waitForIdle()
    assertEquals(sampleDate, viewmodel.uiState.value.date)
    composeTestRule
        .onNodeWithTag(SettingsTestTags.DATE_TEXT)
        .assertTextEquals(viewmodel.formatter.format(sampleDate))
  }

  @Test
  fun callBacksAreCalled() {
    var cleared = false
    var navigated = false
    var added = false
    var onBack = false
    var onConfirm = false
    screenSetup(
        onBack = { onBack = true },
        onConfirm = { onConfirm = true },
        onLogout = { navigated = true },
        onAddTag = { added = true },
        clear = suspend { cleared = true })

    composeTestRule.onNodeWithTag(FlowBottomMenuTestTags.LOGOUT_BUTTON).performClick()
    composeTestRule.onNodeWithTag(LogoutTestTags.ALERT_CONFIRM_BUTTON).performClick()
    assertEquals(true, cleared)
    assertEquals(true, navigated)
    composeTestRule.onNodeWithTag(SettingsTestTags.TAG_BUTTON).performClick()
    assertEquals(true, added)
    composeTestRule.onNodeWithTag(FlowBottomMenuTestTags.BACK_BUTTON).performClick()
    assertEquals(true, onBack)
    composeTestRule.onNodeWithTag(FlowBottomMenuTestTags.CONFIRM_BUTTON).performClick()
    composeTestRule.waitForIdle()
    assertEquals(true, onConfirm)
  }

  @Test
  fun modalTextInput() {
    screenSetup()
    composeTestRule.onNodeWithTag(SettingsTestTags.USERNAME_BUTTON).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(SettingsTestTags.CUSTOMFIELD).performTextClearance()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(SettingsTestTags.CUSTOMFIELD).performTextInput(newUsername)
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(SettingsTestTags.MODAL_CANCEL_BUTTON).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(SettingsTestTags.USERNAME_TEXT).assertTextEquals(user.username)
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(SettingsTestTags.USERNAME_BUTTON).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(SettingsTestTags.CUSTOMFIELD).performTextClearance()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(SettingsTestTags.CUSTOMFIELD).performTextInput(newUsername)
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(SettingsTestTags.MODAL_SAVE_BUTTON).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(SettingsTestTags.USERNAME_TEXT).assertTextEquals(newUsername)
  }
}
