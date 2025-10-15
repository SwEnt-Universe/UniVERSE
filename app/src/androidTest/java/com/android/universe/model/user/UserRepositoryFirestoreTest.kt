package com.android.universe.model.user

import com.android.universe.model.Tag
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertEquals

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

    @Test
    fun canAddUserAndRetrieve() = runTest{
        val userProfile = UserProfile(
            uid = "Bob",
            username = "Bobbb",
            firstName = "Test",
            lastName = "User",
            country = "Switzerland",
            description = "Just a test user",
            dateOfBirth = java.time.LocalDate.of(1990, 1, 1),
            tags = setOf(Tag.MUSIC, Tag.METAL)
        )
        userRepository.addUser(userProfile)
        val resultUser = userRepository.getUser("Bob")
        assertEquals("Bobbb", resultUser.uid)
        assertEquals("Bob", resultUser.username)
        assertEquals("Test", resultUser.firstName)
        assertEquals("User", resultUser.lastName)
        assertEquals("Switzerland", resultUser.country)
        assertEquals("Just a test user", resultUser.description)
        assertEquals(java.time.LocalDate.of(1990, 1, 1), resultUser.dateOfBirth)
        assertEquals(setOf(Tag.MUSIC, Tag.METAL), resultUser.tags)
    }


}