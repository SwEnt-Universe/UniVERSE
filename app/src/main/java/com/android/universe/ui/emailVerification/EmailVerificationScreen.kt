package com.android.universe.ui.emailVerification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.MarkEmailUnread
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.universe.ui.navigation.NavigationTestTags
import com.android.universe.ui.theme.Dimensions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

object EmailVerificationScreenTestTags {
  const val ICON_BOX = "ICON_BOX"
  const val HEADLINE_TEXT = "HEADLINE_TEXT"
  const val MESSAGE_TEXT = "MESSAGE_TEXT"
  const val INSTRUCTIONS_TEXT = "INSTRUCTIONS_TEXT"
  const val COUNTDOWN_TEXT = "COUNTDOWN_TEXT"
  const val RESEND_BUTTON = "RESEND_BUTTON"
  const val BACK_BUTTON = "BACK_BUTTON"
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

  Scaffold(
      containerColor = Color.Transparent,
      topBar = {
        TopAppBar(
            title = {},
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            navigationIcon = {
              IconButton(
                  onClick = onBack,
                  modifier = Modifier.testTag(EmailVerificationScreenTestTags.BACK_BUTTON)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Login")
                  }
            })
      },
      modifier = Modifier.testTag(NavigationTestTags.EMAIL_VALIDATION_SCREEN)) { paddingValues ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier =
                Modifier.padding(paddingValues)
                    .padding(top = Dimensions.PaddingExtraLarge * 2)
                    .fillMaxSize()) {
              EmailStatusScreen(
                  sendEmailFailed = uiState.sendEmailFailed,
                  email = uiState.email,
                  countdown = uiState.countDown,
                  resendEnabled = uiState.resendEnabled,
                  onResend = {
                    if (user != null) viewModel.sendEmailVerification(user) else onBack()
                  })
            }
      }
}

/**
 * A private composable that displays the current status of the email verification process.
 *
 * It adapts its UI based on whether the verification email was sent successfully or failed. It
 * shows an icon, a headline, a message with the user's email, and instructions. If the email was
 * sent, it displays a countdown for when the "Resend" button will be enabled. If the sending
 * failed, it shows an error state and provides instructions to troubleshoot.
 *
 * @param sendEmailFailed A boolean indicating whether the last attempt to send a verification email
 *   failed.
 * @param email The email address to which the verification link was sent.
 * @param countdown The remaining time in seconds before the user can request another verification
 *   email. If null, the countdown is not displayed.
 * @param resendEnabled A boolean indicating whether the "Resend" button should be enabled.
 * @param onResend A lambda function to be invoked when the user clicks the "Resend" button.
 */
@Composable
private fun EmailStatusScreen(
    sendEmailFailed: Boolean,
    email: String,
    countdown: Int? = null,
    resendEnabled: Boolean,
    onResend: () -> Unit,
) {
  val icon = if (sendEmailFailed) Icons.Outlined.FlashOn else Icons.Outlined.MarkEmailUnread
  val primaryColor =
      if (sendEmailFailed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
  val backgroundColor =
      if (sendEmailFailed) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
      else MaterialTheme.colorScheme.inversePrimary.copy(alpha = 0.1f)
  val messagePrefix =
      if (sendEmailFailed) "Couldn't send verification link \n to "
      else "Please verify the email using the link sent \n to "
  val instructions =
      if (sendEmailFailed) "Verify the email address, your network connection\nand try again"
      else null
  Box(
      modifier =
          Modifier.size(Dimensions.IconSizeLarge * 10)
              .background(color = backgroundColor, shape = CircleShape)
              .testTag(EmailVerificationScreenTestTags.ICON_BOX),
      contentAlignment = Alignment.Center) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = primaryColor,
            modifier = Modifier.size(Dimensions.IconSizeLarge * 6))

        if (!sendEmailFailed)
            CircularProgressIndicator(modifier = Modifier.fillMaxSize(), strokeWidth = 6.dp)
      }

  Spacer(Modifier.padding(vertical = Dimensions.SpacerExtraLarge))

  Text(
      text = "Account Verification",
      color = primaryColor,
      fontSize = MaterialTheme.typography.headlineLarge.fontSize,
      fontWeight = MaterialTheme.typography.headlineLarge.fontWeight,
      fontFamily = MaterialTheme.typography.headlineLarge.fontFamily,
      modifier = Modifier.testTag(EmailVerificationScreenTestTags.HEADLINE_TEXT))

  Spacer(Modifier.padding(vertical = Dimensions.SpacerMedium))

  Text(
      text =
          buildAnnotatedString {
            append(messagePrefix)
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append(email) }
          },
      textAlign = TextAlign.Center,
      fontSize = MaterialTheme.typography.bodyLarge.fontSize,
      modifier = Modifier.testTag(EmailVerificationScreenTestTags.MESSAGE_TEXT))

  instructions?.let {
    Spacer(Modifier.padding(vertical = Dimensions.SpacerExtraLarge))
    Text(
        text = it,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.testTag(EmailVerificationScreenTestTags.INSTRUCTIONS_TEXT))
  }

  Spacer(Modifier.padding(vertical = Dimensions.SpacerExtraLarge))

  if (countdown != null) {
    Text(
        text = "Didn't receive the email? Check your spam folder,",
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodyLarge,
    )

    Text(
        text =
            buildAnnotatedString {
              append("or resend a link in ")
              withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append("$countdown") }
              append(" second(s)")
            },
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().testTag(EmailVerificationScreenTestTags.COUNTDOWN_TEXT))

    Spacer(Modifier.padding(vertical = Dimensions.SpacerExtraLarge))
  }

  ResendEmailButton(
      enabled = resendEnabled,
      onClick = onResend,
  )
}

/**
 * A button for resending the verification email.
 *
 * @param enabled Controls the enabled state of the button. When `true`, the button is clickable.
 * @param onClick The lambda to be executed when the button is clicked.
 */
@Composable
private fun ResendEmailButton(enabled: Boolean, onClick: () -> Unit) {
  OutlinedButton(
      onClick = onClick,
      enabled = enabled,
      colors = ButtonDefaults.textButtonColors(),
      modifier = Modifier.testTag(EmailVerificationScreenTestTags.RESEND_BUTTON)) {
        Text(
            text = "Resend Verification Link",
            style =
                MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                ))
      }
}
