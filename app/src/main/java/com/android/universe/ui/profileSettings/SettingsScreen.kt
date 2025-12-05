package com.android.universe.ui.profileSettings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.universe.ui.common.InputLimits
import com.android.universe.ui.common.LogoutConfirmationDialog
import com.android.universe.ui.common.UniversalDatePickerDialog
import com.android.universe.ui.common.ValidationState
import com.android.universe.ui.components.CustomTextField
import com.android.universe.ui.components.LiquidBottomSheet
import com.android.universe.ui.components.LiquidBox
import com.android.universe.ui.components.LiquidImagePicker
import com.android.universe.ui.navigation.FlowBottomMenu
import com.android.universe.ui.navigation.FlowTab
import com.android.universe.ui.navigation.NavigationTestTags
import com.android.universe.ui.theme.Dimensions
import java.time.LocalDate
import java.time.ZoneId

object SettingsScreenStyles {
  @Composable fun sectionTitleStyle() = MaterialTheme.typography.titleLarge
}

object FieldTitles {
  const val MAIL = "Email address"
  const val PASSWORD = "Password"
  const val USERNAME = "Username"
  const val FIRSTNAME = "First Name"
  const val LASTNAME = "Last Name"
  const val DESCRIPTION = "Bio"
  const val DATE = "Date of Birth"
  const val LOCATION = "Location"
  const val TAG = "Tag"
  const val AUTHENTICATION = "Authentication"
  const val PROFILE = "Profile"
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
 * @param onAddTag to go to the select tag screen
 * @param clear to clear the credential state
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uid: String,
    onBack: () -> Unit = {},
    onConfirm: () -> Unit = {},
    viewModel: SettingsViewModel =
        viewModel(factory = SettingsViewModel.provideFactory(LocalContext.current, uid)),
    onLogout: () -> Unit = {},
    onAddTag: () -> Unit = {},
    clear: suspend () -> Unit = {}
) {
  val uiState by viewModel.uiState.collectAsState()
  val tagState by viewModel.userTags.collectAsState()
  val time = LocalDate.now(ZoneId.of("Europe/Berlin"))
  val bottomSheetSize = 300.dp
  val launcher =
      rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
        viewModel.setProfilePicture(uri)
      }
  val showDate = remember { mutableStateOf(false) }

  val showDialog = remember { mutableStateOf(false) }
  LogoutConfirmationDialog(
      showDialog = showDialog.value,
      onConfirm = {
        showDialog.value = false
        viewModel.signOut(clear, onLogout)
      },
      onDismiss = { showDialog.value = false })
  Box(modifier = Modifier.fillMaxSize().testTag(NavigationTestTags.SETTINGS_SCREEN)) {
    Column(
        modifier = Modifier.padding(top = Dimensions.PaddingExtraLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimensions.PaddingExtraLarge)) {
          Spacer(modifier = Modifier.height(Dimensions.PaddingLarge))
          LiquidImagePicker(
              uiState.profilePicture,
              onPickImage = { launcher.launch("image/*") },
              modifier =
                  Modifier.testTag(SettingsTestTags.PICTURE)
                      .width(Dimensions.LiquidImagePickerWidth)
                      .height(Dimensions.LiquidImagePickerHeight),
              onDeleteImage = { viewModel.deleteImage() })
          LiquidBox(
              modifier = Modifier.fillMaxSize(),
              shape = BottomSheetDefaults.ExpandedShape,
              enableLens = false) {
                Column(
                    modifier =
                        Modifier.testTag(SettingsTestTags.LIQUID_BOX_CONTENT)
                            .fillMaxSize()
                            .verticalScroll(state = rememberScrollState())
                            .padding(
                                horizontal = Dimensions.PaddingExtraLarge,
                                vertical = Dimensions.PaddingLarge),
                    verticalArrangement = Arrangement.spacedBy(Dimensions.PaddingLarge)) {
                      Text(
                          FieldTitles.AUTHENTICATION,
                          style = SettingsScreenStyles.sectionTitleStyle())
                      FieldModifier(
                          modifier = Modifier.testTag(SettingsTestTags.EMAIL_TEXT),
                          editModifier = Modifier.testTag(SettingsTestTags.EMAIL_BUTTON),
                          leadingIcon = Icons.Default.Mail,
                          title = FieldTitles.MAIL,
                          endText = uiState.email,
                          trailingIcon = Icons.Default.Edit,
                          onClick = { viewModel.setModalType(ModalType.EMAIL) })
                      if (uiState.passwordEnabled == true) {
                        FieldModifier(
                            modifier = Modifier.testTag(SettingsTestTags.PASSWORD_TEXT),
                            editModifier = Modifier.testTag(SettingsTestTags.PASSWORD_BUTTON),
                            leadingIcon = Icons.Default.Lock, // TODO test on real app
                            title = FieldTitles.PASSWORD,
                            endText = "********",
                            trailingIcon = Icons.Default.Edit,
                            onClick = { viewModel.setModalType(ModalType.PASSWORD) })
                      }
                      Text(FieldTitles.PROFILE, style = SettingsScreenStyles.sectionTitleStyle())
                      FieldModifier(
                          modifier = Modifier.testTag(SettingsTestTags.USERNAME_TEXT),
                          editModifier = Modifier.testTag(SettingsTestTags.USERNAME_BUTTON),
                          leadingIcon = Icons.Default.AccountCircle,
                          title = FieldTitles.USERNAME,
                          endText = uiState.username,
                          trailingIcon = Icons.Default.Edit,
                          onClick = { viewModel.setModalType(ModalType.USERNAME) })
                      FieldModifier(
                          modifier = Modifier.testTag(SettingsTestTags.FIRST_NAME_TEXT),
                          editModifier = Modifier.testTag(SettingsTestTags.FIRST_NAME_BUTTON),
                          leadingIcon = Icons.Default.AccountCircle,
                          title = FieldTitles.FIRSTNAME,
                          endText = uiState.firstName,
                          trailingIcon = Icons.Default.Edit,
                          onClick = { viewModel.setModalType(ModalType.FIRSTNAME) })
                      FieldModifier(
                          modifier = Modifier.testTag(SettingsTestTags.LAST_NAME_TEXT),
                          editModifier = Modifier.testTag(SettingsTestTags.LAST_NAME_BUTTON),
                          leadingIcon = Icons.Default.AccountCircle,
                          title = FieldTitles.LASTNAME,
                          endText = uiState.lastName,
                          trailingIcon = Icons.Default.Edit,
                          onClick = { viewModel.setModalType(ModalType.LASTNAME) })
                      FieldModifier(
                          modifier = Modifier.testTag(SettingsTestTags.DESCRIPTION_TEXT),
                          editModifier = Modifier.testTag(SettingsTestTags.DESCRIPTION_BUTTON),
                          leadingIcon = Icons.Default.Description,
                          title = FieldTitles.DESCRIPTION,
                          endText = uiState.description,
                          trailingIcon = Icons.Default.Edit,
                          onClick = { viewModel.setModalType(ModalType.DESCRIPTION) })
                      Column(
                          modifier = Modifier.fillMaxWidth(),
                          verticalArrangement = Arrangement.spacedBy(Dimensions.PaddingSmall)) {
                            FieldModifier(
                                modifier = Modifier.testTag(SettingsTestTags.DATE_TEXT),
                                editModifier = Modifier.testTag(SettingsTestTags.DATE_BUTTON),
                                leadingIcon = Icons.Default.CalendarMonth,
                                title = FieldTitles.DATE,
                                endText = uiState.formattedDate ?: "Unavailable",
                                trailingIcon = Icons.Default.Edit,
                                onClick = { showDate.value = true })
                            if (uiState.dateValidation is ValidationState.Invalid) {
                              Text(
                                  modifier = Modifier.testTag(SettingsTestTags.DATE_ERROR),
                                  text =
                                      (uiState.dateValidation as ValidationState.Invalid)
                                          .errorMessage,
                                  color = MaterialTheme.colorScheme.error)
                            }
                          }
                      FieldModifier(
                          leadingIcon = Icons.Default.LocationOn,
                          title = FieldTitles.LOCATION,
                          endText = "TBD",
                          trailingIcon = Icons.Default.Edit) // TODO location
                      FieldModifier(
                          modifier = Modifier.testTag(SettingsTestTags.TAG_TEXT),
                          editModifier = Modifier.testTag(SettingsTestTags.TAG_BUTTON),
                          leadingIcon = Icons.Default.LightMode,
                          title = FieldTitles.TAG,
                          endText = tagState.take(4).joinToString { t -> t.displayName },
                          trailingIcon = Icons.Default.Edit,
                          onClick = { onAddTag() })
                      // Padding to cover for the bottom bar and prevent overlap issues
                      Spacer(modifier = Modifier.height(Dimensions.PaddingExtraLarge))
                      Spacer(modifier = Modifier.height(Dimensions.PaddingExtraLarge))
                    }
              }
        }
    LiquidBottomSheet(
        isPresented = uiState.showModal, onDismissRequest = { viewModel.stopModal() }) {
          Column(
              modifier =
                  Modifier.height(bottomSheetSize)
                      .padding(
                          horizontal = Dimensions.PaddingLarge, vertical = Dimensions.PaddingMedium)
                      .testTag(SettingsTestTags.MODAL_POPUP)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                      Text(
                          text = uiState.modalType!!.fieldName,
                          fontSize = 32.sp,
                          modifier = Modifier.testTag(SettingsTestTags.MODAL_TITLE))
                      Row {
                        Text(
                            text = "Cancel",
                            modifier =
                                Modifier.clickable(onClick = { viewModel.stopModal() })
                                    .testTag(SettingsTestTags.MODAL_CANCEL_BUTTON))
                        Spacer(modifier = Modifier.width(Dimensions.PaddingMedium))
                        val enabled =
                            uiState.modalValState == ValidationState.Valid ||
                                uiState.modalValState == ValidationState.Neutral
                        Text(
                            text = "Save",
                            modifier =
                                Modifier.clickable(
                                        enabled = enabled, onClick = { viewModel.saveTempModal() })
                                    .testTag(SettingsTestTags.MODAL_SAVE_BUTTON))
                      }
                    }
                Spacer(modifier = Modifier.height(Dimensions.PaddingExtraLarge))
                CustomTextField(
                    modifier = Modifier.testTag(SettingsTestTags.CUSTOMFIELD),
                    label = "",
                    isPassword = uiState.modalType == ModalType.PASSWORD,
                    placeholder = uiState.modalType!!.fieldName,
                    onValueChange = { str -> viewModel.setModalText(str) },
                    value = uiState.modalText!!,
                    validationState = uiState.modalValState)
                Spacer(modifier = Modifier.height(Dimensions.PaddingExtraLarge))
              }
        }
    UniversalDatePickerDialog(
        modifier = Modifier.testTag(SettingsTestTags.DATE_DIALOG),
        visible = showDate.value,
        initialDate = uiState.date ?: time.minusYears(InputLimits.MIN_AGE.toLong()),
        yearRange = IntRange(time.year - 100, time.minusYears(InputLimits.MIN_AGE.toLong()).year),
        onDismiss = { showDate.value = false },
        onConfirm = {
          viewModel.setDate(it)
          showDate.value = false
        })
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
      FlowBottomMenu(
          listOf(
              FlowTab.Back(onClick = onBack),
              FlowTab.Confirm(
                  onClick = { viewModel.saveProfile(uid, onConfirm) },
                  enabled =
                      uiState.dateValidation == ValidationState.Valid ||
                          uiState.dateValidation == ValidationState.Neutral),
              FlowTab.Logout(onClick = { showDialog.value = true })))
    }
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
}

