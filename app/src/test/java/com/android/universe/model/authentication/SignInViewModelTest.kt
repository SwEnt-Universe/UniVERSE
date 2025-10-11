package com.android.universe.model.authentication

import android.content.Context
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.android.universe.ui.signIn.SignInViewModel
import com.google.firebase.auth.FirebaseUser
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class SignInViewModelTest {

  // Mocks
  private lateinit var mockAuthModel: AuthModel
  private lateinit var mockCredentialManager: CredentialManager
  private lateinit var mockContext: Context
  private lateinit var mockFirebaseUser: FirebaseUser
  private lateinit var mockCredential: Credential
  private lateinit var mockGetCredentialResponse: GetCredentialResponse

  // ViewModel under test
  private lateinit var viewModel: SignInViewModel

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
    viewModel = SignInViewModel(mockAuthModel)
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

      // Act
      // Try to sign in again while the first one is in progress
      viewModel.signIn(context = mockContext, credentialManager = mockCredentialManager)
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
}
