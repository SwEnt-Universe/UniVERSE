package com.android.universe.ui.map

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.view.ViewGroup
import androidx.annotation.VisibleForTesting
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.universe.BuildConfig
import com.android.universe.R
import com.android.universe.di.DefaultDP
import com.android.universe.model.ai.AIConfig.MAX_RADIUS_KM
import com.android.universe.model.ai.AIEventGen
import com.android.universe.model.ai.openai.OpenAIProvider
import com.android.universe.model.ai.prompt.ContextConfig
import com.android.universe.model.ai.prompt.EventQuery
import com.android.universe.model.ai.prompt.TaskConfig
import com.android.universe.model.event.Event
import com.android.universe.model.event.EventRepository
import com.android.universe.model.event.EventTemporaryRepository
import com.android.universe.model.event.EventTemporaryRepositoryProvider
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
import com.android.universe.model.user.UserReactiveRepository
import com.android.universe.model.user.UserReactiveRepositoryProvider
import com.android.universe.model.user.UserRepository
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.location.LocationProvider
import com.tomtom.sdk.map.display.MapOptions
import com.tomtom.sdk.map.display.TomTomMap
import com.tomtom.sdk.map.display.annotation.ExperimentalMapSetAntialiasingMethodApi
import com.tomtom.sdk.map.display.camera.CameraOptions
import com.tomtom.sdk.map.display.common.screen.AntialiasingMethod
import com.tomtom.sdk.map.display.gesture.MapClickListener
import com.tomtom.sdk.map.display.gesture.MapLongClickListener
import com.tomtom.sdk.map.display.location.LocationMarkerOptions
import com.tomtom.sdk.map.display.map.OnlineCachePolicy
import com.tomtom.sdk.map.display.marker.Marker
import com.tomtom.sdk.map.display.marker.MarkerOptions
import com.tomtom.sdk.map.display.style.StyleDescriptor
import com.tomtom.sdk.map.display.style.StyleMode
import com.tomtom.sdk.map.display.ui.MapView
import com.tomtom.sdk.map.display.ui.currentlocation.CurrentLocationButton
import com.tomtom.sdk.map.display.ui.logo.LogoView
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Constants for SharedPreference keys
private const val KEY_CAMERA_LAT = "camera_latitude"
private const val KEY_CAMERA_LON = "camera_longitude"
private const val KEY_CAMERA_ZOOM = "camera_zoom"
private const val CACHE_SIZE = 50L * 1024 * 1024
private const val DEFAULT_ZOOM = 15.0
private const val DEFAULT_TILT = 45.0

private const val LIGHT_STYLE =
    "https://api.tomtom.com/style/2/custom/style/dG9tdG9tQEBAZUJrOHdFRXJIM0oySEUydTsd6ZOYVIJPYKLNwZiNGdLE/drafts/0.json?key=oICGv96tZpkxbJRieRSfAKcW8fmNuUWx"
private const val DARK_STYLE =
    "https://api.tomtom.com/style/2/custom/style/dG9tdG9tQEBAZUJrOHdFRXJIM0oySEUydTuOfCEvj1xLZYKOA_LMlky3/drafts/0.json?key=oICGv96tZpkxbJRieRSfAKcW8fmNuUWx"

/** UI state for the Map screen. */
data class MapUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val markers: List<MapMarkerUiModel> = emptyList(),
    val userLocation: GeoPoint? = null,
    val selectedLocation: GeoPoint? = null,
    val isLocationPermissionGranted: Boolean = false,
    val isMapInteractive: Boolean = false,
    val mapMode: MapMode = MapMode.NORMAL,
    val pendingCameraCenter: GeoPoint? = null,
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
 */
