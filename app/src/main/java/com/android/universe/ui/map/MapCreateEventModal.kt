package com.android.universe.ui.map

import android.R.attr.text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.android.universe.R
import com.android.universe.ui.components.LiquidBottomSheet
import com.android.universe.ui.components.LiquidButton
import com.android.universe.ui.map.MapCreateEventModalTestTags.AI_CREATE_EVENT_BUTTON
import com.android.universe.ui.map.MapCreateEventModalTestTags.MANUAL_CREATE_EVENT_BUTTON
import com.android.universe.ui.theme.Dimensions

object MapCreateEventModalTestTags {
  const val MANUAL_CREATE_EVENT_BUTTON = "manual_create_event_button"
  const val AI_CREATE_EVENT_BUTTON = "ai_create_event_button"
}

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
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {

      // Title
      Text(
          text = stringResource(R.string.map_create_event_title),
          textAlign = TextAlign.Center,
          style = MaterialTheme.typography.titleLarge,
          color = MaterialTheme.colorScheme.onBackground,
          modifier =
              Modifier.fillMaxWidth()
                  .padding(
                      horizontal = Dimensions.PaddingLarge, vertical = Dimensions.PaddingLarge))

      Spacer(Modifier.height(Dimensions.SpacerLarge))

      // AI create button
      LiquidButton(
          onClick = {
            onAiCreate()
            onDismissRequest()
          },
          modifier =
              Modifier.fillMaxWidth(0.8f)
                  .padding(horizontal = Dimensions.PaddingLarge)
                  .testTag(AI_CREATE_EVENT_BUTTON)) {
            Text(stringResource(R.string.map_create_event_ai_button))
          }

      Spacer(Modifier.height(Dimensions.SpacerLarge))

      // Manual create button
      LiquidButton(
          onClick = {
            onManualCreate()
            onDismissRequest()
          },
          modifier =
              Modifier.fillMaxWidth(0.8f)
                  .padding(horizontal = Dimensions.PaddingLarge)
                  .testTag(MANUAL_CREATE_EVENT_BUTTON)) {
            Text(stringResource(R.string.map_create_event_manual_button))
          }

      Spacer(Modifier.padding(top = Dimensions.SpacerLarge).navigationBarsPadding())
    }
  }
}
