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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
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
import com.android.universe.ui.components.ScreenLayout
import com.android.universe.ui.navigation.FlowBottomMenu
import com.android.universe.ui.navigation.FlowTab
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
            LiquidBox(modifier = Modifier.wrapContentSize(), color = Color.Transparent) {
              LinearProgressIndicator()
            }
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

        OnboardingState.CHOOSE_AUTH_METHOD ->
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

/**
 * A composable function that provides a common layout structure for the sign-in/sign-up screens. It
 * places a `LiquidBox` at the bottom of the screen which contains the main content at the top and a
 * bottom bar at the very bottom.
 *
 * @param modifier The modifier to be applied to the root Box.
 * @param bottomBar A composable lambda for the content to be displayed in the bottom bar area.
 * @param content A composable lambda for the main content to be displayed above the bottom bar.
 */
@Composable
fun Layout(
    modifier: Modifier = Modifier,
    bottomBar: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
  ScreenLayout(
      bottomBar = {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) { bottomBar() }
      }) { paddingValues ->
        BoxWithConstraints(Modifier.fillMaxSize()) {
          val defaultSize = maxHeight * 0.4f

          LiquidBox(
              modifier = Modifier.fillMaxWidth().wrapContentHeight().align(Alignment.BottomCenter),
              shape = shape) {
                Column(
                    modifier =
                        Modifier.wrapContentSize()
                            .defaultMinSize(minHeight = defaultSize)
                            .padding(horizontal = Dimensions.PaddingExtraLarge)
                            .padding(top = Dimensions.PaddingExtraLarge),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally) {
                      content()
                      Spacer(Modifier.height(paddingValues.calculateBottomPadding()))
                    }
              }
        }
      }
}

/**
 * A composable for an email input field. This is a specialized version of [CustomTextField]
 * configured for email entry.
 *
 * @param value The current text to display in the text field.
 * @param onValueChange The callback that is triggered when the input service updates the text. An
 *   updated text comes as a parameter of the callback.
 * @param validationState The current validation state of the field, which affects its visual
 *   appearance (e.g., error color). Defaults to [ValidationState.Neutral].
 * @param enabled Controls the enabled state of the text field. When `false`, the text field will be
 *   uneditable and visually disabled.
 */
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

/**
 * A composable that displays a custom password text field. This is a specialized version of
 * [CustomTextField] for password input.
 *
 * @param value The current value of the password field.
 * @param onValueChange The callback that is triggered when the input service updates the text. An
 *   updated text comes as a parameter of the callback.
 * @param validationState The current validation state of the field, which can affect its appearance
 *   (e.g., color). Defaults to [ValidationState.Neutral].
 */
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

/**
 * A composable that displays the application's logo. The logo is presented within a clipped box and
 * scaled to fit.
 */
@Composable
fun UniverseIcon() {
  Box(
      modifier =
          Modifier.size(Dimensions.IconSizeLarge)
              .clip(RoundedCornerShape(Dimensions.RoundedCorner))) {
        Image(
            painter = painterResource(id = R.drawable.color_white_universe_logo),
            contentDescription = "Logo",
            modifier =
                Modifier.fillMaxSize()
                    .graphicsLayer(
                        scaleX = 1.5f, scaleY = 1.5f, translationX = 0f, translationY = 0f),
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center)
      }
}

/**
 * A composable that displays the welcome screen of the application. It shows the application's
 * logo, name, and a tagline. It provides a button to proceed to the next step of the
 * onboarding/sign-in process.
 *
 * @param onClick A callback function to be invoked when the "Join the UniVERSE" button is clicked.
 */
