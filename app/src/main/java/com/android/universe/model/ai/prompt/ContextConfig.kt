package com.android.universe.model.ai.prompt

// TODO! Figure out how to deduce location
data class ContextConfig(
	val location: String = "Lausanne",
	val includeDate: Boolean = true,
	val radiusKm: Int? = null
) {
	companion object {
		val Default = ContextConfig()
	}
}
