package com.android.universe.ui.event

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.universe.ui.common.EventCard
import com.android.universe.ui.navigation.NavigationBottomMenu
import com.android.universe.ui.navigation.NavigationTestTags
import com.android.universe.ui.navigation.Tab
import com.android.universe.ui.theme.Dimensions.PaddingMedium

object EventScreenTestTags {
  // LazyColumn containing all events
  const val EVENTS_LIST = "events_list"
}

/**
 * The main screen for displaying a list of events.
 *
 * This composable function sets up a `Scaffold` with a bottom navigation bar and displays a
 * `LazyColumn` of `LiquidEventCard` composables. The list of events is fetched from the
 * `viewModel`.
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
  val events by viewModel.eventsState.collectAsState()
  Scaffold(
      modifier = Modifier.testTag(NavigationTestTags.EVENT_SCREEN),
      bottomBar = { NavigationBottomMenu(selectedTab = Tab.Event, onTabSelected = onTabSelected) },
  ) { paddingValues ->
    LazyColumn(
        modifier =
            Modifier.fillMaxSize().padding(paddingValues).testTag(EventScreenTestTags.EVENTS_LIST),
        contentPadding = PaddingValues(PaddingMedium),
        verticalArrangement = Arrangement.spacedBy(PaddingMedium)) {
          items(events) { event ->
            EventCard(
                title = event.title,
                description = event.description,
                date = event.date,
                tags = emptyList(),
                participants = event.participants,
                eventImage = event.eventPicture,
                isUserParticipant = event.joined,
                onToggleEventParticipation = { viewModel.joinOrLeaveEvent(event.index) },
                onChatClick = { /* TODO: Implement chat navigation */ },
                onLocationClick = { /* TODO: Implement map navigation */ },
                isMapScreen = false,
                modifier = Modifier.fillMaxWidth())
          }
        }
  }
}
