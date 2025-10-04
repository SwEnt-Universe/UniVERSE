package com.android.universe.model.user

import java.time.LocalDate
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class FakeUserRepositoryTest {

  private lateinit var repository: FakeUserRepository

  @Before
  fun setup() {
    repository = FakeUserRepository()
  }

  @Test
  fun `getUser returns null for non-existent user`() = runTest {
    val result = repository.getUser("nonexistent")
    assertNull(result)
  }

  @Test
  fun `addUser stores user and can be retrieved`() = runTest {
    val user =
        UserProfile(
            username = "alice",
            firstName = "Alice",
            lastName = "Smith",
            country = "CH",
            description = "Bio",
            dateOfBirth = LocalDate.of(1990, 1, 1))
    repository.addUser(user)

    val result = repository.getUser("alice")
    assertNotNull(result)
    assertEquals("alice", result?.username)
    assertEquals("Alice", result?.firstName)
    assertEquals("Smith", result?.lastName)
    assertEquals("CH", result?.country)
    assertEquals("Bio", result?.description)
    assertEquals(LocalDate.of(1990, 1, 1), result?.dateOfBirth)
  }

  @Test
  fun `addUser stores a user with no description and can be retrieved`() = runTest {
    val user =
        UserProfile(
            username = "bob",
            firstName = "Bob",
            lastName = "Jones",
            country = "FR",
            dateOfBirth = LocalDate.of(2000, 8, 11))
    repository.addUser(user)

    val result = repository.getUser("bob")
    assertNotNull(result)
    assertEquals("bob", result?.username)
    assertEquals("Bob", result?.firstName)
    assertEquals("Jones", result?.lastName)
    assertNull(result?.description)
    assertEquals(LocalDate.of(2000, 8, 11), result?.dateOfBirth)
  }

  @Test
  fun `addUser stores multiple users and can all be retrieved`() = runTest {
    val user1 =
        UserProfile(
            username = "alice",
            firstName = "Alice",
            lastName = "Smith",
            country = "CH",
            description = "Bio",
            dateOfBirth = LocalDate.of(1990, 1, 1))
    val user2 =
        UserProfile(
            username = "bob",
            firstName = "Bob",
            lastName = "Jones",
            country = "FR",
            dateOfBirth = LocalDate.of(2000, 8, 11))
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
  }

  @Test
  fun `addUser stores a user then editUser edit the user and can be retrieved`() = runTest {
    val user =
        UserProfile(
            username = "alice",
            firstName = "Alice",
            lastName = "Smith",
            country = "CH",
            description = "Bio",
            dateOfBirth = LocalDate.of(1990, 1, 1))
    repository.addUser(user)

    val newUser =
        UserProfile(
            username = "alice",
            firstName = "Alice",
            lastName = "Smith",
            country = "CH",
            description = "New bio",
            dateOfBirth = LocalDate.of(1990, 1, 1))
    repository.editUser("alice", newUser)

    val result1 = repository.getAllUsers()
    assertEquals(1, result1.size)
    val result2 = repository.getUser("alice")
    assertNotNull(result2)
    assertEquals("alice", result2?.username)
    assertEquals("Alice", result2?.firstName)
    assertEquals("Smith", result2?.lastName)
    assertEquals("CH", result2?.country)
    assertEquals("New bio", result2?.description)
    assertEquals(LocalDate.of(1990, 1, 1), result2?.dateOfBirth)
  }

  @Test
  fun `addUser stores a user with no description then editUser edit the user and can be retrieved`() =
      runTest {
        val user =
            UserProfile(
                username = "bob",
                firstName = "Bob",
                lastName = "Jones",
                country = "FR",
                dateOfBirth = LocalDate.of(2000, 8, 11))
        repository.addUser(user)

        val newUser =
            UserProfile(
                username = "bob",
                firstName = "Bob",
                lastName = "Jones",
                country = "FR",
                description = "Bio",
                dateOfBirth = LocalDate.of(2000, 8, 11))
        repository.editUser("bob", newUser)

        val result1 = repository.getAllUsers()
        assertEquals(1, result1.size)
        val result2 = repository.getUser("bob")
        assertNotNull(result2)
        assertEquals("bob", result2?.username)
        assertEquals("Bob", result2?.firstName)
        assertEquals("Jones", result2?.lastName)
        assertEquals("FR", result2?.country)
        assertEquals("Bio", result2?.description)
        assertEquals(LocalDate.of(2000, 8, 11), result2?.dateOfBirth)
      }

  @Test
  fun `editUser edits a non existing user should do nothing`() = runTest {
    val newUser =
        UserProfile(
            username = "john",
            firstName = "John",
            lastName = "Doe",
            country = "US",
            description = "Bio",
            dateOfBirth = LocalDate.of(1800, 10, 30))
    repository.editUser("john", newUser)

    val result = repository.getAllUsers()
    assertEquals(0, result.size)
  }

  @Test
  fun `deleteUser deletes an existing user`() = runTest {
    val user1 =
        UserProfile(
            username = "alice",
            firstName = "Alice",
            lastName = "Smith",
            country = "CH",
            description = "Bio",
            dateOfBirth = LocalDate.of(1990, 1, 1))
    repository.addUser(user1)
    val user2 =
        UserProfile(
            username = "john",
            firstName = "John",
            lastName = "Doe",
            country = "US",
            description = "Bio",
            dateOfBirth = LocalDate.of(1800, 10, 30))
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
  }

  @Test
  fun `deleteUser do nothing when deleting a non existing user`() = runTest {
    val user =
        UserProfile(
            username = "alice",
            firstName = "Alice",
            lastName = "Smith",
            country = "CH",
            description = "Bio",
            dateOfBirth = LocalDate.of(1990, 1, 1))
    repository.addUser(user)
    val result1 = repository.getAllUsers()
    assertEquals(result1.size, 1)

    repository.deleteUser("john")
    val result2 = repository.getAllUsers()
    assertEquals(result2.size, 1)
    assertEquals("alice", result2[0].username)
    assertEquals("Alice", result2[0].firstName)
    assertEquals("Smith", result2[0].lastName)
    assertEquals("Bio", result2[0].description)
    assertEquals(LocalDate.of(1990, 1, 1), result2[0].dateOfBirth)
  }

  @Test
  fun `isUsernameUnique returns true when username is unique`() = runTest {
    val user =
        UserProfile(
            username = "alice",
            firstName = "Alice",
            lastName = "Smith",
            country = "CH",
            description = "Bio",
            dateOfBirth = LocalDate.of(1990, 1, 1))
    repository.addUser(user)
    val result = repository.isUsernameUnique("john")
    assertEquals(true, result)
  }

  @Test
  fun `isUsernameUnique returns false when username is not Unique`() = runTest {
    val user =
        UserProfile(
            username = "alice",
            firstName = "Alice",
            lastName = "Smith",
            country = "CH",
            description = "Bio",
            dateOfBirth = LocalDate.of(1990, 1, 1))
    repository.addUser(user)
    val result = repository.isUsernameUnique("alice")
    assertEquals(false, result)
  }
}
