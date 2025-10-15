package com.android.universe.ui.map

import app.cash.turbine.test
import com.android.universe.model.location.Location
import com.android.universe.model.location.LocationRepository
import com.tomtom.sdk.location.GeoPoint
import io.mockk.every
import io.mockk.mockk
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
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
  fun `loadLastKnownLocation emits Success on success`() = runTest {
    val fakeLocation = Location(latitude = 46.5196, longitude = 6.5685)
    every { repository.getLastKnownLocation(any(), any()) } answers
        {
          firstArg<(Location) -> Unit>().invoke(fakeLocation)
        }

    viewModel.loadLastKnownLocation()
    testDispatcher.scheduler.advanceUntilIdle()

    val state = viewModel.uiState.value
    assertTrue(state.location == fakeLocation)
    assertNull(state.error)
    assertFalse(state.isLoading)
  }

  @Test
  fun `loadLastKnownLocation emits Error on failure and centers on Lausanne`() = runTest {
    every { repository.getLastKnownLocation(any(), any()) } answers
        {
          secondArg<() -> Unit>().invoke()
        }

    viewModel.loadLastKnownLocation()
    testDispatcher.scheduler.advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals("No last known location available", state.error)
    assertFalse(state.isLoading)
  }

  @Test
  fun `startLocationTracking emits latest location and clears error`() = runTest {
    val fakeFlow = flowOf(Location(46.5, 6.5), Location(46.6, 6.6))
    every { repository.startLocationTracking() } returns fakeFlow

    viewModel.startLocationTracking()
    testDispatcher.scheduler.advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals(Location(46.6, 6.6), state.location)
    assertNull(state.error)
  }

  @Test
  fun `startLocationTracking emits Error on exception`() = runTest {
    every { repository.startLocationTracking() } returns
        flow { throw RuntimeException("Location tracking failed") }

    viewModel.startLocationTracking()
    testDispatcher.scheduler.advanceUntilIdle()

    val state = viewModel.uiState.value
    assertTrue(state.error!!.contains("Location tracking failed"))
  }

  @Test
  fun `centerOn emits CameraOptions`() = runTest {
    viewModel.cameraCommands.test {
      val point = GeoPoint(46.5196, 6.5685)
      viewModel.centerOn(point, 10.0)

      val emitted = awaitItem()
      assertEquals(point, emitted.position)
      assertEquals(10.0, emitted.zoom)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `centerOnLausanne emits correct CameraOptions`() = runTest {
    viewModel.cameraCommands.test {
      viewModel.centerOnLausanne()
      val emitted = awaitItem()
      assertEquals(46.5196535, emitted.position!!.latitude, 0.00001)
      assertEquals(6.6322734, emitted.position!!.longitude, 0.00001)
      assertEquals(14.0, emitted.zoom)
      cancelAndIgnoreRemainingEvents()
    }
  }
}
