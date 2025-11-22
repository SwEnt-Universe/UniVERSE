package com.android.universe.model.ai

import com.android.universe.model.event.Event
import com.android.universe.model.user.UserProfile

/**
 * Defines the contract for generating event suggestions.
 *
 * Example usage:
 * val events = eventGen.generateEventsForUser(profile)
 */
interface EventGen {

  /**
   * Generates events based on user profile
   */
  suspend fun generateEventsForUser(profile: UserProfile): List<Event>
}
