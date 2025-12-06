package com.android.universe.model.ai.gemini

/**
 * A fake implementation of [GeminiEventAssistant] designed for unit testing and UI previews.
 *
 * This class bypasses the actual Firebase AI calls and returns pre-configured data or errors. It
 * allows testing of the ViewModel and UI states (Loading, Success, Error) without network
 * dependencies.
 */
class FakeGeminiEventAssistant : GeminiEventAssistant(providedModel = null) {

  /**
   * Controls the outcome of the [generateProposal] method. If set to `true`, the method will return
   * `null` to simulate a generation failure.
   */
  var shouldFail: Boolean = false

  /**
   * The proposal object to be returned when [generateProposal] succeeds. Defaults to a placeholder
   * title and description.
   */
  var predefinedProposal: EventProposal? =
      EventProposal(title = "Fake Event Title", description = "Fake Event Description")

  /**
   * Simulates the generation of an event proposal.
   *
   * @param userPrompt The user input (ignored in this fake implementation).
   * @return [predefinedProposal] if [shouldFail] is false, otherwise `null`.
   */
  override suspend fun generateProposal(userPrompt: String): EventProposal? {
    return if (shouldFail) {
      null
    } else {
      predefinedProposal
    }
  }
}
