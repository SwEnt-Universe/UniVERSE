package com.android.universe.model.location

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
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
  fun getLastKnownLocation_returnsFirstFakeLocation() {
    var result: Location? = null
    repository.getLastKnownLocation(onSuccess = { result = it }, onFailure = { result = null })
    assertNotNull(result)
    assertEquals(47.3769, result!!.latitude)
    assertEquals(8.5417, result!!.longitude)
  }

  @Test
  fun startLocationTracking_emitsFakeLocationsInOrder() = runTest {
    val flow = repository.startLocationTracking()
    val emitted = flow.take(3).toList()
    assertEquals(3, emitted.size)

    assertEquals(47.3769, emitted[0].latitude)
    assertEquals(8.5417, emitted[0].longitude)

    assertEquals(46.9481, emitted[1].latitude)
    assertEquals(7.4474, emitted[1].longitude)

    assertEquals(46.2044, emitted[2].latitude)
    assertEquals(6.1432, emitted[2].longitude)
  }
}
