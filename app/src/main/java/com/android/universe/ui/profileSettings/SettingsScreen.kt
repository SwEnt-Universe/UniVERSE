package com.android.universe.ui.profileSettings

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.android.universe.model.CountryData
import com.android.universe.model.Tag
import com.android.universe.ui.common.TagGroup
import com.android.universe.ui.profile.SettingsUiState
import com.android.universe.ui.profile.SettingsViewModel
import com.android.universe.ui.selectTag.TagColors

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
private fun modalTitle(field: String): String =
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
      else -> ""
    }

@Composable
private fun ChipsLine(label: String, names: List<String>, testTag: String, onOpen: () -> Unit) {
  val joined = names.joinToString(", ")
  EditableField(
      label = label,
      value = if (joined.length > 30) joined.take(30) + "..." else joined,
      testTag = testTag,
      onClick = onOpen)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CountryDropdown(
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
        modifier = Modifier.fillMaxWidth().menuAnchor(),
        shape = RoundedCornerShape(12.dp))
    ExposedDropdownMenu(
        expanded = expanded,
        onDismissRequest = { onExpandedChange(false) },
        modifier = Modifier.heightIn(max = 240.dp)) {
          CountryData.allCountries.forEach { option ->
            DropdownMenuItem(
                text = { Text(if (option.length > 30) option.take(30) + "..." else option) },
                onClick = { onPick(option) })
          }
        }
  }
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
      onSaveModal = { viewModel.saveModal(username) })
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
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
              }
            })
      }) { padding ->
        LazyColumn(
            modifier =
                Modifier.fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = SettingsScreenPaddings.ContentHorizontalPadding)) {
              item { GeneralSection(uiState = uiState, open = onOpenField) }
              item { ProfileSection(uiState = uiState, open = onOpenField) }
              item { InterestsSection(uiState = uiState, open = onOpenField) }
            }
      }

  if (uiState.showModal) {
    ModalBottomSheet(
        onDismissRequest = onCloseModal, sheetState = rememberModalBottomSheetState()) {
          ModalContentContentOnly(
              uiState = uiState,
              onUpdateTemp = onUpdateTemp,
              onToggleCountryDropdown = onToggleCountryDropdown,
              onAddTag = onAddTag,
              onRemoveTag = onRemoveTag,
              onClose = onCloseModal,
              onSave = onSaveModal)
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
        onClick = { open("email") })
    EditableField(
        label = "Password",
        value = if (uiState.password.isEmpty()) "Unchanged" else "********",
        error = uiState.passwordError,
        testTag = SettingsTestTags.PASSWORD_BUTTON,
        onClick = { open("password") })
  }
}

@Composable
private fun ProfileSection(uiState: SettingsUiState, open: (String) -> Unit) {
  Column(verticalArrangement = Arrangement.spacedBy(SettingsScreenPaddings.InternalSpacing)) {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        thickness = 0.5.dp,
        modifier = Modifier.padding(vertical = SettingsScreenPaddings.DividerPadding))
    Text("Profile", style = SettingsScreenStyles.sectionTitleStyle())
    EditableField(
        label = "First Name",
        value = uiState.firstName,
        error = uiState.firstNameError,
        testTag = SettingsTestTags.FIRST_NAME_BUTTON,
        onClick = { open("firstName") })
    EditableField(
        label = "Last Name",
        value = uiState.lastName,
        error = uiState.lastNameError,
        testTag = SettingsTestTags.LAST_NAME_BUTTON,
        onClick = { open("lastName") })
    EditableField(
        label = "Description",
        value = uiState.description.take(30) + if (uiState.description.length > 30) "..." else "",
        error = uiState.descriptionError,
        testTag = SettingsTestTags.DESCRIPTION_BUTTON,
        onClick = { open("description") })
    EditableField(
        label = "Country",
        value = uiState.country,
        testTag = SettingsTestTags.COUNTRY_BUTTON,
        onClick = { open("country") })
    EditableField(
        label = "Date of Birth",
        value = "${uiState.year}-${uiState.month}-${uiState.day}",
        testTag = SettingsTestTags.DATE_BUTTON,
        onClick = { open("date") })
    uiState.dayError?.let {
      Text(
          it,
          color = MaterialTheme.colorScheme.error,
          modifier = Modifier.padding(start = SettingsScreenPaddings.ErrorIndent))
    }
    uiState.monthError?.let {
      Text(
          it,
          color = MaterialTheme.colorScheme.error,
          modifier = Modifier.padding(start = SettingsScreenPaddings.ErrorIndent))
    }
    uiState.yearError?.let {
      Text(
          it,
          color = MaterialTheme.colorScheme.error,
          modifier = Modifier.padding(start = SettingsScreenPaddings.ErrorIndent))
    }
  }
}

