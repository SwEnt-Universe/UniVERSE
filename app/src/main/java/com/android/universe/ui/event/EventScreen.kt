package com.android.universe.ui.event

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.universe.ui.navigation.NavigationBottomMenu
import com.android.universe.ui.navigation.NavigationTestTags
import com.android.universe.ui.navigation.Tab
import com.android.universe.ui.search.SearchBar
import com.android.universe.ui.search.SearchTestTags
import com.android.universe.ui.theme.Dimensions.PaddingMedium

object EventScreenTestTags {
  // LazyColumn containing all events
  const val EVENTS_LIST = "events_list"
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
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
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

  Scaffold(
      modifier = Modifier.testTag(NavigationTestTags.EVENT_SCREEN),
      contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
      bottomBar = { NavigationBottomMenu(Tab.Event, onTabSelected) }) { _ ->
        Column(
            modifier =
                Modifier.fillMaxSize().padding(horizontal = PaddingMedium).clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }) {
                      focusManager.clearFocus()
                    }) {
              SearchBar(
                  query = viewModel.searchQuery.collectAsState().value,
                  onQueryChange = viewModel::updateSearchQuery,
                  modifier = Modifier.padding(PaddingMedium).testTag(SearchTestTags.SEARCH_BAR))

              LazyColumn(
                  modifier = Modifier.fillMaxSize().testTag(EventScreenTestTags.EVENTS_LIST),
                  verticalArrangement = Arrangement.spacedBy(PaddingMedium)) {
                    items(events) { event -> EventCard(event = event, viewModel = viewModel) }
                  }
            }
      }
}
