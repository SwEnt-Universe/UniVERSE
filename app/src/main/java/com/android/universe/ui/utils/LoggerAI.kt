package com.android.universe.ui.utils

import android.util.Log

object LoggerAI {
  private const val TAG = "AIFlow"

  fun d(msg: String) = Log.d(TAG, msg)

  fun i(msg: String) = Log.i(TAG, msg)

  fun w(msg: String) = Log.w(TAG, msg)

  fun e(msg: String, t: Throwable? = null) = Log.e(TAG, msg, t)
}
