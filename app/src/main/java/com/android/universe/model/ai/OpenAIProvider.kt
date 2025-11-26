package com.android.universe.model.ai

import com.android.universe.BuildConfig
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
 * - Expose a lazily initialized [AIEventGen] implementation (dependency injection style singleton)
 *
 * Intended to be the entry point for accessing OpenAI functionality inside the app.
 */
object OpenAIProvider {

  private const val BASE_URL = "https://api.openai.com/v1/"

  // Single shared OkHttpClient – lazily initialized, thread-safe
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

  private val retrofit: Retrofit by lazy {
    Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(
            Json {
                  ignoreUnknownKeys = true // don't crash if OpenAI adds new fields
                  coerceInputValues = true // safely handle null → default values
                  encodeDefaults = true // include default values if you want
                }
                .asConverterFactory("application/json".toMediaType()))
        .build()
  }

  // Public access point
  val api: OpenAIService by lazy { retrofit.create(OpenAIService::class.java) }

  val eventGen: AIEventGen by lazy { OpenAIEventGen(api) }
}
