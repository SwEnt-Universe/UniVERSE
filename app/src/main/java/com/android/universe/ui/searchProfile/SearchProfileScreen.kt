package com.android.universe.ui.searchProfile

import android.annotation.SuppressLint
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.universe.model.tag.Tag
import com.android.universe.ui.common.TagRow
import com.android.universe.ui.components.CategoryItemDefaults
import com.android.universe.ui.components.LiquidBox
import com.android.universe.ui.components.LiquidSearchBar
import com.android.universe.ui.navigation.NavigationBottomMenu
import com.android.universe.ui.navigation.NavigationTestTags
import com.android.universe.ui.navigation.Tab
import com.android.universe.ui.theme.Dimensions
import kotlinx.coroutines.launch

object SearchProfileScreenTestTags {
  const val HEADER = "searchProfileHeader"
  const val SEARCH_BAR = "searchBar"
  const val TAB_ROW = "tabRow"
  const val PAGER = "searchProfilePager"
  const val PAGE_EXPLORE = "pageExplore"
  const val PAGE_FOLLOWERS = "pageFollowers"
  const val PAGE_FOLLOWING = "pageFollowing"
  const val PROFILE_LIST = "profileList"
  const val LOADING = "profileListLoading"
  const val EMPTY = "profileListEmpty"

  fun tab(index: Int) = "searchProfileTab_$index"
}

/**
 * Composable function that displays the search profile screen with a header containing a search bar
 * and tab row, and a content area with a horizontal pager for different profile categories
 * (Explore, Followers, Following).
 *
 * @param uid The user ID of the current user.
 * @param onTabSelected Callback function invoked when a bottom navigation tab is selected.
 * @param onCardClick Callback function invoked when a profile card is clicked.
 * @param searchProfileViewModel The [SearchProfileViewModel] used to manage UI state and handle
 *   user interactions.
 */
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SearchProfileScreen(
    uid: String,
    onTabSelected: (Tab) -> Unit = {},
    onCardClick: () -> Unit = {},
    searchProfileViewModel: SearchProfileViewModel = viewModel { SearchProfileViewModel(uid) },
) {
  val uiState by searchProfileViewModel.uiState.collectAsState()
  val searchQuery by searchProfileViewModel.searchQuery.collectAsState()
  val profilesState by searchProfileViewModel.profilesState.collectAsState()
  val categories by searchProfileViewModel.categories.collectAsState()

  val pagerState = rememberPagerState(pageCount = { 3 })
  val exploreListState = rememberLazyListState()
  val followersListState = rememberLazyListState()
  val followingListState = rememberLazyListState()

  val currentListState =
      when (pagerState.currentPage) {
        0 -> exploreListState
        1 -> followersListState
        2 -> followingListState
        else -> exploreListState
      }

  val isSearchBarVisible = remember { mutableStateOf(true) }

  LaunchedEffect(pagerState.currentPage, currentListState) {
    snapshotFlow {
          currentListState.firstVisibleItemIndex == 0 &&
              currentListState.firstVisibleItemScrollOffset < 50
        }
        .collect { isVisible -> isSearchBarVisible.value = isVisible }
  }

  LaunchedEffect(pagerState.currentPage) {
    when (pagerState.currentPage) {
      0 -> searchProfileViewModel.loadExplore()
      1 -> searchProfileViewModel.loadFollowers()
      2 -> searchProfileViewModel.loadFollowing()
    }
  }

  Scaffold(
      containerColor = Color.Transparent,
      modifier = Modifier.testTag(NavigationTestTags.SEARCH_PROFILE_SCREEN),
      bottomBar = { NavigationBottomMenu(Tab.Community, onTabSelected) }) { paddingValues ->
        LiquidBox(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(Dimensions.RoundedCornerLarge)) {
              Box(modifier = Modifier.fillMaxSize()) {
                SearchProfileContentPager(
                    pagerState = pagerState,
                    exploreListState = exploreListState,
                    followersListState = followersListState,
                    followingListState = followingListState,
                    exploreProfiles = profilesState.explore,
                    followersProfiles = profilesState.followers,
                    followingProfiles = profilesState.following,
                    isLoading = profilesState.isLoading,
                    searchProfileViewModel = searchProfileViewModel,
                    isSearchBarVisible = isSearchBarVisible.value,
                    onCardClick = onCardClick)

                SearchHeaderOverlay(
                    pagerState = pagerState,
                    searchQuery = searchQuery,
                    onQueryChange = { searchProfileViewModel.updateSearchQuery(it) },
                    topPadding = paddingValues.calculateTopPadding(),
                    isSearchBarVisible = isSearchBarVisible.value)
                    categories = categories,
                    catSelect = searchProfileViewModel::selectCategory)

                uiState.errorMsg?.let { error ->
                  Snackbar(
                      modifier =
                          Modifier.align(Alignment.BottomCenter)
                              .padding(Dimensions.PaddingMedium)) {
                        Text(error)
                      }
                }
              }
            }
      }
}

