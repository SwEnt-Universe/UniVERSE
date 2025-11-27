package com.android.universe.model.ai.openai

import com.android.universe.BuildConfig
import com.android.universe.model.ai.AIEventGen
import com.android.universe.model.ai.openai.LoggingInterceptor
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import kotlin.jvm.java
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit

/**
 * Provides configured networking components for interacting with the OpenAI API.
 *
 * Responsibilities:
 * - Create and configure a shared OkHttpClient with authentication and logging
 * - Build a Retrofit instance with JSON serialization support
 * - Expose a lazily initialized [OpenAIService] Retrofit API interface
 * - Expose a lazily initialized [com.android.universe.model.ai.AIEventGen] implementation
 *
 * Intended to be the entry point for accessing OpenAI functionality inside the app.
 */
object OpenAIProvider {

  private const val BASE_URL = "https://api.openai.com/v1/"

  private val okHttpClient: OkHttpClient by lazy {
    OkHttpClient.Builder()
        // Auth interceptor
        .addInterceptor { chain ->
          val request =
              chain
                  .request()
                  .newBuilder()
                  .addHeader("Authorization", "Bearer ${BuildConfig.OPENAI_API_KEY}")
                  .addHeader("Content-Type", "application/json")
                  .build()
          chain.proceed(request)
        }
        // Logging
        .addInterceptor(LoggingInterceptor(logBody = BuildConfig.DEBUG))
        // Timeouts tuned for OpenAI (they can be slow with gpt-4o, o1, etc.)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()
  }

  /**
   * Defines a reusable JSON configuration for all OpenAI serialization.
   *
   * Ensures consistent decoding behavior across the app and avoids repeatedly creating
   * `Json` instances, which are relatively costly to allocate.
   *
   * - `ignoreUnknownKeys` prevents crashes if OpenAI adds new response fields
   * - `coerceInputValues` tolerates minor type mismatches (e.g., "46.5" → 46.5)
   * - `encodeDefaults` includes default values when encoding outbound JSON
   */
  private val json: Json = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
    encodeDefaults = true
  }

  /**
   * Defines the main Retrofit instance used for all OpenAI HTTP operations.
   *
   * Uses:
   * - the shared `okHttpClient` for authentication, logging, and timeouts
   * - the shared `json` instance for consistent serialization behavior
   *
   * Declared `lazy` to delay construction until first use, reducing startup cost
   * and guaranteeing a single, thread-safe instance.
   */
  private val retrofit: Retrofit by lazy {
    Retrofit.Builder()
      .baseUrl(BASE_URL)
      .client(okHttpClient)
      .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
      .build()
  }

  /**
   * OpenAI API access point, interacts with OpenAI at the networking layer.
   */
  private val api: OpenAIService by lazy { retrofit.create(OpenAIService::class.java) }


  /**
   * Entry point for anything in the app that wants AI-generated events.
   *
   * This wraps the OpenAI API with application-specific logic, building structured prompts and
   * applying the JSON schema
   */
  val eventGen: AIEventGen by lazy { OpenAIEventGen(api) }

  /**
   * Internal accessor for the underlying [OkHttpClient].
   *
   * This exists exclusively for unit testing purposes
   * to allow assertions on:
   *  - auth headers
   *  - interceptors
   *  - timeouts
   *  - request/response behavior
   *
   * Should never be used in production code.
   */
  internal fun testClient() = okHttpClient
}