@Composable
fun WelcomeBox(onClick: () -> Unit = {}) {
  Layout(
      modifier = Modifier.testTag(SignInScreenTestTags.WELCOME_BOX),
      bottomBar = {
        LiquidButton(
            onClick = onClick,
            modifier =
                Modifier.fillMaxWidth(0.8f)
                    .padding(all = Dimensions.PaddingExtraLarge)
                    .testTag(SignInScreenTestTags.JOIN_BUTTON)) {
              Text(
                  text = "Join the UniVERSE",
                  style = MaterialTheme.typography.titleLarge,
                  color = MaterialTheme.colorScheme.onSurface)
            }
      }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
              text = "UniVERSE",
              style = MaterialTheme.typography.headlineLarge,
              color = MaterialTheme.colorScheme.onSurface)
          Spacer(modifier = Modifier.width(Dimensions.SpacerLarge))
          UniverseIcon()
        }
        Spacer(modifier = Modifier.height(Dimensions.SpacerLarge))
        Text(
            text = "Your entire campus social life on one map",
            color = MaterialTheme.colorScheme.onSurface)
      }
}

/**
 * A composable that prompts the user to enter their email address. It includes a text field for the
 * email, provides validation feedback, and has navigation controls to confirm the email or go back.
 *
 * @param email The current value of the email input field.
 * @param validationState The current validation state of the email input, used to show feedback
 *   (e.g., an error).
 * @param onEmailChange A callback function that is invoked when the user types in the email field.
 * @param confirmEnabled A boolean that determines if the confirm button is enabled.
 * @param onConfirm A callback function to be invoked when the user presses the confirm button.
 * @param onBack A callback function to be invoked when the user presses the back button.
 */
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
        Text(
            text = "Enter your email address",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(Dimensions.SpacerLarge))
        EmailTextField(
            value = email, onValueChange = { onEmailChange(it) }, validationState = validationState)
      }
}

/**
 * A composable that displays a screen prompting the user to sign in with Google. It shows the
 * user's email in a disabled text field and provides options to either proceed with Google sign-in
 * or go back to the previous screen.
 *
 * @param email The user's email address, displayed in a non-editable text field.
 * @param onBack A callback function to be invoked when the user clicks the back button.
 * @param onSignIn A callback function to be invoked when the user clicks the Google sign-in button.
 */
@Composable
fun GoogleBox(email: String, onBack: () -> Unit, onSignIn: () -> Unit) {
  Layout(
      modifier = Modifier.testTag(SignInScreenTestTags.GOOGLE_BOX),
      bottomBar = {
        FlowBottomMenu(
            flowTabs = listOf(FlowTab.Back(onClick = onBack), FlowTab.Google(onClick = onSignIn)))
      }) {
        Text(
            text = "Sign in with google",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(Dimensions.SpacerLarge))
        EmailTextField(value = email, onValueChange = {}, enabled = false)
      }
}

/**
 * A composable that displays the password entry screen for signing in. It shows the user's email
 * (which is non-editable), a password input field, and navigation options to confirm or go back.
 *
 * @param email The user's email address, displayed in a disabled text field.
 * @param password The current value of the password input field.
 * @param validationState The validation state of the password, used to show error indicators.
 * @param onPasswordChange A callback function invoked when the user types in the password field.
 * @param confirmEnabled A boolean indicating whether the confirm action is enabled.
 * @param onConfirm A callback function to be invoked when the user confirms the password to sign
 *   in.
 * @param onBack A callback function to be invoked when the user chooses to go back to the previous
 *   screen.
 */
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
        Text(
            text = "Enter your password",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(Dimensions.SpacerLarge))
        EmailTextField(value = email, onValueChange = {}, enabled = false)
        Spacer(modifier = Modifier.height(Dimensions.SpacerLarge))
        PasswordTextField(
            value = password,
            onValueChange = { onPasswordChange(it) },
            validationState = validationState)
      }
}

/**
 * A composable that displays the sign-up/authentication method selection screen. It shows the
 * user's email (which is non-editable at this stage) and provides options to sign up using a
 * password or Google, as well as an option to go back.
 *
 * @param email The email address of the user, displayed in a disabled text field.
 * @param onPassword A callback function to be invoked when the user chooses to sign up with a
 *   password.
 * @param onGoogle A callback function to be invoked when the user chooses to sign up with Google.
 * @param onBack A callback function to be invoked when the user chooses to go back to the previous
 *   step.
 */
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
            text = "Select your authentication method",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(Dimensions.SpacerLarge))
        EmailTextField(value = email, onValueChange = {}, enabled = false)
      }
}
