package com.android.universe.ui.chat

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.model.chat.Utils.getNewSampleMessage
import com.android.universe.ui.chat.ChatListScreenTestTags.NO_CHAT_PREVIEW
import com.android.universe.ui.theme.UniverseTheme
import com.android.universe.utils.setContentWithStubBackdrop
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class ChatListScreenUiTest {

  @get:Rule val composeTestRule = createComposeRule()

  // Mock the ViewModel and its state flow
  private lateinit var mockViewModel: ChatListViewModel
  private val mockChatPreviews = MutableStateFlow<List<ChatPreview>>(emptyList())
  private val mockOnChatSelected: (String) -> Unit = mockk(relaxed = true)

  @Before
  fun setUp() {
    mockViewModel = mockk(relaxed = true) { every { chatPreviews } returns mockChatPreviews }
    // Set up the dependency injection for the ViewModel
    @Suppress("UNCHECKED_CAST")
    composeTestRule.setContentWithStubBackdrop {
      UniverseTheme {
        ChatListScreen(
            userID = "testUser",
            onTabSelected = {},
            onChatSelected = mockOnChatSelected,
            vm = mockViewModel)
      }
    }
  }

  @After
  fun tearDown() {
    unmockkAll()
  }

  @Test
  fun chatListScreen_displaysChatPreviews_whenChatPreviews() {
    val chat = getNewSampleChatPreview()
    mockChatPreviews.value = listOf(chat)
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(ChatListScreenTestTags.CHAT_LIST_COLUMN).assertIsDisplayed()
  }

  @Test
  fun chatListScreen_displaysNoChatPreview_whenNoChatPreviews() = runTest {
    mockChatPreviews.value = emptyList()
    composeTestRule.onNodeWithTag(NO_CHAT_PREVIEW).assertIsDisplayed()
  }

  @Test
  fun chatListScreen_displaysChatPreviews_andContentCorrectly() = runTest {
    val chat1 = getNewSampleChatPreview()
    val chat2 = getNewSampleChatPreview(generateLastMessage = false)

    mockChatPreviews.value = listOf(chat1, chat2)

    val chat1Tag = ChatListScreenTestTags.CHAT_ITEM_PREFIX + chat1.chatID
    composeTestRule.onNodeWithTag(chat1Tag).assertIsDisplayed()

    composeTestRule
        .onNodeWithTag(chat1Tag + ChatListScreenTestTags.LAST_MESSAGE_TEXT, useUnmergedTree = true)
        .assertTextEquals(chat1.lastMessage.value?.message ?: "Empty in here ...")

    composeTestRule
        .onNodeWithTag(chat1Tag + ChatListScreenTestTags.CHAT_NAME, useUnmergedTree = true)
        .assertTextEquals(chat1.chatName)

    val chat2Tag = ChatListScreenTestTags.CHAT_ITEM_PREFIX + chat2.chatID
    composeTestRule.onNodeWithTag(chat2Tag).assertIsDisplayed()

    composeTestRule
        .onNodeWithTag(chat2Tag + ChatListScreenTestTags.LAST_MESSAGE_TEXT, useUnmergedTree = true)
        .assertTextEquals("Empty in here ...")

    composeTestRule
        .onNodeWithTag(chat2Tag + ChatListScreenTestTags.DISPLAY_TIME_TEXT, useUnmergedTree = true)
        .assertDoesNotExist()
  }

  @Test
  fun chatListScreen_clickOnChat_callsOnChatSelectedCallback() = runTest {
    val chatPreview = getNewSampleChatPreview()
    mockChatPreviews.value = listOf(chatPreview)

    val chatItemTag = ChatListScreenTestTags.CHAT_ITEM_PREFIX + chatPreview.chatID

    composeTestRule.onNodeWithTag(chatItemTag).performClick()

    verify(exactly = 1) { mockOnChatSelected(chatPreview.chatID) }
  }
}

var chatPreviewCount = 0

fun getNewSampleChatPreview(generateLastMessage: Boolean = true): ChatPreview {
  chatPreviewCount++
  return ChatPreview(
      chatID = "chat$chatPreviewCount",
      chatName = "Chat $chatPreviewCount",
      lastMessage = mutableStateOf(if (generateLastMessage) getNewSampleMessage() else null))
}
