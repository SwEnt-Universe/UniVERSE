package com.android.universe.model.location

import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.android.universe.di.DefaultDP
import com.tomtom.quantity.Distance
import com.tomtom.sdk.location.DefaultLocationProviderFactory
import com.tomtom.sdk.location.GeoLocation
import com.tomtom.sdk.location.LocationProvider
import com.tomtom.sdk.location.LocationProviderConfig
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Implementation of [LocationRepository] using TomTom SDK.
 *
 * Provides real-time location data using the TomTom SDK. Interfaces with the device's location
 * services to track user position.
 */
class TomTomLocationRepository(private val context: Context) : LocationRepository {

  private val provider: LocationProvider by lazy {
    val config =
        LocationProviderConfig(
            minTimeInterval = 250L.milliseconds, minDistance = Distance.meters(20.0))
    DefaultLocationProviderFactory.create(context, DefaultDP.default, config)
  }

  /**
   * Gets the underlying [LocationProvider] for direct map integration.
   *
   * @return the TomTom [LocationProvider].
   */
  override fun getLocationProvider(): LocationProvider = provider

  /**
   * Checks if location permission is granted by the user.
   *
   * @return true if [ACCESS_FINE_LOCATION] permission is granted.
   */
  override fun hasLocationPermission(): Boolean {
    return ContextCompat.checkSelfPermission(
        context, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED
  }

  /**
   * Gets the last known location using Android's LocationManager.
   *
   * @param onSuccess called with the last known location if available.
   * @param onFailure called if no location is available or permission is denied.
   */
  override fun getLastKnownLocation(onSuccess: (Location) -> Unit, onFailure: () -> Unit) {
    if (!hasLocationPermission()) {
      onFailure()
      return
    }

    try {
      val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
      val androidLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

      if (androidLocation != null) {
        onSuccess(Location(androidLocation.latitude, androidLocation.longitude))
      } else {
        onFailure()
      }
    } catch (_: SecurityException) {
      onFailure()
    }
  }

  /**
   * Starts location tracking and emits updates as a [Flow].
   *
   * @return a [Flow] of [Location] objects.
   */
  override fun startLocationTracking(): Flow<Location> = callbackFlow {
    if (!hasLocationPermission()) {
      close()
      return@callbackFlow
    }

    provider.addOnLocationUpdateListener { geoLocation: GeoLocation ->
      trySend(Location(geoLocation.position.latitude, geoLocation.position.longitude))
    }

    provider.enable()

    awaitClose { provider.disable() }
  }
}
