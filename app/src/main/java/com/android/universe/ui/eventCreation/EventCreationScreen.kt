package com.android.universe.ui.eventCreation

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTimeFilled
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.universe.model.ai.gemini.EventProposal
import com.android.universe.ui.common.InputLimits
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
  const val AI_ASSIST_BUTTON = "AiAssistButton"
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
 * Screen for the Event creation.
 *
 * Allows the user to create a new event by uploading an image, setting a title and description,
 * selecting a location, and picking a date. The UI updates the ViewModel state as the user
 * interacts with the fields and validates the input before allowing the event to be saved.
 *
 * @param eventCreationViewModel the viewModel that holds the state and logic for event creation.
 * @param onSelectLocation callback triggered when the user clicks the location button to set the
 *   event's location.
 * @param onSave callback triggered when the user successfully saves the event.
 * @param onBack callback triggered when the user clicks the back button.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EventCreationScreen(
    eventCreationViewModel: EventCreationViewModel =
        viewModel(factory = EventCreationViewModel.provideFactory(LocalContext.current)),
    onSelectLocation: () -> Unit,
    onSave: () -> Unit = {},
    onBack: () -> Unit = {}
) {
  val uiState = eventCreationViewModel.uiStateEventCreation.collectAsState()

  var aiPrompt by remember { mutableStateOf("") }

  if (uiState.value.isAiAssistVisible) {
    if (uiState.value.proposal == null) {
      AiPromptBox(
          prompt = aiPrompt,
          isGenerating = uiState.value.isGenerating,
          error = uiState.value.generationError,
          onPromptChange = { aiPrompt = it },
          onGenerate = { eventCreationViewModel.generateProposal(aiPrompt) },
          onBack = { eventCreationViewModel.hideAiAssist() })
    } else {
      AiReviewBox(
          proposal = uiState.value.proposal!!,
          prompt = aiPrompt,
          isGenerating = uiState.value.isGenerating,
          onPromptChange = { aiPrompt = it },
          onRegenerate = { eventCreationViewModel.generateProposal(aiPrompt) },
          onConfirm = { eventCreationViewModel.acceptProposal() },
          onBack = { eventCreationViewModel.hideAiAssist() })
    }
  } else {
    StandardEventCreationForm(
        uiState = uiState.value,
        eventCreationViewModel = eventCreationViewModel,
        onSelectLocation = onSelectLocation,
        onSave = onSave,
        onBack = onBack)
  }
}

