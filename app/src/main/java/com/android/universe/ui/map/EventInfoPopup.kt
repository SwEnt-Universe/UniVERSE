package com.android.universe.ui.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.android.universe.model.event.Event
import com.android.universe.ui.common.EventContentLayout
import com.android.universe.ui.common.EventImageHelper
import com.android.universe.ui.components.LiquidBottomSheet
import com.android.universe.ui.theme.Dimensions

/*
 * A popup component that displays detailed information about an event.
 * Places an EventContentLayout inside a LiquidBottomSheet with slide-in/out animations.
 *
 * @param event The [Event] object containing event details to be displayed.
 * @param isUserParticipant Boolean indicating if the user is a participant of the event.
 * @param onDismiss Callback function invoked when the popup is dismissed.
 * @param onToggleEventParticipation Callback function invoked when the user toggles their participation status.
 */
@OptIn(ExperimentalMaterial3Api::class)
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
              LiquidBottomSheet(
                  modifier = Modifier.fillMaxWidth(),
                  isPresented = true,
                  shape = MaterialTheme.shapes.large,
                  onDismissRequest = onDismiss) {
                    EventContentLayout(
                        modifier = Modifier.padding(Dimensions.PaddingLarge),
                        eventId = event.id,
                        title = event.title,
                        description = event.description,
                        date = event.date,
                        tags = event.tags.toList(),
                        participants = event.participants.size,
                        imageContent = { EventImageHelper(eventImage = event.eventPicture) },
                        isUserParticipant = isUserParticipant,
                        onToggleEventParticipation = onToggleEventParticipation,
                        onChatClick = {},
                        onLocationClick = null)
                  }
            }
      }
}
