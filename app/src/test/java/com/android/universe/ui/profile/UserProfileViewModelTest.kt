package com.android.universe.ui.profile

import com.android.universe.model.user.FakeUserRepository
import com.android.universe.model.user.UserProfile
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UserProfileViewModelTest {
  private lateinit var repository: FakeUserRepository
  private lateinit var viewModel: UserProfileViewModel

  private val testDispatcher = StandardTestDispatcher()

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)

    repository = FakeUserRepository()
    viewModel = UserProfileViewModel(repository)
  }

  @After
  fun tearDown() {
    // 👇 Always reset to avoid leaking the test dispatcher
    Dispatchers.resetMain()
  }

  @Test
  fun loadsUserDataCorrectlyForExistingUser() = runTest {
    val profile =
        UserProfile(
            uid = "0",
            username = "alice",
            firstName = "Alice",
            lastName = "Smith",
            country = "CH",
            description = "Hi, I'm Alice.",
            dateOfBirth = LocalDate.of(1990, 1, 1),
            tags = emptySet())

    repository.addUser(profile)

    viewModel.loadUser(profile.uid)

    testDispatcher.scheduler.advanceUntilIdle()
    val state = viewModel.userState.value
    assertEquals("alice", state.userProfile.username)
    assertEquals("Alice", state.userProfile.firstName)
    assertEquals("Smith", state.userProfile.lastName)
    assertEquals("Hi, I'm Alice.", state.userProfile.description)
    assertEquals("CH", state.userProfile.country)
    assertEquals(1990, state.userProfile.dateOfBirth.year)
    assertEquals(1, state.userProfile.dateOfBirth.month.value)
    assertEquals(1, state.userProfile.dateOfBirth.dayOfMonth)
    assertNull(state.errorMsg)
  }
}
