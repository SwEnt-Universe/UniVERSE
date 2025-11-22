package com.android.universe.model.ai.prompt

data class ContextConfig(
	val includeDate: Boolean = true,
	val includeWeather: Boolean = false,
	val radiusKm: Int? = null
) {
	companion object {
		val Default = ContextConfig()
	}
}
