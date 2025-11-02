package com.android.universe.ui.common

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

/**
 * A composable function that displays a button for logging out.
 *
 * @param onClick A lambda function that is called when the button is clicked.
 */
@Composable
fun LogoutButton(
    onClick: () -> Unit = {},
    text: String = "Logout",
    icon: ImageVector = Icons.AutoMirrored.Outlined.Logout
) {
  Button(
      onClick = onClick,
      colors =
          ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.surfaceContainer,
              contentColor = MaterialTheme.colorScheme.onSurface),
      shape = MaterialTheme.shapes.medium,
      elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
      modifier = Modifier.testTag(LogoutTestTags.LOGOUT_BUTTON)) {
        Icon(
            imageVector = icon,
            contentDescription = "Logout",
        )
        Spacer(Modifier.width(8.dp))
        Text(text)
      }
}

/**
 * A composable function that displays a confirmation dialog for logging out.
 *
 * @param showDialog A boolean value indicating whether the dialog should be displayed or not.
 * @param onConfirm A lambda function that is called when the user confirms the logout action.
 * @param onDismiss A lambda function that is called when the user dismisses the dialog.
 */
@Composable
fun LogoutConfirmationDialog(showDialog: Boolean, onConfirm: () -> Unit, onDismiss: () -> Unit) {
  if (showDialog) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
          Text(
              "Are you sure you want to log out?",
              modifier = Modifier.testTag(LogoutTestTags.ALERT_TITLE))
        },
        text = {
          Text(
              "Any unsaved changes will be discarded.",
              modifier = Modifier.testTag(LogoutTestTags.ALERT_TEXT))
        },
        confirmButton = {
          TextButton(
              onClick = onConfirm,
              modifier = Modifier.testTag(LogoutTestTags.ALERT_CONFIRM_BUTTON)) {
                Text("Logout", color = MaterialTheme.colorScheme.onSurface)
              }
        },
        dismissButton = {
          TextButton(
              onClick = onDismiss,
              modifier = Modifier.testTag(LogoutTestTags.ALERT_CANCEL_BUTTON)) {
                Text("Cancel")
              }
        },
        modifier = Modifier.testTag(LogoutTestTags.ALERT_DIALOG))
  }
}

object LogoutTestTags {
  const val LOGOUT_BUTTON = "logout_button"
  const val ALERT_DIALOG = "alert_dialog"
  const val ALERT_TITLE = "alert_title"
  const val ALERT_TEXT = "alert_text"
  const val ALERT_CONFIRM_BUTTON = "alert_confirm_button"
  const val ALERT_CANCEL_BUTTON = "alert_cancel_button"
}
