package com.android.universe.model.ai.gemini

import com.android.universe.model.location.Location
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.type.GenerateContentResponse
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import java.io.IOException
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.test.runTest
import org.junit.Test

private const val VALID_JSON_RESPONSE =
    """
{
    "title": "Sunset Yoga",
    "description": "Relaxing yoga session in the park."
}
"""

private const val MARKDOWN_JSON_RESPONSE =
    """
```json
{
    "title": "Cleaned Title",
    "description": "It works regardless of markdown!"
}
"""

private const val MALFORMED_JSON_RESPONSE = "{ invalid json content... "

private val LAUSANNE = Location(46.5196535, 6.6322734)

class GeminiEventAssistantTest {

  @Test
  fun generateProposal_parsesValidJsonIntoProposalObject() = runTest {
    val mockModel = mockk<GenerativeModel>()
    val mockResponse = mockk<GenerateContentResponse>()

    every { mockResponse.text } returns VALID_JSON_RESPONSE
    coEvery { mockModel.generateContent(any<String>()) } returns mockResponse

    val assistant = GeminiEventAssistant(providedModel = mockModel)

    val result = assistant.generateProposal("Yoga session", LAUSANNE)

    assertNotNull("Result should not be null for valid JSON", result)
    assertEquals("Sunset Yoga", result?.title)
    assertEquals("Relaxing yoga session in the park.", result?.description)
  }

  @Test
  fun generateProposal_cleansMarkdownCodeBlocksBeforeParsing() = runTest {
    val mockModel = mockk<GenerativeModel>()
    val mockResponse = mockk<GenerateContentResponse>()

    every { mockResponse.text } returns MARKDOWN_JSON_RESPONSE
    coEvery { mockModel.generateContent(any<String>()) } returns mockResponse

    val assistant = GeminiEventAssistant(providedModel = mockModel)

    val result = assistant.generateProposal("Markdown test", LAUSANNE)

    assertNotNull(result)
    assertEquals("Cleaned Title", result?.title)
    assertEquals("It works regardless of markdown!", result?.description)
  }

  @Test
  fun generateProposal_returnsNull_onNetworkException() = runTest {
    val mockModel = mockk<GenerativeModel>()
    coEvery { mockModel.generateContent(any<String>()) } throws IOException("No Internet")

    val assistant = GeminiEventAssistant(providedModel = mockModel)
    val result = assistant.generateProposal("Crash test", LAUSANNE)

    assertNull("Should return null when API throws exception", result)
  }

  @Test
  fun generateProposal_returnsNull_onParsingFailure() = runTest {
    val mockModel = mockk<GenerativeModel>()
    val mockResponse = mockk<GenerateContentResponse>()

    every { mockResponse.text } returns MALFORMED_JSON_RESPONSE
    coEvery { mockModel.generateContent(any<String>()) } returns mockResponse

    val assistant = GeminiEventAssistant(providedModel = mockModel)

    val result = assistant.generateProposal("Bad JSON test", LAUSANNE)

    assertNull("Should return null when JSON is invalid", result)
  }

  @Test
  fun generateProposal_returnsNull_whenResponseTextIsNull() = runTest {
    val mockModel = mockk<GenerativeModel>()
    val mockResponse = mockk<GenerateContentResponse>()

    every { mockResponse.text } returns null
    coEvery { mockModel.generateContent(any<String>()) } returns mockResponse

    val assistant = GeminiEventAssistant(providedModel = mockModel)

    val result = assistant.generateProposal("Empty test", LAUSANNE)

    assertNull(result)
  }
}
