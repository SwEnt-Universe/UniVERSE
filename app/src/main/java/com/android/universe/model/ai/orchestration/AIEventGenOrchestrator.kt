package com.android.universe.model.ai.orchestration

import com.android.universe.model.ai.AIEventGen
import com.android.universe.model.ai.prompt.ContextConfig
import com.android.universe.model.ai.prompt.EventQuery
import com.android.universe.model.ai.prompt.TaskConfig
import com.android.universe.model.event.Event
import com.android.universe.model.event.EventRepository
import com.android.universe.model.user.UserRepository
import com.tomtom.sdk.location.GeoPoint

/**
 * Orchestrates passive AI event generation.
 *
 * This class:
 * - Evaluates the passive generation policy
 * - Builds an EventQuery
 * - Calls AIEventGen
 * - Saves results through EventRepository
 *
 * It contains no Android or UI dependencies.
 */
class AIEventGenOrchestrator(
    private val ai: AIEventGen,
    private val events: EventRepository,
    private val users: UserRepository,
    private val policy: PassiveAIGenPolicy
) {

  suspend fun maybeGenerate(
      currentUserId: String,
      userLocation: GeoPoint?,
      cameraCenter: GeoPoint,
      zoom: Double,
      numEvents: Int,
      lastGen: Long,
      now: Long
  ): List<Event> {

    if (!policy.shouldGenerate(
        userLocation = userLocation,
        cameraCenter = cameraCenter,
        zoom = zoom,
        numEvents = numEvents,
        lastGenTimestamp = lastGen,
        now = now)) {
      return emptyList()
    }

    // Load user profile for context
    val user = users.getUser(currentUserId)

    // Build minimal query — refine later with context
    val query =
        EventQuery(
            user = user,
            task = TaskConfig.Default,
            context =
                ContextConfig.fromViewport(
                    userLocation = userLocation, cameraCenter = cameraCenter, zoom = zoom))

    val generated = ai.generateEvents(query)

    // Save events (no dedup/pollution guard for now)
    generated.forEach { events.addEvent(it) }

    return generated
  }
}
