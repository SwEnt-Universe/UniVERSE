package com.android.universe.ui.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.android.universe.model.event.Event
import com.android.universe.ui.theme.Dimensions

@Composable
fun EventInfoPopup(
    modifier: Modifier = Modifier,
    event: Event,
    isUserParticipant: Boolean,
    onDismiss: () -> Unit,
    onToggleEventParticipation: () -> Unit
) {
  Box(
      modifier =
          modifier.fillMaxSize()
              .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f))
              .clickable(onClick = onDismiss)
              .testTag(MapScreenTestTags.EVENT_INFO_POPUP),
      contentAlignment = Alignment.BottomCenter) {
        AnimatedVisibility(
            visible = true,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })) {
              Card(
                  modifier = Modifier.fillMaxWidth(),
                  shape =
                      RoundedCornerShape(
                          topStart = Dimensions.RoundedCorner,
                          topEnd = Dimensions.RoundedCorner,
                          bottomStart = 0.dp,
                          bottomEnd = 0.dp),
                  colors =
                      CardDefaults.cardColors(
                          containerColor = MaterialTheme.colorScheme.surface,
                          contentColor = MaterialTheme.colorScheme.onSurface),
                  elevation = CardDefaults.cardElevation(Dimensions.ElevationCard)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(Dimensions.PaddingLarge)) {
                      Row(
                          modifier = Modifier.fillMaxWidth(),
                          horizontalArrangement = Arrangement.SpaceBetween,
                          verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = event.title,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f))

                            Button(
                                onClick = { onToggleEventParticipation() },
                                modifier =
                                    Modifier.testTag(MapScreenTestTags.EVENT_JOIN_LEAVE_BUTTON),
                                colors =
                                    ButtonDefaults.buttonColors(
                                        containerColor =
                                            if (isUserParticipant) {
                                              MaterialTheme.colorScheme.error
                                            } else {
                                              MaterialTheme.colorScheme.primary
                                            },
                                        contentColor =
                                            if (isUserParticipant) {
                                              MaterialTheme.colorScheme.onError
                                            } else {
                                              MaterialTheme.colorScheme.onPrimary
                                            }),
                                contentPadding =
                                    PaddingValues(
                                        horizontal = Dimensions.PaddingMedium,
                                        vertical = Dimensions.PaddingSmall)) {
                                  Text(
                                      text = if (isUserParticipant) "Leave" else "Join",
                                      style = MaterialTheme.typography.labelMedium)
                                }
                          }

                      Spacer(Modifier.height(Dimensions.SpacerMedium))

                      Text(
                          text = event.description ?: "No description available",
                          style = MaterialTheme.typography.bodyMedium)

                      Spacer(Modifier.height(Dimensions.SpacerMedium))

                      Text(
                          text =
                              "Location: ${event.location.latitude}, ${event.location.longitude}",
                          style = MaterialTheme.typography.bodySmall)

                      Spacer(Modifier.height(Dimensions.SpacerMedium))

                      Text(
                          text = "Participants: ${event.participants.size}",
                          style = MaterialTheme.typography.bodySmall,
                          color = MaterialTheme.colorScheme.onSurfaceVariant)

                      Spacer(Modifier.height(Dimensions.SpacerLarge))

                      Button(
                          onClick = onDismiss,
                          modifier = Modifier.align(Alignment.CenterHorizontally),
                          colors =
                              ButtonDefaults.buttonColors(
                                  containerColor = MaterialTheme.colorScheme.primary,
                                  contentColor = MaterialTheme.colorScheme.onPrimary)) {
                            Text("Close")
                          }
                    }
                  }
            }
      }
}
