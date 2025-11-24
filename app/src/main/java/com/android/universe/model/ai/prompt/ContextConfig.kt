package com.android.universe.model.ai.prompt
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
