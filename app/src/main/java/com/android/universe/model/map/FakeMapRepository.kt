package com.android.universe.model.map

import com.android.universe.model.event.Event
import com.android.universe.model.location.Location
import com.tomtom.sdk.map.display.marker.Marker

/**
 * Fake implementation of [MapRepository] for testing and UI development purposes.
 *
 * Stores markers in memory without interacting with a real [TomTomMap]. Allows screens and
 * ViewModels to work independently of actual map services. Useful for UI previews and tests.
 */
class FakeMapRepository : MapRepository {

  /** Internal list of markers stored in memory. */
  private val markersList = mutableListOf<FakeMarker>()

  /**
   * Represents a fake marker in memory.
   *
   * @property location The geographical location of the marker.
   * @property eventTitle Optional title associated with the marker.
   * @property eventId Unique identifier for the marker (derived from Event).
   */
  data class FakeMarker(val location: Location, val eventTitle: String?, val eventId: String)

  /**
   * Initializes the map.
   *
   * In this fake implementation, this method does nothing because no real map is used.
   *
   * @param tomTomMap The [TomTomMap] instance (ignored).
   */
  override fun initializeMap(tomTomMap: com.tomtom.sdk.map.display.TomTomMap) {}

  /**
   * Centers the map on a given location.
   *
   * In this fake implementation, this method does nothing.
   *
   * @param location The location to center on.
   * @param zoom The zoom level to apply (ignored).
   */
  override fun centerOnLocation(location: Location, zoom: Double) {}

  /**
   * Creates a single marker and stores it in memory.
   *
   * @param location The location of the marker.
   * @param eventTitle Optional title for the marker.
   * @param eventId Optional ID; if null, a unique tag is generated.
   * @param onClick Optional click callback (ignored in fake repository).
   * @return Always returns null because no real [Marker] is created.
   */
  override fun createMarker(
      location: Location,
      eventTitle: String?,
      eventId: String?,
      onClick: (() -> Unit)?
  ): Marker? {
    val tag = eventId ?: "marker_${System.currentTimeMillis()}"
    val fakeMarker = FakeMarker(location, eventTitle, tag)
    markersList.add(fakeMarker)
    return null
  }

  /**
   * Creates multiple markers from a list of [Event] objects.
   *
   * @param events List of events for which to create markers.
   */
  override fun createMarkers(events: List<Event>) {
    events.forEach { event -> createMarker(event.location, event.title, event.id, null) }
  }

  /**
   * Updates a marker by deleting the old one and creating a new one with updated data.
   *
   * @param eventId ID of the marker to update.
   * @param newLocation New location for the marker.
   * @param newTitle Optional new title for the marker.
   */
  override fun updateMarker(eventId: String, newLocation: Location, newTitle: String?) {
    deleteMarker(eventId)
    createMarker(newLocation, newTitle, eventId, null)
  }

  /**
   * Deletes a marker from memory by its ID.
   *
   * @param eventId ID of the marker to remove.
   */
  override fun deleteMarker(eventId: String) {
    markersList.removeAll { it.eventId == eventId }
  }

  /**
   * Returns a snapshot of all markers currently stored in memory.
   *
   * @return List of [FakeMarker] objects.
   */
  fun getMarkers(): List<FakeMarker> = markersList.toList()
}
