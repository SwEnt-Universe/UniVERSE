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
 * Padding/style constants
 * ========================================================= */
object SettingsScreenPaddings {
  val InternalSpacing = 4.dp // Spacing between fields, titles, tag categories, and modal content
  val DividerPadding = 20.dp // Vertical padding for section dividers
  val ContentHorizontalPadding = 20.dp // Horizontal padding for main content and modal
  val ErrorIndent = 8.dp // Start padding for error messages
  val FieldIconSpacing = 10.dp // Horizontal spacing between field value and edit icon
  val DateFieldSpacing = 8.dp // Spacing between date fields in modal
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
      "interest_tags" -> "Edit Interest Tags"
      "sport_tags" -> "Edit Sport Tags"
      "music_tags" -> "Edit Music Tags"
      "transport_tags" -> "Edit Transport Tags"
      "canton_tags" -> "Edit Canton Tags"
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
    // onSaveSuccess: () -> Unit = {},
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
    ChipsLine(
        label = "Interests",
        names = uiState.selectedTags.filter { it.name in tagsInterest }.map { it.name },
        testTag = SettingsTestTags.INTEREST_TAGS_BUTTON,
        onOpen = { open("interest_tags") })
    ChipsLine(
        label = "Sport",
        names = uiState.selectedTags.filter { it.name in tagsSport }.map { it.name },
        testTag = SettingsTestTags.SPORT_TAGS_BUTTON,
        onOpen = { open("sport_tags") })
    ChipsLine(
        label = "Music",
        names = uiState.selectedTags.filter { it.name in tagsMusic }.map { it.name },
        testTag = SettingsTestTags.MUSIC_TAGS_BUTTON,
        onOpen = { open("music_tags") })
    ChipsLine(
        label = "Transport",
        names = uiState.selectedTags.filter { it.name in tagsTransport }.map { it.name },
        testTag = SettingsTestTags.TRANSPORT_TAGS_BUTTON,
        onOpen = { open("transport_tags") })
    ChipsLine(
        label = "Canton",
        names = uiState.selectedTags.filter { it.name in tagsCanton }.map { it.name },
        testTag = SettingsTestTags.CANTON_TAGS_BUTTON,
        onOpen = { open("canton_tags") })
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
    onAddTag: (String) -> Unit,
    onRemoveTag: (String) -> Unit,
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
          "email",
          "password",
          "firstName",
          "lastName",
          "description" -> {
            OutlinedTextField(
                value = uiState.tempValue,
                onValueChange = { onUpdateTemp("tempValue", it) },
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.modalError != null,
                supportingText = { uiState.modalError?.let { msg -> Text(msg) } },
                shape = RoundedCornerShape(12.dp),
                maxLines = if (uiState.currentField == "description") 3 else 1)
          }

          "country" ->
              CountryDropdown(
                  value = uiState.tempValue,
                  expanded = uiState.showCountryDropdown,
                  onExpandedChange = onToggleCountryDropdown,
                  onPick = {
                    onUpdateTemp("tempValue", it)
                    onToggleCountryDropdown(false)
                  })

          "date" -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(SettingsScreenPaddings.DateFieldSpacing)) {
                  OutlinedTextField(
                      value = uiState.tempDay,
                      onValueChange = { onUpdateTemp("tempDay", it) },
                      label = { Text("Day") },
                      modifier = Modifier.weight(1f),
                      isError = uiState.tempDayError != null,
                      supportingText = { uiState.tempDayError?.let { msg -> Text(msg) } },
                      shape = RoundedCornerShape(12.dp))
                  OutlinedTextField(
                      value = uiState.tempMonth,
                      onValueChange = { onUpdateTemp("tempMonth", it) },
                      label = { Text("Month") },
                      modifier = Modifier.weight(1f),
                      isError = uiState.tempMonthError != null,
                      supportingText = { uiState.tempMonthError?.let { msg -> Text(msg) } },
                      shape = RoundedCornerShape(12.dp))
                  OutlinedTextField(
                      value = uiState.tempYear,
                      onValueChange = { onUpdateTemp("tempYear", it) },
                      label = { Text("Year") },
                      modifier = Modifier.weight(1.5f),
                      isError = uiState.tempYearError != null,
                      supportingText = { uiState.tempYearError?.let { msg -> Text(msg) } },
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
      }
}

/* =========================================================
 * Previews (use stateless content only)
 * ========================================================= */
private fun sampleSettingsState(showModal: Boolean = false, field: String = "") =
    SettingsUiState(
        email = "preview@example.com",
        firstName = "Emma",
        lastName = "Prolapse",
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
