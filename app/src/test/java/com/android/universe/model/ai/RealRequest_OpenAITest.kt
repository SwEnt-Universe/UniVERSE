package com.android.universe.model.ai

import com.android.universe.model.ai.openai.OpenAIProvider
import com.android.universe.model.ai.prompt.ContextConfig
import com.android.universe.model.ai.prompt.EventQuery
import com.android.universe.model.ai.prompt.TaskConfig
import com.android.universe.model.event.Event
import com.android.universe.model.event.EventRepository
import com.android.universe.model.event.FakeEventRepository
import com.android.universe.model.tag.Tag
import com.android.universe.model.user.UserProfile
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.junit.Ignore
import org.junit.Test

/**
 * Manual integration tests for OpenAI event generation.
 *
 * These tests:
 * - Perform real, paid OpenAI requests
 * - Should only be run manually
 * - Are excluded from CI builds
 */
@Ignore("OpenAI requests. Run manually only")
class RealRequest_OpenAITest {

  // ------------------------------------------------------------------------
  // Helper: Construct a dummy query for OpenAI
  // ------------------------------------------------------------------------
  private fun buildDummyQuery(): EventQuery {
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
            Tag.PHYSICS,
        )

    val studentProfile =
        UserProfile(
            uid = "69",
            username = "ai_69",
            firstName = "Student",
            lastName = "Studentson",
            country = "CH",
            description = "Dummy Test User",
            dateOfBirth = dummyDate,
            tags = studentTags,
        )

    return EventQuery(
        user = studentProfile,
        task = TaskConfig(eventCount = 1),
        context =
            ContextConfig(
                location = "Lausanne",
                radiusKm = 5,
                timeFrame = "today",
                locationCoordinates = 46.5191 to 6.5668))
  }

  // ------------------------------------------------------------------------
  // TEST 1: calls OpenAI and prints results
  // ------------------------------------------------------------------------
  @Test
  fun testOpenAIRequest() = runBlocking {
    val eventGen = OpenAIProvider.eventGen
    val query = buildDummyQuery()

    val events: List<Event> = eventGen.generateEvents(query)

    println("=== RAW EVENTS RETURNED BY OPENAI ===")
    events.forEach { println(it) }

    assert(events.isNotEmpty()) { "No events returned by OpenAI." }
  }

  // ------------------------------------------------------------------------
  // TEST 2: Request and persistence:
  //  - generate events from OpenAI
  //  - use FakeEventRepository.saveAIEvents()
  //  - verify persistence + ID assignment
  // ------------------------------------------------------------------------
  @Test
  fun testOpenAIRequestAndPersistence() = runBlocking {
    val generator = OpenAIProvider.eventGen
    val repo: EventRepository = FakeEventRepository()
    val query = buildDummyQuery()

    // 1. Generate events
    val generated = generator.generateEvents(query)

    println("=== EVENTS GENERATED ===")
    generated.forEach { println(it) }

    assert(generated.isNotEmpty()) { "OpenAI returned no events" }
    assert(generated.all { it.id.isBlank() }) {
      "Generated events should not have IDs before being saved"
    }

    // 2. Store in repository
    val saved = repo.persistAIEvents(generated)

    println("=== EVENTS AFTER SAVE ===")
    saved.forEach { println(it) }

    assert(saved.all { it.id.isNotBlank() }) { "Events saved in repository must have assigned IDs" }

    // 3. Verify persistence
    val repoEvents = repo.getAllEvents()
    assert(repoEvents.size == saved.size) { "Repository event count mismatch" }

    saved.forEach { e ->
      assert(repoEvents.any { it.id == e.id }) { "Event with id ${e.id} not found in repository" }
    }
  }
}
