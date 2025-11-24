package com.android.universe.model.ai

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody
import retrofit2.Response

class OpenAIServiceFake : OpenAIService {

  override suspend fun chatCompletion(
      request: ChatCompletionRequest
  ): Response<ChatCompletionResponse> {

    val json =
        """
        [
          {
            "id": "event-123",
            "title": "Fake Rock Concert",
            "description": "A generated test event",
            "date": "2025-03-21T20:00",
            "tags": ["Rock", "Music"],
            "creator": "ai-system",
            "participants": [],
            "location": { "latitude": 46.52, "longitude": 6.63 },
            "eventPicture": null
          }
        ]
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
        ResponseBody.create("text/plain".toMediaTypeOrNull(), "streaming not supported"))
  }
}
