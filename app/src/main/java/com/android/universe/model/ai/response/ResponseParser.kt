package com.android.universe.model.ai.response

import com.android.universe.model.event.Event
import com.android.universe.model.event.EventDTO
import com.android.universe.model.location.Location
import com.android.universe.model.tag.Tag
import com.android.universe.ui.utils.LoggerAI
import java.time.LocalDateTime
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

object ResponseParser {

  private val json = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
  }

  // -------------------------------------------------------------------------
  // PUBLIC ENTRYPOINT (never throws)
  // -------------------------------------------------------------------------
  fun parseEvents(rawJson: String): List<Event> {
    LoggerAI.d("ResponseParser: Starting parse pipeline…")

    val cleaned = cleanJson(rawJson)
    LoggerAI.d("ResponseParser: Cleaned JSON (first 300 chars):\n${cleaned.take(300)}")

    val root = parseRootObject(cleaned) ?: return fail("Root JSON object malformed")
    val eventsElement = extractEventsArray(root) ?: return fail("Missing or invalid 'events' array")
    val dtoList = decodeEventDTOs(eventsElement) ?: return fail("Failed to decode EventDTO list")

    LoggerAI.d("ResponseParser: Decoded ${dtoList.size} DTOs. Converting…")

    val events =
      dtoList.mapNotNull { dto ->
        convertToEvent(dto)
      }

    LoggerAI.d("ResponseParser: Successfully parsed ${events.size} events.")

    return events
  }

  // Convenience: return emptyList while logging an error
  private fun fail(message: String): List<Event> {
    LoggerAI.e("ResponseParser FAILURE: $message")
    return emptyList()
  }

  // -------------------------------------------------------------------------
  // STAGE 1 — clean markdown and whitespace
  // -------------------------------------------------------------------------
  private fun cleanJson(raw: String): String =
    raw.trim()
      .removePrefix("```json")
      .removePrefix("```")
      .removeSuffix("```")
      .trim()

  // -------------------------------------------------------------------------
  // STAGE 2 — parse root object safely
  // -------------------------------------------------------------------------
  private fun parseRootObject(cleaned: String): JsonObject? {
    return try {
      json.parseToJsonElement(cleaned).jsonObject
    } catch (e: Exception) {
      LoggerAI.e(
        "ResponseParser: Failed at Stage 2 (parseRootObject). " +
            "Error: ${e.message}\n" +
            "Input snippet: ${cleaned.take(200)}"
      )
      null
    }
  }

  // -------------------------------------------------------------------------
  // STAGE 3 — extract "events" array safely
  // -------------------------------------------------------------------------
  private fun extractEventsArray(root: JsonObject): JsonElement? {
    return try {
      root["events"]
    } catch (e: Exception) {
      LoggerAI.e(
        "ResponseParser: Failed at Stage 3 (extractEventsArray). " +
            "Error: ${e.message}"
      )
      null
    }
  }

  // -------------------------------------------------------------------------
  // STAGE 4 — decode DTO list safely
  // -------------------------------------------------------------------------
  private fun decodeEventDTOs(eventsElement: JsonElement): List<EventDTO>? {
    return try {
      json.decodeFromJsonElement(
        deserializer = ListSerializer(EventDTO.serializer()),
        element = eventsElement
      )
    } catch (e: Exception) {
      LoggerAI.e(
        "ResponseParser: Failed at Stage 4 (decodeEventDTOs). " +
            "Error: ${e.message}\n" +
            "Events raw: ${eventsElement.toString().take(300)}"
      )
      null
    }
  }

  // -------------------------------------------------------------------------
  // STAGE 5 — convert a single DTO to domain Event safely
  // -------------------------------------------------------------------------
  private fun convertToEvent(dto: EventDTO): Event? {
    return try {
      Event(
        id = "",
        title = dto.title,
        description = dto.description,
        date = LocalDateTime.parse(dto.date),
        tags = dto.tags.mapNotNull(Tag::fromDisplayName).toSet(),
        creator = "OpenAI",
        participants = emptySet(),
        location = Location(dto.location.latitude, dto.location.longitude),
      )
    } catch (e: Exception) {
      LoggerAI.e(
        "ResponseParser: Failed at Stage 5 (convertToEvent).\n" +
            "DTO: $dto\n" +
            "Error: ${e.message}"
      )
      null
    }
  }
}
