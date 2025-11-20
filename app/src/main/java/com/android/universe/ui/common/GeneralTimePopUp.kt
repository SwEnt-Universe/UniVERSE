package com.android.universe.ui.common

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniversalTimePickerDialog(
    modifier: Modifier = Modifier,
    visible: Boolean,
    initialTime: LocalTime,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime?) -> Unit
) {
  if (!visible) return

  val state =
      rememberTimePickerState(
          initialHour = initialTime.hour,
          initialMinute = initialTime.minute,
          is24Hour = true // if we want am/pm we can use false
          )

  TimePickerDialog(
      modifier = modifier.testTag(GeneralTimePopUpTestTags.TIME_PICKER_DIALOG),
      title = { Text("Select Time") },
      onDismissRequest = onDismiss,
      confirmButton = {
        TextButton(
            modifier = Modifier.testTag(GeneralTimePopUpTestTags.CONFIRM_BUTTON),
            onClick = {
              val pickedTime = LocalTime.of(state.hour, state.minute)
              onConfirm(pickedTime)
            }) {
              Text("OK")
            }
      },
      dismissButton = {
        TextButton(
            onClick = onDismiss,
            modifier = Modifier.testTag(GeneralTimePopUpTestTags.CANCEL_BUTTON)) {
              Text("Cancel")
            }
      }) {
        TimePicker(state = state, modifier = Modifier.testTag(GeneralTimePopUpTestTags.TIME_PICKER))
      }
}

object GeneralTimePopUpTestTags {
  val TIME_PICKER_DIALOG = "time_picker_dialog"
  val TIME_PICKER = "time_picker"
  val CONFIRM_BUTTON = "confirm_button"
  val CANCEL_BUTTON = "cancel_button"
}
