package com.android.universe.model.event

import com.android.universe.model.location.Location

data class Event(
    val id: String,
    val title: String,
    val description: String? = null,
    val timestamp: Long? = null,
    val location: Location
)
