package com.android.universe.model.ai

import com.android.universe.model.ai.prompt.ContextConfig
import com.android.universe.model.ai.prompt.TaskConfig
import com.android.universe.model.tag.Tag
import com.android.universe.model.user.UserProfile
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.junit.Test

class FakeOpenAIEventGenTest {

  private val fakeService = OpenAIServiceFake()
  private val eventGen = OpenAIEventGen(fakeService)

  private val dummyDate = LocalDate.of(2000, 8, 11)

  private val allTags =
      (Tag.getTagsForCategory(Tag.Category.TOPIC) + Tag.getTagsForCategory(Tag.Category.FOOD))
          .toSet()

  private val testUser =
      UserProfile(
          uid = "69",
          username = "ai_69",
          firstName = "AI",
          lastName = "Base",
          country = "CH",
          description = "Has all tags, country = switzerland",
          dateOfBirth = dummyDate,
          tags = allTags)

  @Test
  fun `test parsing of json response`() = runBlocking {
    val query =
        EventQuery(
            user = testUser,
            task = TaskConfig(eventCount = 1), // tells the model how many to return
            context = ContextConfig(location = "Lausanne"))

    val events = eventGen.generateEvents(query)

    assert(events.isNotEmpty())
    assert(events.first().title == "Fake Rock Concert")
    println(events)
  }
}
