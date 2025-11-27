package com.android.universe.model.event

import kotlinx.serialization.Serializable

/**
 * Data Transfer Object (DTO) for events returned by the OpenAI API.
 *
 * This type represents only the raw AI-generated event data. It is intentionally smaller than the
 * app's internal `Event` model.
 *
 * The fields `id`, `creator`, `participants`, and `eventPicture` are not included here because they
 * are assigned later by the app during parsing and conversion.
 *
 * @property title name of the event.
 * @property description optional detailed information about the event.
 * @property date date and time when the event is scheduled to occur.
 * @property tags set of tags associated with the event for categorization.
 * @property location where the event will take place.
 */
@Serializable
data class EventDTO(
    val title: String,
    val description: String,
    val date: String, // "2025-04-12T20:00"
    val tags: List<String>,
    val location: LocationDTO,
)

@Serializable data class LocationDTO(val latitude: Double, val longitude: Double)
