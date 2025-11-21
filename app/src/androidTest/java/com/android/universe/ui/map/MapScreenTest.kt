package com.android.universe.ui.map

import android.Manifest
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.android.universe.model.event.FakeEventRepository
import com.android.universe.model.location.FakeLocationRepository
import com.android.universe.model.user.FakeUserRepository
import com.android.universe.ui.navigation.Tab
import com.android.universe.utils.EventTestData
import com.android.universe.utils.UserTestData
import com.android.universe.utils.setContentWithStubBackdrop
import com.tomtom.sdk.location.GeoPoint
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MapScreenTest {
  companion object {
    const val commonLat = 46.5196535
    const val commonLng = 6.632
  }

  @get:Rule val composeTestRule = createComposeRule()

  @get:Rule
  val permissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(Manifest.permission.ACCESS_FINE_LOCATION)

  private lateinit var uid: String
  private lateinit var fakeLocationRepository: FakeLocationRepository

  private lateinit var fakeEventRepository: FakeEventRepository
  private lateinit var fakeUserRepository: FakeUserRepository
  private lateinit var viewModel: MapViewModel

  @Before
  fun setUp() {
    uid = UserTestData.Alice.uid
    fakeLocationRepository = FakeLocationRepository()
    fakeEventRepository = FakeEventRepository()
    fakeUserRepository = FakeUserRepository()
    // Really important, the MapViewModel init using a load event functions, this breaks if no user
    // are in the repository
    runTest { fakeUserRepository.addUser(UserTestData.Alice) }
    viewModel =
        MapViewModel(
            prefs = mockk(relaxed = true),
            currentUserId = uid,
            locationRepository = fakeLocationRepository,
            eventRepository = fakeEventRepository,
            userRepository = fakeUserRepository)
  }

  @Test
  fun mapIsDisplayed() {

    composeTestRule.setContentWithStubBackdrop {
      MapScreenTestWrapper(uid = uid, viewModel = viewModel, onTabSelected = {})
    }

    composeTestRule.onNodeWithTag(MapScreenTestTags.MAP_VIEW).assertIsDisplayed()
  }

  @Test
  fun eventCreationButtonAppearsAndClickable() {
    var accessed = false

    composeTestRule.setContentWithStubBackdrop {
      MapScreenTestWrapper(
          uid = uid,
          viewModel = viewModel,
          onTabSelected = {},
          createEvent = { _, _ -> accessed = true })
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(MapScreenTestTags.MAP_VIEW).assertIsDisplayed()
    viewModel.selectLocation(GeoPoint(commonLat, commonLng))
    composeTestRule.waitUntil(5_000L) {
      composeTestRule.onNodeWithTag(MapScreenTestTags.CREATE_EVENT_BUTTON).isDisplayed()
    }
    composeTestRule.onNodeWithTag(MapScreenTestTags.CREATE_EVENT_BUTTON).performClick()
    composeTestRule.waitUntil(1_000L) { accessed }
  }

  @Test
  fun eventInfoPopupAppearsWhenEventSelected() {
    val testEvent = EventTestData.dummyEvent3

    runTest { fakeEventRepository.addEvent(testEvent) }

    composeTestRule.setContentWithStubBackdrop {
      MapScreenTestWrapper(uid = uid, viewModel = viewModel, onTabSelected = {})
    }

    composeTestRule.waitForIdle()

    viewModel.selectEvent(testEvent)
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(MapScreenTestTags.EVENT_INFO_POPUP).assertIsDisplayed()

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText(EventTestData.dummyEvent3.title).assertIsDisplayed()
  }

  @Test
  fun multipleEventsCanBeLoadedAndSelected() {
    val event1 = EventTestData.dummyEvent1
    val event2 = EventTestData.dummyEvent2

    runTest {
      fakeEventRepository.addEvent(event1)
      fakeEventRepository.addEvent(event2)
    }

    composeTestRule.setContentWithStubBackdrop {
      MapScreenTestWrapper(uid = uid, viewModel = viewModel, onTabSelected = {})
    }

    composeTestRule.waitForIdle()
    viewModel.loadAllEvents()

    composeTestRule.waitUntil(5_000L) { viewModel.eventMarkers.value.size == 2 }

    viewModel.selectEvent(event1)
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(MapScreenTestTags.EVENT_INFO_POPUP).assertIsDisplayed()
    assert(viewModel.selectedEvent.value?.title == EventTestData.dummyEvent1.title)

    viewModel.selectEvent(null)
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(MapScreenTestTags.EVENT_INFO_POPUP).assertIsNotDisplayed()

    viewModel.selectEvent(event2)
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(MapScreenTestTags.EVENT_INFO_POPUP).assertIsDisplayed()
    assert(viewModel.selectedEvent.value?.title == EventTestData.dummyEvent2.title)
  }
}

/** Wrapper to add test tags to MapScreen for Compose testing */
@Composable
fun MapScreenTestWrapper(
    uid: String,
    viewModel: MapViewModel,
    onTabSelected: (Tab) -> Unit,
    createEvent: (latitude: Double, longitude: Double) -> Unit = { lat, lng -> }
) {
  Box {
    MapScreen(
        uid = uid, viewModel = viewModel, onTabSelected = onTabSelected, createEvent = createEvent)
  }
}
