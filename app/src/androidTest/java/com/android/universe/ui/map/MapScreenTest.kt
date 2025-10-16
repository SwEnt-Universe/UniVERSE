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
import com.android.universe.ui.navigation.Tab
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MapScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @get:Rule val permissionRule = GrantPermissionRule.grant(Manifest.permission.ACCESS_FINE_LOCATION)

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
}

/** Wrapper to add test tags to MapScreen for Compose testing */
@Composable
fun MapScreenTestWrapper(viewModel: MapViewModel, onTabSelected: (Tab) -> Unit) {
  Box { MapScreen(onTabSelected = onTabSelected) }
}
