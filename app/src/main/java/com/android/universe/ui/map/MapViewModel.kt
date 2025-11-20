package com.android.universe.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.universe.R
import com.android.universe.di.DefaultDP
import com.android.universe.model.event.Event
import com.android.universe.model.event.EventRepository
import com.android.universe.model.location.LocationRepository
import com.android.universe.model.user.UserRepository
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.location.LocationProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class MapUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val markers: List<MapMarkerUiModel> = emptyList(),
    val userLocation: GeoPoint? = null,
    val selectedLocation: GeoPoint? = null,
    val isLocationPermissionGranted: Boolean = false,
    val isMapInteractive: Boolean = false,

    // PERSISTENCE: Store the last known camera state here.
    // Defaults to Amsterdam (or your preferred default).
    val cameraPosition: GeoPoint = GeoPoint(46.5196535, 6.6322734),
    val zoomLevel: Double = 14.0
)

sealed interface MapAction {
  data class MoveCamera(val target: GeoPoint, val currentZoom: Double) : MapAction

  data object ZoomIn : MapAction
}

// Domain model for markers
data class MapMarkerUiModel(
    val event: Event,
    val position: GeoPoint, // TomTom uses GeoPoint(lat, lon)
    val iconResId: Int,
)

/**
 * ViewModel for managing the map screen state and location tracking.
 *
 * @property locationRepository Repository for accessing location data.
 * @property eventRepository Repository for accessing event data.
 */
