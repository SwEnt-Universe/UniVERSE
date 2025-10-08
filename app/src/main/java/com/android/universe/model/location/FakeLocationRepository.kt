package com.android.universe.model.location

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Fake implementation of [LocationRepository] for testing and UI development purposes.
 *
 * Provides simulated location data for testing and UI previews. Emits a predefined set of locations
 * without accessing device hardware, allowing screens and ViewModels to function independently of
 * real location services.
 */
class FakeLocationRepository : LocationRepository {

  private val fakeLocations =
      listOf(Location(47.3769, 8.5417), Location(46.9481, 7.4474), Location(46.2044, 6.1432))

  /** Always returns true for permission in the fake repository. */
  override fun hasLocationPermission(): Boolean = true

  /**
   * Returns the first fake location via callbacks.
   *
   * @param onSuccess called with a fake location.
   * @param onFailure called if no location is available (never called here).
   */
  override fun getLastKnownLocation(onSuccess: (Location) -> Unit, onFailure: () -> Unit) {
    onSuccess(fakeLocations.first())
  }

  /**
   * Starts emitting fake locations every second.
   *
   * @return a [Flow] of [Location] objects.
   */
  override fun startLocationTracking(): Flow<Location> = flow {
    var index = 0
    while (true) {
      emit(fakeLocations[index % fakeLocations.size])
      index++
      delay(1000)
    }
  }
}
