package com.android.universe.model.user

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.model.tag.Tag
import java.time.LocalDate
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FakeUserRepositoryTest {

  private lateinit var repository: FakeUserRepository
  private val tag = Tag.METAL
  private val tags = setOf(tag)

  private val userProfile1 =
      UserProfile(
          uid = "0",
          username = "Bobbb",
          firstName = "Test",
          lastName = "User",
          country = "Switzerland",
          description = "Just a test user",
          dateOfBirth = LocalDate.of(1990, 1, 1),
          tags = setOf(Tag.MUSIC, Tag.METAL),
          followers = emptySet(),
      )

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
          tags = setOf(Tag.DND, Tag.AI))

  @Before
  fun setup() {
    repository = FakeUserRepository()
  }

  @Test
  fun getUser_throwsException_forNonExistentUser() = runTest {
    try {
      repository.getUser("nonexistent")
      fail("Expected NoSuchElementException to be thrown")
    } catch (e: NoSuchElementException) {
      assertEquals("No user found with uid: nonexistent", e.message)
    }
  }

  @Test
  fun addUser_storesUser_andCanBeRetrieved() = runTest {
    val user =
        UserProfile(
            uid = "0",
            username = "alice",
            firstName = "Alice",
            lastName = "Smith",
            country = "CH",
            description = "Bio",
            dateOfBirth = LocalDate.of(1990, 1, 1),
            tags = tags)
    repository.addUser(user)

    val result = repository.getUser("0")
    assertNotNull(result)
    assertEquals("alice", result.username)
    assertEquals("Alice", result.firstName)
    assertEquals("Smith", result.lastName)
    assertEquals("CH", result.country)
    assertEquals("Bio", result.description)
    assertEquals(LocalDate.of(1990, 1, 1), result.dateOfBirth)
    assertEquals(tags, result.tags)
  }

  @Test
  fun addUser_storesUserWithNoDescription_andCanBeRetrieved() = runTest {
    val user = userProfile1
    repository.addUser(user)

    val result = repository.getUser(user.uid)
    assertNotNull(result)
    assertSameUser(user, result)
  }

  @Test
  fun addUser_storesMultipleUsers_andAllCanBeRetrieved() = runTest {
    repository.addUser(userProfile1)
    repository.addUser(userProfile2)
    repository.addUser(userProfile3)
    // can retrieve each user individually
    repository.getUser(userProfile1.uid)
    repository.getUser(userProfile2.uid)
    repository.getUser(userProfile3.uid)

    // all users returned by getAllUsers
    val allUsers = repository.getAllUsers()
    assertEquals(3, allUsers.size)
    assertTrue(allUsers.contains(userProfile1))
    assertTrue(allUsers.contains(userProfile2))
    assertTrue(allUsers.contains(userProfile3))
  }

  @Test
  fun addUser_storesUser_thenUpdateUser_editsAndCanBeRetrieved() = runTest {
    val user =
        UserProfile(
            uid = "0",
            username = "alice",
            firstName = "Alice",
            lastName = "Smith",
            country = "CH",
            description = "Bio",
            dateOfBirth = LocalDate.of(1990, 1, 1),
            tags = tags)
    repository.addUser(user)

    val newUser =
        UserProfile(
            uid = "0",
            username = "alice",
            firstName = "Alice",
            lastName = "Smith",
            country = "CH",
            description = "New bio",
            dateOfBirth = LocalDate.of(1990, 1, 1),
            tags = tags)
    repository.updateUser(user.uid, newUser)

    val result1 = repository.getAllUsers()
    assertEquals(1, result1.size)
    val result2 = repository.getUser(newUser.uid)
    assertNotNull(result2)
    assertEquals("alice", result2.username)
    assertEquals("Alice", result2.firstName)
    assertEquals("Smith", result2.lastName)
    assertEquals("CH", result2.country)
    assertEquals("New bio", result2.description)
    assertEquals(LocalDate.of(1990, 1, 1), result2.dateOfBirth)
    assertEquals(tags, result2.tags)
  }

  @Test
  fun addUser_storesUserWithNoDescription_thenUpdateUser_editsAndCanBeRetrieved() = runTest {
    val user =
        UserProfile(
            uid = "1",
            username = "bob",
            firstName = "Bob",
            lastName = "Jones",
            country = "FR",
            dateOfBirth = LocalDate.of(2000, 8, 11),
            tags = tags)
    repository.addUser(user)

    val newUser =
        UserProfile(
            uid = "1",
            username = "bob",
            firstName = "Bob",
            lastName = "Jones",
            country = "FR",
            description = "Bio",
            dateOfBirth = LocalDate.of(2000, 8, 11),
            tags = tags)
    repository.updateUser(user.uid, newUser)

    val result1 = repository.getAllUsers()
    assertEquals(1, result1.size)
    val result2 = repository.getUser("1")
    assertNotNull(result2)
    assertEquals("bob", result2.username)
    assertEquals("Bob", result2.firstName)
    assertEquals("Jones", result2.lastName)
    assertEquals("FR", result2.country)
    assertEquals("Bio", result2.description)
    assertEquals(LocalDate.of(2000, 8, 11), result2.dateOfBirth)
    assertEquals(tags, result2.tags)
  }

  @Test
  fun updateUser_nonExistingUser_shouldThrowException() = runTest {
    val newUser =
        UserProfile(
            uid = "nonexistent",
            username = "john",
            firstName = "John",
            lastName = "Doe",
            country = "US",
            description = "Bio",
            dateOfBirth = LocalDate.of(1800, 10, 30),
            tags = tags)

    try {
      repository.updateUser(newUser.uid, newUser)
      fail("Expected NoSuchElementException to be thrown")
    } catch (e: NoSuchElementException) {
      assertEquals("No user found with username: nonexistent", e.message)
    }
  }

  @Test
  fun deleteUser_existingUser_shouldBeRemoved() = runTest {
    val user1 =
        UserProfile(
            uid = "0",
            username = "alice",
            firstName = "Alice",
            lastName = "Smith",
            country = "CH",
            description = "Bio",
            dateOfBirth = LocalDate.of(1990, 1, 1),
            tags = tags)
    repository.addUser(user1)
    val user2 =
        UserProfile(
            uid = "1",
            username = "john",
            firstName = "John",
            lastName = "Doe",
            country = "US",
            description = "Bio",
            dateOfBirth = LocalDate.of(1800, 10, 30),
            tags = tags)
    repository.addUser(user2)
    val result1 = repository.getAllUsers()
    assertEquals(result1.size, 2)

    repository.deleteUser(user2.uid)
    val result2 = repository.getAllUsers()
    assertEquals(result2.size, 1)
    assertEquals("alice", result2[0].username)
    assertEquals("Alice", result2[0].firstName)
    assertEquals("Smith", result2[0].lastName)
    assertEquals("Bio", result2[0].description)
    assertEquals(LocalDate.of(1990, 1, 1), result2[0].dateOfBirth)
    assertEquals(tags, result2[0].tags)
  }

  @Test
  fun deleteUser_nonExistingUser_shouldThrowException() = runTest {
    val user =
        UserProfile(
            uid = "0",
            username = "alice",
            firstName = "Alice",
            lastName = "Smith",
            country = "CH",
            description = "Bio",
            dateOfBirth = LocalDate.of(1990, 1, 1),
            tags = tags)
    repository.addUser(user)

    // Verify initial state
    val result1 = repository.getAllUsers()
    assertEquals(1, result1.size)

    // Attempt to delete a non-existing user
    try {
      repository.deleteUser("nonexistent")
      fail("Expected NoSuchElementException to be thrown")
    } catch (e: NoSuchElementException) {
      assertEquals("No user found with username: nonexistent", e.message)
    }

    // Verify the existing user is still intact
    val result2 = repository.getAllUsers()
    assertEquals(1, result2.size)
    val alice = result2[0]
    assertEquals("alice", alice.username)
    assertEquals("Alice", alice.firstName)
    assertEquals("Smith", alice.lastName)
    assertEquals("Bio", alice.description)
    assertEquals(LocalDate.of(1990, 1, 1), alice.dateOfBirth)
    assertEquals(tags, alice.tags)
  }

  @Test
  fun isUsernameUnique_usernameIsUnique_shouldReturnTrue() = runTest {
    val user =
        UserProfile(
            uid = "0",
            username = "alice",
            firstName = "Alice",
            lastName = "Smith",
            country = "CH",
            description = "Bio",
            dateOfBirth = LocalDate.of(1990, 1, 1),
            tags = tags)
    repository.addUser(user)
    val result = repository.isUsernameUnique("john")
    assertEquals(true, result)
  }

  @Test
  fun isUsernameUnique_usernameIsNotUnique_shouldReturnFalse() = runTest {
    val user =
        UserProfile(
            uid = "0",
            username = "alice",
            firstName = "Alice",
            lastName = "Smith",
            country = "CH",
            description = "Bio",
            dateOfBirth = LocalDate.of(1990, 1, 1),
            tags = tags)
    repository.addUser(user)
    val result = repository.isUsernameUnique("alice")
    assertEquals(false, result)
  }

  fun assertSameUser(user: UserProfile, result: UserProfile) {
    assertEquals(user.uid, result.uid)
    assertEquals(user.username, result.username)
    assertEquals(user.firstName, result.firstName)
    assertEquals(user.lastName, result.lastName)
    assertEquals(user.description, result.description)
    assertEquals(user.dateOfBirth, result.dateOfBirth)
    assertEquals(user.tags, result.tags)
  }

  @Test
  fun checkFollowAExistingUser() = runTest {
    repository.addUser(userProfile1)
    repository.addUser(userProfile2)
    repository.followUser(userProfile1.uid, userProfile2.uid)

    val expectedFollowingSet = userProfile1.following + userProfile2.uid
    val expectedFollowerSet = userProfile2.followers + userProfile1.uid

    val currentUSer = repository.getUser(userProfile1.uid)
    val targetUser = repository.getUser(userProfile2.uid)

    assertEquals(expectedFollowingSet, currentUSer.following)
    assertEquals(expectedFollowerSet, targetUser.followers)
  }

  @Test
  fun checkFollowANonExistingUser() = runTest {
    repository.addUser(userProfile1)
    try {
      repository.followUser(userProfile1.uid, userProfile2.uid)
      assertTrue(false)
    } catch (e: Exception) {
      assertTrue(true)
    }
  }

  @Test
  fun checkFollowAlreadyFollowUser() = runTest {
    repository.addUser(userProfile1)
    repository.addUser(userProfile2)
    repository.followUser(userProfile1.uid, userProfile2.uid)
    repository.followUser(userProfile1.uid, userProfile2.uid)

    val expectedFollowingSet = userProfile1.following + userProfile2.uid
    val expectedFollowerSet = userProfile2.followers + userProfile1.uid

    val currentUSer = repository.getUser(userProfile1.uid)
    val targetUser = repository.getUser(userProfile2.uid)

    assertEquals(expectedFollowingSet, currentUSer.following)
    assertEquals(expectedFollowerSet, targetUser.followers)
  }

  @Test
  fun checkUnfollowAExistingUser() = runTest {
    repository.addUser(userProfile1)
    repository.addUser(userProfile2)
    repository.followUser(userProfile1.uid, userProfile2.uid)
    repository.unfollowUser(userProfile1.uid, userProfile2.uid)

    val expectedFollowingSet = userProfile1.following
    val expectedFollowerSet = userProfile2.followers

    val currentUSer = repository.getUser(userProfile1.uid)
    val targetUser = repository.getUser(userProfile2.uid)

    assertEquals(expectedFollowingSet, currentUSer.following)
    assertEquals(expectedFollowerSet, targetUser.followers)
  }

  @Test
  fun checkUnfollowANonExistingUser() = runTest {
    repository.addUser(userProfile1)
    try {
      repository.unfollowUser(userProfile1.uid, userProfile2.uid)
      assertTrue(false)
    } catch (e: Exception) {
      assertTrue(true)
    }
  }

  @Test
  fun checkUnfollowNonFollowUser() = runTest {
    repository.addUser(userProfile1)
    repository.addUser(userProfile2)
    repository.unfollowUser(userProfile1.uid, userProfile2.uid)

    val expectedFollowingSet = userProfile1.following
    val expectedFollowerSet = userProfile2.followers

    val currentUSer = repository.getUser(userProfile1.uid)
    val targetUser = repository.getUser(userProfile2.uid)

    assertEquals(expectedFollowingSet, currentUSer.following)
    assertEquals(expectedFollowerSet, targetUser.followers)
  }

  @Test
  fun getFollowers_existingUser_shouldReturnFollowers() = runTest {
    repository.addUser(userProfile1)
    repository.addUser(userProfile2)
    repository.addUser(userProfile3)
    // userProfile2 and userProfile3 follow userProfile1
    repository.followUser(userProfile2.uid, userProfile1.uid)
    repository.followUser(userProfile3.uid, userProfile1.uid)

    val followers = repository.getFollowers(userProfile1.uid)
    assertEquals(2, followers.size)
    val followerUids = followers.map { it.uid }.toSet()
    assertTrue(followerUids.contains(userProfile2.uid))
    assertTrue(followerUids.contains(userProfile3.uid))
  }

  @Test
  fun getFollowers_userWithNoFollowers_shouldReturnEmptyList() = runTest {
    repository.addUser(userProfile1)
    val followers = repository.getFollowers(userProfile1.uid)
    assertTrue(followers.isEmpty())
  }

  @Test
  fun getFollowing_existingUser_shouldReturnFollowing() = runTest {
    repository.addUser(userProfile1)
    repository.addUser(userProfile2)
    repository.addUser(userProfile3)
    // userProfile1 follows userProfile2 and userProfile3
    repository.followUser(userProfile1.uid, userProfile2.uid)
    repository.followUser(userProfile1.uid, userProfile3.uid)

    val currentUser = repository.getUser(userProfile1.uid)
    val followingUids = currentUser.following
    assertEquals(2, followingUids.size)
    assertTrue(followingUids.contains(userProfile2.uid))
    assertTrue(followingUids.contains(userProfile3.uid))
  }

  @Test
  fun getFollowing_userWithNoFollowing_shouldReturnEmptyList() = runTest {
    repository.addUser(userProfile1)
    val currentUser = repository.getUser(userProfile1.uid)
    val followingUids = currentUser.following
    assertTrue(followingUids.isEmpty())
  }

  @Test
  fun getFollowingRecommendations_existingUser_shouldReturnNonFollowingUsers() = runTest {
    repository.addUser(userProfile1)
    repository.addUser(userProfile2)
    repository.addUser(userProfile3)
    // userProfile1 follows userProfile2
    repository.followUser(userProfile1.uid, userProfile2.uid)

    val allUsers = repository.getAllUsers()
    val currentUser = repository.getUser(userProfile1.uid)
    val followingUids = currentUser.following

    val recommendations =
        allUsers.filter { it.uid != currentUser.uid && !followingUids.contains(it.uid) }
    assertEquals(1, recommendations.size)
    assertEquals(userProfile3.uid, recommendations[0].uid)
  }
}
