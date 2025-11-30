package com.android.universe.ui.map

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.android.universe.R
import com.android.universe.di.DefaultDP
import com.android.universe.model.ai.orchestration.AIEventGenOrchestrator
import com.android.universe.model.event.Event
import com.android.universe.model.event.EventRepository
import com.android.universe.model.location.Location
import com.android.universe.model.location.LocationRepository
import com.android.universe.model.tag.Tag
import com.android.universe.model.user.UserRepository
import com.android.universe.utils.EventTestData
import com.android.universe.utils.MainCoroutineRule
import com.android.universe.utils.UserTestData
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.map.display.map.VisibleRegion
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import java.time.LocalDateTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class MapViewModelTest {
  private lateinit var userId: String

  companion object {
    const val commonLat = 46.5196535
    const val commonLng = 6.6322734
  }

  private lateinit var viewModel: MapViewModel
  private lateinit var locationRepository: LocationRepository
  private lateinit var eventRepository: EventRepository
  private lateinit var userRepository: UserRepository

  @get:Rule val mainCoroutineRule = MainCoroutineRule()

  // NOTE: Assuming your Location model has a toGeoPoint() extension.
  // If strictly needed for mocking, ensure Location objects are created compatible with that
  // extension.
  private val fakeEvents =
      listOf(
          Event(
              id = "event-001",
              title = "Morning Run at the Lake",
              description = "Join us for a casual 5km run around the lake followed by coffee.",
              date = LocalDateTime.of(2025, 10, 15, 7, 30),
              tags = setOf(Tag.JAZZ, Tag.COUNTRY),
              creator = UserTestData.Alice.uid,
              location = Location(latitude = 46.5196535, longitude = 6.6322734)),
          Event(
              id = "event-002",
              title = "Tech Hackathon 2025",
              date = LocalDateTime.of(2025, 11, 3, 9, 0),
              tags = setOf(Tag.PROGRAMMING, Tag.AI, Tag.KARATE),
              creator = UserTestData.Alice.uid,
              location = Location(latitude = 37.423021, longitude = -122.086808)),
          Event(
              id = "event-003",
              title = "Art & Wine Evening",
              description = "Relaxed evening mixing painting, wine, and music.",
              date = LocalDateTime.of(2025, 10, 22, 19, 0),
              tags = setOf(Tag.SCULPTURE, Tag.MUSIC),
              creator = UserTestData.Alice.uid,
              location = Location(latitude = 47.3769, longitude = 8.5417)))

  @Before
  fun setup() {
    userId = "new_id"
    locationRepository = mockk(relaxed = true)
    eventRepository = mockk(relaxed = true)
    userRepository = mockk(relaxed = true)
    mockkObject(DefaultDP)
    every { DefaultDP.io } returns UnconfinedTestDispatcher()
    every { DefaultDP.main } returns mainCoroutineRule.dispatcher

    // Mock the provider property accessed during initialization
    every { locationRepository.getLocationProvider() } returns mockk(relaxed = true)

    viewModel =
        MapViewModel(
            prefs = mockk(relaxed = true),
            currentUserId = userId,
            locationRepository = locationRepository,
            eventRepository = eventRepository,
            userRepository = userRepository,
        )
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  private fun mockTag(cat: Tag.Category): Tag {
    val t = mockk<Tag>()
    every { t.category } returns cat
    return t
  }

  @Test
  fun `loadLastKnownLocation emits Success on success`() = runTest {
    val fakeLocation = Location(latitude = 46.5196, longitude = 6.5685)

    // Capture the callback
    every { locationRepository.getLastKnownLocation(any(), any()) } answers
        {
          firstArg<(Location) -> Unit>().invoke(fakeLocation)
        }

    viewModel.loadLastKnownLocation()
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertNotNull(state.userLocation)
    assertEquals(fakeLocation.latitude, state.userLocation!!.latitude, 0.0001)
    assertEquals(fakeLocation.longitude, state.userLocation!!.longitude, 0.0001)
    assertNull(state.error)
    assertFalse(state.isLoading)
  }

  @Test
  fun `loadLastKnownLocation emits Error on failure`() = runTest {
    every { locationRepository.getLastKnownLocation(any(), any()) } answers
        {
          secondArg<() -> Unit>().invoke()
        }

    viewModel.loadLastKnownLocation()
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals("No last known location available", state.error)
    assertFalse(state.isLoading)
  }

  @Test
  fun `startLocationTracking updates userLocation`() = runTest {
    val fakeFlow = flowOf(Location(46.5, 6.5), Location(46.6, 6.6))
    every { locationRepository.startLocationTracking() } returns fakeFlow

    viewModel.startLocationTracking()
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals(46.6, state.userLocation?.latitude)
    assertEquals(6.6, state.userLocation?.longitude)
    assertNull(state.error)
  }

  @Test
  fun `loadAllEvents maps all categories to correct icons`() = runTest {
    val categoryEvents = EventTestData.categoryEvents

    val testData =
        categoryEvents.mapIndexed { index, (category, expectedIcon) ->
          val event = EventTestData.dummyEvent3.copy(id = "$index", tags = setOf(mockTag(category)))
          event to expectedIcon
        }

    // Extract just the events for the repository
    val eventsList = testData.map { it.first }

    coEvery { eventRepository.getAllEvents() } returns eventsList

    viewModel.loadAllEvents()
    advanceUntilIdle()

    val state = viewModel.uiState.value

    assertEquals("Should have one marker per event", eventsList.size, state.markers.size)

    // Verify each event got the correct icon
    testData.forEachIndexed { _, (event, expectedIcon) ->
      val marker = state.markers.find { it.event.id == event.id }
      assertNotNull("Marker for ${event.title} should exist", marker)
      assertEquals(
          "Incorrect icon for category ${event.tags.first().category}",
          expectedIcon,
          marker!!.iconResId)
    }
  }

  @Test
  fun `loadAllEvents determines icon based on dominant tag category`() = runTest {
    // 1. Setup Tags
    val musicTag = mockTag(Tag.Category.MUSIC)
    val musicTagTwo = mockTag(Tag.Category.MUSIC)
    val foodTag = mockTag(Tag.Category.FOOD)
    val sportTag = mockTag(Tag.Category.SPORT)
    val sportTagTwo = mockTag(Tag.Category.SPORT)

    // 2. Create Events with complex tag situations
    val mixedEvent = EventTestData.dummyEvent1.copy(id = "1", tags = setOf(musicTag, foodTag))

    val emptyTagEvent = EventTestData.NoTagsEvent

    val concurrentEvent =
        EventTestData.dummyEvent1.copy(
            id = "2", tags = setOf(sportTag, musicTag, sportTagTwo, musicTagTwo, foodTag))
    coEvery { eventRepository.getAllEvents() } returns
        listOf(mixedEvent, emptyTagEvent, concurrentEvent)

    viewModel.loadAllEvents()
    advanceUntilIdle()

    val markers = viewModel.uiState.value.markers

    // Assert Mixed Event (Music Dominant)
    val mixedMarker = markers.find { it.event.id == mixedEvent.id }
    assertEquals(
        "Dominant category (Music) should determine icon",
        R.drawable.violet_pin,
        mixedMarker?.iconResId)

    // Assert Empty Tags (Fallback)
    val emptyMarker = markers.find { it.event.id == emptyTagEvent.id }
    assertEquals(
        "Events with no tags should use the base pin", R.drawable.base_pin, emptyMarker?.iconResId)

    val concurrentMarker = markers.find { it.event.id == concurrentEvent.id }
    assertEquals(
        "Events with concurrent tags should use the first dominant category in enum order",
        R.drawable.sky_blue_pin,
        concurrentMarker?.iconResId)
  }

  @Test
  fun `startLocationTracking emits Error on exception`() = runTest {
    every { locationRepository.startLocationTracking() } returns
        flow { throw RuntimeException("Tracking failed") }

    viewModel.startLocationTracking()
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertTrue(state.error!!.contains("Tracking failed"))
  }

  @Test
  fun `onCameraMoveRequest sends MoveCamera action`() = runTest {
    viewModel.mapActions.test {
      val target = GeoPoint(46.5, 6.5)
      val zoom = 12.0
      viewModel.onCameraMoveRequest(target, zoom)

      val action = awaitItem()
      assertTrue(action is MapAction.MoveCamera)
      assertEquals(target, (action as MapAction.MoveCamera).target)
      assertEquals(zoom, (action as MapAction.MoveCamera).currentZoom, 0.0)
    }
  }

  @Test
  fun `onCameraStateChange updates uiState`() = runTest {
    val newPos = GeoPoint(48.8566, 2.3522)
    val newZoom = 15.5

    viewModel.onCameraStateChange(newPos, newZoom)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals(newPos, state.cameraPosition)
    assertEquals(newZoom, state.zoomLevel, 0.0001)
  }

  @Test
  fun `loadAllEvents updates uiState markers`() = runTest {
    coEvery { eventRepository.getAllEvents() } returns fakeEvents

    viewModel.loadAllEvents()
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals(fakeEvents.size, state.markers.size)

    // Verify mapping logic
    val firstMarker = state.markers[0]
    assertEquals(fakeEvents[0].title, firstMarker.event.title)
    // Assuming Location -> GeoPoint mapping is direct
    assertEquals(fakeEvents[0].location.latitude, firstMarker.position.latitude, 0.0001)
  }

  @Test
  fun `loadAllEvents sets error on failure`() = runTest {
    coEvery { eventRepository.getAllEvents() } throws RuntimeException("Failed to load")

    viewModel.loadAllEvents()
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals("Failed to load events: Failed to load", state.error)
  }

  @Test
  fun `loadSuggestedEventsForCurrentUser updates eventMarkers`() = runTest {
    val testUser = UserTestData.ManyTagsUser
    val suggestedEvents = listOf(EventTestData.dummyEvent1, EventTestData.dummyEvent2)

    coEvery { userRepository.getUser(userId) } returns testUser
    coEvery { eventRepository.getSuggestedEventsForUser(testUser) } returns suggestedEvents

    viewModel.loadSuggestedEventsForCurrentUser()
    advanceUntilIdle()

    val markers = viewModel.eventMarkers.value
    assertEquals(suggestedEvents.size, markers.size)
    assertEquals(suggestedEvents, markers)
  }

  @Test
  fun `onMapLongClick updates selectedLocation`() = runTest {
    viewModel.onMapLongClick(commonLat, commonLng)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertNotNull(state.selectedLocation)
    assertEquals(commonLat, state.selectedLocation!!.latitude, 0.0001)
    assertEquals(commonLng, state.selectedLocation!!.longitude, 0.0001)
  }

  @Test
  fun `onMapClick clears selectedLocation`() = runTest {
    // First set it
    viewModel.onMapLongClick(commonLat, commonLng)
    advanceUntilIdle()
    assertNotNull(viewModel.uiState.value.selectedLocation)

    // Then click map to clear
    viewModel.onMapClick()
    advanceUntilIdle()
    assertNull(viewModel.uiState.value.selectedLocation)
  }

  @Test
  fun `polling requests update events`() = runTest {
    // Because we injected testDispatcher into the ViewModel, delay() will obey advanceTimeBy
    val intervalMin = 1L
    val intervalMillis = intervalMin * 60 * 1000

    // Controlled mutable list that mockk will read from
    val currentEvents = mutableListOf<Event>()
    coEvery { eventRepository.getAllEvents() } answers { currentEvents.toList() }

    // Initial state
    assertEquals(0, viewModel.eventMarkers.value.size)

    // Add an event to repo, but polling hasn't started
    currentEvents.add(fakeEvents.first())
    advanceTimeBy(intervalMillis)
    runCurrent()
    // Should still be 0 because initData/loadAllEvents wasn't explicitly called in this test flow
    // yet
    // (except via initData in real app, but here we control calls)

    // Start polling
    viewModel.startEventPolling(intervalMinutes = intervalMin, maxIterations = 3)
    runCurrent()

    assertEquals(1, viewModel.eventMarkers.value.size)

    // Add another event
    currentEvents.add(fakeEvents[1])

    // Advance time to trigger next poll
    advanceTimeBy(intervalMillis)
    runCurrent()

    assertEquals(2, viewModel.eventMarkers.value.size)

    viewModel.stopEventPolling()
  }

  @Test
  fun `selectEvent updates selectedEvent flow`() = runTest {
    val testEvent = EventTestData.dummyEvent1

    viewModel.selectEvent(testEvent)
    advanceUntilIdle()

    assertEquals(testEvent, viewModel.selectedEvent.value)
  }

  @Test
  fun `onMarkerClick selects the event`() = runTest {
    val testEvent = EventTestData.dummyEvent1
    viewModel.onMarkerClick(testEvent)
    advanceUntilIdle()

    assertEquals(testEvent, viewModel.selectedEvent.value)
  }

  @Test
  fun `toggleEventParticipation adds user when not a participant`() = runTest {
    val event = EventTestData.dummyEvent1
    val newParticipants = event.participants + userId
    val updatedEvent = event.copy(participants = newParticipants)

    coEvery { eventRepository.updateEvent(event.id, updatedEvent) } returns Unit
    coEvery { eventRepository.getAllEvents() } returns listOf(updatedEvent)

    viewModel.toggleEventParticipation(event)
    advanceUntilIdle()

    coVerify { eventRepository.updateEvent(event.id, updatedEvent) }
    val selectedEvent = viewModel.selectedEvent.value
    assertEquals(updatedEvent, selectedEvent)
    assertTrue(selectedEvent?.participants?.contains(userId) ?: false)
  }

  @Test
  fun `toggleEventParticipation sets error on failure`() = runTest {
    val event = EventTestData.NoParticipantEvent
    coEvery { eventRepository.updateEvent(any(), any()) } throws
        NoSuchElementException("Update failed")

    viewModel.toggleEventParticipation(event)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals("No event ${event.title} found", state.error)
  }

  @Test
  fun `isUserParticipant returns true when user is in participants list`() {
    val event =
        EventTestData.dummyEvent1.copy(participants = setOf("otherUser", userId, "anotherUser"))
    val result = viewModel.isUserParticipant(event)
    assertTrue(result)
  }

  @Test
  fun `nowInteractable sets isMapInteractive to true`() = runTest {
    assertFalse(viewModel.uiState.value.isMapInteractive)

    viewModel.nowInteractable()

    assertTrue(viewModel.uiState.value.isMapInteractive)
  }

  @Test
  fun `tryPassiveAIGeneration triggers maybeGenerate after debounce when AI is on`() = runTest {
    val orchestrator = mockk<AIEventGenOrchestrator>(relaxed = true)
    coEvery { orchestrator.maybeGenerate(any(), any(), any(), any()) } returns emptyList()

    val viewModel = MapViewModel(
      prefs = mockk(relaxed = true),
      currentUserId = userId,
      locationRepository = locationRepository,
      eventRepository = eventRepository,
      userRepository = userRepository,
      aiOrchestrator = orchestrator
    )

    val region = mockk<VisibleRegion>(relaxed = true)

    viewModel.setAiOn(true)

    viewModel.onViewportChanged(region)

    // Before 300ms → should not be called
    advanceTimeBy(299)
    coVerify(exactly = 0) { orchestrator.maybeGenerate(any(), any(), any(), any()) }

    // After debounce
    advanceTimeBy(1)
    advanceUntilIdle()

    coVerify(exactly = 1) { orchestrator.maybeGenerate(any(), any(), any(), any()) }
  }

  @Test
  fun `tryPassiveAIGeneration skips loadAllEvents when no events generated`() = runTest {
    val orchestrator = mockk<AIEventGenOrchestrator>(relaxed = true)
    coEvery { orchestrator.maybeGenerate(any(), any(), any(), any()) } returns emptyList()

    val eventRepo = mockk<EventRepository>(relaxed = true)

    val viewModel = MapViewModel(
      prefs = mockk(relaxed = true),
      currentUserId = userId,
      locationRepository = locationRepository,
      eventRepository = eventRepo,
      userRepository = userRepository,
      aiOrchestrator = orchestrator
    )

    viewModel.setAiOn(true)

    val region = mockk<VisibleRegion>(relaxed = true)

    viewModel.onViewportChanged(region)

    advanceTimeBy(300)
    advanceUntilIdle()

    coVerify(exactly = 1) { orchestrator.maybeGenerate(any(), any(), any(), any()) }
    coVerify(exactly = 0) { eventRepo.getAllEvents() }
  }

  @Test
  fun `tryPassiveAIGeneration loads events and updates lastGen when AI generates events`() = runTest {
    val newEvents = listOf(EventTestData.dummyEvent1)

    val orchestrator = mockk<AIEventGenOrchestrator>(relaxed = true)
    coEvery { orchestrator.maybeGenerate(any(), any(), any(), any()) } returns newEvents

    val eventRepo = mockk<EventRepository>(relaxed = true)
    coEvery { eventRepo.getAllEvents() } returns newEvents

    val viewModel = MapViewModel(
      prefs = mockk(relaxed = true),
      currentUserId = userId,
      locationRepository = locationRepository,
      eventRepository = eventRepo,
      userRepository = userRepository,
      aiOrchestrator = orchestrator
    )

    viewModel.setAiOn(true)

    val region = mockk<VisibleRegion>(relaxed = true)

    // Use fixed clock for predictable timestamps
    val before = System.currentTimeMillis()

    viewModel.onViewportChanged(region)

    advanceTimeBy(300)
    advanceUntilIdle()

    coVerify(exactly = 1) { orchestrator.maybeGenerate(any(), any(), any(), any()) }
    coVerify(exactly = 1) { eventRepo.getAllEvents() }

    // Verify "lastAIGeneration" was updated (use reflection to access private var)
    val field = MapViewModel::class.java.getDeclaredField("lastAIGeneration")
    field.isAccessible = true
    val lastGen = field.getLong(viewModel)

    assertTrue("lastAIGeneration should be updated", lastGen >= before)
  }

  @Test
  fun `tryPassiveAIGeneration cancels previous job when viewport changes rapidly`() = runTest {
    val orchestrator = mockk<AIEventGenOrchestrator>(relaxed = true)
    coEvery { orchestrator.maybeGenerate(any(), any(), any(), any()) } returns emptyList()

    val viewModel = MapViewModel(
      prefs = mockk(relaxed = true),
      currentUserId = userId,
      locationRepository = locationRepository,
      eventRepository = eventRepository,
      userRepository = userRepository,
      aiOrchestrator = orchestrator
    )

    viewModel.setAiOn(true)

    val region = mockk<VisibleRegion>(relaxed = true)

    viewModel.onViewportChanged(region)

    // Before 300 ms, trigger again
    advanceTimeBy(100)
    viewModel.onViewportChanged(region)

    // Now advance 300ms from last call
    advanceTimeBy(300)
    advanceUntilIdle()

    // Should only run ONCE because first job was cancelled
    coVerify(exactly = 1) { orchestrator.maybeGenerate(any(), any(), any(), any()) }
  }
}
