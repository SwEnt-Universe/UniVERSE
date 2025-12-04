package com.android.universe.ui.searchProfile

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.model.user.FakeUserRepository
import com.android.universe.utils.UserTestData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class SearchProfileViewModelTest {
  private lateinit var repository: FakeUserRepository
  private lateinit var viewModel: SearchProfileViewModel

  @Before
  fun setup() {
    repository = FakeUserRepository()

    runTest {
      repository.addUser(UserTestData.Alice)
      repository.addUser(UserTestData.Bob)
      repository.addUser(UserTestData.Rocky)
    }

    viewModel = SearchProfileViewModel(repository)

    viewModel.currentUserId = UserTestData.Alice.uid
  }

  @Test
  fun loadExplore_populatesExploreList() = runTest {
    viewModel.loadExplore()
    advanceUntilIdle()

    val explore = viewModel.profilesState.value.explore

    assertTrue("Explore list should not be empty", explore.isNotEmpty())
  }

  @Test
  fun loadFollowers_loadsCorrectFollowers() = runTest {
    // Bob + Rocky follow Alice
    repository.followUser(UserTestData.Bob.uid, UserTestData.Alice.uid)
    repository.followUser(UserTestData.Rocky.uid, UserTestData.Alice.uid)

    viewModel.loadFollowers()
    advanceUntilIdle()

    val followers = viewModel.profilesState.value.followers.map { it.user.uid }

    assertEquals(
        setOf("Bob", "Rocky"), followers.toSet().map { repository.getUser(it).username }.toSet())
  }

  @Test
  fun loadFollowing_loadsCorrectFollowing() = runTest {
    // Alice follows Bob only
    repository.followUser(UserTestData.Alice.uid, UserTestData.Bob.uid)

    viewModel.loadFollowing()
    advanceUntilIdle()

    val following = viewModel.profilesState.value.following.map { it.user.uid }

    assertEquals(listOf(UserTestData.Bob.uid), following)
  }

  @Test
  fun followOrUnfollowUser_followsUser() = runTest {
    // Ensure Alice is NOT following Bob
    repository.unfollowUser(UserTestData.Alice.uid, UserTestData.Bob.uid)

    viewModel.loadExplore()
    advanceUntilIdle()

    val bobState =
        viewModel.profilesState.value.explore.first { it.user.uid == UserTestData.Bob.uid }

    viewModel.followOrUnfollowUser(bobState)
    advanceUntilIdle()

    val bobFollowers = repository.getFollowers(UserTestData.Bob.uid)

    assertTrue("Alice should now follow Bob", bobFollowers.any { it.uid == UserTestData.Alice.uid })
  }

  @Test
  fun followOrUnfollowUser_unfollowsUser() = runTest {
    repository.followUser(UserTestData.Alice.uid, UserTestData.Bob.uid)

    viewModel.loadFollowing()
    advanceUntilIdle()

    val bobState =
        viewModel.profilesState.value.following.first { it.user.uid == UserTestData.Bob.uid }

    viewModel.followOrUnfollowUser(bobState)
    advanceUntilIdle()

    val bobFollowers = repository.getFollowers(UserTestData.Bob.uid)

    assertFalse(
        "Alice should no longer follow Bob", bobFollowers.any { it.uid == UserTestData.Alice.uid })
  }
}
