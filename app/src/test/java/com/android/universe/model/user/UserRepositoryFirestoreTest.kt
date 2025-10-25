package com.android.universe.model.user

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.utils.FirestoreUserTest
import com.android.universe.utils.UserTestData
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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

  companion object {
    private val userProfile1 = UserTestData.Bob
    private val userProfile2 = UserTestData.Alice
    private val userProfile3 = UserTestData.Rocky
  }

  @Test
  fun canAddUserAndRetrieve() = runTest {
    userRepository.addUser(userProfile1)
    val resultUser = userRepository.getUser("0")
    assertEquals(userProfile1, resultUser)
  }

  @Test
  fun canRetrieveAllTheUserWithGetAll() = runTest {
    userRepository.addUser(userProfile1)
    userRepository.addUser(userProfile2)
    userRepository.addUser(userProfile3)

    val result = userRepository.getAllUsers()

    assertEquals(3, result.size)
    assertEquals(userProfile1, result[0])
    assertEquals(userProfile2, result[1])
    assertEquals(userProfile3, result[2])
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
    assertEquals(userProfile2.copy(uid = userProfile1.uid), resultUser)
  }

  @Test
  fun updateUserWhenMultipleUsersExist() = runTest {
    userRepository.addUser(userProfile1)
    userRepository.addUser(userProfile2)

    userRepository.updateUser("1", userProfile3)
    val result = userRepository.getAllUsers()

    assertEquals(2, result.size)
    assertEquals(userProfile1, result[0])
    assertEquals(userProfile3.copy(uid = userProfile2.uid), result[1])
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

    assertEquals(userProfile1, result[0])
    assertEquals(userProfile3, result[1])
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
    assertFalse(userRepository.isUsernameUnique(userProfile1.username))
  }

  @Test
  fun checkUserAlreadyExistsFalse() = runTest {
    userRepository.addUser(userProfile1)
    assertTrue(userRepository.isUsernameUnique(userProfile2.username))
  }
}
