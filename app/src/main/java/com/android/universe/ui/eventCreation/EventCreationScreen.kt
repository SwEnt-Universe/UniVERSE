package com.android.universe.ui.eventCreation

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTimeFilled
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.universe.R
import com.android.universe.model.ai.gemini.EventProposal
import com.android.universe.model.location.Location
import com.android.universe.ui.common.UniversalDatePickerDialog
import com.android.universe.ui.common.ValidationState
import com.android.universe.ui.components.CustomTextField
import com.android.universe.ui.components.LiquidBox
import com.android.universe.ui.components.LiquidButton
import com.android.universe.ui.components.LiquidImagePicker
import com.android.universe.ui.components.LiquidToggle
import com.android.universe.ui.components.ScreenLayout
import com.android.universe.ui.eventCreation.EventCreationViewModel.Companion.AiErrors
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
  const val AI_ASSIST_BUTTON = "AiAssistButton"
  const val AI_PROMPT_TEXT_FIELD = "AiPromptTextField"
  const val AI_REVIEW_TITLE_FIELD = "AiReviewTitleField"
  const val AI_REVIEW_DESCRIPTION_FIELD = "AiReviewDescriptionField"
  const val PRIVACY_TOGGLE = "PrivacyToggle"
  const val PRIVACY_SWITCH = "PrivacySwitch"
  const val SET_LOCATION_BUTTON = "SetLocationButton"
}

