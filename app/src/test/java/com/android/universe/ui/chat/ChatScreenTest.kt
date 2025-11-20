package com.android.universe.ui.chat

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.model.chat.Chat
import com.android.universe.model.chat.Utils.getNewSampleMessage
import com.android.universe.ui.chat.ChatScreenTestTags.LOADING
import com.android.universe.ui.chat.composable.ChatUIViewModel
import com.android.universe.ui.chat.composable.MessageItemTestTags
import com.android.universe.ui.chat.composable.SendMessageInputTestTags
import com.android.universe.utils.setContentWithStubBackdrop
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChatScreenTest {

  // Use the concrete ViewModel class for the mock
  private lateinit var mockViewModel: ChatUIViewModel
  private lateinit var mockChat: Chat
  private val sampleMessages = listOf(getNewSampleMessage())

  // Flows to control the state of the Composable
  private lateinit var mockUiStateFlow: MutableStateFlow<ChatUIViewModel.ChatUiState>
  private lateinit var mockMessageTextFlow: MutableStateFlow<String>

  private val TEST_CHAT_ID = "chat1"
  private val TEST_USER_ID = "user1"

  @Before
  fun setUp() {
    mockChat = mockk<Chat>(relaxed = true)
    every { mockChat.messages } returns sampleMessages

    mockUiStateFlow = MutableStateFlow(ChatUIViewModel.ChatUiState.Loading)
    mockMessageTextFlow = MutableStateFlow("")
    mockViewModel = mockk(relaxed = true)

    every { mockViewModel.uiState } returns mockUiStateFlow
    every { mockViewModel.messageText } returns mockMessageTextFlow

    val userFlow = MutableStateFlow("Alice")
    every { mockViewModel.getUserName(any()) } returns userFlow
  }

  @After
  fun tearDown() {
    unmockkAll()
  }

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun chatScreen_displaysMessageListAndInput() {
    composeTestRule.setContentWithStubBackdrop {
      // Direct injection of the mock ViewModel, bypassing the factory
      ChatScreen(
          chatID = TEST_CHAT_ID, userID = TEST_USER_ID, onTabSelected = {}, vm = mockViewModel)
    }

    // Initially shows loading state (from mockUiStateFlow)
    composeTestRule.onNodeWithTag(LOADING).assertIsDisplayed()

    // Simulate successful data load from the ViewModel
    composeTestRule.runOnUiThread {
      mockUiStateFlow.value = ChatUIViewModel.ChatUiState.Success(mockChat)
    }
    composeTestRule.waitForIdle()

    // Verify MessageItem shows the username (from mockViewModel.getUserName)
    composeTestRule
        .onNodeWithTag(MessageItemTestTags.USERNAME)
        .assertIsDisplayed()
        .assert(hasText("Alice"))

    // Verify message text
    composeTestRule
        .onNodeWithTag(MessageItemTestTags.MESSAGE_TEXT)
        .assertIsDisplayed()
        .assert(hasText(sampleMessages.first().message))

    // Verify SendMessageInput exists
    composeTestRule.onNodeWithTag(SendMessageInputTestTags.TEXT_FIELD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SendMessageInputTestTags.SEND_BUTTON).assertIsDisplayed()
  }

  @Test
  fun chatScreen_typingAndSendMessage() {
    val newMessage = "Hi there!"

    composeTestRule.setContentWithStubBackdrop {
      ChatScreen(
          chatID = TEST_CHAT_ID, userID = TEST_USER_ID, onTabSelected = {}, vm = mockViewModel)
    }

    // Simulate successful data load
    composeTestRule.runOnUiThread {
      mockUiStateFlow.value = ChatUIViewModel.ChatUiState.Success(mockChat)
    }
    composeTestRule.waitForIdle()

    val textField = composeTestRule.onNodeWithTag(SendMessageInputTestTags.TEXT_FIELD)
    val sendButton = composeTestRule.onNodeWithTag(SendMessageInputTestTags.SEND_BUTTON)

    // Setup the mock to simulate the typing process: onInput updates the text flow
    every { mockViewModel.onInput(any()) } answers
        {
          // The Composable calls onInput, which updates the flow to reflect the text change.
          mockMessageTextFlow.value = firstArg<String>()
        }

    // Type a new message
    textField.performClick()
    textField.performTextInput(newMessage)

    composeTestRule.waitForIdle()

    // Now button should be enabled
    sendButton.assertIsEnabled()

    // Click send
    sendButton.performClick()

    // Verify the action is delegated to the ViewModel
    verify { mockViewModel.sendMessage() }
  }
}
