package com.android.universe.model.ai.prompt

/**
 * Describes the task-level parameters that govern how many events should be generated and whether
 * they must match the user's interests.
 *
 * TaskConfig complements [ContextConfig] by specifying **what** to generate, rather than **where**
 * or **when**.
 *
 * @property eventCount Optional explicit number of events to generate. If null, the model chooses a
 *   suitable count based on context.
 * @property requireRelevantTags Whether generated events must align with the user's preference
 *   tags. Enabled by default to promote personalized results.
 *
 * The [Default] preset provides sensible generation behavior without requiring the caller to
 * specify any fields.
 */
data class TaskConfig(
    val eventCount: Int? = null, // number of events to generate
    val requireRelevantTags: Boolean? = true,
) {
  companion object {
    val Default = TaskConfig()
  }
}
