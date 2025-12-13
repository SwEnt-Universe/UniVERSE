package com.android.universe.ui.profile

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.android.universe.model.event.Event
import com.android.universe.model.location.Location
import com.android.universe.model.user.UserProfile
import com.android.universe.ui.common.ProfileContentLayout
import com.android.universe.ui.components.LiquidBox
import com.android.universe.ui.components.ScreenLayout
import com.android.universe.ui.event.EventCard
import com.android.universe.ui.event.EventUIState
import com.android.universe.ui.event.EventViewModel
import com.android.universe.ui.navigation.NavigationBottomMenu
import com.android.universe.ui.navigation.NavigationScreens
import com.android.universe.ui.navigation.NavigationTestTags
import com.android.universe.ui.navigation.Tab
import com.android.universe.ui.theme.Dimensions
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

/** Define all the tags for the UserProfile screen. Tags will be used to test the screen. */
object UserProfileScreenTestTags {
  const val PROFILE_EVENT_LIST = "profileEventList"

  fun getTabTestTag(index: Int): String {
    return "profileTab$index"
  }
}

/**
 * The main screen composable for displaying a User's Profile.
 *
 * This component acts as the controller for the profile UI. It implements a complex "Collapsing
 * Toolbar" pattern with Sticky Headers using a custom implementation rather than standard
 * NestedScrollConnection.
 *
 * @param uid The unique identifier of the user to display.
 * @param onTabSelected Callback invoked when a bottom navigation tab is selected.
 * @param onEditProfileClick Callback invoked when the user clicks the "Edit Profile" button.
 * @param onChatNavigate Callback invoked when a chat button is clicked.
 * @param onCardClick Callback invoked when a card is clicked.
 * @param userProfileViewModel The ViewModel managing the user profile state (fetched via [uid]).
 * @param eventViewModel The ViewModel managing event data (History/Incoming).
 * @param onEditButtonClick Callback invoked when the edit button on an event is clicked.
 * @param navController The NavHostController for navigation actions.
 */
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun UserProfileScreen(
    uid: String,
    onTabSelected: (Tab) -> Unit = {},
    onEditProfileClick: (String) -> Unit = {},
    onChatNavigate: (eventId: String, eventTitle: String) -> Unit = { _, _ -> },
    onCardClick: (eventId: String, eventLocation: Location) -> Unit = { _, _ -> },
    userProfileViewModel: UserProfileViewModel = viewModel { UserProfileViewModel(uid) },
    eventViewModel: EventViewModel = viewModel(),
    onEditButtonClick: (uid: String, location: Location) -> Unit = { _, _ -> },
    navController: NavHostController = rememberNavController()
) {
  val userUIState by userProfileViewModel.userState.collectAsState()

  // State Initialization
  val density = LocalDensity.current
  val pagerState = rememberPagerState(pageCount = { 2 })
  val historyListState = rememberLazyListState()
  val incomingListState = rememberLazyListState()
  eventViewModel.storedUid = uid

  // Reload user profile when navigating back to this screen
  LaunchedEffect(navController) {
    navController.currentBackStackEntryFlow.collect { entry ->
      if (entry.destination.route == NavigationScreens.Profile.route) {
        userProfileViewModel.loadUser()
      }
    }
  }

  // Layout Measurements
  var profileContentHeightPx by remember { mutableFloatStateOf(0f) }
  var tabRowHeightPx by remember { mutableFloatStateOf(0f) }

  val elementSpacingDp = Dimensions.PaddingMedium
  val elementSpacingPx = with(density) { elementSpacingDp.toPx() }

  val profileContentHeightDp = with(density) { profileContentHeightPx.toDp() }
  val tabRowHeightDp = with(density) { tabRowHeightPx.toDp() }

  // The total distance the header can scroll before sticking (User Info Height + Gap).
  val totalCollapsibleHeightPx = profileContentHeightPx + elementSpacingPx

  // Since we have two separate LazyLists (History and Incoming), their scroll positions
  // are naturally independent. However, the sticky header (User Profile Info) is shared.
  // We must sync the scroll state of the hidden list to match the active list so that
  // when the user switches tabs, the header doesn't "jump".

  // Sync Logic: History -> Incoming
  ScrollSyncEffect(
      pagerState = pagerState,
      activeListState = historyListState,
      targetListState = incomingListState,
      activePageIndex = 0,
      totalCollapsibleHeightPx = totalCollapsibleHeightPx)

  // Sync Logic: Incoming -> History
  ScrollSyncEffect(
      pagerState = pagerState,
      activeListState = incomingListState,
      targetListState = historyListState,
      activePageIndex = 1,
      totalCollapsibleHeightPx = totalCollapsibleHeightPx)

  // Synchronization Logic
  val headerOffsetPx by remember {
    derivedStateOf {
      val currentListState =
          if (pagerState.currentPage == 0) historyListState else incomingListState
      val totalCollapsibleHeightPx = profileContentHeightPx + elementSpacingPx

      if (currentListState.firstVisibleItemIndex == 0) {
        val scrollOffset = currentListState.firstVisibleItemScrollOffset.toFloat()
        // Move header up by scroll amount, but don't move past the collapsible height
        -scrollOffset.coerceIn(0f, totalCollapsibleHeightPx)
      } else {
        // If we scrolled past index 0, the header is fully hidden (sticky state)
        -totalCollapsibleHeightPx
      }
    }
  }

  ScreenLayout(
      bottomBar = { NavigationBottomMenu(Tab.Profile, onTabSelected) },
      modifier = Modifier.testTag(NavigationTestTags.PROFILE_SCREEN)) { paddingValues ->
        val bottomPadding = paddingValues.calculateBottomPadding()
        val topPadding = paddingValues.calculateTopPadding()
        Box(modifier = Modifier.fillMaxSize()) {
          LiquidBox(modifier = Modifier.fillMaxSize(), shape = RoundedCornerShape(0.dp)) {
            Box(modifier = Modifier.fillMaxSize().padding(top = topPadding).clipToBounds()) {
              Box(modifier = Modifier.fillMaxSize()) {
                ProfileContentPager(
                    pagerState = pagerState,
                    historyListState = historyListState,
                    incomingListState = incomingListState,
                    historyEvents = userUIState.historyEvents,
                    incomingEvents = userUIState.incomingEvents,
                    eventViewModel = eventViewModel,
                    spacerHeightDp = profileContentHeightDp + elementSpacingDp,
                    clipPaddingDp = tabRowHeightDp,
                    listBottomPadding = bottomPadding,
                    onChatNavigate = onChatNavigate,
                    onCardClick = onCardClick,
                    onEditButtonClick = onEditButtonClick)

                ProfileHeaderOverlay(
                    headerOffsetPx = headerOffsetPx,
                    userProfile = userUIState.userProfile,
                    pagerState = pagerState,
                    gapHeightDp = elementSpacingDp,
                    onProfileHeightMeasured = { profileContentHeightPx = it },
                    onTabHeightMeasured = { tabRowHeightPx = it },
                    onEditProfileClick = { onEditProfileClick(uid) })
              }
            }
          }
        }
      }
}

