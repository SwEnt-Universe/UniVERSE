package com.android.universe.model.map

import com.android.universe.R
import com.android.universe.model.event.Event
import com.android.universe.model.location.Location
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.map.display.TomTomMap
import com.tomtom.sdk.map.display.camera.CameraOptions
import com.tomtom.sdk.map.display.image.ImageFactory
import com.tomtom.sdk.map.display.marker.Marker
import com.tomtom.sdk.map.display.marker.MarkerOptions

/**
 * Implementation of [MapRepository] using the TomTom SDK.
 *
 * Provides real map functionality such as centering the map, adding markers, updating and deleting
 * markers, and showing markers. This implementation allows user interactions with the map (markers
 * are currently not clickable).
 */
class TomTomMapRepository : MapRepository {

  /** Reference to the TomTomMap instance. */
  private var tomTomMap: TomTomMap? = null

  /** List of all markers currently added to the map. */
  private val markers = mutableListOf<Marker>()

  /**
   * Initializes the repository with a [TomTomMap] instance.
   *
   * @param tomTomMap the TomTomMap object to interact with.
   */
  override fun initializeMap(tomTomMap: TomTomMap) {
    this.tomTomMap = tomTomMap
  }

  /**
   * Centers the map camera on a given location with a specified zoom level.
   *
   * @param location the target location to center on.
   * @param zoom the zoom level to apply to the camera.
   */
  override fun centerOnLocation(location: Location, zoom: Double) {
    val map = tomTomMap ?: return
    val cameraOptions = CameraOptions(position = location.toGeoPoint(), zoom = zoom)
    map.moveCamera(cameraOptions)
  }

  /**
   * Creates a single marker on the map.
   *
   * @param location the location to place the marker.
   * @param eventTitle optional title displayed in the marker's balloon.
   * @param eventId optional unique identifier for the marker.
   * @param onClick optional click callback (currently not implemented).
   * @return the created [Marker] object, or null if the map is not initialized.
   */
  override fun createMarker(
      location: Location,
      eventTitle: String?,
      eventId: String?,
      onClick: (() -> Unit)?
  ): Marker? {
    val map = tomTomMap ?: return null

    val geoPoint = GeoPoint(location.latitude, location.longitude)

    val markerOptions =
        MarkerOptions(
            coordinate = geoPoint,
            pinImage = ImageFactory.fromResource(R.drawable.ic_marker_icon),
            balloonText = eventTitle ?: "Event",
            tag = eventId ?: "marker_${System.currentTimeMillis()}")

    val marker = map.addMarker(markerOptions)

    markers.add(marker)
    return marker
  }

  /**
   * Creates multiple markers on the map based on a list of [Event] objects.
   *
   * @param events the events for which to add markers.
   */
  override fun createMarkers(events: List<Event>) {
    events.forEach { event -> createMarker(event.location, event.title, event.id, null) }
  }

  /**
   * Updates an existing marker by deleting it and creating a new one with updated data.
   *
   * @param eventId the unique identifier of the marker to update.
   * @param newLocation the new location for the marker.
   * @param newTitle optional new title for the marker.
   */
  override fun updateMarker(eventId: String, newLocation: Location, newTitle: String?) {
    deleteMarker(eventId)
    createMarker(newLocation, newTitle, eventId, null)
  }

  /**
   * Deletes a marker from the map by its unique ID.
   *
   * Removes the marker from the TomTomMap instance and also from the internal [markers] list.
   *
   * @param eventId the identifier of the marker to remove.
   */
  override fun deleteMarker(eventId: String) {
    val map = tomTomMap ?: return

    // Remove marker from the map by tag
    map.removeMarkers(eventId)

    // Remove marker from the internal list
    val iterator = markers.iterator()
    while (iterator.hasNext()) {
      val marker = iterator.next()
      if (marker.tag == eventId) {
        iterator.remove()
      }
    }
  }

  /**
   * Converts a [Location] to a [GeoPoint] used by TomTomMap.
   *
   * @return a GeoPoint representing the same latitude and longitude as the Location.
   */
  private fun Location.toGeoPoint(): GeoPoint = GeoPoint(latitude, longitude)
}
