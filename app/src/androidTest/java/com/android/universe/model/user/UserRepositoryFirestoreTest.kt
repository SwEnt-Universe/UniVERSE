package com.android.universe.model.user

import com.android.universe.model.Tag
import com.android.universe.utils.FirebaseEmulator
import com.android.universe.utils.FirestoreTest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertEquals

class UserRepositoryFirestoreTest : FirestoreTest() {
    var userRepository = createInitializedRepository()

    @Before
    override fun setUp() = runBlocking {
        super.setUp()
    }

    private val userProfile1 = UserProfile(
        uid = "0",
        username = "Bobbb",
        firstName = "Test",
        lastName = "User",
        country = "Switzerland",
        description = "Just a test user",
        dateOfBirth = java.time.LocalDate.of(1990, 1, 1),
        tags = setOf(Tag.MUSIC, Tag.METAL)
    )

    private val userProfile2 = UserProfile(
        uid = "1",
        username = "Al",
        firstName = "second",
        lastName = "User2",
        country = "France",
        description = "a second user",
        dateOfBirth = java.time.LocalDate.of(2005, 12, 15),
        tags = setOf(Tag.TENNIS)
    )

    private val userProfile3 = UserProfile(
        uid = "3",
        username = "Rocky",
        firstName = "third",
        lastName = "User3",
        country = "Portugal",
        description = "a third user",
        dateOfBirth = java.time.LocalDate.of(2012, 9, 12),
        tags = setOf(Tag.ROLE_PLAYING_GAMES, Tag.ARTIFICIAL_INTELLIGENCE)
    )


    @Test
    fun canAddUserAndRetrieve() = runTest{
        userRepository.addUser(userProfile1)
        val resultUser = userRepository.getUser("0")
        with(resultUser){
            assertEquals(userProfile1.uid, uid)
            assertEquals("Bobbb", username)
            assertEquals("Test", firstName)
            assertEquals("User", lastName)
            assertEquals("Switzerland", country)
            assertEquals("Just a test user", description)
            assertEquals(java.time.LocalDate.of(1990, 1, 1), dateOfBirth)
            assertEquals(setOf(Tag.MUSIC, Tag.METAL), tags)
        }
    }

    @Test
    fun canAddMultipleUserAndRetrieveAll() = runTest {
        userRepository.addUser(userProfile1)
        userRepository.addUser(userProfile2)
        userRepository.addUser(userProfile3)

        val resultUser1 = userRepository.getUser("0")
        val resultUser2 = userRepository.getUser("1")
        val resultUser3 = userRepository.getUser("2")

        with(resultUser1) {
            assertEquals("0", uid)
            assertEquals("Bobbb", username)
            assertEquals("Test", firstName)
            assertEquals("User", lastName)
            assertEquals("Switzerland", country)
            assertEquals("Just a test user", description)
            assertEquals(java.time.LocalDate.of(1990, 1, 1), dateOfBirth)
            assertEquals(setOf(Tag.MUSIC, Tag.METAL), tags)
        }
        with(resultUser2) {
            assertEquals("1", uid)
            assertEquals("Al", username)
            assertEquals("second", firstName)
            assertEquals("User2", lastName)
            assertEquals("France", country)
            assertEquals("a second user", description)
            assertEquals(java.time.LocalDate.of(2005, 12, 15), dateOfBirth)
            assertEquals(setOf(Tag.TENNIS), tags)
        }

        with(resultUser3) {
            assertEquals("2", uid)
            assertEquals("Rocky", username)
            assertEquals("third", firstName)
            assertEquals("User3", lastName)
            assertEquals("Portugal", country)
            assertEquals("a third user", description)
            assertEquals(java.time.LocalDate.of(2012, 9, 12), dateOfBirth)
            assertEquals(
                setOf(Tag.ROLE_PLAYING_GAMES, Tag.ARTIFICIAL_INTELLIGENCE),tags)
        }
    }

    @Test
    fun canRetrieveAllTheUserWithGetAll() = runTest {
        userRepository.addUser(userProfile1)
        userRepository.addUser(userProfile2)
        userRepository.addUser(userProfile3)

        val result = userRepository.getAllUsers()

        assertEquals(3, result.size)

        with(result[0]) {
            assertEquals("0", uid)
            assertEquals("Bobbb", username)
            assertEquals("Test", firstName)
            assertEquals("User", lastName)
            assertEquals("Switzerland", country)
            assertEquals("Just a test user", description)
            assertEquals(java.time.LocalDate.of(1990, 1, 1), dateOfBirth)
            assertEquals(setOf(Tag.MUSIC, Tag.METAL), tags)
        }

        with(result[1]) {
            assertEquals("1", uid)
            assertEquals("Al", username)
            assertEquals("second", firstName)
            assertEquals("User2", lastName)
            assertEquals("France", country)
            assertEquals("a second user", description)
            assertEquals(java.time.LocalDate.of(2005, 12, 15), dateOfBirth)
            assertEquals(setOf(Tag.TENNIS), tags)
        }

        with(result[2]) {
            assertEquals("2", uid)
            assertEquals("Rocky", username)
            assertEquals("third", firstName)
            assertEquals("User3", lastName)
            assertEquals("Portugal", country)
            assertEquals("a third user", description)
            assertEquals(java.time.LocalDate.of(2012, 9, 12), dateOfBirth)
            assertEquals(setOf(Tag.ROLE_PLAYING_GAMES, Tag.ARTIFICIAL_INTELLIGENCE), tags)
        }
    }

