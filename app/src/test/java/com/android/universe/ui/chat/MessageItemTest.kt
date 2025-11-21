package com.android.universe.ui.chat

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.model.chat.Utils.getNewSampleMessage
import com.android.universe.ui.chat.composable.ChatUIViewModel
import com.android.universe.ui.chat.composable.MessageItem
import com.android.universe.ui.chat.composable.MessageItemTestTags
import com.android.universe.utils.setContentWithStubBackdrop
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
class MessageItemTest {
  private lateinit var mockViewModel: ChatUIViewModel

  @Before
  fun setUp() {
    mockViewModel = mockk()
  }

  @After
  fun tearDown() {
    unmockkAll()
  }

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun messageItem_displaysUsernameMessageAndTime() {
    val message = getNewSampleMessage()
    val userName = "Alice"
    val isUserMe = false
    val messageTime = "testTime"

    val userFlow: MutableStateFlow<String> = MutableStateFlow(userName)
    every { mockViewModel.getUserName(message.senderID) } returns userFlow

    composeTestRule.setContentWithStubBackdrop {
      MessageItem(
          senderID = message.senderID,
          message = message.message,
          time = messageTime,
          isUserMe = isUserMe,
          vm = mockViewModel)
    }

    composeTestRule
        .onNodeWithTag(MessageItemTestTags.USERNAME)
        .assertIsDisplayed()
        .assert(hasText(userName))

    composeTestRule
        .onNodeWithTag(MessageItemTestTags.MESSAGE_TEXT)
        .assertIsDisplayed()
        .assert(hasText(message.message))

    composeTestRule
        .onNodeWithTag(MessageItemTestTags.TIME)
        .assertIsDisplayed()
        .assert(hasText(messageTime))
  }

  @Test
  fun messageItem_displaysMessageAndTime_userIsMe_hidesUsername() {
    val message = getNewSampleMessage()
    val isUserMe = true
    val messageTime = "testTime"

    // User flow is not needed because username is hidden, but can still mock
    val userFlow: MutableStateFlow<String> = MutableStateFlow("")
    every { mockViewModel.getUserName(message.senderID) } returns userFlow

    composeTestRule.setContentWithStubBackdrop {
      MessageItem(
          senderID = message.senderID,
          message = message.message,
          time = messageTime,
          isUserMe = isUserMe,
          vm = mockViewModel)
    }

    // Username should NOT be displayed
    composeTestRule.onNodeWithTag(MessageItemTestTags.USERNAME).assertDoesNotExist()

    // Message text
    composeTestRule
        .onNodeWithTag(MessageItemTestTags.MESSAGE_TEXT)
        .assertIsDisplayed()
        .assert(hasText(message.message))

    // Time
    composeTestRule
        .onNodeWithTag(MessageItemTestTags.TIME)
        .assertIsDisplayed()
        .assert(hasText(messageTime))
  }
}
