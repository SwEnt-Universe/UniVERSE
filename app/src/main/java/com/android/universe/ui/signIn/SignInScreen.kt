package com.android.universe.ui.signIn

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.universe.R
import com.android.universe.ui.navigation.NavigationTestTags

object SignInScreenTestTags {
  const val SIGN_IN_BUTTON = "signInButton"
  const val SIGN_IN_LOGO = "signInLogo"
  const val SIGN_IN_TITLE = "signInTitle"
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

  Scaffold(
      modifier = Modifier
          .testTag(NavigationTestTags.SIGN_IN_SCREEN)
          .fillMaxSize()
  ) {
      paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(R.drawable.app_logo_placeholder),
                contentDescription = "App Logo",
                modifier = Modifier
                    .testTag(SignInScreenTestTags.SIGN_IN_LOGO)
                    .size(256.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                modifier = Modifier
                    .testTag(SignInScreenTestTags.SIGN_IN_TITLE),
                text = "Welcome to UNIVerse !",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )

            if (uiState.isLoading){
                LinearProgressIndicator()
            } else
                SignInButton(onClick = {
                    viewModel.signIn(
                        context = context,
                        credentialManager = credentialManager
                    )
                })
        }
  }
}

/**
 * A button composable for signing in.
 *
 * @param onClick The callback to be invoked when the button is clicked.
 */
@Composable
fun SignInButton(onClick: () -> Unit) {
  OutlinedButton(
      onClick = onClick, modifier = Modifier.testTag(SignInScreenTestTags.SIGN_IN_BUTTON)) {
        Text("Sign In")
      }
}

/** A preview composable for the sign-in screen. */
@Preview
@Composable
fun SignInScreenPreview() {
  SignInScreen()
}
