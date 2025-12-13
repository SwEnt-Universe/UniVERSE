package com.android.universe.model.ai.gemini

import com.android.universe.model.location.Location
import com.android.universe.model.tag.Tag
import com.android.universe.model.user.UserProfile
import com.android.universe.ui.common.InputLimits
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
                modelName = "gemini-flash-lite-latest",
                generationConfig =
                    generationConfig {
                      responseMimeType = "application/json"
                      temperature = 1f
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
   * Generates a full creative event based on the user's profile and location.
   *
   * @param userProfile The full user profile containing interests and bio.
   * @param location A pair of (Latitude, Longitude) representing the user's current center.
   * @return A [GeneratedEventData] object containing full event details, or null if generation
   *   fails.
   */
  open suspend fun generateCreativeEvent(
      userProfile: UserProfile,
      location: Pair<Double, Double>
  ): GeneratedEventData? {
    val (lat, lon) = location
    val userInterests = userProfile.tags.joinToString(", ") { it.displayName }
    val availableTags = Tag.getAllTagsAsString()

    val prompt =
        """
                You are a creative event organizer.
                Task: Generate a completely new, unique event based on the user's profile and location.
                
                User Context:
                - Profile: $userInterests
                - Bio: ${userProfile.description ?: "None"}
                - Current Location: $lat, $lon
                
                Strict Constraints:
                1. Title: Max ${InputLimits.TITLE_EVENT_MAX_LENGTH} chars.
                2. Description: Max ${InputLimits.DESCRIPTION - 66} chars.
                3. Location: Latitude/Longitude must be within 5km of the user's current location ($lat, $lon).
                4. Date: Must be a future date in ISO-8601 format (e.g., "2025-12-25T18:00:00").
                5. Tags: Select **at least 2** tags from the list below. The output strings must match the list exactly.
                 
                Available Tags List:
                [$availableTags]
               
                Output Schema (Strict JSON):
                {
                  "title": "String",
                  "description": "String",
                  "latitude": Double,
                  "longitude": Double,
                  "dateIso": "String",
                  "tags": ["String"]
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
   * Generates an [EventProposal] based on the provided user prompt.
   *
   * This method sends a structured prompt to Gemini, asking for a catchy title and a short
   * description. It enforces strict character limits (Title: 40, Description: 100) and expects a
   * JSON response.
   *
   * @param userPrompt The raw input from the user describing the event they want to create.
   * @param geoPoint The location of the event.
   * @return An [EventProposal] containing the generated title and description, or `null` if
   *   generation fails, the network request errors, or parsing fails.
   */
  open suspend fun generateProposal(userPrompt: String, geoPoint: Location): EventProposal? {
    val prompt =
        """
            You are a creative event organizer.
            Task: Generate a catchy title and a short description for an event based on the user's input.
            
            Those are geo location of the event: ${geoPoint.latitude}, ${geoPoint.longitude}.
            You need to find the closest places, preferably inside the University campuses.
            Especially if they are association of the campus.
            
            User Input: "$userPrompt"
            
            Strict Constraints:
            1. Title must be of ${InputLimits.TITLE_EVENT_MAX_LENGTH} characters **maximum**.
            2. Description must be of ${InputLimits.DESCRIPTION - 66} characters **maximum**.
            3. Output must be valid JSON
            
            Advices:
            1. If the user points out a destination you need to put it **at least** in the title. 
            2. If the user speaks about relatives like friends, family, you need to sounds less professional and more personal it's the user request after all.
            3. You are creating events for college students, so don't be too professional.
            4. Avoid usual formulation, but stay grounded you don't want to scare them off.
            
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
  private inline fun <reified T> cleanAndParse(rawJson: String): T? {
    return try {
      val cleanJson = rawJson.replace("```json", "").replace("```", "").trim()
      json.decodeFromString<T>(cleanJson)
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

/**
 * A serializable data class representing the structured output of an AI-generated event.
 *
 * @property title The generated event title (max 40 chars).
 * @property description The generated event description (max 100 chars).
 * @property latitude The latitude of the generated event location.
 * @property longitude The longitude of the generated event location.
 * @property dateIso The ISO-8601 formatted date of the generated event.
 * @property tags The list of tags associated with the generated event.
 */
@Serializable
data class GeneratedEventData(
    val title: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val dateIso: String,
    val tags: List<String>
)
