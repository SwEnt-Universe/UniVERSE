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
          val onSuccess = arg<(FirebaseUser) -> Unit>(1)
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
          val onFailure = arg<(Exception) -> Unit>(2)
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