class MapViewModel(
    private val currentUserId: String,
    private val locationRepository: LocationRepository,
    private val eventRepository: EventRepository,
    private val userRepository: UserRepository,
    private val ioDispatcher: CoroutineDispatcher = DefaultDP.io
) : ViewModel() {

  private val _uiState = MutableStateFlow(MapUiState())
  val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

  private val _mapActions = Channel<MapAction>(Channel.BUFFERED)
  val mapActions = _mapActions.receiveAsFlow()

  val locationProvider: LocationProvider? = locationRepository.getLocationProvider()

  private var locationTrackingJob: Job? = null

  private val _eventMarkers = MutableStateFlow<List<Event>>(emptyList())
  val eventMarkers: StateFlow<List<Event>> = _eventMarkers.asStateFlow()

  private val _selectedEvent = MutableStateFlow<Event?>(null)
  val selectedEvent: StateFlow<Event?> = _selectedEvent.asStateFlow()

  fun initData() {
    // Temporary until the filtering of event works well.
    loadAllEvents()
    startEventPolling()
  }

  fun onPermissionGranted() {
    loadLastKnownLocation()
    startLocationTracking()
  }

  private var pollingJob: Job? = null

  /**
   * Starts polling for events at regular intervals.
   *
   * @param intervalMinutes The interval in minutes between polling events.
   * @param maxIterations The maximum number of iterations before stopping polling. user only for
   *   tests
   */
  fun startEventPolling(intervalMinutes: Long = 5, maxIterations: Int? = null) {
    pollingJob?.cancel()
    pollingJob =
        viewModelScope.launch {
          var count = 0
          while (maxIterations == null || count < maxIterations) {
            try {
              loadAllEvents()
            } catch (e: Exception) {
              _uiState.update { it.copy(error = "Polling failed: ${e.message}") }
            }
            count++
            withContext(ioDispatcher) { delay(intervalMinutes * 60 * 1000) }
          }
        }
  }

  /** Stops polling for events. */
  fun stopEventPolling() {
    pollingJob?.cancel()
    pollingJob = null
  }

  fun onCameraStateChange(position: GeoPoint, zoomLevel: Double) {
    _uiState.update { it.copy(cameraPosition = position, zoomLevel = zoomLevel) }
  }

  fun onCameraMoveRequest(target: GeoPoint, currentZoom: Double) {
    viewModelScope.launch { _mapActions.send(MapAction.MoveCamera(target, currentZoom)) }
  }

  /**
   * Loads the last known location from the repository.
   *
   * Updates UI state to Loading, then either Success with location or Error if unavailable.
   */
  fun loadLastKnownLocation() {
    _uiState.update { it.copy(isLoading = true, error = null) }

    locationRepository.getLastKnownLocation(
        onSuccess = { location ->
          _uiState.update { it.copy(isLoading = false, userLocation = location.toGeoPoint()) }
        },
        onFailure = {
          _uiState.update { it.copy(isLoading = false, error = "No last known location available") }
        })
  }

  /**
   * Starts tracking the user's location in real-time.
   *
   * Collects location updates from the repository and updates the UI state accordingly.
   */
  fun startLocationTracking() {
    locationTrackingJob?.cancel()
    locationTrackingJob =
        viewModelScope.launch {
          locationRepository
              .startLocationTracking()
              .catch { exception ->
                _uiState.update {
                  it.copy(error = "Location tracking failed: ${exception.message}")
                }
              }
              .collect { location ->
                _uiState.update { it.copy(userLocation = location.toGeoPoint(), error = null) }
              }
        }
  }

  /** Stops tracking the user's location. */
  fun stopLocationTracking() {
    locationTrackingJob?.cancel()
    locationTrackingJob = null
  }

  override fun onCleared() {
    super.onCleared()
    stopLocationTracking()
  }

  /**
   * Loads all event markers from the event repository.
   *
   * Updates the state flow with the list of events or an error message if loading fails.
   */
  fun loadAllEvents() {
    viewModelScope.launch {
      try {
        val events = eventRepository.getAllEvents()
        _eventMarkers.value = events
        val markers =
            events.map { event ->
              MapMarkerUiModel(event, event.location.toGeoPoint(), R.drawable.ic_marker_icon)
            }
        _uiState.update { it.copy(markers = markers) }
      } catch (e: Exception) {
        _uiState.update { it.copy(error = "Failed to load events: ${e.message}") }
      }
    }
  }

  /**
   * Loads suggested event markers for the current user from the event repository.
   *
   * Updates the state flow with the list of suggested events or an error message if loading fails.
   */
  fun loadSuggestedEventsForCurrentUser() {
    viewModelScope.launch {
      try {
        val user = userRepository.getUser(currentUserId)
        val events = eventRepository.getSuggestedEventsForUser(user)
        _eventMarkers.value = events
      } catch (e: Exception) {
        _uiState.update { it.copy(error = "Failed to load events: ${e.message}") }
      }
    }
  }

  /**
   * Selects a location on the map.
   *
   * Updates the UI state with the selected latitude and longitude.
   */
  fun onMapLongClick(latitude: Double, longitude: Double) {
    _uiState.value = _uiState.value.copy(selectedLocation = GeoPoint(latitude, longitude))
  }

  fun onMarkerClick(event: Event) {
    selectEvent(event)
  }

  /** Updates the UI state to indicate that the map is now interactable. */
  fun nowInteractable() = _uiState.update { it.copy(isMapInteractive = true) }

  /**
   * Selects an event to be the currently selected event by the user.
   *
   * @param event The event to select, or null to clear the selection.
   */
  fun selectEvent(event: Event?) {
    viewModelScope.launch { _selectedEvent.emit(event) }
  }

  /**
   * Toggles the current user's participation in an event. If the user is already a participant,
   * they will be removed. If they are not a participant, they will be added.
   *
   * @param event The event to join or leave
   */
  fun toggleEventParticipation(event: Event) {
    viewModelScope.launch {
      try {
        val isParticipant = event.participants.contains(currentUserId)
        val updatedParticipants =
            if (isParticipant) {
              event.participants - currentUserId
            } else {
              event.participants + currentUserId
            }

        val updatedEvent = event.copy(participants = updatedParticipants)
        eventRepository.updateEvent(event.id, updatedEvent)

        // Update the selected event to reflect the change
        _selectedEvent.value = updatedEvent
      } catch (e: NoSuchElementException) {
        _uiState.update { it.copy(error = "No event ${event.title} found") }
      }
    }
  }

  /**
   * Checks if the current user is a participant in the given event.
   *
   * @param event The event to check
   * @return true if the user is a participant, false otherwise
   */
  fun isUserParticipant(event: Event): Boolean {
    return event.participants.contains(currentUserId)
  }
}
