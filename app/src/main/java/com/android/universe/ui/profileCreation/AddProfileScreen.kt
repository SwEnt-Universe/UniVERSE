package com.android.universe.ui.profileCreation

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
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
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
  const val DAY_ERROR = "day_error"

  // Month
  const val MONTH_FIELD = "month_field"
  const val MONTH_ERROR = "month_error"

  // Year
  const val YEAR_FIELD = "year_field"
  const val YEAR_ERROR = "year_error"

  // Save button
  const val SAVE_BUTTON = "save_button"
}

private data class ProfileInputConfig(
    val label: String,
    val placeholder: String,
    val testTagLabel: String,
    val testTagField: String,
    val testTagError: String,
    val showErrorOnTouchOnly: Boolean = true,
    val maxLines: Int = 1
)


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
 * @param uid The user's unique identifier.
 * @param addProfileViewModel The [AddProfileViewModel] that manages the screen's state and business
 *   logic. Defaults to [ViewModel()] for preview and runtime injection.
 * @see AddProfileViewModel
 * @see AddProfileUIState
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProfileScreen(
    uid: String,
    addProfileViewModel: AddProfileViewModel = viewModel(),
    navigateOnSave: () -> Unit = {}
) {
  val profileUIState by addProfileViewModel.uiState.collectAsState()
  val errorMsg = profileUIState.errorMsg
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
            ProfileInputField(
                config = ProfileInputConfig(
                    label = "Username",
                    placeholder = "Username",
                    testTagLabel = AddProfileScreenTestTags.USERNAME_TEXT,
                    testTagField = AddProfileScreenTestTags.USERNAME_FIELD,
                    testTagError = AddProfileScreenTestTags.USERNAME_ERROR,
                ),
                value = profileUIState.username,
                onValueChange = { addProfileViewModel.setUsername(it) },
                error = profileUIState.usernameError,
            )

          // First name field
            ProfileInputField(
                config = ProfileInputConfig(
                    label = "First Name",
                    placeholder = "First Name",
                    testTagLabel = AddProfileScreenTestTags.FIRST_NAME_TEXT,
                    testTagField = AddProfileScreenTestTags.FIRST_NAME_FIELD,
                    testTagError = AddProfileScreenTestTags.FIRST_NAME_ERROR
                ),
                value = profileUIState.firstName,
                onValueChange = { addProfileViewModel.setFirstName(it) },
                error = profileUIState.firstNameError,
            )

          // Last name field
            ProfileInputField(
                config = ProfileInputConfig(
                    label = "Last Name",
                    placeholder = "Last Name",
                    testTagLabel = AddProfileScreenTestTags.LAST_NAME_TEXT,
                    testTagField = AddProfileScreenTestTags.LAST_NAME_FIELD,
                    testTagError = AddProfileScreenTestTags.LAST_NAME_ERROR
                ),
                value = profileUIState.lastName,
                onValueChange = { addProfileViewModel.setLastName(it) },
                error = profileUIState.lastNameError,
            )

          // Description field
          ProfileInputField(
              config = ProfileInputConfig(
                label = "Description",
                placeholder = "Description",
                testTagLabel = AddProfileScreenTestTags.DESCRIPTION_TEXT,
                testTagField = AddProfileScreenTestTags.DESCRIPTION_FIELD,
                testTagError = AddProfileScreenTestTags.DESCRIPTION_ERROR,
                  showErrorOnTouchOnly = false,
                maxLines = 3
              ),
              value = profileUIState.description ?: "",
              onValueChange = { addProfileViewModel.setDescription(it) },
              error = profileUIState.descriptionError,
          )

          // Country selector
            CountrySelectorField(
                value = profileUIState.country,
                onValueChange = { addProfileViewModel.setCountry(it) }
            )

          // Date of Birth section
            DateOfBirthFields(
                uiState = profileUIState,
                viewModel = addProfileViewModel
            )

          Spacer(modifier = Modifier.size(8.dp))

          // Save Button
          Button(
              onClick = { addProfileViewModel.addProfile(uid, onSuccess = navigateOnSave) },
              modifier =
                  Modifier.height(50.dp)
                      .fillMaxWidth()
                      .testTag(AddProfileScreenTestTags.SAVE_BUTTON),
              enabled =
                  profileUIState.usernameError == null &&
                      profileUIState.firstNameError == null &&
                      profileUIState.lastNameError == null &&
                      profileUIState.descriptionError == null &&
                      profileUIState.dayError == null &&
                      profileUIState.monthError == null &&
                      profileUIState.yearError == null,
              colors =
                  ButtonDefaults.buttonColors(
                      containerColor = Color.Black, contentColor = Color.White),
              shape = RoundedCornerShape(12.dp)) {
                Text(text = "Save")
              }
        }
      })
}

