package com.android.universe.model.user

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Before

class UserRepositoryFirestoreTest {
    private lateinit var userRepository: UserRepository
    suspend fun checkEmulator(){
        FirebaseAuth.getInstance().signInAnonymously().await()
        val currentUser = FirebaseAuth.getInstance().currentUser
        assert(currentUser != null) { "Firebase Auth Emulator doesn't work" }
    }

    suspend fun clearUsersCollection() {
        val snapshot = FirebaseFirestore.getInstance()
            .collection("users")
            .get()
            .await()

        val batch = FirebaseFirestore.getInstance().batch()
        snapshot.documents.forEach { batch.delete(it.reference) }
        batch.commit().await()
    }

    @Before
    fun setUp() = runBlocking{
        FirebaseFirestore.getInstance().useEmulator("10.0.2.2", 8080)
        FirebaseAuth.getInstance().useEmulator("10.0.2.2", 9099)
        checkEmulator()
        userRepository = UserRepositoryFirestore(FirebaseFirestore.getInstance())
    }

    @After
    fun tearDown() = runBlocking {
        clearUsersCollection()
    }


}