package com.android.universe.ui.map

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.android.universe.ui.components.LiquidBottomSheet
import com.android.universe.ui.components.LiquidButton
import com.android.universe.ui.theme.Dimensions

/**
 * A modal bottom sheet prompting the user to choose between:
 * - Creating an event manually
 * - Creating an event using AI
 *
 * Uses the app’s custom LiquidBottomSheet for a glass-blur effect.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapCreateEventModal(
    isPresented: Boolean,
    onDismissRequest: () -> Unit,
    onAiCreate: () -> Unit,
    onManualCreate: () -> Unit,
) {
  LiquidBottomSheet(
      isPresented = isPresented,
      onDismissRequest = onDismissRequest,
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {

      // Optional title
      Text(
          text = "Create Event",
          style = MaterialTheme.typography.headlineSmall,
          color = MaterialTheme.colorScheme.onBackground,
          modifier =
              Modifier.fillMaxWidth()
                  .padding(
                      horizontal = Dimensions.PaddingLarge, vertical = Dimensions.PaddingLarge))

      // OPTIONAL: description
      Text(
          text = "Choose how you want to create your event.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
          modifier =
              Modifier.fillMaxWidth()
                  .padding(
                      horizontal = Dimensions.PaddingLarge, vertical = Dimensions.PaddingSmall))

      Spacer(Modifier.height(Dimensions.SpacerLarge))

      // AI create button
      LiquidButton(
          onClick = {
            onAiCreate()
            onDismissRequest()
          },
          modifier = Modifier.fillMaxWidth().padding(horizontal = Dimensions.PaddingLarge)) {
            Text("Create Event with AI")
          }

      Spacer(Modifier.height(Dimensions.SpacerLarge))

      // Manual create button
      LiquidButton(
          onClick = {
            onManualCreate()
            onDismissRequest()
          },
          modifier = Modifier.fillMaxWidth().padding(horizontal = Dimensions.PaddingLarge)) {
            Text("Create Event Yourself")
          }

      Spacer(Modifier.height(48.dp))
    }
  }
}