@Composable
private fun ProfileInputField(
    config: ProfileInputConfig,
    value: String,
    onValueChange: (String) -> Unit,
    error: String?,
    modifier: Modifier = Modifier,
) {
    var hasBeenTouched by remember { mutableStateOf(false) }

    val showError = if (config.showErrorOnTouchOnly) {
        hasBeenTouched && error != null
    } else {
        error != null
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = config.label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.testTag(config.testTagLabel)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(config.placeholder) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag(config.testTagField)
                .onFocusChanged { focusState ->
                    if (focusState.isFocused && !hasBeenTouched) {
                        hasBeenTouched = true
                    }
                },
            shape = RoundedCornerShape(12.dp),
            singleLine = config.maxLines == 1,
            maxLines = config.maxLines,
            isError = showError
        )
        if (showError) {
            Text(
                text = error!!,
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.testTag(config.testTagError)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CountrySelectorField(
    value: String,
    onValueChange: (String) -> Unit
) {
    var showDropDown by remember { mutableStateOf(false) }

    Text(
        text = "Country",
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.testTag(AddProfileScreenTestTags.COUNTRY_TEXT)
    )
    ExposedDropdownMenuBox(
        expanded = showDropDown,
        onExpandedChange = { showDropDown = !showDropDown }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {}, // Not needed, read-only
            readOnly = true,
            placeholder = { Text("Country") },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
                .testTag(AddProfileScreenTestTags.COUNTRY_FIELD),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showDropDown) }
        )
        ExposedDropdownMenu(
            expanded = showDropDown,
            onDismissRequest = { showDropDown = false },
            modifier = Modifier.heightIn(max = 240.dp).verticalScroll(rememberScrollState())
        ) {
            allCountries.forEach { country ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = country.take(30) + if (country.length > 30) "..." else "",
                            maxLines = 1
                        )
                    },
                    onClick = {
                        onValueChange(country)
                        showDropDown = false
                    },
                    modifier = Modifier.testTag(AddProfileScreenTestTags.COUNTRY_DROPDOWN_ITEM_PREFIX + country)
                )
            }
        }
    }
}

@Composable
private fun DateOfBirthFields(
    uiState: AddProfileUIState,
    viewModel: AddProfileViewModel
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Date of Birth",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.testTag(AddProfileScreenTestTags.DATE_OF_BIRTH_TEXT)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Day input
            ProfileInputField(
                config = ProfileInputConfig(
                    label = "Day",
                    placeholder = "DD",
                    testTagLabel = "ignored",
                    testTagField = AddProfileScreenTestTags.DAY_FIELD,
                    testTagError = AddProfileScreenTestTags.DAY_ERROR
                ),
                value = uiState.day,
                onValueChange = { viewModel.setDay(it) },
                error = uiState.dayError
            )

            // Month input
            ProfileInputField(
                config = ProfileInputConfig(
                    label = "Month",
                    placeholder = "MM",
                    testTagLabel = "ignored",
                    testTagField = AddProfileScreenTestTags.MONTH_FIELD,
                    testTagError = AddProfileScreenTestTags.MONTH_ERROR
                ),
                value = uiState.month,
                onValueChange = { viewModel.setMonth(it) },
                error = uiState.monthError
            )

            // Year input
            ProfileInputField(
                config = ProfileInputConfig(
                    label = "Year",
                    placeholder = "YYYY",
                    testTagLabel = "ignored",
                    testTagField = AddProfileScreenTestTags.YEAR_FIELD,
                    testTagError = AddProfileScreenTestTags.YEAR_ERROR
                ),
                value = uiState.year,
                onValueChange = { viewModel.setYear(it) },
                error = uiState.yearError,
            )
        }
    }
}
