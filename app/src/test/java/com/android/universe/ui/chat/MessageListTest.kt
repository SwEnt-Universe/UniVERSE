package com.android.universe.ui.chat

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.model.chat.Utils.getNewSampleMessage
import com.android.universe.ui.chat.composable.ChatUIViewModel
import com.android.universe.ui.chat.composable.MessageList
import com.android.universe.ui.chat.composable.MessageListTestTags
import io.mockk.every
import io.mockk.mockk
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
class MessageListUiTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var mockViewModel: ChatUIViewModel

  @Before
  fun setUp() {
    mockViewModel = mockk()
  }

  @After
  fun tearDown() {
    unmockkAll()
  }

  @Test
  fun messageList_displaysAllMessages_correctly() {
    val userId = "user1"

    // Sample messages
    val messages =
        listOf(
            getNewSampleMessage(),
            getNewSampleMessage(),
            getNewSampleMessage(),
            getNewSampleMessage())

    messages.forEach { message ->
      val username = if (message.senderID == userId) "Me" else "Alice"
      val userFlow = MutableStateFlow(username)
      every { mockViewModel.getUserName(message.senderID) } returns userFlow
    }

    composeTestRule.setContent {
      MessageList(
          userID = userId,
          messages = messages,
          modifier = androidx.compose.ui.Modifier,
          vm = mockViewModel)
    }

    // LazyColumn exists
    composeTestRule.onNodeWithTag(MessageListTestTags.LIST).assertIsDisplayed()

    // Verify each message renders correctly
    messages.forEach { message ->
      val itemTag = MessageListTestTags.ITEM_PREFIX + message.messageID
      composeTestRule.onNodeWithTag(itemTag).assertIsDisplayed()
    }
  }
}
