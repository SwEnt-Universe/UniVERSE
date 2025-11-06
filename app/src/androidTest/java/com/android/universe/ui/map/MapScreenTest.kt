package com.android.universe.ui.map

import android.Manifest
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.rule.GrantPermissionRule
import com.android.universe.model.event.FakeEventRepository
import com.android.universe.model.location.FakeLocationRepository
import com.android.universe.ui.navigation.Tab
import org.junit.Rule
import org.junit.Test

class MapScreenTest {
  companion object {
    const val commonLat = 46.5196535
    const val commonLng = 6.632
    const val buttonText = "Create your Event !"
  }

  @get:Rule val composeTestRule = createComposeRule()

  @get:Rule
  val permissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(Manifest.permission.ACCESS_FINE_LOCATION)

  private lateinit var fakeLocationRepository: FakeLocationRepository

  private lateinit var fakeEventRepository: FakeEventRepository
  private lateinit var viewModel: MapViewModel

  @Test
  fun mapIsDisplayed() {
    fakeLocationRepository = FakeLocationRepository()
    fakeEventRepository = FakeEventRepository()
    viewModel = MapViewModel(fakeLocationRepository, fakeEventRepository)

    composeTestRule.setContent { MapScreenTestWrapper(viewModel = viewModel, onTabSelected = {}) }

    composeTestRule.onNodeWithTag(MapScreenTestTags.MAP_VIEW).assertIsDisplayed()
  }

  @Test
  fun eventCreationButtonAppearsAndClickable() {
    fakeLocationRepository = FakeLocationRepository()
    fakeEventRepository = FakeEventRepository()
    viewModel = MapViewModel(fakeLocationRepository, fakeEventRepository)
    var accessed = false
    composeTestRule.setContent {
      MapScreenTestWrapper(
          viewModel = viewModel, onTabSelected = {}, createEvent = { _, _ -> accessed = true })
    }

    composeTestRule.onNodeWithTag(MapScreenTestTags.MAP_VIEW).assertIsDisplayed()
    viewModel.selectLocation(commonLat, commonLng)
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(MapScreenTestTags.CREATE_EVENT_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(MapScreenTestTags.CREATE_EVENT_BUTTON).performClick()
    composeTestRule.waitUntil(1000, { accessed })

    assert(accessed)
  }
}

/** Wrapper to add test tags to MapScreen for Compose testing */
@Composable
fun MapScreenTestWrapper(
    viewModel: MapViewModel,
    onTabSelected: (Tab) -> Unit,
    createEvent: (latitude: Double, longitude: Double) -> Unit = { lat, lng -> }
) {
  Box { MapScreen(viewModel = viewModel, onTabSelected = onTabSelected, createEvent = createEvent) }
}
