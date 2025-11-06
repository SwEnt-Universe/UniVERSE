package com.android.universe.ui.emailVerification

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.auth.FirebaseUser
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val TEST_EMAIL = "test@example.com"

@RunWith(AndroidJUnit4::class)
class EmailVerificationScreenTest {

  @get:Rule val composeTestRule = createComposeRule()
  private lateinit var mockUser: FirebaseUser
  private lateinit var mockViewModel: EmailVerificationViewModel
  private lateinit var uiStateFlow: MutableStateFlow<EmailVerificationUIState>

  @Before
  fun setUp() {
    mockUser = mockk(relaxed = true)
    uiStateFlow =
        MutableStateFlow(
            EmailVerificationUIState(
                email = TEST_EMAIL,
                emailVerified = false,
                sendEmailFailed = false,
                countDown = 10,
            ))

    mockViewModel =
        mockk(relaxed = true) {
          every { uiState } returns uiStateFlow
          justRun { sendEmailVerification() }
        }
  }

  @Test
  fun displaysInitialStateCorrectly() = runTest {
    composeTestRule.setContent {
      EmailVerificationScreen(user = mockUser, viewModelInstance = mockViewModel)
    }

    // Headline displayed
    composeTestRule.onNodeWithTag(EmailVerificationScreenTestTags.HEADLINE_TEXT).assertIsDisplayed()

    // Check that the email message contains the correct email
    val messageNode = composeTestRule.onNodeWithTag(EmailVerificationScreenTestTags.MESSAGE_TEXT)
    messageNode.assertExists()
    val messageText =
        messageNode
            .fetchSemanticsNode()
            .config
            .getOrNull(androidx.compose.ui.semantics.SemanticsProperties.Text)
            ?.joinToString("") { it.text } ?: ""
    assert(messageText.contains(TEST_EMAIL)) {
      "Message text should contain '$TEST_EMAIL', but was '$messageText'"
    }

    // Countdown is displayed
    val countdownNode =
        composeTestRule.onNodeWithTag(EmailVerificationScreenTestTags.COUNTDOWN_TEXT)
    countdownNode.assertExists()
    val countdownText =
        countdownNode
            .fetchSemanticsNode()
            .config
            .getOrNull(androidx.compose.ui.semantics.SemanticsProperties.Text)
            ?.joinToString("") { it.text } ?: ""
    assert(countdownText.contains("10")) {
      "Countdown text should contain '10', but was '$countdownText'"
    }

    // Resend button is disabled
    composeTestRule
        .onNodeWithTag(EmailVerificationScreenTestTags.RESEND_BUTTON)
        .assertIsNotEnabled()
  }

  @Test
  fun callsOnResend_whenResendButtonClicked() {
    composeTestRule.setContent {
      EmailVerificationScreen(user = mockUser, viewModelInstance = mockViewModel)
    }

    // Click resend button
    uiStateFlow.value = uiStateFlow.value.copy(countDown = 0)
    composeTestRule.onNodeWithTag(EmailVerificationScreenTestTags.RESEND_BUTTON).performClick()

    // Verify ViewModel's sendEmailVerification called
    verify { mockViewModel.sendEmailVerification() }
  }

  @Test
  fun navigatesOnSuccess_whenEmailVerified() = runTest {
    var successCalled = false

    composeTestRule.setContent {
      EmailVerificationScreen(
          user = mockUser, viewModelInstance = mockViewModel, onSuccess = { successCalled = true })
    }

    // Simulate email verified
    uiStateFlow.value = uiStateFlow.value.copy(emailVerified = true)

    composeTestRule.runOnIdle {
      assert(successCalled) { "onSuccess should be called when email is verified" }
    }
  }

  @Test
  fun displaysErrorState_whenSendEmailFails() = runTest {
    // Simulate failure
    uiStateFlow.value = uiStateFlow.value.copy(sendEmailFailed = true)

    composeTestRule.setContent {
      EmailVerificationScreen(user = mockUser, viewModelInstance = mockViewModel)
    }

    // Error icon displayed
    composeTestRule.onNodeWithTag(EmailVerificationScreenTestTags.ICON_BOX).assertIsDisplayed()
  }

  @Test
  fun countdownTextUpdates_whenCountdownChanges() = runTest {
    composeTestRule.setContent {
      EmailVerificationScreen(user = mockUser, viewModelInstance = mockViewModel)
    }

    // Update countdown
    uiStateFlow.value = uiStateFlow.value.copy(countDown = 5)

    // Check countdown text updated
    val countdownNode =
        composeTestRule.onNodeWithTag(EmailVerificationScreenTestTags.COUNTDOWN_TEXT)
    countdownNode.assertExists()
    val text =
        countdownNode.fetchSemanticsNode().config.getOrNull(SemanticsProperties.Text)?.joinToString(
            "") {
              it.text
            }
    assert(text?.contains("5") ?: false)
  }

  @Test
  fun resendButtonDisabled_whenResendNotEnabled() = runTest {
    composeTestRule.setContent {
      EmailVerificationScreen(user = mockUser, viewModelInstance = mockViewModel)
    }

    // Disable resend
    uiStateFlow.value = uiStateFlow.value.copy(countDown = 1)

    composeTestRule
        .onNodeWithTag(EmailVerificationScreenTestTags.RESEND_BUTTON)
        .assertIsNotEnabled()
  }
}
