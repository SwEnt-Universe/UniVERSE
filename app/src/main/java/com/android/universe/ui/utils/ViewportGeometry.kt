package com.android.universe.ui.utils

import com.android.universe.util.GeoUtils
import com.tomtom.sdk.map.display.map.VisibleRegion

/**
 * Clean domain representation of viewport geometry, derived from TomTom's VisibleRegion.
 *
 * @property centerLat Midpoint latitude of the visible region.
 * @property centerLon Midpoint longitude of the visible region.
 * @property radiusKm Half of the diagonal distance of the region, in kilometers.
 */
data class ViewportGeometry(val centerLat: Double, val centerLon: Double, val radiusKm: Double)

/**
 * Converts a TomTom [VisibleRegion] into a simpler viewport geometry object.
 * - The "center" is the midpoint between farLeft (top-left) and nearRight (bottom-right).
 * - The "radius" is half the diagonal length of the rectangle, converted to kilometers.
 */
fun VisibleRegion.toViewportGeometry(): ViewportGeometry {
  val fl = farLeft
  val nr = nearRight

  val centerLat = (fl.latitude + nr.latitude) / 2.0
  val centerLon = (fl.longitude + nr.longitude) / 2.0

  val diagonalMeters = GeoUtils.distanceMeters(fl.latitude, fl.longitude, nr.latitude, nr.longitude)

  val radiusKm = diagonalMeters / 2000.0

  return ViewportGeometry(centerLat = centerLat, centerLon = centerLon, radiusKm = radiusKm)
}
