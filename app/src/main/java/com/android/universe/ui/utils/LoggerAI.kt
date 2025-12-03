package com.android.universe.ui.utils

import android.util.Log

/**
 * Centralized logger used for AI-related flows within the application.
 *
 * This utility wraps Android's [Log] API and standardizes all AI logging under a single log tag
 * (`"AIFlow"`).
 *
 * ### Usage
 *
 * ```
 * LoggerAI.d("AI request started")
 * LoggerAI.i("Prompt built successfully")
 * LoggerAI.w("AI response contained fallback content")
 * LoggerAI.e("Failed to parse AI output", exception)
 * ```
 *
 * Each log method delegates directly to the corresponding method on [Log]:
 * - [Log.d] for debug-level details
 * - [Log.i] for informational messages
 * - [Log.w] for warnings
 * - [Log.e] for errors (with optional throwable)
 */
object LoggerAI {
  private const val TAG = "AIFlow"

  fun d(msg: String) = Log.d(TAG, msg)

  fun i(msg: String) = Log.i(TAG, msg)

  fun w(msg: String) = Log.w(TAG, msg)

  fun e(msg: String, t: Throwable? = null) = Log.e(TAG, msg, t)
}
