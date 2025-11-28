package com.android.universe.ui.profileCreation

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.universe.model.user.FakeUserRepository
import com.android.universe.ui.common.UniversalDatePickerDialog
import com.android.universe.ui.common.ValidationState
import com.android.universe.ui.components.CustomTextField
import com.android.universe.ui.components.LiquidBox
import com.android.universe.ui.components.LiquidImagePicker
import com.android.universe.ui.navigation.FlowBottomMenu
import com.android.universe.ui.navigation.FlowTab.Back
import com.android.universe.ui.navigation.FlowTab.Confirm
import com.android.universe.ui.navigation.NavigationTestTags
import com.android.universe.ui.profileCreation.OnboardingState.*
import com.android.universe.ui.utils.LocalLayerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import java.time.LocalDate

/**
 * Defines constants for use in UI tests to identify specific composables within the
 * AddProfileScreen. This helps create robust and readable tests.
 */
object AddProfileScreenTestTags {
  // Username
  const val USERNAME_FIELD = "username_field"
  const val USERNAME_ERROR = "username_error"

  // First Name
  const val FIRST_NAME_FIELD = "first_name_field"
  const val FIRST_NAME_ERROR = "first_name_error"

  // Last Name
  const val LAST_NAME_FIELD = "last_name_field"
  const val LAST_NAME_ERROR = "last_name_error"

  // Description
  const val DESCRIPTION_FIELD = "description_field"
  const val DESCRIPTION_ERROR = "description_error"

  // Date of birth
  const val DATE_OF_BIRTH_TEXT = "date_of_birth_text"
  const val DATE_OF_BIRTH_BUTTON = "date_of_birth_button"

  // Save button
  const val SAVE_BUTTON = "save_button"

  const val IMAGE = "image"
}

private const val DATE_OF_BIRTH = "Date of Birth"

private const val FIRST_NAME = "First Name"

private const val USERNAME = "Username"

