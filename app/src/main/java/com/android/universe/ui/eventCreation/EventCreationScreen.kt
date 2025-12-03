package com.android.universe.ui.eventCreation

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTimeFilled
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.universe.model.location.Location
import com.android.universe.di.DefaultDP
import com.android.universe.ui.common.UniversalDatePickerDialog
import com.android.universe.ui.common.ValidationState
import com.android.universe.ui.components.CustomTextField
import com.android.universe.ui.components.LiquidBox
import com.android.universe.ui.components.LiquidButton
import com.android.universe.ui.components.LiquidImagePicker
import com.android.universe.ui.navigation.FlowBottomMenu
import com.android.universe.ui.navigation.FlowTab
import com.android.universe.ui.theme.Dimensions
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDate

/** All the tags that are used to test the EventCreation screen. */
object EventCreationTestTags {
  const val EVENT_TITLE_TEXT_FIELD = "EventTitleTextField"
  const val EVENT_DESCRIPTION_TEXT_FIELD = "EventDescriptionTextField"
  const val EVENT_DATE_TEXT_FIELD = "EventDateTextField"
  const val EVENT_DATE_PICKER = "EventDatePicker"
  const val EVENT_TIME_TEXT_FIELD = "EventTimeTextField"
  const val EVENT_PICTURE_PICKER = "EventPicturePicker"
  const val CREATION_EVENT_TITLE = "CreationEventTitle"
	const val SET_LOCATION_BUTTON = "SetLocationButton"
}

object EventCreationDefaults {
  val eventPictureBoxHeight = 270.dp
  val eventBoxCornerRadius = 24.dp
  val titleFontSize = 32.sp
  const val SET_LOCATION_BUTTON_HEIGHT = 40f
  const val SET_LOCATION_BUTTON_WIDTH = 40f
  val locIconSize = 20.dp
}

