package com.android.universe.model.authentication

import android.content.Context
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.ui.common.ValidationState
import com.android.universe.ui.signIn.OnboardingState
import com.android.universe.ui.signIn.SignInErrorMessage
import com.android.universe.ui.signIn.SignInMethod
import com.android.universe.ui.signIn.SignInViewModel
import com.android.universe.utils.MainCoroutineRule
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.SignInMethodQueryResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SignInViewModelTest {
  private val validEmail = "test@epfl.ch"
  private val invalidEmail = "not-an-email"
  private val validPassword = "Password123"
  private val invalidPassword = "123"

  // Mocks
  private lateinit var mockAuthModel: AuthModel
  private lateinit var mockCredentialManager: CredentialManager
  private lateinit var mockContext: Context
  private lateinit var mockFirebaseUser: FirebaseUser
  private lateinit var mockCredential: Credential
  private lateinit var mockGetCredentialResponse: GetCredentialResponse

  // ViewModel under test
  private lateinit var viewModel: SignInViewModel

  @get:Rule val mainCoroutineRule = MainCoroutineRule()

  @Before
  fun setUp() {
    // Sets the main dispatcher to a test dispatcher to control coroutine execution

    // Initialize mocks
    mockAuthModel = mockk(relaxed = true)
    mockCredentialManager = mockk()
    mockContext = mockk(relaxed = true)
    mockFirebaseUser = mockk()
    mockCredential = mockk()
    mockGetCredentialResponse = mockk()

    // Setup default mock behaviors
    every { mockContext.getString(any()) } returns "test_client_id"
    every { mockGetCredentialResponse.credential } returns mockCredential

    // Instantiate ViewModel
    viewModel = SignInViewModel(mockAuthModel, UnconfinedTestDispatcher())
  }

  /**
   * This test verifies the happy path for the `signIn` function. It checks that when both the
   * Credential Manager and the `AuthModel`'s `signInWithGoogle` methods complete successfully, the
   * UI state is updated correctly. The test ensures that the `isLoading` flag is reset to false,
   * the `user` object is set to the authenticated user, the `errorMsg` is null, and the `onSuccess`
   * callback is invoked as expected.
   */
  @Test
  fun `signIn when successful updates uiState correctly`() {
    // Arrange
    var onSuccessCalled = false
    coEvery { mockCredentialManager.getCredential(context = any(), request = any()) } returns
        mockGetCredentialResponse
    coEvery {
      mockAuthModel.signInWithGoogle(credential = any(), onSuccess = any(), onFailure = any())
    } coAnswers
        {
          // Simulate success by invoking the onSuccess callback
          val onSuccess = secondArg<(FirebaseUser) -> Unit>()
          onSuccess(mockFirebaseUser)
        }

    // Act
    runTest {
      viewModel.signIn(
          context = mockContext,
          credentialManager = mockCredentialManager,
          onSuccess = { onSuccessCalled = true })
    }

    // Assert
    val state = viewModel.uiState.value
    assertFalse(state.isLoading)
    assertEquals(mockFirebaseUser, state.user)
    assertNull(state.errorMsg)
    assertFalse(state.signedOut)
    assertTrue(onSuccessCalled)

    coVerify {
      mockAuthModel.signInWithGoogle(
          credential = mockCredential, onSuccess = any(), onFailure = any())
    }
  }

  /**
   * This test simulates a failure during the credential retrieval process where a
   * `GetCredentialException` is thrown. It verifies that the ViewModel correctly handles this error
   * by:
   * - Setting `isLoading` to false.
   * - Ensuring `user` is null and `signedOut` is true.
   * - Populating `errorMsg` with an appropriate error message.
   * - Invoking the `onFailure` callback.
   */
  @Test
  fun `signIn when getCredential fails with GetCredentialException updates uiState with error`() {
    // Arrange
    val exception = mockk<GetCredentialException>()
    every { exception.localizedMessage } returns "error"
    coEvery { mockCredentialManager.getCredential(context = any(), request = any()) } throws
        exception
    var onFailureCalled = false

    // Act
    runTest {
      viewModel.signIn(
          context = mockContext,
          credentialManager = mockCredentialManager,
          onFailure = { onFailureCalled = true })
    }

    // Assert
    val state = viewModel.uiState.value
    assertFalse(state.isLoading)
    assertNull(state.user)
    assertTrue(state.signedOut)
    assertNotNull(state.errorMsg)
    assertTrue(state.errorMsg!!.contains("Failed to get credentials"))
    assertTrue(onFailureCalled)
  }

  /**
   * This test case simulates the scenario where the user cancels the sign-in flow. This is
   * represented by the `CredentialManager` throwing a `GetCredentialCancellationException`. The
   * test verifies that the ViewModel handles this specific case gracefully by:
   * - Setting `isLoading` to false.
   * - Keeping the user state as signed out (`user` is null, `signedOut` is true).
   * - Displaying a user-friendly message "Sign in cancelled" in `errorMsg`.
   * - Triggering the `onFailure` callback.
   */
  @Test
  fun `signIn when user cancels updates uiState with cancellation message`() {
    // Arrange
    val exception = GetCredentialCancellationException()
    coEvery { mockCredentialManager.getCredential(context = any(), request = any()) } throws
        exception
    var onFailureCalled = false

    // Act
    runTest {
      viewModel.signIn(
          context = mockContext,
          credentialManager = mockCredentialManager,
          onFailure = { onFailureCalled = true })
    }

    // Assert
    val state = viewModel.uiState.value
    assertFalse(state.isLoading)
    assertNull(state.user)
    assertTrue(state.signedOut)
    assertEquals("Sign in cancelled", state.errorMsg)
    assertTrue(onFailureCalled)
  }

  /**
   * This test verifies how the ViewModel behaves when the `AuthModel`'s `signInWithGoogle` method
   * fails after the credential has been successfully retrieved. It ensures that the ViewModel
   * correctly updates the UI state to reflect the failure by:
   * - Setting `isLoading` to false.
   * - Keeping the user state as signed out (`user` is null, `signedOut` is true).
   * - Populating `errorMsg` with an error message.
   * - Invoking the `onFailure` callback.
   */
  @Test
  fun `signIn when authModel fails updates uiState with error`() {
    // Arrange
    val exception = Exception("Firebase auth failed")
    var onFailureCalled = false
    coEvery { mockCredentialManager.getCredential(context = any(), request = any()) } returns
        mockGetCredentialResponse
    coEvery {
      mockAuthModel.signInWithGoogle(credential = any(), onSuccess = any(), onFailure = any())
    } coAnswers
        {
          // Simulate failure
          val onFailure = thirdArg<(Exception) -> Unit>()
          onFailure(exception)
        }

    // Act
    runTest {
      viewModel.signIn(
          context = mockContext,
          credentialManager = mockCredentialManager,
          onFailure = { onFailureCalled = true })
    }

    // Assert
    val state = viewModel.uiState.value
    assertFalse(state.isLoading)
    assertNull(state.user)
    assertTrue(state.signedOut)
    assertNotNull(state.errorMsg)
    assertTrue(onFailureCalled)
  }

  /**
   * This test ensures that if a sign-in operation is already in progress (`isLoading` is true), a
   * subsequent call to `signIn` will not trigger a new sign-in flow. This prevents duplicate
   * network requests and potential race conditions.
   *
   * The test works by initiating a `signIn` call that suspends indefinitely, keeping the
   * `isLoading` state true. It then calls `signIn` a second time and verifies that the underlying
   * credential manager method (`getCredential`) was only invoked once.
   */
  @Test
  fun `signIn when already loading does not start new sign in`() {
    // Arrange
    // The first call to signIn will start a coroutine that suspends indefinitely,
    // leaving the state as isLoading = true.
    coEvery {
      mockCredentialManager.getCredential(context = any(), request = any()).credential
    } coAnswers { mockCredential }
    runTest {
      viewModel.signIn(context = mockContext, credentialManager = mockCredentialManager)
      advanceUntilIdle()
      // Act
      // Try to sign in again while the first one is in progress
      viewModel.signIn(context = mockContext, credentialManager = mockCredentialManager)
      advanceUntilIdle()
    }
    // Assert
    // Verify that getCredential was only called once for the first invocation.
    // The second call should have returned early due to the isLoading guard.
    coVerify(exactly = 1) { mockCredentialManager.getCredential(context = any(), request = any()) }
  }

  /**
   * This test verifies that the `clearErrorMsg` function correctly resets the `errorMsg` in the UI
   * state. It first simulates a failed sign-in attempt to ensure that an error message is present
   * in the state. Then, it calls `clearErrorMsg` and asserts that the `errorMsg` has been set back
   * to null, confirming that the error state can be cleared as expected. This is important for
   * allowing the user to dismiss error messages in the UI.
   */
  @Test
  fun `clearErrorMsg clears the error message in uiState`() {
    // Arrange: Manually set an error to test clearing it
    val mockException = mockk<GetCredentialException>()
    every { mockException.localizedMessage } returns "Initial error"
    val credentialManagerForError =
        mockk<CredentialManager> {
          coEvery { getCredential(context = any(), request = any()) } throws mockException
        }
    // This signIn call will fail and set the error message
    runTest {
      viewModel.signIn(context = mockContext, credentialManager = credentialManagerForError)
    }
    assertNotNull(viewModel.uiState.value.errorMsg)

    // Act
    viewModel.clearErrorMsg()

    // Assert
    assertNull(viewModel.uiState.value.errorMsg)
  }

  @Test
  fun `setEmail updates email and clears error when valid`() = runTest {
    // Act
    viewModel.setEmail(validEmail)
    // Assert
    val state = viewModel.uiState.value
    assertEquals(validEmail, state.email)
    assertTrue(state.emailErrorMsg is ValidationState.Valid)
  }

  @Test
  fun `setEmail sets error when invalid`() = runTest {
    // Act
    viewModel.setEmail(invalidEmail)
    // Assert
    val state = viewModel.uiState.value
    assertEquals(invalidEmail, state.email)
    assertTrue(state.emailErrorMsg is ValidationState.Invalid)
  }

  @Test
  fun `setPassword updates password and clears error when valid`() = runTest {
    viewModel.setPassword(validPassword)
    val state = viewModel.uiState.value
    assertEquals(validPassword, state.password)
    assertTrue(state.passwordErrorMsg is ValidationState.Valid)
  }

  @Test
  fun `setPassword sets error when invalid`() = runTest {
    viewModel.setPassword(invalidPassword)
    val state = viewModel.uiState.value
    assertEquals(invalidPassword, state.password)
    assertTrue(state.passwordErrorMsg is ValidationState.Invalid)
  }

  @Test
  fun `setEmail and setPassword ignored when loading`() = runTest {
    // Arrange: put ViewModel in loading state
    viewModel.nowLoading()
    val originalState = viewModel.uiState.value
    // Act
    viewModel.setEmail(validEmail)
    viewModel.setPassword(invalidPassword)
    // Assert: state unchanged
    assertEquals(originalState, viewModel.uiState.value)
  }

  @Test
  fun `signInWithEmail does nothing when loading`() = runTest {
    viewModel.nowLoading()
    viewModel.signInWithEmail()
    advanceUntilIdle()
    coVerify(exactly = 0) { mockAuthModel.signInWithEmail(any(), any()) }
  }

  @Test
  fun `signInWithEmail does nothing when signInEnabled is false`() = runTest {
    // Set invalid email and password
    viewModel.setEmail(invalidEmail)
    viewModel.setPassword(validPassword)

    viewModel.signInWithEmail()
    advanceUntilIdle()

    coVerify(exactly = 0) { mockAuthModel.signInWithEmail(any(), any()) }
  }

  @Test
  fun `signInWithEmail calls authModel when enabled`() = runTest {
    viewModel.setEmail(validEmail)
    viewModel.setPassword(validPassword)

    coEvery { mockAuthModel.signInWithEmail(validEmail, validPassword) } returns
        Result.success(mockFirebaseUser)

    viewModel.signInWithEmail()
    advanceUntilIdle()

    coVerify(exactly = 1) { mockAuthModel.signInWithEmail(validEmail, validPassword) }
  }

  @Test
  fun `signInWithEmail updates state on success`() = runTest {
    viewModel.setEmail(validEmail)
    viewModel.setPassword(validPassword)

    coEvery { mockAuthModel.signInWithEmail(validEmail, validPassword) } returns
        Result.success(mockFirebaseUser)

    viewModel.signInWithEmail()
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertFalse(state.isLoading)
    assertEquals(mockFirebaseUser, state.user)
    assertTrue(state.emailErrorMsg is ValidationState.Valid)
    assertTrue(state.passwordErrorMsg is ValidationState.Valid)
  }

  @Test
  fun `signInWithEmail updates state on weak password`() = runTest {
    val exception =
        FirebaseAuthWeakPasswordException("weak-password", "Password too weak", "Password too weak")

    viewModel.setEmail(validEmail)
    viewModel.setPassword(validPassword)

    coEvery { mockAuthModel.signInWithEmail(validEmail, validPassword) } returns
        Result.failure(exception)

    viewModel.signInWithEmail()
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertFalse(state.isLoading)
    print(state.errorMsg)
    assertEquals("Password too weak", state.errorMsg)
  }

  @Test
  fun `signInWithEmail updates state on invalid credentials`() = runTest {
    val exception =
        FirebaseAuthInvalidCredentialsException("invalid-credential", "Invalid password")

    viewModel.setEmail(validEmail)
    viewModel.setPassword(validPassword)

    coEvery { mockAuthModel.signInWithEmail(validEmail, validPassword) } returns
        Result.failure(exception)

    viewModel.signInWithEmail()
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertFalse(state.isLoading)
    assertEquals("Invalid password", state.errorMsg)
  }

  @Test
  fun `signInWithEmail updates state on invalid email`() = runTest {
    val exception = InvalidEmailException("Invalid email format")

    viewModel.setEmail(validEmail)
    viewModel.setPassword(validPassword)

    coEvery { mockAuthModel.signInWithEmail(validEmail, validPassword) } returns
        Result.failure(exception)

    viewModel.signInWithEmail()
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertFalse(state.isLoading)
    assertTrue(state.emailErrorMsg is ValidationState.Invalid)
    assertEquals(
        "Invalid email format", (state.emailErrorMsg as ValidationState.Invalid).errorMessage)
  }

  @Test
  fun `signInWithEmail updates state on network failure`() = runTest {
    val exception = FirebaseNetworkException("No network")

    viewModel.setEmail(validEmail)
    viewModel.setPassword(validPassword)

    coEvery { mockAuthModel.signInWithEmail(validEmail, validPassword) } returns
        Result.failure(exception)

    viewModel.signInWithEmail()
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertFalse(state.isLoading)
    assertEquals("No internet connection", state.errorMsg)
  }

  @Test
  fun `signInWithEmail updates state on unknown exception`() = runTest {
    val exception = RuntimeException("Something went wrong")

    viewModel.setEmail(validEmail)
    viewModel.setPassword(validPassword)

    coEvery { mockAuthModel.signInWithEmail(validEmail, validPassword) } returns
        Result.failure(exception)

    viewModel.signInWithEmail()
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertFalse(state.isLoading)
    assertEquals(SIGN_IN_FAILED_EXCEPTION_MESSAGE, state.errorMsg)
  }

  @Test
  fun `viewModel updates onBoardingState`() = runTest {
    assertTrue(viewModel.uiState.value.onboardingState == OnboardingState.WELCOME)

    viewModel.onJoinUniverse()
    advanceUntilIdle()
    assertTrue(viewModel.uiState.value.onboardingState == OnboardingState.ENTER_EMAIL)

    viewModel.onBack()
    advanceUntilIdle()
    assertTrue(viewModel.uiState.value.onboardingState == OnboardingState.WELCOME)

    viewModel.onSignUpWithPassword()
    advanceUntilIdle()
    assertTrue(viewModel.uiState.value.onboardingState == OnboardingState.SIGN_IN_PASSWORD)

    viewModel.onBack()
    advanceUntilIdle()
    assertTrue(viewModel.uiState.value.onboardingState == OnboardingState.ENTER_EMAIL)
  }

  @Test
  fun `viewModel signInMethods is null`() = runTest {
    val mockQueryResponse: SignInMethodQueryResult = mockk(relaxed = true)
    coEvery { mockAuthModel.fetchSignInMethodsForEmail(validEmail) } returns mockQueryResponse
    coEvery { mockQueryResponse.signInMethods } returns null

    assertTrue(viewModel.uiState.value.onboardingState == OnboardingState.WELCOME)
    viewModel.onJoinUniverse()
    advanceUntilIdle()

    assertTrue(viewModel.uiState.value.onboardingState == OnboardingState.ENTER_EMAIL)
    viewModel.setEmail(validEmail)
    advanceUntilIdle()

    assertTrue(viewModel.uiState.value.confirmEmailEnabled)
    viewModel.confirmEmail()
    advanceUntilIdle()
    assertTrue(viewModel.uiState.value.onboardingState == OnboardingState.CHOOSE_AUTH_METHOD)
  }

  @Test
  fun `viewModel signInMethods throws error`() = runTest {
    coEvery { mockAuthModel.fetchSignInMethodsForEmail(validEmail) } throws
        FirebaseNetworkException("test")

    assertTrue(viewModel.uiState.value.onboardingState == OnboardingState.WELCOME)
    viewModel.onJoinUniverse()
    advanceUntilIdle()

    assertTrue(viewModel.uiState.value.onboardingState == OnboardingState.ENTER_EMAIL)
    viewModel.setEmail(validEmail)
    advanceUntilIdle()

    assertTrue(viewModel.uiState.value.confirmEmailEnabled)
    viewModel.confirmEmail()
    advanceUntilIdle()
    assertTrue(viewModel.uiState.value.errorMsg == SignInErrorMessage.NO_INTERNET)
  }

  @Test
  fun `viewModel signInMethods is Google and Email`() = runTest {
    val mockQueryResponse: SignInMethodQueryResult = mockk(relaxed = true)
    coEvery { mockAuthModel.fetchSignInMethodsForEmail(validEmail) } returns mockQueryResponse
    coEvery { mockQueryResponse.signInMethods } returns
        listOf(SignInMethod.GOOGLE, SignInMethod.EMAIL)

    assertTrue(viewModel.uiState.value.onboardingState == OnboardingState.WELCOME)
    viewModel.onJoinUniverse()
    advanceUntilIdle()

    assertTrue(viewModel.uiState.value.onboardingState == OnboardingState.ENTER_EMAIL)
    viewModel.setEmail(validEmail)
    advanceUntilIdle()

    assertTrue(viewModel.uiState.value.confirmEmailEnabled)
    viewModel.confirmEmail()
    advanceUntilIdle()
    assertTrue(viewModel.uiState.value.onboardingState == OnboardingState.CHOOSE_AUTH_METHOD)
  }

  @Test
  fun `viewModel signInMethods is Google`() = runTest {
    val mockQueryResponse: SignInMethodQueryResult = mockk(relaxed = true)
    coEvery { mockAuthModel.fetchSignInMethodsForEmail(validEmail) } returns mockQueryResponse
    coEvery { mockQueryResponse.signInMethods } returns listOf(SignInMethod.GOOGLE)

    assertTrue(viewModel.uiState.value.onboardingState == OnboardingState.WELCOME)
    viewModel.onJoinUniverse()
    advanceUntilIdle()

    assertTrue(viewModel.uiState.value.onboardingState == OnboardingState.ENTER_EMAIL)
    viewModel.setEmail(validEmail)
    advanceUntilIdle()

    assertTrue(viewModel.uiState.value.confirmEmailEnabled)
    viewModel.confirmEmail()
    advanceUntilIdle()
    assertTrue(viewModel.uiState.value.onboardingState == OnboardingState.SIGN_IN_GOOGLE)
  }

  @Test
  fun `viewModel signInMethods is Email`() = runTest {
    val mockQueryResponse: SignInMethodQueryResult = mockk(relaxed = true)
    coEvery { mockAuthModel.fetchSignInMethodsForEmail(validEmail) } returns mockQueryResponse
    coEvery { mockQueryResponse.signInMethods } returns listOf(SignInMethod.EMAIL)

    assertTrue(viewModel.uiState.value.onboardingState == OnboardingState.WELCOME)
    viewModel.onJoinUniverse()
    advanceUntilIdle()

    assertTrue(viewModel.uiState.value.onboardingState == OnboardingState.ENTER_EMAIL)
    viewModel.setEmail(validEmail)
    advanceUntilIdle()

    assertTrue(viewModel.uiState.value.confirmEmailEnabled)
    viewModel.confirmEmail()
    advanceUntilIdle()
    assertTrue(viewModel.uiState.value.onboardingState == OnboardingState.SIGN_IN_PASSWORD)
  }
}
