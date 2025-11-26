package com.android.universe.signIn

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.ui.common.FormTestTags
import com.android.universe.ui.navigation.FlowBottomMenuTestTags
import com.android.universe.ui.signIn.OnboardingState
import com.android.universe.ui.signIn.SignInScreen
import com.android.universe.ui.signIn.SignInScreenTestTags
import com.android.universe.ui.signIn.SignInUIState
import com.android.universe.ui.signIn.SignInViewModel
import com.android.universe.utils.setContentWithStubBackdrop
import com.google.firebase.auth.FirebaseUser
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SignInScreenTest {
  private val validEmail = "test@epfl.ch"
  private val invalidEmail = "not-an-email"
  private val validPassword = "Password123"
  private val invalidPassword = "123"

  @get:Rule val composeTestRule = createComposeRule()

  // Mock the ViewModel to control the UI state
  private lateinit var mockViewModel: SignInViewModel
  private lateinit var fakeUiState: MutableStateFlow<SignInUIState>

  @Before
  fun setUp() {
    // Initialize the fake state flow that we can control in our tests
    fakeUiState = MutableStateFlow(SignInUIState())

    // Create a mock ViewModel
    mockViewModel =
        mockk(relaxed = true) {
          // Tell the mock to return our controllable state flow
          every { uiState } returns fakeUiState
        }
  }

  @Test
  fun whenSignInSucceeds_onSignedInIsCalled() {
    // Arrange
    val mockOnSignedIn = mockk<() -> Unit>(relaxed = true)
    val mockUser = mockk<FirebaseUser>()

    composeTestRule.setContentWithStubBackdrop {
      SignInScreen(viewModel = mockViewModel, onSignedIn = mockOnSignedIn)
    }

    // Act: Update the state to simulate a successful sign-in
    fakeUiState.value = fakeUiState.value.copy(user = mockUser)

    composeTestRule.waitForIdle()

    // Assert: Verify that the onSignedIn callback was invoked
    verify(exactly = 1) { mockOnSignedIn() }
  }

  @Test
  fun whenErrorOccurs_clearErrorMsgIsCalled() {
    // Arrange
    composeTestRule.setContentWithStubBackdrop { SignInScreen(viewModel = mockViewModel) }

    // Act: Update the state to show an error message
    fakeUiState.value = fakeUiState.value.copy(errorMsg = "An error occurred")

    composeTestRule.waitForIdle()

    // Assert: Verify that the ViewModel's function to clear the error was called.
    // This happens in the LaunchedEffect.
    verify(exactly = 1) { mockViewModel.clearErrorMsg() }
  }

  /**
   * -------------------------------
   * WELCOME SCREEN
   * -------------------------------
   */
  @Test
  fun welcomeScreen_displayAndJoin() {
    fakeUiState.value = SignInUIState(onboardingState = OnboardingState.WELCOME)

    composeTestRule.setContentWithStubBackdrop { SignInScreen(viewModel = mockViewModel) }

    composeTestRule.onNodeWithTag(SignInScreenTestTags.WELCOME_BOX).assertIsDisplayed()

    composeTestRule
        .onNodeWithTag(SignInScreenTestTags.JOIN_BUTTON)
        .assertIsDisplayed()
        .assertIsEnabled()
        .performClick()

    verify(exactly = 1) { mockViewModel.onJoinUniverse() }
  }

  /**
   * -------------------------------
   * EMAIL ENTRY SCREEN
   * -------------------------------
   */
  @Test
  fun emailEntryScreen_displayAndConfirm() {
    fakeUiState.value =
        SignInUIState(
            onboardingState = OnboardingState.ENTER_EMAIL,
            email = validEmail,
        )

    composeTestRule.setContentWithStubBackdrop { SignInScreen(viewModel = mockViewModel) }

    composeTestRule.onNodeWithTag(SignInScreenTestTags.ENTER_EMAIL_BOX).assertIsDisplayed()
    composeTestRule.onNodeWithTag(FormTestTags.EMAIL_FIELD).assertIsDisplayed().assertIsEnabled()

    composeTestRule
        .onNodeWithTag(FlowBottomMenuTestTags.CONFIRM_BUTTON)
        .assertIsDisplayed()
        .performClick()

    composeTestRule
        .onNodeWithTag(FlowBottomMenuTestTags.BACK_BUTTON)
        .assertIsDisplayed()
        .performClick()

    composeTestRule.waitForIdle()
    verify(exactly = 1) { mockViewModel.confirmEmail() }
    verify(exactly = 1) { mockViewModel.onBack() }
  }

  /**
   * -------------------------------
   * PASSWORD SCREEN
   * -------------------------------
   */
  @Test
  fun passwordScreen_displayAndSignIn() {
    fakeUiState.value =
        SignInUIState(
            onboardingState = OnboardingState.SIGN_IN_PASSWORD,
            email = validEmail,
            password = validPassword,
        )

    composeTestRule.setContentWithStubBackdrop { SignInScreen(viewModel = mockViewModel) }

    composeTestRule.onNodeWithTag(SignInScreenTestTags.PASSWORD_BOX).assertIsDisplayed()

    composeTestRule.onNodeWithTag(FormTestTags.EMAIL_FIELD).assertIsDisplayed().assertIsNotEnabled()
    composeTestRule.onNodeWithTag(FormTestTags.PASSWORD_FIELD).assertIsDisplayed().assertIsEnabled()

    composeTestRule
        .onNodeWithTag(FlowBottomMenuTestTags.CONFIRM_BUTTON)
        .assertIsDisplayed()
        .performClick()

    composeTestRule
        .onNodeWithTag(FlowBottomMenuTestTags.BACK_BUTTON)
        .assertIsDisplayed()
        .performClick()

    composeTestRule.waitForIdle()
    verify(exactly = 1) { mockViewModel.signInWithEmail() }
    verify(exactly = 1) { mockViewModel.onBack() }
  }

  /**
   * -------------------------------
   * SIGN UP SCREEN
   * -------------------------------
   */
  @Test
  fun signUpScreen_displayAndSignInMethods() {
    fakeUiState.value = SignInUIState(onboardingState = OnboardingState.SIGN_UP, email = validEmail)

    composeTestRule.setContentWithStubBackdrop { SignInScreen(viewModel = mockViewModel) }

    composeTestRule.onNodeWithTag(SignInScreenTestTags.SIGN_IN_BOX).assertIsDisplayed()

    // Google button click
    composeTestRule
        .onNodeWithTag(FlowBottomMenuTestTags.GOOGLE_BUTTON)
        .assertIsDisplayed()
        .performClick()

    // Password button click
    composeTestRule
        .onNodeWithTag(FlowBottomMenuTestTags.PASSWORD_BUTTON)
        .assertIsDisplayed()
        .performClick()

    composeTestRule
        .onNodeWithTag(FlowBottomMenuTestTags.BACK_BUTTON)
        .assertIsDisplayed()
        .performClick()

    composeTestRule.waitForIdle()
    verify(exactly = 1) { mockViewModel.onBack() }
    verify(exactly = 1) { mockViewModel.signIn(any(), any()) }
    verify(exactly = 1) { mockViewModel.onSignUpWithPassword() }
  }
}
