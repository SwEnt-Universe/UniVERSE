package com.android.universe.ui.profileSettings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.testTag
import com.android.universe.model.CountryData
import com.android.universe.model.Tag
import com.android.universe.ui.common.TagGroup
import com.android.universe.ui.profile.SettingsUiState
import com.android.universe.ui.selectTag.TagColors

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
			onValueChange = {},
			readOnly = true,
			modifier = Modifier
				.fillMaxWidth()
				.menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true),
			shape = RoundedCornerShape(12.dp)
		)
		ExposedDropdownMenu(
			expanded = expanded,
			onDismissRequest = { onExpandedChange(false) },
			modifier = Modifier.heightIn(max = 240.dp)
		) {
			CountryData.allCountries.forEach { option ->
				DropdownMenuItem(
					text = { Text(if (option.length > 30) option.take(30) + "..." else option) },
					onClick = { onPick(option) }
				)
			}
		}
	}
}

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
		modifier = Modifier
			.fillMaxWidth()
			.padding(SettingsScreenPaddings.ContentHorizontalPadding),
		verticalArrangement = Arrangement.spacedBy(SettingsScreenPaddings.InternalSpacing)
	) {
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.SpaceBetween
		) {
			Text(
				text = modalTitle(uiState.currentField),
				style = MaterialTheme.typography.titleMedium,
				modifier = Modifier.weight(1f)
			)
			Row(horizontalArrangement = Arrangement.spacedBy(SettingsScreenPaddings.InternalSpacing)) {
				TextButton(
					onClick = onClose,
					modifier = Modifier.testTag(SettingsTestTags.MODAL_CANCEL_BUTTON)
				) { Text("Cancel") }
				TextButton(
					onClick = onSave,
					modifier = Modifier.testTag(SettingsTestTags.MODAL_SAVE_BUTTON)
				) { Text("Save") }
			}
		}

		Spacer(Modifier.height(SettingsScreenPaddings.InternalSpacing))

		when (uiState.currentField) {
			"email", "password", "firstName", "lastName", "description" -> {
				val tag = when (uiState.currentField) {
					"email" -> SettingsTestTags.EMAIL_FIELD
					"password" -> SettingsTestTags.PASSWORD_FIELD
					"firstName" -> SettingsTestTags.FIRST_NAME_FIELD
					"lastName" -> SettingsTestTags.LAST_NAME_FIELD
					else -> SettingsTestTags.DESCRIPTION_FIELD
				}
				val maxLines = if (uiState.currentField == "description") 3 else 1
				OutlinedTextField(
					value = uiState.tempValue,
					onValueChange = { onUpdateTemp("tempValue", it) },
					modifier = Modifier.fillMaxWidth().testTag(tag),
					isError = uiState.modalError != null,
					supportingText = { uiState.modalError?.let { msg -> Text(msg) } },
					shape = RoundedCornerShape(12.dp),
					maxLines = maxLines
				)
			}

			"country" -> {
				CountryDropdown(
					value = uiState.tempValue,
					expanded = uiState.showCountryDropdown,
					onExpandedChange = onToggleCountryDropdown,
					onPick = {
						onUpdateTemp("tempValue", it)
						onToggleCountryDropdown(false)
					}
				)
			}

			"date" -> {
				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.spacedBy(SettingsScreenPaddings.DateFieldSpacing)
				) {
					OutlinedTextField(
						value = uiState.tempDay,
						onValueChange = { onUpdateTemp("tempDay", it) },
						label = { Text("Day") },
						modifier = Modifier.weight(1f).testTag(SettingsTestTags.DAY_FIELD),
						isError = uiState.tempDayError != null,
						supportingText = { uiState.tempDayError?.let { msg -> Text(msg) } },
						shape = RoundedCornerShape(12.dp)
					)
					OutlinedTextField(
						value = uiState.tempMonth,
						onValueChange = { onUpdateTemp("tempMonth", it) },
						label = { Text("Month") },
						modifier = Modifier.weight(1f).testTag(SettingsTestTags.MONTH_FIELD),
						isError = uiState.tempMonthError != null,
						supportingText = { uiState.tempMonthError?.let { msg -> Text(msg) } },
						shape = RoundedCornerShape(12.dp)
					)
					OutlinedTextField(
						value = uiState.tempYear,
						onValueChange = { onUpdateTemp("tempYear", it) },
						label = { Text("Year") },
						modifier = Modifier.weight(1.5f).testTag(SettingsTestTags.YEAR_FIELD),
						isError = uiState.tempYearError != null,
						supportingText = { uiState.tempYearError?.let { msg -> Text(msg) } },
						shape = RoundedCornerShape(12.dp)
					)
				}
			}

			else -> {
				Tag.Category.entries.find { it.fieldName == uiState.currentField }?.let { category ->
					TagGroup(
						name = "",
						tagList = Tag.getDisplayNamesForCategory(category),
						selectedTags = uiState.tempSelectedTags.map { it.displayName },
						color = when (category) {
							Tag.Category.INTEREST -> TagColors.Interest
							Tag.Category.SPORT -> TagColors.Sport
							Tag.Category.MUSIC -> TagColors.Music
							Tag.Category.TRANSPORT -> TagColors.Transport
							Tag.Category.CANTON -> TagColors.Canton
						},
						onTagSelect = { displayName -> Tag.fromDisplayName(displayName)?.let(onAddTag) },
						onTagReSelect = { displayName -> Tag.fromDisplayName(displayName)?.let(onRemoveTag) },
						modifier = Modifier.fillMaxWidth()
					)
				}
			}
		}
	}
}
