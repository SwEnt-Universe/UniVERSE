package com.android.universe.ui.searchProfile

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
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
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.universe.ui.components.LiquidBox
import com.android.universe.ui.components.LiquidSearchBar
import com.android.universe.ui.components.ScreenLayout
import com.android.universe.ui.navigation.NavigationBottomMenu
import com.android.universe.ui.navigation.NavigationTestTags
import com.android.universe.ui.navigation.Tab
import com.android.universe.ui.theme.Dimensions
import kotlinx.coroutines.launch

/**
 * Object containing constant test tags used for UI testing within the Search Profile Screen.
 * These tags allow testing frameworks (like Espresso or Compose Test Rule) to locate
 * specific composables in the UI hierarchy.
 */
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

    /**
     * Generates a specific test tag for a tab based on its index.
     *
     * @param index The index of the tab (e.g., 0 for Explore).
     * @return A string tag in the format "searchProfileTab_{index}".
     */
  fun tab(index: Int) = "searchProfileTab_$index"
}

/**
 * The main entry point for the Search Profile Screen.
 *
* This composable initializes the screen layout, orchestrates the state management via
* [SearchProfileViewModel], handles the navigation bottom bar, and sets up the
* top-level paging logic between Explore, Followers, and Following tabs.
*
* @param uid The unique identifier of the current user, used to initialize the ViewModel.
* @param onTabSelected Callback invoked when a tab on the bottom navigation menu is selected.
* @param onCardClick Callback invoked when a specific user profile card is clicked.
* @param searchProfileViewModel The ViewModel managing the UI state and business logic for this screen.
* Defaults to a new instance factory based on the provided [uid].
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

  // State Initialization
  val pagerState = rememberPagerState(pageCount = { 3 })
  val exploreListState = rememberLazyListState()
  val followersListState = rememberLazyListState()
  val followingListState = rememberLazyListState()

  // Load data when tabs change
  LaunchedEffect(pagerState.currentPage) {
    when (pagerState.currentPage) {
      0 -> searchProfileViewModel.loadExplore()
      1 -> searchProfileViewModel.loadFollowers()
      2 -> searchProfileViewModel.loadFollowing()
    }
  }

  ScreenLayout(
      bottomBar = { NavigationBottomMenu(Tab.Community, onTabSelected) },
      modifier = Modifier.testTag(NavigationTestTags.SEARCH_PROFILE_SCREEN)) { paddingValues ->
        val bottomPadding = paddingValues.calculateBottomPadding()

        Column(modifier = Modifier.fillMaxSize()) {
          LiquidBox(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(0.dp)) {
            SearchHeader(
                searchQuery = searchQuery,
                pagerState = pagerState,
                onQueryChange = { searchProfileViewModel.updateSearchQuery(it) })
          }

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
                listBottomPadding = bottomPadding,
                onCardClick = onCardClick)

            uiState.errorMsg?.let { error ->
              Snackbar(
                  modifier =
                      Modifier.align(Alignment.BottomCenter).padding(Dimensions.PaddingMedium)) {
                    Text(error)
                  }
            }
          }
        }
      }
}

/**
 * Displays the fixed header content at the top of the screen.
 *
 * This includes the status bar spacing, the search bar for filtering profiles,
 * and the tab row for navigation between profile categories.
 *
 * @param searchQuery The current text entered in the search bar.
 * @param pagerState The state of the pager, used to synchronize the tab selection with the displayed page.
 * @param onQueryChange Callback invoked when the text in the search bar changes.
 */
