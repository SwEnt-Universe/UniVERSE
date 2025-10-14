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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.universe.model.CountryData
import com.android.universe.model.Tag
import com.android.universe.model.tagsCanton
import com.android.universe.model.tagsInterest
import com.android.universe.model.tagsMusic
import com.android.universe.model.tagsSport
import com.android.universe.model.tagsTransport
import com.android.universe.ui.common.TagGroup
import com.android.universe.ui.profile.SettingsUiState
import com.android.universe.ui.profile.SettingsViewModel
import com.android.universe.ui.selectTag.TagColors

/* =========================================================
 * Stateful wrapper (production)
 * ========================================================= */
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
    uiState.errorMsg?.let {
      Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
      viewModel.clearErrorMsg()
    }
  }

  LaunchedEffect(username) { viewModel.loadUser(username) }

  SettingsScreenContent(
    uiState = uiState,
    onBack = onCancel,
    onOpenField = viewModel::openModal,
    onCloseModal = viewModel::closeModal,
    onUpdateField = viewModel::updateField,
    onToggleCountryDropdown = viewModel::toggleCountryDropdown,
    onAddTag = viewModel::addTag,
    onRemoveTag = viewModel::removeTag,
    onSaveModal = { viewModel.saveModal(username, onSaveSuccess) })
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
  onUpdateField: (String, String) -> Unit = { _, _ -> },
  onToggleCountryDropdown: (Boolean) -> Unit = {},
  onAddTag: (String) -> Unit = {},
  onRemoveTag: (String) -> Unit = {},
  onSaveModal: () -> Unit = {}
) {
  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Settings") },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Cancel")
          }
        })
    }) { padding ->
    LazyColumn(
      modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)) {
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
        onUpdateField = onUpdateField,
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
  Column {
    Text("General", style = MaterialTheme.typography.titleLarge)
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
  Column {
    HorizontalDivider(
      color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
      thickness = 0.5.dp,
      modifier = Modifier.padding(vertical = 20.dp))
    Text("Profile", style = MaterialTheme.typography.titleLarge)
    Spacer(modifier = Modifier.height(8.dp))
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
      Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(start = 16.dp))
    }
    uiState.monthError?.let {
      Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(start = 16.dp))
    }
    uiState.yearError?.let {
      Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(start = 16.dp))
    }
  }
}

