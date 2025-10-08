package com.android.universe.model.map

import com.android.universe.model.event.Event
import com.android.universe.model.location.Location
import com.tomtom.sdk.map.display.TomTomMap
import com.tomtom.sdk.map.display.marker.Marker

/**
 * Repository interface for managing map operations and markers.
 *
 * Provides functions for initializing a map, centering it on a location, and creating, updating, or
 * deleting markers. Designed to abstract the map implementation and allow different map providers
 * or fake implementations for testing and UI previews.
 */
interface MapRepository {

  /**
   * Initializes the map with the provided [TomTomMap] instance.
   *
   * @param tomTomMap the TomTomMap object to initialize and interact with.
   */
  fun initializeMap(tomTomMap: TomTomMap)

  /**
   * Centers the map on a given location with a specified zoom level.
   *
   * @param location the geographical location to center the map on.
   * @param zoom the zoom level to apply (default is 14.0).
   */
  fun centerOnLocation(location: Location, zoom: Double = 14.0)

  /**
   * Creates a single marker on the map.
   *
   * @param location the location where the marker should be placed.
   * @param eventTitle optional title to associate with the marker.
   * @param eventId optional unique identifier for the marker.
   * @param onClick optional callback to execute when the marker is clicked.
   * @return the created [Marker] object, or null if creation fails or is unsupported.
   */
  fun createMarker(
      location: Location,
      eventTitle: String? = null,
      eventId: String? = null,
      onClick: (() -> Unit)? = null
  ): Marker?

  /**
   * Creates multiple markers on the map based on a list of [Event] objects.
   *
   * @param events the list of events for which markers should be created.
   */
  fun createMarkers(events: List<Event>)

  /**
   * Updates an existing marker's location and/or title.
   *
   * @param eventId the unique identifier of the marker to update.
   * @param newLocation the new geographical location for the marker.
   * @param newTitle optional new title to set on the marker.
   */
  fun updateMarker(eventId: String, newLocation: Location, newTitle: String? = null)

  /**
   * Deletes a marker from the map by its unique ID.
   *
   * @param eventId the identifier of the marker to remove.
   */
  fun deleteMarker(eventId: String)
}