/**
 * A Row of trailing icon with its text and at the end the value text and its icon. The value text
 * is ... if too large. Adaptive layout based on screen width.
 *
 * @param modifier Modifier to be applied to the changing text.
 * @param editModifier Modifier to be applied to the edit button.
 * @param leadingIcon Icon to be displayed at the start of the row.
 * @param title Text to be displayed at the start of the row.
 * @param endText Text to be displayed at the end of the row.
 * @param trailingIcon Icon to be displayed at the end of the row
 * @param onClick Callback to be invoked when the trailing icon is clicked.
 */
@Composable
fun FieldModifier(
    modifier: Modifier = Modifier,
    editModifier: Modifier = Modifier,
    leadingIcon: ImageVector?,
    title: String,
    endText: String,
    trailingIcon: ImageVector,
    onClick: () -> Unit = {}
) {
  Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      if (leadingIcon != null) {
        Icon(imageVector = leadingIcon, contentDescription = title)
        Spacer(modifier = Modifier.width(Dimensions.PaddingSmall))
      }
      Text(text = title)
    }
    Spacer(modifier = Modifier.weight(1f))
    Text(
        text = endText,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier.weight(1f, fill = true),
        textAlign = TextAlign.End)
    Spacer(modifier = Modifier.weight(0.01f))
    Icon(
        imageVector = trailingIcon,
        contentDescription = "Edit $title",
        modifier = editModifier.clickable(onClick = onClick))
  }
}
