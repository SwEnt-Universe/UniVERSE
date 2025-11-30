package com.android.universe.model.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.android.universe.di.DefaultDP
import com.android.universe.di.DispatcherProvider
import com.android.universe.ui.theme.Dimensions
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.withContext

class ImageBitmapManager(
    private val context: Context,
    private val dispatcherProvider: DispatcherProvider = DefaultDP
) {

  suspend fun resizeAndCompressImage(uri: Uri): ByteArray? =
      withContext(dispatcherProvider.io) {
        try {
          val maxSize = Dimensions.ProfilePictureSize

          val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }

          context.contentResolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input, null, options)
          }

          options.inSampleSize = calculateInSampleSize(options, maxSize)
          options.inJustDecodeBounds = false

          val bitmap =
              context.contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input, null, options)
              }

          if (bitmap == null) {
            Log.e("ImageBitmapManager", "Failed to decode bitmap from URI $uri")
            return@withContext null
          }

          val stream = ByteArrayOutputStream()
          bitmap.compress(Bitmap.CompressFormat.JPEG, 45, stream)
          stream.toByteArray()
        } catch (e: Exception) {
          Log.e("ImageBitmapManager", "Error processing image", e)
          null
        }
      }

  private fun calculateInSampleSize(options: BitmapFactory.Options, maxSize: Int): Int {
    val (height: Int, width: Int) = options.run { outHeight to outWidth }
    var inSampleSize = 1

    if (height > maxSize || width > maxSize) {
      val halfHeight = height / 2
      val halfWidth = width / 2

      while ((halfHeight / inSampleSize) >= maxSize && (halfWidth / inSampleSize) >= maxSize) {
        inSampleSize *= 2
      }
    }
    return inSampleSize
  }
}
