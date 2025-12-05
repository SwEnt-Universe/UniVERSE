package com.android.universe.ui.profileCreation

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.di.DispatcherProvider
import com.android.universe.model.image.ImageBitmapManager
import com.android.universe.model.user.FakeUserRepository
import com.android.universe.ui.navigation.FlowBottomMenuTestTags
import com.android.universe.ui.theme.UniverseTheme
import com.android.universe.utils.FirestoreUserTest
import com.android.universe.utils.setContentWithStubBackdrop
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class AddProfileScreenTest : FirestoreUserTest() {
  @get:Rule val composeTestRule = createComposeRule()
  val uid = "AddProfileScreenTest"
  private var onBackSpy: () -> Unit = {}
  private lateinit var viewModel: AddProfileViewModel

  @Before
  override fun setUp() {
    super.setUp()

    val context = ApplicationProvider.getApplicationContext<Context>()
    val imageManager = ImageBitmapManager(context)
    val testDispatcher = UnconfinedTestDispatcher()
    val dispatcherProvider =
        object : DispatcherProvider {
          override val main: CoroutineDispatcher = testDispatcher
          override val default: CoroutineDispatcher = testDispatcher
          override val io: CoroutineDispatcher = testDispatcher
          override val unconfined: CoroutineDispatcher = testDispatcher
        }

    viewModel =
        AddProfileViewModel(
            repository = FakeUserRepository(),
            imageManager = imageManager,
            dispatcherProvider = dispatcherProvider)

    composeTestRule.setContentWithStubBackdrop {
      UniverseTheme {
        AddProfile(uid = uid, viewModel = viewModel, navigateOnSave = {}, onBack = { onBackSpy() })
      }
    }
  }

  @Test
  fun displayAllComponents() {
    // Username
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.USERNAME_FIELD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.USERNAME_ERROR).assertIsNotDisplayed()

    // First name
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.FIRST_NAME_FIELD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.FIRST_NAME_ERROR).assertIsNotDisplayed()

    // Last name
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.LAST_NAME_FIELD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.LAST_NAME_ERROR).assertIsNotDisplayed()

    // Description
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.DESCRIPTION_FIELD).assertIsDisplayed()

    // Date of birth
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.DATE_OF_BIRTH_TEXT).assertIsDisplayed()

    // Save button
    composeTestRule.onNodeWithTag(FlowBottomMenuTestTags.CONFIRM_BUTTON).assertIsDisplayed()
  }

  @Test
  fun canEnterUsername() {
    val text = "john_doe"

    // Enter text in the field
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.USERNAME_FIELD).performTextInput(text)

    // Assert that the field contains the entered text
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.USERNAME_FIELD).assertTextContains(text)

    // Assert that the error message is not displayed
    composeTestRule
        .onNodeWithTag(AddProfileScreenTestTags.USERNAME_ERROR, useUnmergedTree = true)
        .assertIsNotDisplayed()
  }

  @Test
  fun canEnterFirstName() {
    val text = "John"

    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.FIRST_NAME_FIELD).performTextInput(text)

    // Assert that the field contains the entered text
    composeTestRule
        .onNodeWithTag(AddProfileScreenTestTags.FIRST_NAME_FIELD)
        .assertTextContains(text)

    // Assert that the error message is not displayed
    composeTestRule
        .onNodeWithTag(AddProfileScreenTestTags.FIRST_NAME_ERROR, useUnmergedTree = true)
        .assertIsNotDisplayed()
  }

  @Test
  fun canEnterLastName() {
    val text = "Doe"

    // Enter text in the field
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.LAST_NAME_FIELD).performTextInput(text)

    // Assert that the field contains the entered text
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.LAST_NAME_FIELD).assertTextContains(text)

    // Assert that the error message is not displayed
    composeTestRule
        .onNodeWithTag(AddProfileScreenTestTags.LAST_NAME_ERROR, useUnmergedTree = true)
        .assertIsNotDisplayed()
  }

  @Test
  fun canEnterDescription() {
    val text = "I'm a default person"

    // Enter text in the field
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.DESCRIPTION_FIELD).performTextInput(text)

    // Assert that the field contains the entered text
    composeTestRule
        .onNodeWithTag(AddProfileScreenTestTags.DESCRIPTION_FIELD)
        .assertTextContains(text)
  }

  @Test
  fun backButtonInvokesOnBackCallback() {
    var wasCalled = false
    onBackSpy = { wasCalled = true }

    composeTestRule.onNodeWithTag(FlowBottomMenuTestTags.BACK_BUTTON).performClick()

    assertTrue(wasCalled)
  }
}
