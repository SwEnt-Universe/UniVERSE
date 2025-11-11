package com.android.universe.ui.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.android.universe.model.event.Event

@Composable
fun EventInfoPopup(event: Event, onDismiss: () -> Unit) {
  Box(
      modifier =
          Modifier.fillMaxSize()
              .background(Color.Black.copy(alpha = 0.4f))
              .clickable(onClick = onDismiss),
      contentAlignment = Alignment.BottomCenter) {
        AnimatedVisibility(
            visible = true,
            enter =
                slideInVertically(
                    initialOffsetY = { it },
                ),
            exit = slideOutVertically(targetOffsetY = { it })) {
              Card(
                  modifier = Modifier.padding(16.dp).fillMaxWidth(),
                  shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                  elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                      Text(event.title, style = MaterialTheme.typography.titleLarge)
                      Spacer(Modifier.height(8.dp))
                      Text(event.description ?: "No description available")
                      Spacer(Modifier.height(8.dp))
                      Text("Location: ${event.location.latitude}, ${event.location.longitude}")
                      Spacer(Modifier.height(12.dp))
                      Button(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                        Text("Close")
                      }
                    }
                  }
            }
      }
}
