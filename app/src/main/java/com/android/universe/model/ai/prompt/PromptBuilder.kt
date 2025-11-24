package com.android.universe.model.ai.prompt

import com.android.universe.model.user.UserProfile
import kotlinx.serialization.json.*
import java.time.LocalDate
import java.time.Period

/**
 * PromptBuilder (STRICT JSON MODE)
 *
 * Produces:
 * 1. SYSTEM message: strict JSON defining rules + schema.
 * 2. USER message: strict JSON with task, user, and context.
 *
 * This is OpenAI’s recommended modern prompt format.
 */
object PromptBuilder {

  private val json = Json { prettyPrint = false }

  // ----------------------------------------------------------
  // SYSTEM MESSAGE JSON
  // ----------------------------------------------------------
  fun buildSystemMessage(): String {
    val systemObj = buildJsonObject {
      put("role", "EventCuratorGPT")

      putJsonArray("rules") {
        add("Always output ONLY a JSON array of event objects")
        add("No markdown, no commentary, no prose")
        add("Output must strictly follow the provided schema")
      }

      putJsonObject("schema") {
        put("id", "String")
        put("title", "String")
        put("description", "String | null")
        put("date", "YYYY-MM-DD'T'HH:mm")
        put("tags", "[String]")
        put("creator", "String")
        put("participants", "[String]")
        putJsonObject("location") {
          put("latitude", "Double")
          put("longitude", "Double")
        }
        put("eventPicture", "null")
      }
    }

    return json.encodeToString(JsonObject.serializer(), systemObj)
  }

  // ----------------------------------------------------------
  // USER MESSAGE JSON
  // ----------------------------------------------------------
  fun buildUserMessage(
    profile: UserProfile,
    task: TaskConfig,
    context: ContextConfig
  ): String {

    val obj = buildJsonObject {

      // TASK
      putJsonObject("task") {
        put("goal", "generate realistic public events matching the user's interests")
        task.eventCount?.let { put("eventsToGenerate", it) }
        put("requireRelevantTags", task.requireRelevantTags)
      }

      // USER
      putJsonObject("user") {
        put("uid", profile.uid)
        put("name", "${profile.firstName} ${profile.lastName}")
        put("age", calculateAge(profile.dateOfBirth))
        put("country", profile.country)
        profile.description?.let { put("description", it) }

        putJsonArray("interests") {
          profile.tags.forEach { tag ->
            add(tag.displayName)
          }
        }
      }

      // CONTEXT
      putJsonObject("context") {
        context.location?.let { put("location", it) }
        context.locationCoordinates?.let { coords ->
          putJsonObject("coordinates") {
            put("lat", coords.first)
            put("lon", coords.second)
          }
        }
        context.radiusKm?.let { put("radiusKm", it) }
        context.timeFrame?.let { put("timeFrame", it) }
      }
    }

    return json.encodeToString(JsonObject.serializer(), obj)
  }

  private fun calculateAge(dob: LocalDate): Int {
    return Period.between(dob, LocalDate.now()).years
  }
}
