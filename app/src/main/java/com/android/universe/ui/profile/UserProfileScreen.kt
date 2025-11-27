package com.android.universe.ui.profile

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.universe.model.event.Event
import com.android.universe.model.isoToCountryName
import com.android.universe.ui.common.ProfileContentLayout
import com.android.universe.ui.components.LiquidBox
import com.android.universe.ui.event.EventCard
import com.android.universe.ui.event.EventUIState
import com.android.universe.ui.event.EventViewModel
import com.android.universe.ui.navigation.NavigationBottomMenu
import com.android.universe.ui.navigation.NavigationTestTags
import com.android.universe.ui.navigation.Tab
import com.android.universe.ui.theme.DecorationBackground
import com.android.universe.ui.theme.Dimensions
import com.android.universe.ui.theme.Dimensions.PaddingLarge
import com.android.universe.ui.theme.UniverseTheme
import com.android.universe.ui.utils.toImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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

    val defaultBitmap = remember {
        val bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(android.graphics.Color.LTGRAY)
        bitmap.asImageBitmap()
    }

    var userProfileImageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(userUIState.userProfile.profilePicture) {
        userUIState.userProfile.profilePicture?.let { bytes ->
            userProfileImageBitmap = bytes.toImageBitmap()
        }
    }

    val imageToDisplay = userProfileImageBitmap ?: defaultBitmap

    Scaffold(
        containerColor = Color.Transparent,
        modifier = Modifier.testTag(NavigationTestTags.PROFILE_SCREEN),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = { NavigationBottomMenu(Tab.Event, onTabSelected) }
    ) { paddingValues ->
        val density = LocalDensity.current

        var profileHeightPx by remember { mutableFloatStateOf(0f) }
        var tabsHeightPx by remember { mutableFloatStateOf(0f) }

        val headerHeightDp = with(density) { (profileHeightPx + tabsHeightPx).toDp() }

        var scrollOffsetPx by remember { mutableFloatStateOf(0f) }

        val nestedScrollConnection = remember(profileHeightPx) {
            object : NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    val delta = available.y
                    val newOffset = scrollOffsetPx + delta
                    scrollOffsetPx = newOffset.coerceIn(-profileHeightPx, 0f)
                    return Offset.Zero
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .nestedScroll(nestedScrollConnection)
        ) {
            val pagerState = rememberPagerState(pageCount = { 2 })

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.Top
            ) { page ->
                val events =
                    if (page == 0) userUIState.historyEvents else userUIState.incomingEvents

                EventListSection(
                    events = events,
                    topPadding = headerHeightDp,
                    eventViewModel = eventViewModel
                )
            }

            Box(
                modifier = Modifier
                    .offset { IntOffset(x = 0, y = scrollOffsetPx.roundToInt()) }
                    .fillMaxWidth()
            ) {
                LiquidBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Dimensions.PaddingMedium, start = Dimensions.PaddingMedium, end = Dimensions.PaddingMedium),
                    shape = RoundedCornerShape(32.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .onGloballyPositioned { coordinates ->
                                    profileHeightPx = coordinates.size.height.toFloat()
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
                                onSettingsClick = { }
                            )
                        }

                        Box(
                            modifier = Modifier
                                .onGloballyPositioned { coordinates ->
                                    tabsHeightPx = coordinates.size.height.toFloat()
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
}

@Composable
fun EventListSection(
    events: List<Event>,
    topPadding: Dp,
    eventViewModel: EventViewModel
) {
    LazyColumn(
        contentPadding = PaddingValues(top = topPadding, bottom = 16.dp), // Replace with Dimensions
        modifier = Modifier.fillMaxSize()
    ){
        items(events) { event ->
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

            EventCard(
                event = eventUIState,
                viewModel = eventViewModel
            )
        }
    }
}

@Composable
fun ProfileTabRow(
    pagerState: PagerState,
    titles: List<String>
) {
    val scope = rememberCoroutineScope()

    TabRow(
        selectedTabIndex = pagerState.currentPage,
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface,
        divider = {},
        indicator = {
            TabRowDefaults.SecondaryIndicator(
                Modifier.tabIndicatorOffset(it[pagerState.currentPage]),
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

