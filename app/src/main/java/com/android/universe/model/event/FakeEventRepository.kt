package com.android.universe.model.event

import com.android.universe.model.user.UserProfile
import com.android.universe.util.GeoUtils
import java.util.UUID

/**
 * Fake implementation of [EventRepository] for testing and UI development purposes.
 *
 * Stores events in memory and does not persist data between app launches. This allows screen and
 * ViewModels to work independently of the real backend.
 */
class FakeEventRepository : EventRepository {

  /** Internal in-memory storage for events */
  private val events = mutableListOf<Event>()

  /**
   * Retrieves all events currently stored in the repository.
   *
   * @return a list of [Event] objects. Returns a copy to prevent external modification.
   */
  override suspend fun getAllEvents(): List<Event> {
    return events.toList()
  }

  /**
   * Retrieves an event by its ID.
   *
   * @param eventId the unique ID of the event.
   * @return the [Event] associated with the given ID.
   * @throws NoSuchElementException if no event with the given [eventId] exists.
   */
  override suspend fun getEvent(eventId: String): Event {
    return events.firstOrNull { it.id == eventId }
        ?: throw NoSuchElementException("No event found with id: $eventId")
  }

  /**
   * Retrieves suggested events for a given user based on their profile (tags).
   *
   * @param user the [UserProfile] for whom to suggest events.
   * @return a list of suggested [Event] objects.
   */
  override suspend fun getSuggestedEventsForUser(user: UserProfile): List<Event> {
    return events.filter { event -> event.tags.any { it in user.tags } }
  }

  /**
   * Retrieves all events where the specific user is either the creator or a participant.
   *
   * @param userId the unique ID of the user.
   * @return a list of [Event] objects associated with the user.
   */
  override suspend fun getUserInvolvedEvents(userId: String): List<Event> {
    return events.filter { event -> event.creator == userId || userId in event.participants }
  }

  /**
   * Adds a new event to the repository.
   *
   * @param event the [Event] to add.
   */
  override suspend fun addEvent(event: Event) {
    events.add(event)
  }

  /**
   * Updates an existing event identified by eventId.
   *
   * @param eventId the ID of the event to update.
   * @param newEvent the new [Event] to replace the old one.
   * @throws NoSuchElementException if no event with the given [eventId] exists.
   */
  override suspend fun updateEvent(eventId: String, newEvent: Event) {
    val index = events.indexOfFirst { it.id == eventId }
    if (index != -1) {
      events[index] = newEvent
    } else {
      throw NoSuchElementException("No event found with id: $eventId")
    }
  }

  /**
   * Deletes an event identified by eventId.
   *
   * @param eventId the ID of the event to delete.
   * @throws NoSuchElementException if no event with the given [eventId] exists.
   */
  override suspend fun deleteEvent(eventId: String) {
    val removed = events.removeIf { it.id == eventId }
    if (!removed) {
      throw NoSuchElementException("No event found with id: $eventId")
    }
  }

  /**
   * Persists a list of newly generated AI events by:
   * 1. Assigning each event a new Firestore ID
   * 2. Persisting it in the events collection
   * 3. Returning the fully stored events (with IDs assigned)
   */
  override suspend fun persistAIEvents(events: List<Event>): List<Event> {
    return events.map { e ->
      val id = getNewID()
      val saved = e.copy(id = id)
      this.events.add(saved)
      saved
    }
  }

  override suspend fun countEventsInViewport(
      centerLat: Double,
      centerLon: Double,
      radiusKm: Double
  ): Int {
    val events = getAllEvents()

    return events.count { event ->
      val d =
          GeoUtils.distanceMeters(
              centerLat, centerLon, event.location.latitude, event.location.longitude) / 1000.0

      d <= radiusKm
    }
  }

  /**
   * Generates a new unique ID for an event.
   *
   * @return a new unique event ID as a [String].
   */
  override suspend fun getNewID(): String {
    return UUID.randomUUID().toString()
  }
}
