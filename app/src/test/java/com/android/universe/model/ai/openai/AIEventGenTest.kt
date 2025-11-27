package com.android.universe.model.ai.openai

import com.android.universe.model.ai.AIEventGen
import com.android.universe.model.ai.prompt.ContextConfig
import com.android.universe.model.ai.prompt.EventQuery
import com.android.universe.model.ai.prompt.TaskConfig
import com.android.universe.model.event.Event
import com.android.universe.model.tag.Tag
import com.android.universe.model.user.UserProfile
import java.time.LocalDate
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

/**
 * End-to-end test of OpenAIEventGen using OpenAIServiceFake.
 *
 * Covers:
 * - PromptBuilder (system + user)
 * - Request creation
 * - Fake service returning strict JSON
 * - ResponseParser JSON → DTO → Event mapping
 * - EventValidator correctness
 */
class AIEventGenTest {

  @Test
  fun generateEvents_parsesFakeJsonIntoEventObjects() = runTest {
    // Arrange
    val service = OpenAIServiceFake()
    val generator: AIEventGen = OpenAIEventGen(service)

    val tags = setOf(Tag.ROCK, Tag.MUSIC)

    val profile =
        UserProfile(
            uid = "u123",
            username = "johnny",
            firstName = "John",
            lastName = "Doe",
            country = "CH",
            description = "Test user",
            dateOfBirth = LocalDate.of(2000, 8, 11),
            tags = tags)

    val query =
        EventQuery(
            user = profile,
            task = TaskConfig(eventCount = 1, requireRelevantTags = true),
            context =
                ContextConfig(
                    location = "Lausanne",
                    locationCoordinates = 46.52 to 6.63,
                    radiusKm = 5,
                    timeFrame = "today"))

    // Act
    val events: List<Event> = generator.generateEvents(query)

    // Assert
    assertNotNull(events)
    assertEquals(1, events.size)

    val event = events.first()

    assertEquals("Fake Rock Concert", event.title)
    assertEquals("A generated test event", event.description)
    assertEquals(46.52, event.location.latitude, 0.0001)
    assertEquals(6.63, event.location.longitude, 0.0001)

    val tagNames = event.tags.map { it.displayName }.toSet()
    assertEquals(setOf("Rock", "Music"), tagNames)

    assertEquals("OpenAI", event.creator)
    assertEquals("2025-03-21T20:00", event.date.toString())
  }
}
