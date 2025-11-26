package com.android.universe.ui.components

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
import com.android.universe.ui.common.ValidationState
import com.android.universe.ui.theme.UniverseTheme

/**
 * A private constant to enforce a consistent size for icon touch targets. This helps align the text
 * field correctly, whether or not icons are present.
 */
private val IconBoxSize = 32.dp

/**
 * A highly customized, reusable text input field.
 *
 * This component is "dumb" and "stateless." It relies on state hoisting, where the parent
 * composable provides the `value` and listens for `onValueChange`. It is unstyled (transparent) and
 * meant to be placed on any background, like a `LiquidBox`.
 *
 * @param modifier The modifier to apply to the layout.
 * @param label The text displayed floating above the input field.
 * @param placeholder The hint text displayed inside the field when `value` is empty.
 * @param value The current text value (hoisted state from the parent).
 * @param onValueChange The lambda function called when the user types (hoisted state).
 * @param leadingIcon An optional `ImageVector` to display on the far left.
 * @param isPassword If true, masks the text with '•' dots.
 * @param onToggleVisibility An optional lambda for the trailing "lock" icon to toggle password
 *   visibility.
 * @param maxLines The maximum number of lines the text field can have.
 * @param validationState The current validation state (`Valid`, `Neutral`, `Invalid`) which
 *   controls the underline color and error message.
 * @param keyboardOptions Optional settings to configure the keyboard (e.g., `imeAction`,
 *   `keyboardType`).
 * @param keyboardActions Optional actions to run when a keyboard button (like 'Done' or 'Next') is
 *   pressed.
 * @param enabled Controls the enabled state of the input field. When `false`, this input field will
 *   not be clickable.
 */
@Composable
fun CustomTextField(
    modifier: Modifier = Modifier,
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
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    enabled: Boolean = true
) {
  // Determine if the text should be visually masked (for passwords)
  val visualTransformation =
      if (isPassword) {
        PasswordVisualTransformation() // Shows '•'
      } else {
        VisualTransformation.None // Shows raw text
      }

  // Determine the underline color based on the validation state
  val dividerColor =
      when (validationState) {
        ValidationState.Valid -> UniverseTheme.extendedColors.success
        ValidationState.Neutral -> MaterialTheme.colorScheme.onBackground
        is ValidationState.Invalid -> MaterialTheme.colorScheme.error
      }

  // Automatically set the keyboard type to Password if `isPassword` is true,
  // while preserving any other keyboard options the user passed in.
  val finalKeyboardOptions =
      if (isPassword) {
        keyboardOptions.copy(keyboardType = KeyboardType.Password)
      } else {
        keyboardOptions
      }

  // Get the component that controls keyboard focus
  val focusManager = LocalFocusManager.current

  // The "invisible spacer" trick:
  // We prepare the error message and color. If the state is not Invalid,
  // we use a single space (" ") and a Transparent color.
  // This ensures the Text composable for the error always occupies
  // the same height, preventing the layout from "shifting" when an error appears.
  val (errorText, errorColor) =
      when (validationState) {
        is ValidationState.Invalid ->
            validationState.errorMessage to MaterialTheme.colorScheme.error
        else -> " " to Color.Transparent
      }

  // Main container for the label, input row, and underline
  Column(modifier = Modifier.fillMaxWidth()) {
    // 1. The label
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onBackground)

    // 2. The input row (Icon, field, Lock (optional))
    Row(
        modifier =
            Modifier.fillMaxWidth()
                .padding(top = 4.dp)
                // Force the Row to a consistent height, matching the icon size.
                // This prevents the underline from moving up/down if icons are absent.
                .height(IconBoxSize),
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
              enabled = enabled,
              visualTransformation = visualTransformation,
              singleLine = maxLines == 1,
              maxLines = maxLines,
              keyboardOptions = finalKeyboardOptions,
              keyboardActions = keyboardActions,
              textStyle =
                  LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onBackground),
              cursorBrush =
                  SolidColor(MaterialTheme.colorScheme.primary), // Set the blinking cursor color
              modifier = modifier.weight(1f),
              decorationBox = { innerTextField
                -> // `decorationBox` lets us build the UI *around* the core text field
                if (value.isEmpty()) {
                  Text(
                      text = placeholder,
                      style =
                          LocalTextStyle.current.copy(
                              color = UniverseTheme.extendedColors.placeholder))
                }
                innerTextField()
              })

          // 5. Trailing Lock icon (Optional)
          if (onToggleVisibility != null) {
            Box(modifier = Modifier.size(IconBoxSize), contentAlignment = Alignment.Center) {
              IconButton(
                  onClick = {
                    // IMPORTANT: Clear focus *first*!
                    // This prevents the keyboard from popping up again
                    // when the user taps the icon.
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

    // 7. The error message area
    // This Text is *always* rendered, even if there's no error.
    // When there's no error, it renders as " " with a Transparent color,
    // which invisibly reserves the space and prevents layout shifts.
    Text(
        text = errorText,
        color = errorColor,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.padding(top = 4.dp),
        maxLines = 1)
  }
}

/** A preview composable to visualize the [CustomTextField] in different states. */
@Preview
@Composable
private fun CustomTextFieldPreview() {
  var email by remember { mutableStateOf("preview@epfl.ch") }

  val emailValidation =
      if (email.isEmpty()) {
        ValidationState.Invalid("Email cannot be empty")
      } else if (!email.contains("@epfl.ch")) {
        ValidationState.Invalid("Must be a valid email")
      } else {
        ValidationState.Valid
      }

  CustomTextField(
      label = "Email",
      placeholder = "Enter your email...",
      value = email,
      onValueChange = {},
      leadingIcon = Icons.Default.Email,
      validationState = emailValidation)
}
