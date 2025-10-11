package com.android.universe.ui.overview

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.universe.model.CountryData.allCountries

object AddProfileScreenTestTags {
  // Username
  const val USERNAME_TEXT = "username_text"
  const val USERNAME_FIELD = "username_field"
  const val USERNAME_ERROR = "username_error"

  // First Name
  const val FIRST_NAME_TEXT = "first_name_text"
  const val FIRST_NAME_FIELD = "first_name_field"
  const val FIRST_NAME_ERROR = "first_name_error"

  // Last Name
  const val LAST_NAME_TEXT = "last_name_text"
  const val LAST_NAME_FIELD = "last_name_field"
  const val LAST_NAME_ERROR = "last_name_error"

  // Description
  const val DESCRIPTION_TEXT = "description_text"
  const val DESCRIPTION_FIELD = "description_field"
    const val DESCRIPTION_ERROR = "description_error"

  // Country
  const val COUNTRY_TEXT = "country_text"
  const val COUNTRY_FIELD = "country_field"
  const val COUNTRY_DROPDOWN_ITEM_PREFIX = "country_item_"

  // Date of birth
  const val DATE_OF_BIRTH_TEXT = "date_of_birth_text"

  // Day
  const val DAY_FIELD = "day_field"
  const val DAY_ERROR_EMPTY = "day_error_empty"
  const val DAY_ERROR_NUMBER = "day_error_number"

  // Month
  const val MONTH_FIELD = "month_field"
  const val MONTH_ERROR_EMPTY = "month_error_empty"
  const val MONTH_ERROR_NUMBER = "month_error_number"

  // Year
  const val YEAR_FIELD = "year_field"
  const val YEAR_ERROR_EMPTY = "year_error_empty"
  const val YEAR_ERROR_NUMBER = "year_error_number"

  // Save button
  const val SAVE_BUTTON = "save_button"
}

/**
 * Composable screen that allows a user to create a new Profile.
 *
 * This screen collects user input for all profile fields (username, name, description, country and
 * date of birth) and validates the data both locally and through [AddProfileViewModel]. It displays
 * inline validation messages and shows errors as Toast notifications for asynchronous validation
 * results.
 *
 * The composable is reactive: it observes [AddProfileViewModel.uiState] using [collectAsState] and
 * automatically updates the UI when state changes.
 *
 * Structure
 * - Username / First name / last name / description fields are implemented using
 *   [OutlinedTextField] with local touch-tracking for validation messages.
 * - Country selection uses an [ExposedDropdownMenuBox].
 * - Date of birth fields (day, month, year) are validated individually.
 * - A Save button triggers profile creation through [AddProfileViewModel.addProfile].
 *
 * Validation
 * - Inline errors are shown once a field has been focused ("touched").
 * - Errors from the ViewModel are displayed as Toast messages.
 *
 * @param addProfileViewModel The [AddProfileViewModel] that manages the screen's state and business
 *   logic. Defaults to [ViewModel()] for preview and runtime injection.
 * @see AddProfileViewModel
 * @see AddProfileUIState
 */
