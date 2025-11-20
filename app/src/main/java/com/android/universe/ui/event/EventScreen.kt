package com.android.universe.ui.event

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.universe.R
import com.android.universe.di.DefaultDP
import com.android.universe.model.event.EventRepositoryProvider
import com.android.universe.ui.navigation.NavigationTestTags
import com.android.universe.ui.navigation.Tab
import com.android.universe.ui.search.SearchBar
import com.android.universe.ui.search.SearchTestTags
import com.android.universe.ui.theme.Dimensions
import com.android.universe.ui.theme.Dimensions.PaddingLarge
import com.android.universe.ui.theme.Dimensions.PaddingMedium
import com.android.universe.ui.theme.Dimensions.PaddingSmall
import com.android.universe.ui.theme.UniverseTheme
import kotlinx.coroutines.withContext

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
  // Icon of an Image
  const val DEFAULT_EVENT_IMAGE = "default_event_image"

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
 * The main screen for displaying a list of events.
 *
 * This composable function sets up a `Scaffold` with a bottom navigation bar and displays a
 * `LazyColumn` of `EventCard` composables. The list of events is fetched from the `viewModel`.
 *
 * @param onTabSelected A callback function invoked when a tab in the bottom navigation menu is
 *   selected.
 * @param viewModel The [EventViewModel] that provides the state for the screen, including the list
 *   of events. Defaults to a ViewModel instance provided by `viewModel()`.
 */
