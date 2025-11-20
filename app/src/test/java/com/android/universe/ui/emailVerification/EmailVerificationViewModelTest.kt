package com.android.universe.ui.emailVerification

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.utils.MainCoroutineRule
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseUser
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val TEST_EMAIL = "test@epfl.ch"

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class EmailVerificationViewModelTest {
  private lateinit var mockUser: FirebaseUser
  private lateinit var viewModel: EmailVerificationViewModel
  private lateinit var sendEmailTask: Task<Void>
  private lateinit var reloadUserTask: Task<Void>

  @get:Rule val mainCoroutineRule = MainCoroutineRule()

  @Before
  fun setup() {
    mockUser = mockk(relaxed = true)
    viewModel = EmailVerificationViewModel(iODispatcher = UnconfinedTestDispatcher())
    sendEmailTask = Tasks.forResult(null)
    reloadUserTask = Tasks.forResult(null)
    every { mockUser.email } returns TEST_EMAIL
    every { mockUser.sendEmailVerification() } returns sendEmailTask
  }

  @After
  fun tearDown() {
    unmockkAll()
  }

  @Test
  fun `initial email is loaded from FirebaseUser`() = runTest {
    every { mockUser.isEmailVerified } returns false
    viewModel.sendEmailVerification(mockUser)
    advanceUntilIdle() // run all coroutines in the test dispatcher
    assertEquals(TEST_EMAIL, viewModel.uiState.value.email)
  }

  @Test
  fun `email already verified sets emailVerified true`() = runTest {
    every { mockUser.isEmailVerified } returns true

    viewModel.sendEmailVerification(mockUser)
    advanceUntilIdle()

    assertTrue(viewModel.uiState.value.emailVerified)
    assertFalse(viewModel.uiState.value.sendEmailFailed)
  }

  @Test
  fun `sendEmailVerification failure sets sendEmailFailed and resets cooldown`() = runTest {
    every { mockUser.isEmailVerified } returns false
    every { mockUser.sendEmailVerification() } returns Tasks.forException(Exception())

    viewModel.sendEmailVerification(mockUser)
    advanceUntilIdle()

    assertTrue(viewModel.uiState.value.sendEmailFailed)
    assertEquals(0, viewModel.uiState.value.countDown)
  }

  @Test
  fun `countDown decreases over time`() = runTest {
    val isVerifiedSlot = mutableListOf(false, false, false, true, true)
    every { mockUser.isEmailVerified } answers { isVerifiedSlot.removeFirst() }
    coEvery { mockUser.reload() } returns reloadUserTask

    viewModel.sendEmailVerification(mockUser)

    val initialCountdown = viewModel.uiState.value.countDown
    assertEquals(COOLDOWN, initialCountdown)

    advanceUntilIdle()
    assertEquals(COOLDOWN - 2, viewModel.uiState.value.countDown)
  }

  @Test
  fun `email becomes verified during polling`() = runTest {
    // false first, then true
    val isVerifiedSlot = mutableListOf(false, false, true, true)
    every { mockUser.isEmailVerified } answers { isVerifiedSlot.removeFirst() }
    coEvery { mockUser.reload() } returns reloadUserTask

    viewModel.sendEmailVerification(mockUser)
    // Advance time to pass the delay inside the polling loop
    advanceUntilIdle()
    assertTrue(viewModel.uiState.value.emailVerified)
  }

  @Test
  fun `resendEnabled is true when countdown is zero`() = runTest {
    every { mockUser.isEmailVerified } returns true

    val viewModel = EmailVerificationViewModel()
    viewModel.sendEmailVerification(mockUser)

    // countdown is initially COOLDOWN
    assertFalse(viewModel.uiState.value.resendEnabled)

    // advance time until countdown reaches 0
    repeat(COOLDOWN) { viewModel.countDown() }

    assertTrue(viewModel.uiState.value.resendEnabled)
  }
}