/**
 * Composable function that displays the search header overlay containing the search bar and tab row
 * for navigating between different profile categories (Explore, Followers, Following) in the search
 * profile screen.
 *
 * @param pagerState The [PagerState] used to control the horizontal pager.
 * @param searchQuery The current search query string.
 * @param onQueryChange Callback function invoked when the search query changes.
 * @param topPadding The top padding to be applied to the overlay, typically to account for system
 *   UI elements.
 * @param isSearchBarVisible A boolean indicating whether the search bar should be visible.
 * @param categories The set of selected categories.
 * @param catSelect Callback function invoked when a category is selected or deselected.
 */
@Composable
fun SearchHeaderOverlay(
    pagerState: PagerState,
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    topPadding: Dp,
    isSearchBarVisible: Boolean,
    categories: Set<Tag.Category> = emptySet(),
    catSelect: (Tag.Category, Boolean) -> Unit = { _, _ -> }
) {
    val allCats = remember { Tag.tagFromEachCategory.toList() }
  val searchBarHeight by
      animateDpAsState(
          targetValue = if (isSearchBarVisible) 56.dp else 0.dp,
          animationSpec = tween(durationMillis = 300),
          label = "searchBarHeight")

  val searchBarAlpha by
      animateFloatAsState(
          targetValue = if (isSearchBarVisible) 1f else 0f,
          animationSpec = tween(durationMillis = 300),
          label = "searchBarAlpha")

  val surfaceColor by
      animateColorAsState(
          targetValue =
              if (isSearchBarVisible) Color.Transparent
              else MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
          animationSpec = tween(durationMillis = 300),
          label = "surfaceColor")

  Surface(
      modifier = Modifier.fillMaxWidth().testTag(SearchProfileScreenTestTags.HEADER),
      color = surfaceColor) {
        Column(modifier = Modifier.fillMaxWidth().padding(top = topPadding)) {
          Box(
              modifier =
                  Modifier.height(
                          searchBarHeight +
                              if (isSearchBarVisible) Dimensions.PaddingMedium * 2 else 0.dp)
                      .fillMaxWidth()
                      .alpha(searchBarAlpha)) {
                if (isSearchBarVisible) {
                  LiquidSearchBar(
                      query = searchQuery,
                      onQueryChange = onQueryChange,
                      modifier =
                          Modifier.padding(horizontal = Dimensions.PaddingLarge)
                              .testTag(SearchProfileScreenTestTags.SEARCH_BAR))
                }
              }

          if (isSearchBarVisible) {
            Spacer(modifier = Modifier.height(Dimensions.SpacerSmall))
              TagRow(
                  allCats,
                  heightTag = CategoryItemDefaults.HEIGHT_CAT,
                  widthTag = CategoryItemDefaults.WIDTH_CAT,
                  isSelected = { cat -> categories.contains(cat.category) },
                  onTagSelect = { cat -> catSelect(cat.category, true) },
                  onTagReSelect = { cat -> catSelect(cat.category, false) },
                  fadeWidth = (CategoryItemDefaults.HEIGHT_CAT * 0.5f).dp,
                  isCategory = true)
          }

          SearchProfileTabRow(
              pagerState = pagerState, titles = listOf("Explore", "Followers", "Following"))
        }
      }
}

/**
 * Composable function that displays a horizontal pager containing lists of user profiles for
 * different categories (Explore, Followers, Following) in the search profile screen.
 *
 * @param pagerState The [PagerState] used to control the horizontal pager.
 * @param exploreListState The [LazyListState] for the Explore profiles list.
 * @param followersListState The [LazyListState] for the Followers profiles list.
 * @param followingListState The [LazyListState] for the Following profiles list.
 * @param exploreProfiles A list of [ProfileUIState] objects representing the Explore profiles.
 * @param followersProfiles A list of [ProfileUIState] objects representing the Followers profiles.
 * @param followingProfiles A list of [ProfileUIState] objects representing the Following profiles.
 * @param isLoading A boolean indicating whether the profile data is currently being loaded.
 * @param searchProfileViewModel The [SearchProfileViewModel] used to handle user interactions such
 *   as following or unfollowing users.
 * @param isSearchBarVisible A boolean indicating whether the search bar is visible.
 * @param onCardClick Callback function invoked when a profile card is clicked.
 */
