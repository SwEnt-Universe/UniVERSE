package com.android.universe.ui.event

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.android.universe.model.location.Location
import com.android.universe.ui.common.EventContentLayout
import com.android.universe.ui.common.EventContentTestTags
import com.android.universe.ui.components.ImageDisplay
import com.android.universe.ui.components.LiquidBox
import com.android.universe.ui.theme.CardShape
import com.android.universe.ui.theme.Dimensions

object EventCardTestTags {
  const val EVENT_CARD = "event_card"
}

/**
 * Composable function that displays an event card with event details inside a styled LiquidBox.
 *
 * @param event The [EventUIState] object containing event details to be displayed.
 * @param onToggleEventParticipation Callback function invoked when the participation button is
 *   clicked, with event ID as a parameter.
 * @param onChatNavigate Callback function invoked when the chat button is clicked, with event ID
 *   and title as parameters.
 * @param onCardClick Callback function invoked when the card is clicked, with event ID and location
 *   as parameters.
 * @param onEditButtonClick Callback invoked when the user presses the "Edit" button on an event.
 * @param isUserOwner Boolean indicating if the current user is the event owner.
 * @param showActions to determine if the action bar should be shown on some future events
 */
@Composable
fun EventCard(
    event: EventUIState,
    onToggleEventParticipation: (eventId: String) -> Unit,
    onChatNavigate: (eventId: String, eventTitle: String) -> Unit,
    onCardClick: (eventId: String, eventLocation: Location) -> Unit,
    onEditButtonClick: (eventId: String, eventLocation: Location) -> Unit = { _, _ -> },
    isUserOwner: Boolean,
    showActions: Boolean? = null
) {
  LiquidBox(
      shape = CardShape,
      modifier =
          Modifier.padding(Dimensions.PaddingMedium)
              .testTag("${EventCardTestTags.EVENT_CARD}_${event.index}")
              .clickable(enabled = !event.hasPassed) { onCardClick(event.id, event.location) }) {
        EventContentLayout(
            eventId = event.index.toString(),
            title = event.title,
            description = event.description,
            date = event.date,
            tags = event.tags,
            participants = event.participants,
            creator = event.creator,
            imageContent = {
              ImageDisplay(
                  image = event.eventPicture,
                  contentDescription = null,
                  modifier = Modifier.matchParentSize().testTag(EventContentTestTags.EVENT_IMAGE))
            },
            isUserParticipant = event.joined,
            isPrivate = event.isPrivate,
            onToggleEventParticipation = { onToggleEventParticipation(event.id) },
            showActions = event.hasPassed.not() && showActions != false,
            onChatClick = { onChatNavigate(event.id, event.title) },
            modifier = Modifier.padding(Dimensions.PaddingLarge),
            onEditClick = { onEditButtonClick(event.id, event.location) },
            isUserOwner = isUserOwner)
      }
}
