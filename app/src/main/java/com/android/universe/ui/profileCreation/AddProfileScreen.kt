package com.android.universe.ui.profileCreation

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.universe.model.CountryData.allCountries
import com.android.universe.ui.navigation.NavigationTestTags
import com.android.universe.ui.theme.Dimensions
import com.android.universe.ui.theme.Dimensions.PaddingLarge
import com.android.universe.ui.theme.Dimensions.PaddingMedium
import com.android.universe.ui.theme.UniverseTheme

/**
 * Defines constants for use in UI tests to identify specific composables within the
 * AddProfileScreen. This helps create robust and readable tests.
 */
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

  // Back button
  const val BACK_BUTTON = "back_button"
  const val DROPDOWN_SCROLLING_MENU = "dropdown_scrolling_menu"
}

/**
 * A data class that holds the configuration for a [ProfileInputField].
 *
 * This class was created to reduce the number of parameters passed to the [ProfileInputField]
 * composable, improving readability and satisfying code quality metrics (e.g,. Sonar's max
 * parameter rule). It groups related display and testing parameters into a single, logical unit.
 *
 * @param label The Text displayed above the input field.
 * @param placeholder The hint text displayed inside the field when it's empty.
 * @param testTagLabel The test tag for the label Text.
 * @param testTagField The test tag for the outlinedTextField.
 * @param testTagError The test tag for the error message Text.
 * @param useInlineLabel If true, the `label` is shown inside the OutlinedTextField's border. If
 *   false, it's shown as a separate `Text` composable above the field.
 * @param showErrorOnTouchOnly If true, the error message is only shown after the field has been
 *   focused and then unfocused. If false, the error is shown immediately.
 * @param maxLines The maximum number of lines the input field can have.
 */
private data class ProfileInputConfig(
    val label: String,
    val placeholder: String,
    val testTagLabel: String,
    val testTagField: String,
    val testTagError: String,
    val useInlineLabel: Boolean = false,
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
 * automatically updates the UI when state changes. It is structured using reusable child
 * composables like [ProfileInputField], [CountrySelectorField], and [DateOfBirthFields] to improve
 * modularity and reduce complexity.
 *
 * @param uid The user's unique identifier.
 * @param addProfileViewModel The [AddProfileViewModel] that manages the screen's state and business
 *   logic. Defaults to [ViewModel()] for preview and runtime injection.
 * @param navigateOnSave A callback function to be invoked upon successful profile creation to
 *   handle navigation.
 * @param onBack A callback function invoked when the user taps the top bar's back arrow. Used to
 *   navigate back to the previous screen (Sign In) in the navigation stack.
 * @see AddProfileViewModel
 * @see AddProfileUIState
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProfileScreen(
    uid: String,
    addProfileViewModel: AddProfileViewModel = viewModel(),
    navigateOnSave: () -> Unit = {},
    onBack: () -> Unit = {}
) {
  val profileUIState by addProfileViewModel.uiState.collectAsState()

  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text("Create Profile") },
            navigationIcon = {
              IconButton(
                  onClick = onBack,
                  modifier = Modifier.testTag(AddProfileScreenTestTags.BACK_BUTTON)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Login")
                  }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
            ),
            modifier = Modifier.testTag(NavigationTestTags.ADD_PROFILE_SCREEN))
      },
      containerColor = Color.Transparent,
      content = { paddingValues ->
        Column(
            modifier = Modifier.fillMaxWidth().padding(PaddingLarge).padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(PaddingMedium),
        ) {

          // Username field
          ProfileInputField(
              config =
                  ProfileInputConfig(
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
              config =
                  ProfileInputConfig(
                      label = "First Name",
                      placeholder = "First Name",
                      testTagLabel = AddProfileScreenTestTags.FIRST_NAME_TEXT,
                      testTagField = AddProfileScreenTestTags.FIRST_NAME_FIELD,
                      testTagError = AddProfileScreenTestTags.FIRST_NAME_ERROR),
              value = profileUIState.firstName,
              onValueChange = { addProfileViewModel.setFirstName(it) },
              error = profileUIState.firstNameError,
          )

          // Last name field
          ProfileInputField(
              config =
                  ProfileInputConfig(
                      label = "Last Name",
                      placeholder = "Last Name",
                      testTagLabel = AddProfileScreenTestTags.LAST_NAME_TEXT,
                      testTagField = AddProfileScreenTestTags.LAST_NAME_FIELD,
                      testTagError = AddProfileScreenTestTags.LAST_NAME_ERROR),
              value = profileUIState.lastName,
              onValueChange = { addProfileViewModel.setLastName(it) },
              error = profileUIState.lastNameError,
          )

          // Description field
          ProfileInputField(
              config =
                  ProfileInputConfig(
                      label = "Description",
                      placeholder = "Description",
                      testTagLabel = AddProfileScreenTestTags.DESCRIPTION_TEXT,
                      testTagField = AddProfileScreenTestTags.DESCRIPTION_FIELD,
                      testTagError = AddProfileScreenTestTags.DESCRIPTION_ERROR,
                      showErrorOnTouchOnly = false,
                      maxLines = 3),
              value = profileUIState.description ?: "",
              onValueChange = { addProfileViewModel.setDescription(it) },
              error = profileUIState.descriptionError,
          )

          // Country selector
          CountrySelectorField(
              value = profileUIState.country, addProfileViewModel = addProfileViewModel)

          // Date of Birth section
          DateOfBirthFields(uiState = profileUIState, viewModel = addProfileViewModel)

          Spacer(modifier = Modifier.size(Dimensions.SpacerMedium))

          // Save Button
          Button(
              onClick = { addProfileViewModel.addProfile(uid, onSuccess = navigateOnSave) },
              modifier =
                  Modifier.height(50.dp)
                      .fillMaxWidth()
                      .testTag(AddProfileScreenTestTags.SAVE_BUTTON),
              enabled =
                  profileUIState.username.isNotBlank() &&
                      profileUIState.firstName.isNotBlank() &&
                      profileUIState.lastName.isNotBlank() &&
                      profileUIState.country.isNotBlank() &&
                      profileUIState.day.isNotBlank() &&
                      profileUIState.month.isNotBlank() &&
                      profileUIState.year.isNotBlank() &&
                      profileUIState.usernameError == null &&
                      profileUIState.firstNameError == null &&
                      profileUIState.lastNameError == null &&
                      profileUIState.descriptionError == null &&
                      profileUIState.dayError == null &&
                      profileUIState.monthError == null &&
                      profileUIState.yearError == null,
              colors =
                  ButtonDefaults.buttonColors(
                      containerColor = MaterialTheme.colorScheme.primary,
                      contentColor = MaterialTheme.colorScheme.onPrimary),
              shape = RoundedCornerShape(Dimensions.RoundedCorner)) {
                Text(text = "Save")
              }
        }
      })
}

