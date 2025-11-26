package com.android.universe.ui.eventCreation

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.universe.di.DefaultDP
import com.android.universe.model.location.Location
import com.android.universe.ui.common.UniversalDatePickerDialog
import com.android.universe.ui.common.UniversalTimePickerDialog
import com.android.universe.ui.theme.Dimensions
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.withContext

/** All the tags that are used to test the EventCreation screen. */
object EventCreationTestTags {
  const val EVENT_TITLE_TEXT_FIELD = "EventTitleTextField"
  const val EVENT_DESCRIPTION_TEXT_FIELD = "EventDescriptionTextField"
  const val EVENT_DAY_TEXT_FIELD = "EventDayTextField"
  const val EVENT_MONTH_TEXT_FIELD = "EventMonthTextField"
  const val EVENT_YEAR_TEXT_FIELD = "EventYearTextField"
  const val EVENT_HOUR_TEXT_FIELD = "EventHourTextField"
  const val EVENT_MINUTE_TEXT_FIELD = "EventMinuteTextField"
  const val ADD_TAG_BUTTON = "AddTagButton"
  const val TAG = "tags"
  const val IMAGE_EVENT = "image_event"
  const val EDIT_IMAGE_ICON = "edit_image_icon"
  const val IMAGE_ICON = "image_icon"
  const val DELETE_IMAGE_BUTTON = "delete_image_button"
  const val SAVE_EVENT_BUTTON = "SaveEventButton"
  const val ERROR_TITLE = "ErrorTitle"
  const val ERROR_DESCRIPTION = "ErrorDescription"
  const val ERROR_DATE = "ErrorDate"

  const val DATE_BUTTON = "DateButton"
  const val TIME_BUTTON = "TimeButton"
  const val DATE_DIALOG = "DateDialog"
  const val TIME_DIALOG = "TimeDialog"
}

/**
 * returns a OutlinedTextField with all the parameters given.
 *
 * @param modifier the modifier to apply.
 * @param value the string to print on the textField.
 * @param onValueChange the function to call when the value change.
 * @param label the string to display at the top of the textField.
 * @param errorMessage the message to display when there is an error.
 * @param errorModifier the modifier for the Text error message.
 * @param singleLine the boolean true when we want to display the value is one single Line.
 */