object EventCreationDefaults {
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
 * selecting a location, and picking a date. It also provides an AI Assistant flow to generate event
 * details automatically.
 *
 * @param uidEvent the unique identifier of the event being edited, or null for a new event.
 * @param eventCreationViewModel the viewModel that holds the state and logic for event creation.
 * @param location the location of the event.
 * @param onSave callback triggered when the user successfully saves the event.
 * @param onSaveEdition callback triggered when the user successfully edits the event.
 * @param onBack callback triggered when the user clicks the back button.
 * @param onSelectLocation callback triggered when the user clicks the location button to set the
 *   event's location.
 */
@Composable
fun EventCreationScreen(
    uidEvent: String? = null,
    eventCreationViewModel: EventCreationViewModel =
        viewModel(factory = EventCreationViewModel.provideFactory(LocalContext.current)),
    location: Location,
    onSave: () -> Unit = {},
    onSaveEdition: (uid: String) -> Unit = { _ -> },
    onBack: () -> Unit = {},
    onSelectLocation: () -> Unit = {}
) {
  val uiState = eventCreationViewModel.uiStateEventCreation.collectAsState()

  if (uiState.value.isAiAssistVisible) {
    if (uiState.value.proposal == null) {
      AiPromptBox(
          prompt = uiState.value.aiPrompt,
          validationState = uiState.value.aiPromptValid,
          isGenerating = uiState.value.isGenerating,
          error = uiState.value.generationError,
          onPromptChange = eventCreationViewModel::setAiPrompt,
          onGenerate = { eventCreationViewModel.generateProposal(location) },
          onBack = eventCreationViewModel::hideAiAssist)
    } else {
      AiReviewBox(
          proposal = uiState.value.proposal!!,
          prompt = uiState.value.aiPrompt,
          promptValidationState = uiState.value.aiPromptValid,
          titleValidationState = uiState.value.aiProposalTitleValid,
          descriptionValidationState = uiState.value.aiProposalDescriptionValid,
          isProposalValid = uiState.value.isAiProposalValid,
          isGenerating = uiState.value.isGenerating,
          onPromptChange = eventCreationViewModel::setAiPrompt,
          onTitleChange = eventCreationViewModel::updateProposalTitle,
          onDescriptionChange = eventCreationViewModel::updateProposalDescription,
          onRegenerate = { eventCreationViewModel.generateProposal(location) },
          onConfirm = eventCreationViewModel::acceptProposal,
          onBack = eventCreationViewModel::hideAiAssist)
    }
  } else {
    StandardEventCreationForm(
        uidEvent = uidEvent,
        uiState = uiState.value,
        eventCreationViewModel = eventCreationViewModel,
        location = location,
        onSave = onSave,
        onSaveEdition = onSaveEdition,
        onBack = onBack,
        onSelectLocation = onSelectLocation)
  }
}

/**
 * The standard form layout for manually creating an event.
 *
 * This composable displays the main event fields: Image picker, Title, Description, Date, Time, and
 * Location. It handles user interaction by calling methods on the [eventCreationViewModel] and
 * triggers navigation via the provided callbacks.
 *
 * @param uidEvent The unique identifier of the event being edited, or null for a new event.
 * @param uiState The current UI state containing form data and validation results.
 * @param eventCreationViewModel The ViewModel to update state and trigger logic.
 * @param location the location of the event.
 * @param onSave Callback triggered when the event is successfully saved.
 * @param onSaveEdition Callback triggered when the event is successfully edited.
 * @param onBack Callback to return to the previous screen.
 * @param onSelectLocation callback triggered when the user clicks the location button to set the
 *   event's location.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StandardEventCreationForm(
    uidEvent: String? = null,
    uiState: EventCreationUIState,
    eventCreationViewModel: EventCreationViewModel,
    location: Location,
    onSave: () -> Unit,
    onSaveEdition: (uid: String) -> Unit = { _ -> },
    onBack: () -> Unit,
    onSelectLocation: () -> Unit
) {
  val eventImage = uiState.eventPicture
  val dateText = if (uiState.date == null) "" else eventCreationViewModel.formatDate(uiState.date)
  val showDate = remember { mutableStateOf(false) }
  val focusManager = LocalFocusManager.current

  val flowTabBack = FlowTab.Back(onClick = { onBack() })
  val currentUser = FirebaseAuth.getInstance().currentUser?.uid
  val flowTabContinue =
      FlowTab.Confirm(
          onClick = {
            if (currentUser != null) {
              eventCreationViewModel.saveEvent(
                  uidUser = currentUser, uidEvent = uidEvent, location = location)
              if (uidEvent == null) {
                onSave()
              } else {
                onSaveEdition(uidEvent)
              }
            }
          },
          enabled = eventCreationViewModel.validateAll())
  val flowTabDelete =
      uidEvent?.let { id ->
        FlowTab.Delete(
            onClick = {
              eventCreationViewModel.deleteEvent(id)
              onBack()
            })
      }

  LaunchedEffect(Unit) { eventCreationViewModel.init(uidEvent) }
  ScreenLayout(
      bottomBar = {
        FlowBottomMenu(
            flowTabs =
                listOfNotNull(
                    flowTabBack, flowTabDelete.takeIf { uidEvent != null }, flowTabContinue))
      },
      content = { paddingValues ->
        Column(
            modifier = Modifier.padding(top = paddingValues.calculateTopPadding()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween) {
              Box(modifier = Modifier.padding(vertical = Dimensions.PaddingLarge)) {
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
              LiquidBox(modifier = Modifier.weight(1f), shape = BottomSheetDefaults.ExpandedShape) {
                Column(
                    modifier =
                        Modifier.padding(
                                vertical = Dimensions.PaddingMedium,
                                horizontal = Dimensions.PaddingLarge)
                            .verticalScroll(rememberScrollState())) {
                      Row(
                          modifier =
                              Modifier.fillMaxWidth()
                                  .padding(vertical = Dimensions.PaddingMedium)) {
                            Text(
                                modifier =
                                    Modifier.weight(1f)
                                        .testTag(EventCreationTestTags.CREATION_EVENT_TITLE),
                                text = "Create Event",
                                color = MaterialTheme.colorScheme.onSurface,
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
                                    Modifier.padding(end = Dimensions.PaddingMedium)
                                        .testTag(EventCreationTestTags.AI_ASSIST_BUTTON)) {
                                  Icon(
                                      imageVector = Icons.Default.AutoAwesome,
                                      contentDescription = "AI Assist",
                                      tint = MaterialTheme.colorScheme.onSurface,
                                      modifier = Modifier.size(EventCreationDefaults.locIconSize))
                                }
                            if (uidEvent != null) {
                              LiquidButton(
                                  onClick = {
                                    onSelectLocation()
                                    if (currentUser != null) {
                                      eventCreationViewModel.saveEvent(
                                          uidUser = currentUser,
                                          uidEvent = uidEvent,
                                          location = location)
                                    }
                                  },
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
                          keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                          keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
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
                          placeholder = "Select time in format HH:MM",
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
                      Row(
                          modifier =
                              Modifier.fillMaxWidth()
                                  .padding(vertical = Dimensions.PaddingMedium)
                                  .testTag(EventCreationTestTags.PRIVACY_TOGGLE),
                          verticalAlignment = Alignment.CenterVertically,
                          horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                text = stringResource(R.string.event_private),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onBackground)

                            LiquidToggle(
                                modifier = Modifier.testTag(EventCreationTestTags.PRIVACY_SWITCH),
                                selected = { uiState.isPrivate },
                                onSelect = { eventCreationViewModel.setPrivacy(it) })
                          }
                      Spacer(modifier = Modifier.height(paddingValues.calculateBottomPadding()))
                    }
              }
            }
      })
}

/**
 * A shared layout wrapper for AI-related screens (Prompt and Review).
 *
 * It mirrors the structure of [StandardEventCreationForm] using a [Scaffold] and spacer system to
 * ensure the [LiquidBox] containing the content aligns perfectly with the standard form's layout.
 *
 * @param bottomBar The bottom navigation bar content.
 * @param content The internal content of the LiquidBox.
 */
@Composable
fun AiLayout(bottomBar: @Composable () -> Unit, content: @Composable () -> Unit) {
  ScreenLayout(bottomBar = { bottomBar() }) { paddingValues ->
    Column(modifier = Modifier.padding(top = paddingValues.calculateTopPadding())) {
      Spacer(modifier = Modifier.height(Dimensions.PaddingExtraLarge))
      BoxWithConstraints(Modifier.fillMaxSize()) {
        val minHeight = maxHeight * 0.4f

        LiquidBox(
            modifier =
                Modifier.fillMaxWidth()
                    .defaultMinSize(minHeight = minHeight)
                    .align(alignment = Alignment.BottomCenter),
            shape = RoundedCornerShape(EventCreationDefaults.eventBoxCornerRadius)) {
              Column(
                  modifier =
                      Modifier.fillMaxWidth()
                          .wrapContentHeight()
                          .padding(Dimensions.PaddingExtraLarge)
                          .verticalScroll(rememberScrollState()),
                  verticalArrangement = Arrangement.Top,
                  horizontalAlignment = Alignment.CenterHorizontally) {
                    content()
                    Spacer(Modifier.height(height = paddingValues.calculateBottomPadding()))
                  }
            }
      }
    }
  }
}

/**
 * The initial AI prompt screen where users input their event idea.
 *
 * @param prompt The current text input by the user.
 * @param validationState The validation state of the prompt input field.
 * @param isGenerating Whether the AI is currently generating a proposal.
 * @param error Optional error message to display if generation failed.
 * @param onPromptChange Callback when the prompt text changes.
 * @param onGenerate Callback when the user clicks the generate button.
 * @param onBack Callback when the user clicks back to return to standard form.
 */
@Composable
fun AiPromptBox(
    prompt: String,
    validationState: ValidationState,
    isGenerating: Boolean,
    error: String?,
    onPromptChange: (String) -> Unit,
    onGenerate: () -> Unit,
    onBack: () -> Unit
) {
  val finalValidationState = if (error != null) ValidationState.Invalid(error) else validationState

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
          Icon(
              Icons.Default.AutoAwesome,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurface)
          Spacer(modifier = Modifier.width(Dimensions.PaddingMedium))
          Text(
              text = stringResource(R.string.event_ai_assist),
              style = MaterialTheme.typography.titleLarge,
              color = MaterialTheme.colorScheme.onSurface)
        }

