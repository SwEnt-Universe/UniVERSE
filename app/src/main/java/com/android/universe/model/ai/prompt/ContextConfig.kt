package com.android.universe.model.ai.prompt

import com.android.universe.util.GeoUtils
import com.tomtom.sdk.map.display.map.VisibleRegion

/**
 * Defines the contextual parameters used when generating events.
 *
 * Determines **where**, **when**, and **within what radius** events should be generated for the
 * user. These values help the prompt builder precisely scope the request sent to the OpenAI event
 * generator.
 *
 * @property location Optional human-readable location name (e.g., "Lausanne").
 * @property locationCoordinates Optional geographic coordinates (latitude, longitude).
 * @property radiusKm Optional radius in kilometers within which events should be generated.
 * @property timeFrame Natural-language time window such as `"today"` or `"this week"`.
 */
data class ContextConfig(
    val location: String? = "Lausanne",
    val locationCoordinates: Pair<Double, Double>? = null,
    val radiusKm: Int? = null,
    val timeFrame: String? = "today"
) {

  companion object {
    val Default = ContextConfig()

    /**
     * Builds a [ContextConfig] using the currently visible map region.
     *
     * This reduces the viewport to:
     * - a center point (midpoint of diagonal)
     * - a radius (half of the diagonal distance)
     *
     * The radius ensures the AI generates events safely within or near the user-visible area,
     * without needing polygon definitions or TomTom-specific geometry.
     */
    fun fromVisibleRegion(region: VisibleRegion): ContextConfig {
      val farLeft = region.farLeft
      val nearRight = region.nearRight

      // --- 1. Compute center point (midpoint of diagonal)
      val centerLat = (farLeft.latitude + nearRight.latitude) / 2
      val centerLon = (farLeft.longitude + nearRight.longitude) / 2

      // --- 2. Compute radius from diagonal length (meters -> km)
      val diagonalMeters =
          GeoUtils.distanceMeters(
              farLeft.latitude, farLeft.longitude, nearRight.latitude, nearRight.longitude)

      val radiusKm = (diagonalMeters / 2.0) / 1000.0

      return ContextConfig(
          location = null,
          locationCoordinates = centerLat to centerLon,
          radiusKm = radiusKm.toInt().coerceAtLeast(1),
          timeFrame = "today")
    }
  }
}
