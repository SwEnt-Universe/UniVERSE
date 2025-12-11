package com.android.universe.ui.searchProfile

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.model.user.FakeUserRepository
import com.android.universe.ui.navigation.NavigationTestTags
import com.android.universe.utils.MainCoroutineRule
import com.android.universe.utils.UserTestData
import com.android.universe.utils.setContentWithStubBackdrop
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class SearchProfileScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @get:Rule val mainCoroutineRule = MainCoroutineRule()

  private lateinit var repository: FakeUserRepository
  private lateinit var viewModel: SearchProfileViewModel

  @Before
  fun setup() {
    repository = FakeUserRepository()

    runBlocking {
      repository.addUser(UserTestData.Alice)
      repository.addUser(UserTestData.Bob)
      repository.addUser(UserTestData.Rocky)
    }

    viewModel = SearchProfileViewModel(UserTestData.Alice.uid, repository)
  }

  @Test
  fun searchProfileScreen_displaysAllComponents() {
    composeTestRule.setContentWithStubBackdrop {
      SearchProfileScreen(uid = UserTestData.Alice.uid, searchProfileViewModel = viewModel)
    }

    composeTestRule.onNodeWithTag(NavigationTestTags.SEARCH_PROFILE_SCREEN).assertExists()
    composeTestRule.onNodeWithTag(SearchProfileScreenTestTags.HEADER).assertExists()
    composeTestRule.onNodeWithTag(SearchProfileScreenTestTags.SEARCH_BAR).assertExists()
    composeTestRule.onNodeWithTag(SearchProfileScreenTestTags.TAB_ROW).assertExists()
    composeTestRule.onNodeWithTag(SearchProfileScreenTestTags.PAGER).assertExists()
  }

  @Test
  fun searchProfileScreen_displaysTabs() {
    composeTestRule.setContentWithStubBackdrop {
      SearchProfileScreen(uid = UserTestData.Alice.uid, searchProfileViewModel = viewModel)
    }

    composeTestRule.onNodeWithTag(SearchProfileScreenTestTags.tab(0)).assertExists()
    composeTestRule.onNodeWithTag(SearchProfileScreenTestTags.tab(1)).assertExists()
    composeTestRule.onNodeWithTag(SearchProfileScreenTestTags.tab(2)).assertExists()

    composeTestRule.onNodeWithText("Explore").assertExists()
    composeTestRule.onNodeWithText("Followers").assertExists()
    composeTestRule.onNodeWithText("Following").assertExists()
  }

  @Test
  fun tabs_allThreeTabs_areNavigable() {
    composeTestRule.setContentWithStubBackdrop {
      SearchProfileScreen(uid = UserTestData.Alice.uid, searchProfileViewModel = viewModel)
    }

    composeTestRule.waitForIdle()

    // Start on Explore
    composeTestRule.onNodeWithTag(SearchProfileScreenTestTags.PAGE_EXPLORE).assertExists()

    // Navigate to Followers
    composeTestRule.onNodeWithText("Followers").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(SearchProfileScreenTestTags.PAGE_FOLLOWERS).assertExists()

    // Navigate to Following
    composeTestRule.onNodeWithText("Following").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(SearchProfileScreenTestTags.PAGE_FOLLOWING).assertExists()

    // Navigate back to Explore
    composeTestRule.onNodeWithText("Explore").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(SearchProfileScreenTestTags.PAGE_EXPLORE).assertExists()
  }

  @Test
  fun followersList_showsEmptyState_whenNoFollowers() {
    composeTestRule.setContentWithStubBackdrop {
      SearchProfileScreen(uid = UserTestData.Alice.uid, searchProfileViewModel = viewModel)
    }

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Followers").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(SearchProfileScreenTestTags.EMPTY).assertExists()
    composeTestRule.onNodeWithText("No profiles found").assertExists()
  }

  @Test
  fun followingList_showsEmptyState_whenNotFollowingAnyone() {
    composeTestRule.setContentWithStubBackdrop {
      SearchProfileScreen(uid = UserTestData.Alice.uid, searchProfileViewModel = viewModel)
    }

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Following").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(SearchProfileScreenTestTags.EMPTY).assertExists()
    composeTestRule.onNodeWithText("No profiles found").assertExists()
  }
}
