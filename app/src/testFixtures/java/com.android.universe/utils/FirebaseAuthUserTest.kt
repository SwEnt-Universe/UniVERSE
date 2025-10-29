package com.android.universe.utils

import android.util.Log
import androidx.test.core.app.ApplicationProvider
import com.android.universe.model.user.USERS_COLLECTION_PATH
import com.android.universe.model.user.UserProfile
import com.android.universe.model.user.UserRepository
import com.android.universe.model.user.UserRepositoryFirestore
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before

/**
 * Base class for tests that require both Firebase Auth and Firestore emulators. Combines
 * authentication and user profile management.
 */
abstract class FirebaseAuthUserTest {
  protected val emulator = FirebaseEmulator
  protected val auth: FirebaseAuth
    get() = emulator.auth

  suspend fun getFirestoreUserCount(): Int {
    return emulator.firestore.collection(USERS_COLLECTION_PATH).get().await().size()
  }

  fun getAuthUserCount(): Int {
    val projectId = FirebaseApp.getInstance().options.projectId
    val host = FirebaseEmulator.currentHost
    val url =
        URL("http://$host:${FirebaseEmulator.AUTH_PORT}/emulator/v1/projects/$projectId/accounts")
    val conn = url.openConnection() as HttpURLConnection
    return try {
      conn.requestMethod = "GET"
      conn.connectTimeout = 2000
      conn.connect()
      val response = conn.inputStream.bufferedReader().use { it.readText() }
      if (response.contains("\"users\"")) response.split("\"localId\"").size - 1 else 0
    } catch (e: Exception) {
      Log.w("FirebaseAuthUserTest", "Failed to get auth user count: ${e.message}")
      0
    } finally {
      conn.disconnect()
    }
  }

  private suspend fun clearFirestoreUsers() {
    val users = emulator.firestore.collection(USERS_COLLECTION_PATH).get().await()

    if (users.isEmpty) return

    val batch = emulator.firestore.batch()
    users.documents.forEach { batch.delete(it.reference) }
    batch.commit().await()

    assert(getFirestoreUserCount() == 0) {
      "Firestore users collection is not empty after clearing, count: ${getFirestoreUserCount()}"
    }
  }

  fun clearAuthUsers() {
    val projectId = FirebaseApp.getInstance().options.projectId
    val host = FirebaseEmulator.currentHost
    val url =
        URL("http://$host:${FirebaseEmulator.AUTH_PORT}/emulator/v1/projects/$projectId/accounts")
    val conn = url.openConnection() as HttpURLConnection
    try {
      conn.requestMethod = "DELETE"
      conn.connectTimeout = 2000
      conn.connect()
      Log.i("FirebaseAuthUserTest", "Cleared Auth emulator users successfully.")
    } catch (e: Exception) {
      Log.w("FirebaseAuthUserTest", "Failed to clear Auth emulator users: ${e.message}")
    } finally {
      conn.disconnect()
    }
  }

  fun createInitializedRepository(): UserRepository {
    return UserRepositoryFirestore(db = emulator.firestore)
  }

  /**
   * Creates a test user in both Auth and Firestore.
   *
   * @param userProfile The user profile to create in Firestore
   * @param email The email for the Auth user
   * @return The Firebase Auth UID
   */
  suspend fun createTestUser(userProfile: UserProfile, email: String): String {
    val authResult = auth.createUserWithEmailAndPassword(email, "test-password-123").await()
    val uid = authResult.user!!.uid

    val userWithUid = userProfile.copy(uid = uid)
    emulator.firestore.collection(USERS_COLLECTION_PATH).document(uid).set(userWithUid).await()

    return uid
  }

  /** Signs in a test user. */
  suspend fun signInTestUser(email: String, password: String = "test-password-123"): String {
    val authResult = auth.signInWithEmailAndPassword(email, password).await()
    return authResult.user!!.uid
  }

  @Before
  open fun setUp() {
    if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
      FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }

    emulator.connect()

    runTest {
      val firestoreUserCount = getFirestoreUserCount()
      val authUserCount = getAuthUserCount()

      if (firestoreUserCount > 0) {
        Log.w(
            "FirebaseAuthUserTest",
            "Warning: Firestore test collection not empty at start, count: $firestoreUserCount",
        )
        clearFirestoreUsers()
      }

      if (authUserCount > 0) {
        Log.w(
            "FirebaseAuthUserTest",
            "Warning: Auth emulator has users at start, count: $authUserCount",
        )
        clearAuthUsers()
      }
    }
  }

  @After open fun tearDown() {}
}
