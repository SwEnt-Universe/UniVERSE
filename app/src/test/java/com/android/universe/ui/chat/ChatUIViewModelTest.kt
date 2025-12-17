package com.android.universe.ui.chat

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.model.chat.Chat
import com.android.universe.model.chat.ChatManager
import com.android.universe.model.user.UserRepository
import com.android.universe.ui.chat.composable.ChatUIViewModel
import com.android.universe.ui.chat.composable.MAX_MESSAGE_LENGTH
import com.android.universe.utils.UserTestData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class ChatUIViewModelTest {

  // Dependencies
  private val mockChat = mockk<Chat>(relaxed = true)
  private val mockUserRepository = mockk<UserRepository>(relaxed = true)

  private lateinit var viewModel: ChatUIViewModel

  private val testChatId = "chat_id_1"
  private val testUser = UserTestData.Alice
  private val otherUser = UserTestData.Bob
  private val testUserId = testUser.uid
  private val otherUserId = otherUser.uid
  private val otherUserName = otherUser.username

  // Coroutine testing setup
  private val testDispatcher = StandardTestDispatcher()
  private val testScope = TestScope(testDispatcher)

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)

    // Mock static dependency
    mockkObject(ChatManager)

    // --- Mock User Objects ---

    // Create a mock user for 'otherUserId' that explicitly has the expected username

    // Stub dependencies to be available for ViewModel initialization
    coEvery { ChatManager.loadChat(testChatId) } returns mockChat

    // Use UserTestData.Alice for the current user ID
    coEvery { mockUserRepository.getUser(testUserId) } returns UserTestData.Alice

    // Use the mock user for the other user ID
    coEvery { mockUserRepository.getUser(otherUserId) } returns UserTestData.Bob
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    unmockkAll()
  }

  // --- State Initialization Tests ---

  @Test
  fun `initially, uiState is Loading`() =
      testScope.runTest {
        // ViewModel is instantiated in a way that loadChat is launched immediately
        viewModel = ChatUIViewModel(testChatId, testUserId, mockUserRepository)

        // Before coroutine runs, state should be loading
        assertEquals(ChatUIViewModel.ChatUiState.Loading, viewModel.uiState.value)
      }

  @Test
  fun `loadChat success sets uiState to Success`() =
      testScope.runTest {
        // Arrange: Successful loadChat is stubbed in setUp
        viewModel = ChatUIViewModel(testChatId, testUserId, mockUserRepository)

        // Act: Run the coroutine launched in init block
        advanceUntilIdle()

        // Assert: State should be Success with the mock chat object
        assertTrue(viewModel.uiState.value is ChatUIViewModel.ChatUiState.Success)
        assertEquals(
            mockChat, (viewModel.uiState.value as ChatUIViewModel.ChatUiState.Success).chat)
      }

  @Test
  fun `loadChat failure sets uiState to Error`() =
      testScope.runTest {
        // Arrange: Stub loadChat to throw an exception
        coEvery { ChatManager.loadChat(testChatId) } throws NoSuchElementException()
        viewModel = ChatUIViewModel(testChatId, testUserId, mockUserRepository)

        // Act: Run the coroutine launched in init block
        advanceUntilIdle()

        // Assert: State should be Error
        assertTrue(viewModel.uiState.value is ChatUIViewModel.ChatUiState.Error)
        assertEquals(
            "Failed to load chat",
            (viewModel.uiState.value as ChatUIViewModel.ChatUiState.Error).errorMsg)
      }

  // --- Input Handling Tests ---

  @Test
  fun `onInput updates messageText value`() =
      testScope.runTest {
        viewModel = ChatUIViewModel(testChatId, testUserId, mockUserRepository)
        val text = "Test message"

        viewModel.onInput(text)

        assertEquals(text, viewModel.messageText.value)
      }

  @Test
  fun `onInput limits message length to MAX_MESSAGE_LENGTH`() =
      testScope.runTest {
        viewModel = ChatUIViewModel(testChatId, testUserId, mockUserRepository)
        val longText = "x".repeat(MAX_MESSAGE_LENGTH + 50)
        val expectedText = "x".repeat(MAX_MESSAGE_LENGTH)

        viewModel.onInput(longText)

        assertEquals(expectedText, viewModel.messageText.value)
      }

  // --- Message Sending Tests ---

  @Test
  fun `sendMessage calls chatSendMessage and does not clear text (as per code)`() =
      testScope.runTest {
        // Arrange: Load chat successfully
        viewModel = ChatUIViewModel(testChatId, testUserId, mockUserRepository)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is ChatUIViewModel.ChatUiState.Success)

        val messageText = "Sending this message"
        viewModel.onInput(messageText)

        // Act
        viewModel.sendMessage()
        advanceUntilIdle()

        // Assert: Verify that sendMessage was called on the Chat object with correct details
        coVerify {
          mockChat.sendMessage(match { it.senderID == testUserId && it.message == messageText })
        }

        // Assert: Verify messageText is cleared
        assertEquals("", viewModel.messageText.value)
      }

  @Test
  fun `sendMessage does nothing if message is blank`() =
      testScope.runTest {
        // Arrange: Load chat successfully and set blank text
        viewModel = ChatUIViewModel(testChatId, testUserId, mockUserRepository)
        advanceUntilIdle()

        viewModel.onInput("   ") // Text contains only whitespace

        // Act
        viewModel.sendMessage()

        // Assert: No interaction with the mock chat
        coVerify(exactly = 0) { mockChat.sendMessage(any()) }
      }

  @Test
  fun `sendMessage does nothing if uiState is Loading`() =
      testScope.runTest {
        // Arrange: Do not advance coroutines, so uiState remains Loading
        viewModel = ChatUIViewModel(testChatId, testUserId, mockUserRepository)

        viewModel.onInput("Valid message")

        // Act
        viewModel.sendMessage()

        // Assert: No interaction with the mock chat
        coVerify(exactly = 0) { mockChat.sendMessage(any()) }
      }

  // --- User Name Retrieval Tests ---

  @Test
  fun `getUserName successfully loads and caches username`() =
      testScope.runTest {
        // Arrange
        viewModel = ChatUIViewModel(testChatId, testUserId, mockUserRepository)

        // Act 1: Get flow for user B
        val flowB = viewModel.getUserName(otherUserId)

        // Assert 1 (Initial value): Flow should emit placeholder
        assertEquals("...", flowB.first())

        // Act 2: Run the coroutine to fetch the name
        advanceUntilIdle()

        // Assert 2 (Success value): Flow should emit the fetched name
        assertEquals(otherUserName, flowB.first())

        // Assert 3 (Caching): Verify repository was called exactly once
        coVerify(exactly = 1) { mockUserRepository.getUser(otherUserId) }

        // Act 3: Call getUserName again for the same user (cache hit)
        viewModel.getUserName(otherUserId)

        // Assert 4 (Caching): Verify repository was NOT called again
        coVerify(exactly = 1) { mockUserRepository.getUser(otherUserId) }
      }

  @Test
  fun `getUserName handles user not found gracefully`() =
      testScope.runTest {
        // Arrange: Stub getUser to throw for this specific ID
        val deletedId = "deleted_user_id"
        coEvery { mockUserRepository.getUser(deletedId) } throws NoSuchElementException("Not found")
        viewModel = ChatUIViewModel(testChatId, testUserId, mockUserRepository)

        // Act 1: Get flow
        val flow = viewModel.getUserName(deletedId)

        // Assert 1 (Initial value): Flow should emit placeholder
        assertEquals("...", flow.first())

        // Act 2: Run the coroutine to fetch the name
        advanceUntilIdle()

        // Assert 2 (Failure value): Flow should emit "deleted"
        assertEquals("deleted", flow.first())
      }
}
