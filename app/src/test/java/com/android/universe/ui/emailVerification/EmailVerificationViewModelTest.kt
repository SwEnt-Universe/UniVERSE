package com.android.universe.ui.emailVerification

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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

private const val TEST_EMAIL = "test@epfl.ch"

@OptIn(ExperimentalCoroutinesApi::class)
class EmailVerificationViewModelTest {
  private lateinit var mockUser: FirebaseUser
  private lateinit var sendEmailTask: Task<Void>
  private lateinit var reloadUserTask: Task<Void>
  private val testDispatcher = StandardTestDispatcher()

  @Before
  fun setup() {
    mockUser = mockk(relaxed = true)
    sendEmailTask = Tasks.forResult(null)
    reloadUserTask = Tasks.forResult(null)
    Dispatchers.setMain(testDispatcher)
    every { mockUser.email } returns TEST_EMAIL
    every { mockUser.sendEmailVerification() } returns sendEmailTask
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    unmockkAll()
  }

  @Test
  fun `initial email is loaded from FirebaseUser`() = runTest {
    every { mockUser.isEmailVerified } returns false
    val vm = EmailVerificationViewModel()
    vm.sendEmailVerification(mockUser)
    advanceUntilIdle() // run all coroutines in the test dispatcher
    assertEquals(TEST_EMAIL, vm.uiState.value.email)
  }

  @Test
  fun `email already verified sets emailVerified true`() = runTest {
    every { mockUser.isEmailVerified } returns true

    val vm = EmailVerificationViewModel()
    vm.sendEmailVerification(mockUser)
    advanceUntilIdle()

    assertTrue(vm.uiState.value.emailVerified)
    assertFalse(vm.uiState.value.sendEmailFailed)
  }

  @Test
  fun `sendEmailVerification failure sets sendEmailFailed and resets cooldown`() = runTest {
    every { mockUser.isEmailVerified } returns false
    every { mockUser.sendEmailVerification() } returns Tasks.forException(Exception())

    val vm = EmailVerificationViewModel()
    vm.sendEmailVerification(mockUser)
    advanceUntilIdle()

    assertTrue(vm.uiState.value.sendEmailFailed)
    assertEquals(0, vm.uiState.value.countDown)
  }

  @Test
  fun `countDown decreases over time`() = runTest {
    val isVerifiedSlot = mutableListOf(false, false, false, true, true)
    every { mockUser.isEmailVerified } answers { isVerifiedSlot.removeFirst() }
    coEvery { mockUser.reload() } returns reloadUserTask

    val vm = EmailVerificationViewModel()
    vm.sendEmailVerification(mockUser)

    val initialCountdown = vm.uiState.value.countDown
    assertEquals(COOLDOWN, initialCountdown)

    advanceUntilIdle()
    assertEquals(COOLDOWN - 2, vm.uiState.value.countDown)
  }

  @Test
  fun `email becomes verified during polling`() = runTest {
    // false first, then true
    val isVerifiedSlot = mutableListOf(false, false, true, true)
    every { mockUser.isEmailVerified } answers { isVerifiedSlot.removeFirst() }
    coEvery { mockUser.reload() } returns reloadUserTask

    val vm = EmailVerificationViewModel()
    vm.sendEmailVerification(mockUser)
    // Advance time to pass the delay inside the polling loop
    advanceUntilIdle()
    assertTrue(vm.uiState.value.emailVerified)
  }

  @Test
  fun `resendEnabled is true when countdown is zero`() = runTest {
    every { mockUser.isEmailVerified } returns true

    val vm = EmailVerificationViewModel()
    vm.sendEmailVerification(mockUser)

    // countdown is initially COOLDOWN
    assertFalse(vm.uiState.value.resendEnabled)

    // advance time until countdown reaches 0
    repeat(COOLDOWN) { vm.countDown() }

    assertTrue(vm.uiState.value.resendEnabled)
  }
}
