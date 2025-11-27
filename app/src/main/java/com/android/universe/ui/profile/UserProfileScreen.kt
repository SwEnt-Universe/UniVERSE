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
 * Composable for displaying a user's profile.
 *
 * @param uid The uid of the user to display.
 * @param onTabSelected Callback invoked when a tab is selected to switch between screens
 * @param onEditProfileClick Callback when the edit profile button is clicked.
 * @param userProfileViewModel The ViewModel responsible for managing user profile data.
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