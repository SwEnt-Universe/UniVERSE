package com.android.universe.model.ai

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import kotlin.jvm.java

object OpenAIProvider {

	private const val BASE_URL = "https://api.openai.com/v1/"

	private val okHttpClient: OkHttpClient by lazy {
		OkHttpClient.Builder()
			.addInterceptor { chain ->
				chain.request().newBuilder()
					.addHeader("Authorization", "Bearer ${BuildConfig.OPENAI_API_KEY}")
					.addHeader("Content-Type", "application/json")
					.build()
					.let(chain::proceed)
			}
			.addInterceptor(
				HttpLoggingInterceptor().apply {
					level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
					else HttpLoggingInterceptor.Level.NONE
				}
			)
			.connectTimeout(30, TimeUnit.SECONDS)
			.readTimeout(60, TimeUnit.SECONDS)
			.writeTimeout(60, TimeUnit.SECONDS)
			.build()
	}

	private val retrofit: Retrofit by lazy {
		Retrofit.Builder()
			.baseUrl(BASE_URL)
			.client(okHttpClient)
			.addConverterFactory(GsonConverterFactory.create())
			.build()
	}

	val service: OpenAIService by lazy {
		retrofit.create(OpenAIService::class.java)
	}

	val curator: AICurator by lazy {
		AICuratorOpenAI(service)
	}
}