@Composable
private fun InterestsSection(uiState: SettingsUiState, open: (String) -> Unit) {
  Column(verticalArrangement = Arrangement.spacedBy(SettingsScreenPaddings.InternalSpacing)) {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        thickness = 0.5.dp,
        modifier = Modifier.padding(vertical = SettingsScreenPaddings.DividerPadding))
    Text("Interests", style = SettingsScreenStyles.sectionTitleStyle())
    Tag.Category.entries.forEach { category ->
      ChipsLine(
          label = category.displayName,
          names = Tag.toDisplayNamesByCategory(uiState.selectedTags, category),
          testTag = "SettingsTestTags.${category.name}_BUTTON",
          onOpen = { open(category.fieldName) })
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
      modifier =
          Modifier.fillMaxWidth()
              .clickable { onClick() }
              .testTag(testTag)
              .padding(vertical = SettingsScreenPaddings.InternalSpacing),
      verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis)
        Spacer(modifier = Modifier.width(SettingsScreenPaddings.FieldIconSpacing))
        Icon(Icons.Default.Edit, contentDescription = "Edit $label")
      }
  if (error != null) {
    Text(
        error,
        color = MaterialTheme.colorScheme.error,
        modifier = Modifier.padding(start = SettingsScreenPaddings.ErrorIndent))
  }
}

