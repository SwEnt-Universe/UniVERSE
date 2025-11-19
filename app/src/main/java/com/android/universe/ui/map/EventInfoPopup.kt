package com.android.universe.ui.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.android.universe.model.event.Event
import com.android.universe.ui.components.LiquidEventCard

@Composable
fun EventInfoPopup(
    event: Event,
    isUserParticipant: Boolean,
    onDismiss: () -> Unit,
    onToggleEventParticipation: () -> Unit
) {
  Box(
      modifier =
          Modifier.fillMaxSize()
              .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
              .clickable(onClick = onDismiss)
              .testTag(MapScreenTestTags.EVENT_INFO_POPUP),
      contentAlignment = Alignment.BottomCenter) {
        AnimatedVisibility(
            visible = true,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })) {
              LiquidEventCard(
                  modifier = Modifier.fillMaxWidth(),
                  event = event,
                  isUserParticipant = isUserParticipant,
                  onToggleEventParticipation = onToggleEventParticipation,
                  onChatClick = {},
                  isMapScreen = true)
            }
      }
}
