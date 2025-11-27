package com.android.universe.ui.profile

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.universe.R
import com.android.universe.model.event.Event
import com.android.universe.model.user.UserProfile
import com.android.universe.ui.common.ProfileContentLayout
import com.android.universe.ui.components.LiquidBox
import com.android.universe.ui.event.EventCard
import com.android.universe.ui.event.EventUIState
import com.android.universe.ui.event.EventViewModel
import com.android.universe.ui.navigation.NavigationBottomMenu
import com.android.universe.ui.navigation.NavigationTestTags
import com.android.universe.ui.navigation.Tab
import com.android.universe.ui.theme.Dimensions
import com.android.universe.ui.utils.toImageBitmap
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/** Define all the tags for the UserProfile screen. Tags will be used to test the screen. */
object UserProfileScreenTestTags {
  const val FIRSTNAME = "userProfileFirstName"
  const val LASTNAME = "userProfileLastName"
  const val AGE = "userProfileAge"
  const val COUNTRY = "userProfileCountry"
  const val DESCRIPTION = "userProfileDescription"
  const val TAG = "userProfileTag"
  const val EDIT_BUTTON = "userProfileEditButton"
  const val TAGLIST = "userProfileTagList"
  const val PROFILE_PICTURE = "userProfilePicture"

  fun getTagTestTag(index: Int): String {
    return "userProfileTag$index"
  }
}

/**
 * The main screen composable for displaying a User's Profile.
 *
 * This component acts as the controller for the profile UI. It implements a complex "Collapsing Toolbar"
 * pattern with Sticky Headers using a custom implementation rather than standard NestedScrollConnection.
 *
 * @param uid The unique identifier of the user to display.
 * @param onTabSelected Callback invoked when a bottom navigation tab is selected.
 * @param onEditProfileClick Callback invoked when the user clicks the "Edit Profile" button.
 * @param userProfileViewModel The ViewModel managing the user profile state (fetched via [uid]).
 * @param eventViewModel The ViewModel managing event data (History/Incoming).
 */
@Composable
fun UserProfileScreen(
    uid: String,
    onTabSelected: (Tab) -> Unit = {},
    onEditProfileClick: (String) -> Unit = {},
    userProfileViewModel: UserProfileViewModel = viewModel(),
    eventViewModel: EventViewModel = viewModel()
) {
    val userUIState by userProfileViewModel.userState.collectAsState()
    LaunchedEffect(uid) { userProfileViewModel.loadUser(uid) }

    val imageToDisplay = rememberImageBitmap(
        bytes = userUIState.userProfile.profilePicture,
        defaultImageId = R.drawable.default_profile_img
    )

    // State Initialization
    val density = LocalDensity.current
    val pagerState = rememberPagerState(pageCount = { 2 })
    val historyListState = rememberLazyListState()
    val incomingListState = rememberLazyListState()

    // Layout Measurements
    var profileContentHeightPx by remember { mutableFloatStateOf(0f) }
    var tabRowHeightPx by remember { mutableFloatStateOf(0f) }

    val elementSpacingDp = 8.dp
    val elementSpacingPx = with(density) { elementSpacingDp.toPx() }

    val profileContentHeightDp = with(density) { profileContentHeightPx.toDp() }
    val tabRowHeightDp = with(density) { tabRowHeightPx.toDp() }

    // Synchronization Logic
    val headerOffsetPx by remember {
        derivedStateOf {
            val currentListState = if (pagerState.currentPage == 0) historyListState else incomingListState
            val totalCollapsibleHeightPx = profileContentHeightPx + elementSpacingPx

            if (currentListState.firstVisibleItemIndex == 0) {
                val scrollOffset = currentListState.firstVisibleItemScrollOffset.toFloat()
                -scrollOffset.coerceIn(0f, totalCollapsibleHeightPx)
            } else {
                -totalCollapsibleHeightPx
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        modifier = Modifier.testTag(NavigationTestTags.PROFILE_SCREEN),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = { NavigationBottomMenu(Tab.Profile, onTabSelected) }
    ) { _ ->

        LiquidBox(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 24.dp)
            ) {
                ProfileContentPager(
                    pagerState = pagerState,
                    historyListState = historyListState,
                    incomingListState = incomingListState,
                    historyEvents = userUIState.historyEvents,
                    incomingEvents = userUIState.incomingEvents,
                    eventViewModel = eventViewModel,
                    spacerHeightDp = profileContentHeightDp + elementSpacingDp,
                    clipPaddingDp = tabRowHeightDp
                )

                ProfileHeaderOverlay(
                    headerOffsetPx = headerOffsetPx,
                    userProfile = userUIState.userProfile,
                    userImage = imageToDisplay,
                    pagerState = pagerState,
                    gapHeightDp = elementSpacingDp,
                    onProfileHeightMeasured = { profileContentHeightPx = it },
                    onTabHeightMeasured = { tabRowHeightPx = it },
                    onEditProfileClick = { onEditProfileClick(uid) }
                )
            }
        }
    }
}

/**
 * Manages the horizontal paging between the different event lists (History vs Incoming).
 *
 * This composable determines which list state and data source to use based on the current
 * page index and delegates the rendering to [ProfileEventList].
 *
 * @param pagerState The state object for the [HorizontalPager].
 * @param historyListState The scroll state for the History list.
 * @param incomingListState The scroll state for the Incoming list.
 * @param historyEvents The list of past events.
 * @param incomingEvents The list of upcoming events.
 * @param eventViewModel The view model used by event cards.
 * @param spacerHeightDp The height of the transparent spacer at the top of the list (Profile Height + Gap).
 * @param clipPaddingDp The top padding applied to the list container to clip content behind the sticky tabs.
 */
