package com.android.universe.model.ai.prompt

data class TaskConfig(
	val city: String = "Lausanne",
	val requireRealCoordinates: Boolean = true,
	val requireRelevantTags: Boolean = true,
	val outdoorOnly: Boolean = false,
) {
	companion object {
		val Default = TaskConfig()
	}
}
