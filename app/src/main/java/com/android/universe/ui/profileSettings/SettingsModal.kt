package com.android.universe.ui.profileSettings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.android.universe.model.CountryData
import com.android.universe.model.tag.Tag
import com.android.universe.ui.common.TagGroup

/**
 * A composable dropdown component used for selecting a country.
 *
 * Displayed inside the profile settings modal when the user edits their country. The dropdown shows
 * all available countries defined in [CountryData.allCountries] and updates its selection via
 * [onPick].
 *
 * @param value The currently selected country name.
 * @param expanded Whether the dropdown menu is currently expanded.
 * @param onExpandedChange Callback triggered when the dropdown expansion state changes.
 * @param onPick Callback invoked when a user selects a country from the list.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CountryDropdown(
    value: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onPick: (String) -> Unit
) {
  ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = onExpandedChange) {
    OutlinedTextField(
        value = value,
        onValueChange = { /* readOnly */ },
        readOnly = true,
        modifier =
            Modifier.fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                .testTag(SettingsTestTags.COUNTRY_DROPDOWN_FIELD),
        shape = RoundedCornerShape(12.dp))
    ExposedDropdownMenu(
        expanded = expanded,
        onDismissRequest = { onExpandedChange(false) },
        modifier = Modifier.heightIn(max = 240.dp)) {
          CountryData.allCountries.forEach { option ->
            DropdownMenuItem(
                modifier = Modifier.testTag("${SettingsTestTags.COUNTRY_OPTION_PREFIX}$option"),
                text = {
                  val label = if (option.length > 30) option.take(30) + "..." else option
                  Text(label)
                },
                onClick = { onPick(option) })
          }
        }
  }
}

