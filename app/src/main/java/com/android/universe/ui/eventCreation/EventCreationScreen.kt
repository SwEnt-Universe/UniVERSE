package com.android.universe.ui.eventCreation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.universe.model.location.Location
import com.google.firebase.auth.FirebaseAuth

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
  const val SAVE_EVENT_BUTTON = "SaveEventButton"
  const val ERROR_TITLE = "ErrorTitle"
  const val ERROR_DESCRIPTION = "ErrorDescription"
  const val ERROR_DAY = "ErrorDay"
  const val ERROR_MONTH = "ErrorMonth"
  const val ERROR_YEAR = "ErrorYear"
  const val ERROR_HOUR = "ErrorHour"
  const val ERROR_MINUTE = "ErrorMinute"
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
  Scaffold(
      content = { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
          TextFieldEventCreation(
              modifier =
                  Modifier.testTag(EventCreationTestTags.EVENT_TITLE_TEXT_FIELD)
                      .fillMaxWidth()
                      .padding(16.dp),
              value = uiState.value.name,
              onValueChange = { name -> eventCreationViewModel.setEventName(name) },
              label = "Event Title",
              errorMessage = uiState.value.titleError,
              errorModifier = Modifier.testTag(EventCreationTestTags.ERROR_TITLE))
          TextFieldEventCreation(
              modifier =
                  Modifier.testTag(EventCreationTestTags.EVENT_DESCRIPTION_TEXT_FIELD)
                      .fillMaxWidth()
                      .padding(16.dp)
                      .height(120.dp),
              value = uiState.value.description ?: "",
              onValueChange = { description ->
                eventCreationViewModel.setEventDescription(description)
              },
              label = "Event Description",
              errorMessage = null,
              errorModifier = Modifier.testTag(EventCreationTestTags.ERROR_DESCRIPTION),
              singleLine = false)
          Spacer(modifier = Modifier.height(12.dp))
          Row(modifier = Modifier.padding(paddingValues)) {
            TextFieldEventCreation(
                modifier =
                    Modifier.testTag(EventCreationTestTags.EVENT_DAY_TEXT_FIELD)
                        .weight(1f)
                        .padding(16.dp),
                value = uiState.value.day,
                onValueChange = { day -> eventCreationViewModel.setEventDay(day) },
                label = "Day",
                errorMessage = uiState.value.dayError,
                errorModifier = Modifier.testTag(EventCreationTestTags.ERROR_DAY))
            TextFieldEventCreation(
                modifier =
                    Modifier.testTag(EventCreationTestTags.EVENT_MONTH_TEXT_FIELD)
                        .weight(1f)
                        .padding(16.dp),
                value = uiState.value.month,
                onValueChange = { month -> eventCreationViewModel.setEventMonth(month) },
                label = "Month",
                errorMessage = uiState.value.monthError,
                errorModifier = Modifier.testTag(EventCreationTestTags.ERROR_MONTH))
            TextFieldEventCreation(
                modifier =
                    Modifier.testTag(EventCreationTestTags.EVENT_YEAR_TEXT_FIELD)
                        .weight(1f)
                        .padding(16.dp),
                value = uiState.value.year,
                onValueChange = { year -> eventCreationViewModel.setEventYear(year) },
                label = "Year",
                errorMessage = uiState.value.yearError,
                errorModifier = Modifier.testTag(EventCreationTestTags.ERROR_YEAR))
          }
          Row(
              modifier = Modifier.fillMaxWidth().padding(paddingValues),
              horizontalArrangement = Arrangement.Center) {
                TextFieldEventCreation(
                    modifier =
                        Modifier.testTag(EventCreationTestTags.EVENT_HOUR_TEXT_FIELD).width(120.dp),
                    value = uiState.value.hour,
                    onValueChange = { hour -> eventCreationViewModel.setEventHour(hour) },
                    label = "Hour",
                    errorMessage = uiState.value.hourError,
                    errorModifier = Modifier.testTag(EventCreationTestTags.ERROR_HOUR))

                Spacer(modifier = Modifier.width(16.dp))
                TextFieldEventCreation(
                    modifier =
                        Modifier.testTag(EventCreationTestTags.EVENT_MINUTE_TEXT_FIELD)
                            .width(120.dp),
                    value = uiState.value.minute,
                    onValueChange = { minute -> eventCreationViewModel.setEventMinute(minute) },
                    label = "Minute",
                    errorMessage = uiState.value.minuteError,
                    errorModifier = Modifier.testTag(EventCreationTestTags.ERROR_MINUTE))
              }
          Row(modifier = Modifier.padding(paddingValues)) {
            Text(
                "Selected Tags:", modifier = Modifier.padding(horizontal = 16.dp, vertical = 28.dp))

            Button(
                onClick = { onAddTag() },
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = Color.Gray, contentColor = Color.White),
                modifier =
                    Modifier.testTag(EventCreationTestTags.ADD_TAG_BUTTON)
                        .padding(vertical = 16.dp, horizontal = 0.dp)) {
                  Text("Add Tags")
                }
          }
          FlowRow(
              modifier =
                  Modifier.fillMaxWidth()
                      .weight(1f)
                      .padding(horizontal = 16.dp)
                      .verticalScroll(rememberScrollState()),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalArrangement = Arrangement.spacedBy(4.dp)) {
                tags.value.toList().forEach { tag ->
                  Surface(
                      modifier = Modifier.testTag(EventCreationTestTags.TAG),
                      color = MaterialTheme.colorScheme.primary,
                      shape = RoundedCornerShape(50)) {
                        Text(
                            text = tag.displayName,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
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
