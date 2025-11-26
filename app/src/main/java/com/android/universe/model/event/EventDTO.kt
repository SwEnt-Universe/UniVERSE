package com.android.universe.model.event

import kotlinx.serialization.Serializable

/**
 * Notice that the event DTO is **not** identical to the Event object. It does not define:
 * - creator
 * - participants
 * - eventPicture
 */
@Serializable
data class EventDTO(
    val id: String,
    val title: String,
    val description: String,
    val date: String, // "2025-04-12T20:00"
    val tags: List<String>,
    val location: LocationDTO,
)

@Serializable data class LocationDTO(val latitude: Double, val longitude: Double)
