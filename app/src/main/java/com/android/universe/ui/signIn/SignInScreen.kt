package com.android.universe.ui.signIn

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.universe.ui.navigation.NavigationTestTags

object SignInScreenTestTags {
  const val SIGN_IN_BUTTON = "signInButton"
}

/**
 * Sign in screen composable.
 *
 * @param viewModel The view model for the sign in screen.
 * @param onSignedIn The callback to be invoked when the user has signed in.
 */
@Composable
fun SignInScreen(
    viewModel: SignInViewModel = viewModel(),
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

  Scaffold(modifier = Modifier.testTag(NavigationTestTags.SIGN_IN_SCREEN)) { paddingValues ->
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(paddingValues).fillMaxSize()) {
          SignInButton(onClick = {})
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
