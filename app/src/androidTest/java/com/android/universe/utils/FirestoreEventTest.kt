package com.android.universe.utils

import android.util.Log
import com.android.universe.model.event.EVENTS_COLLECTION_PATH
import com.android.universe.model.event.EventRepository
import com.android.universe.model.event.EventRepositoryFirestore
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before

open class FirestoreEventTest {
  val testDispatcher = StandardTestDispatcher()
  val emulator = FirebaseEmulator

  suspend fun getEventCount(): Int {
    return emulator.firestore.collection(EVENTS_COLLECTION_PATH).get().await().size()
  }

  private suspend fun clearTestCollection() {
    val events = emulator.firestore.collection(EVENTS_COLLECTION_PATH).get().await()

    val batch = emulator.firestore.batch()
    events.documents.forEach { batch.delete(it.reference) }
    batch.commit().await()

    assert(getEventCount() == 0) {
      "Test collection is not empty after clearing, count: ${getEventCount()}"
    }
  }

  fun createInitializedRepository(): EventRepository {
    return EventRepositoryFirestore(db = emulator.firestore)
  }

  @Before
  open fun setUp() {
    val url = URL("http://10.0.2.2:8080") // Firestore emulator host for Android
    val connection = url.openConnection() as HttpURLConnection
    connection.connectTimeout = 2000
    connection.requestMethod = "GET"
    runTest {
      val eventCount = getEventCount()
      if (eventCount > 0) {
        Log.w(
            "FirebaseEmulatedTest",
            "Warning: Test collection is not empty at the beginning of the test, count: $eventCount",
        )
        clearTestCollection()
      }
    }
  }

  @After
  open fun tearDown() {
    runTest { // clearTestCollection()
    }
  }
}