@Composable
private fun InterestsSection(uiState: SettingsUiState, open: (String) -> Unit) {
  @Composable
  fun chipsLine(label: String, names: List<String>, tag: String) =
    EditableField(
      label = label,
      value =
        names.joinToString(", ").take(30) +
            if (names.joinToString().length > 30) "..." else "",
      testTag = tag) {
      open(tag.removeSuffix("_button"))
    }

  Column {
    HorizontalDivider(
      color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
      thickness = 0.5.dp,
      modifier = Modifier.padding(vertical = 20.dp))
    Text("Interests", style = MaterialTheme.typography.titleLarge)
    Spacer(modifier = Modifier.height(8.dp))

    chipsLine(
      "Hobbies",
      uiState.selectedTags.filter { it.name in tagsInterest }.map { it.name },
      SettingsTestTags.INTEREST_TAGS_BUTTON)
    HorizontalDivider(
      color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
      thickness = 1.dp,
      modifier = Modifier.padding(vertical = 8.dp))
    chipsLine(
      "Sport",
      uiState.selectedTags.filter { it.name in tagsSport }.map { it.name },
      SettingsTestTags.SPORT_TAGS_BUTTON)
    HorizontalDivider(
      color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
      thickness = 1.dp,
      modifier = Modifier.padding(vertical = 8.dp))
    chipsLine(
      "Music",
      uiState.selectedTags.filter { it.name in tagsMusic }.map { it.name },
      SettingsTestTags.MUSIC_TAGS_BUTTON)
    HorizontalDivider(
      color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
      thickness = 1.dp,
      modifier = Modifier.padding(vertical = 8.dp))
    chipsLine(
      "Transport",
      uiState.selectedTags.filter { it.name in tagsTransport }.map { it.name },
      SettingsTestTags.TRANSPORT_TAGS_BUTTON)
    HorizontalDivider(
      color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
      thickness = 1.dp,
      modifier = Modifier.padding(vertical = 8.dp))
    chipsLine(
      "Canton",
      uiState.selectedTags.filter { it.name in tagsCanton }.map { it.name },
      SettingsTestTags.CANTON_TAGS_BUTTON)
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
      Modifier.fillMaxWidth().clickable { onClick() }.testTag(testTag).padding(vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically) {
    Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
    Text(
      value,
      style = MaterialTheme.typography.bodyMedium,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis)
    Spacer(modifier = Modifier.width(8.dp))
    Icon(Icons.Default.Edit, contentDescription = "Edit $label")
  }
  if (error != null) {
    Text(error, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(start = 16.dp))
  }
}

/* =========================================================
 * Modal (stateless)
 * ========================================================= */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModalContentContentOnly(
  uiState: SettingsUiState,
  onUpdateField: (String, String) -> Unit,
  onToggleCountryDropdown: (Boolean) -> Unit,
  onAddTag: (String) -> Unit,
  onRemoveTag: (String) -> Unit,
  onClose: () -> Unit,
  onSave: () -> Unit
) {
  Column(
    modifier = Modifier.fillMaxWidth().padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Text(
      text =
        when (uiState.currentField) {
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
      style = MaterialTheme.typography.titleMedium)

    when (uiState.currentField) {
      "email",
      "password",
      "firstName",
      "lastName",
      "description" -> {
        val value =
          when (uiState.currentField) {
            "email" -> uiState.email
            "password" -> uiState.password
            "firstName" -> uiState.firstName
            "lastName" -> uiState.lastName
            "description" -> uiState.description
            else -> ""
          }
        val label =
          when (uiState.currentField) {
            "email" -> "Email"
            "password" -> "New Password"
            "firstName" -> "First Name"
            "lastName" -> "Last Name"
            "description" -> "Description"
            else -> ""
          }

        OutlinedTextField(
          value = value,
          onValueChange = { onUpdateField(uiState.currentField, it) },
          label = { Text(label) },
          modifier =
            Modifier.fillMaxWidth().onFocusChanged {
              if (it.isFocused) {
                onUpdateField(uiState.currentField, value)
              }
            },
          isError = uiState.modalError != null,
          supportingText = { uiState.modalError?.let { msg -> Text(msg) } },
          shape = RoundedCornerShape(12.dp),
          maxLines = if (uiState.currentField == "description") 3 else 1)
      }

      "country" -> {
        ExposedDropdownMenuBox(
          expanded = uiState.showCountryDropdown,
          onExpandedChange = { onToggleCountryDropdown(!uiState.showCountryDropdown) }) {
          OutlinedTextField(
            value = uiState.country,
            onValueChange = {},
            readOnly = true,
            label = { Text("Country") },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            shape = RoundedCornerShape(12.dp))
          ExposedDropdownMenu(
            expanded = uiState.showCountryDropdown,
            onDismissRequest = { onToggleCountryDropdown(false) },
            modifier = Modifier.heightIn(max = 240.dp)) {
            CountryData.allCountries.forEach { countryOption ->
              DropdownMenuItem(
                text = {
                  Text(
                    countryOption.take(30) +
                        if (countryOption.length > 30) "..." else "")
                },
                onClick = {
                  onUpdateField("country", countryOption)
                  onToggleCountryDropdown(false)
                })
            }
          }
        }
      }

      "date" -> {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = uiState.day,
            onValueChange = { onUpdateField("day", it) },
            label = { Text("Day") },
            modifier =
              Modifier.weight(1f).onFocusChanged {
                if (it.isFocused) onUpdateField("day", uiState.day)
              },
            isError = uiState.dayError != null,
            supportingText = { uiState.dayError?.let { msg -> Text(msg) } },
            shape = RoundedCornerShape(12.dp))
          OutlinedTextField(
            value = uiState.month,
            onValueChange = { onUpdateField("month", it) },
            label = { Text("Month") },
            modifier =
              Modifier.weight(1f).onFocusChanged {
                if (it.isFocused) onUpdateField("month", uiState.month)
              },
            isError = uiState.monthError != null,
            supportingText = { uiState.monthError?.let { msg -> Text(msg) } },
            shape = RoundedCornerShape(12.dp))
          OutlinedTextField(
            value = uiState.year,
            onValueChange = { onUpdateField("year", it) },
            label = { Text("Year") },
            modifier =
              Modifier.weight(1.5f).onFocusChanged {
                if (it.isFocused) onUpdateField("year", uiState.year)
              },
            isError = uiState.yearError != null,
            supportingText = { uiState.yearError?.let { msg -> Text(msg) } },
            shape = RoundedCornerShape(12.dp))
        }
      }

      "interest_tags" -> {
        TagGroup(
          name = "",
          tagList = tagsInterest,
          selectedTags = uiState.tempSelectedTags,
          color = TagColors.Interest,
          onTagSelect = onAddTag,
          onTagReSelect = onRemoveTag,
          modifier = Modifier.fillMaxWidth())
      }

      "sport_tags" -> {
        TagGroup(
          name = "",
          tagList = tagsSport,
          selectedTags = uiState.tempSelectedTags,
          color = TagColors.Sport,
          onTagSelect = onAddTag,
          onTagReSelect = onRemoveTag,
          modifier = Modifier.fillMaxWidth())
      }

      "music_tags" -> {
        TagGroup(
          name = "",
          tagList = tagsMusic,
          selectedTags = uiState.tempSelectedTags,
          color = TagColors.Music,
          onTagSelect = onAddTag,
          onTagReSelect = onRemoveTag,
          modifier = Modifier.fillMaxWidth())
      }

      "transport_tags" -> {
        TagGroup(
          name = "",
          tagList = tagsTransport,
          selectedTags = uiState.tempSelectedTags,
          color = TagColors.Transport,
          onTagSelect = onAddTag,
          onTagReSelect = onRemoveTag,
          modifier = Modifier.fillMaxWidth())
      }

      "canton_tags" -> {
        TagGroup(
          name = "",
          tagList = tagsCanton,
          selectedTags = uiState.tempSelectedTags,
          color = TagColors.Canton,
          onTagSelect = onAddTag,
          onTagReSelect = onRemoveTag,
          modifier = Modifier.fillMaxWidth())
      }
    }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
      TextButton(
        onClick = onClose,
        modifier = Modifier.testTag(SettingsTestTags.MODAL_CANCEL_BUTTON)) {
        Text("Cancel")
      }

      TextButton(
        onClick = onSave, modifier = Modifier.testTag(SettingsTestTags.MODAL_SAVE_BUTTON)) {
        Text("Save")
      }
    }
  }
}

