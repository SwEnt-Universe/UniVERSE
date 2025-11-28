package com.android.universe.model.ai.orchestration

import com.android.universe.model.ai.AIEventGen
import com.android.universe.model.ai.prompt.ContextConfig
import com.android.universe.model.ai.prompt.EventQuery
import com.android.universe.model.ai.prompt.TaskConfig
import com.android.universe.model.event.Event
import com.android.universe.model.event.EventRepository
import com.android.universe.model.user.UserRepository
import com.tomtom.sdk.map.display.map.VisibleRegion

/**
 * Coordinates passive AI-driven event generation.
 *
 * Responsibilities:
 *  - Evaluate passive generation policy (cooldown, map conditions, etc.)
 *  - Build a fully scoped [EventQuery] using user profile + viewport context
 *  - Invoke the AI backend through [AIEventGen]
 *  - Persist generated events using the [EventRepository]
 *
 * This orchestrator contains **no Android/UI dependencies** and is safe to use
 * inside ViewModels, background workers, or domain services.
 */
class AIEventGenOrchestrator(
  private val ai: AIEventGen,
  private val events: EventRepository,
  private val users: UserRepository,
  private val policy: PassiveAIGenPolicy
) {

  /**
   * Attempts to generate passive AI events for the user.
   *
   * @param currentUserId ID of the active user.
   * @param viewport The region currently visible on the map (four corner coordinates).
   * @param numEvents The number of existing events in the viewport.
   * @param lastGen Timestamp of the last AI generation (ms).
   * @param now Current timestamp (ms).
   *
   * @return A list of newly generated [Event], or an empty list if the policy gate rejects.
   */
  suspend fun maybeGenerate(
    currentUserId: String,
    viewport: VisibleRegion?,
    numEvents: Int,
    lastGen: Long,
    now: Long
  ): List<Event> {

    if (viewport == null) {
      // Cannot reason about passive generation without a known viewport
      return emptyList()
    }

    if (!policy.shouldGenerate(
        viewport = viewport,
        numEvents = numEvents,
        lastGenTimestamp = lastGen,
        now = now
      )) {
      return emptyList()
    }

    // Load user profile (tags, age, preferences, etc.)
    val user = users.getUser(currentUserId)

    // Build prompt context (center + radius derived from VisibleRegion)
    val context = ContextConfig.fromVisibleRegion(viewport)

    // TaskConfig can be expanded later (e.g. "generate 5 events", "tag matching")
    val query = EventQuery(
      user = user,
      task = TaskConfig.Default,
      context = context
    )

    // Generate events via AI
    val generated = ai.generateEvents(query)

    // Store them (dedup / pollution guards added later)
    generated.forEach { events.addEvent(it) }

    return generated
  }
}
