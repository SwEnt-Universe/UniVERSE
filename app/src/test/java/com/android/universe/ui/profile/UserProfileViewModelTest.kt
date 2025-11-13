package com.android.universe.ui.profile

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.model.user.FakeUserRepository
import com.android.universe.model.user.UserProfile
import com.android.universe.utils.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class UserProfileViewModelTest {
  private lateinit var repository: FakeUserRepository
  private lateinit var viewModel: UserProfileViewModel

  @get:Rule val mainCoroutinesRule = MainCoroutineRule()

  @Before
  fun setup() {
    repository = FakeUserRepository()
    viewModel = UserProfileViewModel(repository)
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

    advanceUntilIdle()
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