@Composable
fun StandardEventCreationForm(
    uiState: EventCreationUIState,
    eventCreationViewModel: EventCreationViewModel,
    onSelectLocation: () -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
  val eventImage = uiState.eventPicture
  val dateText = if (uiState.date == null) "" else eventCreationViewModel.formatDate(uiState.date)
  val showDate = remember { mutableStateOf(false) }

  val flowTabBack = FlowTab.Back(onClick = { onBack() })
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
                val launcher =
                    rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
                          uri?.let { selectedUri -> eventCreationViewModel.setImage(selectedUri) }
                        }
                LiquidImagePicker(
                    imageBytes = eventImage,
                    onPickImage = { launcher.launch("image/*") },
                    onDeleteImage = { eventCreationViewModel.deleteImage() },
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
                                onClick = { eventCreationViewModel.showAiAssist() },
                                height = EventCreationDefaults.SET_LOCATION_BUTTON_HEIGHT,
                                width = EventCreationDefaults.SET_LOCATION_BUTTON_WIDTH,
                                contentPadding = Dimensions.PaddingSmall,
                                modifier =
                                    Modifier.padding(end = 8.dp)
                                        .testTag(EventCreationTestTags.AI_ASSIST_BUTTON)) {
                                  Icon(
                                      imageVector = Icons.Default.AutoAwesome,
                                      contentDescription = "AI Assist",
                                      modifier = Modifier.size(EventCreationDefaults.locIconSize))
                                }

                            LiquidButton(
                                onClick = { onSelectLocation() },
                                height = EventCreationDefaults.SET_LOCATION_BUTTON_HEIGHT,
                                width = EventCreationDefaults.SET_LOCATION_BUTTON_WIDTH,
                                contentPadding = Dimensions.PaddingSmall,
                                modifier =
                                    Modifier.testTag(EventCreationTestTags.SET_LOCATION_BUTTON)) {
                                  Icon(
                                      imageVector = Icons.Default.AddLocationAlt,
                                      contentDescription = "Set location",
                                      tint = MaterialTheme.colorScheme.onBackground,
                                      modifier = Modifier.size(EventCreationDefaults.locIconSize))
                                }
                          }

                      Spacer(modifier = Modifier.height(Dimensions.PaddingMedium))
                      CustomTextField(
                          modifier =
                              Modifier.testTag(EventCreationTestTags.EVENT_TITLE_TEXT_FIELD)
                                  .padding(vertical = Dimensions.PaddingMedium),
                          label = "Title",
                          placeholder = "Enter your event title",
                          value = uiState.name,
                          onValueChange = { name ->
                            eventCreationViewModel.setEventName(name)
                            eventCreationViewModel.setOnboardingState(
                                state = OnboardingState.ENTER_EVENT_TITLE, true)
                          },
                          maxLines = 2,
                          leadingIcon = Icons.Default.Title,
                          validationState =
                              if (uiState.onboardingState[OnboardingState.ENTER_EVENT_TITLE] ==
                                  true) {
                                uiState.eventTitleValid
                              } else {
                                ValidationState.Neutral
                              })
                      CustomTextField(
                          modifier =
                              Modifier.testTag(EventCreationTestTags.EVENT_DESCRIPTION_TEXT_FIELD)
                                  .padding(vertical = Dimensions.PaddingMedium),
                          label = "Description",
                          placeholder = "Enter your event description",
                          value = uiState.description ?: "",
                          onValueChange = { description ->
                            eventCreationViewModel.setEventDescription(description)
                            eventCreationViewModel.setOnboardingState(
                                state = OnboardingState.ENTER_DESCRIPTION, true)
                          },
                          maxLines = 3,
                          validationState =
                              if (uiState.onboardingState[OnboardingState.ENTER_DESCRIPTION] ==
                                  true) {
                                uiState.eventDescriptionValid
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
                                    if (uiState.eventDateValid == ValidationState.Valid) {
                                      uiState.eventDateTimeValid
                                    } else {
                                      uiState.eventDateValid
                                    })
                          }
                      UniversalDatePickerDialog(
                          modifier = Modifier.testTag(EventCreationTestTags.EVENT_DATE_PICKER),
                          visible = showDate.value,
                          initialDate = uiState.date ?: LocalDate.now(),
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
                          value = uiState.time,
                          onValueChange = { time ->
                            eventCreationViewModel.setTime(time)
                            eventCreationViewModel.setOnboardingState(
                                OnboardingState.ENTER_TIME, true)
                          },
                          maxLines = 1,
                          leadingIcon = Icons.Default.AccessTimeFilled,
                          validationState =
                              if (uiState.onboardingState[OnboardingState.ENTER_TIME] == true) {
                                if (uiState.eventTimeValid == ValidationState.Valid) {
                                  uiState.eventDateTimeValid
                                } else {
                                  uiState.eventTimeValid
                                }
                              } else {
                                ValidationState.Neutral
                              })
                    }
              }
        }
      })
}

@Composable
fun AiLayout(bottomBar: @Composable () -> Unit, content: @Composable () -> Unit) {
  Scaffold(containerColor = Color.Transparent, bottomBar = { bottomBar() }) { paddingValues ->
    Column(modifier = Modifier.padding(top = paddingValues.calculateTopPadding())) {
      Spacer(modifier = Modifier.height(EventCreationDefaults.eventPictureBoxHeight).fillMaxWidth())

      Spacer(modifier = Modifier.height(Dimensions.PaddingLarge))

      LiquidBox(
          modifier = Modifier.weight(1f).fillMaxWidth(),
          shape = RoundedCornerShape(EventCreationDefaults.eventBoxCornerRadius)) {
            Column(
                modifier = Modifier.fillMaxSize().padding(Dimensions.PaddingExtraLarge),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally) {
                  content()
                }
          }
    }
  }
}