/**
 * A generic, reusable composable for displaying a labeled text input field.
 *
 * This component encapsulates the common UI pattern of a `Text` label, an `OutlinedTextField`, and
 * a `Text` for displaying validation errors. It manages its own "touched" state to control when
 * validation errors become visible, making the main screen's logic cleaner.
 *
 * @param config The [ProfileInputConfig] containing display and testing parameters.
 * @param value The current text value of the input field.
 * @param onValueChange The callback invoked when the user types in the field.
 * @param error The validation error message to display. If null, no error is shown.
 * @param modifier The modifier to be applied to the `Column` wrapping the component.
 */
@Composable
private fun ProfileInputField(
    config: ProfileInputConfig,
    value: String,
    onValueChange: (String) -> Unit,
    error: String?,
    modifier: Modifier = Modifier,
) {
  var hasBeenTouched by remember { mutableStateOf(false) }

  val showError =
      if (config.showErrorOnTouchOnly) {
        hasBeenTouched && error != null
      } else {
        error != null
      }

  Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Dimensions.SpacerSmall)) {
    if (!config.useInlineLabel) {
      Text(
          text = config.label,
          style = MaterialTheme.typography.bodyLarge,
          modifier = Modifier.testTag(config.testTagLabel))
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label =
            if (config.useInlineLabel) {
              { Text(config.label) }
            } else null,
        placeholder = { Text(config.placeholder) },
        modifier =
            Modifier.fillMaxWidth().testTag(config.testTagField).onFocusChanged { focusState ->
              if (focusState.isFocused && !hasBeenTouched) {
                hasBeenTouched = true
              }
            },
        shape = RoundedCornerShape(Dimensions.RoundedCorner),
        singleLine = config.maxLines == 1,
        maxLines = config.maxLines,
        isError = showError)

    if (showError) {
      Text(
          text = error!!,
          color = MaterialTheme.colorScheme.error,
          style = MaterialTheme.typography.bodySmall,
          modifier = Modifier.testTag(config.testTagError))
    }
  }
}