/**
 * Screen for the Event creation
 *
 * The user can enter a name, a description, a day, a month, a year, a hour and a minute for his
 * event. The user can also click on the button 'Add tags' to add tags that correspond to his
 * events. The user can see the selectedTags in the screen. A save tag button is displayed at the
 * button to save the Event with the parameters that have been selected
 *
 * @param eventCreationViewModel the viewModel.
 * @param onSelectLocation triggers location selection flow
 * @param onSave the callBack to call when the user click on the 'Save Event' button.
 * @param onAiClick the callBack to call when the user click on the 'AI' button.
 * @param onBack the callBack to call when the user click on the back button of the bottom bar.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EventCreationScreen(
    eventCreationViewModel: EventCreationViewModel = viewModel(),
    onSelectLocation: () -> Unit,
    onSave: () -> Unit = {},
    onAiClick: () -> Unit = {},
    onBack: () -> Unit = {}
) {
  val uiState = eventCreationViewModel.uiStateEventCreation.collectAsState()
  val eventImage = uiState.value.eventPicture
  val dateText =
      if (uiState.value.date == null) "" else eventCreationViewModel.formatDate(uiState.value.date)
  val showDate = remember { mutableStateOf(false) }
  val flowTabBack = FlowTab.Back(onClick = { onBack() })

  val selectedLocation by eventCreationViewModel.location.collectAsState()

  val flowTabContinue =
      FlowTab.Confirm(
          onClick = {
            val currentUser = FirebaseAuth.getInstance().currentUser?.uid
            if (currentUser != null) {
              eventCreationViewModel.saveEvent(uid = currentUser)
              onSave()
            }
          },
          enabled = eventCreationViewModel.validateAll())
  Scaffold(
      containerColor = Color.Transparent,
      bottomBar = { FlowBottomMenu(flowTabs = listOf(flowTabBack, flowTabContinue)) },
      content = { paddingValues ->
        Column(modifier = Modifier.padding(top = paddingValues.calculateTopPadding())) {
          Box(
              modifier =
                  Modifier.height(EventCreationDefaults.eventPictureBoxHeight).fillMaxWidth()) {
                val context = LocalContext.current
                // The launcher to launch the image selection.
                val launcher =
                    rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
                          uri?.let { selectedUri ->
                            eventCreationViewModel.setImage(context, selectedUri)
                          }
                        }
                LiquidImagePicker(
                    imageBytes = eventImage,
                    onPickImage = { launcher.launch("image/*") },
                    modifier =
                        Modifier.height(Dimensions.EventPictureHeight)
                            .width(Dimensions.EventPictureWidth)
                            .align(Alignment.BottomCenter)
                            .testTag(EventCreationTestTags.EVENT_PICTURE_PICKER))
              }
          Spacer(modifier = Modifier.height(Dimensions.PaddingLarge))
          LiquidBox(
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(EventCreationDefaults.eventBoxCornerRadius)) {
                Column(
                    modifier =
                        Modifier.padding(
                            vertical = Dimensions.PaddingMedium,
                            horizontal = Dimensions.PaddingLarge)) {
                      Row(
                          modifier =
                              Modifier.fillMaxWidth()
                                  .padding(vertical = Dimensions.PaddingMedium)) {
                            Text(
                                modifier =
                                    Modifier.weight(1f)
                                        .testTag(EventCreationTestTags.CREATION_EVENT_TITLE),
                                text = "Create an Event",
                                style =
                                    MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold),
                                fontSize = EventCreationDefaults.titleFontSize)
                            LiquidButton(
                                onClick = { onSelectLocation() },
                                height = EventCreationDefaults.SET_LOCATION_BUTTON_HEIGHT,
                                width = EventCreationDefaults.SET_LOCATION_BUTTON_WIDTH,
                                contentPadding = Dimensions.PaddingSmall) {
                                  Icon(
                                      imageVector = Icons.Default.AddLocationAlt,
                                      contentDescription = "Set location",
                                      tint = MaterialTheme.colorScheme.onBackground,
                                      modifier =
                                          Modifier.size(EventCreationDefaults.locIconSize)
                                              .testTag(EventCreationTestTags.SET_LOCATION_BUTTON))
                                }
                          }
                      Spacer(modifier = Modifier.height(Dimensions.PaddingMedium))
                      CustomTextField(
                          modifier =
                              Modifier.testTag(EventCreationTestTags.EVENT_TITLE_TEXT_FIELD)
                                  .padding(vertical = Dimensions.PaddingMedium),
                          label = "Title",
                          placeholder = "Enter your event title",
                          value = uiState.value.name,
                          onValueChange = { name ->
                            eventCreationViewModel.setEventName(name)
                            eventCreationViewModel.setOnboardingState(
                                state = OnboardingState.ENTER_EVENT_TITLE, true)
                          },
                          maxLines = 2,
                          leadingIcon = Icons.Default.Title,
                          validationState =
                              if (uiState.value.onboardingState[
                                      OnboardingState.ENTER_EVENT_TITLE] == true) {
                                uiState.value.eventTitleValid
                              } else {
                                ValidationState.Neutral
                              })
                      CustomTextField(
                          modifier =
                              Modifier.testTag(EventCreationTestTags.EVENT_DESCRIPTION_TEXT_FIELD)
                                  .padding(vertical = Dimensions.PaddingMedium),
                          label = "Description",
                          placeholder = "Enter your event description",
                          value = uiState.value.description ?: "",
                          onValueChange = { description ->
                            eventCreationViewModel.setEventDescription(description)
                            eventCreationViewModel.setOnboardingState(
                                state = OnboardingState.ENTER_DESCRIPTION, true)
                          },
                          maxLines = 3,
                          validationState =
                              if (uiState.value.onboardingState[
                                      OnboardingState.ENTER_DESCRIPTION] == true) {
                                uiState.value.eventDescriptionValid
                              } else {
                                ValidationState.Neutral
                              })
                      Box(
                          modifier =
                              Modifier.fillMaxWidth()
                                  .padding(vertical = Dimensions.PaddingMedium)
                                  .clickable { showDate.value = true }) {
                            CustomTextField(
                                modifier =
                                    Modifier.testTag(EventCreationTestTags.EVENT_DATE_TEXT_FIELD)
                                        .align(Alignment.CenterStart),
                                label = "Date",
                                placeholder = "Enter a Date",
                                value = dateText,
                                onValueChange = {},
                                maxLines = 1,
                                leadingIcon = Icons.Default.Event,
                                enabled = false,
                                validationState =
                                    if (uiState.value.eventDateValid == ValidationState.Valid) {
                                      uiState.value.eventDateTimeValid
                                    } else {
                                      uiState.value.eventDateValid
                                    })
                          }
                      UniversalDatePickerDialog(
                          modifier = Modifier.testTag(EventCreationTestTags.EVENT_DATE_PICKER),
                          visible = showDate.value,
                          initialDate = uiState.value.date ?: LocalDate.now(),
                          yearRange = IntRange(2025, 2050),
                          onDismiss = { showDate.value = false },
                          onConfirm = {
                            eventCreationViewModel.setDate(it)
                            showDate.value = false
                          })
                      CustomTextField(
                          modifier = Modifier.testTag(EventCreationTestTags.EVENT_TIME_TEXT_FIELD),
                          label = "Time",
                          placeholder = "Select a Time in format HH:MM",
                          value = uiState.value.time,
                          onValueChange = { time ->
                            eventCreationViewModel.setTime(time)
                            eventCreationViewModel.setOnboardingState(
                                OnboardingState.ENTER_TIME, true)
                          },
                          maxLines = 1,
                          leadingIcon = Icons.Default.AccessTimeFilled,
                          validationState =
                              if (uiState.value.onboardingState[OnboardingState.ENTER_TIME] ==
                                  true) {
                                if (uiState.value.eventTimeValid == ValidationState.Valid) {
                                  uiState.value.eventDateTimeValid
                                } else {
                                  uiState.value.eventTimeValid
                                }
                              } else {
                                ValidationState.Neutral
                              })
                    }
              }
        }
      })
}
