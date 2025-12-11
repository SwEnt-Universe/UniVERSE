package com.android.universe.utils

import androidx.test.core.app.ApplicationProvider
import com.android.universe.model.user.USERS_COLLECTION_PATH
import com.android.universe.model.user.UserProfile
import com.android.universe.model.user.UserRepository
import com.android.universe.model.user.UserRepositoryFirestore
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import kotlinx.coroutines.tasks.await
import org.junit.Before

/**
 * Base class for tests that require both Firebase Auth and Firestore emulators.
 * * DESIGN FOR SEQUENTIAL EXECUTION: This class performs a "Hard Wipe" of the emulator data before
 *   every test. This ensures a clean state but prevents parallel test execution.
 */
open class FirebaseAuthUserTest(private val isRobolectric: Boolean = true) {
  // Assuming FirebaseEmulator is a singleton object you have defined elsewhere
  private val emulator = FirebaseEmulator

  protected val auth: FirebaseAuth
    get() = emulator.auth

  companion object {
    const val EMAIL_SUFFIX = "@epfl.ch"
    const val EMAIL_PREFIX = "test_"
    const val PASSWORD = "password123"
  }

  @Before
  open fun setUp() {
    initializeFirebaseSafe()
    emulator.connect(isRobolectric)

    clearAuthEmulator()
    clearFirestoreEmulator()

    // Ensure the client SDK is also aware we are signed out
    emulator.auth.signOut()
  }

  private fun initializeFirebaseSafe() {
    if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
      FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }
  }

  /**
   * Wipes ALL users from the Auth Emulator via REST API. Throws an exception if cleanup fails to
   * prevent tests running on dirty data.
   */
  private fun clearAuthEmulator() {
    val projectId = FirebaseApp.getInstance().options.projectId
    val host = if (isRobolectric) "127.0.0.1" else "10.0.2.2"
    val url =
        URL("http://$host:${FirebaseEmulator.AUTH_PORT}/emulator/v1/projects/$projectId/accounts")

    sendDeleteRequest(url, "Auth")
  }

  /**
   * Wipes ALL data from the Firestore Emulator via REST API. This is atomic and much faster than
   * deleting documents one by one.
   */
  private fun clearFirestoreEmulator() {
    val projectId = FirebaseApp.getInstance().options.projectId
    val host = if (isRobolectric) "127.0.0.1" else "10.0.2.2"
    // The endpoint to wipe the default database
    val url =
        URL(
            "http://$host:${FirebaseEmulator.FIRESTORE_PORT}/emulator/v1/projects/$projectId/databases/(default)/documents")

    sendDeleteRequest(url, "Firestore")
  }

  /**
   * Helper to send DELETE requests to Emulator REST endpoints. Fails FAST if the emulator is
   * unreachable or returns an error.
   */
  private fun sendDeleteRequest(url: URL, serviceName: String) {
    var conn: HttpURLConnection? = null
    try {
      conn = url.openConnection() as HttpURLConnection
      conn.requestMethod = "DELETE"
      conn.connectTimeout = 2000 // 2 seconds timeout
      conn.readTimeout = 2000

      val responseCode = conn.responseCode
      if (responseCode != HttpURLConnection.HTTP_OK) {
        // FAIL FAST: Throw exception instead of logging warning
        throw RuntimeException("Failed to clear $serviceName emulator. Response: $responseCode")
      }
    } catch (e: Exception) {
      // Re-throw as RuntimeException to crash the test setup immediately.
      // This prevents "flaky" tests where the cleanup silently failed.
      throw RuntimeException(
          "CRITICAL: Could not connect to $serviceName emulator for cleanup. Is it running?", e)
    } finally {
      conn?.disconnect()
    }
  }

  /**
   * Creates a test user. Since we wipe the DB, we can be more relaxed about IDs, but using random
   * IDs is still a good habit.
   */
  suspend fun createRandomTestUser(userProfile: UserProfile): Pair<String, String> {
    val emailUid = UUID.randomUUID().toString()
    val uniqueEmail = EMAIL_PREFIX + emailUid + EMAIL_SUFFIX

    val authResult = auth.createUserWithEmailAndPassword(uniqueEmail, PASSWORD).await()
    val uid = authResult.user!!.uid

    val userWithUid = userProfile.copy(uid = uid)

    emulator.firestore
        .collection(USERS_COLLECTION_PATH)
        .document(uid)
        .set(userProfileToMap(userWithUid))
        .await()

    return Pair(uniqueEmail, uid)
  }

  fun createInitializedRepository(): UserRepository {
    return UserRepositoryFirestore(db = emulator.firestore)
  }

  private fun userProfileToMap(user: UserProfile): Map<String, Any?> {
    return mapOf(
        "uid" to user.uid,
        "username" to user.username,
        "firstName" to user.firstName,
        "lastName" to user.lastName,
        "country" to user.country,
        "description" to user.description,
        "dateOfBirth" to user.dateOfBirth.toString(),
        "tags" to user.tags.map { it.ordinal })
  }
}
