package com.android.universe.ui.profileSettings

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.universe.model.CountryData
import com.android.universe.model.tagsCanton
import com.android.universe.model.tagsInterest
import com.android.universe.model.tagsMusic
import com.android.universe.model.tagsSport
import com.android.universe.model.tagsTransport
import com.android.universe.ui.common.TagGroup
import com.android.universe.ui.profile.SettingsUiState
import com.android.universe.ui.profile.SettingsViewModel
import com.android.universe.ui.selectTag.TagColors


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
	username: String,
	onSaveSuccess: () -> Unit = {},
	onCancel: () -> Unit = {},
	viewModel: SettingsViewModel = viewModel()
) {
	val uiState by viewModel.uiState.collectAsState()
	val context = LocalContext.current

	LaunchedEffect(uiState.errorMsg) {
		if (uiState.errorMsg != null) {
			Toast.makeText(context, uiState.errorMsg, Toast.LENGTH_SHORT).show()
			viewModel.clearErrorMsg()
		}
	}

	LaunchedEffect(username) {
		viewModel.loadUser(username)
	}

	Scaffold(
		topBar = {
			TopAppBar(
				title = { Text("Settings") },
				navigationIcon = {
					IconButton(onClick = onCancel) {
						Icon(Icons.Default.ArrowBack, contentDescription = "Cancel")
					}
				},
				actions = {
					IconButton(onClick = { viewModel.saveProfile(username, onSaveSuccess) }) {
						Icon(Icons.Default.Save, contentDescription = "Save", modifier = Modifier.testTag(SettingsTestTags.SAVE_BUTTON))
					}
				}
			)
		}
	) { padding ->
		LazyColumn(
			modifier = Modifier
				.fillMaxSize()
				.padding(padding)
				.padding(horizontal = 16.dp),
			verticalArrangement = Arrangement.spacedBy(16.dp)
		) {
			item { GeneralSection(uiState, viewModel) }
			item { ProfileSection(uiState, viewModel) }
			item { InterestsSection(uiState, viewModel) }
		}
	}

	if (uiState.showModal) {
		ModalBottomSheet(
			onDismissRequest = { viewModel.closeModal() },
			sheetState = rememberModalBottomSheetState()
		) {
			ModalContent(uiState, viewModel)
		}
	}
}

@Composable
private fun GeneralSection(uiState: SettingsUiState, viewModel: SettingsViewModel) {
	Column {
		Text("General", style = MaterialTheme.typography.titleLarge)
		EditableField(
			label = "Email address",
			value = uiState.email,
			error = uiState.emailError,
			testTag = SettingsTestTags.EMAIL_BUTTON,
			onClick = { viewModel.openModal("email") }
		)
		EditableField(
			label = "Password",
			value = if (uiState.password.isEmpty()) "Unchanged" else "********",
			error = uiState.passwordError,
			testTag = SettingsTestTags.PASSWORD_BUTTON,
			onClick = { viewModel.openModal("password") }
		)
	}
}