/**
 * The core content of the profile settings modal.
 *
 * This composable adapts its layout dynamically based on the currently active field
 * (`uiState.currentField`), showing different input controls for:
 * - Text fields (email, password, name, description)
 * - Country selection (dropdown)
 * - Date of birth (three separate fields)
 * - Tag-based interest groups
 *
 * The modal also contains a header with the field title and action buttons ("Cancel" / "Save"),
 * both of which can trigger callbacks for closing or persisting changes.
 *
 * @param uiState The current UI state containing temporary form values and error messages.
 * @param onUpdateTemp Called when a temporary field value changes (e.g., typing in an input box).
 * @param onToggleCountryDropdown Called to expand or collapse the country dropdown.
 * @param onAddTag Adds a tag to the user’s temporary selection when a new tag is selected.
 * @param onRemoveTag Removes a tag from the user’s temporary selection when deselected.
 * @param onClose Invoked when the user presses the "Cancel" button.
 * @param onSave Invoked when the user presses the "Save" button.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ModalContent(
    uiState: SettingsUiState,
    onUpdateTemp: (String, String) -> Unit,
    onToggleCountryDropdown: (Boolean) -> Unit,
    onAddTag: (Tag) -> Unit,
    onRemoveTag: (Tag) -> Unit,
    onClose: () -> Unit,
    onSave: () -> Unit
) {

  Column(
      modifier = Modifier.fillMaxWidth().padding(SettingsScreenPaddings.ContentHorizontalPadding),
      verticalArrangement = Arrangement.spacedBy(SettingsScreenPaddings.InternalSpacing)) {

        // ───────────── Modal Header (title + Save/Cancel) ─────────────
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
          Text(
              text = modalTitle(uiState.currentField),
              style = MaterialTheme.typography.titleMedium,
              modifier = Modifier.weight(1f).testTag(SettingsTestTags.MODAL_TITLE))
          Row(
              horizontalArrangement =
                  Arrangement.spacedBy(SettingsScreenPaddings.InternalSpacing)) {
                TextButton(
                    onClick = onClose,
                    modifier = Modifier.testTag(SettingsTestTags.MODAL_CANCEL_BUTTON)) {
                      Text("Cancel")
                    }
                TextButton(
                    onClick = onSave,
                    modifier = Modifier.testTag(SettingsTestTags.MODAL_SAVE_BUTTON)) {
                      Text("Save")
                    }
              }
        }

        Spacer(Modifier.height(SettingsScreenPaddings.InternalSpacing))
        // ───────────── Conditional Rendering by Field Type ─────────────
        when (uiState.currentField) {
          // ────── Simple Text Fields (Email, Password, Name, Description) ──────
          "email",
          "password",
          "firstName",
          "lastName",
          "description" -> {
            val tag =
                when (uiState.currentField) {
                  "email" -> SettingsTestTags.EMAIL_FIELD
                  "password" -> SettingsTestTags.PASSWORD_FIELD
                  "firstName" -> SettingsTestTags.FIRST_NAME_FIELD
                  "lastName" -> SettingsTestTags.LAST_NAME_FIELD
                  else -> SettingsTestTags.DESCRIPTION_FIELD
                }
            val maxLines = if (uiState.currentField == "description") 3 else 1

            // Main editable text input
            OutlinedTextField(
                value = uiState.tempValue,
                onValueChange = { newValue -> onUpdateTemp("tempValue", newValue) },
                modifier = Modifier.fillMaxWidth().testTag(tag),
                isError = uiState.modalError != null,
                supportingText = {
                  val message = uiState.modalError
                  if (message != null) Text(message)
                },
                shape = RoundedCornerShape(12.dp),
                maxLines = maxLines,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onSave() }))
          }

          // ────── Country Dropdown ──────
          "country" -> {
            CountryDropdown(
                value = uiState.tempValue,
                expanded = uiState.showCountryDropdown,
                onExpandedChange = { isExpanded -> onToggleCountryDropdown(isExpanded) },
                onPick = { picked ->
                  onUpdateTemp("tempValue", picked)
                  onToggleCountryDropdown(false)
                })
          }

          // ────── Date Picker (Day / Month / Year) ──────
          "date" -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(SettingsScreenPaddings.DateFieldSpacing)) {
                  OutlinedTextField(
                      value = uiState.tempDay,
                      onValueChange = { newDay -> onUpdateTemp("tempDay", newDay) },
                      label = { Text("Day") },
                      modifier = Modifier.weight(1f).testTag(SettingsTestTags.DAY_FIELD),
                      isError = uiState.tempDayError != null,
                      supportingText = {
                        val message = uiState.tempDayError
                        if (message != null) Text(message)
                      },
                      shape = RoundedCornerShape(12.dp))
                  OutlinedTextField(
                      value = uiState.tempMonth,
                      onValueChange = { newMonth -> onUpdateTemp("tempMonth", newMonth) },
                      label = { Text("Month") },
                      modifier = Modifier.weight(1f).testTag(SettingsTestTags.MONTH_FIELD),
                      isError = uiState.tempMonthError != null,
                      supportingText = {
                        val message = uiState.tempMonthError
                        if (message != null) Text(message)
                      },
                      shape = RoundedCornerShape(12.dp))
                  OutlinedTextField(
                      value = uiState.tempYear,
                      onValueChange = { newYear -> onUpdateTemp("tempYear", newYear) },
                      label = { Text("Year") },
                      modifier = Modifier.weight(1.5f).testTag(SettingsTestTags.YEAR_FIELD),
                      isError = uiState.tempYearError != null,
                      supportingText = {
                        val message = uiState.tempYearError
                        if (message != null) Text(message)
                      },
                      shape = RoundedCornerShape(12.dp))
                }
          }

          // ────── Tag Selection Groups (Interest, Sport, etc.) ──────
          else -> {
            Tag.Category.entries
                .find { it.fieldName == uiState.currentField }
                ?.let { category ->
                  TagGroup(
                      name = category.displayName,
                      tagList = Tag.getDisplayNamesForCategory(category),
                      selectedTags = uiState.tempSelectedTags.map { it.displayName },
                      displayText = false,
                      onTagSelect = { displayName ->
                        val tag = Tag.fromDisplayName(displayName)
                        if (tag != null) onAddTag(tag)
                      },
                      onTagReSelect = { displayName ->
                        val tag = Tag.fromDisplayName(displayName)
                        if (tag != null) onRemoveTag(tag)
                      },
                      modifier = Modifier.fillMaxWidth())
                }
          }
        }
      }
}