/* =========================================================
 * Previews (use stateless content only)
 * ========================================================= */
private fun sampleSettingsState(showModal: Boolean = false, field: String = "") =
  SettingsUiState(
    email = "preview@example.com",
    firstName = "Emma",
    lastName = "Univers",
    country = "Switzerland",
    description = "Loves Kotlin, skiing, and fondue. Building UniVERSE app.",
    day = "05",
    month = "01",
    year = "2000",
    selectedTags = listOf("Hiking", "Cycling", "Classical", "Train", "Vaud").map { Tag(it) },
    showModal = showModal,
    currentField = field)

@Preview(showBackground = true, name = "Settings")
@Composable
private fun SettingsScreenContent_Preview() {
  MaterialTheme {
    SettingsScreenContent(
      uiState = sampleSettingsState(), onOpenField = {}, onBack = {})
  }
}

@Preview(showBackground = true, name = "Settings – modal open")
@Composable
private fun SettingsScreenContent_Modal_Preview() {
  MaterialTheme {
    SettingsScreenContent(
      uiState = sampleSettingsState(showModal = true, field = "firstName"),
      onOpenField = {},
      onUpdateField = { _, _ -> },
      onToggleCountryDropdown = {},
      onAddTag = {},
      onRemoveTag = {},
      onCloseModal = {},
      onSaveModal = {})
  }
}