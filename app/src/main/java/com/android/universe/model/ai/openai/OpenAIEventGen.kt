package com.android.universe.model.ai.openai

import com.android.universe.model.ai.AIEventGen
import com.android.universe.model.ai.prompt.EventQuery
import com.android.universe.model.ai.ResponseParser
import com.android.universe.model.ai.prompt.PromptBuilder
import com.android.universe.model.event.Event

private const val MAX_TOKENS = 800

/** See [OpenAI pricing](https://platform.openai.com/docs/pricing?utm_source=chatgpt.com) */
private const val MODEL = "gpt-4o-mini"

class OpenAIEventGen(private val service: OpenAIService) : AIEventGen {

  override suspend fun generateEvents(query: EventQuery): List<Event> {

    // Build prompts
    val system = PromptBuilder.buildSystemMessage()
    val user = PromptBuilder.buildUserMessage(query.user, query.task, query.context)

    // Strict JSON schema
    val eventFormat = ResponseFormat(type = "json_schema", json_schema = EventSchema.jsonObject)

    // Build request body
    val request =
        ChatCompletionRequest(
            model = MODEL,
            messages =
                listOf(
                    Message(role = "system", content = system),
                    Message(role = "user", content = user)),
            max_completion_tokens = MAX_TOKENS,
            response_format = eventFormat)

    // Call OpenAI API
    val response = service.chatCompletion(request)

    if (!response.isSuccessful) {
      val error = response.errorBody()?.string()
      throw IllegalStateException("OpenAI error: ${response.code()} ${response.message()}\n$error")
    }

    // Extract completion
    val body = response.body()
    val choice =
        body?.choices?.firstOrNull() ?: throw IllegalStateException("OpenAI returned no choices")

    val raw = choice.message.content.orEmpty()

    // Validate content
    if (raw.isBlank()) {
      throw IllegalStateException(
          "OpenAI returned empty content. " +
              "finish_reason=${choice.finish_reason}, " +
              "prompt_tokens=${body?.usage?.prompt_tokens}, " +
              "completion_tokens=${body?.usage?.completion_tokens}")
    }

    // Parse JSON → events
    return try {
      ResponseParser.parseEvents(raw)
    } catch (e: Exception) {
      throw IllegalStateException("Failed to parse model output: ${e.message}\nInput was:\n$raw")
    }
  }
}
