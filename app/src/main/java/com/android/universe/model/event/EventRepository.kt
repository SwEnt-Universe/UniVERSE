package com.android.universe.model.event

import com.android.universe.model.user.UserProfile

/**
 * Repository interface for accessing and managing event.
 *
 * Provides basic CRUD operations as well as helper functions commonly used in the event flow.
 */
interface EventRepository {

  /**
   * Retrieves all events.
   *
   * @return a list of all [Event] objects.
   */
  suspend fun getAllEvents(): List<Event>

  /**
   * Retrieves a single event by its ID.
   *
   * @param eventId the unique ID of the event.
   * @return the [Event] associated with the given ID.
   */
  suspend fun getEvent(eventId: String): Event

  /**
   * Retrieves suggested events for a given user based on their profile (tags).
   *
   * @param user the [UserProfile] for whom to suggest events.
   * @return a list of suggested [Event] objects.
   */
  suspend fun getSuggestedEventsForUser(user: UserProfile): List<Event>

  /**
   * Retrieves all events where the specific user is a participant.
   *
   * @param userId the unique ID of the user.
   * @return a list of [Event] objects the user has joined.
   */
  suspend fun getUserInvolvedEvents(userId: String): List<Event>

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
   * Persists a list of newly generated AI events and returns the stored results.
   *
   * The method performs the following steps:
   * 1. Assigns each event a new Firestore-generated ID.
   * 2. Saves the event into the `events` collection.
   * 3. Reconstructs and returns the fully persisted events, including their assigned IDs.
   *
   * @param events list of AI-generated events that have not yet been stored.
   * @return a new list of events identical to the input but with Firestore IDs assigned and
   *   guaranteed to be persisted in storage.
   */
  suspend fun persistAIEvents(events: List<Event>): List<Event>

  suspend fun countEventsInViewport(centerLat: Double, centerLon: Double, radiusKm: Double): Int

  /**
   * Generates a new unique ID for an event.
   *
   * @return a new unique event ID as a [String].
   */
  suspend fun getNewID(): String
}
