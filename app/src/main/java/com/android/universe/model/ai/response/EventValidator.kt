package com.android.universe.model.ai.response

import com.android.universe.model.event.EventDTO
import java.time.LocalDateTime

object EventValidator {

  fun validate(dto: EventDTO) {
    require(dto.title.isNotBlank()) { "Event title cannot be empty." }

    require(dto.description.isNotBlank()) { "Event description cannot be empty." }

    requireNotNull(dto.location) { "Event location is missing." }

    require(dto.location.latitude in -90.0..90.0) { "Invalid latitude: ${dto.location.latitude}" }

    require(dto.location.longitude in -180.0..180.0) {
      "Invalid longitude: ${dto.location.longitude}"
    }

    // Validate date format
    runCatching { LocalDateTime.parse(dto.date) }
        .getOrElse { throw IllegalArgumentException("Invalid date format: ${dto.date}") }
  }
}