/* =========================================================
 * Modal (stateless)
 * ========================================================= */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModalContentContentOnly(
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {
              Text(
                  text = modalTitle(uiState.currentField),
                  style = MaterialTheme.typography.titleMedium,
                  modifier = Modifier.weight(1f))
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
        Spacer(modifier = Modifier.height(SettingsScreenPaddings.InternalSpacing))

        when (uiState.currentField) {
          "email" -> {
            OutlinedTextField(
                value = uiState.tempValue,
                onValueChange = { onUpdateTemp("tempValue", it) },
                modifier = Modifier.fillMaxWidth().testTag(SettingsTestTags.EMAIL_FIELD),
                isError = uiState.modalError != null,
                supportingText = { uiState.modalError?.let { msg -> Text(msg) } },
                shape = RoundedCornerShape(12.dp),
                maxLines = 1)
          }
          "password" -> {
            OutlinedTextField(
                value = uiState.tempValue,
                onValueChange = { onUpdateTemp("tempValue", it) },
                modifier = Modifier.fillMaxWidth().testTag(SettingsTestTags.PASSWORD_FIELD),
                isError = uiState.modalError != null,
                supportingText = { uiState.modalError?.let { msg -> Text(msg) } },
                shape = RoundedCornerShape(12.dp),
                maxLines = 1)
          }
          "firstName" -> {
            OutlinedTextField(
                value = uiState.tempValue,
                onValueChange = { onUpdateTemp("tempValue", it) },
                modifier = Modifier.fillMaxWidth().testTag(SettingsTestTags.FIRST_NAME_FIELD),
                isError = uiState.modalError != null,
                supportingText = { uiState.modalError?.let { msg -> Text(msg) } },
                shape = RoundedCornerShape(12.dp),
                maxLines = 1)
          }
          "lastName" -> {
            OutlinedTextField(
                value = uiState.tempValue,
                onValueChange = { onUpdateTemp("tempValue", it) },
                modifier = Modifier.fillMaxWidth().testTag(SettingsTestTags.LAST_NAME_FIELD),
                isError = uiState.modalError != null,
                supportingText = { uiState.modalError?.let { msg -> Text(msg) } },
                shape = RoundedCornerShape(12.dp),
                maxLines = 1)
          }
          "description" -> {
            OutlinedTextField(
                value = uiState.tempValue,
                onValueChange = { onUpdateTemp("tempValue", it) },
                modifier = Modifier.fillMaxWidth().testTag(SettingsTestTags.DESCRIPTION_FIELD),
                isError = uiState.modalError != null,
                supportingText = { uiState.modalError?.let { msg -> Text(msg) } },
                shape = RoundedCornerShape(12.dp),
                maxLines = 3)
          }
          "country" -> {
            CountryDropdown(
                value = uiState.tempValue,
                expanded = uiState.showCountryDropdown,
                onExpandedChange = onToggleCountryDropdown,
                onPick = {
                  onUpdateTemp("tempValue", it)
                  onToggleCountryDropdown(false)
                })
          }
          "date" -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(SettingsScreenPaddings.DateFieldSpacing)) {
                  OutlinedTextField(
                      value = uiState.tempDay,
                      onValueChange = { onUpdateTemp("tempDay", it) },
                      label = { Text("Day") },
                      modifier = Modifier.weight(1f).testTag(SettingsTestTags.DAY_FIELD),
                      isError = uiState.tempDayError != null,
                      supportingText = { uiState.tempDayError?.let { msg -> Text(msg) } },
                      shape = RoundedCornerShape(12.dp))
                  OutlinedTextField(
                      value = uiState.tempMonth,
                      onValueChange = { onUpdateTemp("tempMonth", it) },
                      label = { Text("Month") },
                      modifier = Modifier.weight(1f).testTag(SettingsTestTags.MONTH_FIELD),
                      isError = uiState.tempMonthError != null,
                      supportingText = { uiState.tempMonthError?.let { msg -> Text(msg) } },
                      shape = RoundedCornerShape(12.dp))
                  OutlinedTextField(
                      value = uiState.tempYear,
                      onValueChange = { onUpdateTemp("tempYear", it) },
                      label = { Text("Year") },
                      modifier = Modifier.weight(1.5f).testTag(SettingsTestTags.YEAR_FIELD),
                      isError = uiState.tempYearError != null,
                      supportingText = { uiState.tempYearError?.let { msg -> Text(msg) } },
                      shape = RoundedCornerShape(12.dp))
                }
          }
          else -> {
            // Tag categories (bridge Tag <-> String for TagGroup)
            Tag.Category.entries
                .find { it.fieldName == uiState.currentField }
                ?.let { category ->
                  TagGroup(
                      name = "",
                      tagList = Tag.getDisplayNamesForCategory(category), // List<String>
                      selectedTags =
                          uiState.tempSelectedTags.map { it.displayName }, // List<String>
                      color =
                          when (category) {
                            Tag.Category.INTEREST -> TagColors.Interest
                            Tag.Category.SPORT -> TagColors.Sport
                            Tag.Category.MUSIC -> TagColors.Music
                            Tag.Category.TRANSPORT -> TagColors.Transport
                            Tag.Category.CANTON -> TagColors.Canton
                          },
                      onTagSelect = { displayName ->
                        Tag.fromDisplayName(displayName)?.let(onAddTag)
                      },
                      onTagReSelect = { displayName ->
                        Tag.fromDisplayName(displayName)?.let(onRemoveTag)
                      },
                      modifier = Modifier.fillMaxWidth())
                }
          }
        }
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
        currentField = field)

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
        onSaveModal = {})
  }
}
