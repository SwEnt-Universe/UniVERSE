package com.android.universe.model.ai

import kotlinx.serialization.Serializable
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

@Serializable
data class ChatCompletionRequest(
	val model: String,
	val messages: List<Message>,
	val temperature: Double? = null,
	val max_tokens: Int? = null,
	val top_p: Double? = null,
	val stream: Boolean? = null,
	val user: String? = null
)

@Serializable
data class Message(
	val role: String,
	val content: String
)

@Serializable
data class ChatCompletionResponse(
	val id: String,
	val choices: List<Choice>,
	val created: Long,
	val model: String,
	val usage: Usage?
)

@Serializable
data class Choice(
	val index: Int,
	val message: Message,
	val finish_reason: String? = null
)

@Serializable
data class Usage(
	val prompt_tokens: Int,
	val completion_tokens: Int,
	val total_tokens: Int
)