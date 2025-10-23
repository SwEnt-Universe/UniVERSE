package com.android.universe.signIn

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.ui.signIn.SignInScreen
import com.android.universe.ui.signIn.SignInScreenTestTags.SIGN_IN_BUTTON
import com.android.universe.ui.signIn.SignInScreenTestTags.SIGN_IN_LOGO
import com.android.universe.ui.signIn.SignInScreenTestTags.SIGN_IN_PROGRESS_BAR
import com.android.universe.ui.signIn.SignInScreenTestTags.SIGN_IN_TITLE
import com.android.universe.ui.signIn.SignInUIState
import com.android.universe.ui.signIn.SignInViewModel
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

  /**
   * Tests that the initial state of the screen is correct. It should display the sign-in button in
   * an enabled state.
   */
  @Test
  fun initialState_displays() {
    // Arrange: Set the initial state
    fakeUiState.value = SignInUIState(isLoading = false, errorMsg = null)

    // Act: Render the screen
    composeTestRule.setContent { SignInScreen(viewModel = mockViewModel) }

    // Assert: Check that the sign-in button is displayed and enabled
    composeTestRule.onNodeWithTag(SIGN_IN_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SIGN_IN_TITLE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SIGN_IN_LOGO).assertIsDisplayed()

    // Assert: Check that the progress indicator is not displayed
    composeTestRule.onNodeWithTag(SIGN_IN_PROGRESS_BAR).assertDoesNotExist()
  }

  /**
   * Tests that a progress indicator is shown and the button is disabled when the state is
   * `isLoading = true`.
   */
  @Test
  fun whenLoading_displaysProgressIndicator_andButtonNotDisplayed() {
    // Arrange: Set the state to loading
    fakeUiState.value = SignInUIState(isLoading = true)

    // Act: Render the screen
    composeTestRule.setContent { SignInScreen(viewModel = mockViewModel) }

    // Assert: Check that the progress indicator is displayed
    composeTestRule.onNodeWithTag(SIGN_IN_PROGRESS_BAR).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SIGN_IN_LOGO).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SIGN_IN_TITLE).assertIsDisplayed()

    // Assert: Check that the sign-in button is not displayed
    composeTestRule.onNodeWithTag(SIGN_IN_BUTTON).assertDoesNotExist()
  }

  /** Tests that clicking the sign-in button triggers the `signIn` function on the ViewModel. */
  @Test
  fun signInButton_onClick_callsViewModelSignIn() {
    // Arrange
    composeTestRule.setContent { SignInScreen(viewModel = mockViewModel) }

    // Act: Click the sign-in button
    composeTestRule.onNodeWithTag(SIGN_IN_BUTTON).performClick()

    // Assert: Verify that the signIn function was called on the ViewModel.
    // We use `any()` for context and credentialManager as we don't care about their
    // specific values in this UI test.
    verify(exactly = 1) {
      mockViewModel.signIn(
          context = any(), credentialManager = any(), onSuccess = any(), onFailure = any())
    }
  }

  @Test
  fun whenSignInSucceeds_onSignedInIsCalled() {
    // Arrange
    val mockOnSignedIn = mockk<() -> Unit>(relaxed = true)
    val mockUser = mockk<FirebaseUser>()

    composeTestRule.setContent {
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
    composeTestRule.setContent { SignInScreen(viewModel = mockViewModel) }

    // Act: Update the state to show an error message
    fakeUiState.value = fakeUiState.value.copy(errorMsg = "An error occurred")

    composeTestRule.waitForIdle()

    // Assert: Verify that the ViewModel's function to clear the error was called.
    // This happens in the LaunchedEffect.
    verify(exactly = 1) { mockViewModel.clearErrorMsg() }
  }
}
