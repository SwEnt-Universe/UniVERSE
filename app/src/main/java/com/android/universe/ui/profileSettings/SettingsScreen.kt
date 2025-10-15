package com.android.universe.ui.profileSettings

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.universe.model.Tag
import com.android.universe.ui.profile.SettingsUiState
import com.android.universe.ui.profile.SettingsViewModel

/* =========================================================
 * Padding/style constants
 * ========================================================= */
object SettingsScreenPaddings {
  val InternalSpacing = 4.dp
  val DividerPadding = 20.dp
  val ContentHorizontalPadding = 20.dp
  val ErrorIndent = 8.dp
  val FieldIconSpacing = 10.dp
  val DateFieldSpacing = 8.dp
}

object SettingsScreenStyles {
  @Composable fun sectionTitleStyle() = MaterialTheme.typography.titleLarge
}

/* =========================================================
 * Helper methods
 * ========================================================= */
internal fun modalTitle(field: String): String =
  when (field) {
    "email" -> "Edit Email"
    "password" -> "Edit Password"
    "firstName" -> "Edit First Name"
    "lastName" -> "Edit Last Name"
    "description" -> "Edit Description"
    "country" -> "Edit Country"
    "date" -> "Edit Date of Birth"
    Tag.Category.INTEREST.fieldName -> Tag.Category.INTEREST.displayName
    Tag.Category.SPORT.fieldName -> Tag.Category.SPORT.displayName
    Tag.Category.MUSIC.fieldName -> Tag.Category.MUSIC.displayName
    Tag.Category.TRANSPORT.fieldName -> Tag.Category.TRANSPORT.displayName
    Tag.Category.CANTON.fieldName -> Tag.Category.CANTON.displayName
    else -> field.ifBlank { "Edit" }
  }

@Composable
private fun ChipsLine(label: String, names: List<String>, testTag: String, onOpen: () -> Unit) {
  val joined = names.joinToString(", ")
  EditableField(
    label = label,
    value = if (joined.length > 30) joined.take(30) + "..." else joined,
    testTag = testTag,
    onClick = onOpen
  )
}

/* =========================================================
 * Stateful wrapper (production)
 * ========================================================= */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
  username: String,
  onBack: () -> Unit = {},
  viewModel: SettingsViewModel = viewModel()
) {
  val uiState by viewModel.uiState.collectAsState()
  val context = LocalContext.current

  LaunchedEffect(uiState.errorMsg) {
    uiState.errorMsg?.let {
      Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
      viewModel.clearErrorMsg()
    }
  }

  LaunchedEffect(username) { viewModel.loadUser(username) }

  SettingsScreenContent(
    uiState = uiState,
    onBack = onBack,
    onOpenField = viewModel::openModal,
    onCloseModal = viewModel::closeModal,
    onUpdateTemp = viewModel::updateTemp,
    onToggleCountryDropdown = viewModel::toggleCountryDropdown,
    onAddTag = viewModel::addTag,
    onRemoveTag = viewModel::removeTag,
    onSaveModal = { viewModel.saveModal(username) }
  )
}

/* =========================================================
 * Stateless, preview-friendly UI
 * ========================================================= */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenContent(
  uiState: SettingsUiState,
  onBack: () -> Unit = {},
  onOpenField: (String) -> Unit = {},
  onCloseModal: () -> Unit = {},
  onUpdateTemp: (String, String) -> Unit = { _, _ -> },
  onToggleCountryDropdown: (Boolean) -> Unit = {},
  onAddTag: (Tag) -> Unit = {},
  onRemoveTag: (Tag) -> Unit = {},
  onSaveModal: () -> Unit = {}
) {
  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Settings") },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(
              Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back"
            )
          }
        }
      )
    }
  ) { padding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(horizontal = SettingsScreenPaddings.ContentHorizontalPadding)
    ) {
      item { GeneralSection(uiState = uiState, open = onOpenField) }
      item { ProfileSection(uiState = uiState, open = onOpenField) }
      item { InterestsSection(uiState = uiState, open = onOpenField) }
    }
  }

  if (uiState.showModal) {
    ModalBottomSheet(
      onDismissRequest = onCloseModal,
      sheetState = rememberModalBottomSheetState()
    ) {
      ModalContent(
        uiState = uiState,
        onUpdateTemp = onUpdateTemp,
        onToggleCountryDropdown = onToggleCountryDropdown,
        onAddTag = onAddTag,
        onRemoveTag = onRemoveTag,
        onClose = onCloseModal,
        onSave = onSaveModal
      )
    }
  }
}

/* =========================================================
 * Sections (stateless)
 * ========================================================= */
@Composable
private fun GeneralSection(uiState: SettingsUiState, open: (String) -> Unit) {
  Column(verticalArrangement = Arrangement.spacedBy(SettingsScreenPaddings.InternalSpacing)) {
    Text("General", style = SettingsScreenStyles.sectionTitleStyle())
    EditableField(
      label = "Email address",
      value = uiState.email,
      error = uiState.emailError,
      testTag = SettingsTestTags.EMAIL_BUTTON,
      onClick = { open("email") }
    )
    EditableField(
      label = "Password",
      value = if (uiState.password.isEmpty()) "Unchanged" else "********",
      error = uiState.passwordError,
      testTag = SettingsTestTags.PASSWORD_BUTTON,
      onClick = { open("password") }
    )
  }
}

