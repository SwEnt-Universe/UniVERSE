package com.android.universe.model.user

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.model.Tag
import com.android.universe.utils.FirestoreUserTest
import java.time.LocalDate
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserRepositoryFirestoreTest : FirestoreUserTest() {
  private lateinit var userRepository: UserRepository

  @Before
  override fun setUp() = runTest {
    super.setUp()
    userRepository = createInitializedRepository()
  }

  private val userProfile1 =
      UserProfile(
          uid = "0",
          username = "Bobbb",
          firstName = "Test",
          lastName = "User",
          country = "Switzerland",
          description = "Just a test user",
          dateOfBirth = LocalDate.of(1990, 1, 1),
          tags = setOf(Tag.MUSIC, Tag.METAL))

  private val userProfile2 =
      UserProfile(
          uid = "1",
          username = "Al",
          firstName = "second",
          lastName = "User2",
          country = "France",
          description = "a second user",
          dateOfBirth = LocalDate.of(2005, 12, 15),
          tags = setOf(Tag.TENNIS))

  private val userProfile3 =
      UserProfile(
          uid = "2",
          username = "Rocky",
          firstName = "third",
          lastName = "User3",
          country = "Portugal",
          description = "a third user",
          dateOfBirth = LocalDate.of(2012, 9, 12),
          tags = setOf(Tag.ROLE_PLAYING_GAMES, Tag.ARTIFICIAL_INTELLIGENCE))

  @Test
  fun canAddUserAndRetrieve() = runTest {
    userRepository.addUser(userProfile1)
    val resultUser = userRepository.getUser("0")
    with(resultUser) {
      Assert.assertEquals(userProfile1.uid, uid)
      Assert.assertEquals("Bobbb", username)
      Assert.assertEquals("Test", firstName)
      Assert.assertEquals("User", lastName)
      Assert.assertEquals("Switzerland", country)
      Assert.assertEquals("Just a test user", description)
      Assert.assertEquals(LocalDate.of(1990, 1, 1), dateOfBirth)
      Assert.assertEquals(setOf(Tag.MUSIC, Tag.METAL), tags)
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
      Assert.assertEquals("0", uid)
      Assert.assertEquals("Bobbb", username)
      Assert.assertEquals("Test", firstName)
      Assert.assertEquals("User", lastName)
      Assert.assertEquals("Switzerland", country)
      Assert.assertEquals("Just a test user", description)
      Assert.assertEquals(LocalDate.of(1990, 1, 1), dateOfBirth)
      Assert.assertEquals(setOf(Tag.MUSIC, Tag.METAL), tags)
    }
    with(resultUser2) {
      Assert.assertEquals("1", uid)
      Assert.assertEquals("Al", username)
      Assert.assertEquals("second", firstName)
      Assert.assertEquals("User2", lastName)
      Assert.assertEquals("France", country)
      Assert.assertEquals("a second user", description)
      Assert.assertEquals(LocalDate.of(2005, 12, 15), dateOfBirth)
      Assert.assertEquals(setOf(Tag.TENNIS), tags)
    }

    with(resultUser3) {
      Assert.assertEquals("2", uid)
      Assert.assertEquals("Rocky", username)
      Assert.assertEquals("third", firstName)
      Assert.assertEquals("User3", lastName)
      Assert.assertEquals("Portugal", country)
      Assert.assertEquals("a third user", description)
      Assert.assertEquals(LocalDate.of(2012, 9, 12), dateOfBirth)
      Assert.assertEquals(setOf(Tag.ROLE_PLAYING_GAMES, Tag.ARTIFICIAL_INTELLIGENCE), tags)
    }
  }

  @Test
  fun canRetrieveAllTheUserWithGetAll() = runTest {
    userRepository.addUser(userProfile1)
    userRepository.addUser(userProfile2)
    userRepository.addUser(userProfile3)

    val result = userRepository.getAllUsers()

    Assert.assertEquals(3, result.size)

    with(result[0]) {
      Assert.assertEquals("0", uid)
      Assert.assertEquals("Bobbb", username)
      Assert.assertEquals("Test", firstName)
      Assert.assertEquals("User", lastName)
      Assert.assertEquals("Switzerland", country)
      Assert.assertEquals("Just a test user", description)
      Assert.assertEquals(LocalDate.of(1990, 1, 1), dateOfBirth)
      Assert.assertEquals(setOf(Tag.MUSIC, Tag.METAL), tags)
    }

    with(result[1]) {
      Assert.assertEquals("1", uid)
      Assert.assertEquals("Al", username)
      Assert.assertEquals("second", firstName)
      Assert.assertEquals("User2", lastName)
      Assert.assertEquals("France", country)
      Assert.assertEquals("a second user", description)
      Assert.assertEquals(LocalDate.of(2005, 12, 15), dateOfBirth)
      Assert.assertEquals(setOf(Tag.TENNIS), tags)
    }

    with(result[2]) {
      Assert.assertEquals("2", uid)
      Assert.assertEquals("Rocky", username)
      Assert.assertEquals("third", firstName)
      Assert.assertEquals("User3", lastName)
      Assert.assertEquals("Portugal", country)
      Assert.assertEquals("a third user", description)
      Assert.assertEquals(LocalDate.of(2012, 9, 12), dateOfBirth)
      Assert.assertEquals(setOf(Tag.ROLE_PLAYING_GAMES, Tag.ARTIFICIAL_INTELLIGENCE), tags)
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
      Assert.assertEquals("1", uid)
      Assert.assertEquals("Al", username)
      Assert.assertEquals("second", firstName)
      Assert.assertEquals("User2", lastName)
      Assert.assertEquals("France", country)
      Assert.assertEquals("a second user", description)
      Assert.assertEquals(LocalDate.of(2005, 12, 15), dateOfBirth)
      Assert.assertEquals(setOf(Tag.TENNIS), tags)
    }
  }

  @Test
  fun updateUserWhenMultipleUsersExist() = runTest {
    userRepository.addUser(userProfile1)
    userRepository.addUser(userProfile2)

    userRepository.updateUser("1", userProfile3)
    val result = userRepository.getAllUsers()
    Assert.assertEquals(2, result.size)

    with(result[0]) {
      Assert.assertEquals("0", uid)
      Assert.assertEquals("Bobbb", username)
      Assert.assertEquals("Test", firstName)
      Assert.assertEquals("User", lastName)
      Assert.assertEquals("Switzerland", country)
      Assert.assertEquals("Just a test user", description)
      Assert.assertEquals(LocalDate.of(1990, 1, 1), dateOfBirth)
      Assert.assertEquals(setOf(Tag.MUSIC, Tag.METAL), tags)
    }

    with(result[1]) {
      Assert.assertEquals("2", uid)
      Assert.assertEquals("Rocky", username)
      Assert.assertEquals("third", firstName)
      Assert.assertEquals("User3", lastName)
      Assert.assertEquals("Portugal", country)
      Assert.assertEquals("a third user", description)
      Assert.assertEquals(LocalDate.of(2012, 9, 12), dateOfBirth)
      Assert.assertEquals(setOf(Tag.ROLE_PLAYING_GAMES, Tag.ARTIFICIAL_INTELLIGENCE), tags)
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
    Assert.assertEquals(0, result.size)
  }

  @Test
  fun deleteUserWhenMultipleUsersExist() = runTest {
    userRepository.addUser(userProfile1)
    userRepository.addUser(userProfile2)
    userRepository.addUser(userProfile3)

    userRepository.deleteUser("1")
    val result = userRepository.getAllUsers()
    Assert.assertEquals(2, result.size)

    with(result[0]) {
      Assert.assertEquals("0", uid)
      Assert.assertEquals("Bobbb", username)
      Assert.assertEquals("Test", firstName)
      Assert.assertEquals("User", lastName)
      Assert.assertEquals("Switzerland", country)
      Assert.assertEquals("Just a test user", description)
      Assert.assertEquals(LocalDate.of(1990, 1, 1), dateOfBirth)
      Assert.assertEquals(setOf(Tag.MUSIC, Tag.METAL), tags)
    }

    with(result[1]) {
      Assert.assertEquals("2", uid)
      Assert.assertEquals("Rocky", username)
      Assert.assertEquals("third", firstName)
      Assert.assertEquals("User3", lastName)
      Assert.assertEquals("Portugal", country)
      Assert.assertEquals("a third user", description)
      Assert.assertEquals(LocalDate.of(2012, 9, 12), dateOfBirth)
      Assert.assertEquals(setOf(Tag.ROLE_PLAYING_GAMES, Tag.ARTIFICIAL_INTELLIGENCE), tags)
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
    Assert.assertEquals(false, userRepository.isUsernameUnique("Bobbb"))
  }

  @Test
  fun checkUserAlreadyExistsFalse() = runTest {
    userRepository.addUser(userProfile1)
    Assert.assertEquals(true, userRepository.isUsernameUnique("Al"))
  }
}
