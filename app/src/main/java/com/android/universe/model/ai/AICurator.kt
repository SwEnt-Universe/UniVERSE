package com.android.universe.model.ai

import com.android.universe.model.event.Event
import com.android.universe.model.user.UserProfile

/**  */
interface AICurator {
  suspend fun generateEventsForUser(profile: UserProfile): List<Event>
}
