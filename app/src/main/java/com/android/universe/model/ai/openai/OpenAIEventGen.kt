package com.android.universe.model.ai.openai

import com.android.universe.model.ai.AIEventGen
import com.android.universe.model.ai.prompt.EventQuery
import com.android.universe.model.ai.prompt.PromptBuilder
import com.android.universe.model.ai.response.ResponseParser
import com.android.universe.model.event.Event

private const val MAX_TOKENS = 800

/** See [OpenAI pricing](https://platform.openai.com/docs/pricing?utm_source=chatgpt.com) */
private const val MODEL = "gpt-4o-mini"

/**
 * Generates events using the OpenAI Chat Completions API.
 *
 * The generation pipeline proceeds in five stages:
 * 1. **Prompt construction** — build system & user messages from the [EventQuery].
 * 2. **Response specification** — attach [EventSchema] as a strict JSON output contract.
 * 3. **API invocation** — send the request via [OpenAIService] and validate the HTTP result.
 * 4. **Model output validation** — ensure the completion contains usable JSON content.
 * 5. **Parsing & conversion** — parse JSON using [ResponseParser] and return domain [Event]
 *    objects.
 *
 * @property service Retrofit-backed OpenAI API client.
 */
class OpenAIEventGen(private val service: OpenAIService) : AIEventGen {

  /**
   * Executes the full OpenAI -> JSON -> Event pipeline.
   *
   * @param query High-level request describing who is asking, what to generate, and in which
   *   context.
   * @return A list of strongly-typed domain [Event] objects.
   * @throws IllegalStateException if the model returns invalid, empty, or unparsable output.
   */
  override suspend fun generateEvents(query: EventQuery): List<Event> {

    // ========================================================================
    // 1: PROMPT CONSTRUCTION
    // ========================================================================
    val system = PromptBuilder.buildSystemMessage()
    val user = PromptBuilder.buildUserMessage(query.user, query.task, query.context)

    // ========================================================================
    // 2: RESPONSE SPECIFICATION
    // ========================================================================
    val eventFormat = ResponseFormat(type = "json_schema", json_schema = EventSchema.jsonObject)

    // ========================================================================
    // 3: API INVOCATION
    // ========================================================================
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

    // ========================================================================
    // 4: MODEL OUTPUT VALIDATION
    // ========================================================================
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

    // ========================================================================
    // 5: PARSING
    // ========================================================================
    return ResponseParser.parseEvents(raw)
  }
}