@Composable
fun SearchHeader(searchQuery: String, pagerState: PagerState, onQueryChange: (String) -> Unit) {
  val topPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

  Column(modifier = Modifier.fillMaxWidth().testTag(SearchProfileScreenTestTags.HEADER)) {
    Spacer(modifier = Modifier.height(topPadding))

    Box(modifier = Modifier.fillMaxWidth().padding(vertical = Dimensions.PaddingMedium)) {
      LiquidSearchBar(
          query = searchQuery,
          onQueryChange = onQueryChange,
          modifier =
              Modifier.padding(horizontal = Dimensions.PaddingLarge)
                  .testTag(SearchProfileScreenTestTags.SEARCH_BAR))
    }

    SearchProfileTabRow(
        pagerState = pagerState, titles = listOf("Explore", "Followers", "Following"))
  }
}

/**
 * Manages the horizontal paging content of the screen.
 *
 * It switches between the 'Explore', 'Followers', and 'Following' lists based on the
 * current page of the [pagerState]. It is responsible for passing the correct
 * state and data to the [ProfileList] for the currently active page.
 *
 * @param pagerState The state object controlling the horizontal pager (current page, scrolling).
 * @param exploreListState The scroll state for the 'Explore' list LazyColumn.
 * @param followersListState The scroll state for the 'Followers' list LazyColumn.
 * @param followingListState The scroll state for the 'Following' list LazyColumn.
 * @param exploreProfiles The list of profiles to display in the 'Explore' tab.
 * @param followersProfiles The list of profiles to display in the 'Followers' tab.
 * @param followingProfiles The list of profiles to display in the 'Following' tab.
 * @param isLoading Boolean flag indicating if data is currently being fetched.
 * @param searchProfileViewModel The ViewModel passed down to handle interactions within the lists.
 * @param listBottomPadding The padding required at the bottom of the list to accommodate the navigation bar or other overlays.
 * @param onCardClick Callback invoked when a profile card within any of the lists is clicked.
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
    listBottomPadding: Dp,
    onCardClick: () -> Unit = {}
) {
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
              bottomContentPadding = listBottomPadding,
              onCardClick = onCardClick)
        }
      }
}

/**
 * Renders a list of user profiles for a specific category (Explore, Followers, or Following).
 *
 * This composable handles three distinct states:
 * 1. **Loading:** Displays a circular progress indicator if [isLoading] is true and the list is empty.
 * 2. **Empty:** Displays a "No profiles found" message if [isLoading] is false and the list is empty.
 * 3. **Content:** Displays the list of [ProfileCard] items if profiles are available.
 *
 * @param listState The scroll state used for this specific LazyColumn.
 * @param profiles The list of [ProfileUIState] data to populate the list.
 * @param isLoading Whether the screen is currently fetching data.
 * @param searchProfileViewModel The ViewModel used by individual [ProfileCard]s for logic (e.g., follow/unfollow).
 * @param bottomContentPadding Padding to apply to the bottom of the content list.
 * @param onCardClick Callback invoked when a profile card is clicked.
 */
@Composable
fun ProfileList(
    listState: LazyListState,
    profiles: List<ProfileUIState>,
    isLoading: Boolean,
    searchProfileViewModel: SearchProfileViewModel,
    bottomContentPadding: Dp,
    onCardClick: () -> Unit = {}
) {
  LazyColumn(
      state = listState,
      contentPadding = PaddingValues(bottom = bottomContentPadding, top = Dimensions.PaddingMedium),
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
        } else if (!isLoading && profiles.isEmpty()) {
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
        } else {
          items(profiles, key = { it.user.uid }) { profile ->
            Box(
                modifier =
                    Modifier.padding(
                        horizontal = Dimensions.PaddingMedium,
                        vertical = Dimensions.PaddingSmall)) {
                  ProfileCard(
                      profile = profile,
                      viewModel = searchProfileViewModel,
                      onCardClick = onCardClick)
                }
          }
        }
      }
}

/**
 * Displays the row of tabs ("Explore", "Followers", "Following") corresponding to the pager pages.
 *
 * It manages the visual indicator animation and handles tab clicks to scroll the pager
 * to the corresponding page.
 *
 * @param pagerState The state of the pager, used to determine the selected tab and handle scroll animations.
 * @param titles A list of strings representing the titles for each tab.
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
