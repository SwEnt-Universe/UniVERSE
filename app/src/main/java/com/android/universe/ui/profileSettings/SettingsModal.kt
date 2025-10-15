package com.android.universe.ui.profileSettings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.android.universe.model.CountryData
import com.android.universe.model.Tag
import com.android.universe.ui.common.TagGroup
import com.android.universe.ui.profile.SettingsUiState
import com.android.universe.ui.selectTag.TagColors

/**
 * Dropdown component used in the modal for selecting a country.
 *
 * @param value Currently selected country.
 * @param expanded Whether the dropdown menu is open.
 * @param onExpandedChange Callback when dropdown is toggled.
 * @param onPick Callback when a country is selected.
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
 * Modal bottom-sheet content for editing profile fields.
 *
 * Dynamically adapts its layout based on `uiState.currentField`
 * to display either text fields, dropdowns, date pickers, or tag groups.
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
  // Local mirrors keep modal responsive without direct upstream updates
  var localText by remember(uiState.currentField) { mutableStateOf(uiState.tempValue) }
  var localDay by remember(uiState.currentField) { mutableStateOf(uiState.tempDay) }
  var localMonth by remember(uiState.currentField) { mutableStateOf(uiState.tempMonth) }
  var localYear by remember(uiState.currentField) { mutableStateOf(uiState.tempYear) }

  Column(
    modifier = Modifier.fillMaxWidth().padding(SettingsScreenPaddings.ContentHorizontalPadding),
    verticalArrangement = Arrangement.spacedBy(SettingsScreenPaddings.InternalSpacing)) {
    // Modal header with title + action buttons
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
      Text(
        text = modalTitle(uiState.currentField),
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.weight(1f).testTag(SettingsTestTags.MODAL_TITLE))
      Row(horizontalArrangement = Arrangement.spacedBy(SettingsScreenPaddings.InternalSpacing)) {
        TextButton(onClick = onClose, modifier = Modifier.testTag(SettingsTestTags.MODAL_CANCEL_BUTTON)) { Text("Cancel") }
        TextButton(onClick = onSave, modifier = Modifier.testTag(SettingsTestTags.MODAL_SAVE_BUTTON)) { Text("Save") }
      }
    }

    Spacer(Modifier.height(SettingsScreenPaddings.InternalSpacing))

    // Conditional rendering depending on field type
    when (uiState.currentField) {
      "email", "password", "firstName", "lastName", "description" -> { /* text field block */ }
      "country" -> { /* country dropdown block */ }
      "date" -> { /* date input triple */ }
      else -> { /* tag group */ }
    }
  }
}