package com.android.universe.model.ai.prompt

data class TaskConfig(
    val eventCount: Int? = null, // number of events to generate
    val requireRelevantTags: Boolean = true,
) {
  companion object {
    val Default = TaskConfig()
  }
}
