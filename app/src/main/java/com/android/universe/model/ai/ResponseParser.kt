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

object ResponseParser {

  private const val CREATOR = "OpenAI"

  private val json = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
  }

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
      validate(dto)
      Event(
          id = dto.id,
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

  private fun cleanJson(raw: String): String =
      raw.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()

  private fun validate(dto: EventDTO) {
    require(dto.id.isNotBlank()) { "Event id missing or blank" }
    require(dto.title.isNotBlank()) { "Title cannot be empty" }
    require(dto.description.isNotBlank()) { "Description cannot be empty" }

    requireNotNull(dto.location) { "Location missing" }
    require(dto.location.latitude in -90.0..90.0) { "Invalid latitude" }
    require(dto.location.longitude in -180.0..180.0) { "Invalid longitude" }

    // Optional: validate date
    runCatching { LocalDateTime.parse(dto.date) }
      .onFailure { throw IllegalArgumentException("Invalid date format: ${dto.date}") }
  }
}
