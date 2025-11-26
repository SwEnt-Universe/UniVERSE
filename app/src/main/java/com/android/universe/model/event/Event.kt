package com.android.universe.model.event

import com.android.universe.model.location.Location
import com.android.universe.model.tag.Tag
import java.time.LocalDateTime

/**
 * Represents an event in the app.
 *
 * @property id unique identifier for the event.
 * @property title name of the event.
 * @property description optional detailed information about the event.
 * @property date date and time when the event is scheduled to occur.
 * @property tags set of tags associated with the event for categorization.
 * @property creator unique identifier (UID) of the user who created the event.
 * @property participants set of unique identifiers (UIDs) of the users participating in the event.
 * @property location where the event will take place.
 * @property eventPicture optional byte array representing the event's picture.
 */
data class Event(
    val id: String,
    val title: String,
    val description: String? = null,
    val date: LocalDateTime,
    val tags: Set<Tag>,
    val creator: String,
    val participants: Set<String> = emptySet(),
    val location: Location,
    val eventPicture: ByteArray? = null
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as Event

    if (id != other.id) return false
    if (title != other.title) return false
    if (description != other.description) return false
    if (date != other.date) return false
    if (tags != other.tags) return false
    if (creator != other.creator) return false
    if (participants != other.participants) return false
    if (location != other.location) return false
    if ((eventPicture != null || other.eventPicture != null) &&
        (!eventPicture.contentEquals(other.eventPicture)))
        return false
    return true
  }

  override fun hashCode(): Int {
    var result = id.hashCode()
    result = 31 * result + title.hashCode()
    result = 31 * result + (description?.hashCode() ?: 0)
    result = 31 * result + date.hashCode()
    result = 31 * result + tags.hashCode()
    result = 31 * result + creator.hashCode()
    result = 31 * result + participants.hashCode()
    result = 31 * result + location.hashCode()
    result = 31 * result + (eventPicture?.contentHashCode() ?: 0)
    return result
  }
}