        Spacer(modifier = Modifier.height(Dimensions.SpacerLarge))

        CustomTextField(
            modifier = Modifier.testTag(EventCreationTestTags.AI_PROMPT_TEXT_FIELD),
            label = "Describe your event",
            placeholder = "State what you want to create…",
            value = prompt,
            onValueChange = onPromptChange,
            maxLines = 10,
            validationState = finalValidationState)

        if (isGenerating) {
          Spacer(modifier = Modifier.height(Dimensions.PaddingLarge))
          LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
      }
}

/**
 * The review screen for AI-generated event proposals.
 *
 * Users can view the generated title and description, modify the prompt to regenerate, or confirm
 * the proposal to populate the main form.
 *
 * @param proposal The generated [EventProposal] containing title and description.
 * @param prompt The current prompt text (allows modification for regeneration).
 * @param promptValidationState Validation state for the prompt field.
 * @param titleValidationState Validation state for the generated title (e.g. length check).
 * @param descriptionValidationState Validation state for the generated description.
 * @param isProposalValid Overall validity of the proposal.
 * @param isGenerating Whether regeneration is in progress.
 * @param onPromptChange Callback for prompt text changes.
 * @param onRegenerate Callback to trigger a new generation attempt.
 * @param onConfirm Callback to accept the proposal and fill the main form.
 * @param onBack Callback to return to the previous screen.
 */
