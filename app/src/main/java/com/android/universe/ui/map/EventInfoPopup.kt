package com.android.universe.ui.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import com.android.universe.ui.navigation.FlowBottomMenu
import com.android.universe.ui.navigation.FlowTab
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
 * @param currentUserId The ID of the currently logged-in user.
 * @param onDismiss Callback function invoked when the popup is dismissed.
 * @param onChatNavigate Callback function invoked when the user clicks on the chat button.
 * @param onEditButtonClick Callback invoked when the user presses the "Edit" button on an event.
 * @param isUserParticipant Boolean indicating if the user is a participant of the event.
 * @param onToggleEventParticipation Callback function invoked when the user toggles their
 *   participation status.
 * @param isPreview modifies options if used to preview AI event suggestion
 * @param onAccept Callback function invoked when the user accepts the preview
 * @param onReject Callback function invoked when the user rejects the preview
 * @param onRegenerate Callback function invoked when the user wants to regenerate the preview
 * @param onEdit Callback function invoked when the user wants to edit the event
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventInfoPopup(
    modifier: Modifier = Modifier,
    event: Event,
    creator: String,
    currentUserId: String,
    onDismiss: () -> Unit,
    onChatNavigate: (eventId: String, eventTitle: String) -> Unit,
    onEditButtonClick: () -> Unit = {},
    isUserParticipant: Boolean,
    onToggleEventParticipation: () -> Unit,
    isPreview: Boolean = false,
    onAccept: () -> Unit = {},
    onReject: () -> Unit = {},
    onRegenerate: () -> Unit = {}
) {
  LiquidBottomSheet(
      isPresented = true,
      onDismissRequest = { if (isPreview) onReject() else onDismiss() },
      modifier = modifier) {
        EventContentLayout(
            modifier = Modifier.padding(Dimensions.PaddingLarge).navigationBarsPadding(),
            eventId = event.id,
            title = event.title,
            description = event.description,
            date = event.date,
            tags = event.tags.toList(),
            participants = event.participants.size,
            creator = creator,
            isUserOwner = event.creator == currentUserId,
            imageContent = {
              ImageDisplay(
                  image = event.eventPicture,
                  contentDescription = null,
                  modifier = Modifier.fillMaxSize().testTag(EventContentTestTags.EVENT_IMAGE))
            },
            isUserParticipant = isUserParticipant,
            isPrivate = event.isPrivate,
            onToggleEventParticipation = onToggleEventParticipation,
            showActions = !isPreview,
            onChatClick = { onChatNavigate(event.id, event.title) },
            onEditClick = { onEditButtonClick() })

        if (isPreview) {
          FlowBottomMenu(
              flowTabs =
                  listOf(
                      FlowTab.Reject(onClick = onReject),
                      FlowTab.Regenerate(onClick = onRegenerate, enabled = true),
                      FlowTab.Confirm(onClick = onAccept, enabled = true)))
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
        currentUserId = "",
        isUserParticipant = true,
        onDismiss = {},
        onChatNavigate = { _, _ -> },
        onToggleEventParticipation = {})
  }
}
