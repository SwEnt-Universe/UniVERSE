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
    eventViewModel: EventViewModel = viewModel()
) {
  val userUIState by userProfileViewModel.userState.collectAsState()

  // State Initialization
  val density = LocalDensity.current
  val pagerState = rememberPagerState(pageCount = { 2 })
  val historyListState = rememberLazyListState()
  val incomingListState = rememberLazyListState()

  // Layout Measurements
  var profileContentHeightPx by remember { mutableFloatStateOf(0f) }
  var tabRowHeightPx by remember { mutableFloatStateOf(0f) }

  val elementSpacingDp = Dimensions.PaddingMedium
  val elementSpacingPx = with(density) { elementSpacingDp.toPx() }

  val profileContentHeightDp = with(density) { profileContentHeightPx.toDp() }
  val tabRowHeightDp = with(density) { tabRowHeightPx.toDp() }

  val totalCollapsibleHeightPx = profileContentHeightPx + elementSpacingPx

  LaunchedEffect(
      pagerState.currentPage,
      historyListState.firstVisibleItemScrollOffset,
      historyListState.firstVisibleItemIndex,
      totalCollapsibleHeightPx) {
        if (pagerState.currentPage == 0 && totalCollapsibleHeightPx > 0) {
          val scrollOffset = historyListState.firstVisibleItemScrollOffset
          val firstIndex = historyListState.firstVisibleItemIndex

          val collapseAmount =
              if (firstIndex == 0) scrollOffset.toFloat() else totalCollapsibleHeightPx

          val targetOffset =
              if (incomingListState.firstVisibleItemIndex == 0)
                  incomingListState.firstVisibleItemScrollOffset
              else totalCollapsibleHeightPx.toInt()

          if (collapseAmount >= totalCollapsibleHeightPx) {
            if (targetOffset < totalCollapsibleHeightPx) {
              incomingListState.scrollToItem(0, totalCollapsibleHeightPx.toInt())
            }
          } else {
            if (targetOffset != collapseAmount.toInt()) {
              incomingListState.scrollToItem(0, collapseAmount.toInt())
            }
          }
        }
      }

  LaunchedEffect(
      pagerState.currentPage,
      incomingListState.firstVisibleItemScrollOffset,
      incomingListState.firstVisibleItemIndex,
      totalCollapsibleHeightPx) {
        if (pagerState.currentPage == 1 && totalCollapsibleHeightPx > 0) {
          val scrollOffset = incomingListState.firstVisibleItemScrollOffset
          val firstIndex = incomingListState.firstVisibleItemIndex

          val collapseAmount =
              if (firstIndex == 0) scrollOffset.toFloat() else totalCollapsibleHeightPx
          val targetOffset =
              if (historyListState.firstVisibleItemIndex == 0)
                  historyListState.firstVisibleItemScrollOffset
              else totalCollapsibleHeightPx.toInt()

          if (collapseAmount >= totalCollapsibleHeightPx) {
            if (targetOffset < totalCollapsibleHeightPx) {
              historyListState.scrollToItem(0, totalCollapsibleHeightPx.toInt())
            }
          } else {
            if (targetOffset != collapseAmount.toInt()) {
              historyListState.scrollToItem(0, collapseAmount.toInt())
            }
          }
        }
      }

  // Synchronization Logic
  val headerOffsetPx by remember {
    derivedStateOf {
      val currentListState =
          if (pagerState.currentPage == 0) historyListState else incomingListState
      val totalCollapsibleHeightPx = profileContentHeightPx + elementSpacingPx

      if (currentListState.firstVisibleItemIndex == 0) {
        val scrollOffset = currentListState.firstVisibleItemScrollOffset.toFloat()
        -scrollOffset.coerceIn(0f, totalCollapsibleHeightPx)
      } else {
        -totalCollapsibleHeightPx
      }
    }
  }

  ScreenLayout(
      bottomBar = { NavigationBottomMenu(Tab.Profile, onTabSelected) },
      modifier = Modifier.testTag(NavigationTestTags.PROFILE_SCREEN)) { _ ->
        Box(modifier = Modifier.fillMaxSize()) {
          LiquidBox(
              modifier = Modifier.fillMaxSize(),
              shape = RoundedCornerShape(Dimensions.RoundedCornerLarge)) {
                Box(modifier = Modifier.fillMaxSize().padding(top = Dimensions.PaddingExtraLarge)) {
                  ProfileContentPager(
                      pagerState = pagerState,
                      historyListState = historyListState,
                      incomingListState = incomingListState,
                      historyEvents = userUIState.historyEvents,
                      incomingEvents = userUIState.incomingEvents,
                      eventViewModel = eventViewModel,
                      spacerHeightDp = profileContentHeightDp + elementSpacingDp,
                      clipPaddingDp = tabRowHeightDp,
                      onChatNavigate = onChatNavigate,
                      onCardClick = onCardClick)

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
    onChatNavigate: (eventId: String, eventTitle: String) -> Unit = { _, _ -> },
    onCardClick: (eventId: String, eventLocation: Location) -> Unit = { _, _ -> }
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
            onChatNavigate = onChatNavigate,
            onCardClick = onCardClick)
      }
}

/**
 * Renders a specific list of events (e.g., History or Incoming) within a [LazyColumn].
 *
 * @param listState The scroll state for this specific list.
 * @param events The list of [Event] objects to display.
 * @param eventViewModel The view model for event interactions.
 * @param headerSpacerHeight The height of the invisible spacer (Index 0).
 * @param topClipPadding The top padding used to clip the scrolling content.
 */
@Composable
fun ProfileEventList(
    listState: LazyListState,
    events: List<EventUIState>,
    eventViewModel: EventViewModel,
    headerSpacerHeight: Dp,
    topClipPadding: Dp,
    onChatNavigate: (eventId: String, eventTitle: String) -> Unit = { _, _ -> },
    onCardClick: (eventId: String, eventLocation: Location) -> Unit = { _, _ -> }
) {
  val density = LocalDensity.current
  val configuration = LocalConfiguration.current

  val screenHeight = configuration.screenHeightDp.dp
  var footerHeight by remember { mutableStateOf(screenHeight) }

  val bottomPaddingDp = 80.dp

  LaunchedEffect(events, listState) {
    snapshotFlow { listState.layoutInfo }
        .collect { layoutInfo ->
          val visibleItems = layoutInfo.visibleItemsInfo
          val viewportHeight = layoutInfo.viewportSize.height

          if (visibleItems.isNotEmpty() && events.isNotEmpty()) {
            val lastEventIndex = events.size
            val isLastItemVisible = visibleItems.any { it.index == lastEventIndex }

            if (isLastItemVisible) {
              val eventsHeightPx =
                  visibleItems.filter { it.index > 0 && it.index <= events.size }.sumOf { it.size }

              val bottomPaddingPx = with(density) { bottomPaddingDp.toPx() }

              val neededPx = (viewportHeight - eventsHeightPx - bottomPaddingPx).coerceAtLeast(0f)
              footerHeight = with(density) { neededPx.toDp() }
            } else {
              footerHeight = 0.dp
            }
          }
        }
  }

  LazyColumn(
      state = listState,
      contentPadding = PaddingValues(bottom = 80.dp),
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
                    viewModel = eventViewModel)
              }
        }
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