@Composable
private fun ProfileSection(uiState: SettingsUiState, viewModel: SettingsViewModel) {
	Column {
		HorizontalDivider(
			color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
			thickness = 0.5.dp,
			modifier = Modifier.padding(vertical = 20.dp)
		)
		Text("Profile", style = MaterialTheme.typography.titleLarge)
		Spacer(modifier = Modifier.height(8.dp))
		EditableField(
			label = "First Name",
			value = uiState.firstName,
			error = uiState.firstNameError,
			testTag = SettingsTestTags.FIRST_NAME_BUTTON,
			onClick = { viewModel.openModal("firstName") }
		)
		EditableField(
			label = "Last Name",
			value = uiState.lastName,
			error = uiState.lastNameError,
			testTag = SettingsTestTags.LAST_NAME_BUTTON,
			onClick = { viewModel.openModal("lastName") }
		)
		EditableField(
			label = "Description",
			value = uiState.description.take(30) + if (uiState.description.length > 30) "..." else "",
			error = uiState.descriptionError,
			testTag = SettingsTestTags.DESCRIPTION_BUTTON,
			onClick = { viewModel.openModal("description") }
		)
		EditableField(
			label = "Country",
			value = uiState.country,
			testTag = SettingsTestTags.COUNTRY_BUTTON,
			onClick = { viewModel.openModal("country") }
		)
		EditableField(
			label = "Date of Birth",
			value = "${uiState.year}-${uiState.month}-${uiState.day}",
			testTag = SettingsTestTags.DATE_BUTTON,
			onClick = { viewModel.openModal("date") }
		)
		if (uiState.dayError != null) {
			Text(uiState.dayError, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(start = 16.dp))
		}
		if (uiState.monthError != null) {
			Text(uiState.monthError, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(start = 16.dp))
		}
		if (uiState.yearError != null) {
			Text(uiState.yearError, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(start = 16.dp))
		}
	}
}

@Composable
private fun InterestsSection(uiState: SettingsUiState, viewModel: SettingsViewModel) {
	Column {
		HorizontalDivider(
			color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
			thickness = 0.5.dp,
			modifier = Modifier.padding(vertical = 20.dp)
		)
		Text("Interests", style = MaterialTheme.typography.titleLarge)
		Spacer(modifier = Modifier.height(8.dp))
		EditableField(
			label = "Hobbies",
			value = uiState.selectedTags.filter { it.name in tagsInterest }.joinToString(", ") { it.name }
				.take(30) + if (uiState.selectedTags.filter { it.name in tagsInterest }.joinToString().length > 30) "..." else "",
			testTag = SettingsTestTags.INTEREST_TAGS_BUTTON,
			onClick = { viewModel.openModal("interest_tags") }
		)
		HorizontalDivider(
			color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
			thickness = 1.dp,
			modifier = Modifier.padding(vertical = 8.dp)
		)
		EditableField(
			label = "Sport",
			value = uiState.selectedTags.filter { it.name in tagsSport }.joinToString(", ") { it.name }
				.take(30) + if (uiState.selectedTags.filter { it.name in tagsSport }.joinToString().length > 30) "..." else "",
			testTag = SettingsTestTags.SPORT_TAGS_BUTTON,
			onClick = { viewModel.openModal("sport_tags") }
		)
		HorizontalDivider(
			color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
			thickness = 1.dp,
			modifier = Modifier.padding(vertical = 8.dp)
		)
		EditableField(
			label = "Music",
			value = uiState.selectedTags.filter { it.name in tagsMusic }.joinToString(", ") { it.name }
				.take(30) + if (uiState.selectedTags.filter { it.name in tagsMusic }.joinToString().length > 30) "..." else "",
			testTag = SettingsTestTags.MUSIC_TAGS_BUTTON,
			onClick = { viewModel.openModal("music_tags") }
		)
		HorizontalDivider(
			color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
			thickness = 1.dp,
			modifier = Modifier.padding(vertical = 8.dp)
		)
		EditableField(
			label = "Transport",
			value = uiState.selectedTags.filter { it.name in tagsTransport }.joinToString(", ") { it.name }
				.take(30) + if (uiState.selectedTags.filter { it.name in tagsTransport }.joinToString().length > 30) "..." else "",
			testTag = SettingsTestTags.TRANSPORT_TAGS_BUTTON,
			onClick = { viewModel.openModal("transport_tags") }
		)
		HorizontalDivider(
			color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
			thickness = 1.dp,
			modifier = Modifier.padding(vertical = 8.dp)
		)
		EditableField(
			label = "Canton",
			value = uiState.selectedTags.filter { it.name in tagsCanton }.joinToString(", ") { it.name }
				.take(30) + if (uiState.selectedTags.filter { it.name in tagsCanton }.joinToString().length > 30) "..." else "",
			testTag = SettingsTestTags.CANTON_TAGS_BUTTON,
			onClick = { viewModel.openModal("canton_tags") }
		)
	}
}

