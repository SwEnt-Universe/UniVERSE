package com.android.universe.model.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.android.universe.di.DefaultDP
import com.android.universe.di.DispatcherProvider
import com.android.universe.ui.theme.Dimensions
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException

/**
 * Manages the processing, resizing, and compression of image bitmaps.
 *
 * This utility class is responsible for taking image URIs, loading them efficiently using
 * sub-sampling to avoid OutOfMemory errors, and compressing them into byte arrays suitable for
 * network transmission or storage.
 *
 * @property context The application context used to access the content resolver for opening URI
 *   streams.
 * @property dispatcherProvider The provider for coroutine dispatchers, defaulting to [DefaultDP].
 *   Operations are performed on the IO dispatcher.
 */
class ImageBitmapManager(
    private val context: Context,
    private val dispatcherProvider: DispatcherProvider = DefaultDP
) {

  /**
   * Resizes and compresses an image from a given URI.
   *
   * This function performs the following steps on the dispatcherProvider.io thread:
   * 1. Decodes the image bounds to calculate the optimal sample size.
   * 2. Decodes the actual bitmap using the calculated sample size to reduce memory usage.
   * 3. Compresses the bitmap into a JPEG format with a quality of 45%.
   *
   * The target maximum size for width/height is determined by [Dimensions.ProfilePictureSize].
   *
   * @param uri The URI of the image to process.
   * @return A [ByteArray] containing the compressed JPEG data, or `null` if decoding fails or an
   *   error occurs.
   */
  suspend fun resizeAndCompressImage(
      uri: Uri,
      maxSize: Int = Dimensions.ProfilePictureSize
  ): ByteArray? =
      withContext(dispatcherProvider.io) {
        try {
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
        } catch (e: IOException) {
          Log.e("ImageBitmapManager", "IO Error processing image: $uri", e)
          null
        } catch (e: SecurityException) {
          Log.e("ImageBitmapManager", "Permission denied: $uri", e)
          null
        } catch (e: IllegalArgumentException) {
          Log.e("ImageBitmapManager", "Invalid arguments", e)
          null
        }
      }

  /**
   * Calculates the largest "inSampleSize" value that is a power of 2 and keeps both height and
   * width larger than the requested [maxSize].
   *
   * @param options The [BitmapFactory.Options] containing the raw width and height of the image.
   * @param maxSize The maximum desired width or height of the image.
   * @return The calculated sample size (e.g., 1, 2, 4, 8).
   */
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
