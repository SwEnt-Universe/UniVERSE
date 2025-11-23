package com.android.universe.ui.utils

import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "ImageExtensions"

/**
 * Converts a [ByteArray] into a Jetpack Compose [ImageBitmap] asynchronously.
 *
 * This extension is primarily used to convert images stored as BLOBs in the database into a format
 * renderable by Compose UI.
 *
 * @return the decoded [ImageBitmap], or null if the ByteArray could not be decoded (e.g., corrupt
 *   data or unsupported format).
 */
suspend fun ByteArray.toImageBitmap(): ImageBitmap? {
  return withContext(Dispatchers.Default) {
    try {
      val bitmap = BitmapFactory.decodeByteArray(this@toImageBitmap, 0, this@toImageBitmap.size)
      bitmap?.asImageBitmap()
    } catch (e: Exception) {
      Log.e(TAG, "Failed to convert ByteArray to ImageBitmap", e)
      null
    }
  }
}
