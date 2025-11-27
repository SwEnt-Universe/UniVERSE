import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

/**
 * Provides the JSON Schema definition used to enforce strict, structured output
 * from the OpenAI API.
 *
 * This schema is embedded inside the `response_format` field of
 * `ChatCompletionRequest`.
 *
 * The schema uses:
 * - `"strict": true` to prevent the model from introducing extra fields.
 * - `"additionalProperties": false` for strong structural guarantees.
 * - regex validation for timestamp formatting.
 */
object EventSchema {
  val json =
      """
      {
        "name": "EventList",
        "strict": true,
        "schema": {
          "type": "object",
          "additionalProperties": false,
          "required": ["events"],
          "properties": {
            "events": {
              "type": "array",
              "items": {
                "type": "object",
                "additionalProperties": false,
                "required": [
                  "title",
                  "description",
                  "date",
                  "tags",
                  "location"
                ],
                "properties": {
                  "title": { "type": "string" },
                  "description": { "type": ["string"] },
                  "date": {
                    "type": "string",
                    "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}${'$'}"
                  },
                  "tags": {
                    "type": "array",
                    "items": { "type": "string" }
                  },
                  "location": {
                    "type": "object",
                    "additionalProperties": false,
                    "required": ["latitude", "longitude"],
                    "properties": {
                      "latitude": { "type": "number" },
                      "longitude": { "type": "number" }
                    }
                  }
                }
              }
            }
          }
        }
      }
      """
          .trimIndent()

  val jsonObject = Json.parseToJsonElement(json).jsonObject
}
