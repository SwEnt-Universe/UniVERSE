package com.android.universe.ui.map

import android.content.SharedPreferences
import android.graphics.Bitmap
import androidx.core.content.edit
import androidx.core.graphics.scale
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.universe.R
import com.android.universe.background.BackgroundSnapshotRepository
import com.android.universe.di.DefaultDP
import com.android.universe.model.ai.openai.OpenAIProvider
import com.android.universe.model.ai.orchestration.AIEventGenOrchestrator
import com.android.universe.model.ai.orchestration.PassiveAIGenPolicy
import com.android.universe.model.event.Event
import com.android.universe.model.event.EventRepository
import com.android.universe.model.location.LocationRepository
import com.android.universe.model.tag.Tag.Category
import com.android.universe.model.tag.Tag.Category.ART
import com.android.universe.model.tag.Tag.Category.FOOD
import com.android.universe.model.tag.Tag.Category.GAMES
import com.android.universe.model.tag.Tag.Category.MUSIC
import com.android.universe.model.tag.Tag.Category.SPORT
import com.android.universe.model.tag.Tag.Category.TECHNOLOGY
import com.android.universe.model.tag.Tag.Category.TOPIC
import com.android.universe.model.tag.Tag.Category.TRAVEL
import com.android.universe.model.user.UserRepository
import com.android.universe.ui.theme.Dimensions
import com.android.universe.ui.utils.LoggerAI
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.location.LocationProvider
import com.tomtom.sdk.map.display.map.VisibleRegion
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

// Constants for SharedPreference keys
private const val KEY_CAMERA_LAT = "camera_latitude"
private const val KEY_CAMERA_LON = "camera_longitude"
private const val KEY_CAMERA_ZOOM = "camera_zoom"

/** UI state for the Map screen. */
data class MapUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val markers: List<MapMarkerUiModel> = emptyList(),
    val userLocation: GeoPoint? = null,
    val selectedLocation: GeoPoint? = null,
    val isLocationPermissionGranted: Boolean = false,
    val isMapInteractive: Boolean = false,

    // Defaults to Lausanne
    val cameraPosition: GeoPoint = GeoPoint(46.5196535, 6.6322734),
    val zoomLevel: Double = 14.0
)

/** One-off actions for Map interactions. */
sealed interface MapAction {
  data class MoveCamera(val target: GeoPoint, val currentZoom: Double) : MapAction

  data object ZoomIn : MapAction
}

/** UI model representing a map marker. */
data class MapMarkerUiModel(
    val event: Event,
    val position: GeoPoint, // TomTom uses GeoPoint(lat, lon)
    val iconResId: Int,
)

/**
 * Manages map screen state, location tracking, and event data.
 *
 * @property currentUserId ID of the currently logged-in user.
 * @property locationRepository Repository for accessing location data.
 * @property eventRepository Repository for accessing event data.
 * @property userRepository Repository for user profile data.
 * @property ioDispatcher Dispatcher for background operations.
 */
