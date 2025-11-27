package com.android.universe.model.ai.openai

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.internal.http.promisesBody

/**
 * Lightweight, test-friendly logging interceptor.
 * - Shows method, URL, headers you want, duration, status code.
 * - Never prints Authorization headers.
 * - Body logging optional.
 */
class LoggingInterceptor(private val logBody: Boolean = false) : Interceptor {

  override fun intercept(chain: Interceptor.Chain): Response {
    val request = chain.request()

    val startNs = System.nanoTime()
    println("→ ${request.method} ${request.url}")

    val response = chain.proceed(request)

    val tookMs = (System.nanoTime() - startNs) / 1e6

    println("← ${response.code} ${response.message} (${tookMs.toInt()}ms)")

    if (logBody && response.body != null && response.promisesBody()) {
      val bodyStr = response.peekBody(Long.MAX_VALUE).string()
      println("Response body:\n$bodyStr")
    }

    return response
  }
}