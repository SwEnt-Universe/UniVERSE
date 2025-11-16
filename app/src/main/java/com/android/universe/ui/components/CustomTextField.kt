package com.android.universe.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.universe.ui.theme.UniverseTheme

sealed class ValidationState {
  object Valid : ValidationState()

  object Neutral : ValidationState()

  data class Invalid(val errorMessage: String) : ValidationState()
}

private val IconBoxSize = 32.dp

@Composable
fun CustomTextField(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    leadingIcon: ImageVector? = null,
    isPassword: Boolean = false,
    onToggleVisibility: (() -> Unit)? = null,
    maxLines: Int = 1,
    validationState: ValidationState = ValidationState.Neutral,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
  val visualTransformation =
      if (isPassword) {
        PasswordVisualTransformation()
      } else {
        VisualTransformation.None
      }

  val dividerColor =
      when (validationState) { // Use the theme for the colors
        ValidationState.Valid -> UniverseTheme.extendedColors.success
        ValidationState.Neutral -> MaterialTheme.colorScheme.onBackground
        is ValidationState.Invalid -> MaterialTheme.colorScheme.error
      }

  val finalKeyboardOptions =
      if (isPassword) {
        keyboardOptions.copy(keyboardType = KeyboardType.Password)
      } else {
        keyboardOptions
      }

  val focusManager = LocalFocusManager.current

  val (errorText, errorColor) =
      when (validationState) {
        is ValidationState.Invalid ->
            validationState.errorMessage to MaterialTheme.colorScheme.error
        else -> " " to Color.Transparent
      }

  Column(modifier = Modifier.fillMaxWidth()) {
    // 1. The label
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onBackground)

    // 2. The input row (Icon, field, Lock (optional))
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp).height(IconBoxSize),
        verticalAlignment = Alignment.CenterVertically) {
          // 3. Leading icon
          if (leadingIcon != null) {
            Box(modifier = Modifier.size(IconBoxSize), contentAlignment = Alignment.Center) {
              Icon(
                  imageVector = leadingIcon,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.onBackground)
            }
          }

          // 4. The actual text field
          BasicTextField(
              value = value,
              onValueChange = onValueChange,
              visualTransformation = visualTransformation,
              singleLine = maxLines == 1,
              maxLines = maxLines,
              keyboardOptions = finalKeyboardOptions,
              keyboardActions = keyboardActions,
              textStyle =
                  LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onBackground),
              cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
              modifier = Modifier.weight(1f),
              decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                  Text(
                      text = placeholder,
                      style =
                          LocalTextStyle.current.copy(
                              color = UniverseTheme.extendedColors.placeholder))
                }
                innerTextField()
              })

          // 5. Trailing Lock icon
          if (onToggleVisibility != null) {
            Box(modifier = Modifier.size(IconBoxSize), contentAlignment = Alignment.Center) {
              IconButton(
                  onClick = {
                    focusManager.clearFocus()
                    onToggleVisibility()
                  }) {
                    Icon(
                        imageVector =
                            if (isPassword) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = "Toggle visibility",
                        tint = MaterialTheme.colorScheme.onBackground)
                  }
            }
          }
        }

    // 6. The underline
    HorizontalDivider(color = dividerColor, thickness = 1.dp)

    Text(
        text = errorText,
        color = errorColor,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.padding(top = 4.dp),
        maxLines = 1)
  }
}

@Preview
@Composable
fun CustomTextFieldPreview() {
  var email by remember { mutableStateOf("preview@epfl.ch") }
  var password by remember { mutableStateOf("password") }
  var isPasswordSecure by remember { mutableStateOf(true) }
  var description by remember { mutableStateOf("") }

  val emailValidation =
      if (email.isEmpty()) {
        ValidationState.Invalid("Email cannot be empty")
      } else if (!email.contains("@epfl.ch")) {
        ValidationState.Invalid("Must be a valid email")
      } else {
        ValidationState.Valid
      }

  Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
    CustomTextField(
        label = "Email",
        placeholder = "Enter your email...",
        value = email,
        onValueChange = { email = it },
        leadingIcon = Icons.Default.Email,
        validationState = emailValidation)

    CustomTextField(
        label = "Password",
        placeholder = "Enter your password...",
        value = password,
        onValueChange = { password = it },
        leadingIcon = Icons.Default.Lock,
        isPassword = isPasswordSecure,
        onToggleVisibility = { isPasswordSecure = !isPasswordSecure },
        validationState = ValidationState.Neutral)

    CustomTextField(
        label = "Description",
        placeholder = "Enter your description...",
        value = description,
        onValueChange = { description = it },
        leadingIcon = null,
        maxLines = 4,
        validationState = ValidationState.Invalid("Empty description"))
  }
}
