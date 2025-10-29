package com.android.universe.ui.signIn

/**
 * Part of the code in this file is copy-pasted from the BootCamp solution provided by the SwEnt
 * staff
 */
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.universe.R
import com.android.universe.ui.common.EmailInputField
import com.android.universe.ui.common.PasswordInputField
import com.android.universe.ui.navigation.NavigationTestTags
import com.android.universe.ui.theme.Dimensions
import com.android.universe.ui.theme.UniverseTheme

object SignInScreenTestTags {
  const val SIGN_IN_BUTTON = "signInButton"
  const val EMAIL_SIGN_IN_BUTTON = "emailSignInButton"
  const val SIGN_IN_LOGO = "signInLogo"
  const val SIGN_IN_TITLE = "signInTitle"
  const val SIGN_IN_PROGRESS_BAR = "signInProgressBar"
}

/**
 * Sign in screen composable.
 *
 * @param viewModel The view model for the sign in screen.
 * @param onSignedIn The callback to be invoked when the user has signed in.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignInScreen(
    viewModel: SignInViewModel = viewModel(),
    credentialManager: CredentialManager = CredentialManager.create(LocalContext.current),
    onSignedIn: () -> Unit = {},
    context: Context = LocalContext.current,
) {
  val uiState by viewModel.uiState.collectAsState()

  LaunchedEffect(uiState.errorMsg) {
    uiState.errorMsg?.let { message ->
      Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
      viewModel.clearErrorMsg()
    }
  }

  LaunchedEffect(uiState.user) {
    uiState.user?.let {
      Toast.makeText(context, "Signed in successfully", Toast.LENGTH_SHORT).show()
      onSignedIn()
    }
  }

  Scaffold(modifier = Modifier.testTag(NavigationTestTags.SIGN_IN_SCREEN).fillMaxSize()) {
      paddingValues ->
    Column(
        modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Text(
          modifier = Modifier.testTag(SignInScreenTestTags.SIGN_IN_TITLE),
          text = "Welcome to",
          style = MaterialTheme.typography.headlineLarge.copy(fontSize = 56.sp, lineHeight = 64.sp),
          textAlign = TextAlign.Center,
          fontWeight = FontWeight.Bold)

      Spacer(modifier = Modifier.height(Dimensions.SpacerLarge))

      EmailInputField(
          value = uiState.email,
          onValueChange = { viewModel.setEmail(it) },
          errorMsg = uiState.emailErrorMsg)

      PasswordInputField(
          value = uiState.password,
          onValueChange = { viewModel.setPassword(it) },
          errorMsg = uiState.passwordErrorMsg)

      Spacer(modifier = Modifier.height(16.dp))

      if (uiState.isLoading)
          LinearProgressIndicator(
              modifier = Modifier.testTag(SignInScreenTestTags.SIGN_IN_PROGRESS_BAR))
      else {
        SignInButton(onClick = { viewModel.signInWithEmail() }, enabled = uiState.signInEnabled)
        Spacer(modifier = Modifier.height(64.dp))
        Text(
            text = "OR",
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp, lineHeight = 24.sp))
        GoogleSignInButton(
            onClick = {
              viewModel.signIn(context = context, credentialManager = credentialManager)
            })
      }
    }
  }
}

/**
 * A button composable for signing in.
 *
 * @param onClick The callback to be invoked when the button is clicked.
 */
@Composable
fun GoogleSignInButton(onClick: () -> Unit) {
  OutlinedButton(
      onClick = onClick,
      colors =
          ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.surface), // Button color
      modifier =
          Modifier.padding(horizontal = 64.dp)
              .height(48.dp) // Adjust height as needed
              .testTag(SignInScreenTestTags.SIGN_IN_BUTTON)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()) {
              // Load the Google logo from resources
              Image(
                  painter =
                      painterResource(id = R.drawable.google_logo), // Ensure this drawable exists
                  contentDescription = "Google Logo",
                  modifier =
                      Modifier.size(Dimensions.IconSizeLarge) // Size of the Google logo
                          .padding(end = Dimensions.PaddingMedium))

              // Text for the button
              Text(
                  text = "Sign in with Google",
                  color = MaterialTheme.colorScheme.onSurface,
                  style = MaterialTheme.typography.bodyLarge)
            }
      }
}

/**
 * A button composable for signing in with email and password.
 *
 * @param onClick The callback to be invoked when the button is clicked.
 * @param enabled Controls the enabled state of the button. When `false`, this button will not be
 *   clickable.
 */
@Composable
fun SignInButton(onClick: () -> Unit, enabled: Boolean) {
  OutlinedButton(
      onClick = onClick,
      enabled = enabled,
      colors =
          ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface, contentColor = Color.Black),
      modifier =
          Modifier.padding(horizontal = 64.dp)
              .height(48.dp)
              .testTag(SignInScreenTestTags.EMAIL_SIGN_IN_BUTTON)) {
        Text(text = "Sign In", fontSize = 16.sp, fontWeight = FontWeight.Medium)
      }
}

/** A preview composable for the sign-in screen. */
@Preview
@Composable
fun SignInScreenPreview() {
  UniverseTheme { SignInScreen() }
}
