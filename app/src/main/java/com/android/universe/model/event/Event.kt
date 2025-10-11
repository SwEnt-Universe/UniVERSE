package com.android.universe.model.event

import com.android.universe.model.Tag
import com.android.universe.model.user.UserProfile
import java.time.LocalDateTime

/**
 * Represents an event in the app.
 *
 * @property id unique identifier for the event.
 * @property title name of the event.
 * @property description optional detailed information about the event.
 * @property date date and time when the event is scheduled to occur.
 * @property tags set of tags associated with the event for categorization.
 * @property participants set of user profiles participating in the event.
 */
data class Event(
    val id: String,
    val title: String,
    val description: String? = null,
    val date: LocalDateTime,
    val tags: Set<Tag>,
    val participants: Set<UserProfile> = emptySet()
)
