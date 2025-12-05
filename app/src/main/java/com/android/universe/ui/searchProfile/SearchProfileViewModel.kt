package com.android.universe.ui.searchProfile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.universe.model.user.UserProfile
import com.android.universe.model.user.UserRepository
import com.android.universe.model.user.UserRepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUIState(
    val user: UserProfile,
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val isFollowing: Boolean = false
)

data class ProfileTabsState(
    val explore: List<ProfileUIState> = emptyList(),
    val followers: List<ProfileUIState> = emptyList(),
    val following: List<ProfileUIState> = emptyList()
)

data class UiState(val errorMsg: String? = null)

/**
 * ViewModel for searching and managing user profiles.
 *
 * @param userRepository The repository to fetch user profiles from.
 */
class SearchProfileViewModel(
    private val userRepository: UserRepository = UserRepositoryProvider.repository,
) : ViewModel() {
  private val _uiState = MutableStateFlow(UiState())
  val uiState: StateFlow<UiState> = _uiState.asStateFlow()

  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  var currentUserId: String = ""

  private val _profilesState = MutableStateFlow(ProfileTabsState())
  val profilesState: StateFlow<ProfileTabsState> = _profilesState.asStateFlow()

  // To avoid recomposing UI state on every search
  private var exploreRaw = emptyList<UserProfile>()
  private var followersRaw = emptyList<UserProfile>()
  private var followingRaw = emptyList<UserProfile>()

  /** Load explore recommendations */
  fun loadExplore() {
    viewModelScope.launch {
      try {
        val users = userRepository.getFollowRecommendations(currentUserId)

        exploreRaw = users
        updateExploreUI(users)
      } catch (_: Exception) {
        setError("Failed to load explore users")
      }
    }
  }

  /** Load followers of current user */
  fun loadFollowers() {
    viewModelScope.launch {
      try {
        followersRaw = userRepository.getFollowers(currentUserId)
        updateFollowersUI(followersRaw)
      } catch (_: Exception) {
        setError("Failed to load followers")
      }
    }
  }

  /** Load following of current user */
  fun loadFollowing() {
    viewModelScope.launch {
      try {
        followingRaw = userRepository.getFollowing(currentUserId)
        updateFollowingUI(followingRaw)
      } catch (_: Exception) {
        setError("Failed to load following")
      }
    }
  }

  /**
   * Toggle follow/unfollow for a target user
   *
   * @param target The target user's profile UI state.
   */
  fun followOrUnfollowUser(target: ProfileUIState) {
    viewModelScope.launch {
      try {
        if (target.isFollowing) {
          userRepository.unfollowUser(currentUserId, target.user.uid)
        } else {
          userRepository.followUser(currentUserId, target.user.uid)
        }

        reloadLists()
      } catch (_: Exception) {
        setError("Failed to update follow status")
      }
    }
  }

  /** Reload all profile lists to reflect changes */
  private fun reloadLists() {
    loadExplore()
    loadFollowers()
    loadFollowing()
  }

  /**
   * Convert UserProfile to ProfileUIState
   *
   * @param user The user profile to convert.
   * @return The corresponding ProfileUIState.
   */
  private suspend fun toProfileUIState(user: UserProfile): ProfileUIState {
    val followers = userRepository.getFollowers(user.uid)
    val following = userRepository.getFollowing(user.uid)

    return ProfileUIState(
        user = user,
        followersCount = followers.size,
        followingCount = following.size,
        isFollowing = followingRaw.any { it.uid == user.uid })
  }

  /**
   * Update explore tab UI state
   *
   * @param users The list of user profiles to update.
   */
  private fun updateExploreUI(users: List<UserProfile>) {
    viewModelScope.launch {
      val uiStates = users.map { toProfileUIState(it) }
      _profilesState.value = _profilesState.value.copy(explore = uiStates)
    }
  }

  /**
   * Update followers tab UI state
   *
   * @param users The list of user profiles to update.
   */
  private fun updateFollowersUI(users: List<UserProfile>) {
    viewModelScope.launch {
      val uiStates = users.map { toProfileUIState(it) }
      _profilesState.value = _profilesState.value.copy(followers = uiStates)
    }
  }

  /**
   * Update following tab UI state
   *
   * @param users The list of user profiles to update.
   */
  private fun updateFollowingUI(users: List<UserProfile>) {
    viewModelScope.launch {
      val uiStates = users.map { toProfileUIState(it) }
      _profilesState.value = _profilesState.value.copy(following = uiStates)
    }
  }

  /**
   * Update search query and filter explore list
   *
   * @param query The new search query.
   */
  fun updateSearchQuery(query: String) {
    _searchQuery.value = query
    filterExplore()
  }

  /** Filter explore list based on search query */
  private fun filterExplore() {
    val q = searchQuery.value.lowercase()

    val filtered =
        exploreRaw.filter {
          it.firstName.lowercase().contains(q) || it.lastName.lowercase().contains(q)
        }

    updateExploreUI(filtered)
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
