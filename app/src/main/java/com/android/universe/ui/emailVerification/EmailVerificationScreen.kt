package com.android.universe.ui.emailVerification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.MarkEmailUnread
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.universe.ui.components.LiquidBox
import com.android.universe.ui.components.ScreenLayout
import com.android.universe.ui.navigation.FlowBottomMenu
import com.android.universe.ui.navigation.FlowTab
import com.android.universe.ui.signIn.shape
import com.android.universe.ui.theme.Dimensions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

object EmailVerificationScreenTestTags {
  const val ICON_BOX = "ICON_BOX"
  const val HEADLINE_TEXT = "HEADLINE_TEXT"
  const val MESSAGE_TEXT = "MESSAGE_TEXT"
  const val INSTRUCTIONS_TEXT = "INSTRUCTIONS_TEXT"
  const val COUNTDOWN_TEXT = "COUNTDOWN_TEXT"
}

/**
 * A composable screen that handles the email verification process.
 *
 * This screen is displayed after a user registers with an email and password. It manages the UI
 * state for sending the verification email, waiting for the user to verify, handling failures in
 * sending the email, and providing an option to resend the verification link.
 *
 * It observes the user's verification status from Firebase using a `LaunchedEffect` and
 * automatically navigates upon successful verification. It also handles cases where the user
 * becomes null (e.g., signed out), navigating back.
 *
 * @param user The current `FirebaseUser` whose email needs to be verified. Defaults to the
 *   currently signed-in user from `Firebase.auth`.
 * @param onSuccess A lambda function to be invoked when the email has been successfully verified.
 *   This is typically used for navigation to the next screen (e.g., home or profile creation).
 * @param onBack A lambda function to be invoked when the user clicks the back button in the top app
 *   bar, or if the user becomes null. This is typically used for navigating back to the previous
 *   screen (e.g., login or registration).
 * @param viewModel The [EmailVerificationViewModel] that manages the state and logic for this
 *   screen. It defaults to an instance provided by `viewModel()`.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailVerificationScreen(
    user: FirebaseUser? = FirebaseAuth.getInstance().currentUser,
    onSuccess: () -> Unit = {},
    onBack: () -> Unit = {},
    viewModel: EmailVerificationViewModel = viewModel()
) {
  val uiState by viewModel.uiState.collectAsState()

  LaunchedEffect(uiState.emailVerified) {
    if (uiState.emailVerified) onSuccess()
    else if (user != null) viewModel.sendEmailVerification(user)
    else onBack() // User became null, go back to login
  }

  ScreenLayout(
      bottomBar = {
        FlowBottomMenu(
            flowTabs =
                listOf(
                    FlowTab.Back(onClick = onBack),
                    FlowTab.Email(
                        onClick = {
                          if (user != null && uiState.resendEnabled)
                              viewModel.sendEmailVerification(user)
                        },
                        enabled = uiState.resendEnabled)))
      }) { paddingValues ->
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
          val defaultSize = maxHeight * 0.4f
          Column(Modifier.fillMaxSize()) {
            Spacer(Modifier.height(paddingValues.calculateTopPadding()))
            LoadingAnimation(
                sendEmailFailed = uiState.sendEmailFailed,
                modifier = Modifier.weight(1f).align(Alignment.CenterHorizontally))
            LiquidBox(modifier = Modifier.fillMaxWidth().wrapContentHeight(), shape = shape) {
              Column(
                  modifier =
                      Modifier.fillMaxWidth()
                          .wrapContentSize()
                          .defaultMinSize(minHeight = defaultSize)
                          .padding(horizontal = Dimensions.PaddingExtraLarge)
                          .padding(top = Dimensions.PaddingExtraLarge),
                  verticalArrangement = Arrangement.Top,
                  horizontalAlignment = Alignment.CenterHorizontally) {
                    EmailStatusScreen(
                        sendEmailFailed = uiState.sendEmailFailed,
                        email = uiState.email,
                        countdown = uiState.countDown)
                    Spacer(Modifier.height(paddingValues.calculateBottomPadding()))
                  }
            }
          }
        }
      }
}

/**
 * A composable that displays a loading animation or an error indicator.
 *
 * This animation consists of a central icon within a circular background. If `sendEmailFailed` is
 * `false`, it shows a `CircularProgressIndicator` spinning around a "MarkEmailUnread" icon,
 * indicating that the email is being sent or the app is waiting for verification. If
 * `sendEmailFailed` is `true`, it displays a static "FlashOn" icon with an error color scheme,
 * indicating a failure in sending the verification email.
 *
 * @param sendEmailFailed A boolean that determines the visual state. `true` for an error state,
 *   `false` for a loading state.
 * @param modifier The [Modifier] to be applied to the container Box.
 */
