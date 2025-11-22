package com.android.universe.model.ai

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Streaming

/**
 * Retrofit service interface defining HTTP endpoints for OpenAI.
 *
 * Responsibilities:
 * - Map Kotlin functions to REST endpoints (chat completions, streaming completions, etc.)
 * - Provide strongly typed request and response models used by [OpenAIEventGen]
 *
 * Does not contain business logic — only API definitions.
 * @see <a href="https://platform.openai.com/docs/api-reference/chat">OpenAI Chat API</a>
 */
interface OpenAIService {

	// Non-streaming chat completion
	@POST("chat/completions")
	suspend fun chatCompletion(
		@Body request: ChatCompletionRequest
	): Response<ChatCompletionResponse>

	// Streaming chat completion (for real-time typing effect)
	@Streaming
	@POST("chat/completions")
	suspend fun chatCompletionStream(
		@Body request: ChatCompletionRequest
	): Response<okhttp3.ResponseBody>
}

// ===================================================================
// Request / Response models
// ===================================================================

data class ChatCompletionRequest(
	val model: String = "gpt-4o-mini",
	val messages: List<Message>,
	val temperature: Double? = null,
	val max_tokens: Int? = null,
	val top_p: Double? = null,
	val stream: Boolean? = null,        // set to true for streaming endpoint
	val user: String? = null            // end-user identifier (for abuse monitoring)
)

data class Message(
	val role: String,         // "system", "user", "assistant", "tool"
	val content: String,
	val name: String? = null, // optional for role="tool" or function calling
	val tool_calls: List<ToolCall>? = null,
	val tool_call_id: String? = null
)

data class ChatCompletionResponse(
	val id: String,
	val choices: List<Choice>,
	val created: Long,
	val model: String,
	val usage: Usage?
)

data class Choice(
	val index: Int,
	val message: Message,
	val finish_reason: String?   // "stop", "length", "tool_calls", etc.
)

data class Usage(
	val prompt_tokens: Int,
	val completion_tokens: Int,
	val total_tokens: Int
)

data class ToolCall(
	val id: String,
	val type: String = "function",
	val function: FunctionCall
)

data class FunctionCall(
	val name: String,
	val arguments: String // JSON string
)