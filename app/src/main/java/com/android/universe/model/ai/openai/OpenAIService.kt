package com.android.universe.model.ai.openai

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Streaming

/**
 * Retrofit service interface defining HTTP endpoints for OpenAI.
 *
 * Responsibilities:
 * - Map Kotlin functions to REST endpoints (chat completions, streaming completions, etc.)
 * - Provide strongly typed request and response models used by [OpenAIEventGen].
 *
 * Does not contain business logic — only API definitions.
 */
interface OpenAIService {

  // Non-streaming: normal chat completion
  @POST("chat/completions")
  suspend fun chatCompletion(@Body request: ChatCompletionRequest): Response<ChatCompletionResponse>

  // Streaming (real-time token events)
  @Streaming
  @POST("chat/completions")
  suspend fun chatCompletionStream(@Body request: ChatCompletionRequest): Response<ResponseBody>
}

// ===================================================================
// Request / Response model definitions
// ===================================================================

/**
 * Chat completion input object.
 *
 * @param model OpenAI model id (e.g. "gpt-5-nano")
 * @param messages Conversation history (system + user messages)
 * @param temperature (optional) creativity parameter (range from 0.0 to 1.0)
 * @param max_completion_tokens (optional) max generated output tokens
 * @param response_format (optional) structured output format (JSON schema)
 */
@Serializable
data class ChatCompletionRequest(
    val model: String,
    val messages: List<Message>,
    val temperature: Double? = null,
    val max_completion_tokens: Int? = null,
    val response_format: ResponseFormat? = null
)

/** One chat message (system / user / assistant). */
@Serializable data class Message(val role: String, val content: String)

/** Controls structured output, e.g. JSON schema. */
@Serializable
data class ResponseFormat(
    val type: String, // "json_schema"
    val json_schema: JsonObject? = null
)

/** Response wrapper returned by OpenAI. */
@Serializable
data class ChatCompletionResponse(
    val id: String,
    val choices: List<Choice>,
    val created: Long,
    val model: String,
    val usage: Usage? = null
)

/**
 * Represents a single model-generated alternative within a chat completion response.
 *
 * OpenAI may return multiple "choices" for a single request, each containing:
 * - **index** — The position of this choice in the returned list.
 * - **message** — The assistant's generated message content (role + text).
 * - **finish_reason** — Why the model stopped generating:
 *     - `"stop"`: natural stopping (e.g., end of message)
 *     - `"length"`: hit max token limit
 *     - `"content_filter"`: blocked due to safety filter
 *     - `"error"`: generation interrupted
 *     - `null`: provider omitted the field
 *
 * Our case this is almost always a single result (index `0`) because we request deterministic JSON
 * output for event generation, but the model still formally returns a list.
 */
@Serializable
data class Choice(val index: Int, val message: Message, val finish_reason: String? = null)

/**
 * Token usage metadata returned by OpenAI for billing and diagnostics.
 *
 * Contains counts for:
 * - **prompt_tokens** — Tokens consumed by the input (system + user messages).
 * - **completion_tokens** — Tokens produced by the model.
 * - **total_tokens** — Sum of prompt + completion tokens.
 *
 * OpenAI may omit this object for streaming responses, in which case `usage` on the parent
 * [ChatCompletionResponse] will be `null`.
 */
@Serializable
data class Usage(val prompt_tokens: Int, val completion_tokens: Int, val total_tokens: Int)
