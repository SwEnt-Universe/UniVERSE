package com.android.universe.model.ai

import com.android.universe.model.location.Location
import com.android.universe.model.tag.Tag
import com.android.universe.model.user.UserProfile
import java.time.LocalDateTime

object PromptBuilder {

  fun build(profile: UserProfile): String {
    return listOf(
            responseBlock(),
            taskBlock(),
            userProfileBlock(profile),
            contextBlock(),
            outputFormatBlock())
        .joinToString("\n\n")
  }

  private fun calculateAge(dob: java.time.LocalDate): Int {
    return java.time.Period.between(dob, java.time.LocalDate.now()).years
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
        Current Date: ${java.time.LocalDate.now()}
        """
          .trimIndent()

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
        
        JSON Example Structure:
        [
          {
            "id": "example-id-123",
            "title": "Rock Night at Flon",
            "description": "Local metal event",
            "date": "2025-03-21T20:00",
            "tags": ["MUSIC", "METAL"],
            "creator": "example-user-id",
            "participants": ["user-1", "user-2"],
            "location": { "latitude": 46.52, "longitude": 6.63 },
            "eventPicture": null
          }
        ]
        """
          .trimIndent()
}

data class Event(
    val id: String,
    val title: String,
    val description: String? = null,
    val date: LocalDateTime,
    val tags: Set<Tag>,
    val creator: String,
    val participants: Set<String> = emptySet(),
    val location: Location,
    val eventPicture: ByteArray? = null
) {}
