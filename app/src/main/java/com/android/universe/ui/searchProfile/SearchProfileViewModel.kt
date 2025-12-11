package com.android.universe.ui.searchProfile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.universe.model.tag.Tag.Category
import com.android.universe.model.user.UserProfile
import com.android.universe.model.user.UserRepository
import com.android.universe.model.user.UserRepositoryProvider
import com.android.universe.ui.search.SearchEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUIState(val user: UserProfile, val isFollowing: Boolean = false)

data class ProfileTabsState(
    val explore: List<ProfileUIState> = emptyList(),
    val followers: List<ProfileUIState> = emptyList(),
    val following: List<ProfileUIState> = emptyList(),
    val isLoading: Boolean = false
)

data class UiState(val errorMsg: String? = null)

/**
 * ViewModel for searching and managing user profiles.
 *
 * @param userRepository The repository to fetch user profiles from.
 */
class SearchProfileViewModel(
    private val uid: String,
    private val userRepository: UserRepository = UserRepositoryProvider.repository,
) : ViewModel() {
  private val _uiState = MutableStateFlow(UiState())
  val uiState: StateFlow<UiState> = _uiState.asStateFlow()

  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  private val _categories = MutableStateFlow<Set<Category>>(emptySet())
  val categories: StateFlow<Set<Category>> = _categories.asStateFlow()

  private val explore = MutableStateFlow<List<UserProfile>>(emptyList())
  private val followers = MutableStateFlow<List<UserProfile>>(emptyList())
  private val following = MutableStateFlow<List<UserProfile>>(emptyList())

  private val currentUserFollowingIds = MutableStateFlow<Set<String>>(emptySet())

  private val _isLoading = MutableStateFlow(false)

  /** Combined state of profile tabs with filtering based on search query */
  val baseProfilesState: StateFlow<ProfileTabsState> =
      combine(explore, followers, following, currentUserFollowingIds, searchQuery, categories) { arr
            ->
            val exploreRaw = arr[0] as List<UserProfile>
            val followersRaw = arr[1] as List<UserProfile>
            val followingRaw = arr[2] as List<UserProfile>
            val followingIds = arr[3] as Set<String>
            val query = arr[4] as String
            val cats = arr[5] as Set<Category>

            val q = query.lowercase()

            fun filter(list: List<UserProfile>) =
                if (q.isBlank())
                    list
                        .filter { SearchEngine.tagMatch(it.tags, cats) }
                        .sortedWith(SearchEngine.categoryCoverageComparator(cats) { e -> e.tags })
                        .reversed()
                else
                    list
                        .filter {
                          val full = "${it.firstName} ${it.lastName}".lowercase()
                          full.contains(q)
                        }
                        .filter { SearchEngine.tagMatch(it.tags, cats) }
                        .sortedWith(SearchEngine.categoryCoverageComparator(cats) { e -> e.tags })
                        .reversed()

            ProfileTabsState(
                explore = filter(exploreRaw).map { it.toUI(followingIds) },
                followers = filter(followersRaw).map { it.toUI(followingIds) },
                following = filter(followingRaw).map { it.toUI(followingIds) },
                isLoading = false)
          }
          .stateIn(viewModelScope, SharingStarted.Eagerly, ProfileTabsState())

  /** Final profiles state including loading state */
  val profilesState: StateFlow<ProfileTabsState> =
      combine(baseProfilesState, _isLoading) { base, loading -> base.copy(isLoading = loading) }
          .stateIn(viewModelScope, SharingStarted.Eagerly, ProfileTabsState())

  init {
    loadInitialData()
  }

  /** Load initial data for explore, followers, and following lists. */
  fun loadInitialData() {
    viewModelScope.launch {
      _isLoading.value = true
      try {
        currentUserFollowingIds.value = userRepository.getFollowing(uid).map { it.uid }.toSet()
      } catch (_: Exception) {
        setError("Failed to load initial data")
      } finally {
        _isLoading.value = false
      }
    }
  }

  /** Load user's explore recommendations */
  fun loadExplore() =
      viewModelScope.launch {
        _isLoading.value = true
        try {
          explore.value = userRepository.getFollowRecommendations(uid)
        } catch (_: Exception) {
          setError("Failed to load explore")
        } finally {
          _isLoading.value = false
        }
      }

  /** Load user's followers list */
  fun loadFollowers() =
      viewModelScope.launch {
        _isLoading.value = true
        try {
          followers.value = userRepository.getFollowers(uid)
        } catch (_: Exception) {
          setError("Failed to load followers")
        } finally {
          _isLoading.value = false
        }
      }

  /** Load user's following list */
  fun loadFollowing() =
      viewModelScope.launch {
        _isLoading.value = true
        try {
          following.value = userRepository.getFollowing(uid)
        } catch (_: Exception) {
          setError("Failed to load following")
        } finally {
          _isLoading.value = false
        }
      }

  /**
   * Toggle follow/unfollow for a target user and update relevant lists accordingly.
   *
   * @param target The target user's profile UI state.
   */
  fun followOrUnfollowUser(target: ProfileUIState) {
    viewModelScope.launch {
      try {
        if (target.isFollowing) {
          updateFollowerCount(target.user, isFollowing = true)
          userRepository.unfollowUser(uid, target.user.uid)
          currentUserFollowingIds.value = currentUserFollowingIds.value - target.user.uid
        } else {
          updateFollowerCount(target.user, isFollowing = false)
          userRepository.followUser(uid, target.user.uid)
          currentUserFollowingIds.value = currentUserFollowingIds.value + target.user.uid
        }
      } catch (_: Exception) {
        setError("Failed to update follow status")
      }
    }
  }

  /** Update follower count in all relevant lists after follow/unfollow action */
  private fun updateFollowerCount(targetUser: UserProfile, isFollowing: Boolean) {
    val updated =
        if (isFollowing) {
          targetUser.followers - uid
        } else {
          targetUser.followers + uid
        }
    val updatedProfile = targetUser.copy(followers = updated)
    explore.value = explore.value.map { if (it.uid == targetUser.uid) updatedProfile else it }
    followers.value = followers.value.map { if (it.uid == targetUser.uid) updatedProfile else it }
    following.value = following.value.map { if (it.uid == targetUser.uid) updatedProfile else it }
  }

  /**
   * Convert UserProfile to ProfileUIState
   *
   * @param followingIds The set of user IDs that the current user is following.
   * @return The corresponding ProfileUIState.
   */
  private fun UserProfile.toUI(followingIds: Set<String>) =
      ProfileUIState(this, isFollowing = followingIds.contains(this.uid))

  /**
   * Update search query and filter explore list
   *
   * @param query The new search query.
   */
  fun updateSearchQuery(query: String) {
    _searchQuery.value = query
  }

  /**
   * Selects a category.
   *
   * @param category The category to select.
   * @param select Whether to select or deselect the category. true means selecting, false means
   *   deselecting.
   */
  fun selectCategory(category: Category, select: Boolean) {
    if (select) _categories.update { it + category } else _categories.update { it - category }
  }

  /**
   * Set error message in UI state
   *
   * @param msg The error message to set.
   */
  private fun setError(msg: String?) {
    _uiState.value = UiState(msg)
  }
}
