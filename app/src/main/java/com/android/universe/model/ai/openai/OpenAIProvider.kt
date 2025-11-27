package com.android.universe.model.ai.openai

import com.android.universe.BuildConfig
import com.android.universe.model.ai.AIEventGen
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

/**
 * Provides configured networking components for interacting with the OpenAI API.
 *
 * Includes:
 * - Auth interceptor
 * - Debug-only HTTP logging interceptor
 * - Retrofit with Kotlinx Serialization
 */
object OpenAIProvider {

  private const val BASE_URL = "https://api.openai.com/v1/"

  private val okHttpClient: OkHttpClient by lazy {

    // --- Debug-only logger ---
    val logging =
        HttpLoggingInterceptor().apply {
          level =
              if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.HEADERS
              } else {
                HttpLoggingInterceptor.Level.NONE // disabled in release
              }
        }

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
        // Logging interceptor (debug only)
        .addInterceptor(logging)
        // Timeouts
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()
  }

  // Shared JSON config
  private val json: Json = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
    encodeDefaults = true
  }

  // Retrofit instance
  private val retrofit: Retrofit by lazy {
    Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
  }

  // Networking API
  private val api: OpenAIService by lazy { retrofit.create(OpenAIService::class.java) }

  // Domain wrapper
  val eventGen: AIEventGen by lazy { OpenAIEventGen(api) }

  // Test accessor
  internal fun testClient() = okHttpClient
}