class MapViewModel(
    private val applicationContext: Context,
    private val prefs: SharedPreferences,
    private val locationRepository: LocationRepository,
    private val eventRepository: EventRepository,
    private val eventTemporaryRepository: EventTemporaryRepository =
        EventTemporaryRepositoryProvider.repository,
    private val userRepository: UserRepository,
    private val userReactiveRepository: UserReactiveRepository? =
        UserReactiveRepositoryProvider.repository,
    private val ai: AIEventGen = OpenAIProvider.eventGen,
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

  sealed interface EventSelectionState {
    data object None : EventSelectionState

    data class Selected(val event: Event, val creator: String) : EventSelectionState
  }

  private val _selectedEvent = MutableStateFlow<EventSelectionState>(EventSelectionState.None)
  /** The currently selected event, if any. */
  val selectedEvent: StateFlow<EventSelectionState> = _selectedEvent.asStateFlow()

  private val _previewEvent = MutableStateFlow<Event?>(null)
  val previewEvent: StateFlow<Event?> = _previewEvent.asStateFlow()

  // Map & Location Internal State
  @SuppressLint("StaticFieldLeak") private var tomtomMapView: MapView? = null
  private var tomTomMap: TomTomMap? = null
  private val locationProvider: LocationProvider? = locationRepository.getLocationProvider()
  private val markerToEvent = mutableMapOf<String, Event>()
  private lateinit var currentUserId: String
  private var clickListener: MapClickListener? = null
  private var longClickListener: MapLongClickListener? = null
  private var mapTheme: StyleMode =
      if (applicationContext.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
          Configuration.UI_MODE_NIGHT_YES)
          StyleMode.DARK
      else StyleMode.MAIN

  // Jobs
  private var locationTrackingJob: Job? = null
  private var pollingJob: Job? = null

  /**
   * Initializes data loading and starts event polling.
   *
   * @param uid The ID of the current user.
   */
  fun init(uid: String) {
    currentUserId = uid
    // Re-attach listener if map is already interactive (e.g., config change)
    updateClickListeners()
    loadAllEvents()
    startEventPolling()
  }

  override fun onCleared() {
    super.onCleared()
    tomtomMapView?.onDestroy()
    tomtomMapView = null
    stopLocationTracking()
    stopEventPolling()
  }

  fun setTheme(isDarkTheme: Boolean) {
    if (isDarkTheme) this.mapTheme = StyleMode.DARK else this.mapTheme = StyleMode.MAIN
    tomTomMap?.setStyleMode(this.mapTheme)
  }

  /**
   * Creates or returns the existing MapView instance. Use this to attach the map to the View
   * hierarchy.
   */
  fun getMapInstance(): MapView {
    if (tomtomMapView == null) {
      val mapOptions =
          MapOptions(
              mapKey = BuildConfig.TOMTOM_API_KEY,
              mapStyle = StyleDescriptor(uri = LIGHT_STYLE.toUri(), darkUri = DARK_STYLE.toUri()),
              onlineCachePolicy = OnlineCachePolicy.Custom(CACHE_SIZE),
              renderToTexture = true)
      tomtomMapView = MapView(applicationContext, mapOptions)
      tomtomMapView?.onCreate(null)
      tomtomMapView?.configureUiSettings()
      tomtomMapView?.getMapAsync { onMapReady(it) }
    }
    return tomtomMapView!!
  }

  /**
   * Detaches the MapView from its parent ViewGroup. Useful for preventing memory leaks or view
   * hierarchy errors when navigating away.
   */
  fun decoupleFromParent() {
    tomtomMapView?.let { map ->
      val parent = map.parent
      if (parent != null && parent is ViewGroup) {
        parent.removeView(map)
      }
    }
  }

  /**
   * Callback when the TomTom map is fully loaded. Sets up listeners, camera, and markers.
   *
   * @param map The loaded TomTomMap instance.
   */
  @OptIn(ExperimentalMapSetAntialiasingMethodApi::class)
  fun onMapReady(map: TomTomMap) {
    tomTomMap = map
    map.apply {
      setInitialCamera(uiState.value.cameraPosition, uiState.value.zoomLevel)
      setAntialiasingMethod(AntialiasingMethod.FastApproximateAntialiasing)
      setUpMapListeners(
          onMarkerClick = { marker ->
            markerToEvent[marker.tag]?.let { event: Event ->
              onMarkerClick(event)
              true
            } ?: false
          },
          onCameraChange = { pos, zoom -> onCameraStateChange(pos, zoom) })
      setMarkerSettings()
    }
    nowInteractable()
  }

  private fun MapView.configureUiSettings() {
    this.currentLocationButton.visibilityPolicy = CurrentLocationButton.VisibilityPolicy.Invisible
    this.logoView.visibilityPolicy = LogoView.VisibilityPolicy.Invisible
    this.scaleView.isVisible = false
  }

  private fun TomTomMap.setMarkerSettings() {
    this.markersFadingRange = IntRange(300, 500)
  }

  /**
   * Updates the current map interaction mode. Clears the selected location if switching to NORMAL
   * mode.
   *
   * @param mapMode The new mode (e.g., NORMAL, SELECT_LOCATION).
   */
  fun switchMapMode(mapMode: MapMode) {
    if (mapMode == MapMode.NORMAL) {
      _uiState.update { it.copy(selectedLocation = null) }
    }
    _uiState.update { it.copy(mapMode = mapMode) }
  }

  /** Marks the map as ready for user interaction. */
  fun nowInteractable() = _uiState.update { it.copy(isMapInteractive = true) }

  /**
   * Triggers a camera move action via a one-off event.
   *
   * @param target Destination coordinates.
   */
  fun onCameraMoveRequest(target: GeoPoint) {
    if (tomTomMap == null) return
    tomTomMap!!.moveCamera(CameraOptions(target, zoom = DEFAULT_ZOOM, tilt = DEFAULT_TILT))
  }

  /**
   * Updates the camera position and zoom level in state and persistence.
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
   * modify the location of the uiState when the user select it.
   *
   * @param location the location selected by the user
   */
  fun selectLocation(location: GeoPoint) {
    _uiState.update { it.copy(selectedLocation = location) }
  }

  private fun TomTomMap.setInitialCamera(position: GeoPoint, zoom: Double) {
    this.moveCamera(CameraOptions(position = position, zoom = zoom))
  }

  private fun TomTomMap.setUpMapListeners(
      onMarkerClick: (Marker) -> Boolean,
      onCameraChange: (GeoPoint, Double) -> Unit
  ) {
    this.addMarkerClickListener { clickedMarker -> onMarkerClick(clickedMarker) }
    this.addCameraSteadyListener {
      onCameraChange(this.cameraPosition.position, this.cameraPosition.zoom)
    }
  }

  /** Sets up map click and long-click listeners based on the current map mode. */
  private fun TomTomMap.setMapClickListeners(
      mode: MapMode,
  ) {
    clickListener =
        MapClickListener { geoPoint: GeoPoint ->
              if (mode == MapMode.SELECT_LOCATION) {
                selectLocation(geoPoint)
              }
              true
            }
            .let {
              this.addMapClickListener(it)
              it
            }
    longClickListener =
        MapLongClickListener { geoPoint: GeoPoint ->
              if (mode == MapMode.SELECT_LOCATION) {
                selectLocation(geoPoint)
              }
              true
            }
            .let {
              this.addMapLongClickListener(it)
              it
            }
  }

  /** Updates map click listeners when mode changes. */
  fun updateClickListeners() {
    if (uiState.value.isMapInteractive && tomTomMap != null) {
      clickListener?.let { tomTomMap?.removeMapClickListener(it) }
      longClickListener?.let { tomTomMap?.removeMapLongClickListener(it) }
      tomTomMap?.setMapClickListeners(mode = uiState.value.mapMode)
    }
  }

  /** Initializes the location provider and starts tracking after permission is granted. */
  fun onPermissionGranted() {
    tomTomMap!!.initLocationProvider(locationProvider)
    loadLastKnownLocation()
    startLocationTracking()
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

  /** Starts real-time location tracking flow. */
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

  private fun stopLocationTracking() {
    locationTrackingJob?.cancel()
    locationTrackingJob = null
  }

  private fun TomTomMap.initLocationProvider(provider: LocationProvider?) {
    provider?.let {
      this.setLocationProvider(it)
      val locationMarkerOptions = LocationMarkerOptions(type = LocationMarkerOptions.Type.Pointer)
      this.enableLocationMarker(locationMarkerOptions)
      it.enable()
    }
  }

  /**
   * Fetches all events, retrieves creator profiles, and updates the UI state. Uses a reactive flow
   * if [userReactiveRepository] is available.
   */
  fun loadAllEvents() {
    viewModelScope.launch {
      try {
        val following =
            try {
              userRepository.getUser(currentUserId).following.toSet()
            } catch (e: Exception) {
              if (e is CancellationException) throw e
              android.util.Log.e("MapViewModel", "Failed to fetch user following list", e)
              emptySet()
            }

        val events = eventRepository.getAllEvents(currentUserId, following)
        _eventMarkers.value = events

        if (userReactiveRepository != null) {
          val distinctCreators = events.map { it.creator }.distinct()
          if (distinctCreators.isEmpty() || events.isEmpty()) {
            _uiState.update { it.copy(markers = emptyList()) }
            return@launch
          }

          combine(
                  distinctCreators.map { uid ->
                    userReactiveRepository.getUserFlow(uid).map { user ->
                      uid to "${user?.firstName} ${user?.lastName}"
                    }
                  }) {
                    events.map { event -> mapEventToMarker(event) }
                  }
              .collect { markers -> _uiState.update { it.copy(markers = markers) } }
        } else {
          val markers = events.map { event -> mapEventToMarker(event) }
          _uiState.update { it.copy(markers = markers) }
        }
      } catch (e: Exception) {
        if (e is CancellationException) throw e
        _uiState.update { it.copy(error = "Failed to load events: ${e.message}") }
      }
    }
  }

  /**
   * Polls events periodically.
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
            withContext(DefaultDP.io) { delay(intervalMinutes * 60 * 1000) }
          }
        }
  }

  /** Stops the background event polling. */
  fun stopEventPolling() {
    pollingJob?.cancel()
    pollingJob = null
  }

  /**
   * Diffs and syncs the list of markers on the actual map instance. Adds new markers and removes
   * obsolete ones.
   *
   * @param markers The list of UI models to display.
   */
  fun syncEventMarkers(markers: List<MapMarkerUiModel>) {
    viewModelScope.launch {
      val map = tomTomMap ?: return@launch
      val (optionsToAdd, markersToRemove, eventForNewMarkers) =
          withContext(DefaultDP.io) { markerLogic(markerToEvent, markers) }

      if (markersToRemove.isNotEmpty()) {
        markersToRemove.forEach { markerToEvent.remove(it) }
      }
      if (optionsToAdd.isNotEmpty()) {
        val addedMarkers = map.addMarkers(optionsToAdd)
        addedMarkers.forEachIndexed { index, marker ->
          markerToEvent[marker.tag!!] = eventForNewMarkers[index]
        }
      }
    }
  }

  /**
   * Updates the custom marker for the user's selected location.
   *
   * @param location The GeoPoint to mark, or null to remove the marker.
   */
  fun syncSelectedLocationMarker(location: GeoPoint?) {
    viewModelScope.launch {
      val map = tomTomMap ?: return@launch
      map.removeMarkers("selected_location")
      location?.let { geoPoint ->
        val image = withContext(DefaultDP.default) { MarkerImageCache.get(R.drawable.base_pin) }
        map.addMarker(
            MarkerOptions(tag = "selected_location", coordinate = geoPoint, pinImage = image))
      }
    }
  }

  /** Loads events suggested specifically for the current user. */
  fun loadSuggestedEventsForCurrentUser() {
    viewModelScope.launch {
      try {
        val user = userRepository.getUser(currentUserId)
        val events = eventRepository.getSuggestedEventsForUser(user)
        _eventMarkers.value = events
      } catch (e: Exception) {
        if (e is CancellationException) throw e
        _uiState.update { it.copy(error = "Failed to load events: ${e.message}") }
      }
    }
  }

  /** Handles a click on an event marker. */
  fun onMarkerClick(event: Event) {
    selectEvent(event)
  }

  /** Sets the currently active event in the state. */
  fun selectEvent(event: Event?) {
    viewModelScope.launch {
      if (event == null) _selectedEvent.emit(EventSelectionState.None)
      else
          _selectedEvent.emit(
              EventSelectionState.Selected(event, userRepository.getUser(event.creator).username))
    }
  }

  /**
   * Toggles the current user's participation status for [event]. Updates remote repository and
   * local state.
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
        _selectedEvent.update {
          it as EventSelectionState.Selected
          it.copy(event = updatedEvent)
        }
      } catch (e: NoSuchElementException) {
        _uiState.update { it.copy(error = "No event ${event.title} found") }
      }
    }
  }

  /** Checks if the current user is a participant in the given event. */
  fun isUserParticipant(event: Event): Boolean {
    return event.participants.contains(currentUserId)
  }

  /**
   * Generates an AI event near the user's current location.
   *
   * @param radiusKm Max radius for generation.
   * @param timeFrame Time frame for context (e.g., "today").
   */
  fun generateAiEventAroundUser(
      radiusKm: Int = MAX_RADIUS_KM,
      timeFrame: String = "today",
  ) {
    val userLoc =
        uiState.value.userLocation
            ?: run {
              _uiState.update { it.copy(error = "User location unavailable") }
              return
            }

    // Trigger loading icon
    _uiState.update { it.copy(isLoading = true, error = null) }

    viewModelScope.launch {
      try {
        val profile = userRepository.getUser(currentUserId)
        val context =
            ContextConfig(
                location = null,
                locationCoordinates = Pair(userLoc.latitude, userLoc.longitude),
                radiusKm = radiusKm,
                timeFrame = timeFrame)
        val task = TaskConfig(eventCount = 1, requireRelevantTags = true)
        val query = EventQuery(user = profile, task = task, context = context)
        val events = ai.generateEvents(query)

        val event = events.firstOrNull() ?: throw IllegalStateException("AI returned no events")

        // Store temporarily
        eventTemporaryRepository.updateEventAsObject(event)

        // Update preview + selection state
        _previewEvent.value = event
        // creator username lookup
        val creatorName = userRepository.getUser(event.creator).username

        _selectedEvent.value = EventSelectionState.Selected(event = event, creator = creatorName)

        // Center camera on preview event
        requestCameraCenter(event.location.toGeoPoint())
      } catch (e: Exception) {
        _uiState.update { it.copy(error = e.message ?: "AI generation failed") }
      } finally {
        // Hide loading icon
        _uiState.update { it.copy(isLoading = false) }
      }
    }
  }

  fun acceptPreview() {
    val event = _previewEvent.value ?: return

    viewModelScope.launch {
      try {
        // 1. Save to real repository
        eventRepository.persistAIEvents(listOf(event))

        // 2. Clear preview
        eventTemporaryRepository.deleteEvent()
        _previewEvent.value = null
        _selectedEvent.value = EventSelectionState.None

        // 3. Refresh event list
        loadAllEvents()
      } catch (e: Exception) {
        _uiState.update { it.copy(error = e.message ?: "Failed to accept preview") }
      }
    }
  }

  fun rejectPreview() {
    viewModelScope.launch {
      eventTemporaryRepository.deleteEvent()

      _previewEvent.value = null
      _selectedEvent.value = EventSelectionState.None
    }
  }

  /**
   * Requests the map camera to center on the given geographic point.
   *
   * This does not move the camera directly. Instead, it updates the UI state with a
   * `pendingCameraCenter` value, which the UI layer (e.g., `MapScreen`) can observe and react to by
   * performing the actual camera movement on the map instance.
   *
   * @param point The target geographic coordinate to center the camera on.
   */
  fun requestCameraCenter(point: GeoPoint) {
    _uiState.update { it.copy(pendingCameraCenter = point) }
  }

  /**
   * Clears any previously requested camera-center action.
   *
   * Call after the UI has consumed and applied the pending camera movement. Prevents the same
   * camera command from being executed multiple times.
   */
  fun clearPendingCameraCenter() {
    _uiState.update { it.copy(pendingCameraCenter = null) }
  }

  private fun mapEventToMarker(event: Event): MapMarkerUiModel {
    val category = event.tags.groupingBy { it.category }.eachCount().maxByOrNull { it.value }?.key
    val drawable = getCategoryDrawable(category)
    return MapMarkerUiModel(
        event = event, position = event.location.toGeoPoint(), iconResId = drawable)
  }

  private fun getCategoryDrawable(category: Category?): Int {
    return when (category) {
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
  }
}

/**
 * Calculates marker diffs for efficient map updates.
 *
 * @param markerMap Current map of marker IDs to Events.
 * @param markers New list of UI models.
 * @return Triple containing options to add, IDs to remove, and events corresponding to additions.
 */
@VisibleForTesting
internal suspend fun markerLogic(
    markerMap: MutableMap<String, Event>,
    markers: List<MapMarkerUiModel>
): Triple<List<MarkerOptions>, Set<String>, List<Event>> {
  val previousEvents = markerMap.values.toSet()
  val currentEvents = markers.map { it.event }.toSet()
  val toAdd = markers.filter { it.event !in previousEvents }
  val toRemove = markerMap.filterValues { it !in currentEvents }.keys

  val optionsToAdd =
      toAdd.map {
        val pin = MarkerImageCache.get(it.iconResId)
        MarkerOptions(tag = it.event.id, coordinate = it.position, pinImage = pin)
      }
  return Triple(optionsToAdd, toRemove, toAdd.map { it.event })
}
