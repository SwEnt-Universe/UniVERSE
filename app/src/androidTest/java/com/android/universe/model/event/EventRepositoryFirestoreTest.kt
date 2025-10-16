package com.android.universe.model.event

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Before

class EventRepositoryFirestoreTest {
    private lateinit var eventRepository: EventRepository
    private lateinit var db: FirebaseFirestore

    suspend fun checkEmulator(){
        FirebaseAuth.getInstance().signInAnonymously().await()
        val currentUser = FirebaseAuth.getInstance().currentUser
        assert(currentUser != null) { "Firebase Auth Emulator doesn't work" }
    }

    suspend fun clearUsersCollection() {
        val snapshot = db
            .collection("users")
            .get()
            .await()

        val batch = db.batch()
        snapshot.documents.forEach { batch.delete(it.reference) }
        batch.commit().await()
    }

    @Before
    fun setUp() = runBlocking{
        db = FirebaseFirestore.getInstance()
        db.useEmulator("10.0.2.2", 8080)

        val auth = FirebaseAuth.getInstance()
        auth.useEmulator("10.0.2.2", 9099)

        checkEmulator()
        eventRepository = EventRepositoryFirestore(db)
    }

    @After
    fun tearDown() = runBlocking {
        clearUsersCollection()
    }
}