@Composable
fun SearchProfileContentPager(
    pagerState: PagerState,
    exploreListState: LazyListState,
    followersListState: LazyListState,
    followingListState: LazyListState,
    exploreProfiles: List<ProfileUIState>,
    followersProfiles: List<ProfileUIState>,
    followingProfiles: List<ProfileUIState>,
    isLoading: Boolean,
    searchProfileViewModel: SearchProfileViewModel,
    isSearchBarVisible: Boolean,
    onCardClick: () -> Unit = {}
) {
  val headerHeight by
      animateDpAsState(
          targetValue = if (isSearchBarVisible) 180.dp else 100.dp,
          animationSpec = tween(durationMillis = 300),
          label = "headerHeight")

  HorizontalPager(
      state = pagerState,
      modifier = Modifier.fillMaxSize().testTag(SearchProfileScreenTestTags.PAGER),
      verticalAlignment = Alignment.Top) { page ->
        val (listState, profiles) =
            when (page) {
              0 -> exploreListState to exploreProfiles
              1 -> followersListState to followersProfiles
              2 -> followingListState to followingProfiles
              else -> exploreListState to exploreProfiles
            }

        val pageTag =
            when (page) {
              0 -> SearchProfileScreenTestTags.PAGE_EXPLORE
              1 -> SearchProfileScreenTestTags.PAGE_FOLLOWERS
              2 -> SearchProfileScreenTestTags.PAGE_FOLLOWING
              else -> "unknownPage"
            }

        Box(modifier = Modifier.testTag(pageTag)) {
          ProfileList(
              listState = listState,
              profiles = profiles,
              isLoading = isLoading,
              searchProfileViewModel = searchProfileViewModel,
              topPadding = headerHeight,
              onCardClick = onCardClick)
        }
      }
}

/**
 * Composable function that displays a list of user profiles in a lazy column.
 *
 * @param listState The [LazyListState] used to control the scroll state of the list.
 * @param profiles A list of [ProfileUIState] objects representing the user profiles to be
 *   displayed.
 * @param isLoading A boolean indicating whether the profile data is currently being loaded.
 * @param searchProfileViewModel The [SearchProfileViewModel] used to handle user interactions such
 *   as following or unfollowing users.
 * @param topPadding The top padding to be applied to the list, typically to account for overlaying
 *   UI elements.
 * @param onCardClick Callback function invoked when a profile card is clicked.
 */
@Composable
fun ProfileList(
    listState: LazyListState,
    profiles: List<ProfileUIState>,
    isLoading: Boolean,
    searchProfileViewModel: SearchProfileViewModel,
    topPadding: Dp,
    onCardClick: () -> Unit = {}
) {
  LazyColumn(
      state = listState,
      contentPadding = PaddingValues(top = topPadding, bottom = 80.dp),
      modifier = Modifier.fillMaxSize().testTag(SearchProfileScreenTestTags.PROFILE_LIST)) {
        if (isLoading && profiles.isEmpty()) {
          item(key = "loading") {
            Box(
                modifier =
                    Modifier.fillMaxWidth()
                        .padding(Dimensions.PaddingLarge)
                        .testTag(SearchProfileScreenTestTags.LOADING),
                contentAlignment = Alignment.Center) {
                  CircularProgressIndicator()
                }
          }
        }

        if (!isLoading && profiles.isEmpty()) {
          item(key = "empty_state") {
            Box(
                modifier =
                    Modifier.fillMaxWidth()
                        .padding(Dimensions.PaddingLarge)
                        .testTag(SearchProfileScreenTestTags.EMPTY),
                contentAlignment = Alignment.Center) {
                  Text(
                      text = "No profiles found",
                      style = MaterialTheme.typography.bodyLarge,
                      color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
          }
        }

        items(profiles, key = { it.user.uid }) { profile ->
          Box(
              modifier =
                  Modifier.padding(
                      horizontal = Dimensions.PaddingMedium, vertical = Dimensions.PaddingSmall)) {
                ProfileCard(
                    profile = profile,
                    viewModel = searchProfileViewModel,
                    onCardClick = onCardClick)
              }
        }
      }
}

/**
 * Composable function that displays a tab row for navigating between different profile categories
 * (Explore, Followers, Following) in the search profile screen.
 *
 * @param pagerState The [PagerState] used to control the horizontal pager.
 * @param titles A list of titles for the tabs.
 */
@Composable
fun SearchProfileTabRow(pagerState: PagerState, titles: List<String>) {
  val scope = rememberCoroutineScope()

  PrimaryTabRow(
      modifier = Modifier.testTag(SearchProfileScreenTestTags.TAB_ROW),
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
              modifier = Modifier.testTag(SearchProfileScreenTestTags.tab(index)))
        }
      }
}
