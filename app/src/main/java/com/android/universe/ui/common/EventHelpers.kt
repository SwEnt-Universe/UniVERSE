package com.android.universe.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import com.android.universe.ui.components.LiquidButton
import com.android.universe.ui.theme.Dimensions

/** Object containing test tags for Event content components. */
object EventContentTestTags {
  const val EVENT_IMAGE_CONTAINER = "event_image_container"
  const val EVENT_IMAGE = "event_image"
  const val EVENT_TITLE = "event_title"
  const val EVENT_DATE = "event_date"
  const val EVENT_TIME = "event_time"
  const val EVENT_DESCRIPTION = "event_description"
  const val EVENT_PARTICIPANTS = "event_participants"
  const val EVENT_CREATOR = "event_creator"
  const val EVENT_TAGS = "event_tags"
  const val PARTICIPATION_BUTTON = "event_participation_button"
  const val CHAT_BUTTON = "event_chat_button"
  const val EDIT_BUTTON = "event_edit_button"
}

/**
 * Composable function that displays the number of participants and the event creator's name in a
 * column or row layout based on user participation (if the Chat button is visible).
 *
 * @param eventId The unique identifier of the event.
 * @param participants The number of participants in the event.
 * @param creator The creator of the event.
 * @param isUserParticipant Boolean indicating if the current user is a participant of the event.
 */
@Composable
fun ParticipantsAuthorColumn(
    modifier: Modifier = Modifier,
    eventId: String,
    participants: Int,
    creator: String,
    isUserParticipant: Boolean
) {
  if (isUserParticipant) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
      Text(
          modifier = Modifier.testTag("${EventContentTestTags.EVENT_PARTICIPANTS}_$eventId"),
          text = "$participants joined",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis)
      HorizontalDivider(
          modifier =
              Modifier.padding(vertical = Dimensions.SpacerSmall)
                  .width(Dimensions.BoxDescriptionSize),
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          thickness = Dimensions.CardDividerThickness)
      Text(
          modifier = Modifier.testTag("${EventContentTestTags.EVENT_CREATOR}_$eventId"),
          text = "by $creator",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis)
    }
  } else {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
      Text(
          modifier = Modifier.testTag("${EventContentTestTags.EVENT_PARTICIPANTS}_$eventId"),
          text = "$participants joined",
          style = MaterialTheme.typography.bodyLarge,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis)
      VerticalDivider(
          modifier =
              Modifier.height(Dimensions.IconSizeMedium)
                  .padding(horizontal = Dimensions.SpacerSmall),
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          thickness = Dimensions.CardDividerThickness)
      Text(
          modifier = Modifier.testTag("${EventContentTestTags.EVENT_CREATOR}_$eventId"),
          text = "by $creator",
          style = MaterialTheme.typography.bodyLarge,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis)
    }
  }
}

/**
 * Composable function that displays action buttons and event info in a row for an event card.
 *
 * @param eventId The unique identifier of the event.
 * @param participants The number of participants in the event.
 * @param creator The creator of the event.
 * @param isUserParticipant Boolean indicating if the current user is a participant of the event.
 * @param isUserOwner Boolean indicating if the current user is the owner of the event.
 * @param onToggleEventParticipation Lambda function to be called when the participation button is
 *   clicked.
 * @param onEditClick Lambda function to be called when the edit button is clicked.
 * @param onChatClick Lambda function to be called when the chat button is clicked.
 */
@Composable
fun EventCardActionsRow(
    eventId: String,
    participants: Int,
    creator: String,
    isUserParticipant: Boolean,
    isUserOwner: Boolean,
    onToggleEventParticipation: () -> Unit,
    onEditClick: () -> Unit = {},
    onChatClick: () -> Unit
) {
  Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier.fillMaxWidth()) {
        if (isUserParticipant) {
          LiquidButton(
              onClick = onChatClick,
              height = Dimensions.CardButtonHeight,
              contentPadding = Dimensions.PaddingMedium,
              modifier =
                  Modifier.testTag("${EventContentTestTags.CHAT_BUTTON}_$eventId")
                      .widthIn(max = Dimensions.CardButtonWidthDp)
                      .wrapContentWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                      imageVector = Icons.AutoMirrored.Filled.Chat,
                      contentDescription = "Chat",
                      tint = MaterialTheme.colorScheme.onSurface,
                      modifier = Modifier.size(Dimensions.IconSizeMedium))
                  Spacer(Modifier.width(Dimensions.SpacerSmall))
                  Text(
                      "Chat",
                      style = MaterialTheme.typography.labelLarge,
                      color = MaterialTheme.colorScheme.onSurface)
                }
              }
          Spacer(Modifier.width(Dimensions.SpacerLarge))
        }

        ParticipantsAuthorColumn(
            Modifier.weight(1f), eventId, participants, creator, isUserParticipant)

        Spacer(Modifier.width(Dimensions.SpacerMedium))

        if (isUserOwner) {
          LiquidButton(
              onClick = { onEditClick() },
              height = Dimensions.CardButtonHeight,
              contentPadding = Dimensions.PaddingSmall,
              modifier =
                  Modifier.testTag("${EventContentTestTags.EDIT_BUTTON}_$eventId")
                      .widthIn(max = Dimensions.CardButtonWidthDp)
                      .wrapContentWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                      imageVector = Icons.Filled.Edit,
                      contentDescription = "Edit Event",
                      tint = MaterialTheme.colorScheme.onSurface,
                      modifier = Modifier.size(Dimensions.IconSizeMedium))
                  Spacer(Modifier.width(Dimensions.SpacerSmall))
                  Text(
                      text = "Edit",
                      style = MaterialTheme.typography.labelLarge,
                      color = MaterialTheme.colorScheme.onSurface)
                }
              }
        } else {
          LiquidButton(
              onClick = { onToggleEventParticipation() },
              height = Dimensions.CardButtonHeight,
              contentPadding = Dimensions.PaddingSmall,
              modifier =
                  Modifier.testTag("${EventContentTestTags.PARTICIPATION_BUTTON}_$eventId")
                      .widthIn(max = Dimensions.CardButtonWidthDp)
                      .wrapContentWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                      imageVector =
                          if (isUserParticipant) Icons.Filled.Close else Icons.Filled.Check,
                      contentDescription = "Toggle Participation",
                      tint = MaterialTheme.colorScheme.onSurface,
                      modifier = Modifier.size(Dimensions.IconSizeMedium))
                  Spacer(Modifier.width(Dimensions.SpacerSmall))
                  Text(
                      text = if (isUserParticipant) "Leave" else "Join",
                      style = MaterialTheme.typography.labelLarge,
                      color = MaterialTheme.colorScheme.onSurface)
                }
              }
        }
      }
}
