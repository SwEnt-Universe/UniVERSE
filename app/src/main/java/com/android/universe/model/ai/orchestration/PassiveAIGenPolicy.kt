package com.android.universe.model.ai.orchestration

import com.android.universe.model.ai.AIConfig.MAX_VIEWPORT_RADIUS_KM
import com.android.universe.model.ai.AIConfig.MIN_EVENT_SPACING_KM
import com.android.universe.model.ai.AIConfig.REQUEST_COOLDOWN
import com.android.universe.util.GeoUtils.estimateRadiusKm
import com.tomtom.sdk.map.display.map.VisibleRegion
import kotlin.Int
import kotlin.math.pow

sealed interface Decision {

  object Reject : Decision

  data class Accept(
      val eventsToGenerate: Int,
  ) : Decision
}

/**
 * Policy that determines whether passive AI event generation should be triggered.
 *
 * Pure logic only — no Android/UI/network dependencies. Ensures we generate events only when the
 * user is:
 * - Looking at a meaningful area (valid viewport)
 * - Lacking sufficient events in that area
 * - Not spamming the AI (cooldown)
 * - Zoomed in close enough (viewport small enough)
 */
class PassiveAIGenPolicy {

  /**
   * Returns true if AI event generation should be triggered.
   *
   * @param viewport The currently visible TomTom map region.
   * @param numEvents Number of events currently visible in the viewport.
   * @param lastGenTimestamp Timestamp of previous generation (ms).
   * @param now Current timestamp (ms).
   */
  fun evaluate(
      viewport: VisibleRegion,
      numEvents: Int,
      lastGenTimestamp: Long,
      now: Long
  ): Decision {

    // 1. Cooldown guard
    if (now - lastGenTimestamp < REQUEST_COOLDOWN) {
      return Decision.Reject
    }

    // 2. Estimate viewport radius (diagonal / 2)
    val radiusKm = viewport.estimateRadiusKm()

    // If user is zoomed out extremely far: avoid generating
    if (radiusKm > MAX_VIEWPORT_RADIUS_KM) {
      return Decision.Reject
    }

    /**
     * Computes the maximum number of events that can fit inside the current viewport without
     * causing visual clutter, based on a minimum spacing requirement.
     *
     * TUNED WITH [MIN_EVENT_SPACING_KM]
     *
     * Simple but effective density heuristic that:
     * - Scales naturally with zoom level
     * - Prevents generating events too close together
     * - Avoids clutter when zoomed in tightly
     * - Allows more events when zoomed out and the map covers a larger area
     *
     * Intuition:
     * - The viewport is approximated as a circle with radius `radiusKm`.
     * - Each event is assumed to occupy its own "exclusion circle" of radius
     *   `MIN_EVENT_SPACING_KM`, representing the minimum distance allowed between two event
     *   markers.
     *
     * The total area of the viewport grows with R^2, and the "area budget" per event also grows
     * with d^2. Their ratio gives the maximum number of evenly spaced events that can fit:
     * maxEvents ≈ (R / d)^2
     */
    val maxEventsAllowed = ((radiusKm / MIN_EVENT_SPACING_KM).pow(2)).toInt()

    // Prevent overflow if viewport is extremely tiny
    val threshold = maxEventsAllowed.coerceAtLeast(1)

    // If we already have enough events, skip generation
    if (numEvents >= threshold) return Decision.Reject

    return Decision.Accept(eventsToGenerate = threshold - numEvents)
  }
}
