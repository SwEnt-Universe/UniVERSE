package com.android.universe.utils

import android.util.Log
import androidx.test.core.app.ApplicationProvider
import com.android.universe.model.user.USERS_COLLECTION_PATH
import com.android.universe.model.user.UserRepository
import com.android.universe.model.user.UserRepositoryFirestore
import com.google.firebase.FirebaseApp
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before

open class FirestoreUserTest() {
  val emulator = FirebaseEmulator

  suspend fun getUserCount(): Int {
    return emulator.firestore.collection(USERS_COLLECTION_PATH).get().await().size()
  }

  private suspend fun clearTestCollection() {
    val users = emulator.firestore.collection(USERS_COLLECTION_PATH).get().await()

    val batch = emulator.firestore.batch()
    users.documents.forEach { batch.delete(it.reference) }
    batch.commit().await()

    assert(getUserCount() == 0) {
      "Test collection is not empty after clearing, count: ${getUserCount()}"
    }
  }

  fun createInitializedRepository(): UserRepository {
    return UserRepositoryFirestore(db = emulator.firestore)
  }

  @Before
  open fun setUp() {
    FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    val url = URL("http://10.0.2.2:8080") // Firestore emulator host for Android
    val connection = url.openConnection() as HttpURLConnection
    connection.connectTimeout = 2000
    connection.requestMethod = "GET"
    runTest {
      val userCount = getUserCount()
      if (userCount > 0) {
        Log.w(
            "FirebaseEmulatedTest",
            "Warning: Test collection is not empty at the beginning of the test, count: $userCount",
        )
        clearTestCollection()
      }
    }
  }

  @After open fun tearDown() {}
}
