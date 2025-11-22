package com.android.universe.model.ai

import com.android.universe.model.ai.prompt.PromptBuilder
import com.android.universe.model.tag.Tag
import com.android.universe.model.user.UserProfile
import java.time.LocalDate
import org.junit.Test

class PromptBuilderTest {

  private val DummyDate = LocalDate.of(2000, 8, 11)

  val allTags =
      (Tag.getTagsForCategory(Tag.Category.TOPIC) + Tag.getTagsForCategory(Tag.Category.FOOD))
          .toSet()

  val tags = setOf(Tag.ROCK, Tag.POP)
  val allTags_CH_user =
      UserProfile(
          uid = "69",
          username = "ai_69",
          firstName = "AI",
          lastName = "Base",
          country = "CH",
          description = "Has all tags, country = switzerland",
          dateOfBirth = DummyDate,
          tags = allTags)

  @Test
  fun printPrompt() {
    val prompt = PromptBuilder.build(allTags_CH_user)
    println(prompt) // prints to test output window
  }
}
