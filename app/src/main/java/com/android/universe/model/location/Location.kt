package com.android.universe.model.location

import com.tomtom.sdk.location.GeoPoint

data class Location(val latitude: Double, val longitude: Double) {
  fun toGeoPoint(): GeoPoint {
    return GeoPoint(latitude, longitude)
  }
}
