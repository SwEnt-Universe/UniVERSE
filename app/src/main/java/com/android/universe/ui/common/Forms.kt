package com.android.universe.ui.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

object FormTestTags {
  const val FORM_ERROR = "form_error"
  const val FORM_HINT = "form_hint"
  const val PASSWORD_FIELD = "password_field"
  const val EMAIL_FIELD = "email_field"
}

@Composable
fun TextInputField(
    value: String,
    onValueChange: (String) -> Unit,
    errorMsg: String?,
    hint: String?,
    tag: String = "",
    minLines: Int = 1,
    maxLines: Int = 1
) {
  OutlinedTextField(
      value = value,
      onValueChange = onValueChange,
      placeholder = { FormHint(hint) },
      label = { FormHint(hint) },
      modifier = Modifier.fillMaxWidth().testTag(tag),
      shape = RoundedCornerShape(12.dp),
      singleLine = true,
      minLines = minLines,
      maxLines = maxLines,
      supportingText = { FormError(errorMsg) })
}

@Composable
fun EmailInputField(value: String, onValueChange: (String) -> Unit, errorMsg: String?) {
  TextInputField(
      value = value,
      onValueChange = onValueChange,
      errorMsg = errorMsg,
      hint = "Email",
      tag = FormTestTags.EMAIL_FIELD)
}

@Composable
fun PasswordInputField(value: String, onValueChange: (String) -> Unit, errorMsg: String?) {
  val hint = "Password"
  OutlinedTextField(
      value = value,
      onValueChange = onValueChange,
      placeholder = { FormHint(hint) },
      label = { FormHint(hint) },
      modifier = Modifier.fillMaxWidth().testTag(FormTestTags.PASSWORD_FIELD),
      visualTransformation = PasswordVisualTransformation(),
      keyboardOptions =
          KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
      shape = RoundedCornerShape(12.dp),
      singleLine = true,
      supportingText = { FormError(errorMsg) })
}

@Composable
fun FormHint(hint: String?) {
  hint?.let { Text(text = it, modifier = Modifier.testTag(tag = FormTestTags.FORM_HINT)) }
}

@Composable
fun FormError(msg: String?) {
  msg?.let { Text(text = it, modifier = Modifier.testTag(tag = FormTestTags.FORM_ERROR)) }
}