@Composable
fun LoadingAnimation(sendEmailFailed: Boolean, modifier: Modifier = Modifier) {
  val icon = if (sendEmailFailed) Icons.Outlined.FlashOn else Icons.Outlined.MarkEmailUnread
  val primaryColor =
      if (sendEmailFailed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
  val backgroundColor =
      if (sendEmailFailed) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
      else MaterialTheme.colorScheme.inversePrimary.copy(alpha = 0.1f)

  BoxWithConstraints(
      modifier = modifier.fillMaxSize().padding(Dimensions.PaddingExtraLarge),
      contentAlignment = Alignment.Center) {
        Box(
            modifier =
                modifier
                    .size(min(maxHeight, maxWidth))
                    .background(color = backgroundColor, shape = CircleShape)
                    .testTag(EmailVerificationScreenTestTags.ICON_BOX),
            contentAlignment = Alignment.Center) {
              Icon(
                  imageVector = icon,
                  contentDescription = null,
                  tint = primaryColor,
                  modifier = Modifier.fillMaxSize(0.6f))

              if (!sendEmailFailed)
                  CircularProgressIndicator(modifier = Modifier.fillMaxSize(), strokeWidth = 6.dp)
            }
      }
}

/**
 * Displays the status of the email verification process, including instructions and user feedback.
 *
 * This composable is a central part of the `EmailVerificationScreen`. It shows a headline, a
 * message indicating where the verification email was sent, and further instructions. The content
 * adapts based on the `sendEmailFailed` state.
 * - If `sendEmailFailed` is `true`, it displays an error message and troubleshooting advice.
 * - If `sendEmailFailed` is `false`, it shows a confirmation that the email was sent. It can also
 *   display a countdown timer (`countdown`) indicating when the user can request a new email.
 *
 * @param sendEmailFailed A boolean that is `true` if the last attempt to send the verification
 *   email failed, and `false` otherwise.
 * @param email The user's email address to display in the message.
 * @param countdown An optional integer representing the remaining seconds before the resend action
 *   is available. If not null, a countdown text is displayed.
 */
@Composable
private fun EmailStatusScreen(
    sendEmailFailed: Boolean,
    email: String,
    countdown: Int? = null,
) {

  val messagePrefix =
      if (sendEmailFailed) "Couldn't send a verification link to "
      else "Please verify the email using the link sent to "
  val instructions =
      if (sendEmailFailed) "Verify the email address, your internet connection and try again"
      else null

  Text(
      text = "Account Verification",
      textAlign = TextAlign.Left,
      fontSize = MaterialTheme.typography.headlineLarge.fontSize,
      fontWeight = MaterialTheme.typography.headlineLarge.fontWeight,
      fontFamily = MaterialTheme.typography.headlineLarge.fontFamily,
      color = MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.fillMaxWidth().testTag(EmailVerificationScreenTestTags.HEADLINE_TEXT))

  Spacer(Modifier.padding(vertical = Dimensions.SpacerMedium))

  Text(
      text =
          buildAnnotatedString {
            append(messagePrefix)
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append(email) }
          },
      color = MaterialTheme.colorScheme.onSurface,
      textAlign = TextAlign.Left,
      fontSize = MaterialTheme.typography.bodyLarge.fontSize,
      modifier = Modifier.fillMaxWidth().testTag(EmailVerificationScreenTestTags.MESSAGE_TEXT))

  instructions?.let {
    Spacer(Modifier.padding(vertical = Dimensions.SpacerMedium))
    Text(
        text = it,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Left,
        style = MaterialTheme.typography.bodyLarge,
        modifier =
            Modifier.fillMaxWidth().testTag(EmailVerificationScreenTestTags.INSTRUCTIONS_TEXT))
  }

  Spacer(Modifier.padding(vertical = Dimensions.SpacerMedium))

  if (countdown != null && !sendEmailFailed) {
    Text(
        text =
            buildAnnotatedString {
              append("Didn't receive the email? Check your spam folder or resend a link in ")
              withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append("$countdown") }
              append(" second(s)")
            },
        color = MaterialTheme.colorScheme.onSurface,
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Left,
        modifier = Modifier.fillMaxWidth().testTag(EmailVerificationScreenTestTags.COUNTDOWN_TEXT))
  }
}
