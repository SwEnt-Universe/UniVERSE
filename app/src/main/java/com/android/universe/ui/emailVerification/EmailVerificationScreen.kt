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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.android.universe.ui.navigation.NavigationTestTags
import com.android.universe.ui.profileCreation.AddProfileScreenTestTags
import com.android.universe.ui.theme.Dimensions
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth

object EmailVerificationScreenTestTags {
  const val ICON_BOX = "ICON_BOX"
  const val HEADLINE_TEXT = "HEADLINE_TEXT"
  const val MESSAGE_TEXT = "MESSAGE_TEXT"
  const val INSTRUCTIONS_TEXT = "INSTRUCTIONS_TEXT"
  const val COUNTDOWN_TEXT = "COUNTDOWN_TEXT"
  const val RESEND_BUTTON = "RESEND_BUTTON"
}

/**
 * A composable screen that handles the email verification process.
 *
 * This screen is displayed after a user registers with an email and password. It manages the UI
 * state for sending the verification email, waiting for the user to verify, handling failures in
 * sending the email, and providing an option to resend the verification link.
 *
 * It observes the user's verification status from Firebase and automatically navigates upon
 * successful verification.
 *
 * @param user The current `FirebaseUser` whose email needs to be verified. Defaults to the
 *   currently signed-in user.
 * @param onSuccess A lambda function to be invoked when the email has been successfully verified.
 *   Typically used for navigation to the next screen (e.g., home or profile creation).
 * @param onBack A lambda function to be invoked when the user clicks the back button in the top app
 *   bar. Typically used for navigating back to the previous screen (e.g., login or registration).
 * @param viewModelInstance An instance of [EmailVerificationViewModel] that manages the state and
 *   logic for this screen. This is provided for testability and defaults to a new instance.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailVerificationScreen(
    user: FirebaseUser = Firebase.auth.currentUser!!,
    onSuccess: () -> Unit = {},
    onBack: () -> Unit = {},
    viewModelInstance: EmailVerificationViewModel = EmailVerificationViewModel(user)
) {
  val viewModel = remember { viewModelInstance } // Otherwise recomposition re-init's the viewModel
  val uiState by viewModel.uiState.collectAsState()

  LaunchedEffect(uiState.emailVerified) { if (uiState.emailVerified) onSuccess() }

  Scaffold(
      topBar = {
        TopAppBar(
            title = {},
            navigationIcon = {
              IconButton(
                  onClick = onBack,
                  modifier = Modifier.testTag(AddProfileScreenTestTags.BACK_BUTTON)) {
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
              if (uiState.sendEmailFailed) {
                EmailStatusScreen(
                    icon = Icons.Outlined.FlashOn,
                    iconTint = MaterialTheme.colorScheme.error,
                    backgroundColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                    headlineColor = MaterialTheme.colorScheme.error,
                    messagePrefix = "Couldn't send verification link \n to ",
                    email = uiState.email,
                    instructions =
                        "Verify the email address, your network connection\nand try again",
                    resendEnabled = uiState.resendEnabled,
                    onResend = { viewModel.sendEmailVerification() })
              } else {
                EmailStatusScreen(
                    icon = Icons.Outlined.MarkEmailUnread,
                    iconTint = MaterialTheme.colorScheme.primary,
                    backgroundColor = MaterialTheme.colorScheme.inversePrimary.copy(alpha = 0.1f),
                    headlineColor = MaterialTheme.colorScheme.primary,
                    messagePrefix = "Please verify the email using the link sent \n to ",
                    email = uiState.email,
                    instructions = null,
                    countdown = uiState.countDown,
                    resendEnabled = uiState.resendEnabled,
                    onResend = { viewModel.sendEmailVerification() },
                    enableProgressIndicator = true)
              }
            }
      }
}

/**
 * A private composable that displays the status of the email verification process.
 *
 * This component is used within [EmailVerificationScreen] to render different states, such as the
 * initial "email sent" state or the "email send failed" state. It's a general-purpose component
 * that can be configured with different icons, colors, and messages to suit the specific status
 * being displayed.
 *
 * @param icon The main icon to display on the screen.
 * @param iconTint The tint color for the [icon].
 * @param backgroundColor The background color for the circular area behind the [icon].
 * @param headlineColor The color for the main headline text ("Account Verification").
 * @param messagePrefix A string that precedes the user's email address in the main message.
 * @param email The user's email address to be displayed.
 * @param instructions An optional string providing additional instructions or error details.
 * @param countdown An optional integer representing the seconds remaining before the user can
 *   resend the email. If provided, a countdown message is displayed.
 * @param resendEnabled A boolean indicating whether the "Resend" button should be enabled.
 * @param onResend A lambda function to be invoked when the "Resend" button is clicked.
 * @param enableProgressIndicator A boolean that, if true, shows a circular progress indicator
 *   around the icon. Useful for indicating an ongoing network operation.
 */
@Composable
private fun EmailStatusScreen(
    icon: ImageVector,
    iconTint: Color,
    backgroundColor: Color,
    headlineColor: Color,
    messagePrefix: String,
    email: String,
    instructions: String? = null,
    countdown: Int? = null,
    resendEnabled: Boolean,
    onResend: () -> Unit,
    enableProgressIndicator: Boolean = false
) {
  Box(
      modifier =
          Modifier.size(Dimensions.IconSizeLarge * 10)
              .background(color = backgroundColor, shape = CircleShape)
              .testTag(EmailVerificationScreenTestTags.ICON_BOX),
      contentAlignment = Alignment.Center) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(Dimensions.IconSizeLarge * 6))

        if (enableProgressIndicator)
            CircularProgressIndicator(modifier = Modifier.fillMaxSize(), strokeWidth = 6.dp)
      }

  Spacer(Modifier.padding(vertical = Dimensions.SpacerExtraLarge))

  Text(
      text = "Account Verification",
      color = headlineColor,
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
