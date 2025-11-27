package com.android.universe.model.ai.response

import com.android.universe.model.event.EventDTO
import java.time.LocalDateTime

/**
 * Performs sanity checks on an incoming [EventDTO] before it is
 * converted into an internal `Event` instance. Protects the app from malformed
 * or incomplete data returned by the OpenAI API.
 *
 * Validation rules:
 * - `title` must not be blank.
 * - `description` must not be blank.
 * - `location` must be present.
 * - `latitude` must be within the valid range [-90, 90].
 * - `longitude` must be within the valid range [-180, 180].
 * - `date` must be a valid ISO-8601 datetime string (`LocalDateTime.parse`).
 *
 * Failures throw [IllegalArgumentException] with a descriptive message indicating
 * which field was invalid.
 *
 * This validator is intended to be used inside parsing or mapping layers before
 * constructing the final domain-level `Event`.
 *
 * @param dto The incoming event data from the AI response to validate.
 * @throws IllegalArgumentException if any required field is missing, blank,
 *         out of range, or formatted incorrectly.
 */
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