private const val MIN_AGE = 13

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProfile(
    uid: String,
    navigateOnSave: () -> Unit = {},
    onBack: () -> Unit = {},
    viewModel: AddProfileViewModel = viewModel()
) {
  var showDatePicker by remember { mutableStateOf(false) }
  val profileUIState by viewModel.uiState.collectAsState()
  val context = LocalContext.current

  val launcher =
      rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
        viewModel.setProfilePicture(context, uri)
      }
  Scaffold(
      modifier = Modifier.testTag(NavigationTestTags.ADD_PROFILE_SCREEN).fillMaxSize(),
      bottomBar = {
        FlowBottomMenu(
            listOf(
                Back(onBack),
                Confirm(
                    { viewModel.addProfile(uid = uid, onSuccess = navigateOnSave) },
                    profileUIState.canSave)))
      },
      containerColor = Color.Transparent) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
          LiquidImagePicker(
              modifier =
                  Modifier.testTag(AddProfileScreenTestTags.IMAGE)
                      .padding(top = paddingValues.calculateTopPadding())
                      .align(Alignment.TopCenter)
                      .width(200.dp)
                      .height(140.dp),
              imageBytes = profileUIState.profilePicture,
              onPickImage = { launcher.launch("image/*") })
        }
        LiquidBox(
            modifier =
                Modifier.fillMaxSize().padding(top = paddingValues.calculateTopPadding() + 210.dp),
            shape = BottomSheetDefaults.ExpandedShape) {
              Column(
                  modifier =
                      Modifier.matchParentSize()
                          .padding(paddingValues)
                          .padding(horizontal = 16.dp)
                          .padding(top = 16.dp)
                          .verticalScroll(rememberScrollState()),
                  verticalArrangement = Arrangement.SpaceEvenly) {
                    CustomTextField(
                        modifier = Modifier.testTag(AddProfileScreenTestTags.USERNAME_FIELD),
                        label = USERNAME,
                        leadingIcon = Icons.Default.AccountCircle,
                        placeholder = USERNAME,
                        value = profileUIState.username,
                        validationState =
                            if (profileUIState.onboardingState[ENTER_USERNAME]!!)
                                profileUIState.userNameValid
                            else ValidationState.Neutral,
                        onValueChange = {
                          viewModel.setUsername(it)
                          viewModel.setOnboardingState(ENTER_USERNAME, true)
                        })
                    CustomTextField(
                        modifier = Modifier.testTag(AddProfileScreenTestTags.FIRST_NAME_FIELD),
                        label = FIRST_NAME,
                        leadingIcon = Icons.Default.AccountCircle,
                        placeholder = FIRST_NAME,
                        value = profileUIState.firstName,
                        validationState =
                            if (profileUIState.onboardingState[ENTER_FIRSTNAME]!!)
                                profileUIState.firstNameValid
                            else ValidationState.Neutral,
                        onValueChange = {
                          viewModel.setFirstName(it)
                          viewModel.setOnboardingState(ENTER_FIRSTNAME, true)
                        })
                    CustomTextField(
                        modifier = Modifier.testTag(AddProfileScreenTestTags.LAST_NAME_FIELD),
                        label = "Last Name",
                        leadingIcon = Icons.Default.AccountCircle,
                        placeholder = "Last Name",
                        value = profileUIState.lastName,
                        validationState =
                            if (profileUIState.onboardingState[ENTER_LASTNAME]!!)
                                profileUIState.lastNameValid
                            else ValidationState.Neutral,
                        onValueChange = {
                          viewModel.setLastName(it)
                          viewModel.setOnboardingState(ENTER_LASTNAME, true)
                        })
                    CustomTextField(
                        modifier = Modifier.testTag(AddProfileScreenTestTags.DESCRIPTION_FIELD),
                        label = "Bio",
                        placeholder = "Bio",
                        maxLines = 3,
                        value = profileUIState.description ?: "",
                        validationState =
                            if (profileUIState.onboardingState[ENTER_DESCRIPTION]!!)
                                profileUIState.descriptionValid
                            else ValidationState.Neutral,
                        onValueChange = {
                          viewModel.setDescription(it)
                          viewModel.setOnboardingState(ENTER_DESCRIPTION, true)
                        })
                    Box(
                        modifier =
                            Modifier.testTag(AddProfileScreenTestTags.DATE_OF_BIRTH_BUTTON)
                                .clickable(
                                    interactionSource = null,
                                    indication = null,
                                    onClick = { showDatePicker = true })) {
                          CustomTextField(
                              modifier =
                                  Modifier.testTag(AddProfileScreenTestTags.DATE_OF_BIRTH_TEXT),
                              label = DATE_OF_BIRTH,
                              placeholder = DATE_OF_BIRTH,
                              leadingIcon = Icons.Default.EditCalendar,
                              value =
                                  if (profileUIState.day.isNotBlank() &&
                                      profileUIState.month.isNotBlank() &&
                                      profileUIState.year.isNotBlank())
                                      profileUIState.year +
                                          "-" +
                                          profileUIState.month +
                                          "-" +
                                          profileUIState.day
                                  else "",
                              onValueChange = {
                                viewModel.setOnboardingState(ENTER_DATE_OF_BIRTH, true)
                              },
                              validationState =
                                  if ((profileUIState.day.isNotBlank() &&
                                      profileUIState.month.isNotBlank() &&
                                      profileUIState.year.isNotBlank()))
                                      profileUIState.dateOfBirthValid
                                  else ValidationState.Neutral,
                              enabled = false)
                        }
                    UniversalDatePickerDialog(
                        visible = showDatePicker,
                        initialDate = LocalDate.now(),
                        yearRange =
                            IntRange(LocalDate.now().year - 100, LocalDate.now().year - MIN_AGE),
                        onConfirm = {
                          Log.e("AddProfile", it.toString())
                          if (it != null) {
                            viewModel.setYear(it.year.toString())
                            viewModel.setMonth(it.monthValue.toString())
                            viewModel.setDay(it.dayOfMonth.toString())
                          }
                          showDatePicker = false
                        },
                        onDismiss = { showDatePicker = false })
                  }
            }
      }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AddProfileScreenPreview() {
  // Just render the UI (no real logic, safe for preview)
  val stubBackdrop = rememberLayerBackdrop { drawRect(Color.Transparent) }

  CompositionLocalProvider(LocalLayerBackdrop provides stubBackdrop) {
    AddProfile(
        uid = "preview_user_001",
        navigateOnSave = {},
        viewModel = viewModel { AddProfileViewModel(FakeUserRepository()) })
  }
}
