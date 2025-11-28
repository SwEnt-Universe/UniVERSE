package com.android.universe.model.ai.response

import com.android.universe.model.event.EventDTO
import java.time.LocalDateTime

/**
 * Performs sanity checks on an incoming [EventDTO] before it is converted into an internal `Event`
 * instance. Protects the app from malformed or incomplete data returned by the OpenAI API.
 *
 * Validation rules:
 * - `title` must not be blank.
 * - `description` must not be blank.
 * - `location` must be present.
 * - `latitude` must be within the valid range [-90, 90].
 * - `longitude` must be within the valid range [-180, 180].
 * - `date` must be a valid ISO-8601 datetime string (`LocalDateTime.parse`).
 *
 * Failures throw [IllegalArgumentException] with a descriptive message indicating which field was
 * invalid.
 *
 * This validator is intended to be used inside parsing or mapping layers before constructing the
 * final domain-level `Event`.
 *
 * @param dto The incoming event data from the AI response to validate.
 * @throws IllegalArgumentException if any required field is missing, blank, out of range, or
 *   formatted incorrectly.
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
    val dateTime =
        runCatching { LocalDateTime.parse(dto.date) }
            .getOrElse { throw IllegalArgumentException("Invalid date format: ${dto.date}") }

    // Ensure future event
    require(dateTime.isAfter(LocalDateTime.now())) {
      "Event date must be in the future: ${dto.date}"
    }
  }

  /**
   * Returns true if the event date/time is strictly in the future compared to now.
   *
   * This is used to reject AI-generated events that are already in the past, which can occur if the
   * model generates outdated timestamps.
   *
   * @param dto The incoming event data.
   * @return true if the event occurs after the current system time.
   * @throws IllegalArgumentException if the date field is not a valid ISO-8601 string.
   */
  fun isInFuture(dto: EventDTO): Boolean {
    val eventDateTime = LocalDateTime.parse(dto.date)
    return eventDateTime.isAfter(LocalDateTime.now())
  }
}
