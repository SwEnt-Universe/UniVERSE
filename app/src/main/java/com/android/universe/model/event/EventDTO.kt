package com.android.universe.model.event

import kotlinx.serialization.Serializable

@Serializable
data class EventDTO(
    val id: String,
    val title: String,
    val description: String? = null,
    val date: String, // "2025-04-12T20:00"
    val tags: List<String>,
    val creator: String,
    val participants: List<String> = emptyList(),
    val location: LocationDTO,
    val eventPicture: String? = null // AI will not generate images anyway
)

@Serializable data class LocationDTO(val latitude: Double, val longitude: Double)
