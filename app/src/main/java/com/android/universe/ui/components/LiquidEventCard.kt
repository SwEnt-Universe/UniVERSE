package com.android.universe.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
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
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.android.universe.R
import com.android.universe.di.DefaultDP
import com.android.universe.model.event.Event
import com.android.universe.ui.theme.CardShape
import com.android.universe.ui.theme.Dimensions
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.withContext

@Composable
fun LiquidEventCard(
    event: Event,
    isUserParticipant: Boolean,
    onToggleEventParticipation: () -> Unit,
    onChatClick: () -> Unit,
    modifier: Modifier = Modifier,
    isMapScreen: Boolean = false
) {
  LiquidBox(modifier = modifier.testTag("LiquidEventCard"), shape = CardShape) {
    Column(modifier = Modifier.fillMaxWidth().padding(Dimensions.PaddingLarge)) {
      Box(modifier = Modifier.fillMaxWidth()) {
        val bitmap =
            produceState<Bitmap?>(initialValue = null, event.eventPicture) {
                  value =
                      if (event.eventPicture != null) {
                        withContext(DefaultDP.io) {
                          BitmapFactory.decodeByteArray(
                              event.eventPicture, 0, event.eventPicture.size)
                        }
                      } else {
                        null
                      }
                }
                .value

        val imageModifier =
            Modifier.fillMaxWidth(2f / 3f)
                .height(Dimensions.EventCardImageHeight)
                .align(Alignment.TopStart)
                .clip(RoundedCornerShape(Dimensions.RoundedCornerLarge))

        if (bitmap == null) {
          Image(
              painter = painterResource(id = R.drawable.default_event_img),
              contentDescription = null,
              contentScale = ContentScale.Crop,
              modifier = imageModifier)
        } else {
          Image(
              bitmap = bitmap.asImageBitmap(),
              contentDescription = null,
              contentScale = ContentScale.Crop,
              modifier = imageModifier)
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
                    text = event.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White)
                Spacer(Modifier.height(Dimensions.SpacerSmall))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    content = {
                      Text(
                          text = event.date.format(DateTimeFormatter.ofPattern("dd/MMM/yyyy")),
                          style = MaterialTheme.typography.bodyLarge,
                          color = Color.White)
                      Spacer(Modifier.width(Dimensions.SpacerSmall))
                      Icon(
                          imageVector = Icons.Outlined.StarBorder,
                          contentDescription = null,
                          tint = Color.White,
                          modifier = Modifier.size(Dimensions.IconSizeSmall))
                      Spacer(Modifier.width(Dimensions.SpacerSmall))
                      Text(
                          text = event.date.format(DateTimeFormatter.ofPattern("hh:mm")),
                          style = MaterialTheme.typography.bodyLarge,
                          color = Color.White)
                    })
              }
            }
      }

      Spacer(Modifier.height(Dimensions.SpacerMedium))

      Text(
          text = event.description ?: "No description available",
          style = MaterialTheme.typography.bodyMedium)

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
                width = Dimensions.EventCardButtonWidth) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = "Chat",
                        modifier = Modifier.size(Dimensions.IconSizeMedium))
                    Spacer(Modifier.width(Dimensions.SpacerSmall))
                    Text("Chat", style = MaterialTheme.typography.labelLarge)
                  }
                }

            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                  imageVector = Icons.Outlined.Person,
                  contentDescription = "Participants",
                  modifier = Modifier.size(Dimensions.IconSizeMedium))
              Spacer(Modifier.width(Dimensions.SpacerSmall))
              Text(
                  text = "${event.participants.size} people going",
                  style = MaterialTheme.typography.bodyMedium)
            }

            LiquidButton(
                onClick = onToggleEventParticipation,
                height = Dimensions.EventCardButtonHeight,
                width = Dimensions.EventCardButtonWidth) {
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

      if (isMapScreen) {
        Spacer(Modifier.height(100.dp))
      }
    }
  }
}
