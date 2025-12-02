package com.android.universe.model.event

import com.android.universe.model.location.Location
import java.time.LocalDateTime

interface EventTemporaryRepository {
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
  suspend fun updateEvent(
      id: String,
      title: String,
      description: String?,
      dateTime: LocalDateTime,
      creator: String,
      participants: Set<String>,
      location: Location,
      eventPicture: ByteArray?
  )

  /** Return the current stocked event. */
  suspend fun getEvent(): Event

  /** Clear the current stocked event */
  suspend fun deleteEvent()
}
