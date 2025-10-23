package com.android.universe.model.authentication

import android.content.Context
import androidx.core.os.bundleOf
import androidx.credentials.Credential
import androidx.test.core.app.ApplicationProvider
import com.android.universe.utils.FakeJwtGenerator
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

private lateinit var mockAuth: FirebaseAuth
private lateinit var mockHelper: GoogleSignInHelper
private lateinit var mockCredential: Credential
private lateinit var mockGoogleIdTokenCredential: GoogleIdTokenCredential
private lateinit var mockFirebaseUser: FirebaseUser
private lateinit var authModelFirebase: AuthModelFirebase
private lateinit var mockAuthCredential: AuthCredential
private lateinit var mockAuthResult: AuthResult

@RunWith(RobolectricTestRunner::class)
class AuthModelFirebaseTest {

  companion object {
    private val notFromEpfl = "hacker@gmail.com"
    private val fromEpfl = "test@epfl.ch"
    private val validToken = FakeJwtGenerator.createFakeGoogleIdToken("test", fromEpfl)
    private val invalidToken = "invalidToken"
  }

  private fun setupSignInMock(type: String, email: String? = null, idToken: String? = null) {
    // Mock for type check
    every { mockCredential.type } returns type
    email ?: return

    // Mock for email check
    // Here the bundleOf() is just a placeHolder such that credential.data does not throw a
    // MockKException
    every { mockCredential.data } returns bundleOf()
    every { mockHelper.extractIdTokenCredential(any()) } returns mockGoogleIdTokenCredential
    every { mockGoogleIdTokenCredential.id } returns email

    // Mock for sign-in with firebase
    idToken ?: return
    every { mockGoogleIdTokenCredential.idToken } returns idToken
    every { mockHelper.toFirebaseCredential(any()) } returns mockAuthCredential

    // This mimics the behavior of Firebase's signInWithCredential() if the credential is malformed
    if (idToken == invalidToken)
        coEvery { mockHelper.signInWithFirebase(any(), any()) } throws Exception()
    else coEvery { mockHelper.signInWithFirebase(any(), any()) } returns mockAuthResult

    every { mockAuthResult.user } returns mockFirebaseUser
  }

  @Before
  fun setUp() {
    mockAuth = mockk()
    mockHelper = mockk()
    mockCredential = mockk()
    mockGoogleIdTokenCredential = mockk()
    mockFirebaseUser = mockk()
    mockAuthCredential = mockk()
    mockAuthResult = mockk()

    authModelFirebase = AuthModelFirebase(mockAuth, mockHelper)
  }

  @Test
  fun google_sign_in_is_configured() {
    val context = ApplicationProvider.getApplicationContext<Context>()

    val resourceId =
        context.resources.getIdentifier("default_web_client_id", "string", context.packageName)

    // Skip test if resource doesn't exist (useful for CI environments)
    assumeTrue("Google Sign-In not configured - skipping test", resourceId != 0)

    val clientId = context.getString(resourceId)
    assertTrue(
        "Invalid Google client ID format: $clientId", clientId.endsWith(".googleusercontent.com"))
  }

  @Test
  fun `test signInWithGoogle badTokenType`() {
    setupSignInMock("", null)
    var exception: Exception? = null
    var successCalled = false
    runTest {
      authModelFirebase.signInWithGoogle(
          credential = mockCredential,
          onSuccess = { successCalled = true },
          onFailure = { e -> exception = e })

      assertFalse(successCalled)
      assertNotNull(exception)
      assertTrue(exception is IllegalStateException)
      assertTrue(
          (exception as IllegalStateException).message!! ==
              "Credential type not supported: ${mockCredential.type}")
    }
  }