@Preview(showBackground = true)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProfileScreen(addProfileViewModel: AddProfileViewModel = viewModel()) {
  val profileUIState by addProfileViewModel.uiState.collectAsState()
  val errorMsg = profileUIState.errorMsg

  var hasTouchedUsername by remember { mutableStateOf(false) }
  var hasTouchedFirstName by remember { mutableStateOf(false) }
  var hasTouchedLastName by remember { mutableStateOf(false) }
  var hasTouchedDay by remember { mutableStateOf(false) }
  var hasTouchedMonth by remember { mutableStateOf(false) }
  var hasTouchedYear by remember { mutableStateOf(false) }
  var showDropDown by remember { mutableStateOf(false) }

  val context = LocalContext.current

  // Observe and display asynchronous validation errors as Toast messages.
  LaunchedEffect(errorMsg) {
    if (errorMsg != null) {
      Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
      addProfileViewModel.clearErrorMsg()
    }
  }

  Scaffold(
      content = { paddingValues ->
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp).padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {

          // Username field
          Text(
              text = "Username",
              style = MaterialTheme.typography.bodyLarge,
              modifier = Modifier.testTag(AddProfileScreenTestTags.USERNAME_TEXT))
          OutlinedTextField(
              value = profileUIState.username,
              onValueChange = { addProfileViewModel.setUsername(it) },
              placeholder = { Text("Username") },
              modifier =
                  Modifier.fillMaxWidth()
                      .testTag(AddProfileScreenTestTags.USERNAME_FIELD)
                      .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                          hasTouchedUsername = true
                        }
                      },
              shape = RoundedCornerShape(12.dp),
              singleLine = true)
            val usernamePair = addProfileViewModel.validUsername(profileUIState.username)
            if(hasTouchedUsername && !usernamePair.first){
                Text(
                    text = usernamePair.second,
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.testTag(AddProfileScreenTestTags.USERNAME_ERROR))
            }

          // First name field
          Text(
              text = "First Name",
              style = MaterialTheme.typography.bodyLarge,
              modifier = Modifier.testTag(AddProfileScreenTestTags.FIRST_NAME_TEXT))
          OutlinedTextField(
              value = profileUIState.firstName,
              onValueChange = { addProfileViewModel.setFirstName(it) },
              placeholder = { Text("First Name") },
              modifier =
                  Modifier.fillMaxWidth()
                      .testTag(AddProfileScreenTestTags.FIRST_NAME_FIELD)
                      .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                          hasTouchedFirstName = true
                        }
                      },
              shape = RoundedCornerShape(12.dp),
              singleLine = true)
            val firstNamePair = addProfileViewModel.validFirstName(profileUIState.firstName)
          if (hasTouchedFirstName && !firstNamePair.first) {
            Text(
                text = firstNamePair.second,
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.testTag(AddProfileScreenTestTags.FIRST_NAME_ERROR))
          }

          // Last name field
          Text(
              text = "Last Name",
              style = MaterialTheme.typography.bodyLarge,
              modifier = Modifier.testTag(AddProfileScreenTestTags.LAST_NAME_TEXT))
          OutlinedTextField(
              value = profileUIState.lastName,
              onValueChange = { addProfileViewModel.setLastName(it) },
              placeholder = { Text("Last Name") },
              modifier =
                  Modifier.fillMaxWidth()
                      .testTag(AddProfileScreenTestTags.LAST_NAME_FIELD)
                      .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                          hasTouchedLastName = true
                        }
                      },
              shape = RoundedCornerShape(12.dp),
              singleLine = true)
            val lastNamePair = addProfileViewModel.validLastName(profileUIState.lastName)
          if (hasTouchedLastName && !lastNamePair.first) {
            Text(
                text = lastNamePair.second,
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.testTag(AddProfileScreenTestTags.LAST_NAME_ERROR))
          }

          // Description field
          Text(
              text = "Description",
              style = MaterialTheme.typography.bodyLarge,
              modifier = Modifier.testTag(AddProfileScreenTestTags.DESCRIPTION_TEXT))
          OutlinedTextField(
              value = profileUIState.description ?: "",
              onValueChange = { addProfileViewModel.setDescription(it) },
              placeholder = { Text("Description") },
              modifier =
                  Modifier.fillMaxWidth().testTag(AddProfileScreenTestTags.DESCRIPTION_FIELD),
              shape = RoundedCornerShape(12.dp), maxLines = 3, singleLine = false)
            val descriptionPair = addProfileViewModel.validDescription(profileUIState.description)
            if (!descriptionPair.first) {
                Text(
                    text = descriptionPair.second,
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.testTag(AddProfileScreenTestTags.DESCRIPTION_ERROR))
            }

          // Country selector
          Text(
              text = "Country",
              style = MaterialTheme.typography.bodyLarge,
              modifier = Modifier.testTag(AddProfileScreenTestTags.COUNTRY_TEXT))
          ExposedDropdownMenuBox(
              expanded = showDropDown, onExpandedChange = { showDropDown = !showDropDown }) {
                OutlinedTextField(
                    value = profileUIState.country,
                    onValueChange = { addProfileViewModel.setCountry(it) },
                    readOnly = true,
                    label = { Text(text = "Country") },
                    placeholder = { Text("Country") },
                    modifier =
                        Modifier.menuAnchor()
                            .fillMaxWidth()
                            .testTag(AddProfileScreenTestTags.COUNTRY_FIELD),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true)
                ExposedDropdownMenu(
                    expanded = showDropDown,
                    onDismissRequest = { showDropDown = false },
                    modifier =
                        Modifier.heightIn(max = 240.dp).verticalScroll(rememberScrollState())) {
                      allCountries.forEach { country ->
                        DropdownMenuItem(
                            text = {
                              Text(
                                  text = country.take(30) + if (country.length > 30) "..." else "",
                                  maxLines = 1)
                            },
                            onClick = {
                              addProfileViewModel.setCountry(country)
                              showDropDown = false
                            },
                            modifier =
                                Modifier.padding(8.dp)
                                    .testTag(AddProfileScreenTestTags.COUNTRY_DROPDOWN_ITEM_PREFIX))
                      }
                    }
              }

          // Date of Birth section
          Text(
              text = "Date of Birth",
              style = MaterialTheme.typography.bodyLarge,
              modifier = Modifier.testTag(AddProfileScreenTestTags.DATE_OF_BIRTH_TEXT))
          Row(
              modifier = Modifier.fillMaxWidth().padding(paddingValues),
              horizontalArrangement = Arrangement.spacedBy(32.dp)) {

                // Day input
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                      OutlinedTextField(
                          value = profileUIState.day,
                          onValueChange = { addProfileViewModel.setDay(it) },
                          label = { Text(text = "Day") },
                          placeholder = { Text("Day") },
                          modifier =
                              Modifier.fillMaxWidth()
                                  .testTag(AddProfileScreenTestTags.DAY_FIELD)
                                  .onFocusChanged { focusState ->
                                    if (focusState.isFocused) {
                                      hasTouchedDay = true
                                    }
                                  },
                          shape = RoundedCornerShape(12.dp),
                          singleLine = true)
                      if (hasTouchedDay && profileUIState.day.isBlank()) {
                        Text(
                            text = "Day cannot be empty",
                            color = Color.Red,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.testTag(AddProfileScreenTestTags.DAY_ERROR_EMPTY))
                      } else if (hasTouchedDay && profileUIState.day.toIntOrNull() == null) {
                        Text(
                            text = "Day need to be a number",
                            color = Color.Red,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.testTag(AddProfileScreenTestTags.DAY_ERROR_NUMBER))
                      }
                    }

                // Month input
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                      OutlinedTextField(
                          value = profileUIState.month,
                          onValueChange = { addProfileViewModel.setMonth(it) },
                          label = { Text(text = "Month") },
                          placeholder = { Text("Month") },
                          modifier =
                              Modifier.fillMaxWidth()
                                  .testTag(AddProfileScreenTestTags.MONTH_FIELD)
                                  .onFocusChanged { focusState ->
                                    if (focusState.isFocused) {
                                      hasTouchedMonth = true
                                    }
                                  },
                          shape = RoundedCornerShape(12.dp),
                          singleLine = true)
                      if (hasTouchedMonth && profileUIState.month.isBlank()) {
                        Text(
                            text = "Month cannot be empty",
                            color = Color.Red,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.testTag(AddProfileScreenTestTags.MONTH_ERROR_EMPTY))
                      } else if (hasTouchedMonth && profileUIState.month.toIntOrNull() == null) {
                        Text(
                            text = "Month need to be a number",
                            color = Color.Red,
                            style = MaterialTheme.typography.bodySmall,
                            modifier =
                                Modifier.testTag(AddProfileScreenTestTags.MONTH_ERROR_NUMBER))
                      }
                    }

                // Year input
                Column(
                    modifier = Modifier.weight(1.5f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                      OutlinedTextField(
                          value = profileUIState.year,
                          onValueChange = { addProfileViewModel.setYear(it) },
                          label = { Text(text = "Year") },
                          placeholder = { Text("Year") },
                          modifier =
                              Modifier.fillMaxWidth()
                                  .testTag(AddProfileScreenTestTags.YEAR_FIELD)
                                  .onFocusChanged { focusState ->
                                    if (focusState.isFocused) {
                                      hasTouchedYear = true
                                    }
                                  },
                          shape = RoundedCornerShape(12.dp),
                          singleLine = true)
                      if (hasTouchedYear && profileUIState.year.isBlank()) {
                        Text(
                            text = "Year cannot be empty",
                            color = Color.Red,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.testTag(AddProfileScreenTestTags.YEAR_ERROR_EMPTY))
                      } else if (hasTouchedYear && profileUIState.year.toIntOrNull() == null) {
                        Text(
                            text = "Year need to be a number",
                            color = Color.Red,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.testTag(AddProfileScreenTestTags.YEAR_ERROR_NUMBER))
                      }
                    }
              }

          Spacer(modifier = Modifier.size(8.dp))

          // Save Button
          Button(
              onClick = { addProfileViewModel.addProfile() },
              modifier =
                  Modifier.height(50.dp)
                      .fillMaxWidth()
                      .testTag(AddProfileScreenTestTags.SAVE_BUTTON),
              enabled =
                  profileUIState.username.isNotBlank() &&
                      profileUIState.firstName.isNotBlank() &&
                      profileUIState.lastName.isNotBlank() &&
                      profileUIState.day.isNotBlank() &&
                      profileUIState.day.toIntOrNull() != null &&
                      profileUIState.month.isNotBlank() &&
                      profileUIState.month.toIntOrNull() != null &&
                      profileUIState.year.isNotBlank() &&
                      profileUIState.year.toIntOrNull() != null,
              colors =
                  ButtonDefaults.buttonColors(
                      containerColor = Color.Black, contentColor = Color.White),
              shape = RoundedCornerShape(12.dp)) {
                Text(text = "Save")
              }
        }
      })
}
