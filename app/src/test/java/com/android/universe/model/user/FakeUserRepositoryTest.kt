package com.android.universe.model.user

import com.android.universe.model.Tag
import java.time.LocalDate
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.fail
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class FakeUserRepositoryTest {

  private lateinit var repository: FakeUserRepository
  private val tag = Tag(name = "Metal")
  private val tags = listOf<Tag>(tag)

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
      assertEquals("No user found with username: nonexistent", e.message)
    }
  }

  @Test
  fun addUser_storesUser_andCanBeRetrieved() = runTest {
    val user =
        UserProfile(
            username = "alice",
            firstName = "Alice",
            lastName = "Smith",
            country = "CH",
            description = "Bio",
            dateOfBirth = LocalDate.of(1990, 1, 1),
            tags = tags)
    repository.addUser(user)

    val result = repository.getUser("alice")
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
    val user =
        UserProfile(
            username = "bob",
            firstName = "Bob",
            lastName = "Jones",
            country = "FR",
            dateOfBirth = LocalDate.of(2000, 8, 11),
            tags = tags)
    repository.addUser(user)

    val result = repository.getUser("bob")
    assertNotNull(result)
    assertEquals("bob", result.username)
    assertEquals("Bob", result.firstName)
    assertEquals("Jones", result.lastName)
    assertNull(result.description)
    assertEquals(LocalDate.of(2000, 8, 11), result.dateOfBirth)
    assertEquals(tags, result.tags)
  }

  @Test
  fun addUser_storesMultipleUsers_andAllCanBeRetrieved() = runTest {
    val user1 =
        UserProfile(
            username = "alice",
            firstName = "Alice",
            lastName = "Smith",
            country = "CH",
            description = "Bio",
            dateOfBirth = LocalDate.of(1990, 1, 1),
            tags = tags)
    val user2 =
        UserProfile(
            username = "bob",
            firstName = "Bob",
            lastName = "Jones",
            country = "FR",
            dateOfBirth = LocalDate.of(2000, 8, 11),
            tags = tags)
    repository.addUser(user1)
    repository.addUser(user2)

    val result = repository.getAllUsers()
    assertEquals(2, result.size)
    assertEquals("alice", result[0].username)
    assertEquals("bob", result[1].username)
    assertEquals("Alice", result[0].firstName)
    assertEquals("Bob", result[1].firstName)
    assertEquals("Smith", result[0].lastName)
    assertEquals("Jones", result[1].lastName)
    assertEquals("Bio", result[0].description)
    assertNull(result[1].description)
    assertEquals(LocalDate.of(1990, 1, 1), result[0].dateOfBirth)
    assertEquals(LocalDate.of(2000, 8, 11), result[1].dateOfBirth)
    assertEquals(tags, result[0].tags)
    assertEquals(tags, result[1].tags)
  }

  @Test
  fun addUser_storesUser_thenUpdateUser_editsAndCanBeRetrieved() = runTest {
    val user =
        UserProfile(
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
            username = "alice",
            firstName = "Alice",
            lastName = "Smith",
            country = "CH",
            description = "New bio",
            dateOfBirth = LocalDate.of(1990, 1, 1),
            tags = tags)
    repository.updateUser("alice", newUser)

    val result1 = repository.getAllUsers()
    assertEquals(1, result1.size)
    val result2 = repository.getUser("alice")
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
            username = "bob",
            firstName = "Bob",
            lastName = "Jones",
            country = "FR",
            dateOfBirth = LocalDate.of(2000, 8, 11),
            tags = tags)
    repository.addUser(user)

    val newUser =
        UserProfile(
            username = "bob",
            firstName = "Bob",
            lastName = "Jones",
            country = "FR",
            description = "Bio",
            dateOfBirth = LocalDate.of(2000, 8, 11),
            tags = tags)
    repository.updateUser("bob", newUser)

    val result1 = repository.getAllUsers()
    assertEquals(1, result1.size)
    val result2 = repository.getUser("bob")
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
            username = "john",
            firstName = "John",
            lastName = "Doe",
            country = "US",
            description = "Bio",
            dateOfBirth = LocalDate.of(1800, 10, 30),
            tags = tags)

    try {
      repository.updateUser("john", newUser)
      fail("Expected NoSuchElementException to be thrown")
    } catch (e: NoSuchElementException) {
      assertEquals("No user found with username: john", e.message)
    }
  }

  @Test
  fun deleteUser_existingUser_shouldBeRemoved() = runTest {
    val user1 =
        UserProfile(
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

    repository.deleteUser("john")
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
      repository.deleteUser("john")
      fail("Expected NoSuchElementException to be thrown")
    } catch (e: NoSuchElementException) {
      assertEquals("No user found with username: john", e.message)
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
}
