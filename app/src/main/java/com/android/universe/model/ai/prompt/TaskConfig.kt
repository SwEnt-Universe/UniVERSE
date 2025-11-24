package com.android.universe.model.ai.prompt

// TODO Figure out what is necessary here
data class TaskConfig(
    val eventCount: Int? = null, // number of events to generate
    val requireRelevantTags: Boolean = true,
) {
  companion object {
    val Default = TaskConfig()
  }
}
