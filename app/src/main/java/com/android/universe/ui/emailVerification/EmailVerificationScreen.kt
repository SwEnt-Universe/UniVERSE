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
              if (uiState.sendEmailFailed)
                  SendEmailErrorScreen(
                      resendEnabled = uiState.resendEnabled,
                      onResend = { viewModel.sendEmailVerification() },
                      email = uiState.email)
              else
                  AwaitingVerificationScreen(
                      resendEnabled = uiState.resendEnabled,
                      onResend = { viewModel.sendEmailVerification() },
                      countDown = uiState.countDown,
                      email = uiState.email)
            }
      }
}

/**
 * A composable that displays a screen while awaiting email verification.
 *
 * This screen informs the user that a verification email has been sent to their address and that
 * they need to click the link in it. It includes a visual indicator (a progress circle with an
 * email icon), a countdown timer for when the "Resend" button will be enabled, and the button
 * itself.
 *
 * @param resendEnabled A boolean to control the enabled state of the resend button.
 * @param onResend A lambda function to be invoked when the user clicks the "Resend" button.
 * @param countDown The remaining time in seconds before the resend button is enabled.
 * @param email The email address to which the verification link has been sent.
 */
@Composable
private fun AwaitingVerificationScreen(
    resendEnabled: Boolean,
    onResend: () -> Unit,
    countDown: Int,
    email: String
) {
  Box(
      modifier =
          Modifier.size(Dimensions.IconSizeLarge * 10)
              .background(
                  color = MaterialTheme.colorScheme.inversePrimary.copy(alpha = 0.1f),
                  shape = CircleShape),
      contentAlignment = Alignment.Center) {
        CircularProgressIndicator(modifier = Modifier.fillMaxSize(), strokeWidth = 6.dp)
        Icon(
            imageVector = Icons.Outlined.MarkEmailUnread,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(Dimensions.IconSizeLarge * 6))
      }
  Spacer(Modifier.padding(vertical = Dimensions.SpacerExtraLarge))
  Text(
      text = "Account Verification",
      color = MaterialTheme.colorScheme.primary,
      fontSize = MaterialTheme.typography.headlineLarge.fontSize,
      fontWeight = MaterialTheme.typography.headlineLarge.fontWeight,
      fontFamily = MaterialTheme.typography.headlineLarge.fontFamily)
  Spacer(Modifier.padding(vertical = Dimensions.SpacerMedium))
  Text(
      text =
          buildAnnotatedString {
            append("Please verify the email using the link sent \n to ")
            withStyle(
                style = SpanStyle(fontWeight = FontWeight.Bold),
            ) {
              append(email)
            }
          },
      textAlign = TextAlign.Center,
      fontSize = MaterialTheme.typography.bodyLarge.fontSize,
  )
  Spacer(Modifier.padding(vertical = Dimensions.SpacerExtraLarge))
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(
        text = "Didn't receive the email? Check your spam folder,",
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodyLarge,
    )

    Text(
        text =
            buildAnnotatedString {
              append("or resend a link in ")
              withStyle(
                  style = SpanStyle(fontWeight = FontWeight.Bold),
              ) {
                append("$countDown")
              }
              append(" second(s)")
            },
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth())

    Spacer(Modifier.padding(vertical = Dimensions.SpacerExtraLarge))
    ResendEmailButton(resendEnabled, onClick = onResend)
  }
}

/**
 * A composable that displays an error screen when sending the verification email fails.
 *
 * This screen informs the user about the failure, suggests possible reasons (incorrect email,
 * network issues), and provides an option to try resending the email.
 *
 * @param resendEnabled A boolean to control the enabled state of the resend button.
 * @param onResend A lambda function to be invoked when the user clicks the resend button.
 * @param email The email address to which the verification link failed to be sent.
 */
@Composable
private fun SendEmailErrorScreen(resendEnabled: Boolean, onResend: () -> Unit, email: String) {
  Box(
      modifier =
          Modifier.size(Dimensions.IconSizeLarge * 10)
              .background(
                  color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                  shape = CircleShape),
      contentAlignment = Alignment.Center) {
        Icon(
            imageVector = Icons.Outlined.FlashOn,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(Dimensions.IconSizeLarge * 6))
      }
  Spacer(Modifier.padding(vertical = Dimensions.SpacerExtraLarge))
  Text(
      text = "Account Verification",
      color = MaterialTheme.colorScheme.error,
      fontSize = MaterialTheme.typography.headlineLarge.fontSize,
      fontWeight = MaterialTheme.typography.headlineLarge.fontWeight,
      fontFamily = MaterialTheme.typography.headlineLarge.fontFamily)

  Spacer(Modifier.padding(vertical = Dimensions.SpacerMedium))
  Text(
      text =
          buildAnnotatedString {
            append("Couldn't send verification link \n to ")
            withStyle(
                style = SpanStyle(fontWeight = FontWeight.Bold),
            ) {
              append(email)
            }
          },
      textAlign = TextAlign.Center,
      fontSize = MaterialTheme.typography.bodyLarge.fontSize,
  )
  Spacer(Modifier.padding(vertical = Dimensions.SpacerExtraLarge))
  Text(
      text = "Verify the email address, your network connection\nand try again",
      textAlign = TextAlign.Center,
      style = MaterialTheme.typography.bodyLarge,
  )
  Spacer(Modifier.padding(vertical = Dimensions.SpacerExtraLarge))
  ResendEmailButton(resendEnabled, onClick = onResend)
}

/**
 * A button for resending the verification email.
 *
 * @param enabled Controls the enabled state of the button. When `true`, the button is clickable.
 * @param onClick The lambda to be executed when the button is clicked.
 */
@Composable
private fun ResendEmailButton(enabled: Boolean, onClick: () -> Unit) {
  OutlinedButton(onClick = onClick, enabled = enabled, colors = ButtonDefaults.textButtonColors()) {
    Text(
        text = "Resend Verification Link",
        style =
            MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold,
            ))
  }
}
