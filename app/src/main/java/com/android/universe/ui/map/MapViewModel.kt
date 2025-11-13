package com.android.universe.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.universe.di.DefaultDP
import com.android.universe.model.event.Event
import com.android.universe.model.event.EventRepository
import com.android.universe.model.location.Location
import com.android.universe.model.location.LocationRepository
import com.android.universe.model.user.UserRepository
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.location.LocationProvider
import com.tomtom.sdk.map.display.camera.CameraOptions
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class MapUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isPermissionRequired: Boolean = false,
    val location: Location? = null,
    val selectedLat: Double? = null,
    val selectedLng: Double? = null,
    val eventCount: Int? = null,
    val isMapInteractive: Boolean = false,
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

  private val _cameraCommands = MutableSharedFlow<CameraOptions>(extraBufferCapacity = 1)
  val cameraCommands = _cameraCommands.asSharedFlow()

  val locationProvider: LocationProvider? = locationRepository.getLocationProvider()

  private var locationTrackingJob: Job? = null

  private val _eventMarkers = MutableStateFlow<List<Event>>(emptyList())
  val eventMarkers: StateFlow<List<Event>> = _eventMarkers.asStateFlow()

  private val _selectedEvent = MutableStateFlow<Event?>(null)
  val selectedEvent: StateFlow<Event?> = _selectedEvent.asStateFlow()

  init {
    loadSuggestedEventsForCurrentUser()
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

  /**
   * Loads the last known location from the repository.
   *
   * Updates UI state to Loading, then either Success with location or Error if unavailable.
   */
  fun loadLastKnownLocation() {
    _uiState.update { it.copy(isLoading = true, error = null) }

    locationRepository.getLastKnownLocation(
        onSuccess = { location ->
          _uiState.update { it.copy(isLoading = false, location = location) }
          centerOn(location.toGeoPoint(), zoom = 15.0)
        },
        onFailure = {
          _uiState.update { it.copy(isLoading = false, error = "No last known location available") }
          centerOnLausanne()
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
                _uiState.update { it.copy(location = location, error = null) }
                centerOn(location.toGeoPoint(), zoom = 15.0)
              }
        }
  }

  /** Stops tracking the user's location. */
  fun stopLocationTracking() {
    locationTrackingJob?.cancel()
    locationTrackingJob = null
  }

  /**
   * Centers the map camera on a specific location.
   *
   * @param position The geographic point to center on.
   * @param zoom The zoom level for the camera.
   */
  fun centerOn(position: GeoPoint, zoom: Double) {
    _cameraCommands.tryEmit(CameraOptions(position = position, zoom = zoom))
  }

  /** Centers the map on Lausanne as a fallback location. */
  fun centerOnLausanne() {
    val lausanne = GeoPoint(latitude = 46.5196535, longitude = 6.6322734)
    centerOn(lausanne, zoom = 14.0)
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
        // This is added so that the ui updates correctly when a new event is added
        _uiState.update { it.copy(eventCount = events.size) }
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
  fun selectLocation(latitude: Double?, longitude: Double?) {
    _uiState.value = _uiState.value.copy(selectedLat = latitude, selectedLng = longitude)
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
}
