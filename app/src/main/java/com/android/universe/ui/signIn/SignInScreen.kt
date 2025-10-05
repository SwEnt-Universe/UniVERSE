package com.android.universe.ui.signIn

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel

object SignInScreenTestTags {
  const val SCREEN = "signInScreen"
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
) {
  val context = LocalContext.current
  val uiState by viewModel.uiState.collectAsState()

  LaunchedEffect(uiState.errorMsg) {
    uiState.errorMsg?.let { message ->
      Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
      viewModel.clearErrorMsg()
    }
  }

  Scaffold(modifier = Modifier.testTag(SignInScreenTestTags.SCREEN)) { paddingValues ->
    Column(modifier = Modifier.padding(paddingValues)) {}
  }
}

@Preview
@Composable
fun SignInScreenPreview() {
  SignInScreen()
}
