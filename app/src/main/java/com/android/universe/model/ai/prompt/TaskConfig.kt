package com.android.universe.model.ai.prompt

data class TaskConfig(
	val targetCity: String,
	val includeRealCoordinates: Boolean = true,
	val requireOutdoorOnly: Boolean = false
) {
	companion object {
		val Default = TaskConfig(targetCity = "Lausanne")
	}
}
