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
 * @property creator user profile of the event creator.
 * @property participants set of user profiles participating in the event.
 * @property location where the event will take place.
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
) {}
