package com.android.universe.model.ai.prompt

//TODO Figure out what is necessary here
data class TaskConfig(
	val requireRealCoordinates: Boolean = true,
	val requireRelevantTags: Boolean = true,
) {
	companion object {
		val Default = TaskConfig()
	}
}
