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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
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

    // Instead of taking all tags, we shuffle them and take a random number (1 to 3)
    // This ensures the AI doesn't always create the same events.
    val randomTagCount = (3..8).random()
    val shuffledTags = userProfile.tags.shuffled().take(randomTagCount)
    val userInterests = shuffledTags.joinToString(", ") { it.displayName }

    // Randomly decide whether to include the bio or not (50% chance)
    // to prevent the bio from always dominating the context.
    val useBio = (0..1).random() == 1
    val bioContext = if (useBio) userProfile.description ?: "None" else "None"

    val availableTags = Tag.getAllTagsAsString()

    val now = LocalDateTime.now()
    val twoWeeksLater = now.plusWeeks(2)
    val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    val nowIso = now.format(formatter)
    val maxIso = twoWeeksLater.format(formatter)

    val prompt =
        """
                You are a creative event organizer.
                Task: Generate a completely new, unique event based on the user's profile and location.
                
                **Hierarchy of Rules (NON-NEGOTIABLE):**
                1. **LOCATION IS #1 PRIORITY**: The event MUST be within 5km of ($lat, $lon). 
                   - If a tag (e.g., "City Trip") requires traveling further, **IGNORE THE TAG**.
                   - Never hallucinate a location outside the 5km radius to satisfy a theme.
                2. **Coherence**: The activity must make sense for that specific location.
                
                **Directives for Coherence:**
                - You are provided with a list of "User Interests".
                - **Select ONE dominant theme** or a logical pair from the list.
                - **IGNORE** interests that do not fit the chosen theme OR the location.
                
                **Directives for Creativity:**
                - Do NOT always generate a "Meetup" or "Talk".
                - Think about activities: Workshops, Outdoor Games, Tasting sessions, Mini-tournaments, etc.
                - Be surprising.
                
                **Contextual & Logic Requirements:**
                1. **Seasonality**: Date is between "$nowIso" and "$maxIso".
                   - Winter: Avoid stationary outdoor activities. Favor cozy indoor spots/snow.
                   - Summer: Favor outdoor, sunny activities.
                2. **Geography**: Analyze terrain at ($lat, $lon).
                   - **Feasibility**: Do NOT propose natural features (Mountains, Lake) unless they exist there.
                   - **Context**: In a city? Urban activity. In mountains? Adventure.
           
                User Context:
                - Interest Pool: $userInterests
                - Bio: $bioContext                                                                                              
                - Current Location: $lat, $lon
                
                Strict Constraints:
                1. Title: Max ${InputLimits.TITLE_EVENT_MAX_LENGTH} chars.
                2. Description: Max ${InputLimits.DESCRIPTION - 66} chars.                                                                                                                          
                3. Location: 
                   - Latitude/Longitude **MUST** be within 5km of (${'$'}lat, ${'$'}lon).
                    - **IMPORTANT**: Choose a real, existing place. Use the specific name.
                4. Date: 
                   - Future ISO-8601 date between "$nowIso" and "$maxIso".
                5. Tags: Select **2 to 4** tags from the list below. The output strings must match the list exactly.
                 
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
