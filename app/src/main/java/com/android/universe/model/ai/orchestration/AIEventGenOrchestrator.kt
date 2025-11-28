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
 * Pure domain logic — no Android/UI dependencies.
 *
 * Pipeline:
 *   1. Count events inside viewport
 *   2. Ask the passive policy whether we *should* generate
 *   3. If accepted, retrieve how many events to generate
 *   4. Build prompt context from map viewport
 *   5. Build task config (how many events AI should produce)
 *   6. Build EventQuery (user + task + context)
 *   7. Call AIEventGen
 *   8. Persist generated events
 */
class AIEventGenOrchestrator(
  private val ai: AIEventGen,
  private val events: EventRepository,
  private val users: UserRepository,
  private val policy: PassiveAIGenPolicy
) {

  /**
   * Tries to generate passive AI events.
   *
   * Returns:
   *  - A list of **newly generated events**
   *  - Or an **empty list** if the policy rejects generation
   */
  suspend fun maybeGenerate(
    currentUserId: String,
    viewport: VisibleRegion?,
    lastGen: Long,
    now: Long
  ): List<Event> {

    // ----------------------------------------------------
    // 0. Preconditions: we must know the map viewport
    // ----------------------------------------------------
    if (viewport == null) {
      // Without viewport geometry we cannot estimate radius, density, etc.
      return emptyList()
    }

    // ----------------------------------------------------
    // 1. Count current events inside the viewport
    // ----------------------------------------------------
    val numEvents = events.countEventsInViewport(viewport)

    // ----------------------------------------------------
    // 2. Pass everything to the policy and evaluate
    // ----------------------------------------------------
    val decision = policy.evaluate(
      viewport = viewport,
      numEvents = numEvents,
      lastGenTimestamp = lastGen,
      now = now
    )

    // If policy rejects, stop immediately
    if (decision is Decision.Reject) {
      return emptyList()
    }

    // If we are here, policy has accepted
    val eventsToGenerate = (decision as Decision.Accept).eventsToGenerate

    // Safety: never ask for zero or negative
    if (eventsToGenerate <= 0) return emptyList()

    // ----------------------------------------------------
    // 3. Load user profile for AI personalization
    // ----------------------------------------------------
    val user = users.getUser(currentUserId)

    // ----------------------------------------------------
    // 4. Define ContextConfig using viewport
    // ----------------------------------------------------
    val context = ContextConfig.fromVisibleRegion(viewport)

    // ----------------------------------------------------
    // 5. Build the task config
    //    - "eventCount" = how many AI should generate
    //    - For now we always require relevant tags (maybe remove this field)
    // ----------------------------------------------------
    val task = TaskConfig(
      eventCount = eventsToGenerate,
      requireRelevantTags = true
    )

    // ----------------------------------------------------
    // 6. Construct the final query for AIEventGen
    // ----------------------------------------------------
    val query = EventQuery(
      user = user,
      task = task,
      context = context
    )

    // ----------------------------------------------------
    // 7. Ask AI to generate the events
    // ----------------------------------------------------
    val generated = ai.generateEvents(query)

    // ----------------------------------------------------
    // 8. Persist events in Firestore
    // ----------------------------------------------------
    events.persistAIEvents(generated)

    // Return all new events to caller
    return generated
  }
}
