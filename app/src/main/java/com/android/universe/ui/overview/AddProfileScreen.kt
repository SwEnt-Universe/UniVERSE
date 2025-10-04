package com.android.universe.ui.overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview(showBackground = true)
@Composable
fun AddProfileScreen() {
  var hasTouchedUsername by remember { mutableStateOf(false) }
  var hasTouchedFirstName by remember { mutableStateOf(false) }
  var hasTouchedLastName by remember { mutableStateOf(false) }
  var hasTouchedDay by remember { mutableStateOf(false) }
  var hasTouchedMonth by remember { mutableStateOf(false) }
  var hasTouchedYear by remember { mutableStateOf(false) }

  Scaffold(
      content = { paddingValues ->
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp).padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {

          // Username part
          Text(text = "Username", style = MaterialTheme.typography.bodyLarge)
          OutlinedTextField(
              value = "TODO",
              onValueChange = { "TODO" },
              placeholder = { Text("Username") },
              modifier =
                  Modifier.fillMaxWidth()
                      .testTag("TODO")
                      .onFocusChanged({ focusState ->
                        if (focusState.isFocused) {
                          hasTouchedUsername = true
                        }
                      }),
              shape = RoundedCornerShape(12.dp),
              singleLine = true)
          if (hasTouchedUsername) {
            /* Ajouter une condition et gérer unique */
            Text(
                text = "Username cannot be empty",
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.testTag("TODO"))
          }

          // First Name part
          Text(text = "First Name", style = MaterialTheme.typography.bodyLarge)
          OutlinedTextField(
              value = "TODO",
              onValueChange = { "TODO" },
              placeholder = { Text("First Name") },
              modifier =
                  Modifier.fillMaxWidth()
                      .testTag("TODO")
                      .onFocusChanged({ focusState ->
                        if (focusState.isFocused) {
                          hasTouchedFirstName = true
                        }
                      }),
              shape = RoundedCornerShape(12.dp),
              singleLine = true)
          if (hasTouchedFirstName) {
            /* Ajouter une condition */
            Text(
                text = "First Name cannot be empty",
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.testTag("TODO"))
          }

          // Last Name part
          Text(text = "Last Name", style = MaterialTheme.typography.bodyLarge)
          OutlinedTextField(
              value = "TODO",
              onValueChange = { "TODO" },
              placeholder = { Text("Last Name") },
              modifier =
                  Modifier.fillMaxWidth()
                      .testTag("TODO")
                      .onFocusChanged({ focusState ->
                        if (focusState.isFocused) {
                          hasTouchedLastName = true
                        }
                      }),
              shape = RoundedCornerShape(12.dp),
              singleLine = true)
          if (hasTouchedLastName) {
            /* Ajouter une condition */
            Text(
                text = "Last Name cannot be empty",
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.testTag("TODO"))
          }

          // Description part
          Text(text = "Description", style = MaterialTheme.typography.bodyLarge)
          OutlinedTextField(
              value = "TODO",
              onValueChange = { "TODO" },
              placeholder = { Text("Description") },
              modifier = Modifier.fillMaxWidth().testTag("TODO"),
              shape = RoundedCornerShape(12.dp))

          // Country part

          // Date of Birth part
          Text(text = "Date of Birth", style = MaterialTheme.typography.bodyLarge)
          Row(
              modifier = Modifier.fillMaxWidth().padding(paddingValues),
              horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                      OutlinedTextField(
                          value = "TODO",
                          onValueChange = { "TODO" },
                          label = { Text(text = "Day") },
                          placeholder = { Text("Day") },
                          modifier =
                              Modifier.fillMaxWidth()
                                  .testTag("TODO")
                                  .onFocusChanged({ focusState ->
                                    if (focusState.isFocused) {
                                      hasTouchedDay = true
                                    }
                                  }),
                          shape = RoundedCornerShape(12.dp),
                          singleLine = true)
                      if (hasTouchedDay) {
                        Text(
                            text = "Day cannot be empty",
                            color = Color.Red,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.testTag("TODO"))
                      }
                    }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                      OutlinedTextField(
                          value = "TODO",
                          onValueChange = { "TODO" },
                          label = { Text(text = "Month") },
                          placeholder = { Text("Month") },
                          modifier =
                              Modifier.fillMaxWidth()
                                  .testTag("TODO")
                                  .onFocusChanged({ focusState ->
                                    if (focusState.isFocused) {
                                      hasTouchedMonth = true
                                    }
                                  }),
                          shape = RoundedCornerShape(12.dp),
                          singleLine = true)
                      if (hasTouchedMonth) {
                        Text(
                            text = "Month cannot be empty",
                            color = Color.Red,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.testTag("TODO"))
                      }
                    }
                Column(
                    modifier = Modifier.weight(1.5f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                      OutlinedTextField(
                          value = "TODO",
                          onValueChange = { "TODO" },
                          label = { Text(text = "Year") },
                          placeholder = { Text("Year") },
                          modifier =
                              Modifier.fillMaxWidth()
                                  .testTag("TODO")
                                  .onFocusChanged({ focusState ->
                                    if (focusState.isFocused) {
                                      hasTouchedYear = true
                                    }
                                  }),
                          shape = RoundedCornerShape(12.dp),
                          singleLine = true)
                      if (hasTouchedYear) {
                        Text(
                            text = "Year cannot be empty",
                            color = Color.Red,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.testTag("TODO"))
                      }
                    }
              }
        }
      })
}
