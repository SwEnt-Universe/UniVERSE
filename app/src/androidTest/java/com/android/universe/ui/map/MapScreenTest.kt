package com.android.universe.ui.map

import android.Manifest
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.rule.GrantPermissionRule
import com.android.universe.model.event.FakeEventRepository
import com.android.universe.model.location.FakeLocationRepository
import com.android.universe.model.user.FakeUserRepository
import com.android.universe.ui.navigation.Tab
import org.junit.Rule
import org.junit.Test

class MapScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @get:Rule
  val permissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(Manifest.permission.ACCESS_FINE_LOCATION)

  private lateinit var uid: String
  private lateinit var fakeLocationRepository: FakeLocationRepository

  private lateinit var fakeEventRepository: FakeEventRepository
  private lateinit var fakeUserRepository: FakeUserRepository
  private lateinit var viewModel: MapViewModel

  @Test
  fun mapIsDisplayed() {
    uid = "test_uid"
    fakeLocationRepository = FakeLocationRepository()
    fakeEventRepository = FakeEventRepository()
    fakeUserRepository = FakeUserRepository()
    viewModel =
        MapViewModel(
            currentUserId = uid,
            locationRepository = fakeLocationRepository,
            eventRepository = fakeEventRepository,
            userRepository = fakeUserRepository)

    composeTestRule.setContent {
      MapScreenTestWrapper(uid = uid, viewModel = viewModel, onTabSelected = {})
    }

    composeTestRule.onNodeWithTag(MapScreenTestTags.MAP_VIEW).assertIsDisplayed()
  }
}

/** Wrapper to add test tags to MapScreen for Compose testing */
@Composable
fun MapScreenTestWrapper(uid: String, viewModel: MapViewModel, onTabSelected: (Tab) -> Unit) {
  Box { MapScreen(uid = uid, viewModel = viewModel, onTabSelected = onTabSelected) }
}
