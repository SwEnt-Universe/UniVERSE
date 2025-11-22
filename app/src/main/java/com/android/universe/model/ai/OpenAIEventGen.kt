package com.android.universe.model.ai

import com.android.universe.model.event.Event
import com.android.universe.model.user.UserProfile

class OpenAIEventGen(
	private val service: OpenAIService
) : EventGen {

	override suspend fun generateEventsForUser(profile: UserProfile): List<Event> {
		// 1: build the prompt
		val prompt = PromptBuilder.build(profile)

		// 2: create request
		val request = ChatCompletionRequest(
			model = "gpt-4o-mini",
			messages = listOf(
				Message(role = "system", content = "You are UniVERSE Event Curator."),
				Message(role = "user", content = prompt)
			),
			max_tokens = 800,
			temperature = 0.9
		)

		// 3: call OpenAI API
		val response = service.chatCompletion(request)

		if (!response.isSuccessful) {
			throw IllegalStateException("OpenAI error: ${response.code()} ${response.message()}")
		}

		val json = response.body()?.choices?.first()?.message?.content
			?: throw IllegalStateException("OpenAI returned empty response")

		// 4: parse returned JSON into objects
		return ResponseParser.parseEvents(json)
	}
}
