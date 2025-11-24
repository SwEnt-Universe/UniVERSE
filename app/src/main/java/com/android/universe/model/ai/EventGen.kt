package com.android.universe.model.ai

import com.android.universe.model.ai.prompt.ContextConfig
import com.android.universe.model.ai.prompt.TaskConfig
import com.android.universe.model.event.Event
import com.android.universe.model.user.UserProfile

/**
 * Defines the contract for generating event suggestions.
 *
 * Example usage:
 * val query = EventQuery(
 *     user = profile,
 *     task = TaskConfig(eventSum = 5),             // generate 5 events
 *     context = ContextConfig(
 *         location = "Lausanne",
 *         radiusKm = 5,
 *         timeFrame = "today"
 *     )
 * )
 *
 * generateEvents(query)
 */
interface EventGen {
  suspend fun generateEvents(query: EventQuery): List<Event>
}

