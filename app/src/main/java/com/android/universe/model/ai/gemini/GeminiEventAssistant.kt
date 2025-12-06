package com.android.universe.model.ai.gemini

import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.generationConfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * A helper class responsible for generating creative event proposals using the Gemini AI model via
 * Firebase.
 *
 * This class handles the communication with the generative AI backend, constructs the prompt with
 * specific constraints (length, JSON format), and parses the response into a structured
 * [EventProposal].
 *
 * @param providedModel An optional [GenerativeModel] instance. This is primarily used for
 *   dependency injection during testing to avoid actual network calls to Firebase. If null, a
 *   default Firebase-backed model is lazily initialized.
 */
open class GeminiEventAssistant(private val providedModel: GenerativeModel? = null) {

  /**
   * The generative model instance used to interact with the AI.
   *
   * Initialized lazily to ensure Firebase resources are only accessed when actually needed,
   * preventing initialization crashes in unit test environments where the Firebase SDK is not
   * mocked.
   */
  private val model: GenerativeModel by lazy {
    providedModel
        ?: Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel(
                modelName = "gemini-2.5-flash",
                generationConfig =
                    generationConfig {
                      responseMimeType = "application/json"
                      temperature = 0.7f
                    })
  }

  /**
   * A configured JSON instance for parsing AI responses.
   *
   * Configured to be lenient and ignore unknown keys to handle potential inconsistencies in AI
   * output.
   */
  private val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    encodeDefaults = true
  }

  /**
   * Generates an [EventProposal] based on the provided user prompt.
   *
   * This method sends a structured prompt to Gemini, asking for a catchy title and a short
   * description. It enforces strict character limits (Title: 40, Description: 100) and expects a
   * JSON response.
   *
   * @param userPrompt The raw input from the user describing the event they want to create.
   * @return An [EventProposal] containing the generated title and description, or `null` if
   *   generation fails, the network request errors, or parsing fails.
   */
  open suspend fun generateProposal(userPrompt: String): EventProposal? {
    val prompt =
        """
            You are a creative event organizer.
            Task: Generate a catchy title and a short description for an event based on the user's input.
            
            User Input: "$userPrompt"
            
            Strict Constraints:
            1. Title must be maximum 40 characters.
            2. Description must be maximum 100 characters.
            3. Output must be valid JSON
            
            Output Schema:
            {
                "title": "string",
                "description": "string"
            }
        """
            .trimIndent()

    return try {
      val response = model.generateContent(prompt)
      val text = response.text ?: return null
      cleanAndParse(text)
    } catch (e: Exception) {
      e.printStackTrace()
      null
    }
  }

  /**
   * Cleans and parses the raw text response from the AI.
   *
   * AI models often wrap JSON output in Markdown code blocks (e.g., ```json ... ```). This method
   * strips those markers before attempting to deserialize the string.
   *
   * @param rawJson The raw string response from the AI model.
   * @return The parsed [EventProposal] object, or `null` if parsing fails.
   */
  private fun cleanAndParse(rawJson: String): EventProposal? {
    return try {
      val cleanJson = rawJson.replace("```json", "").replace("```", "").trim()
      json.decodeFromString<EventProposal>(cleanJson)
    } catch (e: Exception) {
      e.printStackTrace()
      null
    }
  }
}

/**
 * A serializable data class representing the structured output of an AI-generated event proposal.
 *
 * @property title The generated event title (max 40 chars).
 * @property description The generated event description (max 100 chars).
 */
@Serializable data class EventProposal(val title: String, val description: String)
