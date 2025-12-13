package com.android.universe.ui.event

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.android.universe.model.location.Location
import com.android.universe.model.tag.Tag
import com.android.universe.ui.common.TagRow
import com.android.universe.ui.components.CategoryItemDefaults
import com.android.universe.ui.components.LiquidSearchBar
import com.android.universe.ui.components.LiquidSearchBarTestTags
import com.android.universe.ui.components.ScreenLayout
import com.android.universe.ui.navigation.NavigationBottomMenu
import com.android.universe.ui.navigation.NavigationScreens
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
 * `LazyColumn` of `EventCard` composables. The list of events is fetched from the `viewModel`.
 *
 * @param onTabSelected A callback function invoked when a tab in the bottom navigation menu is
 *   selected.
 * @param uid The unique identifier for the current user. Used to load user-specific events.
 * @param viewModel The [EventViewModel] that provides the state for the screen, including the list
 *   of events. Defaults to a ViewModel instance provided by `viewModel()`.
 * @param onChatNavigate A callback function invoked when the chat button on an event card is
 *   clicked, with the event ID and title as parameters.
 * @param onCardClick A callback function invoked when an event card is clicked, with the event ID
 *   and location as parameters.
 * @param onEditButtonClick A callback invoked when the user presses the "Edit" button on an event.
 * @param navController The NavHostController for navigation actions.
 */
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun EventScreen(
    onTabSelected: (Tab) -> Unit = {},
    uid: String = "",
    viewModel: EventViewModel = viewModel(),
    onChatNavigate: (eventId: String, eventTitle: String) -> Unit = { _, _ -> },
    onCardClick: (eventId: String, eventLocation: Location) -> Unit = { _, _ -> },
    onEditButtonClick: (uid: String, location: Location) -> Unit = { _, _ -> },
    navController: NavHostController = rememberNavController()
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

  LaunchedEffect(navController) {
    navController.currentBackStackEntryFlow.collect { entry ->
      if (entry.destination.route == NavigationScreens.Event.route) {
        viewModel.loadEvents()
      }
    }
  }
  val events by viewModel.filteredEvents.collectAsState()
  val focusManager = LocalFocusManager.current
  val allCats = Tag.tagFromEachCategory.toList()
  val categories by viewModel.categories.collectAsState()

  ScreenLayout(
      modifier = Modifier.testTag(NavigationTestTags.EVENT_SCREEN),
      bottomBar = { NavigationBottomMenu(Tab.Event, onTabSelected) }) { paddingValues ->
        Column(
            modifier =
                Modifier.fillMaxSize().padding(horizontal = PaddingMedium).clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }) {
                      focusManager.clearFocus()
                    }) {
              LiquidSearchBar(
                  query = viewModel.searchQuery.collectAsState().value,
                  onQueryChange = viewModel::updateSearchQuery,
                  modifier =
                      Modifier.padding(
                              top = paddingValues.calculateTopPadding(),
                              bottom = PaddingMedium,
                              start = PaddingMedium,
                              end = PaddingMedium)
                          .testTag(LiquidSearchBarTestTags.SEARCH_BAR))
              Box(modifier = Modifier.fillMaxWidth()) {
                TagRow(
                    allCats,
                    modifierBox = Modifier.fillMaxWidth(),
                    heightTag = CategoryItemDefaults.HEIGHT_CAT,
                    widthTag = CategoryItemDefaults.WIDTH_CAT,
                    isSelected = { cat -> categories.contains(cat.category) },
                    onTagSelect = { cat -> viewModel.selectCategory(cat.category) },
                    onTagReSelect = { cat -> viewModel.deselectCategory(cat.category) },
                    fadeWidth = (CategoryItemDefaults.HEIGHT_CAT * 0.5f).dp,
                    isCategory = true)
              }
              Spacer(modifier = Modifier.height(PaddingMedium))

              LazyColumn(
                  modifier = Modifier.fillMaxSize().testTag(EventScreenTestTags.EVENTS_LIST),
                  verticalArrangement = Arrangement.spacedBy(PaddingMedium)) {
                    items(events) { event ->
                      EventCard(
                          event = event,
                          viewModel = viewModel,
                          onChatNavigate = onChatNavigate,
                          onCardClick = onCardClick,
                          onEditButtonClick = onEditButtonClick)
                    }
                    item {
                      Spacer(Modifier.height(height = paddingValues.calculateBottomPadding()))
                    }
                  }
            }
      }
}
