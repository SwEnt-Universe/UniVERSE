package com.android.universe.ui.common

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android.universe.R
import com.android.universe.model.tag.Tag
import com.android.universe.ui.components.LiquidButton
import com.android.universe.ui.theme.Dimensions
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun EventContentLayout(
    modifier: Modifier = Modifier,
    title: String,
    description: String? = null,
    date: LocalDateTime,
    tags: List<Tag>,
    participants: Int,
    eventBitmap: Bitmap? = null,
    isUserParticipant: Boolean,
    onToggleEventParticipation: () -> Unit,
    onChatClick: () -> Unit,
    onLocationClick: (() -> Unit)? = null,
    bottomSpacing: Dp = 0.dp
) {
  Column(modifier = modifier.fillMaxWidth()) {
    Box(modifier = Modifier.fillMaxWidth()) {
      Box(
          modifier =
              Modifier.fillMaxWidth(2f / 3f)
                  .height(Dimensions.EventCardImageHeight)
                  .align(Alignment.TopStart)
                  .clip(RoundedCornerShape(Dimensions.RoundedCornerLarge))
                  .testTag(EventCardTestTags.EVENT_IMAGE_CONTAINER)) {
            if (eventBitmap == null) {
              Image(
                  painter = painterResource(id = R.drawable.default_event_img),
                  contentDescription = null,
                  contentScale = ContentScale.Crop,
                  modifier =
                      Modifier.matchParentSize().testTag(EventCardTestTags.DEFAULT_EVENT_IMAGE))
            } else {
              Image(
                  bitmap = eventBitmap.asImageBitmap(),
                  contentDescription = null,
                  contentScale = ContentScale.Crop,
                  modifier = Modifier.matchParentSize().testTag(EventCardTestTags.EVENT_IMAGE))
            }

            // Only show location button if callback is provided
            if (onLocationClick != null) {
              Box(
                  modifier =
                      Modifier.align(Alignment.TopEnd)
                          .padding(Dimensions.PaddingLarge)
                          .size(70.dp)
                          .clip(CircleShape)
                          .background(Color.White.copy(alpha = 0.4f))
                          .clickable { onLocationClick() }
                          .testTag(EventCardTestTags.EVENT_LOCATION_BUTTON),
                  contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = "Location",
                        modifier = Modifier.size(50.dp),
                        tint = Color.White)
                  }
            }

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
                        modifier = Modifier.testTag(EventCardTestTags.EVENT_TITLE))
                    Spacer(Modifier.height(Dimensions.SpacerSmall))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        content = {
                          Text(
                              text = date.format(DateTimeFormatter.ofPattern("dd/MMM/yyyy")),
                              style = MaterialTheme.typography.bodyLarge,
                              color = Color.White,
                              modifier = Modifier.testTag(EventCardTestTags.EVENT_DATE))
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
                              modifier = Modifier.testTag(EventCardTestTags.EVENT_TIME))
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
        modifier = Modifier.testTag(EventCardTestTags.EVENT_DESCRIPTION))

    Spacer(Modifier.height(Dimensions.SpacerMedium))

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier =
            Modifier.fillMaxWidth()
                .padding(start = Dimensions.PaddingMedium, end = Dimensions.PaddingMedium)) {
          LiquidButton(
              onClick = onChatClick,
              height = Dimensions.EventCardButtonHeight,
              width = Dimensions.EventCardButtonWidth,
              modifier = Modifier.testTag(EventCardTestTags.CHAT_BUTTON)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                      imageVector = Icons.AutoMirrored.Filled.Chat,
                      contentDescription = "Chat",
                      modifier = Modifier.size(Dimensions.IconSizeMedium))
                  Spacer(Modifier.width(Dimensions.SpacerSmall))
                  Text("Chat", style = MaterialTheme.typography.labelLarge)
                }
              }

          Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.testTag(EventCardTestTags.EVENT_PARTICIPANTS)) {
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
              height = Dimensions.EventCardButtonHeight,
              width = Dimensions.EventCardButtonWidth,
              modifier = Modifier.testTag(EventCardTestTags.PARTICIPATION_BUTTON)) {
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

    if (bottomSpacing > 0.dp) {
      Spacer(Modifier.height(bottomSpacing))
    }
  }
}
