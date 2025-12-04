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
    val profileImage: ByteArray? = null,
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

  fun loadExplore() {
    viewModelScope.launch {
      try {
        val users = userRepository.getAllUsers().filter { it.uid != currentUserId }

        exploreRaw = users
        updateExploreUI(users)
      } catch (_: Exception) {
        setError("Failed to load explore users")
      }
    }
  }

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

  fun followOrUnfollowUser(targetUid: String, isFollowing: Boolean) {
    if (isFollowing) {
      unAddUser(targetUid)
    } else {
      addUser(targetUid)
    }
  }

  fun addUser(targetUid: String) {
    viewModelScope.launch {
      try {
        userRepository.followUser(currentUserId, targetUid)
        reloadLists()
      } catch (_: Exception) {
        setError("Failed to follow user")
      }
    }
  }

  fun unAddUser(targetUid: String) {
    viewModelScope.launch {
      try {
        userRepository.unfollowUser(currentUserId, targetUid)
        reloadLists()
      } catch (_: Exception) {
        setError("Failed to unfollow user")
      }
    }
  }

  private fun reloadLists() {
    loadExplore()
    loadFollowers()
    loadFollowing()
  }

  private suspend fun toProfileUIState(user: UserProfile): ProfileUIState {
    val followers = userRepository.getFollowers(user.uid)
    val following = userRepository.getFollowing(user.uid)

    return ProfileUIState(
        user = user,
        profileImage = user.profilePicture,
        followersCount = followers.size,
        followingCount = following.size,
        isFollowing = followingRaw.any { it.uid == user.uid })
  }

  private fun updateExploreUI(users: List<UserProfile>) {
    viewModelScope.launch {
      val uiStates = users.map { toProfileUIState(it) }
      _profilesState.value = _profilesState.value.copy(explore = uiStates)
    }
  }

  private fun updateFollowersUI(users: List<UserProfile>) {
    viewModelScope.launch {
      val uiStates = users.map { toProfileUIState(it) }
      _profilesState.value = _profilesState.value.copy(followers = uiStates)
    }
  }

  private fun updateFollowingUI(users: List<UserProfile>) {
    viewModelScope.launch {
      val uiStates = users.map { toProfileUIState(it) }
      _profilesState.value = _profilesState.value.copy(following = uiStates)
    }
  }

  fun updateSearchQuery(query: String) {
    _searchQuery.value = query
    filterExplore()
  }

  private fun filterExplore() {
    val q = searchQuery.value.lowercase()

    val filtered =
        exploreRaw.filter {
          it.firstName.lowercase().contains(q) || it.lastName.lowercase().contains(q)
        }

    updateExploreUI(filtered)
  }

  private fun setError(msg: String?) {
    _uiState.value = UiState(msg)
  }
}
