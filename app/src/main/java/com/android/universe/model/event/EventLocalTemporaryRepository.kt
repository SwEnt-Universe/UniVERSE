package com.android.universe.model.event

import com.android.universe.model.location.Location
import java.time.LocalDateTime

class EventLocalTemporaryRepository : EventTemporaryRepository {
  private var stockedEvent: Event? = null

  /**
   * Update the current stocked event.
   *
   * @param id The unique id of the event.
   * @param title The title of the event.
   * @param description The potential description of the event.
   * @param dateTime The temporarily of the event.
   * @param creator The creator of the event.
   * @param participants The participants of the event.
   * @param location The location of the event.
   * @param isPrivate Whether the event is private or not.
   * @param eventPicture The picture of the event.
   */
  override suspend fun updateEvent(
      id: String,
      title: String,
      description: String?,
      dateTime: LocalDateTime,
      creator: String,
      participants: Set<String>,
      location: Location,
      isPrivate: Boolean,
      eventPicture: ByteArray?
  ) {
    stockedEvent =
        Event(
            id = id,
            title = title,
            description = description,
            date = dateTime,
            tags = emptySet(),
            creator = creator,
            participants = participants,
            location = location,
            isPrivate = isPrivate,
            eventPicture = eventPicture)
  }

  /**
   * Update the current stocked event.
   *
   * @param event The event to update.
   */
  override suspend fun updateEventAsObject(event: Event) {
    updateEvent(
        id = event.id,
        title = event.title,
        description = event.description,
        dateTime = event.date,
        creator = event.creator,
        participants = event.participants,
        location = event.location,
        isPrivate = event.isPrivate,
        eventPicture = event.eventPicture)
  }

  /**
   * Update only the location of the stocked event.
   *
   * @param location The new location.
   */
  override suspend fun updateLocation(location: Location) {
    stockedEvent =
        Event(
            id = "",
            title = "",
            description = null,
            date = LocalDateTime.now(),
            tags = emptySet(),
            creator = "",
            participants = emptySet(),
            location = location,
            isPrivate = false,
            eventPicture = null)
  }

  /** Check if the location of the stocked event is null. */
  override suspend fun isLocationNull(): Boolean {
    return stockedEvent?.location == null
  }

  /** Return the current stocked event. */
  override suspend fun getEvent(): Event {
    if (stockedEvent == null) {
      throw IllegalStateException("No event stocked")
    } else {
      return stockedEvent!!
    }
  }

  /** Clear the current stocked event */
  override suspend fun deleteEvent() {
    stockedEvent = null
  }
}
