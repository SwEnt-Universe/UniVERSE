package com.android.universe.model.ai.prompt

import com.android.universe.model.user.UserProfile
import java.time.LocalDate
import java.time.Period

/**
 * Builds the complete prompt string sent to OpenAI when generating event recommendations.
 *
 * This class is intentionally a single object. Flexibility is controlled through [TaskConfig]
 * and [ContextConfig] rather than splitting logic across many files.
 */
object PromptBuilder {

  fun build(
    profile: UserProfile,
    task: TaskConfig = TaskConfig.Default,
    context: ContextConfig = ContextConfig.Default
  ): String {
    return listOf(
      systemBlock(),
      taskBlock(task),
      userProfileBlock(profile),
      contextBlock(context),
      outputFormatBlock()
    ).joinToString("\n\n")
  }

  private fun systemBlock(): String =
    "You are EventCuratorGPT, an assistant that generates realistic event suggestions."

  private fun taskBlock(task: TaskConfig): String = """
        Task:
        Generate realistic public events that could take place in ${task.city}.
        Requirements:
        - must be feasible (no fantasy)
        - must match the user’s interests
        - include a short description
        ${if (task.requireRealCoordinates) "- include real coordinates inside ${task.city}" else ""}
        ${if (task.requireRelevantTags) "- include meaningful tags" else ""}
        ${if (task.outdoorOnly) "- only outdoor events" else ""}
    """.trimIndent()

  private fun userProfileBlock(profile: UserProfile): String =
      """
        User Profile:
        UID: ${profile.uid}
        Name: ${profile.firstName} ${profile.lastName}
        Age: ${calculateAge(profile.dateOfBirth)}
        Country: ${profile.country}
        Description: ${profile.description ?: "No description provided"}
        Interests (tags): ${profile.tags.joinToString(", ")}
        """
          .trimIndent()

  private fun contextBlock(context: ContextConfig): String = """
        Context:
        Location: Lausanne, Switzerland
        ${if (context.includeDate) "Current Date: ${LocalDate.now()}" else ""}
        ${context.radiusKm?.let { "Search Radius: $it km" } ?: ""}
        ${if (context.includeWeather) "Weather: <INSERT WEATHER DATA>" else ""}
    """.trimIndent()

  // TODO! See if this is optimal, we need to define the ID later on in the flow.
  private fun outputFormatBlock(): String =
      """
        Response Format:
        Return ONLY valid JSON. No commentary, no markdown, no explanations.
        
        Event Object Definition:
        id: String
        title: String
        description: String | null
        date: LocalDateTime "yyyy-MM-dd'T'HH:mm"
        tags: Set<Tag>
        creator: String
        participants: Set<String>
        location: Location(latitude: Double, longitude: Double)
        eventPicture: ByteArray | null
        
        Tags must be returned using display names exactly as provided:
        ["Metal", "Rock", "Jazz"]

        JSON Example Structure:
        [
          {
            "id": "example-id-123",
            "title": "Rock Night at Flon",
            "description": "Local metal event",
            "date": "2025-03-21T20:00",
            "tags": ["Music", "Metal"],
            "creator": "example-user-id",
            "participants": ["user-1", "user-2"],
            "location": { "latitude": 46.52, "longitude": 6.63 },
            "eventPicture": null
          }
        ]
        """
          .trimIndent()

  private fun calculateAge(dob: LocalDate): Int {
    return Period.between(dob, LocalDate.now()).years
  }
}