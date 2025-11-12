import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.model.chat.Chat
import com.android.universe.ui.chat.composable.MAX_MESSAGE_LENGTH
import com.android.universe.ui.chat.composable.SendMessageInput
import com.android.universe.ui.chat.composable.SendMessageInputTestTags
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class SendMessageInputTest {

  private lateinit var mockChat: Chat

  @Before
  fun setUp() {
    mockChat = mockk(relaxed = true)
  }

  @After
  fun tearDown() {
    unmockkAll()
  }

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun sendMessageInput_typingAndSend() {
    val userID = "user1"
    val testMessage = "Hello World"

    composeTestRule.setContent {
      Column(modifier = Modifier.fillMaxSize()) {
        SendMessageInput(chat = mockChat, userID = userID)
      }
    }

    val textField = composeTestRule.onNodeWithTag(SendMessageInputTestTags.TEXT_FIELD)
    val sendButton = composeTestRule.onNodeWithTag(SendMessageInputTestTags.SEND_BUTTON)

    // Initially disabled
    sendButton.assertIsNotEnabled()

    // Type message
    textField.performClick() // focus required
    textField.performTextInput(testMessage)

    // Wait for Compose to process state and recomposition
    composeTestRule.waitForIdle()

    // Now button should be enabled
    sendButton.assertIsEnabled()

    // Click send
    sendButton.performClick()

    // Verify message sent, ignore timestamp
    coVerify { mockChat.sendMessage(match { it.senderID == userID && it.message == testMessage }) }
  }

  @Test
  fun sendMessageInput_limitsMaxLength() {
    val userID = "user1"
    val longText = "x".repeat(MAX_MESSAGE_LENGTH + 50)

    composeTestRule.setContent { SendMessageInput(chat = mockChat, userID = userID) }

    val textField = composeTestRule.onNodeWithTag(SendMessageInputTestTags.TEXT_FIELD)
    textField.performTextInput(longText)

    textField.assertTextEquals("x".repeat(MAX_MESSAGE_LENGTH))
  }
}
