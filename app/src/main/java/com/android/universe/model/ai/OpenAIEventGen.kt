package com.android.universe.model.ai

import com.android.universe.model.ai.prompt.PromptBuilder
import com.android.universe.model.event.Event

/**
 * Maximum number of tokens the model is allowed to generate. Higher values allow longer responses
 * but increase cost and latency.
 */
private const val MAX_TOKENS = 800

/**
 * Controls randomness/creativity of the model’s output. 0.0 = deterministic, 1.0 = highly creative.
 * For event generation we allow creativity, so 0.9 is used.
 */
private const val TEMPERATURE = 0.9

/**
 * The OpenAI model used for event generation.
 *
 * **gpt-5-nano** is the most cost efficient model Pricing (as of 2025-11 — may change):
 * - **Input:** ~$0.02 per 1M tokens
 * - **Output:** ~$0.04 per 1M tokens
 *
 * @see <a href="https://platform.openai.com/docs/pricing">OpenAI Pricing</a>
 */
private const val MODEL = "gpt-5-nano"

class OpenAIEventGen(private val service: OpenAIService) : EventGen {

  override suspend fun generateEvents(query: EventQuery): List<Event> {

    val system = PromptBuilder.buildSystemMessage()
    val user = PromptBuilder.buildUserMessage(query.user, query.task, query.context)

    // Build OpenAI request
    val request =
        ChatCompletionRequest(
            model = MODEL,
            messages =
                listOf(
                    Message(role = "system", content = system),
                    Message(role = "user", content = user)),
            max_tokens = MAX_TOKENS,
            temperature = TEMPERATURE)

    // Call OpenAI API
    val response = service.chatCompletion(request)
    if (!response.isSuccessful) {
      throw IllegalStateException("OpenAI error: ${response.code()} ${response.message()}")
    }

    val json =
        response.body()?.choices?.first()?.message?.content
            ?: throw IllegalStateException("OpenAI returned empty response")

    // Parse to domain objects
    return ResponseParser.parseEvents(json)
  }
}
