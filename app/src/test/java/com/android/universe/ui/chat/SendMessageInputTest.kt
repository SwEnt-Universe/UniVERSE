package com.android.universe.ui.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.ui.chat.composable.ChatUIViewModel
import com.android.universe.ui.chat.composable.SendMessageInput
import com.android.universe.ui.chat.composable.SendMessageInputTestTags
import com.android.universe.utils.setContentWithStubBackdrop
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SendMessageInputTest {

  // Mock the new ViewModel
  private lateinit var mockViewModel: ChatUIViewModel
  // Create a real MutableStateFlow to control the text state for the composable
  private val messageFlow = MutableStateFlow("")

  @Before
  fun setUp() {
    mockViewModel = mockk(relaxed = true)

    every { mockViewModel.messageText } returns messageFlow
    justRun { mockViewModel.onInput(any<String>()) }
  }

  @After
  fun tearDown() {
    unmockkAll()
  }

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun sendMessageInput_sendButtonControl() {
    composeTestRule.setContentWithStubBackdrop {
      Column(modifier = Modifier.fillMaxSize()) { SendMessageInput(vm = mockViewModel) }
    }

    val textField = composeTestRule.onNodeWithTag(SendMessageInputTestTags.TEXT_FIELD)
    val sendButton = composeTestRule.onNodeWithTag(SendMessageInputTestTags.SEND_BUTTON)

    val testMessage = "Hello"

    // 1. Initially disabled
    sendButton.assertIsNotEnabled()

    // 2. Simulate text input by updating the ViewModel's state flow
    // We update the flow directly to simulate the ViewModel receiving the text.
    messageFlow.value = testMessage
    composeTestRule.waitForIdle()

    // 3. Now button should be enabled
    sendButton.assertIsEnabled()

    // 4. Click send
    sendButton.performClick()

    // 5. Verify the ViewModel's sendMessage function was called
    verify { mockViewModel.sendMessage() }
  }

  @Test
  fun sendMessageInput_typingCallsOnInput() {
    val testMessage = "Test"

    composeTestRule.setContentWithStubBackdrop { SendMessageInput(vm = mockViewModel) }

    val textField = composeTestRule.onNodeWithTag(SendMessageInputTestTags.TEXT_FIELD)

    // Type text
    textField.performTextInput(testMessage)

    verify { mockViewModel.onInput(testMessage) }
  }
}
