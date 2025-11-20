package com.android.universe.model.location

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class FakeLocationRepositoryTest {

  private lateinit var repository: FakeLocationRepository

  @Before
  fun setup() {
    repository = FakeLocationRepository()
  }

  @Test
  fun hasLocationPermission_alwaysReturnsTrue() {
    assertTrue(repository.hasLocationPermission())
  }

  @Test
  fun getLocationProvider_returnsNull() {
    val provider = repository.getLocationProvider()
    assertNull(provider)
  }

  @Test
  fun getLastKnownLocation_returnsFirstFakeLocation() {
    var result: Location? = null
    repository.getLastKnownLocation(onSuccess = { result = it }, onFailure = { result = null })
    assertNotNull(result)
    assertEquals(47.3769, result!!.latitude, 0.0)
    assertEquals(8.5417, result!!.longitude, 0.0)
  }

  @Test
  fun startLocationTracking_emitsFakeLocationsInOrder() = runTest {
    val flow = repository.startLocationTracking()
    val emitted = flow.take(3).toList()
    assertEquals(3, emitted.size)

    assertEquals(47.3769, emitted[0].latitude, 0.0)
    assertEquals(8.5417, emitted[0].longitude, 0.0)

    assertEquals(46.9481, emitted[1].latitude, 0.0)
    assertEquals(7.4474, emitted[1].longitude, 0.0)

    assertEquals(46.2044, emitted[2].latitude, 0.0)
    assertEquals(6.1432, emitted[2].longitude, 0.0)
  }
}
