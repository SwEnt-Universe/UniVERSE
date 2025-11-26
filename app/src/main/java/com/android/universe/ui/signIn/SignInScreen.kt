package com.android.universe.ui.signIn

/**
 * Part of the code in this file is copy-pasted from the BootCamp solution provided by the SwEnt
 * staff
 */
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.universe.R
import com.android.universe.ui.common.FormTestTags
import com.android.universe.ui.common.ValidationState
import com.android.universe.ui.components.CustomTextField
import com.android.universe.ui.components.LiquidBox
import com.android.universe.ui.components.LiquidButton
import com.android.universe.ui.navigation.FlowBottomMenu
import com.android.universe.ui.navigation.FlowTab
import com.android.universe.ui.signIn.SignInScreenTestTags.WELCOME_BOX
import com.android.universe.ui.theme.Dimensions

object SignInScreenTestTags {
  const val JOIN_BUTTON = "joinButton"
  const val WELCOME_BOX = "welcomeBox"
  const val ENTER_EMAIL_BOX = "enter_emailBox"
  const val GOOGLE_BOX = "googleBox"
  const val PASSWORD_BOX = "passwordBox"
  const val SIGN_IN_BOX = "signInBox"
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
  if (uiState.isLoading)
      Column(
          modifier = Modifier.fillMaxSize(),
          verticalArrangement = Arrangement.Center,
          horizontalAlignment = Alignment.CenterHorizontally) {
            LinearProgressIndicator()
          }
  else
      when (uiState.onboardingState) {
        OnboardingState.WELCOME -> WelcomeBox(onClick = viewModel::onJoinUniverse)

        OnboardingState.ENTER_EMAIL ->
            EmailBox(
                email = uiState.email,
                validationState = uiState.emailErrorMsg,
                onEmailChange = viewModel::setEmail,
                confirmEnabled = uiState.confirmEmailEnabled,
                onConfirm = viewModel::confirmEmail,
                onBack = viewModel::onBack)

        OnboardingState.SIGN_IN_GOOGLE ->
            GoogleBox(
                email = uiState.email,
                onBack = viewModel::onBack,
                onSignIn = {
                  viewModel.signIn(context = context, credentialManager = credentialManager)
                })

        OnboardingState.SIGN_IN_PASSWORD ->
            PasswordBox(
                email = uiState.email,
                password = uiState.password,
                validationState = uiState.passwordErrorMsg,
                onPasswordChange = viewModel::setPassword,
                confirmEnabled = uiState.confirmPasswordEnabled,
                onConfirm = viewModel::signInWithEmail,
                onBack = viewModel::onBack)

        else ->
            SignUpBox(
                email = uiState.email,
                onPassword = viewModel::onSignUpWithPassword,
                onGoogle = {
                  viewModel.signIn(context = context, credentialManager = credentialManager)
                },
                onBack = viewModel::onBack)
      }
}

val shape =
    RoundedCornerShape(
        topStart = Dimensions.RoundedCornerLarge,
        topEnd = Dimensions.RoundedCornerLarge,
        bottomStart = 0.dp,
        bottomEnd = 0.dp)

const val FRACTION = 0.4f

@Composable
fun Layout(
    modifier: Modifier = Modifier,
    bottomBar: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
  Box(modifier = modifier.fillMaxSize()) {
    LiquidBox(
        modifier = Modifier.fillMaxHeight(FRACTION).align(Alignment.BottomCenter), shape = shape) {
          Column(
              modifier = Modifier.fillMaxSize().padding(Dimensions.PaddingExtraLarge),
              verticalArrangement = Arrangement.Top,
              horizontalAlignment = Alignment.CenterHorizontally) {
                content()
              }
          Box(
              modifier =
                  Modifier.align(Alignment.BottomCenter).padding(Dimensions.PaddingExtraLarge)) {
                bottomBar()
              }
        }
  }
}

@Composable
fun EmailTextField(
    value: String,
    onValueChange: (String) -> Unit,
    validationState: ValidationState = ValidationState.Neutral,
    enabled: Boolean = true,
) {
  CustomTextField(
      modifier = Modifier.testTag(FormTestTags.EMAIL_FIELD),
      label = "Email",
      placeholder = "example@epfl.ch",
      leadingIcon = Icons.Filled.MarkEmailUnread,
      value = value,
      onValueChange = onValueChange,
      validationState = validationState,
      enabled = enabled)
}

