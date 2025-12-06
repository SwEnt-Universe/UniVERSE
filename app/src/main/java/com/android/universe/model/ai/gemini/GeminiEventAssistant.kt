package com.android.universe.model.ai.gemini

import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.generationConfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

open class GeminiEventAssistant(private val providedModel: GenerativeModel? = null) {

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

  private val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    encodeDefaults = true
  }

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

@Serializable data class EventProposal(val title: String, val description: String)
