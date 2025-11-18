package com.android.universe.ui.profileSettings

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.universe.model.tag.Tag
import com.android.universe.ui.common.LogoutButton
import com.android.universe.ui.common.LogoutConfirmationDialog
import com.android.universe.ui.navigation.NavigationTestTags
import com.android.universe.ui.theme.Dimensions
import com.android.universe.ui.theme.UniverseTheme
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/* =========================================================
 * Padding/style constants
 * ========================================================= */
object SettingsScreenPaddings {
  val InternalSpacing = Dimensions.PaddingSmall
  val DividerPadding = Dimensions.PaddingExtraLarge
  val ContentHorizontalPadding = Dimensions.PaddingExtraLarge
  val ErrorIndent = Dimensions.PaddingMedium
  val FieldIconSpacing = Dimensions.PaddingFieldIconSpacing
  val DateFieldSpacing = Dimensions.PaddingMedium
}

object SettingsScreenStyles {
  @Composable fun sectionTitleStyle() = MaterialTheme.typography.titleLarge
}

/* =========================================================
 * Helper methods
 * ========================================================= */
/** Maps internal field keys to modal titles. */
internal fun modalTitle(field: String): String =
    when (field) {
      "email" -> "Edit Email"
      "password" -> "Edit Password"
      "firstName" -> "Edit First Name"
      "lastName" -> "Edit Last Name"
      "description" -> "Edit Description"
      "country" -> "Edit Country"
      "date" -> "Edit Date of Birth"
      Tag.Category.MUSIC.fieldName -> Tag.Category.MUSIC.displayName
      Tag.Category.SPORT.fieldName -> Tag.Category.SPORT.displayName
      Tag.Category.FOOD.fieldName -> Tag.Category.FOOD.displayName
        Tag.Category.ART.fieldName -> Tag.Category.ART.displayName
        Tag.Category.TRAVEL.fieldName -> Tag.Category.TRAVEL.displayName
        Tag.Category.GAMES.fieldName -> Tag.Category.GAMES.displayName
        Tag.Category.TECHNOLOGY.fieldName -> Tag.Category.TECHNOLOGY.displayName
        Tag.Category.TOPIC.fieldName -> Tag.Category.TOPIC.displayName

      else -> field.ifBlank { "Edit" }
    }

/** Reusable editable text row with trailing edit icon. */
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
        Icon(Icons.Filled.Edit, contentDescription = "Edit $label")
      }
  if (error != null) {
    Text(
        error,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.error,
        modifier = Modifier.padding(start = SettingsScreenPaddings.ErrorIndent))
  }
}

/** Reusable row for displaying a list of tags. */
@Composable
private fun ChipsLine(label: String, names: List<String>, testTag: String, onOpen: () -> Unit) {
  val joined = names.joinToString(", ")
  EditableField(
      label = label,
      value = if (joined.length > 30) joined.take(30) + "..." else joined,
      testTag = testTag,
      onClick = onOpen)
}

/* =========================================================
 * Stateful wrapper (production)
 * ========================================================= */