@Composable
fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    validationState: ValidationState = ValidationState.Neutral,
) {
  CustomTextField(
      modifier = Modifier.testTag(FormTestTags.PASSWORD_FIELD),
      label = "Password",
      isPassword = true,
      placeholder = "",
      leadingIcon = Icons.Filled.Lock,
      value = value,
      onValueChange = onValueChange,
      validationState = validationState)
}

@Composable
fun WelcomeBox(onClick: () -> Unit = {}) {
  Layout(
      modifier = Modifier.testTag(SignInScreenTestTags.WELCOME_BOX),
      bottomBar = {
        LiquidButton(
            onClick = onClick,
            modifier =
                Modifier.fillMaxWidth(0.8f)
                    .padding(bottom = Dimensions.PaddingExtraLarge)
                    .testTag(SignInScreenTestTags.JOIN_BUTTON)) {
              Text(text = "Join the UniVERSE", style = MaterialTheme.typography.titleLarge)
            }
      }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(text = "UniVERSE", style = MaterialTheme.typography.headlineLarge)
          Spacer(modifier = Modifier.width(Dimensions.SpacerLarge))
          Image(
              painter = painterResource(id = R.drawable.color_white_universe_logo),
              contentDescription = "Logo",
              modifier = Modifier.size(Dimensions.IconSizeLarge))
        }
        Spacer(modifier = Modifier.height(Dimensions.SpacerLarge))
        Text(
            text = "Your entire campus social life on one map",
        )
      }
}

@Composable
fun EmailBox(
    email: String,
    validationState: ValidationState,
    onEmailChange: (String) -> Unit,
    confirmEnabled: Boolean,
    onConfirm: () -> Unit,
    onBack: () -> Unit
) {
  Layout(
      modifier = Modifier.testTag(SignInScreenTestTags.ENTER_EMAIL_BOX),
      bottomBar = {
        FlowBottomMenu(
            flowTabs =
                listOf(
                    FlowTab.Back(onClick = onBack),
                    FlowTab.Confirm(onClick = onConfirm, enabled = confirmEnabled)))
      }) {
        Text(text = "Enter your email address", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(Dimensions.SpacerLarge))
        EmailTextField(
            value = email, onValueChange = { onEmailChange(it) }, validationState = validationState)
      }
}

@Composable
fun GoogleBox(email: String, onBack: () -> Unit, onSignIn: () -> Unit) {
  Layout(
      modifier = Modifier.testTag(SignInScreenTestTags.GOOGLE_BOX),
      bottomBar = {
        FlowBottomMenu(
            flowTabs = listOf(FlowTab.Back(onClick = onBack), FlowTab.Google(onClick = onSignIn)))
      }) {
        Text(text = "Enter your email address", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(Dimensions.SpacerLarge))
        EmailTextField(value = email, onValueChange = {}, enabled = false)
      }
}

@Composable
fun PasswordBox(
    email: String,
    password: String,
    validationState: ValidationState,
    onPasswordChange: (String) -> Unit,
    confirmEnabled: Boolean,
    onConfirm: () -> Unit,
    onBack: () -> Unit
) {
  Layout(
      modifier = Modifier.testTag(SignInScreenTestTags.PASSWORD_BOX),
      bottomBar = {
        FlowBottomMenu(
            flowTabs =
                listOf(
                    FlowTab.Back(onClick = onBack),
                    FlowTab.Confirm(onClick = onConfirm, enabled = confirmEnabled)))
      }) {
        Text(text = "Enter your password", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(Dimensions.SpacerLarge))
        EmailTextField(value = email, onValueChange = {}, enabled = false)
        Spacer(modifier = Modifier.height(Dimensions.SpacerLarge))
        PasswordTextField(
            value = password,
            onValueChange = { onPasswordChange(it) },
            validationState = validationState)
      }
}

@Composable
fun SignUpBox(email: String, onPassword: () -> Unit, onGoogle: () -> Unit, onBack: () -> Unit) {
  Layout(
      modifier = Modifier.testTag(SignInScreenTestTags.SIGN_IN_BOX),
      bottomBar = {
        FlowBottomMenu(
            flowTabs =
                listOf(
                    FlowTab.Back(onClick = onBack),
                    FlowTab.Google(onClick = onGoogle),
                    FlowTab.Password(onClick = onPassword)))
      }) {
        Text(
            text = "Select your authentication method", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(Dimensions.SpacerLarge))
        EmailTextField(value = email, onValueChange = {}, enabled = false)
      }
}
