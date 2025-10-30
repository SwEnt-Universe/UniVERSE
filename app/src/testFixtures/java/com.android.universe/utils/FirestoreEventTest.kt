package com.android.universe.utils

import android.util.Log
import androidx.test.core.app.ApplicationProvider
import com.android.universe.model.event.EVENTS_COLLECTION_PATH
import com.android.universe.model.event.EventRepository
import com.android.universe.model.event.EventRepositoryFirestore
import com.google.firebase.FirebaseApp
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before

open class FirestoreEventTest {
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
    if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
      FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }

    emulator.connect()

    try {
      val host = FirebaseEmulator.currentHost
      val url = URL("http://$host:${FirebaseEmulator.FIRESTORE_PORT}")
      val conn = url.openConnection() as HttpURLConnection
      conn.connectTimeout = 2000
      conn.requestMethod = "GET"
      conn.connect()
      conn.disconnect()
    } catch (e: Exception) {
      Log.w("FirestoreEventTest", "Firestore emulator might not be reachable: ${e.message}")
    }

    runTest {
      val eventCount = getEventCount()
      if (eventCount > 0) {
        Log.w(
            "FirestoreEventTest",
            "Warning: Event collection not empty at test start, count: $eventCount")
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
