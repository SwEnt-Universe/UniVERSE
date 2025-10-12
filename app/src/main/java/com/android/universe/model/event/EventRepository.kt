package com.android.universe.model.event

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