@Composable
private fun ProfileSection(uiState: SettingsUiState, open: (String) -> Unit) {
  Column(verticalArrangement = Arrangement.spacedBy(SettingsScreenPaddings.InternalSpacing)) {
    HorizontalDivider(
      color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
      thickness = 0.5.dp,
      modifier = Modifier.padding(vertical = SettingsScreenPaddings.DividerPadding)
    )
    Text("Profile", style = SettingsScreenStyles.sectionTitleStyle())
    EditableField(
      label = "First Name",
      value = uiState.firstName,
      error = uiState.firstNameError,
      testTag = SettingsTestTags.FIRST_NAME_BUTTON,
      onClick = { open("firstName") }
    )
    EditableField(
      label = "Last Name",
      value = uiState.lastName,
      error = uiState.lastNameError,
      testTag = SettingsTestTags.LAST_NAME_BUTTON,
      onClick = { open("lastName") }
    )
    EditableField(
      label = "Description",
      value = uiState.description.take(30) + if (uiState.description.length > 30) "..." else "",
      error = uiState.descriptionError,
      testTag = SettingsTestTags.DESCRIPTION_BUTTON,
      onClick = { open("description") }
    )
    EditableField(
      label = "Country",
      value = uiState.country,
      testTag = SettingsTestTags.COUNTRY_BUTTON,
      onClick = { open("country") }
    )
    EditableField(
      label = "Date of Birth",
      value = "${uiState.year}-${uiState.month}-${uiState.day}",
      testTag = SettingsTestTags.DATE_BUTTON,
      onClick = { open("date") }
    )
    uiState.dayError?.let {
      Text(
        it,
        color = MaterialTheme.colorScheme.error,
        modifier = Modifier.padding(start = SettingsScreenPaddings.ErrorIndent)
      )
    }
    uiState.monthError?.let {
      Text(
        it,
        color = MaterialTheme.colorScheme.error,
        modifier = Modifier.padding(start = SettingsScreenPaddings.ErrorIndent)
      )
    }
    uiState.yearError?.let {
      Text(
        it,
        color = MaterialTheme.colorScheme.error,
        modifier = Modifier.padding(start = SettingsScreenPaddings.ErrorIndent)
      )
    }
  }
}

@Composable
private fun InterestsSection(uiState: SettingsUiState, open: (String) -> Unit) {
  Column(verticalArrangement = Arrangement.spacedBy(SettingsScreenPaddings.InternalSpacing)) {
    HorizontalDivider(
      color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
      thickness = 0.5.dp,
      modifier = Modifier.padding(vertical = SettingsScreenPaddings.DividerPadding)
    )
    Text("Interests", style = SettingsScreenStyles.sectionTitleStyle())
    Tag.Category.entries.forEach { category ->
      ChipsLine(
        label = category.displayName,
        names = Tag.toDisplayNamesByCategory(uiState.selectedTags, category),
        testTag = "SettingsTestTags.${category.name}_BUTTON",
        onOpen = { open(category.fieldName) }
      )
    }
  }
}

/* =========================================================
 * Reusable field row (stateless)
 * ========================================================= */
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
      .padding(vertical = SettingsScreenPaddings.InternalSpacing),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
    Text(
      value,
      style = MaterialTheme.typography.bodyMedium,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
    Spacer(modifier = Modifier.width(SettingsScreenPaddings.FieldIconSpacing))
    Icon(Icons.Filled.Edit, contentDescription = "Edit $label")
  }
  if (error != null) {
    Text(
      error,
      color = MaterialTheme.colorScheme.error,
      modifier = Modifier.padding(start = SettingsScreenPaddings.ErrorIndent)
    )
  }
}

/* =========================================================
 * Previews (use stateless content only)
 * ========================================================= */
fun sampleSettingsState(showModal: Boolean = false, field: String = "") =
  SettingsUiState(
    email = "preview@example.com",
    firstName = "Emma",
    lastName = "Prolapse",
    country = "Switzerland",
    description = "Loves Kotlin, skiing, and fondue. Building UniVERSE app.",
    day = "05",
    month = "01",
    year = "2000",
    // Use Tag enums directly
    selectedTags = listOf(Tag.HIKING, Tag.CYCLING, Tag.CLASSICAL, Tag.TRAIN, Tag.VAUD),
    showModal = showModal,
    currentField = field
  )

@Preview(showBackground = true, name = "Settings")
@Composable
private fun SettingsScreenContent_Preview() {
  MaterialTheme {
    SettingsScreenContent(uiState = sampleSettingsState(), onOpenField = {}, onBack = {})
  }
}

@Preview(showBackground = true, name = "Settings – modal open")
@Composable
private fun SettingsScreenContent_Modal_Preview() {
  MaterialTheme {
    SettingsScreenContent(
      uiState = sampleSettingsState(showModal = true, field = "firstName"),
      onOpenField = {},
      onUpdateTemp = { _, _ -> },
      onToggleCountryDropdown = {},
      onAddTag = {},
      onRemoveTag = {},
      onCloseModal = {},
      onSaveModal = {}
    )
  }
}