    @Test
    fun getUserThrowsExceptionWhenUserNotFound() = runTest {
        try {
            userRepository.getUser("NonExistentUser")
            assert(false) { "Expected NoSuchElementException was not thrown" }
        } catch (e: NoSuchElementException) {
            assert(true)
        } catch (e: Exception) {
            assert(false) { "Unexpected exception type: ${e::class.java}" }
        }
    }

    @Test
    fun updateUserReplacesExistingUserCompletely() = runTest {
        userRepository.addUser(userProfile1)
        userRepository.updateUser("0", userProfile2)
        val resultUser = userRepository.getUser("0")
        with(resultUser) {
            assertEquals("1", uid)
            assertEquals("Al", username)
            assertEquals("second", firstName)
            assertEquals("User2", lastName)
            assertEquals("France", country)
            assertEquals("a second user", description)
            assertEquals(java.time.LocalDate.of(2005, 12, 15), dateOfBirth)
            assertEquals(setOf(Tag.TENNIS), tags)
        }
    }

    @Test
    fun updateUserWhenMultipleUsersExist() = runTest {
        userRepository.addUser(userProfile1)
        userRepository.addUser(userProfile2)

        userRepository.updateUser("1", userProfile3)
        val result = userRepository.getAllUsers()
        assertEquals(3, result.size)

        with(result[0]) {
            assertEquals("0", uid)
            assertEquals("Bobbb", username)
            assertEquals("Test", firstName)
            assertEquals("User", lastName)
            assertEquals("Switzerland", country)
            assertEquals("Just a test user", description)
            assertEquals(java.time.LocalDate.of(1990, 1, 1), dateOfBirth)
            assertEquals(setOf(Tag.MUSIC, Tag.METAL), tags)
        }

        with(result[1]) {
            assertEquals("2", uid)
            assertEquals("Rocky", username)
            assertEquals("third", firstName)
            assertEquals("User3", lastName)
            assertEquals("Portugal", country)
            assertEquals("a third user", description)
            assertEquals(java.time.LocalDate.of(2012, 9, 12), dateOfBirth)
            assertEquals(setOf(Tag.ROLE_PLAYING_GAMES, Tag.ARTIFICIAL_INTELLIGENCE), tags)
        }
    }

    @Test
    fun updateNonExistentUserThrowsException() = runTest {
        try {
            userRepository.updateUser("NonExistentUser", userProfile1)
            assert(false) { "Expected NoSuchElementException was not thrown" }
        } catch (e: NoSuchElementException) {
            assert(true)
        } catch (e: Exception) {
            assert(false) { "Unexpected exception type: ${e::class.java}" }
        }
    }

    @Test
    fun deleteUserProfile() = runTest {
        userRepository.addUser(userProfile1)
        userRepository.deleteUser("0")
        val result = userRepository.getAllUsers()
        assertEquals(0, result.size)
    }

    @Test
    fun deleteUserWhenMultipleUsersExist() = runTest {
        userRepository.addUser(userProfile1)
        userRepository.addUser(userProfile2)
        userRepository.addUser(userProfile3)

        userRepository.deleteUser("1")
        val result = userRepository.getAllUsers()
        assertEquals(2, result.size)

        with(result[0]) {
            assertEquals("0", uid)
            assertEquals("Bobbb", username)
            assertEquals("Test", firstName)
            assertEquals("User", lastName)
            assertEquals("Switzerland", country)
            assertEquals("Just a test user", description)
            assertEquals(java.time.LocalDate.of(1990, 1, 1), dateOfBirth)
            assertEquals(setOf(Tag.MUSIC, Tag.METAL), tags)
        }

        with(result[1]) {
            assertEquals("2", uid)
            assertEquals("Rocky", username)
            assertEquals("third", firstName)
            assertEquals("User3", lastName)
            assertEquals("Portugal", country)
            assertEquals("a third user", description)
            assertEquals(java.time.LocalDate.of(2012, 9, 12), dateOfBirth)
            assertEquals(setOf(Tag.ROLE_PLAYING_GAMES, Tag.ARTIFICIAL_INTELLIGENCE), tags)
        }
    }

    @Test
    fun deleteNonExistentUserThrowsException() = runTest {
        try {
            userRepository.deleteUser("NonExistentUser")
            assert(false) { "Expected NoSuchElementException was not thrown" }
        } catch (e: NoSuchElementException) {
            assert(true)
        } catch (e: Exception) {
            assert(false) { "Unexpected exception type: ${e::class.java}" }
        }
    }

    @Test
    fun checkUserAlreadyExistsTrue() = runTest {
        userRepository.addUser(userProfile1)
        assertEquals(false, userRepository.isUsernameUnique("Bobbb"))
    }

    @Test
    fun checkUserAlreadyExistsFalse() = runTest {
        userRepository.addUser(userProfile1)
        assertEquals(true, userRepository.isUsernameUnique("Al"))
    }




}