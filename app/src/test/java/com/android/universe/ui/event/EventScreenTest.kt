package com.android.universe.ui.event

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.model.event.FakeEventRepository
import com.android.universe.model.user.FakeUserRepository
import com.android.universe.utils.EventTestData
import com.android.universe.utils.MainCoroutineRule
import com.android.universe.utils.UserTestData
import com.android.universe.utils.setContentWithStubBackdrop
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class EventScreenTest {

  @get:Rule val composeTestRule = createComposeRule()
  @get:Rule val mainCoroutineRule = MainCoroutineRule()

  private lateinit var fakeEventRepository: FakeEventRepository
  private lateinit var fakeUserRepository: FakeUserRepository
  private lateinit var viewModel: EventViewModel

  companion object {
    private val firstEvent = EventTestData.FullDescriptionEvent
    private val secondEvent = EventTestData.NullDescriptionEvent
    private val thirdEvent = EventTestData.NoTagsEvent
    private val sampleEvents = listOf(firstEvent, secondEvent, thirdEvent)
    private val sampleUsers =
        listOf(UserTestData.NullDescription, UserTestData.ManyTagsUser, UserTestData.Alice)
  }

  @Before
  fun setUp() {
    // Create a fresh fake repository for every test (isolated)
    fakeEventRepository = FakeEventRepository()
    fakeUserRepository = FakeUserRepository()

    runTest {
      sampleEvents.forEach { fakeEventRepository.addEvent(it) }
      sampleUsers.forEach { fakeUserRepository.addUser(it) }
    }

    viewModel = EventViewModel(fakeEventRepository, null, fakeUserRepository)

    composeTestRule.setContentWithStubBackdrop { EventScreen(viewModel = viewModel) }

    viewModel.loadEvents()

    runTest { advanceUntilIdle() }
    // Wait until Compose settles
    composeTestRule.waitForIdle()
  }

  @Test
  fun displayEventCards() = runTest {
    // LazyColumn list is displayed
    composeTestRule.onNodeWithTag(EventScreenTestTags.EVENTS_LIST).assertIsDisplayed()

    // There should be at least one event card: fetch semantics nodes and assert non-empty
    val cards = composeTestRule.onAllNodesWithTag("${EventCardTestTags.EVENT_CARD}_0")
    assertTrue(cards.fetchSemanticsNodes().isNotEmpty())
  }
}
