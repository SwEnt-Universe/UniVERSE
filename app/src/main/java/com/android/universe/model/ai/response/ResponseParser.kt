package com.android.universe.model.ai.response

import com.android.universe.model.event.Event
import com.android.universe.model.event.EventDTO
import com.android.universe.model.location.Location
import com.android.universe.model.tag.Tag
import java.time.LocalDateTime
import kotlin.String
import kotlin.collections.map
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
   * Converts raw OpenAI JSON into a list of validated domain-level [Event] objects.
   *
   * Workflow:
   * - Cleans the input by removing optional json fences.
   * - Parses the cleaned JSON into a root object.
   * - Extracts the `"events"` field, throwing an exception if missing.
   * - Deserializes the array into [EventDTO] instances.
   * - Validates each DTO via [EventValidator].
   * - Maps DTOs into fully initialized [Event] objects.
   *
   * Notes:
   * - Unknown fields are ignored.
   * - Values may be coerced when types differ slightly.
   * - Tag strings are converted to [Tag] objects; unknown tags are discarded.
   *
   * @param rawJson The raw JSON string from OpenAI (may include Markdown code fences).
   * @return A list of validated [Event] objects ready for UI rendering or storage.
   * @throws IllegalStateException if the `"events"` field is missing.
   * @throws IllegalArgumentException if any DTO fails validation.
   */
  fun parseEvents(rawJson: String): List<Event> {
    val cleaned = cleanJson(rawJson)

    // Parse root object
    val root = json.parseToJsonElement(cleaned).jsonObject

    // Extract events array
    val eventsJson =
        root["events"] ?: throw IllegalStateException("Missing 'events' field in OpenAI response")

    // Decode into DTOs
    val dtos: List<EventDTO> =
        json.decodeFromJsonElement(
            deserializer = ListSerializer(EventDTO.serializer()), element = eventsJson)

    // Convert to domain objects
    return dtos.map { dto ->
      EventValidator.validate(dto)

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
