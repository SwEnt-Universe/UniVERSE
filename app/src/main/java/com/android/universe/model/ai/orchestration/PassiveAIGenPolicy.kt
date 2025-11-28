package com.android.universe.model.ai.orchestration

import com.android.universe.util.GeoUtils.estimateRadiusKm
import com.tomtom.sdk.map.display.map.VisibleRegion

private const val REQUEST_COOLDOWN_MS = 60_000L // 60 seconds

private const val MAX_VIEWPORT_RADIUS_KM = 3.0


/**
 * Policy that determines whether passive AI event generation should be triggered.
 *
 * Pure logic only — no Android/UI/network dependencies.
 * Ensures we generate events only when the user is:
 *  - Looking at a meaningful area (valid viewport)
 *  - Lacking sufficient events in that area
 *  - Not spamming the AI (cooldown)
 *  - Zoomed in close enough (viewport small enough)
 */
class PassiveAIGenPolicy(
  private val minEventThreshold: Int = 5,
) {

  /**
   * Returns true if AI event generation should be triggered.
   *
   * @param viewport The currently visible TomTom map region.
   * @param numEvents Number of events currently visible in the viewport.
   * @param lastGenTimestamp Timestamp of previous generation (ms).
   * @param now Current timestamp (ms).
   */
  fun shouldGenerate(
    viewport: VisibleRegion,
    numEvents: Int,
    lastGenTimestamp: Long,
    now: Long
  ): Boolean {

    // 1. Cooldown guard
    if (now - lastGenTimestamp < REQUEST_COOLDOWN_MS) {
      return false
    }

    // 2. If enough events already exist, do nothing
    if (numEvents >= minEventThreshold) {
      return false
    }

    // 3. Estimate viewport radius (diagonal / 2)
    val radiusKm = viewport.estimateRadiusKm()

    // If user is zoomed out extremely far: avoid generating
    if (radiusKm > MAX_VIEWPORT_RADIUS_KM) {
      return false
    }

    return true
  }
}
