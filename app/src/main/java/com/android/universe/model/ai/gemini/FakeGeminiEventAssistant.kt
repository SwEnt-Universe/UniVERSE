package com.android.universe.model.ai.gemini

class FakeGeminiEventAssistant : GeminiEventAssistant(providedModel = null) {
  var shouldFail: Boolean = false
  var predefinedProposal: EventProposal? =
      EventProposal(title = "Fake Event Title", description = "Fake Event Description")

  override suspend fun generateProposal(userPrompt: String): EventProposal? {
    return if (shouldFail) {
      null
    } else {
      predefinedProposal
    }
  }
}
