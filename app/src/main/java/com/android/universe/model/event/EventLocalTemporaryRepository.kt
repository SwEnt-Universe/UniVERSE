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
            eventPicture = eventPicture)
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
