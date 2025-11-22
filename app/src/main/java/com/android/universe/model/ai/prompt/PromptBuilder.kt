package com.android.universe.model.ai.prompt

import com.android.universe.model.user.UserProfile
import java.time.LocalDate
import java.time.Period

/**
 * Builds the full prompt string sent to OpenAI for generating event recommendations.
 *
 * Responsibilities:
 * - Convert a [com.android.universe.model.user.UserProfile] into contextual prompt text
 * - Include metadata such as location, preferences, and instructions
 * - Specify required JSON output format and constraints
 *
 * The generated prompt is used as input to the Chat Completion request.
 */
object PromptBuilder {

  fun build(
		profile: UserProfile,
		taskConfig: TaskConfig = TaskConfig.Default,
		contextConfig: ContextConfig = ContextConfig.Default
  ): String {
    return listOf(
      responseBlock(),
      taskBlock(taskConfig),
      userProfileBlock(profile),
      contextBlock(contextConfig),
      outputFormatBlock()
    ).joinToString("\n\n")
  }

  private fun calculateAge(dob: LocalDate): Int {
    return Period.between(dob, LocalDate.now()).years
  }

  private fun responseBlock(): String =
      "You are EventCuratorGPT, an assistant that generates realistic event suggestions."

  private fun taskBlock(): String =
      """
        Task:
        Generate realistic public event ideas that could actually take place in Lausanne.
        Each event must:
        - be feasible (no fantasy or impossible situations)
        - match the user's interests
        - include a short description
        - include real coordinates within Lausanne
        - include relevant tags
        """
          .trimIndent()

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

  // TODO! See if it is possible to include the weather here
  private fun contextBlock(): String =
      """
        Context:
        Location: Lausanne, Switzerland
        Current Date: ${LocalDate.now()}
        """
          .trimIndent()

  // TODO! See if this is optimal
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
}