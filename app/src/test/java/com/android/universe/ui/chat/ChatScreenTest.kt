import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.model.chat.Chat
import com.android.universe.model.chat.ChatManager
import com.android.universe.model.chat.Utils.getNewSampleMessage
import com.android.universe.ui.chat.ChatScreen
import com.android.universe.ui.chat.composable.MessageItemTestTags
import com.android.universe.ui.chat.composable.MessageItemViewModel
import com.android.universe.ui.chat.composable.SendMessageInputTestTags
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class ChatScreenTest {

  private val mockChat = mockk<Chat>(relaxed = true)

  private lateinit var mockViewModel: MessageItemViewModel
  private val sampleMessages = listOf(getNewSampleMessage())

  @Before
  fun setUp() {
    mockkObject(ChatManager)
    every { mockChat.messages } returns sampleMessages
    coEvery { ChatManager.loadChat("chat1") } returns mockChat

    mockViewModel = mockk(relaxed = true)
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
    val userID = "user1"

    composeTestRule.setContent {
      ChatScreen(
          chatID = "chat1",
          userID = userID,
          onTabSelected = {},
          messageItemViewModel = mockViewModel)
    }

    // Wait for chat to load
    composeTestRule.waitUntil(timeoutMillis = 2_000) {
      composeTestRule.onAllNodesWithText("Loading chat...").fetchSemanticsNodes().isEmpty()
    }

    // Verify MessageItem shows the username
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
    val userID = "user1"
    val newMessage = "Hi there!"

    composeTestRule.setContent {
      ChatScreen(
          chatID = "chat1",
          userID = userID,
          onTabSelected = {},
          messageItemViewModel = mockViewModel)
    }

    // Wait until chat loads
    composeTestRule.waitUntil(timeoutMillis = 2_000) {
      composeTestRule.onAllNodesWithText("Loading chat...").fetchSemanticsNodes().isEmpty()
    }

    val textField = composeTestRule.onNodeWithTag(SendMessageInputTestTags.TEXT_FIELD)
    val sendButton = composeTestRule.onNodeWithTag(SendMessageInputTestTags.SEND_BUTTON)

    // Type a new message
    textField.performClick()
    textField.performTextInput(newMessage)

    composeTestRule.waitForIdle()

    // Now button should be enabled
    sendButton.assertIsEnabled()

    // Click send
    sendButton.performClick()

    // Verify sendMessage is called on mockChat
    coVerify { mockChat.sendMessage(match { it.senderID == userID && it.message == newMessage }) }

    // Wait until the TextField is cleared
    composeTestRule.waitUntil(timeoutMillis = 2_000) {
      textField.fetchSemanticsNode().config[SemanticsProperties.EditableText]?.text?.isEmpty() ==
          true
    }
  }
}
