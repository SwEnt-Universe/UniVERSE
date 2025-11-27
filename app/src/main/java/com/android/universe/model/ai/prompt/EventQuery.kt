package com.android.universe.model.ai.prompt

import com.android.universe.model.user.UserProfile

/**
 * Bundles together all inputs required for generating events using the AI pipeline.
 *
 * An [EventQuery] is the top-level request object passed into [AIEventGen].
 * It contains the user's profile, task-specific configuration, and contextual
 * generation constraints.
 *
 * @property user The profile of the user requesting event generation.
 * @property task The specification of what should be generated (e.g., number
 *   of events, tag-relevance requirements). Defaults to [TaskConfig.Default].
 * @property context Environmental and situational context such as location,
 *   coordinates, radius, and time window. Defaults to [ContextConfig.Default].
 *
 * EventQuery acts as the single source of truth when constructing system + user
 * messages for OpenAI.
 */
data class EventQuery(
    val user: UserProfile,
    val task: TaskConfig = TaskConfig.Default,
    val context: ContextConfig = ContextConfig.Default
)