  @Test
  fun `test signInWithGoogle notFromEpfl`() {
    setupSignInMock(TYPE_GOOGLE_ID_TOKEN_CREDENTIAL, notFromEpfl)

    var exception: Exception? = null
    var successCalled = false
    runTest {
      authModelFirebase.signInWithGoogle(
          credential = mockCredential,
          onSuccess = { successCalled = true },
          onFailure = { e -> exception = e })

      assertNotNull(exception)
      assertFalse(successCalled)
      assertTrue(exception is IllegalStateException)
      assertTrue(
          (exception as IllegalStateException).message!! ==
              "Email address is not from EPFL: $notFromEpfl")
    }
  }

  @Test
  fun `test signInWithGoogle fromEPFL`() {
    setupSignInMock(TYPE_GOOGLE_ID_TOKEN_CREDENTIAL, fromEpfl, "")

    var exception: Exception? = null
    var successCalled = false
    runTest {
      // Act
      authModelFirebase.signInWithGoogle(
          credential = mockCredential,
          onSuccess = { successCalled = true },
          onFailure = { e -> exception = e })
      assertNull(exception)
      assertTrue(successCalled)
    }
  }

  @Test
  fun `test signInWithGoogle invalidToken`() {
    setupSignInMock(TYPE_GOOGLE_ID_TOKEN_CREDENTIAL, fromEpfl, invalidToken)
    var exception: Exception? = null
    var successCalled = false
    runTest {
      authModelFirebase.signInWithGoogle(
          credential = mockCredential,
          onSuccess = { successCalled = true },
          onFailure = { e -> exception = e })

      assertNotNull(exception)
      assertFalse(exception is IllegalStateException)
      assertFalse(successCalled)
    }
  }

  @Test
  fun `test signInWithGoogle validToken`() {
    setupSignInMock(TYPE_GOOGLE_ID_TOKEN_CREDENTIAL, fromEpfl, validToken)
    every { mockGoogleIdTokenCredential.idToken } returns validToken
    var exception: Exception? = null
    var successCalled = false
    runTest {

      // Act
      authModelFirebase.signInWithGoogle(
          credential = mockCredential,
          onSuccess = { successCalled = true },
          onFailure = { e -> exception = e })

      assertNull(exception)
      assertTrue(successCalled)
    }
  }

  @Test
  fun `signInWithGoogle whenFirebaseReturnsNullUser callsOnFailure`() {
    // Arrange: Set up mocks to return a null user
    setupSignInMock(TYPE_GOOGLE_ID_TOKEN_CREDENTIAL, fromEpfl, validToken)
    every { mockAuthResult.user } returns null // Override the user to be null

    var successCalled = false
    var capturedException: Exception? = null

    // Act
    runTest {
      authModelFirebase.signInWithGoogle(
          credential = mockCredential,
          onSuccess = { successCalled = true },
          onFailure = { e -> capturedException = e })

      // Assert
      assertFalse(successCalled)
      assertNotNull(capturedException)
      assertTrue(capturedException is IllegalStateException)
    }
  }

  @Test
  fun `signOut whenFirebaseSucceeds callsOnSuccess`() {
    // Arrange: Mock auth.signOut() to do nothing
    every { mockAuth.signOut() } returns Unit

    var successCalled = false
    var failureCalled = false

    runTest {
      // Act
      authModelFirebase.signOut(
          onSuccess = { successCalled = true }, onFailure = { failureCalled = true })

      // Assert
      assertTrue(successCalled)
      assertFalse(failureCalled)
      verify(exactly = 1) { mockAuth.signOut() } // Verify signOut was called
    }
  }

  @Test
  fun `signOut whenFirebaseThrowsException callsOnFailure`() {
    // Arrange: Mock auth.signOut() to throw an exception
    every { mockAuth.signOut() } throws Exception()

    var capturedException: Exception? = null
    var successCalled = false

    runTest {
      // Act
      authModelFirebase.signOut(
          onSuccess = { successCalled = true }, onFailure = { e -> capturedException = e })

      // Assert
      assertFalse(successCalled)
      assertNotNull(capturedException)
    }
  }
}
