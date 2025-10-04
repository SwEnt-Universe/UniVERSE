package com.android.universe.ui.user

import com.android.universe.model.user.UserProfile
import java.time.LocalDate
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UserViewModelTest {

  private val testDispatcher: TestDispatcher = StandardTestDispatcher()

  private lateinit var viewModel: UserViewModel

  @Before
  fun setup() {
    // Set the main dispatcher to our test dispatcher
    Dispatchers.setMain(testDispatcher)
    viewModel = UserViewModel()
  }

  // Add a teardown method to clean up
  @After
  fun tearDown() {
    Dispatchers.resetMain() // Reset the main dispatcher to the original one
  }

  @Test
  fun `loadUsers updates users StateFlow`() = runTest {
    // Advance until all coroutines finish
    testDispatcher.scheduler.advanceUntilIdle() // wait for coroutine

    val users = viewModel.users.first()
    assertTrue(users.isNotEmpty())
  }

  @Test
  fun `addUser adds new user and refreshes StateFlow`() = runTest {
    val newUser =
        UserProfile(
            username = "test_user",
            firstName = "Test",
            lastName = "User",
            country = "CH",
            description = "Just testing",
            dateOfBirth = LocalDate.of(2000, 1, 1))

    viewModel.addUser(newUser)
    testDispatcher.scheduler.advanceUntilIdle() // wait for coroutine

    val users = viewModel.users.first()
    assertTrue(users.any { it.username == "test_user" })
  }

  @Test
  fun `editUser updates existing user and refreshes StateFlow`() = runTest {
    val editedUser =
        UserProfile(
            username = "alice",
            firstName = "Alice",
            lastName = "Smith",
            country = "CH",
            description = "Edited description",
            dateOfBirth = LocalDate.of(1990, 1, 1))

    viewModel.editUser("alice", editedUser)
    testDispatcher.scheduler.advanceUntilIdle() // wait for coroutine

    val users = viewModel.users.first()
    val alice = users.first { it.username == "alice" }
    assertEquals("Edited description", alice.description)
  }

  @Test
  fun `deleteUser removes user and refreshes StateFlow`() = runTest {
    viewModel.deleteUser("bob")
    testDispatcher.scheduler.advanceUntilIdle() // wait for coroutine

    val users = viewModel.users.first()
    assertFalse(users.any { it.username == "bob" })
  }

  @Test
  fun `getUser returns correct user via callback`() = runTest {
    var result: UserProfile? = null
    viewModel.getUser("alice") { result = it }
    testDispatcher.scheduler.advanceUntilIdle() // wait for coroutine

    val alice = result
    assertNotNull(alice)
    assertEquals("alice", alice?.username)
  }

  @Test
  fun `isUsernameUnique returns correct value via callback`() = runTest {
    var isUnique: Boolean? = null
    viewModel.isUsernameUnique("alice") { isUnique = it }
    testDispatcher.scheduler.advanceUntilIdle() // wait for coroutine
    assertFalse(isUnique!!)

    viewModel.isUsernameUnique("new_user") { isUnique = it }
    testDispatcher.scheduler.advanceUntilIdle() // wait for coroutine
    assertTrue(isUnique!!)
  }
}
