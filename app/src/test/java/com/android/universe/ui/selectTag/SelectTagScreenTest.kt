package com.android.universe.ui.selectTag

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.model.event.EventLocalTemporaryRepository
import com.android.universe.model.event.EventRepository
import com.android.universe.model.event.EventTemporaryRepository
import com.android.universe.model.event.FakeEventRepository
import com.android.universe.model.tag.Tag
import com.android.universe.model.tag.TagLocalTemporaryRepository
import com.android.universe.model.tag.TagTemporaryRepository
import com.android.universe.model.user.FakeUserRepository
import com.android.universe.model.user.UserRepository
import com.android.universe.ui.navigation.FlowBottomMenuTestTags
import com.android.universe.utils.UserTestData
import com.android.universe.utils.setContentWithStubBackdrop
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SelectTagScreenTest {

  @get:Rule val composeTestRule = createComposeRule()
  private lateinit var userRepository: UserRepository
  private lateinit var tagRepository: TagTemporaryRepository
  private lateinit var eventTemporaryRepository: EventTemporaryRepository
  private lateinit var eventRepository: EventRepository
  private lateinit var viewModel: SelectTagViewModel

  companion object {
    private val dummyUser = UserTestData.NoTagsUser
    private const val LAZY_COLUMN_TAGS = SelectTagsScreenTestTags.LAZY_COLUMN
    private val BUTTON_METAL = SelectTagsScreenTestTags.tagItem(Tag.METAL)
    private val BUTTON_HANDBALL = SelectTagsScreenTestTags.tagItem(Tag.HANDBALL)
    private val BUTTON_BAKING = SelectTagsScreenTestTags.tagItem(Tag.BAKING)
  }

  private fun launchDefaultScreen() {
    composeTestRule.setContentWithStubBackdrop {
      SelectTagScreen(selectedTagOverview = viewModel, uid = dummyUser.uid)
    }
  }

  @Before
  fun setUp() {
    userRepository = FakeUserRepository()
    tagRepository = TagLocalTemporaryRepository()
    eventRepository = FakeEventRepository()
    eventTemporaryRepository = EventLocalTemporaryRepository()
    runTest { userRepository.addUser(dummyUser) }
    viewModel =
        SelectTagViewModel(
            uid = dummyUser.uid,
            mode = SelectTagMode.USER_PROFILE,
            userRepository = userRepository,
            tagRepository = tagRepository,
            eventRepository = eventRepository,
            eventTemporaryRepository = eventTemporaryRepository)
  }

  @Test
  fun allTagCategoriesAreDisplayedAndScrollable() {
    launchDefaultScreen()

    val categories =
        listOf(
            SelectTagsScreenTestTags.MUSIC_TAGS,
            SelectTagsScreenTestTags.SPORT_TAGS,
            SelectTagsScreenTestTags.FOOD_TAGS,
            SelectTagsScreenTestTags.ART_TAGS,
            SelectTagsScreenTestTags.TRAVEL_TAGS,
            SelectTagsScreenTestTags.GAMES_TAGS,
            SelectTagsScreenTestTags.TECHNOLOGY_TAGS,
            SelectTagsScreenTestTags.TOPIC_TAGS)

    categories.forEach { categoryTag ->
      composeTestRule.onNodeWithTag(LAZY_COLUMN_TAGS).performScrollToNode(hasTestTag(categoryTag))

      composeTestRule.onNodeWithTag(categoryTag).assertIsDisplayed()
    }
  }

  @Test
  fun flowBottomMenu_ButtonsAreDisplayed() {
    launchDefaultScreen()
    composeTestRule.onNodeWithTag(FlowBottomMenuTestTags.BACK_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(FlowBottomMenuTestTags.CONFIRM_BUTTON).assertIsDisplayed()
  }

  @Test
  fun flowBottomMenu_ButtonsHaveClickAction() {
    launchDefaultScreen()
    composeTestRule.onNodeWithTag(FlowBottomMenuTestTags.BACK_BUTTON).assertHasClickAction()
    composeTestRule.onNodeWithTag(FlowBottomMenuTestTags.CONFIRM_BUTTON).assertHasClickAction()
  }

  @Test
  fun selectingTags_InteractionsWork() {
    launchDefaultScreen()

    composeTestRule
        .onNodeWithTag(LAZY_COLUMN_TAGS)
        .performScrollToNode(hasTestTag(SelectTagsScreenTestTags.MUSIC_TAGS))

    composeTestRule
        .onNodeWithTag(SelectTagsScreenTestTags.MUSIC_TAGS)
        .performScrollToNode(hasTestTag(BUTTON_METAL))

    composeTestRule.onNodeWithTag(BUTTON_METAL).performClick()

    composeTestRule.onNodeWithTag(BUTTON_METAL).performClick()
  }

  @Test
  fun selectingMultipleTagsFromDifferentCategories() {
    launchDefaultScreen()

    composeTestRule.onNodeWithTag(LAZY_COLUMN_TAGS).performScrollToNode(hasTestTag(BUTTON_METAL))
    composeTestRule.onNodeWithTag(BUTTON_METAL).performClick()

    composeTestRule.onNodeWithTag(LAZY_COLUMN_TAGS).performScrollToNode(hasTestTag(BUTTON_HANDBALL))
    composeTestRule.onNodeWithTag(BUTTON_HANDBALL).performClick()

    composeTestRule.onNodeWithTag(LAZY_COLUMN_TAGS).performScrollToNode(hasTestTag(BUTTON_BAKING))
    composeTestRule.onNodeWithTag(BUTTON_BAKING).performClick()

    composeTestRule.onNodeWithTag(FlowBottomMenuTestTags.CONFIRM_BUTTON).assertIsDisplayed()
  }

  @Test
  fun confirmButton_TriggersSaveAction() {
    launchDefaultScreen()

    composeTestRule.onNodeWithTag(FlowBottomMenuTestTags.CONFIRM_BUTTON).performClick()
  }
}