@Composable
private fun TextFieldEventCreation(
    modifier: Modifier = Modifier,
    value: String = "",
    onValueChange: (String) -> Unit = {},
    label: String = "",
    errorMessage: String? = "",
    errorModifier: Modifier = Modifier,
    singleLine: Boolean = true
) {
  var hasBeenTouched by remember { mutableStateOf(false) }

  val isError =
      if (hasBeenTouched) {
        errorMessage != null
      } else {
        false
      }
  OutlinedTextField(
      modifier =
          modifier.onFocusChanged { focusState ->
            if (focusState.isFocused && !hasBeenTouched) {
              hasBeenTouched = true
            }
          },
      value = value,
      onValueChange = onValueChange,
      isError = isError,
      supportingText =
          if (isError) {
            { Text(errorMessage!!, modifier = errorModifier) }
          } else {
            null
          },
      label = { Text(label) },
      singleLine = singleLine)
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
 * @param location the location of the event.
 * @param onSave the callBack to call when the user click on the 'Save Event' button.
 * @param onAddTag the callBack to call when the user click on the 'Add Tag' button.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EventCreationScreen(
    eventCreationViewModel: EventCreationViewModel = viewModel(),
    location: Location,
    onSave: () -> Unit = {},
    onAddTag: () -> Unit = {}
) {
  val uiState = eventCreationViewModel.uiStateEventCreation.collectAsState()
  val tags = eventCreationViewModel.eventTags.collectAsState()
  val eventImage = uiState.value.eventPicture
  val dateText =
      if (uiState.value.date == null) eventCreationViewModel.noDateText
      else eventCreationViewModel.formatDate(uiState.value.date)
  val showDate = remember { mutableStateOf(false) }
  val timeText =
      if (uiState.value.time == null) eventCreationViewModel.noTimeText
      else eventCreationViewModel.formatTime(uiState.value.time)
  val showTime = remember { mutableStateOf(false) }
  Scaffold(
      containerColor = Color.Transparent,
      content = { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
          val context = LocalContext.current
          val bitmap =
              produceState<Bitmap?>(initialValue = null, eventImage) {
                    value =
                        if (eventImage != null) {
                          withContext(DefaultDP.io) {
                            BitmapFactory.decodeByteArray(
                                uiState.value.eventPicture, 0, eventImage.size)
                          }
                        } else {
                          null
                        }
                  }
                  .value
          // Event Image box with image selection
          Box(
              modifier =
                  Modifier.align(Alignment.CenterHorizontally)
                      .clip(RoundedCornerShape(16.dp))
                      .height(140.dp)
                      .width(220.dp)
                      .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                      .border(
                          2.dp, MaterialTheme.colorScheme.onSurface, RoundedCornerShape(16.dp))) {
                // If there is no image we display an Image Icon.
                if (bitmap == null) {
                  Icon(
                      tint = MaterialTheme.colorScheme.onSurface,
                      contentDescription = "Image",
                      imageVector = Icons.Filled.Image,
                      modifier =
                          Modifier.size(Dimensions.IconSizeLarge)
                              .align(Alignment.Center)
                              .testTag(EventCreationTestTags.IMAGE_ICON))
                } else {
                  Image(
                      bitmap = bitmap.asImageBitmap(),
                      contentDescription = "Selected image",
                      modifier =
                          Modifier.clip(RoundedCornerShape(16.dp))
                              .align(Alignment.Center)
                              .fillMaxSize()
                              .testTag(EventCreationTestTags.IMAGE_EVENT),
                      contentScale = ContentScale.Crop)
                }
                // The launcher to launch the image selection.
                val launcher =
                    rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
                          uri?.let { selectedUri ->
                            eventCreationViewModel.setImage(context, selectedUri)
                          }
                        }
                IconButton(
                    onClick = { launcher.launch("image/*") },
                    modifier =
                        Modifier.align(Alignment.BottomEnd)
                            .size(Dimensions.IconSizeLarge)
                            .testTag(EventCreationTestTags.EDIT_IMAGE_ICON)) {
                      Icon(
                          contentDescription = "Image",
                          imageVector = Icons.Filled.Create,
                      )
                    }
              }
          Button(
              onClick = { eventCreationViewModel.setImage(context = context, uri = null) },
              modifier =
                  Modifier.padding(vertical = Dimensions.PaddingMedium)
                      .width(150.dp)
                      .height(40.dp)
                      .align(Alignment.CenterHorizontally)
                      .testTag(EventCreationTestTags.DELETE_IMAGE_BUTTON)) {
                Text("Remove Image")
              }
          TextFieldEventCreation(
              modifier =
                  Modifier.testTag(EventCreationTestTags.EVENT_TITLE_TEXT_FIELD)
                      .fillMaxWidth()
                      .padding(Dimensions.PaddingLarge),
              value = uiState.value.name,
              onValueChange = { name -> eventCreationViewModel.setEventName(name) },
              label = "Event Title",
              errorMessage = uiState.value.titleError,
              errorModifier = Modifier.testTag(EventCreationTestTags.ERROR_TITLE))
          TextFieldEventCreation(
              modifier =
                  Modifier.testTag(EventCreationTestTags.EVENT_DESCRIPTION_TEXT_FIELD)
                      .fillMaxWidth()
                      .padding(Dimensions.PaddingMedium)
                      .height(120.dp),
              value = uiState.value.description ?: "",
              onValueChange = { description ->
                eventCreationViewModel.setEventDescription(description)
              },
              label = "Event Description",
              errorMessage = null,
              errorModifier = Modifier.testTag(EventCreationTestTags.ERROR_DESCRIPTION),
              singleLine = false)
          Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Spacer(modifier = Modifier.width(Dimensions.PaddingLarge))
            Text(text = dateText)
            Spacer(modifier = Modifier.width(Dimensions.PaddingLarge))
            Button(
                onClick = { showDate.value = true },
                modifier = Modifier.testTag(EventCreationTestTags.DATE_BUTTON)) {
                  Text("Pick Date")
                }
            if (uiState.value.dateError != null) {
              Spacer(modifier = Modifier.width(Dimensions.PaddingLarge))
              Text(
                  text = uiState.value.dateError!!,
                  color = MaterialTheme.colorScheme.error,
                  modifier = Modifier.testTag(EventCreationTestTags.ERROR_DATE))
            }
          }
          UniversalDatePickerDialog(
              modifier = Modifier.testTag(EventCreationTestTags.DATE_DIALOG),
              visible = showDate.value,
              initialDate = uiState.value.date ?: LocalDate.now(),
              yearRange = IntRange(2025, 2050),
              onDismiss = { showDate.value = false },
              onConfirm = {
                eventCreationViewModel.setDate(it)
                showDate.value = false
              })
          Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Spacer(modifier = Modifier.width(Dimensions.PaddingLarge))
            Text(text = timeText)
            Spacer(modifier = Modifier.width(Dimensions.PaddingLarge))
            Button(
                onClick = { showTime.value = true },
                modifier = Modifier.testTag(EventCreationTestTags.TIME_BUTTON)) {
                  Text("Pick Time")
                }
            if (uiState.value.timeError != null) {
              Spacer(modifier = Modifier.width(Dimensions.PaddingLarge))
              Text(text = uiState.value.timeError!!, color = MaterialTheme.colorScheme.error)
            }
          }
          UniversalTimePickerDialog(
              modifier = Modifier.testTag(EventCreationTestTags.TIME_DIALOG),
              visible = showTime.value,
              initialTime = uiState.value.time ?: LocalTime.of(12, 0),
              onDismiss = { showTime.value = false },
              onConfirm = { localTime ->
                eventCreationViewModel.setTime(localTime)
                showTime.value = false
              })

          Row(modifier = Modifier.padding(Dimensions.PaddingMedium)) {
            Text(
                "Selected Tags:",
                modifier =
                    Modifier.padding(
                        horizontal = Dimensions.PaddingLarge,
                        vertical = Dimensions.PaddingExtraLarge))

            Button(
                onClick = { onAddTag() },
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = Color.Gray, contentColor = Color.White),
                modifier =
                    Modifier.testTag(EventCreationTestTags.ADD_TAG_BUTTON)
                        .padding(vertical = Dimensions.PaddingLarge, horizontal = 0.dp)) {
                  Text("Add Tags")
                }
          }
          FlowRow(
              modifier =
                  Modifier.fillMaxWidth()
                      .weight(1f)
                      .verticalScroll(rememberScrollState())
                      .padding(horizontal = Dimensions.PaddingMedium),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalArrangement = Arrangement.spacedBy(4.dp)) {
                tags.value.toList().forEach { tag ->
                  Surface(
                      modifier = Modifier.testTag(EventCreationTestTags.TAG),
                      color = MaterialTheme.colorScheme.primary,
                      shape = RoundedCornerShape(50)) {
                        Text(
                            text = tag.displayName,
                            modifier =
                                Modifier.padding(
                                    horizontal = Dimensions.PaddingMedium,
                                    vertical = Dimensions.PaddingMedium))
                      }
                }
              }
          Button(
              modifier =
                  Modifier.testTag(EventCreationTestTags.SAVE_EVENT_BUTTON)
                      .fillMaxWidth()
                      .padding(16.dp),
              onClick = {
                val currentUser = FirebaseAuth.getInstance().currentUser?.uid
                if (currentUser != null) {
                  eventCreationViewModel.saveEvent(location = location, uid = currentUser)
                  onSave()
                }
              },
              enabled = eventCreationViewModel.validateAll()) {
                Text("Save Event")
              }
        }
      })
}