/**
 * Top-level composable for the user Settings screen.
 *
 * @param uid Logged-in user's uid.
 * @param onBack Callback when back arrow pressed.
 * @param viewModel Shared [SettingsViewModel] for state and actions.
 * @param onLogout to log the user out
 * @param clear to clear the credential state
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uid: String,
    onBack: () -> Unit = {},
    viewModel: SettingsViewModel = viewModel(),
    onLogout: () -> Unit = {},
    clear: suspend () -> Unit = {}
) {
  val uiState by viewModel.uiState.collectAsState()
  val context = LocalContext.current

  LaunchedEffect(uiState.errorMsg) {
    uiState.errorMsg?.let {
      Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
      viewModel.clearErrorMsg()
    }
  }

  LaunchedEffect(uid) { viewModel.loadUser(uid) }

  SettingsScreenContent(
      uiState = uiState,
      onBack = onBack,
      onOpenField = viewModel::openModal,
      onCloseModal = viewModel::closeModal,
      onUpdateTemp = viewModel::updateTemp,
      onToggleCountryDropdown = viewModel::toggleCountryDropdown,
      onAddTag = viewModel::addTag,
      onRemoveTag = viewModel::removeTag,
      onSaveModal = { viewModel.saveModal(uid) },
      onLogout = { viewModel.signOut(clear, onLogout) },
      onSelectPicture = { byteArray -> viewModel.updateProfilePicture(byteArray, uid) })
}

/** Stateless content of the Settings screen, allowing for previews and tests. */
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
    onSaveModal: () -> Unit = {},
    onLogout: () -> Unit = {},
    onSelectPicture: (ByteArray?) -> Unit = {}
) {
  val showDialog = remember { mutableStateOf(false) }
  LogoutConfirmationDialog(
      showDialog = showDialog.value,
      onConfirm = {
        showDialog.value = false
        onLogout()
      },
      onDismiss = { showDialog.value = false })
  Scaffold(
      topBar = {
        TopAppBar(
            title = {
              Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically) {
                    Text("Settings")
                    LogoutButton(onClick = { showDialog.value = true })
                  }
            },
            navigationIcon = {
              IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
              }
            },
            modifier = Modifier.testTag(NavigationTestTags.SETTINGS_SCREEN))
      }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
          // Profile picture of the user.
          val context = LocalContext.current
          val scope = rememberCoroutineScope()
          val launcher =
              rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) {
                  uri: Uri? ->
                uri?.let { selectedUri ->
                  scope.launch(Dispatchers.IO) {
                    // We redimension the image to have a 256*256 image to reduce the space of the
                    // image.
                    val maxSize = Dimensions.ProfilePictureSize

                    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }

                    context.contentResolver.openInputStream(selectedUri)?.use { input ->
                      BitmapFactory.decodeStream(input, null, options)
                    }

                    val (height: Int, width: Int) = options.run { outHeight to outWidth }
                    var inSampleSize = 1
                    if (height > maxSize || width > maxSize) {
                      val halfHeight = height / 2
                      val halfWidth = width / 2
                      while ((halfHeight / inSampleSize) >= maxSize &&
                          (halfWidth / inSampleSize) >= maxSize) {
                        inSampleSize *= 2
                      }
                    }

                    options.inSampleSize = inSampleSize
                    options.inJustDecodeBounds = false

                    val bitmap =
                        context.contentResolver.openInputStream(selectedUri)?.use { input ->
                          BitmapFactory.decodeStream(input, null, options)
                        }

                    if (bitmap == null) {
                      Log.e("ImageError", "Failed to decode bitmap from URI $selectedUri")
                    } else {
                      val stream = ByteArrayOutputStream()
                      // We compress the image with a low quality to reduce the space of the image.
                      bitmap.compress(Bitmap.CompressFormat.JPEG, 45, stream)
                      val byteArray = stream.toByteArray()
                      withContext(Dispatchers.Main) { onSelectPicture(byteArray) }
                    }
                  }
                }
              }
          Box(
              modifier =
                  Modifier.align(Alignment.CenterHorizontally)
                      .padding(Dimensions.PaddingSmall)
                      .size(100.dp)
                      .background(MaterialTheme.colorScheme.surface, CircleShape)
                      .testTag(SettingsTestTags.PICTURE_EDITING),
              contentAlignment = Alignment.Center) {
                IconButton(onClick = { launcher.launch("image/*") }) {
                  Icon(
                      tint = MaterialTheme.colorScheme.onSurface,
                      contentDescription = "Image",
                      imageVector = Icons.Filled.Image,
                      modifier = Modifier.size(Dimensions.IconSizeLarge))
                }
              }
          Button(
              onClick = { onSelectPicture(null) },
              modifier =
                  Modifier.align(Alignment.CenterHorizontally)
                      .testTag(SettingsTestTags.DELETE_PICTURE_BUTTON)) {
                Text("delete profile picture")
              }
          LazyColumn(
              modifier =
                  Modifier.fillMaxSize()
                      .padding(
                          horizontal = SettingsScreenPaddings.ContentHorizontalPadding,
                          vertical = Dimensions.PaddingSmall)) {
                item { GeneralSection(uiState = uiState, open = onOpenField) }
                item { ProfileSection(uiState = uiState, open = onOpenField) }
                item { InterestsSection(uiState = uiState, open = onOpenField) }
              }
        }
      }

  if (uiState.showModal) {
    ModalBottomSheet(
        onDismissRequest = onCloseModal, sheetState = rememberModalBottomSheetState()) {
          ModalContent(
              uiState = uiState,
              onUpdateTemp = onUpdateTemp,
              onToggleCountryDropdown = onToggleCountryDropdown,
              onAddTag = onAddTag,
              onRemoveTag = onRemoveTag,
              onClose = onCloseModal,
              onSave = onSaveModal)
        }
  }

  // This is the loading icon which will appear during the signing out
  if (uiState.isLoading) {
    Box(
        Modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag(SettingsTestTags.LOADING_ICON),
        contentAlignment = Alignment.Center) {
          CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
  }
}

/* =========================================================
 * Sections
 * ========================================================= */
/** Section for displaying general account information like email and password. */
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

/** Section for displaying personal profile information. */
@Composable
private fun ProfileSection(uiState: SettingsUiState, open: (String) -> Unit) {
  Column(verticalArrangement = Arrangement.spacedBy(SettingsScreenPaddings.InternalSpacing)) {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        thickness = Dimensions.DividerThickness,
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
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.error,
          modifier = Modifier.padding(start = SettingsScreenPaddings.ErrorIndent))
    }
    uiState.monthError?.let {
      Text(
          it,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.error,
          modifier = Modifier.padding(start = SettingsScreenPaddings.ErrorIndent))
    }
    uiState.yearError?.let {
      Text(
          it,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.error,
          modifier = Modifier.padding(start = SettingsScreenPaddings.ErrorIndent))
    }
  }
}

/** Section for displaying and editing tag-based interests. */
@Composable
private fun InterestsSection(uiState: SettingsUiState, open: (String) -> Unit) {
  Column(verticalArrangement = Arrangement.spacedBy(SettingsScreenPaddings.InternalSpacing)) {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        thickness = Dimensions.DividerThickness,
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
 * Previews (use stateless content only)
 * ========================================================= */
fun sampleSettingsState(showModal: Boolean = false, field: String = "") =
    SettingsUiState(
        email = "preview@epfl.ch",
        firstName = "Emma",
        lastName = "Prolapse",
        country = "Switzerland",
        description = "Loves Kotlin, skiing, and fondue. Building UniVERSE app.",
        day = "05",
        month = "01",
        year = "2000",
        // Use Tag enums directly
        selectedTags = listOf(Tag.HIKING, Tag.CYCLING, Tag.CLASSICAL, Tag.KARATE, Tag.METAL),
        showModal = showModal,
        currentField = field)

@Preview(showBackground = true, name = "Settings")
@Composable
private fun SettingsScreenContent_Preview() {
  UniverseTheme {
    SettingsScreenContent(uiState = sampleSettingsState(), onOpenField = {}, onBack = {})
  }
}
