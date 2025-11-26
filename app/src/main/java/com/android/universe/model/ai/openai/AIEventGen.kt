package com.android.universe.model.ai.openai

import com.android.universe.model.ai.prompt.EventQuery
import com.android.universe.model.event.Event

/**
 * Defines the contract for generating event suggestions.
 *
 * generateEvents(query)
 */
interface AIEventGen {
  suspend fun generateEvents(query: EventQuery): List<Event>
}
