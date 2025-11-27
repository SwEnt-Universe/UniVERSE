package com.android.universe.model.ai.openai

import com.android.universe.model.ai.AIEventGen
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Converter
import retrofit2.Retrofit

class OpenAIProviderTest {

  /** Helper that reads Kotlin lazy fields whose real name is: <name>$delegate */
  private fun <T> getLazyValue(instance: Any, fieldName: String): T {
    val clazz = instance::class.java
    val field = clazz.getDeclaredField("${fieldName}\$delegate")
    field.isAccessible = true
    @Suppress("UNCHECKED_CAST") val lazyDelegate = field.get(instance) as Lazy<T>
    return lazyDelegate.value
  }

  @Test
  fun retrofit_and_api_are_initialized_and_use_same_client() = runTest {
    // Trigger initialization
    val eventGen: AIEventGen = OpenAIProvider.eventGen
    assertTrue(eventGen is OpenAIEventGen)

    // Extract retrofit via lazy delegate
    val retrofit: Retrofit = getLazyValue(OpenAIProvider, "retrofit")

    assertNotNull(retrofit)

    // Retrofit must have converter factories
    assertTrue(retrofit.converterFactories().any { it is Converter.Factory })

    // Should use the same OkHttpClient
    val client = retrofit.callFactory()
    val providerClient = OpenAIProvider.testClient()

    assertSame(providerClient, client)
  }

  @Test
  fun eventGen_is_singleton() = runTest {
    val g1 = OpenAIProvider.eventGen
    val g2 = OpenAIProvider.eventGen
    assertSame(g1, g2)
  }
}