/**
 * Helper Composable to synchronize scroll state between two lists. It listens to the active list's
 * scroll and updates the target list to match the header collapse state.
 *
 * @param pagerState The state object for the [HorizontalPager].
 * @param activeListState The scroll state for the active list.
 * @param targetListState The scroll state for the target list.
 * @param activePageIndex The index of the active list.
 */
@Composable
private fun ScrollSyncEffect(
    pagerState: PagerState,
    activeListState: LazyListState,
    targetListState: LazyListState,
    activePageIndex: Int,
    totalCollapsibleHeightPx: Float
) {
  LaunchedEffect(
      pagerState.currentPage,
      activeListState.firstVisibleItemScrollOffset,
      activeListState.firstVisibleItemIndex,
      totalCollapsibleHeightPx) {
        // Only run this effect if the Pager is currently on the active page for this source list
        if (pagerState.currentPage == activePageIndex && totalCollapsibleHeightPx > 0) {
          val scrollOffset = activeListState.firstVisibleItemScrollOffset
          val firstIndex = activeListState.firstVisibleItemIndex

          val collapseAmount =
              if (firstIndex == 0) scrollOffset.toFloat() else totalCollapsibleHeightPx

          val targetOffset =
              if (targetListState.firstVisibleItemIndex == 0)
                  targetListState.firstVisibleItemScrollOffset
              else totalCollapsibleHeightPx.toInt()

          if (collapseAmount >= totalCollapsibleHeightPx) {
            // Active header is fully collapsed: Ensure target is also at least fully collapsed
            if (targetOffset < totalCollapsibleHeightPx) {
              targetListState.scrollToItem(0, totalCollapsibleHeightPx.toInt())
            }
          } else {
            // Active header is partially visible: Target must match exactly
            if (targetOffset != collapseAmount.toInt()) {
              targetListState.scrollToItem(0, collapseAmount.toInt())
            }
          }
        }
      }
}