@Composable
fun AiReviewBox(
    proposal: EventProposal,
    prompt: String,
    promptValidationState: ValidationState,
    titleValidationState: ValidationState,
    descriptionValidationState: ValidationState,
    isProposalValid: Boolean,
    isGenerating: Boolean,
    onPromptChange: (String) -> Unit,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onRegenerate: () -> Unit,
    onConfirm: () -> Unit,
    onBack: () -> Unit
) {
  val focusManager = LocalFocusManager.current

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
                        onClick = onConfirm, enabled = !isGenerating && isProposalValid)))
      }) {
        Column(modifier = Modifier.fillMaxWidth()) {
          Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.event_review_refine),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface)
          }

          Spacer(modifier = Modifier.height(Dimensions.PaddingMedium))

          CustomTextField(
              modifier = Modifier.testTag(EventCreationTestTags.AI_PROMPT_TEXT_FIELD),
              label = "Edit Your Prompt",
              placeholder = "",
              value = prompt,
              onValueChange = onPromptChange,
              maxLines = 2,
              validationState = promptValidationState)

          Spacer(modifier = Modifier.height(Dimensions.PaddingMedium))

          CustomTextField(
              modifier = Modifier.testTag(EventCreationTestTags.AI_REVIEW_TITLE_FIELD),
              label = "Title",
              placeholder = "",
              value = proposal.title,
              onValueChange = onTitleChange,
              validationState = titleValidationState)

          Spacer(modifier = Modifier.height(Dimensions.PaddingMedium))

          CustomTextField(
              modifier = Modifier.testTag(EventCreationTestTags.AI_REVIEW_DESCRIPTION_FIELD),
              label = "Description",
              placeholder = "",
              value = proposal.description,
              onValueChange = onDescriptionChange,
              maxLines = 3,
              validationState = descriptionValidationState)

          if (!isProposalValid) {
            Spacer(modifier = Modifier.height(Dimensions.PaddingMedium))
            Text(
                text = AiErrors.CONTENT_TOO_LONG_UI,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall)
          }

          if (isGenerating) {
            Spacer(modifier = Modifier.height(Dimensions.PaddingMedium))
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
          }
        }
      }
}
