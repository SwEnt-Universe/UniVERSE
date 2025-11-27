package com.android.universe.model.ai.prompt

import com.android.universe.model.tag.Tag
import com.android.universe.model.user.UserProfile
import java.time.LocalDate
import org.junit.Test

class PromptBuilderTest {

  // TODO Move user to fixtures
  private val DummyDate = LocalDate.of(2000, 8, 11)

  private val allTags =
      (Tag.Companion.getTagsForCategory(Tag.Category.TOPIC) +
              Tag.Companion.getTagsForCategory(Tag.Category.FOOD))
          .toSet()

  private val user =
      UserProfile(
          uid = "69",
          username = "ai_69",
          firstName = "AI",
          lastName = "Base",
          country = "CH",
          description = "Has all tags, country = Switzerland",
          dateOfBirth = DummyDate,
          tags = allTags)

  @Test
  fun printPrompt() {
    val task = TaskConfig(eventCount = 5, requireRelevantTags = true)

    val context =
        ContextConfig(
            location = "Lausanne",
            radiusKm = 5,
            timeFrame = "today",
            locationCoordinates = 46.5191 to 6.5668)

    val system = PromptBuilder.buildSystemMessage()
    val userMsg = PromptBuilder.buildUserMessage(user, task, context)

    println("SYSTEM MESSAGE:\n$system\n")
    println("USER MESSAGE:\n$userMsg")
  }
}
