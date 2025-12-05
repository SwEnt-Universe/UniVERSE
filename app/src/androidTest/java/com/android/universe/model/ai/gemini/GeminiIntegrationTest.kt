package com.android.universe.model.ai.gemini

import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.test.runTest
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GeminiIntegrationTest {

  @Ignore("Manual integration test: Costs quota and requires a valid environment.")
  @Test
  fun manual_integration_check_real_api_call() = runTest {
    // This uses the REAL Firebase instance, which automatically
    // reads the API Key from your google-services.json on the emulator.
    val assistant = GeminiEventAssistant()

    val result = assistant.generateProposal("A competitive chess tournament in the park")

    println("Real AI Output: $result")

    assertNotNull(result)
    assertEquals(true, result?.title?.length!! <= 40)
    assertEquals(true, result.description.length <= 100)
  }
}
