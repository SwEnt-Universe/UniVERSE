import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

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
                "id",
                "title",
                "description",
                "date",
                "tags",
                "location"
              ],
              "properties": {
                "id": { "type": "string" },
                "title": { "type": "string" },
                "description": { "type": ["string"] },
                "date": { "type": "string" },
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
