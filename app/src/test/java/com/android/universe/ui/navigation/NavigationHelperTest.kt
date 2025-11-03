package com.android.universe.ui.navigation

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.model.user.UserRepository
import com.google.firebase.auth.FirebaseUser
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationHelperTest {

  private lateinit var mockUserRepository: UserRepository
  private lateinit var mockUser: FirebaseUser

  @Before
  fun setUp() {
    mockUserRepository = mockk(relaxed = true)
    mockUser = mockk(relaxed = true)
  }

  @Test
  fun `returns SignIn when user is null`() {
    val result = resolveUserDestinationScreen(user = null, userRepository = mockUserRepository)

    assertEquals(NavigationScreens.SignIn, result)
  }

  @Test
  fun `returns Map when user is anonymous`() {
    every { mockUser.isAnonymous } returns true

    val result = resolveUserDestinationScreen(user = mockUser, userRepository = mockUserRepository)

    assertEquals(NavigationScreens.Map, result)
  }

  @Test
  fun `returns Map when user has profile`() {
    every { mockUser.isAnonymous } returns false
    every { mockUser.uid } returns "uid123"

    // Mock getUser to succeed (simulates existing profile)
    coEvery { mockUserRepository.getUser("uid123") } returns mockk()

    val result = resolveUserDestinationScreen(user = mockUser, userRepository = mockUserRepository)

    assertEquals(NavigationScreens.Map, result)
  }

  @Test
  fun `returns AddProfile when user does not have profile`() {
    every { mockUser.isAnonymous } returns false
    every { mockUser.uid } returns "uid123"

    // Mock getUser to throw exception (simulates missing profile)
    coEvery { mockUserRepository.getUser("uid123") } throws IllegalArgumentException()

    val result = resolveUserDestinationScreen(user = mockUser, userRepository = mockUserRepository)

    assertEquals(NavigationScreens.AddProfile, result)
  }
}
