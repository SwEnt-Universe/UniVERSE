package com.android.universe.ui.settings

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
import androidx.compose.runtime.*
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
import com.android.universe.model.Tag
import com.android.universe.model.user.UserProfile
import com.android.universe.model.user.UserRepositoryProvider
import com.android.universe.ui.profile.UserProfileViewModel
import com.android.universe.ui.profileCreation.AddProfileScreenTestTags
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDate
import java.time.format.DateTimeParseException

object SettingsScreenTestTags {
	const val EMAIL_BUTTON = "email_button"
	const val PASSWORD_BUTTON = "password_button"
	const val USERNAME_FIELD = "username_field"
	const val FIRST_NAME_BUTTON = AddProfileScreenTestTags.FIRST_NAME_FIELD + "_button"
	const val LAST_NAME_BUTTON = AddProfileScreenTestTags.LAST_NAME_FIELD + "_button"
	const val DESCRIPTION_BUTTON = AddProfileScreenTestTags.DESCRIPTION_FIELD + "_button"
	const val COUNTRY_BUTTON = AddProfileScreenTestTags.COUNTRY_FIELD + "_button"
	const val DATE_BUTTON = "date_button"
	const val TAG_CHIP_PREFIX = "tag_chip_"
	const val SAVE_BUTTON = "save_button"
	const val MODAL_SAVE_BUTTON = "modal_save_button"
	const val MODAL_CANCEL_BUTTON = "modal_cancel_button"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
	username: String,
	onSaveSuccess: () -> Unit = {},
	onCancel: () -> Unit = {},
	userProfileViewModel: UserProfileViewModel = viewModel()
) {
	val userUIState by userProfileViewModel.userState.collectAsState()
	val errorMsg = userUIState.errorMsg

	// State for General section
	var email by remember { mutableStateOf("preview@example.com") }
	var password by remember { mutableStateOf("") }
	var emailError by remember { mutableStateOf<String?>(null) }
	var passwordError by remember { mutableStateOf<String?>(null) }
	var hasTouchedEmail by remember { mutableStateOf(false) }
	var hasTouchedPassword by remember { mutableStateOf(false) }

	// State for Profile section
	var firstName by remember { mutableStateOf("") }
	var lastName by remember { mutableStateOf("") }
	var country by remember { mutableStateOf("") }
	var description by remember { mutableStateOf("") }
	var day by remember { mutableStateOf("") }
	var month by remember { mutableStateOf("") }
	var year by remember { mutableStateOf("") }
	var hasTouchedFirstName by remember { mutableStateOf(false) }
	var hasTouchedLastName by remember { mutableStateOf(false) }
	var hasTouchedDay by remember { mutableStateOf(false) }
	var hasTouchedMonth by remember { mutableStateOf(false) }
	var hasTouchedYear by remember { mutableStateOf(false) }

	// State for Interests section
	var selectedTags by remember { mutableStateOf(emptyList<Tag>()) }

	// Modal state
	var showModal by remember { mutableStateOf(false) }
	var currentField by remember { mutableStateOf("") }
	var modalInput by remember { mutableStateOf("") }
	var modalError by remember { mutableStateOf<String?>(null) }
	var showCountryDropdown by remember { mutableStateOf(false) }

	// Validation errors
	var firstNameError by remember { mutableStateOf<String?>(null) }
	var lastNameError by remember { mutableStateOf<String?>(null) }
	var descriptionError by remember { mutableStateOf<String?>(null) }
	var dayError by remember { mutableStateOf<String?>(null) }
	var monthError by remember { mutableStateOf<String?>(null) }
	var yearError by remember { mutableStateOf<String?>(null) }

	// Sample tags
	val availableTags = listOf(
		Tag("Music"), Tag("Sports"), Tag("Travel"), Tag("Food"), Tag("Tech")
	)

	val context = LocalContext.current
	LaunchedEffect(errorMsg) {
		if (errorMsg != null) {
			Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
			userProfileViewModel.clearErrorMsg()
		}
	}

	// Initialize state after loading user profile
	LaunchedEffect(username, userUIState.userProfile) {
		userProfileViewModel.loadUser(username)
		email = FirebaseAuth.getInstance().currentUser?.email ?: "preview@example.com"
		firstName = userUIState.userProfile.firstName
		lastName = userUIState.userProfile.lastName
		country = userUIState.userProfile.country
		description = userUIState.userProfile.description ?: ""
		day = userUIState.userProfile.dateOfBirth.dayOfMonth.toString()
		month = userUIState.userProfile.dateOfBirth.monthValue.toString()
		year = userUIState.userProfile.dateOfBirth.year.toString()
		selectedTags = userUIState.userProfile.tags
	}

	// ModalBottomSheet for editing fields
	if (showModal) {
		ModalBottomSheet(
			onDismissRequest = { showModal = false },
			sheetState = rememberModalBottomSheetState()
		) {
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.padding(16.dp),
				verticalArrangement = Arrangement.spacedBy(8.dp)
			) {
				Text(
					text = when (currentField) {
						"email" -> "Edit Email"
						"password" -> "Edit Password"
						"firstName" -> "Edit First Name"
						"lastName" -> "Edit Last Name"
						"description" -> "Edit Description"
						"country" -> "Edit Country"
						"date" -> "Edit Date of Birth"
						else -> ""
					},
					style = MaterialTheme.typography.titleMedium
				)
				when (currentField) {
					"email", "password", "firstName", "lastName", "description" -> {
						OutlinedTextField(
							value = modalInput,
							onValueChange = { modalInput = it },
							label = {
								Text(
									when (currentField) {
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
										when (currentField) {
											"email" -> hasTouchedEmail = true
											"password" -> hasTouchedPassword = true
											"firstName" -> hasTouchedFirstName = true
											"lastName" -> hasTouchedLastName = true
										}
									}
								},
							isError = modalError != null,
							supportingText = { if (modalError != null) Text(modalError!!) },
							shape = RoundedCornerShape(12.dp),
							maxLines = if (currentField == "description") 3 else 1
						)
					}
					"country" -> {
						ExposedDropdownMenuBox(
							expanded = showCountryDropdown,
							onExpandedChange = { showCountryDropdown = !showCountryDropdown }
						) {
							OutlinedTextField(
								value = modalInput,
								onValueChange = {},
								readOnly = true,
								label = { Text("Country") },
								modifier = Modifier
									.fillMaxWidth()
									.menuAnchor(),
								shape = RoundedCornerShape(12.dp)
							)
							ExposedDropdownMenu(
								expanded = showCountryDropdown,
								onDismissRequest = { showCountryDropdown = false },
								modifier = Modifier.heightIn(max = 240.dp)
							) {
								CountryData.allCountries.forEach { countryOption ->
									DropdownMenuItem(
										text = { Text(countryOption.take(30) + if (countryOption.length > 30) "..." else "") },
										onClick = {
											modalInput = countryOption
											showCountryDropdown = false
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
								value = day,
								onValueChange = { day = it },
								label = { Text("Day") },
								modifier = Modifier
									.weight(1f)
									.onFocusChanged { hasTouchedDay = true },
								isError = hasTouchedDay && dayError != null,
								supportingText = { if (hasTouchedDay && dayError != null) Text(dayError!!) },
								shape = RoundedCornerShape(12.dp)
							)
							OutlinedTextField(
								value = month,
								onValueChange = { month = it },
								label = { Text("Month") },
								modifier = Modifier
									.weight(1f)
									.onFocusChanged { hasTouchedMonth = true },
								isError = hasTouchedMonth && monthError != null,
								supportingText = { if (hasTouchedMonth && monthError != null) Text(monthError!!) },
								shape = RoundedCornerShape(12.dp)
							)
							OutlinedTextField(
								value = year,
								onValueChange = { year = it },
								label = { Text("Year") },
								modifier = Modifier
									.weight(1.5f)
									.onFocusChanged { hasTouchedYear = true },
								isError = hasTouchedYear && yearError != null,
								supportingText = { if (hasTouchedYear && yearError != null) Text(yearError!!) },
								shape = RoundedCornerShape(12.dp)
							)
						}
					}
				}
				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.End
				) {
					TextButton(
						onClick = { showModal = false },
						modifier = Modifier.testTag(SettingsScreenTestTags.MODAL_CANCEL_BUTTON)
					) {
						Text("Cancel")
					}
					TextButton(
						onClick = {
							modalError = when (currentField) {
								"email" -> when {
									modalInput.isEmpty() -> "Email cannot be empty"
									!modalInput.contains("@") -> "Invalid email format"
									else -> null
								}
								"password" -> when {
									modalInput.isNotEmpty() && modalInput.length < 6 -> "Password must be at least 6 characters"
									else -> null
								}
								"firstName" -> if (modalInput.isEmpty()) "First name cannot be empty" else null
								"lastName" -> if (modalInput.isEmpty()) "Last name cannot be empty" else null
								"description" -> if (modalInput.length > 200) "Description too long" else null
								"country" -> if (modalInput.isEmpty()) "Country cannot be empty" else null
								"date" -> {
									try {
										LocalDate.of(year.toInt(), month.toInt(), day.toInt())
										null
									} catch (e: Exception) {
										"Invalid date"
									}
								}
								else -> null
							}
							if (modalError == null) {
								when (currentField) {
									"email" -> email = modalInput
									"password" -> password = modalInput
									"firstName" -> firstName = modalInput
									"lastName" -> lastName = modalInput
									"description" -> description = modalInput
									"country" -> country = modalInput
								}
								showModal = false
							}
						},
						modifier = Modifier.testTag(SettingsScreenTestTags.MODAL_SAVE_BUTTON)
					) {
						Text("Save")
					}
				}
			}
		}
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
					IconButton(onClick = {
						// Validate General section
						emailError = when {
							email.isEmpty() -> "Email cannot be empty"
							!email.contains("@") -> "Invalid email format"
							else -> null
						}
						passwordError = when {
							password.isNotEmpty() && password.length < 6 -> "Password must be at least 6 characters"
							else -> null
						}

						// Validate Profile section
						firstNameError = if (firstName.isEmpty()) "First name cannot be empty" else null
						lastNameError = if (lastName.isEmpty()) "Last name cannot be empty" else null
						descriptionError = if (description.length > 200) "Description too long" else null
						dayError = when {
							day.isEmpty() -> "Day cannot be empty"
							day.toIntOrNull()?.let { it !in 1..31 } == true -> "Invalid day"
							else -> null
						}
						monthError = when {
							month.isEmpty() -> "Month cannot be empty"
							month.toIntOrNull()?.let { it !in 1..12 } == true -> "Invalid month"
							else -> null
						}
						yearError = when {
							year.isEmpty() -> "Year cannot be empty"
							year.toIntOrNull()?.let { it !in 1900..LocalDate.now().year } == true -> "Invalid year"
							else -> null
						}

						// Validate date
						val dateOfBirth = try {
							LocalDate.of(year.toInt(), month.toInt(), day.toInt())
						} catch (e: Exception) {
							yearError = "Invalid date"
							return@IconButton
						}

						if (emailError == null && passwordError == null && firstNameError == null &&
							lastNameError == null && descriptionError == null && dayError == null &&
							monthError == null && yearError == null
						) {
							val updatedProfile = userUIState.userProfile.copy(
								firstName = firstName,
								lastName = lastName,
								country = country,
								dateOfBirth = dateOfBirth,
								description = description,
								tags = selectedTags
							)
							userProfileViewModel.saveUser(username, updatedProfile)
							if (email != FirebaseAuth.getInstance().currentUser?.email) {
								FirebaseAuth.getInstance().currentUser?.updateEmail(email)
									?.addOnFailureListener { e ->
										Toast.makeText(context, "Failed to update email: ${e.message}", Toast.LENGTH_SHORT).show()
									}
							}
							if (password.isNotEmpty()) {
								FirebaseAuth.getInstance().currentUser?.updatePassword(password)
									?.addOnFailureListener { e ->
										Toast.makeText(context, "Failed to update password: ${e.message}", Toast.LENGTH_SHORT).show()
									}
							}
							onSaveSuccess()
						}
					}) {
						Icon(Icons.Default.Save, contentDescription = "Save", modifier = Modifier.testTag(SettingsScreenTestTags.SAVE_BUTTON))
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
			// General Section
			item {
				Text("General", style = MaterialTheme.typography.titleLarge)
				Row(
					modifier = Modifier
						.fillMaxWidth()
						.clickable {
							currentField = "email"
							modalInput = email
							showModal = true
						}
						.testTag(SettingsScreenTestTags.EMAIL_BUTTON)
						.padding(vertical = 8.dp),
					verticalAlignment = Alignment.CenterVertically
				) {
					Text("Email address", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
					Text(email, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
					Spacer(modifier = Modifier.width(8.dp))
					Icon(Icons.Default.Edit, contentDescription = "Edit Email")
				}
				if (hasTouchedEmail && emailError != null) {
					Text(emailError!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(start = 16.dp))
				}
				Row(
					modifier = Modifier
						.fillMaxWidth()
						.clickable {
							currentField = "password"
							modalInput = password
							showModal = true
						}
						.testTag(SettingsScreenTestTags.PASSWORD_BUTTON)
						.padding(vertical = 8.dp),
					verticalAlignment = Alignment.CenterVertically
				) {
					Text("Password", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
					Text(
						if (password.isEmpty()) "Unchanged" else "********",
						style = MaterialTheme.typography.bodyMedium,
						maxLines = 1,
						overflow = TextOverflow.Ellipsis
					)
					Spacer(modifier = Modifier.width(8.dp))
					Icon(Icons.Default.Edit, contentDescription = "Edit Password")
				}
				if (hasTouchedPassword && passwordError != null) {
					Text(passwordError!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(start = 16.dp))
				}
			}


			// Profile Section
			item {
				HorizontalDivider(
					color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
					thickness = 0.5.dp,
					modifier = Modifier.padding(vertical = 20.dp)
				)
				Text("Profile", style = MaterialTheme.typography.titleLarge)
				Spacer(modifier = Modifier.height(8.dp))
				Row(
					modifier = Modifier
						.fillMaxWidth()
						.clickable {
							currentField = "firstName"
							modalInput = firstName
							showModal = true
						}
						.testTag(SettingsScreenTestTags.FIRST_NAME_BUTTON)
						.padding(vertical = 8.dp),
					verticalAlignment = Alignment.CenterVertically
				) {
					Text("First Name", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
					Text(firstName, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
					Spacer(modifier = Modifier.width(8.dp))
					Icon(Icons.Default.Edit, contentDescription = "Edit First Name")
				}
				if (hasTouchedFirstName && firstNameError != null) {
					Text(firstNameError!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(start = 16.dp))
				}
				Row(
					modifier = Modifier
						.fillMaxWidth()
						.clickable {
							currentField = "lastName"
							modalInput = lastName
							showModal = true
						}
						.testTag(SettingsScreenTestTags.LAST_NAME_BUTTON)
						.padding(vertical = 8.dp),
					verticalAlignment = Alignment.CenterVertically
				) {
					Text("Last Name", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
					Text(lastName, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
					Spacer(modifier = Modifier.width(8.dp))
					Icon(Icons.Default.Edit, contentDescription = "Edit Last Name")
				}
				if (hasTouchedLastName && lastNameError != null) {
					Text(lastNameError!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(start = 16.dp))
				}
				Row(
					modifier = Modifier
						.fillMaxWidth()
						.clickable {
							currentField = "description"
							modalInput = description
							showModal = true
						}
						.testTag(SettingsScreenTestTags.DESCRIPTION_BUTTON)
						.padding(vertical = 8.dp),
					verticalAlignment = Alignment.CenterVertically
				) {
					Text("Description", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
					Text(
						description.take(30) + if (description.length > 30) "..." else "",
						style = MaterialTheme.typography.bodyMedium,
						maxLines = 1,
						overflow = TextOverflow.Ellipsis
					)
					Spacer(modifier = Modifier.width(8.dp))
					Icon(Icons.Default.Edit, contentDescription = "Edit Description")
				}
				if (descriptionError != null) {
					Text(descriptionError!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(start = 16.dp))
				}
				Row(
					modifier = Modifier
						.fillMaxWidth()
						.clickable {
							currentField = "country"
							modalInput = country
							showModal = true
						}
						.testTag(SettingsScreenTestTags.COUNTRY_BUTTON)
						.padding(vertical = 8.dp),
					verticalAlignment = Alignment.CenterVertically
				) {
					Text("Country", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
					Text(country, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
					Spacer(modifier = Modifier.width(8.dp))
					Icon(Icons.Default.Edit, contentDescription = "Edit Country")
				}
				Row(
					modifier = Modifier
						.fillMaxWidth()
						.clickable {
							currentField = "date"
							showModal = true
						}
						.testTag(SettingsScreenTestTags.DATE_BUTTON)
						.padding(vertical = 8.dp),
					verticalAlignment = Alignment.CenterVertically
				) {
					Text("Date of Birth", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
					Text(
						"$year-$month-$day",
						style = MaterialTheme.typography.bodyMedium,
						maxLines = 1,
						overflow = TextOverflow.Ellipsis
					)
					Spacer(modifier = Modifier.width(8.dp))
					Icon(Icons.Default.Edit, contentDescription = "Edit Date of Birth")
				}
				if (hasTouchedDay && dayError != null) {
					Text(dayError!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(start = 16.dp))
				}
				if (hasTouchedMonth && monthError != null) {
					Text(monthError!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(start = 16.dp))
				}
				if (hasTouchedYear && yearError != null) {
					Text(yearError!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(start = 16.dp))
				}
			}

			// Interests Section
			item {
				HorizontalDivider(
					color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
					thickness = 0.5.dp,
					modifier = Modifier.padding(vertical = 20.dp)
				)
				Text("Interests", style = MaterialTheme.typography.titleLarge)
				Spacer(modifier = Modifier.height(8.dp))
				val rows = availableTags.chunked(3)
				rows.forEachIndexed { index, row ->
					Row(
						modifier = Modifier.fillMaxWidth(),
						horizontalArrangement = Arrangement.spacedBy(8.dp)
					) {
						row.forEach { tag ->
							FilterChip(
								selected = tag in selectedTags,
								onClick = {
									selectedTags = if (tag in selectedTags) {
										selectedTags - tag
									} else {
										selectedTags + tag
									}
								},
								label = { Text(tag.name) },
								modifier = Modifier
									.weight(1f)
									.testTag(SettingsScreenTestTags.TAG_CHIP_PREFIX + tag.name)
							)
						}
					}
					Spacer(modifier = Modifier.height(8.dp))
				}
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
			userProfileViewModel = UserProfileViewModel(UserRepositoryProvider.repository)
		)
	}
}