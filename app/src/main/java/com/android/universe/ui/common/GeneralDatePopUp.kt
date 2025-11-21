package com.android.universe.ui.common

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniversalDatePickerDialog(
    modifier: Modifier = Modifier,
    visible: Boolean,
    initialDate: LocalDate,
    yearRange: IntRange,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate?) -> Unit
) {
  if (!visible) return

  val initialMillis =
      remember(initialDate) {
        initialDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
      }

  val pickerState =
      rememberDatePickerState(initialSelectedDateMillis = initialMillis, yearRange = yearRange)

  DatePickerDialog(
      modifier = modifier.testTag(GeneralDatePopUpTestTags.DATE_PICKER_DIALOG),
      onDismissRequest = onDismiss,
      confirmButton = {
        TextButton(
            modifier = Modifier.testTag(GeneralDatePopUpTestTags.CONFIRM_BUTTON),
            onClick = {
              val millis = pickerState.selectedDateMillis
              val date =
                  millis?.let {
                    Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                  }
              onConfirm(date)
            }) {
              Text("OK")
            }
      },
      dismissButton = {
        TextButton(
            onClick = onDismiss,
            modifier = Modifier.testTag(GeneralDatePopUpTestTags.CANCEL_BUTTON)) {
              Text("Cancel")
            }
      }) {
        DatePicker(
            state = pickerState, modifier = Modifier.testTag(GeneralDatePopUpTestTags.DATE_PICKER))
      }
}

object GeneralDatePopUpTestTags {
  val DATE_PICKER_DIALOG = "date_picker_dialog"
  val DATE_PICKER = "date_picker"
  val CONFIRM_BUTTON = "confirm_button"
  val CANCEL_BUTTON = "cancel_button"
}
