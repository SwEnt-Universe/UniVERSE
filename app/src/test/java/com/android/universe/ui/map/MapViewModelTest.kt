package com.android.universe.ui.map

import app.cash.turbine.test
import com.android.universe.model.location.Location
import com.android.universe.model.location.LocationRepository
import com.android.universe.model.map.MapUiState
import com.tomtom.sdk.location.GeoPoint
import io.mockk.every
import io.mockk.mockk
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MapViewModelTest {

  private lateinit var viewModel: MapViewModel
  private lateinit var repository: LocationRepository

  private val testDispatcher = StandardTestDispatcher()

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    repository = mockk(relaxed = true)
    viewModel = MapViewModel(repository)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun `loadLastKnownLocation emits PermissionRequired when no permission`() = runTest {
    every { repository.hasLocationPermission() } returns false

    viewModel.loadLastKnownLocation()

    assertTrue(viewModel.uiState.value is MapUiState.PermissionRequired)
  }

  @Test
  fun `loadLastKnownLocation emits LocationAvailable on success`() = runTest {
    val fakeLocation = Location(latitude = 46.5196, longitude = 6.5685)
    every { repository.hasLocationPermission() } returns true
    every { repository.getLastKnownLocation(any(), any()) } answers
        {
          firstArg<(Location) -> Unit>().invoke(fakeLocation)
        }

    viewModel.loadLastKnownLocation()

    assertEquals(fakeLocation, viewModel.userLocation.value)
    assertTrue(viewModel.uiState.value is MapUiState.LocationAvailable)
  }

  @Test
  fun `startLocationTracking emits user locations`() = runTest {
    val fakeFlow = flowOf(Location(46.5, 6.5), Location(46.6, 6.6))
    every { repository.hasLocationPermission() } returns true
    every { repository.startLocationTracking() } returns fakeFlow

    viewModel.startLocationTracking()

    testDispatcher.scheduler.advanceUntilIdle()

    assertEquals(Location(46.6, 6.6), viewModel.userLocation.value)
    assertTrue(viewModel.uiState.value is MapUiState.Tracking)
  }

  @Test
  fun `startLocationTracking emits Error on exception`() = runTest {
    every { repository.hasLocationPermission() } returns true
    every { repository.startLocationTracking() } returns
        flow { throw RuntimeException("Location tracking failed") }

    viewModel.startLocationTracking()

    testDispatcher.scheduler.advanceUntilIdle()

    val state = viewModel.uiState.value
    assertTrue(state is MapUiState.Error)
    assertTrue(state.message.contains("failed"))
  }

  @Test
  fun `centerOn emits CameraOptions`() = runTest {
    viewModel.cameraCommands.test {
      val point = GeoPoint(46.5196, 6.5685)
      viewModel.centerOn(point, 10.0)

      val emitted = awaitItem()
      assertEquals(point, emitted.position)
      cancelAndIgnoreRemainingEvents()
    }
  }
}
