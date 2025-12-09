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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import com.android.universe.model.event.Event
import com.android.universe.model.location.Location
import com.android.universe.ui.common.EventContentLayout
import com.android.universe.ui.common.EventContentTestTags
import com.android.universe.ui.components.ImageDisplay
import com.android.universe.ui.components.LiquidBottomSheet
import com.android.universe.ui.theme.Dimensions
import com.android.universe.ui.utils.LocalLayerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import java.time.LocalDateTime

/**
 * A popup component that displays detailed information about an event. Places an EventContentLayout
 * inside a LiquidBottomSheet with slide-in/out animations.
 *
 * @param modifier The modifier to be applied to the popup.
 * @param event The [Event] object containing event details to be displayed.
 * @param creator The name of the event creator.
 * @param isUserParticipant Boolean indicating if the user is a participant of the event.
 * @param onDismiss Callback function invoked when the popup is dismissed.
 * @param onChatNavigate Callback function invoked when the user clicks on the chat button.
 * @param onToggleEventParticipation Callback function invoked when the user toggles their
 *   participation status.
 * @param isPreview modifies options if used to preview AI event suggestion
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventInfoPopup(
    modifier: Modifier = Modifier,
    event: Event,
    creator: String,
    isUserParticipant: Boolean,
    onDismiss: () -> Unit,
    onChatNavigate: (eventId: String, eventTitle: String) -> Unit,
    onToggleEventParticipation: () -> Unit,
    isPreview: Boolean = false
) {
  Box(
      modifier =
          modifier
              .fillMaxSize()
              .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f))
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
                        creator = creator,
                        imageContent = {
                          ImageDisplay(
                              image = event.eventPicture,
                              contentDescription = null,
                              modifier = Modifier.testTag(EventContentTestTags.EVENT_IMAGE))
                        },
                        isUserParticipant = isUserParticipant,
                        isPrivate = event.isPrivate,
                        onToggleEventParticipation = onToggleEventParticipation,
                        onChatClick = { onChatNavigate(event.id, event.title) },
                        showActions = !isPreview,
                    )
                  }
            }
      }
}

@Composable
@Preview
private fun EventInfoPopUpPreview() {
  val previewEvent =
      Event(
          id = "preview",
          title = "preview",
          date = LocalDateTime.now(),
          tags = emptySet(),
          creator = "preview",
          location = Location(0.0, 0.0))

  val stubBackdrop = rememberLayerBackdrop { drawRect(Color.Transparent) }

  CompositionLocalProvider(LocalLayerBackdrop provides stubBackdrop) {
    EventInfoPopup(
        event = previewEvent,
        creator = "",
        isUserParticipant = true,
        onDismiss = {},
        onChatNavigate = { _, _ -> },
        onToggleEventParticipation = {},
        isPreview = false,
    )
  }
}