@Composable
fun AiPromptBox(
    prompt: String,
    isGenerating: Boolean,
    error: String?,
    onPromptChange: (String) -> Unit,
    onGenerate: () -> Unit,
    onBack: () -> Unit
) {
  var validationError by remember { mutableStateOf<String?>(null) }

  val currentError = validationError ?: error
  val promptValidationState =
      if (currentError != null) {
        ValidationState.Invalid(currentError)
      } else {
        ValidationState.Neutral
      }

  AiLayout(
      bottomBar = {
        FlowBottomMenu(
            flowTabs =
                listOf(
                    FlowTab.Back(onClick = onBack),
                    FlowTab.Generate(
                        onClick = {
                          if (prompt.isNotBlank()) {
                            onGenerate()
                          }
                        },
                        enabled = !isGenerating)))
      }) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
          Icon(Icons.Default.AutoAwesome, contentDescription = null)
          Spacer(modifier = Modifier.width(Dimensions.PaddingMedium))
          Text(text = "AI Assist", style = MaterialTheme.typography.titleLarge)
        }

        Spacer(modifier = Modifier.height(Dimensions.SpacerLarge))

        CustomTextField(
            label = "Describe your event",
            placeholder = "Share what you want participants to experience…",
            value = prompt,
            onValueChange = { newValue ->
              validationError =
                  if (newValue.isBlank()) {
                    "Prompt cannot be empty"
                  } else {
                    null
                  }
              onPromptChange(newValue)
            },
            maxLines = 4,
            validationState = promptValidationState)

        if (isGenerating) {
          Spacer(modifier = Modifier.height(Dimensions.PaddingLarge))
          LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
      }
}

@Composable
fun AiReviewBox(
    proposal: EventProposal,
    prompt: String,
    isGenerating: Boolean,
    onPromptChange: (String) -> Unit,
    onRegenerate: () -> Unit,
    onConfirm: () -> Unit,
    onBack: () -> Unit
) {
  val focusManager = LocalFocusManager.current

  var promptValidationError by remember { mutableStateOf<String?>(null) }
  val promptValidationState =
      if (promptValidationError != null) {
        ValidationState.Invalid(promptValidationError!!)
      } else {
        ValidationState.Neutral
      }

  val isTitleValid = proposal.title.length <= InputLimits.TITLE_EVENT_MAX_LENGTH
  val isDescriptionValid = proposal.description.length <= InputLimits.DESCRIPTION
  val hasProposalError = !isTitleValid || !isDescriptionValid

  AiLayout(
      bottomBar = {
        FlowBottomMenu(
            flowTabs =
                listOf(
                    FlowTab.Back(onClick = onBack),
                    FlowTab.Regenerate(
                        onClick = {
                          focusManager.clearFocus()
                          if (prompt.isNotBlank()) {
                            onRegenerate()
                          }
                        },
                        enabled = prompt.isNotBlank() && !isGenerating),
                    FlowTab.Confirm(
                        onClick = onConfirm, enabled = !isGenerating && !hasProposalError)))
      }) {
        Row(modifier = Modifier.fillMaxWidth()) {
          Text(text = "Review Proposal", style = MaterialTheme.typography.titleLarge)
        }

        Spacer(modifier = Modifier.height(Dimensions.PaddingMedium))

        CustomTextField(
            label = "Your Prompt",
            placeholder = "",
            value = prompt,
            onValueChange = { newValue ->
              promptValidationError = if (newValue.isBlank()) "Prompt cannot be empty" else null
              onPromptChange(newValue)
            },
            maxLines = 2,
            validationState = promptValidationState)

        Spacer(modifier = Modifier.height(Dimensions.PaddingMedium))

        CustomTextField(
            label = "Title",
            placeholder = "",
            value = proposal.title,
            onValueChange = {},
            enabled = false,
            leadingIcon = Icons.Default.Title,
            validationState =
                if (isTitleValid) ValidationState.Neutral
                else ValidationState.Invalid("Title too long (${proposal.title.length}/50)"))

        Spacer(modifier = Modifier.height(Dimensions.PaddingMedium))

        CustomTextField(
            label = "Description",
            placeholder = "",
            value = proposal.description,
            onValueChange = {},
            enabled = false,
            leadingIcon = Icons.Default.Description,
            maxLines = 3,
            validationState =
                if (isDescriptionValid) ValidationState.Neutral
                else
                    ValidationState.Invalid(
                        "Description too long (${proposal.description.length}/100)"))

        if (hasProposalError) {
          Spacer(modifier = Modifier.height(Dimensions.PaddingMedium))
          Text(
              text =
                  "Sorry, the AI generated content that is too long. Please reduce or regenerate.",
              color = MaterialTheme.colorScheme.error,
              style = MaterialTheme.typography.labelSmall)
        }

        if (isGenerating) {
          Spacer(modifier = Modifier.height(Dimensions.PaddingMedium))
          LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
      }
}
