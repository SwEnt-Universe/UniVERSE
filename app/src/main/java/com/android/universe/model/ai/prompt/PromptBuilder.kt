package com.android.universe.model.ai.prompt

import android.R.attr.radius
import com.android.universe.model.user.UserProfile
import java.time.LocalDate
import java.time.Period

/**
 * Lightweight prompt builder for generating event recommendations. This version minimizes token
 * usage by:
 * - Removing verbose prose
 * - Using compact JSON-like structures
 * - Removing full example JSON
 * - Compressing schema into a single definition line
 * - Shrinking context and user profile blocks
 */
object PromptBuilder {

  fun build(profile: UserProfile, task: TaskConfig, context: ContextConfig): String {
    return listOf(taskBlock(task), userBlock(profile), contextBlock(context), outputFormatBlock())
        .joinToString("\n\n")
  }

  // ----------------------------------------------------------------------
  // TASK — compressed into one line
  // ----------------------------------------------------------------------
  private fun taskBlock(task: TaskConfig): String {
    val fields =
        listOfNotNull(
                task.eventCount?.let { "\"eventsToGenerate\": $it" },
                "\"requireRelevantTags\": ${task.requireRelevantTags}")
            .joinToString(",\n  ")

    return """
        Task: {
          "goal": "generate realistic, feasible public events matching the user's interests",
          $fields
        }
    """
        .trimIndent()
  }

  // ----------------------------------------------------------------------
  // USER — compressed compact JSON-like object
  // ----------------------------------------------------------------------
  private fun userBlock(profile: UserProfile): String {
    val age = calculateAge(profile.dateOfBirth)
    val interests = profile.tags.joinToString(", ") { "\"${it.displayName}\"" }

    val uid = "\"uid\": \"${profile.uid}\""
    val name = "\"name\": \"${profile.firstName} ${profile.lastName}\""
    val ageField = "\"age\": $age"
    val country = "\"country\": \"${profile.country}\""
    val desc = profile.description?.let { "\"description\": \"$it\"" }
    val interestsField = "\"interests\": [$interests]"

    val fields =
        listOfNotNull(uid, name, ageField, country, desc, interestsField).joinToString(",\n  ")

    return """
        User: {
          $fields
        }
    """
        .trimIndent()
  }

  // ----------------------------------------------------------------------
  // CONTEXT — compact structure, minimal prose
  // ----------------------------------------------------------------------
  private fun contextBlock(context: ContextConfig): String {
    val location = context.location?.let { "\"location\": \"$it\"" }
    val radius = context.radiusKm?.let { "\"radiusKm\": $it" }
    val timeframe = context.timeFrame?.let { "\"timeFrame\": \"$it\"" }
    val locationCoordinates =
        context.locationCoordinates?.let {
          "\"coordinates\": { \"lat\": ${it.first}, \"lon\": ${it.second} }"
        }

    // Filter out null fields and join with commas
    val fields =
        listOfNotNull(location, locationCoordinates, radius, timeframe).joinToString(",\n  ")

    return """
        Context: {
          $fields
        }
    """
        .trimIndent()
  }

  // ----------------------------------------------------------------------
  // OUTPUT FORMAT — ultra-compact schema (no example JSON)
  // ----------------------------------------------------------------------
  private fun outputFormatBlock(): String =
      """
        Output:
        Return ONLY a JSON array of:
        {
          id: String,
          title: String,
          description: String | null,
          date: "YYYY-MM-DD'T'HH:mm",
          tags: [String],
          creator: String,
          participants: [String],
          location: { latitude: Double, longitude: Double },
          eventPicture: null
        }
        No commentary or markdown.
    """
          .trimIndent()

  private fun calculateAge(dob: LocalDate): Int {
    return Period.between(dob, LocalDate.now()).years
  }
}
