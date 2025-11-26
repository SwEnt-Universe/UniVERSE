package com.android.universe.ui.event

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.android.universe.ui.common.EventContentLayout
import com.android.universe.ui.common.EventImageHelper
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
 * @param viewModel The [EventViewModel] used to handle user interactions such as joining or leaving
 *   the event.
 * @param onChatNavigate A lambda function that takes a chatId and chatto navigate to the chat.
 */
@Composable
fun EventCard(
    event: EventUIState,
    viewModel: EventViewModel,
    onChatNavigate: (eventId: String, eventTitle: String) -> Unit
) {
  LiquidBox(
      shape = CardShape,
      modifier =
          Modifier.padding(Dimensions.PaddingMedium)
              .testTag("${EventCardTestTags.EVENT_CARD}_${event.index}")) {
        EventContentLayout(
            eventId = event.index.toString(),
            title = event.title,
            description = event.description,
            date = event.date,
            tags = emptyList(),
            participants = event.participants,
            imageContent = {
              EventImageHelper(
                  eventImage = event.eventPicture, modifier = Modifier.matchParentSize())
            },
            isUserParticipant = event.joined,
            onToggleEventParticipation = { viewModel.joinOrLeaveEvent(event.index) },
            onChatClick = { onChatNavigate(event.id, event.title) },
            onLocationClick = { /* TODO: Implement map navigation */ },
            modifier = Modifier.padding(Dimensions.PaddingLarge))
      }
}