/**
 * A specialized composable for selecting a country from a dropdown menu.
 *
 * It uses an [ExposedDropdownMenuBox] to present a list of countries from [allCountries]. This
 * component manages its own expanded state (`showDropDown`).
 *
 * @param value The currently selected country name.
 * @param addProfileViewModel The [AddProfileViewModel] instance used to set the new country value
 *   when a selection is made.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CountrySelectorField(value: String, addProfileViewModel: AddProfileViewModel) {
  var showDropDown by remember { mutableStateOf(false) }

  Text(
      text = "Country",
      style = MaterialTheme.typography.bodyLarge,
      modifier = Modifier.testTag(AddProfileScreenTestTags.COUNTRY_TEXT))
  ExposedDropdownMenuBox(
      expanded = showDropDown, onExpandedChange = { showDropDown = !showDropDown }) {
        OutlinedTextField(
            value = value,
            onValueChange = {}, // Not needed, read-only
            readOnly = true,
            placeholder = { Text("Country") },
            modifier =
                Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth()
                    .testTag(AddProfileScreenTestTags.COUNTRY_FIELD),
            shape = RoundedCornerShape(Dimensions.RoundedCorner),
            singleLine = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showDropDown) })
        ExposedDropdownMenu(
            expanded = showDropDown,
            onDismissRequest = { showDropDown = false },
            scrollState = rememberScrollState(),
            modifier =
                Modifier.heightIn(max = 240.dp)
                    .testTag(AddProfileScreenTestTags.DROPDOWN_SCROLLING_MENU)) {
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
                        Modifier.testTag(
                            "${AddProfileScreenTestTags.COUNTRY_DROPDOWN_ITEM_PREFIX}$country"))
              }
            }
      }
}

/**
 * A composable that groups the Day, Month, and Year input fields for the date of birth.
 *
 * This component provides a clear structural grouping for the date fields and uses the reusable
 * [ProfileInputField] for each part of the date, laid out in a [Row].
 *
 * @param uiState The current [AddProfileUIState] containing the date values and their errors.
 * @param viewModel The [AddProfileViewModel] to call for updating date values.
 */
@Composable
private fun DateOfBirthFields(uiState: AddProfileUIState, viewModel: AddProfileViewModel) {
  Column(verticalArrangement = Arrangement.spacedBy(Dimensions.SpacerMedium)) {
    Text(
        text = "Date of Birth",
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.testTag(AddProfileScreenTestTags.DATE_OF_BIRTH_TEXT))
    Row(horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacerLarge)) {
      // Day input
      ProfileInputField(
          modifier = Modifier.weight(1f),
          config =
              ProfileInputConfig(
                  label = "Day",
                  placeholder = "DD",
                  useInlineLabel = true,
                  testTagLabel = "ignored",
                  testTagField = AddProfileScreenTestTags.DAY_FIELD,
                  testTagError = AddProfileScreenTestTags.DAY_ERROR),
          value = uiState.day,
          onValueChange = { viewModel.setDay(it) },
          error = uiState.dayError)

      // Month input
      ProfileInputField(
          modifier = Modifier.weight(1f),
          config =
              ProfileInputConfig(
                  label = "Month",
                  placeholder = "MM",
                  useInlineLabel = true,
                  testTagLabel = "ignored",
                  testTagField = AddProfileScreenTestTags.MONTH_FIELD,
                  testTagError = AddProfileScreenTestTags.MONTH_ERROR),
          value = uiState.month,
          onValueChange = { viewModel.setMonth(it) },
          error = uiState.monthError)

      // Year input
      ProfileInputField(
          modifier = Modifier.weight(1.5f),
          config =
              ProfileInputConfig(
                  label = "Year",
                  placeholder = "YYYY",
                  useInlineLabel = true,
                  testTagLabel = "ignored",
                  testTagField = AddProfileScreenTestTags.YEAR_FIELD,
                  testTagError = AddProfileScreenTestTags.YEAR_ERROR),
          value = uiState.year,
          onValueChange = { viewModel.setYear(it) },
          error = uiState.yearError,
      )
    }
  }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AddProfileScreenPreview() {
  UniverseTheme {
    // A no-op fake ViewModel substitute
    val dummyViewModel = object : AddProfileViewModel() {}

    // Just render the UI (no real logic, safe for preview)
    AddProfileScreen(
        uid = "preview_user_001",
        addProfileViewModel = dummyViewModel,
        navigateOnSave = {},
    )
  }
}
