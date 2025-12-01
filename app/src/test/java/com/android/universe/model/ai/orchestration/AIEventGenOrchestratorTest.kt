package com.android.universe.model.ai.orchestration

import com.android.universe.model.ai.openai.OpenAIEventGen
import com.android.universe.model.ai.openai.OpenAIServiceFake
import com.android.universe.model.event.Event
import com.android.universe.model.event.FakeEventRepository
import com.android.universe.model.location.Location
import com.android.universe.model.tag.Tag
import com.android.universe.model.user.FakeUserRepository
import com.android.universe.model.user.UserProfile
import com.android.universe.ui.utils.ViewportGeometry
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Tests the AIEventGenOrchestrator using ONLY real fakes:
 * - FakeEventRepository
 * - FakeUserRepository
 * - OpenAIServiceFake
 * - OpenAIEventGen
 * - PassiveAIGenPolicy (real)
 *
 * No mocking, no subclassing, no artificial behavior.
 */
class AIEventGenOrchestratorTest {

  private lateinit var users: FakeUserRepository
  private lateinit var events: FakeEventRepository

  private val vp = ViewportGeometry(centerLat = 46.52, centerLon = 6.63, radiusKm = 2.0)

  private val user =
      UserProfile(
          uid = "u1",
          username = "Hans",
          firstName = "Hans",
          lastName = "P",
          country = "CH",
          description = "bio",
          dateOfBirth = LocalDate.of(2000, 1, 1),
          tags = emptySet<Tag>())

  @Before
  fun setup() = runTest {
    users = FakeUserRepository()
    users.addUser(user)

    events = FakeEventRepository()
  }

  // -------------------------------------------------------------------------
  // TEST 1 — Cooldown rejects (real PassiveAIGenPolicy)
  // -------------------------------------------------------------------------

  @Test
  fun rejects_dueToCooldown_returnsEmptyList() = runTest {
    val policy = PassiveAIGenPolicy()
    val ai = OpenAIEventGen(OpenAIServiceFake())
    val orch = AIEventGenOrchestrator(ai, events, users, policy)

    // now - lastGen < REQUEST_COOLDOWN → Reject
    val out =
        orch.maybeGenerate(currentUserId = "u1", vpGeometry = vp, lastGen = 1000L, now = 1001L)

    assertTrue(out.isEmpty())
    assertTrue(events.getAllEvents().isEmpty())
  }

  // -------------------------------------------------------------------------
  // TEST 2 — Radius too large → policy rejects
  // -------------------------------------------------------------------------

  @Test
  fun rejects_dueToLargeRadius_returnsEmpty() = runTest {
    val policy = PassiveAIGenPolicy()
    val ai = OpenAIEventGen(OpenAIServiceFake())
    val orch = AIEventGenOrchestrator(ai, events, users, policy)

    // Force radius rejection using real logic
    val zoomedOutVp = vp.copy(radiusKm = 9999.0)

    val out =
        orch.maybeGenerate(
            currentUserId = "u1", vpGeometry = zoomedOutVp, lastGen = 0L, now = 50_000L)

    assertTrue(out.isEmpty())
    assertTrue(events.getAllEvents().isEmpty())
  }

  // -------------------------------------------------------------------------
  // TEST 3 — Density too high → Reject
  // -------------------------------------------------------------------------

  @Test
  fun rejects_dueToDensityTooManyEvents() = runTest {
    val policy = PassiveAIGenPolicy()
    val ai = OpenAIEventGen(OpenAIServiceFake())
    val orch = AIEventGenOrchestrator(ai, events, users, policy)

    // Use a very small viewport radius to force a tiny density threshold
    val tinyVp = vp.copy(radiusKm = 0.2) // threshold ≈ 1 event for MIN_EVENT_SPACING_KM=0.15

    val closeLoc = Location(46.52, 6.63)

    // Add just enough events to exceed threshold (2 events > ~1 threshold)
    repeat(3) { i ->
      events.addEvent(
          Event(
              id = "e$i",
              title = "T",
              description = "D",
              date = LocalDateTime.now(),
              location = closeLoc,
              tags = emptySet(),
              creator = "AI",
              participants = emptySet(),
              eventPicture = null))
    }

    val out =
        orch.maybeGenerate(currentUserId = "u1", vpGeometry = tinyVp, lastGen = 0L, now = 100_000L)

    // Must reject due to density
    assertTrue(out.isEmpty())

    // Repository should still have only the manual events
    assertEquals(3, events.getAllEvents().size)
  }

  // -------------------------------------------------------------------------
  // TEST 4 — Full success scenario (real fakes, real policy)
  // -------------------------------------------------------------------------

  @Test
  fun success_generatesAndPersistsEvents() = runTest {
    val policy = PassiveAIGenPolicy()
    val ai = OpenAIEventGen(OpenAIServiceFake())
    val orch = AIEventGenOrchestrator(ai, events, users, policy)

    val out =
        orch.maybeGenerate(currentUserId = "u1", vpGeometry = vp, lastGen = 0L, now = 1_000_000L)

    // Fake OpenAI returns exactly one event
    assertEquals(1, out.size)
    val rawEvent = out.first()

    assertEquals("Fake Rock Concert", rawEvent.title)
    assertEquals("A generated test event", rawEvent.description)

    // Check persistence
    val stored = events.getAllEvents()
    assertEquals(1, stored.size)

    val storedEvent = stored.first()

    // FakeEventRepository assigns NEW IDs during persistAIEvents
    assertNotEquals(rawEvent.id, storedEvent.id)

    // Core fields preserved
    assertEquals(rawEvent.title, storedEvent.title)
    assertEquals(rawEvent.description, storedEvent.description)
    assertEquals(rawEvent.location, storedEvent.location)
    assertEquals(rawEvent.tags, storedEvent.tags)
  }
}
