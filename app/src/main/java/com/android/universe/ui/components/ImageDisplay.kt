package com.android.universe.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.android.universe.di.DefaultDP
import com.android.universe.ui.utils.toImageBitmap
import kotlinx.coroutines.CoroutineDispatcher

/**
 * A Composable component that displays an image from a raw [ByteArray].
 *
 * This component handles the asynchronous conversion of the byte array to an [ImageBitmap] using
 * [produceState] to avoid blocking the UI thread. While the image is being processed, or if the
 * provided [image] data is null, the component displays a default fallback image resource.
 *
 * @param image The raw image data as a [ByteArray]. If null, the [defaultImageId] will be shown
 *   immediately.
 * @param defaultImageId The drawable resource ID to display as a placeholder or fallback if [image]
 *   is null or loading.
 * @param contentDescription Text used by accessibility services to describe what this image
 *   represents.
 * @param modifier The [Modifier] to be applied to the container [Box].
 * @param contentScale Optional scaling parameter to determine how the image fits within the bounds.
 *   Defaults to [ContentScale.Crop].
 * @param dispatcher The [CoroutineDispatcher] used for converting the byte array to a bitmap
 *   asynchronously. Defaults to [DefaultDP.default].
 */
@Composable
fun ImageDisplay(
    image: ByteArray?,
    @DrawableRes defaultImageId: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    dispatcher: CoroutineDispatcher = DefaultDP.default
) {
  val imageBitmap by
      produceState<ImageBitmap?>(initialValue = null, key1 = image) {
        value = image?.toImageBitmap(dispatcher)
      }

  Box(modifier = modifier, contentAlignment = Alignment.Center) {
    if (imageBitmap != null) {
      Image(
          bitmap = imageBitmap!!,
          contentDescription = contentDescription,
          contentScale = contentScale,
          modifier = Modifier.fillMaxSize())
    } else {
      Image(
          painter = painterResource(id = defaultImageId),
          contentDescription = contentDescription,
          contentScale = contentScale,
          modifier = Modifier.fillMaxSize())
    }
  }
}
