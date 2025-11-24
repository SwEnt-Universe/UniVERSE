package com.android.universe.model.ai

import com.android.universe.model.event.Event
import com.android.universe.model.tag.Tag
import com.android.universe.model.user.UserProfile
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.junit.Ignore
import org.junit.Test

@Ignore("OpenAI query. Run manually only")
class OpenAIEventGenTest {

  @Test
  fun testOpenAIEventGenWithDummyData() = runBlocking {
    // 1. Use real EventGen
    val generator = OpenAIProvider.eventGen

    // 2. Dummy user profile
    val dummyDate = LocalDate.of(2000, 8, 11)

    val studentTags =
        setOf(
            Tag.ROCK,
            Tag.POP,
            Tag.ELECTRONIC,
            Tag.LIVE_MUSIC,
            Tag.HIKING,
            Tag.RUNNING,
            Tag.FITNESS,
            Tag.CYCLING,
            Tag.CAFES,
            Tag.STREET_FOOD,
            Tag.BRUNCH,
            Tag.BARS,
            Tag.CINEMA,
            Tag.COMEDY,
            Tag.VIDEO_GAMES,
            Tag.CHESS,
            Tag.PROGRAMMING,
            Tag.AI,
            Tag.COMPUTER_SCIENCE,
            Tag.MATHEMATICS,
            Tag.PHYSICS)

    val studentProfile =
        UserProfile(
            uid = "69",
            username = "ai_69",
            firstName = "Student",
            lastName = "Studentson",
            country = "CH",
            description = "Student",
            dateOfBirth = dummyDate,
            tags = studentTags)

    // 3. Run actual OpenAI query
    val events: List<Event> = generator.generateEventsForUser(studentProfile)

    // 4. Print parsed event objects
    println("=== RAW EVENTS RETURNED BY OPENAI ===")
    events.forEach { println(it) }
  }
}
