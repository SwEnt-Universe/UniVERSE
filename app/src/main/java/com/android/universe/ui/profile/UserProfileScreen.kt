package com.android.universe.ui.profile

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.universe.R
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
@OptIn(ExperimentalMaterial3Api::class)
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

    val density = LocalDensity.current

    var profileContentHeightPx by remember { mutableFloatStateOf(0f) }
    var tabRowHeightPx by remember { mutableFloatStateOf(0f) }

    val headerHeightPx = profileContentHeightPx + tabRowHeightPx
    val headerHeightDp = with(density) { headerHeightPx.toDp() }

    val historyListState = rememberLazyListState()
    val incomingListState = rememberLazyListState()
    val pagerState = rememberPagerState(pageCount = { 2 })

    val headerOffsetPx by remember {
        derivedStateOf {
            val currentListState = if (pagerState.currentPage == 0) historyListState else incomingListState

            if (profileContentHeightPx == 0f || currentListState.firstVisibleItemIndex > 0) {
                -profileContentHeightPx
            } else {
                val scrollOffset = currentListState.firstVisibleItemScrollOffset.toFloat()
                -scrollOffset.coerceIn(0f, profileContentHeightPx)
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
            modifier = Modifier
                .fillMaxSize(),
            shape = RoundedCornerShape(32.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.Top
                ) { page ->
                    val events = if (page == 0) userUIState.historyEvents else userUIState.incomingEvents

                    val listState = if (page == 0) historyListState else incomingListState

                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(top = headerHeightDp, bottom = 80.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
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
                                profileContentHeightPx = coordinates.size.height.toFloat()
                            }
                    ) {
                        ProfileContentLayout(
                            modifier = Modifier,
                            userProfile = userUIState.userProfile,
                            userProfileImage = imageToDisplay,
                            followers = 0,
                            following = 0,
                            onChatClick = { },
                            onAddClick = { },
                            onSettingsClick = { onEditProfileClick(uid) }
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { coordinates ->
                                tabRowHeightPx = coordinates.size.height.toFloat()
                            }
                    ) {
                        ProfileTabRow(
                            pagerState = pagerState,
                            titles = listOf("History", "Incoming")
                        )
                    }
                }
            }
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

