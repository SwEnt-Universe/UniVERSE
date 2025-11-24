package com.android.universe.model.ai

import com.android.universe.model.ai.prompt.PromptBuilder
import com.android.universe.model.event.Event

class OpenAIEventGen(
  private val service: OpenAIService
) : EventGen {

  override suspend fun generateEvents(query: EventQuery): List<Event> {

    val system = PromptBuilder.buildSystemMessage()
    val user = PromptBuilder.buildUserMessage(
      query.user, query.task, query.context
    )

    // Build OpenAI request
    val request = ChatCompletionRequest(
      model = "gpt-5-nano",
      messages = listOf(
        Message(role = "system", content = system),
        Message(role = "user", content = user)
      ),
      max_tokens = 800,
      temperature = 0.9
    )

    // Call OpenAI API
    val response = service.chatCompletion(request)
    if (!response.isSuccessful) {
      throw IllegalStateException("OpenAI error: ${response.code()} ${response.message()}")
    }

    val json = response.body()?.choices?.first()?.message?.content
      ?: throw IllegalStateException("OpenAI returned empty response")

    // Parse to domain objects
    return ResponseParser.parseEvents(json)
  }
}
