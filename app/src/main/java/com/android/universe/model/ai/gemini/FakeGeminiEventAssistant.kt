package com.android.universe.model.ai.gemini

import com.android.universe.model.location.Location
import com.android.universe.model.user.UserProfile

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
   * The event data object to be returned when [generateCreativeEvent] succeeds. Defaults to a valid
   * event structure.
   */
  var predefinedEventData: GeneratedEventData? =
      GeneratedEventData(
          title = "Fake Creative Event",
          description = "This is a fake event generated for testing purposes.",
          latitude = 46.5196535,
          longitude = 6.6322734,
          dateIso = "2025-12-25T18:00:00",
          tags = listOf("TECHNOLOGY", "RUNNING", "PROGRAMMING"))

  /**
   * Simulates the generation of a full creative event.
   *
   * @param userProfile The user profile (ignored in this fake implementation).
   * @param location The location (ignored in this fake implementation).
   * @return [predefinedEventData] if [shouldFail] is false, otherwise `null`.
   */
  override suspend fun generateCreativeEvent(
      userProfile: UserProfile,
      location: Pair<Double, Double>
  ): GeneratedEventData? {
    return if (shouldFail) {
      null
    } else {
      predefinedEventData
    }
  }

  /**
   * Simulates the generation of an event proposal.
   *
   * @param userPrompt The user input (ignored in this fake implementation).
   * @return [predefinedProposal] if [shouldFail] is false, otherwise `null`.
   */
  override suspend fun generateProposal(userPrompt: String, geoPoint: Location): EventProposal? {
    return if (shouldFail) {
      null
    } else {
      predefinedProposal
    }
  }
}
