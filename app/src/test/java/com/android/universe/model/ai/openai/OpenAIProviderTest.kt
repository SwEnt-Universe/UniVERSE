package com.android.universe.model.ai.openai

import com.android.universe.BuildConfig
import kotlinx.coroutines.test.runTest
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.TimeUnit

/**
 * Does NOT hit network.
 *
 * Validates:
 *  - OkHttpClient interceptor configuration
 *  - Authorization + Content-Type header injection
 *  - Timeout configuration
 *
 */
class OpenAIProviderTest {

	@Test
	fun okHttpClient_hasAuthAndLoggingInterceptors() = runTest {
		val client = OpenAIProvider.testClient()

		// LoggingInterceptor must exist
		assertTrue(client.interceptors.any { it is LoggingInterceptor })

		// Two interceptors total: auth + logging
		assertEquals(2, client.interceptors.size)
	}

	@Test
	fun okHttpClient_addsAuthorizationAndContentTypeHeaders() = runTest {
		val client = OpenAIProvider.testClient()

		val originalRequest = Request.Builder()
			.url("https://example.com")
			.build()

		// ----- Fake Chain -----
		val chain = object : Interceptor.Chain {

			private var currentRequest = originalRequest

			override fun request(): Request = currentRequest

			override fun proceed(request: Request): Response {
				// Capture mutated request
				currentRequest = request

				return Response.Builder()
					.request(request)
					.protocol(Protocol.HTTP_1_1)
					.code(200)
					.message("OK")
					.body(ResponseBody.create("text/plain".toMediaTypeOrNull(), "ok"))
					.build()
			}

			// Required methods on OkHttp 4.x
			override fun connection(): Connection? = null
			override fun call(): Call = throw NotImplementedError()
			override fun connectTimeoutMillis(): Int = 0
			override fun readTimeoutMillis(): Int = 0
			override fun writeTimeoutMillis(): Int = 0

			override fun withConnectTimeout(timeout: Int, unit: TimeUnit) = this
			override fun withReadTimeout(timeout: Int, unit: TimeUnit) = this
			override fun withWriteTimeout(timeout: Int, unit: TimeUnit) = this
		}

		val authInterceptor = client.interceptors.first()

		val response = authInterceptor.intercept(chain)
		val modified = response.request

		assertEquals(
			"Bearer ${BuildConfig.OPENAI_API_KEY}",
			modified.header("Authorization")
		)

		assertEquals(
			"application/json",
			modified.header("Content-Type")
		)
	}

	@Test
	fun okHttpClient_hasCorrectTimeouts() = runTest {
		val client = OpenAIProvider.testClient()

		assertEquals(30_000, client.connectTimeoutMillis.toLong())
		assertEquals(60_000, client.readTimeoutMillis.toLong())
		assertEquals(60_000, client.writeTimeoutMillis.toLong())
	}
}
