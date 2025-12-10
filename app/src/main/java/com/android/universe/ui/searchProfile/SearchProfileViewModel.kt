package com.android.universe.ui.searchProfile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.universe.model.user.UserProfile
import com.android.universe.model.user.UserRepository
import com.android.universe.model.user.UserRepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
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

  private val explore = MutableStateFlow<List<UserProfile>>(emptyList())
  private val followers = MutableStateFlow<List<UserProfile>>(emptyList())
  private val following = MutableStateFlow<List<UserProfile>>(emptyList())

  private val currentUserFollowingIds = MutableStateFlow<Set<String>>(emptySet())

  private val _isLoading = MutableStateFlow(false)

  /** Combined state of profile tabs with filtering based on search query */
  val baseProfilesState: StateFlow<ProfileTabsState> =
      combine(explore, followers, following, currentUserFollowingIds, searchQuery) {
              exploreRaw,
              followersRaw,
              followingRaw,
              followingIds,
              query ->
            val q = query.lowercase()

            fun filter(list: List<UserProfile>) =
                if (q.isBlank()) list
                else
                    list.filter {
                      val full = "${it.firstName} ${it.lastName}".lowercase()
                      full.contains(q)
                    }

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

        explore.value = userRepository.getFollowRecommendations(uid)
        followers.value = userRepository.getFollowers(uid)
        following.value = userRepository.getFollowing(uid)
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
          userRepository.unfollowUser(uid, target.user.uid)
          currentUserFollowingIds.value = currentUserFollowingIds.value - target.user.uid
        } else {
          userRepository.followUser(uid, target.user.uid)
          currentUserFollowingIds.value = currentUserFollowingIds.value + target.user.uid
        }
      } catch (_: Exception) {
        setError("Failed to update follow status")
      }
    }
  }

  /**
   * Convert UserProfile to ProfileUIState
   *
   * @param followingIds The set of user IDs that the current user is following.
   * @return The corresponding ProfileUIState.
   */
  private fun UserProfile.toUI(followingIds: Set<String>) =
      ProfileUIState(this, isFollowing = followingIds.contains(uid))

  /**
   * Update search query and filter explore list
   *
   * @param query The new search query.
   */
  fun updateSearchQuery(query: String) {
    _searchQuery.value = query
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