class MapViewModel(
    private val prefs: SharedPreferences,
    private val currentUserId: String,
    private val locationRepository: LocationRepository,
    private val eventRepository: EventRepository,
    private val userRepository: UserRepository,
    private val ioDispatcher: CoroutineDispatcher = DefaultDP.io,
    private val aiOrchestrator: AIEventGenOrchestrator =
        AIEventGenOrchestrator(
            ai = OpenAIProvider.aiEventGen, // TODO Is this correct? Do I even need this.
            events = eventRepository,
            users = userRepository,
            policy = PassiveAIGenPolicy())
) : ViewModel() {

  private val _uiState =
      MutableStateFlow(
          MapUiState(
              cameraPosition =
                  GeoPoint(
                      prefs.getFloat(KEY_CAMERA_LAT, 46.519653f).toDouble(),
                      prefs.getFloat(KEY_CAMERA_LON, 6.632273f).toDouble()),
              zoomLevel = prefs.getFloat(KEY_CAMERA_ZOOM, 14f).toDouble()))
  /** Observable UI state. */
  val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

  private val _mapActions = Channel<MapAction>(Channel.BUFFERED)
  /** Stream of one-off map actions. */
  val mapActions = _mapActions.receiveAsFlow()

  private val _eventMarkers = MutableStateFlow<List<Event>>(emptyList())
  /** List of events to display as markers. */
  val eventMarkers: StateFlow<List<Event>> = _eventMarkers.asStateFlow()

  private val _selectedEvent = MutableStateFlow<Event?>(null)
  /** The currently selected event, if any. */
  val selectedEvent: StateFlow<Event?> = _selectedEvent.asStateFlow()

  /** Provider for location services. */
  val locationProvider: LocationProvider? = locationRepository.getLocationProvider()

  // Jobs for tracking execution
  private var locationTrackingJob: Job? = null
  private var pollingJob: Job? = null

  /** Initializes data loading and starts event polling. */
  fun initData() {
    // Temporary until the filtering of event works well.
    loadAllEvents()
    startEventPolling()
  }

  /** Handles permission grant by loading location and starting tracking. */
  fun onPermissionGranted() {
    loadLastKnownLocation()
    startLocationTracking()
  }

  override fun onCleared() {
    super.onCleared()
    stopLocationTracking()
    stopEventPolling()
  }

  /**
   * Updates the camera position and zoom level in state.
   *
   * @param position New camera center.
   * @param zoomLevel New zoom level.
   */
  fun onCameraStateChange(position: GeoPoint, zoomLevel: Double) {
    _uiState.update { it.copy(cameraPosition = position, zoomLevel = zoomLevel) }
    prefs.edit {
      putFloat(KEY_CAMERA_LAT, position.latitude.toFloat())
      putFloat(KEY_CAMERA_LON, position.longitude.toFloat())
      putFloat(KEY_CAMERA_ZOOM, zoomLevel.toFloat())
      apply()
    }
  }

  /**
   * Triggers a camera move action.
   *
   * @param target Destination coordinates.
   * @param currentZoom Current zoom level to maintain or adjust.
   */
  fun onCameraMoveRequest(target: GeoPoint, currentZoom: Double) {
    viewModelScope.launch { _mapActions.send(MapAction.MoveCamera(target, currentZoom)) }
  }

  /** Marks the map as interactive. */
  fun nowInteractable() = _uiState.update { it.copy(isMapInteractive = true) }

  /** Clears selected location on map click. */
  fun onMapClick() {
    _uiState.value = _uiState.value.copy(selectedLocation = null)
  }

  /**
   * Selects a specific map location.
   *
   * @param latitude Latitude of the selected point.
   * @param longitude Longitude of the selected point.
   */
  fun onMapLongClick(latitude: Double, longitude: Double) {
    _uiState.value = _uiState.value.copy(selectedLocation = GeoPoint(latitude, longitude))
  }

  /**
   * Manually selects a location (testing only).
   *
   * @param location The GeoPoint to select.
   */
  fun selectLocation(location: GeoPoint) {
    _uiState.value = _uiState.value.copy(selectedLocation = location)
  }

  /** Loads last known location, updating state with result or error. */
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

  /** Starts real-time location tracking. */
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

  /** Stops real-time location tracking. */
  fun stopLocationTracking() {
    locationTrackingJob?.cancel()
    locationTrackingJob = null
  }

  /** Loads all events from repository and updates markers. */
  fun loadAllEvents() {
    viewModelScope.launch {
      try {
        val events = eventRepository.getAllEvents()
        _eventMarkers.value = events
        val markers =
            events.map { event ->
              val category: Category? =
                  event.tags.groupingBy { it.category }.eachCount().maxByOrNull { it.value }?.key
              val drawableBasedOnCategory =
                  when (category) {
                    MUSIC -> R.drawable.violet_pin
                    SPORT -> R.drawable.sky_blue_pin
                    FOOD -> R.drawable.yellow_pin
                    ART -> R.drawable.red_pin
                    TRAVEL -> R.drawable.brown_pin
                    GAMES -> R.drawable.orange_pin
                    TECHNOLOGY -> R.drawable.grey_pin
                    TOPIC -> R.drawable.pink_pin
                    null -> R.drawable.base_pin
                  }
              MapMarkerUiModel(event, event.location.toGeoPoint(), drawableBasedOnCategory)
            }
        _uiState.update { it.copy(markers = markers) }
      } catch (e: Exception) {
        _uiState.update { it.copy(error = "Failed to load events: ${e.message}") }
      }
    }
  }

  /** Loads events suggested for the current user. */
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
   * Polls events every [intervalMinutes].
   *
   * @param intervalMinutes Minutes between poll attempts.
   * @param maxIterations Optional limit on poll count (for testing).
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

  /** Stops the event polling job. */
  fun stopEventPolling() {
    pollingJob?.cancel()
    pollingJob = null
  }

  /**
   * Selects the clicked event.
   *
   * @param event The event associated with the clicked marker.
   */
  fun onMarkerClick(event: Event) {
    selectEvent(event)
  }

  /**
   * Sets or clears the currently selected event.
   *
   * @param event The event to select, or null to clear selection.
   */
  fun selectEvent(event: Event?) {
    viewModelScope.launch { _selectedEvent.emit(event) }
  }

  /**
   * Toggles current user's participation in [event].
   *
   * @param event The event to join or leave.
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
   * Returns true if current user is participating in [event].
   *
   * @param event The event to check.
   * @return True if the user is a participant, false otherwise.
   */
  fun isUserParticipant(event: Event): Boolean {
    return event.participants.contains(currentUserId)
  }

  /**
   * Called when a snapshot is available.
   *
   * @param bmp The bitmap of the snapshot.
   */
  fun onSnapshotAvailable(bmp: Bitmap) {
    viewModelScope.launch {
      val scaled =
          bmp.scale(
              (bmp.width * Dimensions.ImageScale).toInt(),
              (bmp.height * Dimensions.ImageScale).toInt())
      BackgroundSnapshotRepository.updateSnapshot(scaled)
    }
  }

  // ----------------------------------------------------
  // AI related
  // ----------------------------------------------------
  private var lastAIGeneration: Long = 0L
  private var aiGenerationJob: Job? = null

  /**
   * Called whenever the visible map viewport changes.
   *
   * This method is invoked after TomTom recalculates the current [VisibleRegion], which happens
   * whenever the user pans, zooms, or the camera otherwise moves.
   *
   * The viewport represents the exact geographic rectangle the user is currently looking at,
   * defined by four corner coordinates:
   * - `farLeft` (top-left)
   * - `farRight` (top-right)
   * - `nearLeft` (bottom-left)
   * - `nearRight` (bottom-right)
   *
   * The updated region is **not stored** in ViewModel state. Instead, it is used immediately to
   * evaluate whether passive AI event generation should be triggered.
   *
   * @param region the newly computed visible map region
   */
  fun onViewportChanged(region: VisibleRegion) {
    tryPassiveAIGeneration(region)
  }

  private fun tryPassiveAIGeneration(region: VisibleRegion) {
    aiGenerationJob?.cancel()
    aiGenerationJob =
        viewModelScope.launch {
          delay(300L) // debounce camera movement

          val now = System.currentTimeMillis()

          LoggerAI.d("Viewport changed → evaluating AI generation...")
          LoggerAI.d(
            "Viewport corners:\n" +
                "farLeft=${region.farLeft}\n" +
                "farRight=${region.farRight}\n" +
                "nearLeft=${region.nearLeft}\n" +
                "nearRight=${region.nearRight}"
          )

          val newEvents =
              aiOrchestrator.maybeGenerate(
                  currentUserId = currentUserId,
                  viewport = region,
                  lastGen = lastAIGeneration,
                  now = now)

          if (newEvents.isNotEmpty()) {
            LoggerAI.i("Generated ${newEvents.size} new AI events")

            // Update last generation time
            lastAIGeneration = now

            LoggerAI.d("JUST BEFORE LOAD ALL EVENTS")
            // Refresh markers to include AI-generated events
            loadAllEvents()
            LoggerAI.d("JUST AFTER LOAD ALL EVENTS")
          } else {
            LoggerAI.d("AI generation skipped.")
          }
        }
  }
}
