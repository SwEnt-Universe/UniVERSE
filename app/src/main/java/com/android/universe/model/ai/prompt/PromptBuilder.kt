package com.android.universe.model.ai.prompt

import EventSchema.json
import com.android.universe.model.user.UserProfile
import java.time.LocalDate
import java.time.Period
import kotlinx.serialization.json.*

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

        // ======================================================
        // OUTPUT DISCIPLINE
        // ======================================================
        add(
            "Always output a JSON object with a top-level \"events\" array that matches EXACTLY the provided JSON schema.")
        add("No markdown, no commentary, no prose—ONLY the JSON object.")

        // ======================================================
        // REALISM RULES
        // ======================================================
        add(
            "All events must be public, open, casual, and drop-in friendly. They must NOT require an organizer, reservations, instructors, or paid facilities.")
        add(
            "Do NOT generate classes, workshops, lessons, tours, coached activities, or anything requiring staff, equipment rental, or venue booking.")

        add(
            "Events must represent spontaneous, community-friendly, self-organizable activities people can simply show up to, such as outdoor gatherings, walks, picnics, casual sports, local meetups, or open public-space activities.")

        add(
            "If the user’s interest normally requires a facility (e.g. indoor cycling, fencing, pottery), convert it into a realistic public variant appropriate for the environment (e.g., outdoor fitness meetup, running group, sketching meetup, photography walk).")

        add(
            "Realism takes priority over user interests. If an interest is not feasible in the location, reinterpret it into a related, physically plausible public activity.")

        // ======================================================
        // ENVIRONMENT & LOCATION MATCHING
        // ======================================================
        add(
            "Events must be consistent with the environment implied by the coordinates and radiusKm.")
        add(
            "Do NOT invent non-existent infrastructure (e.g., indoor gyms, beaches, ski slopes, concert halls).")
        add(
            "Use ONLY plausible public spaces: parks, lakesides, plazas, streets, promenades, seafronts (if applicable), small squares, viewpoints, playgrounds, trails, or known city areas typical for the region.")

        add(
            "Event titles and descriptions must match the environment (urban → food/culture/meetups, lakeside → relaxing/outdoors, park → fitness/picnics, etc.).")

        // ======================================================
        // GEOGRAPHIC CONSTRAINTS
        // ======================================================
        add(
            "Event coordinates must lie within radiusKm, but should not be identical to user location unless no other plausible point exists.")

        // ======================================================
        // TIME RULES
        // ======================================================
        add("All event dates must be strictly in the future relative to currentDate.")
        add("Dates must fall between 1 hour from now and 60 days in the future.")

        // ======================================================
        // TAG RELEVANCE (SOFT) — FIX FOR PASSIVE MODE
        // ======================================================
        add(
            "When requireRelevantTags = true, integrate user interests ONLY when they can be expressed as public, open, organizer-free activities.")
        add("Interests should inspire the theme, mood, or activity style—not the venue type.")
        add("If the interest is too restrictive, reinterpret it creatively but realistically.")

        // ======================================================
        // SCHEMA ENFORCEMENT
        // ======================================================
        add(
            "Do not add or omit fields. Do not include nulls unless the schema explicitly allows them.")
      }
    }

    return json.encodeToString(JsonObject.serializer(), systemObj)
  }

  // ----------------------------------------------------------
  // USER MESSAGE JSON
  // ----------------------------------------------------------
  fun buildUserMessage(profile: UserProfile, task: TaskConfig, context: ContextConfig): String {

    val obj = buildJsonObject {

      // TASK
      putJsonObject("task") {
        put(
            "goal",
            "generate public, drop-in, realistic events that match the environment and the user's interests when feasible.")
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

        putJsonArray("interests") { profile.tags.forEach { tag -> add(tag.displayName) } }
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

        put("currentDate", context.currentDate.toString())
      }
    }

    return json.encodeToString(JsonObject.serializer(), obj)
  }

  private fun calculateAge(dob: LocalDate): Int {
    return Period.between(dob, LocalDate.now()).years
  }
}
