package com.android.universe.model.ai.orchestration

import com.tomtom.sdk.location.GeoPoint
import kotlin.math.abs

private const val REQUEST_COOLDOWN_MS = 60_000L // x_000L = x seconds

/**
 * Simple pure-policy class that decides whether passive AI event generation should be triggered.
 *
 * This class contains NO external dependencies and NO side effects.
 */
class PassiveAIGenPolicy(
    private val minEventThreshold: Int = 5,
    private val minZoomLevel: Double = 13.0,
) {

  fun shouldGenerate(
      userLocation: GeoPoint?,
      cameraCenter: GeoPoint,
      zoom: Double,
      numEvents: Int,
      lastGenTimestamp: Long,
      now: Long
  ): Boolean {

    // No user location → do nothing
    if (userLocation == null) return false

    // Require zoomed in enough (avoid generating across huge areas)
    if (zoom < minZoomLevel) return false

    // Avoid spamming the model
    if (now - lastGenTimestamp < REQUEST_COOLDOWN_MS) return false

    // Require low event density
    if (numEvents >= minEventThreshold) return false

    // If camera moved far away very quickly, ignore for now
    val distMoved =
        abs(cameraCenter.latitude - userLocation.latitude) +
            abs(cameraCenter.longitude - userLocation.longitude)

    if (distMoved > 0.5) return false // ~rough heuristic

    return true
  }
}