@Composable
fun EventScreen(
    onTabSelected: (Tab) -> Unit = {},
    uid: String = "",
    viewModel: EventViewModel = viewModel()
) {
  val context = LocalContext.current
  LaunchedEffect(uid) {
    if (viewModel.storedUid != uid) {
      viewModel.storedUid = uid
      viewModel.loadEvents()
    }
  }
  val error by viewModel.uiState.collectAsState()

  LaunchedEffect(error.errormsg) {
    if (error.errormsg != null) {
      viewModel.setErrorMsg(null)
      Toast.makeText(context, error.errormsg, Toast.LENGTH_SHORT).show()
    }
  }
  val events by viewModel.filteredEvents.collectAsState()
  val focusManager = LocalFocusManager.current

  Column(
      modifier =
          Modifier.fillMaxSize()
              .padding(horizontal = PaddingMedium)
              .testTag(NavigationTestTags.EVENT_SCREEN)
              .clickable(
                  indication = null, interactionSource = remember { MutableInteractionSource() }) {
                    focusManager.clearFocus()
                  }) {
        SearchBar(
            query = viewModel.searchQuery.collectAsState().value,
            onQueryChange = viewModel::updateSearchQuery,
            modifier = Modifier.padding(PaddingMedium).testTag(SearchTestTags.SEARCH_BAR))

        LazyColumn(
            modifier = Modifier.fillMaxSize().testTag(EventScreenTestTags.EVENTS_LIST),
            verticalArrangement = Arrangement.spacedBy(PaddingMedium)) {
              items(events) { event ->
                EventCard(
                    title = event.title,
                    description = event.description,
                    date = event.date,
                    tags = event.tags,
                    creator = event.creator,
                    participants = event.participants,
                    onJoin = viewModel::joinOrLeaveEvent,
                    index = event.index,
                    joined = event.joined,
                    eventImage = event.eventPicture)
              }
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
 * @param onJoin A callback function invoked when the "Join In" button is clicked.
 * @param joined Whether the current user has joined the event.
 * @param index The index of the event in the list of events of the viewmodel
 * @param eventImage the image of the event.
 */
@Composable
fun EventCard(
    title: String,
    description: String,
    date: String,
    tags: List<String>,
    creator: String,
    participants: Int,
    onJoin: (Int) -> Unit = {},
    joined: Boolean = false,
    index: Int = 0,
    eventImage: ByteArray? = null
) {
  Card(
      modifier =
          Modifier.fillMaxWidth().padding(PaddingMedium).testTag(EventScreenTestTags.EVENT_CARD),
      shape = RoundedCornerShape(Dimensions.RoundedCorner),
      elevation = CardDefaults.cardElevation(Dimensions.ElevationCard)) {
        val bitmap =
            produceState<Bitmap?>(initialValue = null, eventImage) {
                  value =
                      if (eventImage != null) {
                        withContext(DefaultDP.io) {
                          BitmapFactory.decodeByteArray(eventImage, 0, eventImage.size)
                        }
                      } else {
                        null
                      }
                }
                .value
        Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
          // Image with overlay
          Box(modifier = Modifier.height(104.dp).fillMaxWidth()) {
            if (bitmap == null) {
              Image(
                  painter = painterResource(id = R.drawable.default_event_img),
                  contentDescription = null,
                  contentScale = ContentScale.Crop,
                  modifier =
                      Modifier.fillMaxSize().testTag(EventScreenTestTags.DEFAULT_EVENT_IMAGE))
            } else {
              Image(
                  bitmap = bitmap.asImageBitmap(),
                  contentDescription = null,
                  contentScale = ContentScale.Crop,
                  modifier = Modifier.fillMaxSize().testTag(EventScreenTestTags.EVENT_IMAGE))
            }
            Box(
                modifier =
                    Modifier.align(Alignment.TopEnd)
                        .padding(PaddingMedium)
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(Dimensions.RoundedCorner))
                        .padding(horizontal = PaddingMedium, vertical = PaddingSmall)
                        .testTag(EventScreenTestTags.EVENT_DATE)) {
                  Text(
                      text = date,
                      color = MaterialTheme.colorScheme.onSurface,
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = FontWeight.Bold)
                }

            Column(
                modifier =
                    Modifier.align(Alignment.TopStart)
                        .padding(PaddingMedium)
                        .testTag(EventScreenTestTags.EVENT_TAGS_COLUMN),
                verticalArrangement = Arrangement.spacedBy(PaddingMedium)) {
                  tags.forEach { tag -> TagCard(tag, EventScreenTestTags.EVENT_TAG) }
                }
          }

          // Title
          Text(
              text = title,
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onSurface,
              modifier =
                  Modifier.padding(horizontal = PaddingLarge, vertical = PaddingMedium)
                      .testTag(EventScreenTestTags.EVENT_TITLE),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis)

          // Description
          Text(
              text = description,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurface,
              modifier =
                  Modifier.padding(horizontal = PaddingLarge)
                      .testTag(EventScreenTestTags.EVENT_DESCRIPTION),
              maxLines = 3,
              overflow = TextOverflow.Ellipsis)

          // Creator & participants
          Row(
              modifier =
                  Modifier.fillMaxWidth()
                      .padding(PaddingLarge, PaddingMedium)
                      .testTag(EventScreenTestTags.EVENT_CREATOR_PARTICIPANTS),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$participants joined • by $creator",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface)

                Button(
                    onClick = { onJoin(index) },
                    shape = RoundedCornerShape(Dimensions.RoundedCorner),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor =
                                if (joined) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.primary),
                    modifier = Modifier.testTag(EventScreenTestTags.EVENT_JOIN_BUTTON)) {
                      Text(
                          text =
                              if (joined) {
                                "Leave"
                              } else "Join In",
                          color = MaterialTheme.colorScheme.onPrimary)
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
          Modifier.background(
                  MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                  RoundedCornerShape(Dimensions.RoundedCorner))
              .padding(horizontal = PaddingMedium, vertical = PaddingSmall)
              .testTag(testTag)) {
        Text(
            text = tag,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
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
  // Grab a single sample event
  val event = EventRepositoryProvider.sampleEvents.first()

  UniverseTheme {
    EventCard(
        title = event.title,
        description = event.description ?: "",
        date = event.date.toLocalDate().toString(),
        tags = event.tags.map { it.name },
        creator = event.creator,
        participants = event.participants.size)
  }
}
