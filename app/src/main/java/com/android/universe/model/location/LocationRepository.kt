package com.android.universe.model.location

import com.tomtom.sdk.location.LocationProvider
import kotlinx.coroutines.flow.Flow

interface LocationRepository {

  /** Returns true if location permission is granted */
  fun hasLocationPermission(): Boolean

  /**
   * Get the last known location (once) via callbacks
   *
   * @param onSuccess called with location if available
   * @param onFailure called if no location is available
   */
  fun getLastKnownLocation(onSuccess: (Location) -> Unit, onFailure: () -> Unit)

  /** Starts continuous location tracking and returns a Flow of Location objects */
  fun startLocationTracking(): Flow<Location>

  /**
   * Gets the underlying location provider for direct map integration. Returns null if not supported
   * by this implementation.
   */
  fun getLocationProvider(): LocationProvider?
}
