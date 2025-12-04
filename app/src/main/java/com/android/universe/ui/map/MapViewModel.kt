package com.android.universe.ui.map

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
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
import com.tomtom.sdk.map.display.location.LocationMarkerOptions
import com.tomtom.sdk.map.display.map.OnlineCachePolicy
import com.tomtom.sdk.map.display.marker.Marker
import com.tomtom.sdk.map.display.marker.MarkerOptions
import com.tomtom.sdk.map.display.style.StyleDescriptor
import com.tomtom.sdk.map.display.ui.MapView
import com.tomtom.sdk.map.display.ui.currentlocation.CurrentLocationButton
import com.tomtom.sdk.map.display.ui.logo.LogoView
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
    private val userRepository: UserRepository,
    private val userReactiveRepository: UserReactiveRepository? =
        UserReactiveRepositoryProvider.repository,
    private val ai: AIEventGen = OpenAIProvider.eventGen,
) : ViewModel() {

  @SuppressLint("StaticFieldLeak") private var tomtomMapView: MapView? = null
  private val CACHE_SIZE = 50L * 1024 * 1024

  fun setMapMode(mapMode: MapMode) {
    _uiState.update { it.copy(mapMode = mapMode) }
  }

  fun getMapInstance(): MapView {
    if (tomtomMapView == null) {
      val mapOptions =
          MapOptions(
              mapKey = BuildConfig.TOMTOM_API_KEY,
              mapStyle =
                  StyleDescriptor(
                      "https://api.tomtom.com/style/2/custom/style/dG9tdG9tQEBAZUJrOHdFRXJIM0oySEUydTsd6ZOYVIJPYKLNwZiNGdLE/drafts/0.json?key=oICGv96tZpkxbJRieRSfAKcW8fmNuUWx"
                          .toUri()),
              onlineCachePolicy = OnlineCachePolicy.Custom(CACHE_SIZE),
              renderToTexture = true)
      tomtomMapView = MapView(applicationContext, mapOptions)

      tomtomMapView?.onCreate(null)
      tomtomMapView?.configureUiSettings()
      tomtomMapView?.getMapAsync { onMapReady(it) }
    }
    return tomtomMapView!!
  }

  fun decoupleFromParent() {
    tomtomMapView?.let { map ->
      val parent = map.parent
      if (parent != null && parent is ViewGroup) {
        parent.removeView(map)
      }
    }
  }

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
  private val locationProvider: LocationProvider? = locationRepository.getLocationProvider()

  // Jobs for tracking execution
  private var locationTrackingJob: Job? = null
  private var pollingJob: Job? = null

  private var tomTomMap: TomTomMap? = null
  private val markerToEvent = mutableMapOf<String, Event>()
  private lateinit var currentUserId: String

  /** Initializes data loading and starts event polling. */
  fun init(uid: String, locationSelectedCallback: (Double, Double) -> Unit) {
    // Temporary until the filtering of event works well.
    currentUserId = uid
    if (uiState.value.isMapInteractive)
        tomTomMap!!.setMapLongClickListener(
            mode = uiState.value.mapMode,
            onMapLongClick = { pos -> onMapLongClick(pos.latitude, pos.longitude) },
            onLocationSelected = locationSelectedCallback)
    loadAllEvents()
    startEventPolling()
  }

  /** Handles permission grant by loading location and starting tracking. */
  fun onPermissionGranted() {
    tomTomMap!!.initLocationProvider(locationProvider)
    loadLastKnownLocation()
    startLocationTracking()
  }

    fun getEventCreatorUsername(uid: String): String {
        var username = ""
        viewModelScope.launch {
            val user = userRepository.getUser(uid)
            username = user.username
            return@launch
        }
        return username
    }

  override fun onCleared() {
    super.onCleared()

    // The MapView get destroyed with the viewModel
    tomtomMapView?.onDestroy()
    tomtomMapView = null

    stopLocationTracking()
    stopEventPolling()
  }

  private fun MapView.configureUiSettings() {
    this.currentLocationButton.visibilityPolicy = CurrentLocationButton.VisibilityPolicy.Invisible
    this.logoView.visibilityPolicy = LogoView.VisibilityPolicy.Invisible
    this.scaleView.isVisible = false
  }

  @OptIn(ExperimentalMapSetAntialiasingMethodApi::class)
  fun onMapReady(map: TomTomMap) {
    tomTomMap = map
    map.apply {
      setInitialCamera(uiState.value.cameraPosition, uiState.value.zoomLevel)
      setAntialiasingMethod(AntialiasingMethod.FastApproximateAntialiasing)
      setUpMapListeners(
          mode = uiState.value.mapMode,
          onMapClick = { onMapClick() },
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

  private fun TomTomMap.initLocationProvider(provider: LocationProvider?) {
    provider?.let {
      this.setLocationProvider(it)
      val locationMarkerOptions = LocationMarkerOptions(type = LocationMarkerOptions.Type.Pointer)
      this.enableLocationMarker(locationMarkerOptions)
      it.enable()
    }
  }

  private fun TomTomMap.setUpMapListeners(
      mode: MapMode,
      onMapClick: () -> Unit,
      onMarkerClick: (Marker) -> Boolean,
      onCameraChange: (GeoPoint, Double) -> Unit
  ) {

    this.addMapClickListener {
      when (mode) {
        MapMode.NORMAL -> onMapClick()
        else -> {
          /* Nothing */
        }
      }
      true
    }

    this.addMarkerClickListener { clickedMarker -> onMarkerClick(clickedMarker) }

    this.addCameraSteadyListener {
      onCameraChange(this.cameraPosition.position, this.cameraPosition.zoom)
    }
  }

  private fun TomTomMap.setMapLongClickListener(
      mode: MapMode,
      onMapLongClick: (GeoPoint) -> Unit,
      onLocationSelected: (Double, Double) -> Unit
  ) {
    this.addMapLongClickListener { geoPoint ->
      when (mode) {
        MapMode.NORMAL -> onMapLongClick(geoPoint)
        MapMode.SELECT_LOCATION -> onLocationSelected(geoPoint.latitude, geoPoint.longitude)
      }
      true
    }
  }

  private fun TomTomMap.setInitialCamera(position: GeoPoint, zoom: Double) {
    this.moveCamera(CameraOptions(position = position, zoom = zoom))
  }

  private fun TomTomMap.executeMapAction(action: MapAction) {
    when (action) {
      is MapAction.MoveCamera -> {
        this.moveCamera(CameraOptions(position = action.target, zoom = this.cameraPosition.zoom))
      }
      is MapAction.ZoomIn -> {
        val newZoom = (this.cameraPosition.zoom + 1.0).coerceAtMost(22.0)
        this.animateCamera(CameraOptions(zoom = newZoom))
      }
    }
  }

  private fun TomTomMap.setMarkerSettings() {
    this.markersFadingRange = IntRange(300, 500)
  }

  fun syncEventMarkers(
      markers: List<MapMarkerUiModel>,
  ) {
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
  private fun stopLocationTracking() {
    locationTrackingJob?.cancel()
    locationTrackingJob = null
  }

  /**
   * Loads all events from the repository and converts them to map markers with appropriate icons.
   *
   * This function:
   * 1. Fetches all events from the repository
   * 2. Retrieves user profiles for each event creator (reactively if available)
   * 3. Determines the primary category for each event based on its tags
   * 4. Assigns a colored pin drawable based on the event's primary category
   * 5. Creates MapMarkerUiModel objects containing event, creator, location, and icon data
   *
   * Uses reactive Flow-based user fetching when userReactiveRepository is available, otherwise
   * falls back to synchronous repository calls.
   */
  fun loadAllEvents() {
    viewModelScope.launch {
      try {
        val events = eventRepository.getAllEvents()
        _eventMarkers.value = events

        if (userReactiveRepository != null) {
          val distinctCreators = events.map { it.creator }.distinct()

          if (distinctCreators.isEmpty()) {
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
          val markers =
              events.map { event ->
                mapEventToMarker(event)
              }
          _uiState.update { it.copy(markers = markers) }
        }
      } catch (e: Exception) {
        _uiState.update { it.copy(error = "Failed to load events: ${e.message}") }
      }
    }
  }

  /** Returns the drawable resource ID for a given event category. */
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

  /** Maps an Event and its creator UserProfile to a MapMarkerUiModel with appropriate icon. */
  private fun mapEventToMarker(event: Event): MapMarkerUiModel {
    val category = event.tags.groupingBy { it.category }.eachCount().maxByOrNull { it.value }?.key

    val drawable = getCategoryDrawable(category)

    return MapMarkerUiModel(
        event = event, position = event.location.toGeoPoint(), iconResId = drawable)
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
            withContext(DefaultDP.io) { delay(intervalMinutes * 60 * 1000) }
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

  fun generateAiEventAroundUser(radiusKm: Int = MAX_RADIUS_KM, timeFrame: String = "today") {
    val userLoc =
        uiState.value.userLocation
            ?: run {
              _uiState.update { it.copy(error = "User location unavailable") }
              return
            }

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

        eventRepository.persistAIEvents(events)
        loadAllEvents()
      } catch (e: Exception) {
        _uiState.update { it.copy(error = e.message ?: "AI generation failed") }
      }
    }
  }
}