/**
 * Manages the horizontal paging between the different event lists (History vs Incoming).
 *
 * This composable determines which list state and data source to use based on the current page
 * index and delegates the rendering to [ProfileEventList].
 *
 * @param pagerState The state object for the [HorizontalPager].
 * @param historyListState The scroll state for the History list.
 * @param incomingListState The scroll state for the Incoming list.
 * @param historyEvents The list of past events.
 * @param incomingEvents The list of upcoming events.
 * @param eventViewModel The view model used by event cards.
 * @param spacerHeightDp The height of the transparent spacer at the top of the list (Profile
 *   Height + Gap).
 * @param clipPaddingDp The top padding applied to the list container to clip content behind the
 *   sticky tabs.
 * @param onEditButtonClick Callback invoked when the edit button on an event is clicked.
 */
@Composable
fun ProfileContentPager(
    pagerState: PagerState,
    historyListState: LazyListState,
    incomingListState: LazyListState,
    historyEvents: List<EventUIState>,
    incomingEvents: List<EventUIState>,
    eventViewModel: EventViewModel,
    spacerHeightDp: Dp,
    clipPaddingDp: Dp,
    listBottomPadding: Dp,
    onChatNavigate: (eventId: String, eventTitle: String) -> Unit = { _, _ -> },
    onCardClick: (eventId: String, eventLocation: Location) -> Unit = { _, _ -> },
    onEditButtonClick: (uid: String, location: Location) -> Unit = { _, _ -> }
) {
  HorizontalPager(
      state = pagerState, modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.Top) {
          page ->
        val isHistoryPage = page == 0
        val listState = if (isHistoryPage) historyListState else incomingListState
        val events = if (isHistoryPage) historyEvents else incomingEvents

        ProfileEventList(
            listState = listState,
            events = events,
            eventViewModel = eventViewModel,
            headerSpacerHeight = spacerHeightDp,
            topClipPadding = clipPaddingDp,
            bottomContentPadding = listBottomPadding,
            onChatNavigate = onChatNavigate,
            onCardClick = onCardClick,
            onEditButtonClick = onEditButtonClick)
      }
}

/**
 * Renders a specific list of events (e.g., History or Incoming) within a [LazyColumn].
 *
 * @param listState The scroll state for this specific list.
 * @param events The list of [Event] objects to display.
 * @param eventViewModel The view model for event interactions.
 * @param headerSpacerHeight The height of the invisible spacer (Index 0).
 * @param topClipPadding The top padding used to clip the scrolling content.$
 * @param bottomContentPadding The bottom padding to apply to the list content.
 * @param onChatNavigate Callback invoked when a chat button is clicked.
 * @param onCardClick Callback invoked when a card is clicked.
 * @param onEditButtonClick Callback invoked when the edit button on an event is clicked.
 */
@Composable
fun ProfileEventList(
    listState: LazyListState,
    events: List<EventUIState>,
    eventViewModel: EventViewModel,
    headerSpacerHeight: Dp,
    topClipPadding: Dp,
    bottomContentPadding: Dp,
    onChatNavigate: (eventId: String, eventTitle: String) -> Unit = { _, _ -> },
    onCardClick: (eventId: String, eventLocation: Location) -> Unit = { _, _ -> },
    onEditButtonClick: (uid: String, location: Location) -> Unit = { _, _ -> }
) {
  val density = LocalDensity.current
  val configuration = LocalConfiguration.current

  // If we start with 0, a short list cannot accept the scroll offset from the other tab
  // during the initial sync, causing the header to expand (jump).
  val screenHeight = configuration.screenHeightDp.dp
  var footerHeight by remember { mutableStateOf(screenHeight) }

  // We want the user to be able to scroll until the sticky header hits the top.
  // If the list content is too short, the LazyColumn won't scroll that far.
  // We calculate exactly how much extra space (footer) is needed to allow this scroll.
  LaunchedEffect(events, listState) {
    snapshotFlow { listState.layoutInfo }
        .collect { layoutInfo ->
          val visibleItems = layoutInfo.visibleItemsInfo
          val viewportHeight = layoutInfo.viewportSize.height

          if (visibleItems.isNotEmpty() && events.isNotEmpty()) {
            val lastEventIndex = events.size
            val isLastItemVisible = visibleItems.any { it.index == lastEventIndex }

            // Only calculate footer if the end of the list is visible (i.e., it's short)
            if (isLastItemVisible) {
              val eventsHeightPx =
                  visibleItems.filter { it.index > 0 && it.index <= events.size }.sumOf { it.size }

              val bottomPaddingPx = with(density) { bottomContentPadding.toPx() }

              // Formula: Viewport - Events - BottomPadding
              // This ensures that (Events + Footer) fills the Viewport exactly,
              // leaving the HeaderSpacer as the only scrollable distance.
              val neededPx = (viewportHeight - eventsHeightPx - bottomPaddingPx).coerceAtLeast(0f)
              footerHeight = with(density) { neededPx.toDp() }
            } else {
              // List is naturally long enough, no artificial footer needed.
              footerHeight = 0.dp
            }
          }
        }
  }

  LazyColumn(
      state = listState,
      contentPadding = PaddingValues(bottom = bottomContentPadding),
      modifier =
          Modifier.fillMaxSize()
              .padding(top = topClipPadding)
              .testTag(UserProfileScreenTestTags.PROFILE_EVENT_LIST)) {
        item(key = "header_spacer") {
          Spacer(
              modifier =
                  Modifier.fillMaxWidth().height(headerSpacerHeight).background(Color.Transparent))
        }
        items(events, key = { it.id }) { eventUIState ->
          Box(
              modifier =
                  Modifier.padding(
                      horizontal = Dimensions.PaddingMedium, vertical = Dimensions.PaddingSmall)) {
                EventCard(
                    event = eventUIState,
                    onChatNavigate = onChatNavigate,
                    onCardClick = onCardClick,
                    viewModel = eventViewModel,
                    onEditButtonClick = onEditButtonClick)
              }
        }
        // Ensures the list is tall enough to scroll the header away.
        item(key = "safety_spacer") { Spacer(modifier = Modifier.height(footerHeight)) }
      }
}

