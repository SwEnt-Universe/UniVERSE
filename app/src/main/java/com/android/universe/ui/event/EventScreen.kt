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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.universe.R
import com.android.universe.model.event.EventRepositoryProvider

object EventScreenTestTags {
  // LazyColumn containing all events
  const val EVENTS_LIST = "events_list"

  // Individual EventCard
  const val EVENT_CARD = "event_card"

  // Title
  const val EVENT_TITLE = "event_title"

  // Description
  const val EVENT_DESCRIPTION = "event_description"

  // Date overlay on image
  const val EVENT_DATE = "event_date"

  // Image
  const val EVENT_IMAGE = "event_image"

  // Tags container
  const val EVENT_TAGS_COLUMN = "event_tags_column"

  // Individual TagCard
  const val EVENT_TAG = "event_tag"

  // Creator and participants row
  const val EVENT_CREATOR_PARTICIPANTS = "event_creator_participants"

  // Join In button
  const val EVENT_JOIN_BUTTON = "event_join_button"
}

/**
 * Displays a scrollable list of events using a LazyColumn.
 *
 * Each event in the ViewModel's [eventsState] is represented by an [EventCard]
 *
 * @param viewModel The [EventViewModel] providing the list of events. Defaults to a Compose-managed
 *   ViewModel instance.
 */
@Composable
fun EventScreen(viewModel: EventViewModel = viewModel()) {
  val events by viewModel.eventsState.collectAsState()

  LazyColumn(
      modifier = Modifier.fillMaxSize().testTag(EventScreenTestTags.EVENTS_LIST),
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

/**
 * Displays an individual event card with image, title, description, date, tags, creator and
 * participant number.
 *
 * The card includes:
 * - A background image with a floating date overlay.
 * - A column of floating tags at the top left.
 * - Event title and description below the image.
 * - A row at the bottom displaying participant count and creator, along with a "Join In" button.
 *
 * @param title The title of the event.
 * @param description The description of the event.
 * @param date The formatted date string of the event.
 * @param tags A list of up to three tag strings associated with the event.
 * @param creator The full name of the user who created the event.
 * @param participants The number of participants who joined the event.
 */
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
      modifier = Modifier.fillMaxWidth().padding(8.dp).testTag(EventScreenTestTags.EVENT_CARD),
      shape = RoundedCornerShape(12.dp),
      elevation = CardDefaults.cardElevation(50.dp)) {
        Column(modifier = Modifier.background(Color.White)) {
          // Image with overlay
          Box(modifier = Modifier.height(100.dp).fillMaxWidth()) {
            Image(
                painter = painterResource(id = R.drawable.default_event_img),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().testTag(EventScreenTestTags.EVENT_IMAGE))

            Box(
                modifier =
                    Modifier.align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(Color.Gray.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                        .testTag(EventScreenTestTags.EVENT_DATE)) {
                  Text(
                      text = date,
                      color = Color.Black,
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = FontWeight.Bold)
                }

            Column(
                modifier =
                    Modifier.align(Alignment.TopStart)
                        .padding(8.dp)
                        .testTag(EventScreenTestTags.EVENT_TAGS_COLUMN),
                verticalArrangement = Arrangement.spacedBy(6.dp)) {
                  tags.forEach { tag -> TagCard(tag, EventScreenTestTags.EVENT_TAG) }
                }
          }

          // Title
          Text(
              text = title,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = Color.Black,
              modifier =
                  Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                      .testTag(EventScreenTestTags.EVENT_TITLE),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis)

          // Description
          Text(
              text = description,
              style = MaterialTheme.typography.bodyMedium,
              color = Color.DarkGray,
              modifier =
                  Modifier.padding(horizontal = 12.dp)
                      .testTag(EventScreenTestTags.EVENT_DESCRIPTION),
              maxLines = 3,
              overflow = TextOverflow.Ellipsis)

          // Creator & participants
          Row(
              modifier =
                  Modifier.fillMaxWidth()
                      .padding(horizontal = 12.dp, vertical = 8.dp)
                      .testTag(EventScreenTestTags.EVENT_CREATOR_PARTICIPANTS),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$participants joined • by $creator",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray)

                Button(
                    onClick = {},
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    modifier = Modifier.testTag(EventScreenTestTags.EVENT_JOIN_BUTTON)) {
                      Text(text = "Join In", color = Color.White)
                    }
              }
        }
      }
}

/**
 * Displays a single tag as a small, rounded box with text.
 *
 * Used inside [EventCard] to display event tags.
 *
 * @param tag The text of the tag to display.
 */
@Composable
fun TagCard(tag: String, testTag: String) {
  Box(
      modifier =
          Modifier.background(Color.Gray.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
              .padding(horizontal = 6.dp, vertical = 4.dp)
              .testTag(testTag)) {
        Text(
            text = tag,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Black,
            fontWeight = FontWeight.Bold)
      }
}

/**
 * Preview composable for the EventScreen.
 *
 * This preview uses the [EventViewModel] from [EventRepositoryProvider] to display a list of events
 * for design inspection in Android Studio.
 */
@Preview(showBackground = true)
@Composable
fun EventCardPreview() {
  val previewViewModel = EventViewModel(EventRepositoryProvider.repository)
  EventScreen(previewViewModel)
}
