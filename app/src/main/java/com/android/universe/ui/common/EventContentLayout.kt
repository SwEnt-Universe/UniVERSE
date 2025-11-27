package com.android.universe.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import com.android.universe.model.tag.Tag
import com.android.universe.ui.components.LiquidButton
import com.android.universe.ui.theme.Dimensions
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Displays the main content layout of an Event object (Card or Popup).
 *
 * @param modifier Modifier for styling/layout.
 * @param eventId Unique ID used for test tags.
 * @param title Event title text.
 * @param description Optional event description.
 * @param date Event date and time.
 * @param tags List of event tags (currently unused in this layout).
 * @param participants Number of people attending.
 * @param imageContent Composable that renders the event image.
 * @param isUserParticipant Whether the user is part of the event.
 * @param onToggleEventParticipation Callback triggered when user taps Join/Leave.
 * @param onChatClick Callback for chat button.
 */
@Composable
fun EventContentLayout(
    modifier: Modifier = Modifier,
    eventId: String,
    title: String,
    description: String? = null,
    date: LocalDateTime,
    tags: List<Tag>,
    participants: Int,
    imageContent: @Composable () -> Unit,
    isUserParticipant: Boolean,
    onToggleEventParticipation: () -> Unit,
    onChatClick: () -> Unit
) {
  Column(modifier = modifier.fillMaxWidth()) {
    Box(modifier = Modifier.fillMaxWidth()) {
      Box(
          modifier =
              Modifier.fillMaxWidth(2f / 3f)
                  .height(Dimensions.CardImageHeight)
                  .align(Alignment.TopStart)
                  .clip(RoundedCornerShape(Dimensions.RoundedCornerLarge))
                  .testTag("${EventContentTestTags.EVENT_IMAGE_CONTAINER}_$eventId")) {
            imageContent()

            Box(
                modifier =
                    Modifier.align(Alignment.BottomStart)
                        .padding(Dimensions.PaddingMedium)
                        .padding(
                            horizontal = Dimensions.PaddingMedium,
                            vertical = Dimensions.PaddingMedium)) {
                  Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.testTag("${EventContentTestTags.EVENT_TITLE}_$eventId"))
                    Spacer(Modifier.height(Dimensions.SpacerSmall))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        content = {
                          Text(
                              text = date.format(DateTimeFormatter.ofPattern("dd/MMM/yyyy")),
                              style = MaterialTheme.typography.bodyLarge,
                              color = Color.White,
                              modifier =
                                  Modifier.testTag("${EventContentTestTags.EVENT_DATE}_$eventId"))
                          Spacer(Modifier.width(Dimensions.SpacerSmall))
                          Icon(
                              imageVector = Icons.Outlined.StarBorder,
                              contentDescription = null,
                              tint = Color.White,
                              modifier = Modifier.size(Dimensions.IconSizeSmall),
                          )
                          Spacer(Modifier.width(Dimensions.SpacerSmall))
                          Text(
                              text = date.format(DateTimeFormatter.ofPattern("hh:mm")),
                              style = MaterialTheme.typography.bodyLarge,
                              color = Color.White,
                              modifier =
                                  Modifier.testTag("${EventContentTestTags.EVENT_TIME}_$eventId"))
                        })
                  }
                }
          }
    }

    Spacer(Modifier.height(Dimensions.SpacerMedium))

    Text(
        text = description ?: "No description available",
        style = MaterialTheme.typography.bodyMedium,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.testTag("${EventContentTestTags.EVENT_DESCRIPTION}_$eventId"))

    Spacer(Modifier.height(Dimensions.SpacerMedium))

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier =
            Modifier.fillMaxWidth()
                .padding(start = Dimensions.PaddingMedium, end = Dimensions.PaddingMedium)) {
          if (isUserParticipant) {
            LiquidButton(
                onClick = onChatClick,
                height = Dimensions.CardButtonHeight,
                width = Dimensions.CardButtonWidth,
                modifier = Modifier.testTag("${EventContentTestTags.CHAT_BUTTON}_$eventId")) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = "Chat",
                        modifier = Modifier.size(Dimensions.IconSizeMedium))
                    Spacer(Modifier.width(Dimensions.SpacerSmall))
                    Text("Chat", style = MaterialTheme.typography.labelLarge)
                  }
                }
          }

          Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.testTag("${EventContentTestTags.EVENT_PARTICIPANTS}_$eventId")) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = "Participants",
                    modifier = Modifier.size(Dimensions.IconSizeMedium))
                Spacer(Modifier.width(Dimensions.SpacerSmall))
                Text(
                    text = "$participants people going",
                    style = MaterialTheme.typography.bodyMedium)
              }

          LiquidButton(
              onClick = onToggleEventParticipation,
              height = Dimensions.CardButtonHeight,
              width = Dimensions.CardButtonWidth,
              modifier =
                  Modifier.testTag("${EventContentTestTags.PARTICIPATION_BUTTON}_$eventId")) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                      imageVector =
                          if (isUserParticipant) Icons.Filled.Close else Icons.Filled.Check,
                      contentDescription = "Toggle Participation",
                      modifier = Modifier.size(Dimensions.IconSizeMedium))
                  Spacer(Modifier.width(Dimensions.SpacerSmall))
                  Text(
                      text = if (isUserParticipant) "Leave" else "Join",
                      style = MaterialTheme.typography.labelLarge)
                }
              }
        }
  }
}