/**
 * The floating overlay component containing the User Profile Info and the Sticky Tabs.
 *
 * This component sits on top of the list (Z-axis). It moves vertically based on the
 * `headerOffsetPx`. It creates the "Sticky" effect by stopping its movement once the Profile Info
 * is scrolled away, leaving only the Tabs visible.
 *
 * @param headerOffsetPx The current vertical offset (in pixels) calculated by the parent
 *   controller.
 * @param userProfile The user data model.
 * @param pagerState The state of the pager (passed to the tabs).
 * @param gapHeightDp The spacing between the profile info and the tabs.
 * @param onProfileHeightMeasured Callback to report the height of the profile info block to the
 *   parent.
 * @param onTabHeightMeasured Callback to report the height of the tab row to the parent.
 * @param onEditProfileClick Action to perform when settings/edit is clicked.
 */
@Composable
fun ProfileHeaderOverlay(
    headerOffsetPx: Float,
    userProfile: UserProfile,
    pagerState: PagerState,
    gapHeightDp: Dp,
    onProfileHeightMeasured: (Float) -> Unit,
    onTabHeightMeasured: (Float) -> Unit,
    onEditProfileClick: () -> Unit
) {
  Column(
      modifier =
          Modifier.fillMaxWidth()
              .offset { IntOffset(x = 0, y = headerOffsetPx.roundToInt()) }
              .pointerInput(Unit) { detectHorizontalDragGestures { _, _ -> } }) {
        Box(
            modifier =
                Modifier.fillMaxWidth().onGloballyPositioned { coordinates ->
                  onProfileHeightMeasured(coordinates.size.height.toFloat())
                }) {
              ProfileContentLayout(
                  modifier = Modifier,
                  userProfile = userProfile,
                  onToggleFollowing = {},
                  onSettingsClick = onEditProfileClick)
            }

        Spacer(modifier = Modifier.height(gapHeightDp))

        Box(
            modifier =
                Modifier.fillMaxWidth().onGloballyPositioned { coordinates ->
                  onTabHeightMeasured(coordinates.size.height.toFloat())
                }) {
              ProfileTabRow(pagerState = pagerState, titles = listOf("History", "Incoming"))
            }
      }
}

/**
 * A wrapper around Material 3's [PrimaryTabRow] customized for the Profile Screen.
 *
 * It sets up the tabs (History/Incoming) and handles the click animations to switch pages.
 *
 * @param pagerState The state of the horizontal pager to synchronize tab selection.
 * @param titles The list of titles to display in the tabs.
 */
@Composable
fun ProfileTabRow(pagerState: PagerState, titles: List<String>) {
  val scope = rememberCoroutineScope()

  PrimaryTabRow(
      selectedTabIndex = pagerState.currentPage,
      containerColor = Color.Transparent,
      contentColor = MaterialTheme.colorScheme.onSurface,
      divider = {},
      indicator = {
        TabRowDefaults.SecondaryIndicator(
            modifier = Modifier.tabIndicatorOffset(pagerState.currentPage),
            height = Dimensions.PaddingSmall,
            color = MaterialTheme.colorScheme.onSurface)
      }) {
        titles.forEachIndexed { index, title ->
          Tab(
              selected = pagerState.currentPage == index,
              onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
              text = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = MaterialTheme.typography.titleMedium.fontWeight,
                    color = MaterialTheme.colorScheme.onSurface)
              },
              modifier = Modifier.testTag(UserProfileScreenTestTags.getTabTestTag(index)))
        }
      }
}
