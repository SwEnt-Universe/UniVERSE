package com.android.universe.ui.map

import app.cash.turbine.test
import com.android.universe.model.Tag
import com.android.universe.model.event.Event
import com.android.universe.model.event.EventRepository
import com.android.universe.model.location.Location
import com.android.universe.model.location.LocationRepository
import com.android.universe.model.user.UserProfile
import com.tomtom.sdk.location.GeoPoint
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import java.time.LocalDateTime
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
  private lateinit var locationRepository: LocationRepository

  private lateinit var eventRepository: EventRepository

  private val testDispatcher = StandardTestDispatcher()

  private val fakeEvents =
      listOf(
          Event(
              id = "event-001",
              title = "Morning Run at the Lake",
              description = "Join us for a casual 5km run around the lake followed by coffee.",
              date = LocalDateTime.of(2025, 10, 15, 7, 30),
              tags = setOf(Tag.JAZZ, Tag.COUNTRY),
              creator =
                  UserProfile(
                      uid = "0",
                      username = "alice_smith",
                      firstName = "Alice",
                      lastName = "Smith",
                      country = "US",
                      description = "Loves running",
                      dateOfBirth = LocalDate.of(1990, 1, 1),
                      tags = setOf(Tag.SCULPTURE)),
              location = Location(latitude = 46.5196535, longitude = 6.6322734)),
          Event(
              id = "event-002",
              title = "Tech Hackathon 2025",
              date = LocalDateTime.of(2025, 11, 3, 9, 0),
              tags = setOf(Tag.PROGRAMMING, Tag.ARTIFICIAL_INTELLIGENCE, Tag.BOAT),
              creator =
                  UserProfile(
                      uid = "0",
                      username = "alice_smith",
                      firstName = "Alice",
                      lastName = "Smith",
                      country = "US",
                      description = "Loves running",
                      dateOfBirth = LocalDate.of(1990, 1, 1),
                      tags = setOf(Tag.SCULPTURE)),
              location = Location(latitude = 37.423021, longitude = -122.086808)),
          Event(
              id = "event-003",
              title = "Art & Wine Evening",
              description = "Relaxed evening mixing painting, wine, and music.",
              date = LocalDateTime.of(2025, 10, 22, 19, 0),
              tags = setOf(Tag.SCULPTURE, Tag.MUSIC),
              creator =
                  UserProfile(
                      uid = "0",
                      username = "alice_smith",
                      firstName = "Alice",
                      lastName = "Smith",
                      country = "US",
                      description = "Loves running",
                      dateOfBirth = LocalDate.of(1990, 1, 1),
                      tags = setOf(Tag.SCULPTURE)),
              location = Location(latitude = 47.3769, longitude = 8.5417)))

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    locationRepository = mockk(relaxed = true)
    eventRepository = mockk(relaxed = true)
    viewModel = MapViewModel(locationRepository, eventRepository)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun `loadLastKnownLocation emits Success on success`() = runTest {
    val fakeLocation = Location(latitude = 46.5196, longitude = 6.5685)
    every { locationRepository.getLastKnownLocation(any(), any()) } answers
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
    every { locationRepository.getLastKnownLocation(any(), any()) } answers
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
    every { locationRepository.startLocationTracking() } returns fakeFlow

    viewModel.startLocationTracking()
    testDispatcher.scheduler.advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals(Location(46.6, 6.6), state.location)
    assertNull(state.error)
  }

  @Test
  fun `startLocationTracking emits Error on exception`() = runTest {
    every { locationRepository.startLocationTracking() } returns
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

  @Test
  fun `loadAllEvents updates eventMarkers on success`() = runTest {
    coEvery { eventRepository.getAllEvents() } returns fakeEvents

    viewModel.loadAllEvents()
    testDispatcher.scheduler.advanceUntilIdle()

    val markers = viewModel.eventMarkers.value
    assertEquals(fakeEvents.size, markers.size)
    assertEquals("Morning Run at the Lake", markers[0].title)
    assertEquals("Tech Hackathon 2025", markers[1].title)
    assertEquals("Art & Wine Evening", markers[2].title)
  }

  @Test
  fun `loadAllEvents sets error on failure`() = runTest {
    coEvery { eventRepository.getAllEvents() } throws RuntimeException("Failed to load")

    viewModel.loadAllEvents()
    testDispatcher.scheduler.advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals("Failed to load events: Failed to load", state.error)
  }
}
