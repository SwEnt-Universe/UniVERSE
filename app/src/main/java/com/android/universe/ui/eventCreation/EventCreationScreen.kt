package com.android.universe.ui.eventCreation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.universe.model.location.Location

@Composable
private fun textFieldEventCreation(
    modifier : Modifier = Modifier,
    value: String = "",
    onValueChange: (String) -> Unit = {},
    isError: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null,
    label: String = "",
    errorMessage: String = "",
    singleLine: Boolean = true
){
    OutlinedTextField(
        modifier = modifier,
        value = value,
        onValueChange = onValueChange,
        isError = isError,
        supportingText = supportingText ?: if (isError) {
            { Text(errorMessage) }
        } else {
            null
        },
        label = { Text(label) },
        singleLine = singleLine
    )
}
@Composable
fun EventCreationScreen(
    eventCreationViewModel: EventCreationViewModel = viewModel(),
    location: Location,
    onSave: () -> Unit = {},
    onAddTag: () -> Unit = {}
) {
    val uiState = eventCreationViewModel.uiStateEventCreation.collectAsState()
    val isErrorName = remember { mutableStateOf(false) }
    val isErrorDescription = remember { mutableStateOf(false) }
    val isErrorDay = remember { mutableStateOf(false) }
    val isErrorMonth = remember { mutableStateOf(false) }
    val isErrorYear = remember { mutableStateOf(false) }
    val isErrorHour = remember { mutableStateOf(false) }
    val isErrorMinute = remember { mutableStateOf(false) }

    Scaffold(
        content = {paddingValues ->
            Column(modifier = Modifier.padding(paddingValues)) {
                textFieldEventCreation(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    value = uiState.value.name,
                    onValueChange = { name ->
                        eventCreationViewModel.setEventName(name)
                        isErrorName.value = name.isEmpty()},
                    isError = isErrorName.value,
                    supportingText = if (isErrorName.value){
                        { Text("Title cannot be empty") }
                    } else {
                        null
                    },
                    label = "Event Title",
                    errorMessage = "Title cannot be empty"
                )
                textFieldEventCreation(
                    modifier = Modifier.fillMaxWidth().padding(16.dp).height(120.dp),
                    value = uiState.value.description,
                    onValueChange = { description ->
                        eventCreationViewModel.setEventDescription(description)
                        isErrorDescription.value = description.isEmpty()},
                    isError = isErrorDescription.value,
                    supportingText = if (isErrorDescription.value){
                        { Text("Description cannot be empty") }
                    } else {
                        null
                    },
                    label = "Event Description",
                    errorMessage = "Description cannot be empty",
                    singleLine = false
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row (modifier = Modifier.padding(paddingValues)){
                    textFieldEventCreation(
                        modifier = Modifier.weight(1f).padding(16.dp),
                        value = uiState.value.day,
                        onValueChange = { day ->
                            if (day.length <= 2) {
                                eventCreationViewModel.setEventDay(day)
                                isErrorDay.value = day.isEmpty() || day.toIntOrNull() == null || day.toInt() !in 1..31 || day.length < 2
                            }},
                        isError = isErrorDay.value,
                        supportingText = if (isErrorDay.value){
                            { Text("Enter a valid day format XX") }
                        } else {
                            null
                        },
                        label = "Day",
                        errorMessage = "Enter a valid day format XX"
                    )
                    textFieldEventCreation(
                        modifier = Modifier.weight(1f).padding(16.dp),
                        value = uiState.value.month,
                        onValueChange = { month ->
                            if (month.length <= 2){
                                eventCreationViewModel.setEventMonth(month)
                                isErrorMonth.value = month.isEmpty() || month.toIntOrNull() == null || month.toInt() !in 1..12 || month.length < 2}
                        },
                        isError = isErrorMonth.value,
                        supportingText = if(isErrorMonth.value){
                            { Text("Enter a valid month format XX") }
                        } else {
                            null
                        },
                        label = "Month",
                        errorMessage = "Enter a valid month format XX"
                    )
                    textFieldEventCreation(
                        modifier = Modifier.weight(1f).padding(16.dp),
                        value = uiState.value.year,
                        onValueChange = { year ->
                            if (year.length <= 4) {
                                eventCreationViewModel.setEventYear(year)
                                isErrorYear.value = year.isEmpty() || year.toIntOrNull() == null || year.toInt() < 2025 || year.length < 4
                            }
                        },
                        isError = isErrorYear.value,
                        supportingText = if (isErrorYear.value){
                            { Text("Enter a valid year format XXXX") }
                        } else {
                            null
                        },
                        label = "Year",
                        errorMessage = "Enter a valid year format XXXX"
                    )
                }
                Row(modifier = Modifier.fillMaxWidth().padding(paddingValues), horizontalArrangement = Arrangement.Center) {
                    textFieldEventCreation(
                        modifier = Modifier.width(120.dp),
                        value = uiState.value.hour,
                        onValueChange = { hour ->
                            if (hour.length <= 2) {
                                eventCreationViewModel.setEventHour(hour)
                                isErrorHour.value = hour.isEmpty() || hour.toIntOrNull() == null || hour.toInt() !in 0..23 || hour.length < 2
                            }
                        },
                        isError = isErrorHour.value,
                        supportingText = if (isErrorHour.value){
                            { Text("Enter a valid hour format XX") }
                        } else {
                            null
                        },
                        label = "Hour",
                        errorMessage = "Enter a valid hour format XX"
                    )

                    Spacer(modifier = Modifier.width(16.dp))
                    textFieldEventCreation(
                        modifier = Modifier.width(120.dp),
                        value = uiState.value.minute,
                        onValueChange = { minute ->
                            if (minute.length <= 2) {
                                eventCreationViewModel.setEventMinute(minute)
                                isErrorMinute.value =
                                    minute.isEmpty() || minute.toIntOrNull() == null || minute.toInt() !in 0..59 || minute.length < 2
                            }
                        },
                        isError = isErrorMinute.value,
                        supportingText = if (isErrorMinute.value){
                            { Text("Enter a valid minute format XX") }
                        } else {
                            null
                        },
                        label = "Minute",
                        errorMessage = "Enter a valid minute format XX"
                    )
                }
                Row(modifier = Modifier.padding(paddingValues)) {
                    Text(
                        "Selected Tags:",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 28.dp)
                    )

                    Button(
                        onClick = { onAddTag() },
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text("Add Tags")
                    }
                }
                Button(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    onClick = {
                        if (!isErrorName.value && !isErrorDescription.value && !isErrorDay.value &&
                            !isErrorMonth.value && !isErrorYear.value && !isErrorHour.value && !isErrorMinute.value){
                            eventCreationViewModel.saveEvent(location)
                            onSave()
                        }
                    }
                ){
                    Text("Save Event")
                }
            }
        }
    )
}

@Preview
@Composable
fun EventCreationPreview() {
    EventCreationScreen(EventCreationViewModel(), location = Location(0.0, 0.0))
}