package com.android.universe.model.event

import com.android.universe.model.user.UserProfile
import com.google.firebase.firestore.Source

/**
 * Repository interface for accessing and managing event.
 *
 * Provides basic CRUD operations as well as helper functions commonly used in the event flow.
 */
interface EventRepository {

  /**
   * Retrieves all events.
   *
   * @param source the [Source] from which to fetch the events (DEFAULT/CACHE/SERVER).
   * @return a list of all [Event] objects.
   */
  suspend fun getAllEvents(source: Source): List<Event>

  /**
   * Retrieves a single event by its ID.
   *
   * @param eventId the unique ID of the event.
   * @param source the [Source] from which to fetch the event (DEFAULT/CACHE/SERVER).
   * @return the [Event] associated with the given ID.
   */
  suspend fun getEvent(eventId: String, source: Source): Event

  /**
   * Retrieves suggested events for a given user based on their profile (tags).
   *
   * @param user the [UserProfile] for whom to suggest events.
   * @param source the [Source] from which to fetch the events (DEFAULT/CACHE/SERVER).
   * @return a list of suggested [Event] objects.
   */
  suspend fun getSuggestedEventsForUser(user: UserProfile, source: Source): List<Event>

  /**
   * Adds a new event to the repository.
   *
   * @param event the [Event] to add.
   */
  suspend fun addEvent(event: Event)

  /**
   * Updates an existing event.
   *
   * @param eventId the ID of the event to update.
   * @param newEvent the updated [Event].
   */
  suspend fun updateEvent(eventId: String, newEvent: Event)

  /**
   * Deletes an event from the repository.
   *
   * @param eventId the ID of the event to delete.
   */
  suspend fun deleteEvent(eventId: String)

  /**
   * Generates a new unique ID for an event.
   *
   * @return a new unique event ID as a [String].
   */
  suspend fun getNewID(): String
}
