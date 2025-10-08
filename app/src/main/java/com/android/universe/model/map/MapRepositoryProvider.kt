package com.android.universe.model.map

import com.android.universe.model.event.Event
import com.android.universe.model.location.Location
import kotlinx.coroutines.runBlocking

/**
 * Singleton provider for a [MapRepository] instance.
 *
 * Supplies a single repository instance for use throughout the application. In this implementation,
 * it provides a [FakeMapRepository] pre-populated with sample events for testing, UI previews, and
 * development purposes.
 */
object MapRepositoryProvider {

  /** Internal fake repository used by the provider. */
  private val _repository: FakeMapRepository = FakeMapRepository()

  init {
    val sampleEvents =
        listOf(
            Event(id = "event1", title = "Concert", location = Location(47.0, 8.0)),
            Event("event2", title = "Market", location = Location(47.1, 8.1)))
    runBlocking { _repository.createMarkers(sampleEvents) }
  }

  /** Public repository instance (read-only) */
  var repository: MapRepository = _repository
}
