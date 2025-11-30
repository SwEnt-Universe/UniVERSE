package com.android.universe.model.ai.prompt

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
  }
}
