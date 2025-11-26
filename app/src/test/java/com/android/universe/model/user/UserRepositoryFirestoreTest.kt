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
    val resultUser = userRepository.getUser(userProfile1.uid)
    assertEquals(userProfile1, resultUser)
  }

  @Test
  fun canRetrieveAllTheUserWithGetAll() = runTest {
    userRepository.addUser(userProfile1)
    userRepository.addUser(userProfile2)
    userRepository.addUser(userProfile3)

    val result = userRepository.getAllUsers().toSet()

    assertEquals(3, result.size)

    val expectedSet = setOf(userProfile1, userProfile2, userProfile3)
    assertEquals(expectedSet, result)
  }

  @Test(expected = NoSuchElementException::class)
  fun getUserThrowsExceptionWhenUserNotFound() = runTest {
    userRepository.getUser("NonExistentUser")
  }

  @Test
  fun updateUserReplacesExistingUserCompletely() = runTest {
    userRepository.addUser(userProfile1)
    userRepository.updateUser(userProfile1.uid, userProfile2)
    val resultUser = userRepository.getUser(userProfile1.uid)
    assertEquals(userProfile2.copy(uid = userProfile1.uid), resultUser)
  }

  @Test
  fun updateUserWhenMultipleUsersExist() = runTest {
    userRepository.addUser(userProfile1)
    userRepository.addUser(userProfile2)

    userRepository.updateUser(userProfile2.uid, userProfile3)
    val result = userRepository.getAllUsers().toSet()

    assertEquals(2, result.size)

    val expectedSet = setOf(userProfile1, userProfile3.copy(uid = userProfile2.uid))
    assertEquals(expectedSet, result)
  }

  @Test(expected = NoSuchElementException::class)
  fun updateNonExistentUserThrowsException() = runTest {
    userRepository.updateUser("NonExistentUser", userProfile1)
  }

  @Test
  fun deleteUserProfile() = runTest {
    userRepository.addUser(userProfile1)
    userRepository.deleteUser(userProfile1.uid)
    val result = userRepository.getAllUsers()
    assertEquals(0, result.size)
  }

  @Test
  fun deleteUserWhenMultipleUsersExist() = runTest {
    userRepository.addUser(userProfile1)
    userRepository.addUser(userProfile2)
    userRepository.addUser(userProfile3)

    userRepository.deleteUser(userProfile2.uid)
    val result = userRepository.getAllUsers().toSet()
    assertEquals(2, result.size)

    val expectedSet = setOf(userProfile1, userProfile3)
    assertEquals(expectedSet, result)
  }

  @Test(expected = NoSuchElementException::class)
  fun deleteNonExistentUserThrowsException() = runTest {
    userRepository.deleteUser("NonExistentUser")
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

  @Test
  fun checkFollowAExistingUser() = runTest {
    userRepository.addUser(userProfile1)
    userRepository.addUser(userProfile2)
    userRepository.followUser(userProfile1.uid, userProfile2.uid)

    val expectedFollowingSet = userProfile1.following + userProfile2.uid
    val expectedFollowerSet = userProfile2.followers + userProfile1.uid

    val currentUSer = userRepository.getUser(userProfile1.uid)
    val targetUser = userRepository.getUser(userProfile2.uid)

    assertEquals(expectedFollowingSet, currentUSer.following)
    assertEquals(expectedFollowerSet, targetUser.followers)
  }

  @Test
  fun checkFollowANonExistingUser() = runTest {
    userRepository.addUser(userProfile1)
    try {
      userRepository.followUser(userProfile1.uid, userProfile2.uid)
      assertTrue(false)
    } catch (e: Exception) {
      assertTrue(true)
    }
  }

  @Test
  fun checkFollowAlreadyFollowUser() = runTest {
    userRepository.addUser(userProfile1)
    userRepository.addUser(userProfile2)
    userRepository.followUser(userProfile1.uid, userProfile2.uid)
    userRepository.followUser(userProfile1.uid, userProfile2.uid)

    val expectedFollowingSet = userProfile1.following + userProfile2.uid
    val expectedFollowerSet = userProfile2.followers + userProfile1.uid

    val currentUSer = userRepository.getUser(userProfile1.uid)
    val targetUser = userRepository.getUser(userProfile2.uid)

    assertEquals(expectedFollowingSet, currentUSer.following)
    assertEquals(expectedFollowerSet, targetUser.followers)
  }

  @Test
  fun checkUnfollowAExistingUser() = runTest {
    userRepository.addUser(userProfile1)
    userRepository.addUser(userProfile2)
    userRepository.followUser(userProfile1.uid, userProfile2.uid)
    userRepository.unfollowUser(userProfile1.uid, userProfile2.uid)

    val expectedFollowingSet = userProfile1.following
    val expectedFollowerSet = userProfile2.followers

    val currentUSer = userRepository.getUser(userProfile1.uid)
    val targetUser = userRepository.getUser(userProfile2.uid)

    assertEquals(expectedFollowingSet, currentUSer.following)
    assertEquals(expectedFollowerSet, targetUser.followers)
  }

  @Test
  fun checkUnfollowANonExistingUser() = runTest {
    userRepository.addUser(userProfile1)
    try {
      userRepository.unfollowUser(userProfile1.uid, userProfile2.uid)
      assertTrue(false)
    } catch (e: Exception) {
      assertTrue(true)
    }
  }

  @Test
  fun checkUnfollowNonFollowUser() = runTest {
    userRepository.addUser(userProfile1)
    userRepository.addUser(userProfile2)
    userRepository.unfollowUser(userProfile1.uid, userProfile2.uid)

    val expectedFollowingSet = userProfile1.following
    val expectedFollowerSet = userProfile2.followers

    val currentUSer = userRepository.getUser(userProfile1.uid)
    val targetUser = userRepository.getUser(userProfile2.uid)

    assertEquals(expectedFollowingSet, currentUSer.following)
    assertEquals(expectedFollowerSet, targetUser.followers)
  }
}
