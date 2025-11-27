package com.android.universe.model.ai.openai

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody
import retrofit2.Response

/**
 * Fake implementation of [OpenAIService] used for unit testing.
 *
 * Returns a deterministic chat completion response containing a valid top-level JSON object with an
 * "events" array matching the schema expected by [ResponseParser] and [OpenAIEventGen].
 * - No network calls
 * - No OpenAI dependency
 * - Ensures stable, repeatable test behavior
 */
class OpenAIServiceFake : OpenAIService {

  override suspend fun chatCompletion(
      request: ChatCompletionRequest
  ): Response<ChatCompletionResponse> {

    // IMPORTANT:
    // The OpenAIEventGen → ResponseParser pipeline expects:
    //   {
    //     "events": [ { eventDTO } ]
    //   }
    //
    // This JSON is validated and parsed into EventDTO → Event.
    val json =
        """
        {
          "events": [
            {
              "title": "Fake Rock Concert",
              "description": "A generated test event",
              "date": "2025-03-21T20:00",
              "tags": ["Rock", "Music"],
              "location": { "latitude": 46.52, "longitude": 6.63 }
            }
          ]
        }
        """
            .trimIndent()

    val fakeResponse =
        ChatCompletionResponse(
            id = "fake-id",
            created = System.currentTimeMillis(),
            model = "fake-model",
            usage = null,
            choices =
                listOf(
                    Choice(
                        index = 0,
                        message = Message(role = "assistant", content = json),
                        finish_reason = "stop")))

    return Response.success(fakeResponse)
  }

  override suspend fun chatCompletionStream(
      request: ChatCompletionRequest
  ): Response<ResponseBody> {
    return Response.success(
        ResponseBody.create(
            "text/plain".toMediaTypeOrNull(), "streaming not supported in fake service"))
  }
}
