package com.android.universe.model.ai

import com.android.universe.model.ai.prompt.EventQuery
import com.android.universe.model.event.Event

/**
 * Defines the contract for generating an event with AI.
 *
 * Implementations of this interface take an [EventQuery]—which describes the user, the task, and
 * any contextual preferences—and return a list of domain-level [Event] objects produced by an AI
 * model or another generation mechanism.
 *
 * Example usage:
 * ```
 * val generator: AIEventGen = OpenAIEventGen(openAIService)
 *
 * val query = EventQuery(
 *     user = currentUserProfile,
 *     task = EventQuery.Task.AUTOGEN,
 *     context = EventQuery.Context(location = userLocation)
 * )
 *
 * val events: List<Event> = generator.generateEvents(query)
 * // Use events for rendering on the map or storing in Firestore
 * ```
 */
interface AIEventGen {
  suspend fun generateEvents(query: EventQuery): List<Event>
}
