package com.android.universe.ui.event

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.universe.R
import com.android.universe.model.event.EventRepositoryProvider

@Composable
fun EventScreen(viewModel: EventViewModel = viewModel()) {
  val events by viewModel.eventsState.collectAsState()

  LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(8.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(events) { event ->
          EventCard(
              title = event.title,
              description = event.description,
              date = event.date,
              tags = event.tags,
              creator = event.creator,
              participants = event.participants)
        }
      }
}

@Composable
fun EventCard(
    title: String,
    description: String,
    date: String,
    tags: List<String>,
    creator: String,
    participants: Int
) {
  Card(
      modifier = Modifier.fillMaxWidth().padding(8.dp),
      shape = RoundedCornerShape(12.dp),
      elevation = CardDefaults.cardElevation(50.dp)) {
        Column(modifier = Modifier.background(Color.White)) {
          // Image with overlay
          Box(modifier = Modifier.height(100.dp).fillMaxWidth()) {
            Image(
                painter = painterResource(id = R.drawable.default_event_img),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize())

            Box(
                modifier =
                    Modifier.align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(Color.Gray.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 6.dp, vertical = 4.dp)) {
                  Text(
                      text = date,
                      color = Color.Black,
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = FontWeight.Bold)
                }

            Column(
                modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)) {
                  tags.forEach { tag -> TagCard(tag) }
                }
          }

          // Title
          Text(
              text = title,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = Color.Black,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis)

          // Description
          Text(
              text = description,
              style = MaterialTheme.typography.bodyMedium,
              color = Color.DarkGray,
              modifier = Modifier.padding(horizontal = 12.dp),
              maxLines = 3,
              overflow = TextOverflow.Ellipsis)

          // Creator & participants
          Row(
              modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$participants joined • by $creator",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray)

                Button(
                    onClick = {},
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)) {
                      Text(text = "Join In", color = Color.White)
                    }
              }
        }
      }
}

@Composable
fun TagCard(tag: String) {
  Box(
      modifier =
          Modifier.background(Color.Gray.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
              .padding(horizontal = 6.dp, vertical = 4.dp)) {
        Text(
            text = tag,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Black,
            fontWeight = FontWeight.Bold)
      }
}

@Preview(showBackground = true)
@Composable
fun EventCardPreview() {
  val previewViewModel = EventViewModel(EventRepositoryProvider.repository)
  EventScreen(previewViewModel)
}
