package com.android.universe.model.ai.prompt

import com.android.universe.model.user.UserProfile
import java.time.LocalDate
import java.time.Period

/**
 * Lightweight prompt builder for generating event recommendations.
 * This version minimizes token usage by:
 *  - Removing verbose prose
 *  - Using compact JSON-like structures
 *  - Removing full example JSON
 *  - Compressing schema into a single definition line
 *  - Shrinking context and user profile blocks
 */
object PromptBuilder {

  fun build(
    profile: UserProfile,
    task: TaskConfig = TaskConfig.Default,
    context: ContextConfig = ContextConfig.Default
  ): String {
    return listOf(
      taskBlock(task),
      userBlock(profile),
      contextBlock(context),
      outputFormatBlock()
    ).joinToString("\n\n")
  }

  // ----------------------------------------------------------------------
  // TASK — compressed into one line
  // ----------------------------------------------------------------------
  private fun taskBlock(task: TaskConfig): String {
    val tags = if (task.requireRelevantTags) "tags," else ""

    return """
            Task:
            Generate realistic, feasible public events matching the user's interests and country.
            Requirements: short description, $tags no fantasy elements.
        """.trimIndent()
  }

  // ----------------------------------------------------------------------
  // USER — compressed compact JSON-like object
  // ----------------------------------------------------------------------
  private fun userBlock(profile: UserProfile): String {
    val age = calculateAge(profile.dateOfBirth)
    val tags = profile.tags.joinToString(",") { "\"${it.displayName}\"" }

    return """
            User: {
              uid: "${profile.uid}",
              name: "${profile.firstName} ${profile.lastName}",
              age: $age,
              country: "${profile.country}",
              interests: [$tags]
            }
        """.trimIndent()
  }

  // ----------------------------------------------------------------------
  // CONTEXT — compact structure, minimal prose
  // ----------------------------------------------------------------------
  private fun contextBlock(context: ContextConfig): String {
    val radius = context.radiusKm?.let { "\"radiusKm\": $it," } ?: ""
    val date = if (context.includeDate) "\"date\": \"${LocalDate.now()}\"," else ""
    //val weather = if (context.includeWeather) "\"weather\": \"<INSERT>\"," else ""

    return """
            Context: {
              "location": "${context.location}",
              $radius
              $date
            }
        """.trimIndent()
  }

  // ----------------------------------------------------------------------
  // OUTPUT FORMAT — ultra-compact schema (no example JSON)
  // ----------------------------------------------------------------------
  private fun outputFormatBlock(): String = """
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
    """.trimIndent()

  private fun calculateAge(dob: LocalDate): Int {
    return Period.between(dob, LocalDate.now()).years
  }
}
