package com.android.universe.util

import com.tomtom.sdk.map.display.map.VisibleRegion

/**
 * Utility functions for geographic calculations.
 *
 * This object provides lightweight helpers for working with latitude/longitude coordinates without
 * depending on an external GIS library.
 */
object GeoUtils {

	/** Computes a radius based on the viewport, and gives it in kilometers */
	internal fun VisibleRegion.estimateRadiusKm(): Double {
		val diagonalMeters =
			distanceMeters(farLeft.latitude, farLeft.longitude, nearRight.latitude, nearRight.longitude)
		return (diagonalMeters / 2.0) / 1000.0
	}

	// Radius of the Earth in meters
	private const val EARTH_RADIUS_METERS = 6371000.0

	/**
	 * Computes the great-circle distance between two geographic coordinates.
	 *
	 * The calculation uses the Haversine formula, which returns the shortest distance over the
	 * Earth's surface between two points specified in latitude/longitude.
	 *
	 * @param lat1 Latitude of the first point in **degrees**.
	 * @param lon1 Longitude of the first point in **degrees**.
	 * @param lat2 Latitude of the second point in **degrees**.
	 * @param lon2 Longitude of the second point in **degrees**.
	 * @return The distance in **meters** as a double.
	 */
	fun distanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
		val dLat = Math.toRadians(lat2 - lat1)
		val dLon = Math.toRadians(lon2 - lon1)

		val a =
			Math.sin(dLat / 2).pow(2.0) +
					Math.cos(Math.toRadians(lat1)) *
					Math.cos(Math.toRadians(lat2)) *
					Math.sin(dLon / 2).pow(2.0)

		return 2 * EARTH_RADIUS_METERS * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
	}

	private fun Double.pow(exp: Double) = Math.pow(this, exp)
}