@Composable
private fun EditableField(
	label: String,
	value: String,
	error: String? = null,
	testTag: String,
	onClick: () -> Unit
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clickable { onClick() }
			.testTag(testTag)
			.padding(vertical = 8.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
		Text(value, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
		Spacer(modifier = Modifier.width(8.dp))
		Icon(Icons.Default.Edit, contentDescription = "Edit $label")
	}
	if (error != null) {
		Text(error, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(start = 16.dp))
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModalContent(uiState: SettingsUiState, viewModel: SettingsViewModel) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.padding(16.dp),
		verticalArrangement = Arrangement.spacedBy(8.dp)
	) {
		Text(
			text = when (uiState.currentField) {
				"email" -> "Edit Email"
				"password" -> "Edit Password"
				"firstName" -> "Edit First Name"
				"lastName" -> "Edit Last Name"
				"description" -> "Edit Description"
				"country" -> "Edit Country"
				"date" -> "Edit Date of Birth"
				"interest_tags" -> "Edit Interest Tags"
				"sport_tags" -> "Edit Sport Tags"
				"music_tags" -> "Edit Music Tags"
				"transport_tags" -> "Edit Transport Tags"
				"canton_tags" -> "Edit Canton Tags"
				else -> ""
			},
			style = MaterialTheme.typography.titleMedium
		)
		when (uiState.currentField) {
			"email", "password", "firstName", "lastName", "description" -> {
				OutlinedTextField(
					value = when (uiState.currentField) {
						"email" -> uiState.email
						"password" -> uiState.password
						"firstName" -> uiState.firstName
						"lastName" -> uiState.lastName
						"description" -> uiState.description
						else -> ""
					},
					onValueChange = { viewModel.updateField(uiState.currentField, it) },
					label = {
						Text(
							when (uiState.currentField) {
								"email" -> "Email"
								"password" -> "New Password"
								"firstName" -> "First Name"
								"lastName" -> "Last Name"
								"description" -> "Description"
								else -> ""
							}
						)
					},
					modifier = Modifier
						.fillMaxWidth()
						.onFocusChanged {
							if (it.isFocused) {
								when (uiState.currentField) {
									"email" -> viewModel.updateField("email", uiState.email)
									"password" -> viewModel.updateField("password", uiState.password)
									"firstName" -> viewModel.updateField("firstName", uiState.firstName)
									"lastName" -> viewModel.updateField("lastName", uiState.lastName)
								}
							}
						},
					isError = uiState.modalError != null,
					supportingText = { if (uiState.modalError != null) Text(uiState.modalError) },
					shape = RoundedCornerShape(12.dp),
					maxLines = if (uiState.currentField == "description") 3 else 1
				)
			}
			"country" -> {
				ExposedDropdownMenuBox(
					expanded = uiState.showCountryDropdown,
					onExpandedChange = { viewModel.toggleCountryDropdown(!uiState.showCountryDropdown) }
				) {
					OutlinedTextField(
						value = uiState.country,
						onValueChange = {},
						readOnly = true,
						label = { Text("Country") },
						modifier = Modifier
							.fillMaxWidth()
							.menuAnchor(),
						shape = RoundedCornerShape(12.dp)
					)
					ExposedDropdownMenu(
						expanded = uiState.showCountryDropdown,
						onDismissRequest = { viewModel.toggleCountryDropdown(false) },
						modifier = Modifier.heightIn(max = 240.dp)
					) {
						CountryData.allCountries.forEach { countryOption ->
							DropdownMenuItem(
								text = { Text(countryOption.take(30) + if (countryOption.length > 30) "..." else "") },
								onClick = {
									viewModel.updateField("country", countryOption)
									viewModel.toggleCountryDropdown(false)
								}
							)
						}
					}
				}
			}
			"date" -> {
				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.spacedBy(8.dp)
				) {
					OutlinedTextField(
						value = uiState.day,
						onValueChange = { viewModel.updateField("day", it) },
						label = { Text("Day") },
						modifier = Modifier
							.weight(1f)
							.onFocusChanged { if (it.isFocused) viewModel.updateField("day", uiState.day) },
						isError = uiState.dayError != null,
						supportingText = { if (uiState.dayError != null) Text(uiState.dayError) },
						shape = RoundedCornerShape(12.dp)
					)
					OutlinedTextField(
						value = uiState.month,
						onValueChange = { viewModel.updateField("month", it) },
						label = { Text("Month") },
						modifier = Modifier
							.weight(1f)
							.onFocusChanged { if (it.isFocused) viewModel.updateField("month", uiState.month) },
						isError = uiState.monthError != null,
						supportingText = { if (uiState.monthError != null) Text(uiState.monthError) },
						shape = RoundedCornerShape(12.dp)
					)
					OutlinedTextField(
						value = uiState.year,
						onValueChange = { viewModel.updateField("year", it) },
						label = { Text("Year") },
						modifier = Modifier
							.weight(1.5f)
							.onFocusChanged { if (it.isFocused) viewModel.updateField("year", uiState.year) },
						isError = uiState.yearError != null,
						supportingText = { if (uiState.yearError != null) Text(uiState.yearError) },
						shape = RoundedCornerShape(12.dp)
					)
				}
			}
			"interest_tags" -> {
				TagGroup(
					name = "",
					tagList = tagsInterest,
					selectedTags = uiState.tempSelectedTags,
					color = TagColors.Interest,
					onTagSelect = { viewModel.addTag(it) },
					onTagReSelect = { viewModel.removeTag(it) },
					modifier = Modifier.fillMaxWidth()
				)
			}
			"sport_tags" -> {
				TagGroup(
					name = "",
					tagList = tagsSport,
					selectedTags = uiState.tempSelectedTags,
					color = TagColors.Sport,
					onTagSelect = { viewModel.addTag(it) },
					onTagReSelect = { viewModel.removeTag(it) },
					modifier = Modifier.fillMaxWidth()
				)
			}
			"music_tags" -> {
				TagGroup(
					name = "",
					tagList = tagsMusic,
					selectedTags = uiState.tempSelectedTags,
					color = TagColors.Music,
					onTagSelect = { viewModel.addTag(it) },
					onTagReSelect = { viewModel.removeTag(it) },
					modifier = Modifier.fillMaxWidth()
				)
			}
			"transport_tags" -> {
				TagGroup(
					name = "",
					tagList = tagsTransport,
					selectedTags = uiState.tempSelectedTags,
					color = TagColors.Transport,
					onTagSelect = { viewModel.addTag(it) },
					onTagReSelect = { viewModel.removeTag(it) },
					modifier = Modifier.fillMaxWidth()
				)
			}
			"canton_tags" -> {
				TagGroup(
					name = "",
					tagList = tagsCanton,
					selectedTags = uiState.tempSelectedTags,
					color = TagColors.Canton,
					onTagSelect = { viewModel.addTag(it) },
					onTagReSelect = { viewModel.removeTag(it) },
					modifier = Modifier.fillMaxWidth()
				)
			}
		}
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.End
		) {
			TextButton(
				onClick = { viewModel.closeModal() },
				modifier = Modifier.testTag(SettingsTestTags.MODAL_CANCEL_BUTTON)
			) {
				Text("Cancel")
			}
			TextButton(
				onClick = { viewModel.saveModal() },
				modifier = Modifier.testTag(SettingsTestTags.MODAL_SAVE_BUTTON)
			) {
				Text("Save")
			}
		}
	}
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
	MaterialTheme {
		SettingsScreen(
			username = "emma",
			viewModel = SettingsViewModel()
		)
	}
}

