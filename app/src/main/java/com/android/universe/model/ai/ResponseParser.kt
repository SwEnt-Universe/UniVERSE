package com.android.universe.model.ai

import com.android.universe.model.event.Event
import com.android.universe.model.event.EventDTO
import com.android.universe.model.location.Location
import com.android.universe.model.tag.Tag
import kotlinx.serialization.json.Json
import java.time.LocalDateTime
import kotlin.collections.map

/**
 * Converts raw JSON returned from OpenAI into strongly typed [Event] objects.
 *
 * Responsibilities:
 * - Parse JSON string into a list of DTOs
 * - Validate required fields and convert values (e.g. tags to [Tag], location, LocalDateTime)
 * - Transform DTOs to domain-level [Event] instances usable by application logic
 *
 * Isolated to maintain separation between AI data structures and real app models.
 */
object ResponseParser {

	private val json = Json {
		ignoreUnknownKeys = true        // tolerate missing & new fields
		coerceInputValues = true
	}

	fun parseEvents(rawJson: String): List<Event> {
		val dtos: List<EventDTO> = json.decodeFromString(rawJson)

		return dtos.map { dto ->
			Event(
				id = dto.id,
				title = dto.title,
				description = dto.description,
				date = LocalDateTime.parse(dto.date),
				tags = dto.tags.mapNotNull(Tag::fromDisplayName).toSet(),
				creator = dto.creator,
				participants = dto.participants.toSet(),
				location = Location(dto.location.latitude, dto.location.longitude),
				eventPicture = null
			)
		}
	}
}
