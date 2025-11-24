package com.android.universe.model.ai

import com.android.universe.model.event.Event

/**
 * Defines the contract for generating event suggestions.
 *
 * Example usage: val query = EventQuery( user = profile, task = TaskConfig(eventSum = 5), //
 * generate 5 events context = ContextConfig( location = "Lausanne", radiusKm = 5, timeFrame =
 * "today" ) )
 *
 * generateEvents(query)
 */
interface EventGen {
  suspend fun generateEvents(query: EventQuery): List<Event>
}
