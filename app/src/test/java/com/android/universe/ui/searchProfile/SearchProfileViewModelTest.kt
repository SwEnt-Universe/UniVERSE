package com.android.universe.ui.searchProfile

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.model.user.FakeUserRepository
import com.android.universe.utils.MainCoroutineRule
import com.android.universe.utils.UserTestData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class SearchProfileViewModelTest {
  private lateinit var repository: FakeUserRepository
  private lateinit var viewModel: SearchProfileViewModel

  @get:Rule val mainCoroutinesRule = MainCoroutineRule()

  @Before
  fun setup() {
    repository = FakeUserRepository()

    runTest {
      repository.addUser(UserTestData.Alice)
      repository.addUser(UserTestData.Bob)
      repository.addUser(UserTestData.Rocky)
    }

    viewModel = SearchProfileViewModel(uid = UserTestData.Alice.uid, userRepository = repository)
  }

  @Test
  fun init_loadsAllDataAutomatically() = runTest {
    advanceUntilIdle()

    val state = viewModel.profilesState.value
    assertTrue("Explore list should be loaded", state.explore.isNotEmpty())
    assertFalse("Should not be in loading state", state.isLoading)
  }

  @Test
  fun loadInitialData_loadsCorrectFollowers() = runTest {
    // Bob + Rocky follow Alice
    repository.followUser(UserTestData.Bob.uid, UserTestData.Alice.uid)
    repository.followUser(UserTestData.Rocky.uid, UserTestData.Alice.uid)

    viewModel = SearchProfileViewModel(UserTestData.Alice.uid, repository)
    advanceUntilIdle()

    val followerIds = viewModel.profilesState.value.followers.map { it.user.uid }.toSet()

    assertEquals(setOf(UserTestData.Bob.uid, UserTestData.Rocky.uid), followerIds)
  }

  @Test
  fun loadInitialData_loadsCorrectFollowing() = runTest {
    // Alice follows Bob only
    repository.followUser(UserTestData.Alice.uid, UserTestData.Bob.uid)

    viewModel.loadInitialData()
    advanceUntilIdle()

    val followingIds = viewModel.profilesState.value.following.map { it.user.uid }

    assertEquals(listOf(UserTestData.Bob.uid), followingIds)
  }

  @Test
  fun loadExplore_loadsExploreRecommendations() = runTest {
    repository.followUser(UserTestData.Alice.uid, UserTestData.Bob.uid)

    viewModel.loadExplore()
    advanceUntilIdle()

    val exploreIds = viewModel.profilesState.value.explore.map { it.user.uid }

    assertFalse(exploreIds.contains(UserTestData.Alice.uid))
    assertFalse(exploreIds.contains(UserTestData.Bob.uid))
    assertTrue(exploreIds.contains(UserTestData.Rocky.uid))
  }

  @Test
  fun loadFollowers_loadsCorrectFollowers() = runTest {
    repository.followUser(UserTestData.Bob.uid, UserTestData.Alice.uid)
    repository.followUser(UserTestData.Rocky.uid, UserTestData.Alice.uid)

    viewModel.loadFollowers()
    advanceUntilIdle()

    val followerIds = viewModel.profilesState.value.followers.map { it.user.uid }.toSet()

    assertEquals(setOf(UserTestData.Bob.uid, UserTestData.Rocky.uid), followerIds)
  }

  @Test
  fun loadFollowing_loadsCorrectFollowing() = runTest {
    repository.followUser(UserTestData.Alice.uid, UserTestData.Bob.uid)

    viewModel.loadFollowing()
    advanceUntilIdle()

    val followingIds = viewModel.profilesState.value.following.map { it.user.uid }

    assertEquals(listOf(UserTestData.Bob.uid), followingIds)
  }

  @Test
  fun followOrUnfollowUser_followsUser() = runTest {
    repository.unfollowUser(UserTestData.Alice.uid, UserTestData.Bob.uid)

    viewModel.loadInitialData()
    advanceUntilIdle()

    val bobState =
        viewModel.profilesState.value.explore.first { it.user.uid == UserTestData.Bob.uid }

    viewModel.followOrUnfollowUser(bobState)
    advanceUntilIdle()

    val followers = repository.getFollowers(UserTestData.Bob.uid)

    assertTrue("Alice should now follow Bob", followers.any { it.uid == UserTestData.Alice.uid })
  }

  @Test
  fun followOrUnfollowUser_unfollowsUser() = runTest {
    repository.followUser(UserTestData.Alice.uid, UserTestData.Bob.uid)

    viewModel.loadInitialData()
    advanceUntilIdle()

    val bobState =
        viewModel.profilesState.value.following.first { it.user.uid == UserTestData.Bob.uid }

    viewModel.followOrUnfollowUser(bobState)
    advanceUntilIdle()

    val followers = repository.getFollowers(UserTestData.Bob.uid)

    assertFalse(
        "Alice should no longer follow Bob", followers.any { it.uid == UserTestData.Alice.uid })
  }

  @Test
  fun updateSearchQuery_updatesQueryState() = runTest {
    val testQuery = "Bob"

    viewModel.updateSearchQuery(testQuery)
    advanceUntilIdle()

    assertEquals(testQuery, viewModel.searchQuery.value)
  }
}
