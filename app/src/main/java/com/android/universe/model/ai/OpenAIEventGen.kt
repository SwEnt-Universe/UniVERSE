package com.android.universe.model.ai

import com.android.universe.model.ai.prompt.ContextConfig
import com.android.universe.model.ai.prompt.PromptBuilder
import com.android.universe.model.ai.prompt.TaskConfig
import com.android.universe.model.event.Event
import com.android.universe.model.user.UserProfile

class OpenAIEventGen(private val service: OpenAIService) : EventGen {

  override suspend fun generateEventsForUser(
      profile: UserProfile,
      task: TaskConfig,
      context: ContextConfig
  ): List<Event> {

    // 1: build the prompt with task + context
    val SYSTEM_MESSAGE =
        "You are UniVERSE EventCuratorGPT. Always output ONLY valid JSON arrays of event objects."

    val prompt = PromptBuilder.build(profile = profile, task = task, context = context)

    // 2: create request
    val request =
        ChatCompletionRequest(
            model = "gpt-5-nano",
            messages =
                listOf(
                    Message(role = "system", content = SYSTEM_MESSAGE),
                    Message(role = "user", content = prompt)),
            max_tokens = 800,
            temperature = 0.9)

    // 3: OpenAI API call
    val response = service.chatCompletion(request)
    if (!response.isSuccessful) {
      throw IllegalStateException("OpenAI error: ${response.code()} ${response.message()}")
    }

    val json =
        response.body()?.choices?.first()?.message?.content
            ?: throw IllegalStateException("OpenAI returned empty response")

    // 4: parse JSON to Event objects
    return ResponseParser.parseEvents(json)
  }
}
