package com.android.universe.ui.chat

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.model.user.UserRepository
import com.android.universe.ui.chat.composable.MessageItemViewModel
import com.android.universe.utils.UserTestData
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class MessageItemViewModelTest {

  private lateinit var userRepository: UserRepository
  private lateinit var viewModel: MessageItemViewModel
  private val testDispatcher = StandardTestDispatcher()
  private val testScope = TestScope(testDispatcher)

  @Before
  fun setUp() {
    userRepository = mockk()
    viewModel = MessageItemViewModel(userRepository)
  }

  @After
  fun tearDown() {
    unmockkAll()
  }

  @Test
  fun `getUserName returns username when user exists`() =
      testScope.runTest {
        val testUser = UserTestData.Alice
        val userID = testUser.uid

        // Mock repository
        coEvery { userRepository.getUser(userID) } returns testUser

        // Act
        val flow = viewModel.getUserName(userID)
        advanceUntilIdle() // let coroutine complete

        // Assert
        assertEquals(testUser.username, flow.first())
      }

  @Test
  fun `getUserName returns deleted when user does not exist`() =
      testScope.runTest {
        val userId = "456"

        // Mock repository to throw exception
        coEvery { userRepository.getUser(userId) } throws IllegalArgumentException()

        // Act
        val flow = viewModel.getUserName(userId)
        advanceUntilIdle() // let coroutine complete

        // Assert
        assertEquals("deleted", flow.first())
      }
}
