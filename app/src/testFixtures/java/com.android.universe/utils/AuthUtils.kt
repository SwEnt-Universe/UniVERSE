package com.android.universe.utils

import android.content.Context
import android.util.Base64
import androidx.core.os.bundleOf
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import org.json.JSONObject

/**
 * A utility object for generating fake JSON Web Tokens (JWTs) to simulate Google ID tokens in
 * tests.
 *
 * This is primarily used for testing authentication flows that require a Google ID token without
 * making real network calls or depending on Google's identity services.
 *
 * Each generated token includes a unique `"sub"` (subject) claim based on an internal counter, as
 * well as `"email"`, `"name"`, and `"picture"` fields.
 *
 * ### Example usage:
 * ```
 * val fakeToken = FakeJwtGenerator.createFakeGoogleIdToken(
 *     name = "Test User",
 *     email = "test@example.com"
 * )
 * println(fakeToken) // Outputs a string like "header.payload.sig"
 * ```
 *
 * This token can then be injected into a fake authentication manager such as
 * [FakeCredentialManager].
 */
object FakeJwtGenerator {
  private var _counter = 0
  private val counter
    get() = _counter++

  private fun base64UrlEncode(input: ByteArray): String {
    return Base64.encodeToString(input, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
  }

  /**
   * Creates a fake Google ID token with the given name and email.
   *
   * @param name User's name
   * @param email User's email
   * @return JWT string in the format header.payload.signature
   */
  fun createFakeGoogleIdToken(name: String, email: String): String {
    val header = JSONObject(mapOf("alg" to "none"))
    val payload =
        JSONObject(
            mapOf(
                "sub" to counter.toString(),
                "email" to email,
                "name" to name,
                "picture" to "http://example.com/avatar.png"))

    val headerEncoded = base64UrlEncode(header.toString().toByteArray())
    val payloadEncoded = base64UrlEncode(payload.toString().toByteArray())

    // Signature can be anything, emulator doesn't check it
    val signature = "sig"

    return "$headerEncoded.$payloadEncoded.$signature"
  }
}

/**
 * A fake implementation of [CredentialManager] for testing authentication flows without connecting
 * to Google services.
 *
 * This simulates a user login by providing a fake Google ID token, and mocks the behavior of
 * [GoogleIdTokenCredential] and the credential retrieval process.
 *
 * Usage example:
 * ```
 * val fakeToken = FakeJwtGenerator.createFakeGoogleIdToken("Test User", "test@example.com")
 * val fakeCredentialManager = FakeCredentialManager.create(fakeToken)
 * ```
 *
 * Then inject [fakeCredentialManager] in place of a real [CredentialManager] in tests to avoid real
 * network calls or dependency on Google services.
 *
 * @property context The application [Context] used to initialize the base [CredentialManager].
 * @constructor Private to enforce creation through [create].
 */
class FakeCredentialManager private constructor(private val context: Context) :
    CredentialManager by CredentialManager.create(context) {
  companion object {
    /**
     * Creates a FakeCredentialManager that always returns the provided fakeUserIdToken. Sets up
     * mocks for GoogleIdTokenCredential and the credential retrieval process.
     *
     * @param fakeUserIdToken A fake JWT token to use for testing
     * @return A CredentialManager instance that uses the fake token
     */
    fun create(fakeUserIdToken: String): CredentialManager {
      mockkObject(GoogleIdTokenCredential)
      val googleIdTokenCredential = mockk<GoogleIdTokenCredential>()
      every { googleIdTokenCredential.idToken } returns fakeUserIdToken
      every { googleIdTokenCredential.id } returns "fake@epfl.ch"
      every { GoogleIdTokenCredential.createFrom(any()) } returns googleIdTokenCredential
      val fakeCredentialManager = mockk<FakeCredentialManager>()
      val mockGetCredentialResponse = mockk<GetCredentialResponse>()

      val fakeCustomCredential =
          CustomCredential(
              type = TYPE_GOOGLE_ID_TOKEN_CREDENTIAL,
              data = bundleOf("id_token" to fakeUserIdToken))

      every { mockGetCredentialResponse.credential } returns fakeCustomCredential
      coEvery {
        fakeCredentialManager.getCredential(any<Context>(), any<GetCredentialRequest>())
      } returns mockGetCredentialResponse

      return fakeCredentialManager
    }
  }
}
