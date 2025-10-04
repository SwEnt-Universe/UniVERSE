package com.android.universe.model.map

/** Data class representing a location, e.g., latitude, longitude, optional name or description. */
data class Location(val latitude: Double = 0.0, val longitude: Double = 0.0, val name: String = "")
