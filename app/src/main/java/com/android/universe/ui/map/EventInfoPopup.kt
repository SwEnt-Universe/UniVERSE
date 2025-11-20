package com.android.universe.ui.map

import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.android.universe.di.DefaultDP
import com.android.universe.model.event.Event
import com.android.universe.ui.common.EventContentLayout
import com.android.universe.ui.components.LiquidBottomSheet
import com.android.universe.ui.theme.Dimensions
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventInfoPopup(
    event: Event,
    isUserParticipant: Boolean,
    onDismiss: () -> Unit,
    onToggleEventParticipation: () -> Unit
) {
  val bitmap =
      produceState<Bitmap?>(initialValue = null, event.eventPicture) {
            value =
                if (event.eventPicture != null) {
                  withContext(DefaultDP.io) {
                    BitmapFactory.decodeByteArray(event.eventPicture, 0, event.eventPicture.size)
                  }
                } else {
                  null
                }
          }
          .value

  Box(
      modifier =
          Modifier.fillMaxSize()
              .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
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
                        title = event.title,
                        description = event.description,
                        date = event.date,
                        tags = event.tags.toList(),
                        participants = event.participants.size,
                        eventBitmap = bitmap,
                        isUserParticipant = isUserParticipant,
                        onToggleEventParticipation = onToggleEventParticipation,
                        onChatClick = {},
                        onLocationClick = null,
                        bottomSpacing = 100.dp)
                  }
            }
      }
}