@Composable
fun ProfileContentPager(
    pagerState: PagerState,
    historyListState: LazyListState,
    incomingListState: LazyListState,
    historyEvents: List<Event>,
    incomingEvents: List<Event>,
    eventViewModel: EventViewModel,
    spacerHeightDp: Dp,
    clipPaddingDp: Dp
) {
    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.Top
    ) { page ->
        val isHistoryPage = page == 0
        val listState = if (isHistoryPage) historyListState else incomingListState
        val events = if (isHistoryPage) historyEvents else incomingEvents

        ProfileEventList(
            listState = listState,
            events = events,
            eventViewModel = eventViewModel,
            headerSpacerHeight = spacerHeightDp,
            topClipPadding = clipPaddingDp
        )
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
    events: List<Event>,
    eventViewModel: EventViewModel,
    headerSpacerHeight: Dp,
    topClipPadding: Dp
) {
    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(bottom = 80.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(top = topClipPadding)
    ) {
        item(key = "header_spacer") {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(headerSpacerHeight)
                    .background(Color.Transparent)
            )
        }

        items(events, key = { it.id }) { event ->
            val eventUIState = EventUIState(
                title = event.title,
                description = event.description ?: "",
                date = event.date,
                tags = event.tags.map { it.displayName },
                creator = event.creator,
                participants = event.participants.size,
                index = event.id.hashCode(),
                joined = true,
                eventPicture = event.eventPicture
            )

            Box(modifier = Modifier.padding(horizontal = Dimensions.PaddingMedium, vertical = 4.dp)) {
                EventCard(
                    event = eventUIState,
                    viewModel = eventViewModel
                )
            }
        }
    }
}

/**
 * The floating overlay component containing the User Profile Info and the Sticky Tabs.
 *
 * This component sits on top of the list (Z-axis). It moves vertically based on the `headerOffsetPx`.
 * It creates the "Sticky" effect by stopping its movement once the Profile Info is scrolled away,
 * leaving only the Tabs visible.
 *
 * @param headerOffsetPx The current vertical offset (in pixels) calculated by the parent controller.
 * @param userProfile The user data model.
 * @param userImage The bitmap of the user's profile picture.
 * @param pagerState The state of the pager (passed to the tabs).
 * @param gapHeightDp The spacing between the profile info and the tabs.
 * @param onProfileHeightMeasured Callback to report the height of the profile info block to the parent.
 * @param onTabHeightMeasured Callback to report the height of the tab row to the parent.
 * @param onEditProfileClick Action to perform when settings/edit is clicked.
 */
@Composable
fun ProfileHeaderOverlay(
    headerOffsetPx: Float,
    userProfile: UserProfile,
    userImage: ImageBitmap,
    pagerState: PagerState,
    gapHeightDp: Dp,
    onProfileHeightMeasured: (Float) -> Unit,
    onTabHeightMeasured: (Float) -> Unit,
    onEditProfileClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .offset { IntOffset(x = 0, y = headerOffsetPx.roundToInt()) }
            .draggable(
                orientation = Orientation.Horizontal,
                state = rememberDraggableState { }
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    onProfileHeightMeasured(coordinates.size.height.toFloat())
                }
        ) {
            ProfileContentLayout(
                modifier = Modifier,
                userProfile = userProfile,
                userProfileImage = userImage,
                followers = 0,
                following = 0,
                heightTagList = 200.dp,
                onChatClick = { },
                onAddClick = { },
                onSettingsClick = onEditProfileClick
            )
        }

        Spacer(modifier = Modifier.height(gapHeightDp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    onTabHeightMeasured(coordinates.size.height.toFloat())
                }
        ) {
            ProfileTabRow(
                pagerState = pagerState,
                titles = listOf("History", "Incoming")
            )
        }
    }
}

/**
 * A helper composable that converts a ByteArray into an [ImageBitmap].
 *
 * It handles the asynchronous loading state and provides a default image if the byte array
 * is null or invalid.
 *
 * @param bytes The raw image data.
 * @param defaultImageId The resource ID of the drawable to use as a fallback.
 * @return The loaded [ImageBitmap] or the default image.
 */
@Composable
fun rememberImageBitmap(
    bytes: ByteArray?,
    defaultImageId: Int = R.drawable.default_profile_img
): ImageBitmap {
    val context = LocalContext.current
    val defaultBitmap = remember(defaultImageId) {
        BitmapFactory.decodeResource(context.resources, defaultImageId)?.asImageBitmap()
            ?: Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888).apply {
                eraseColor(android.graphics.Color.LTGRAY)
            }.asImageBitmap()
    }
    val imageBitmapState = produceState<ImageBitmap?>(initialValue = null, bytes) {
        value = bytes?.toImageBitmap()
    }
    return imageBitmapState.value ?: defaultBitmap
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
fun ProfileTabRow(
    pagerState: PagerState,
    titles: List<String>
) {
    val scope = rememberCoroutineScope()

    PrimaryTabRow(
        selectedTabIndex = pagerState.currentPage,
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface,
        divider = {},
        indicator = {
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(pagerState.currentPage),
                height = 3.dp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    ) {
        titles.forEachIndexed { index, title ->
            Tab(
                selected = pagerState.currentPage == index,
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                },
                text = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = MaterialTheme.typography.titleMedium.fontWeight,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            )
        }
    }
}