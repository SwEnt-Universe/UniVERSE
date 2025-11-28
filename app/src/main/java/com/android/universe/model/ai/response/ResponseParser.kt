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
 * The parser handles:
 * - Stripping optional Markdown code fences
 * - Extracting the `"events"` array from the response
 * - Deserializing into [EventDTO] objects using Kotlinx Serialization
 * - Validating each DTO using [EventValidator]
 * - Converting DTOs into fully formed [Event] instances
 *
 * This object forms the bridge between raw AI output and the app's internal event model.
 */
object ResponseParser {

  private const val CREATOR = "OpenAI"

  private val json = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
  }

  /**
   * Represents the result of attempting to parse and validate a single [EventDTO].
   *
   * A DTO may either:
   * - Produce a fully constructed domain [Event] (`Success`), or
   * - Fail validation and record the associated exception (`Failure`).
   */
  sealed class EventParseResult {
    data class Success(val event: Event) : EventParseResult()

    data class Failure(val dto: EventDTO, val error: IllegalArgumentException) : EventParseResult()
  }

  /**
   * The aggregated result of a lenient parse operation.
   *
   * Contains:
   * - [events] — all successfully parsed, fully validated domain [Event] objects.
   * - [failures] — all individual DTOs that failed validation, each with its error.
   *
   * This allows the caller to accept partial success while still inspecting or logging failures.
   */
  data class ParseOutcome(val events: List<Event>, val failures: List<EventParseResult.Failure>)

  /**
   * Parses OpenAI JSON output in:
   * - Keeps all valid events.
   * - Discards invalid ones.
   * - Records per-item validation failures.
   *
   * Never throws for individual DTO validation errors.
   *
   * @param rawJson Raw JSON returned by the AI model (may include Markdown code fences).
   * @return A [ParseOutcome] containing both valid events and detailed failure information.
   * @throws IllegalStateException If the `"events"` field is missing entirely.
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
   * Parses and validates a single [EventDTO] in lenient fashion.
   * - If validation succeeds, returns [EventParseResult.Success].
   * - If validation fails, returns [EventParseResult.Failure] containing the DTO and error.
   *
   * No exceptions propagate out of this function.
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
