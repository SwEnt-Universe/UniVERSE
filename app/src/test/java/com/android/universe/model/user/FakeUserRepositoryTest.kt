package com.android.universe.model.user

import com.android.universe.model.Tag
import java.time.LocalDate
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.fail
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class FakeUserRepositoryTest {

  private lateinit var repository: FakeUserRepository
  private val tag = Tag.METAL
  private val tags = setOf(tag)

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
    val user = sampleUsers[0]
    repository.addUser(user)

    val result = repository.getUser(user.uid)
    assertNotNull(result)
    assertSameUser(user, result)
  }

  @Test
  fun addUser_storesMultipleUsers_andAllCanBeRetrieved() = runTest {
    sampleUsers.forEach { repository.addUser(it) }
    // can retrieve each user individually
    sampleUsers.forEach { user ->
      val result = repository.getUser(user.uid)
      assertNotNull(result)
      assertSameUser(user, result)
    }

    // all users returned by getAllUsers
    val allUsers = repository.getAllUsers()
    assertEquals(sampleUsers.size, allUsers.size)
    sampleUsers.forEach { user ->
      val result = allUsers.find { it.uid == user.uid }
      assertNotNull(result)
      assertSameUser(user, result!!)
    }
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
}
