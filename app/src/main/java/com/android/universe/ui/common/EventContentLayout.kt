package com.android.universe.ui.common

import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
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
import androidx.compose.ui.unit.dp
import com.android.universe.model.tag.Tag
import com.android.universe.ui.components.LiquidButton
import com.android.universe.ui.theme.Dimensions
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM")
private val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")

/**
 * Displays the main content layout of an Event object (Card or Popup).
 *
 * @param modifier Modifier for styling/layout.
 * @param eventId Unique ID used for test tags.
 * @param title Event title text.
 * @param description Optional event description.
 * @param date Event date and time.
 * @param tags List of event tags.
 * @param participants Number of people attending.
 * @param creator Event author/creator name.
 * @param imageContent Composable that renders the event image.
 * @param isUserParticipant Whether the user is part of the event.
 * @param isPrivate Whether the event is private.
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
    creator: String,
    imageContent: @Composable () -> Unit,
    isUserParticipant: Boolean,
    isPrivate: Boolean,
    onToggleEventParticipation: () -> Unit,
    onChatClick: () -> Unit
) {
  Column(modifier = modifier.fillMaxWidth()) {
    Row(modifier = Modifier.fillMaxWidth()) {
      Box(
          modifier =
              Modifier.fillMaxWidth(2f / 3f)
                  .height(Dimensions.CardImageHeight)
                  .clip(RoundedCornerShape(Dimensions.RoundedCornerLarge))
                  .testTag("${EventContentTestTags.EVENT_IMAGE_CONTAINER}_$eventId")) {
            imageContent()

            Row(
                modifier = Modifier.align(Alignment.TopStart).padding(Dimensions.PaddingLarge),
                verticalAlignment = Alignment.CenterVertically) {
                  LiquidButton(
                      modifier =
                          Modifier.widthIn(max = Dimensions.CardImageTagOverlayWidthDp)
                              .wrapContentWidth(),
                      enabled = false,
                      isInteractive = false,
                      height = Dimensions.CardImageTagOverlayHeight,
                      tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                      onClick = {}) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                          Text(
                              text = date.format(dateFormatter),
                              style = MaterialTheme.typography.bodySmall,
                              color = Color.White,
                              maxLines = 1,
                              overflow = TextOverflow.Ellipsis,
                              modifier =
                                  Modifier.testTag("${EventContentTestTags.EVENT_DATE}_$eventId"))
                          Spacer(Modifier.width(Dimensions.SpacerSmall))
                          Text(
                              text = date.format(timeFormatter),
                              style = MaterialTheme.typography.bodySmall,
                              color = Color.White,
                              maxLines = 1,
                              overflow = TextOverflow.Ellipsis,
                              modifier =
                                  Modifier.testTag("${EventContentTestTags.EVENT_TIME}_$eventId"))
                        }
                      }

                  if (isPrivate) {
                    Spacer(Modifier.width(Dimensions.SpacerSmall))
                    LiquidButton(
                        modifier = Modifier.wrapContentWidth(),
                        enabled = false,
                        isInteractive = false,
                        height = Dimensions.CardImageTagOverlayHeight,
                        width = Dimensions.CardImageTagOverlayHeight,
                        contentPadding = 0.dp,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        onClick = {}) {
                          Icon(
                              imageVector = Icons.Outlined.Lock,
                              contentDescription = "Private Event",
                              tint = Color.White,
                              modifier = Modifier.size(16.dp))
                        }
                  }
                }

            Box(modifier = Modifier.align(Alignment.BottomStart).padding(Dimensions.PaddingLarge)) {
              Text(
                  text = title,
                  style = MaterialTheme.typography.titleLarge,
                  color = Color.White,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                  modifier = Modifier.testTag("${EventContentTestTags.EVENT_TITLE}_$eventId"))
            }
          }

      Column(modifier = Modifier.weight(1f).height(Dimensions.CardImageHeight)) {
        TagColumn(
            tags = tags,
            isSelectable = false,
            isSelected = { false },
            heightList = Dimensions.CardImageHeight,
            modifierBox =
                Modifier.testTag("${EventContentTestTags.EVENT_TAGS}_${eventId}")
                    .padding(start = Dimensions.PaddingLarge))
      }
    }

    Spacer(Modifier.height(Dimensions.SpacerLarge))

    Text(
        text = description ?: "No description available",
        style = MaterialTheme.typography.bodyLarge,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.testTag("${EventContentTestTags.EVENT_DESCRIPTION}_$eventId"))

    Spacer(Modifier.height(Dimensions.SpacerLarge))

    EventCardActionsRow(
        eventId = eventId,
        participants = participants,
        creator = creator,
        isUserParticipant = isUserParticipant,
        onToggleEventParticipation = onToggleEventParticipation,
        onChatClick = onChatClick)
  }
}
