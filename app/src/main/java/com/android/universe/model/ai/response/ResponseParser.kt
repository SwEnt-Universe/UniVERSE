package com.android.universe.model.ai.response

import com.android.universe.model.event.Event
import com.android.universe.model.event.EventDTO
import com.android.universe.model.location.Location
import com.android.universe.model.tag.Tag
import java.time.LocalDateTime
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

/**
 * Parses JSON returned by the OpenAI event-generation API into domain-level [Event] objects.
 *
 * Returns all valid events while collecting per-item validation failures.
 *
 * This parser is the bridge between raw AI output and the app's validated internal [Event] model.
 */
object ResponseParser {

  private const val CREATOR = "OpenAI"

  private val json = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
  }

  /** Represents the per-item result of attempting to parse and validate an [EventDTO]. */
  sealed class EventParseResult {
    data class Success(val event: Event) : EventParseResult()

    data class Failure(val dto: EventDTO, val error: IllegalArgumentException) : EventParseResult()
  }

  /**
   * Returned by parsing. Contains both the successfully parsed events and all validation failures
   * encountered.
   */
  data class ParseOutcome(val events: List<Event>, val failures: List<EventParseResult.Failure>)

  /**
   * Lenient parsing:
   * - Keeps all valid events
   * - Discards invalid DTOs
   * - Collects validation failures for logging/telemetry
   *
   * Never throws for individual event validation errors.
   *
   * @return [ParseOutcome] containing both valid events and detailed failures.
   * @throws IllegalStateException If the "events" field is missing.
   */
  fun parseEvents(rawJson: String): ParseOutcome {
    val cleaned = cleanJson(rawJson)

    // Root object
    val root = json.parseToJsonElement(cleaned).jsonObject

    // Extract array
    val eventsJson =
        root["events"] ?: throw IllegalStateException("Missing 'events' field in OpenAI response")

    // Decode into DTOs
    val dtos: List<EventDTO> =
        json.decodeFromJsonElement(
            deserializer = ListSerializer(EventDTO.serializer()), element = eventsJson)

    // Parse each DTO independently
    val results = dtos.map(::parseEvent)

    val successes = results.filterIsInstance<EventParseResult.Success>().map { it.event }
    val failures = results.filterIsInstance<EventParseResult.Failure>()

    return ParseOutcome(successes, failures)
  }

  // -------------------------------------------------------------------------
  // Internal helpers
  // -------------------------------------------------------------------------

  /**
   * Parses a single DTO in lenient fashion:
   * - Valid DTO -> Success(Event)
   * - Invalid DTO -> Failure(dto, error)
   */
  private fun parseEvent(dto: EventDTO): EventParseResult {
    return try {
      EventValidator.validate(dto)

      val event =
          Event(
              id = "",
              title = dto.title,
              description = dto.description,
              date = LocalDateTime.parse(dto.date),
              tags = dto.tags.mapNotNull(Tag::fromDisplayName).toSet(),
              creator = CREATOR,
              participants = emptySet(),
              location = Location(dto.location.latitude, dto.location.longitude),
          )

      EventParseResult.Success(event)
    } catch (e: IllegalArgumentException) {
      EventParseResult.Failure(dto, e)
    }
  }

  /**
   * Removes optional Markdown code fences from an OpenAI JSON string.
   *
   * @param raw The raw JSON string, possibly wrapped in Markdown fences.
   * @return The cleaned JSON string, safe for deserialization.
   */
  private fun cleanJson(raw: String): String =
      raw.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
}
