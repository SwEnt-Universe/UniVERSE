package com.android.universe.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.android.universe.di.DefaultDP
import com.android.universe.ui.theme.CardShape
import com.android.universe.ui.theme.Dimensions.Padding2XL
import com.android.universe.ui.utils.toImageBitmap
import kotlinx.coroutines.CoroutineDispatcher

/**
 * A Composable component that displays an image from a raw [ByteArray].
 *
 * This component handles the asynchronous conversion of the byte array to an [ImageBitmap]. If the
 * image data is null or loading, it displays a standard placeholder icon centered within the
 * layout, matching the style of [LiquidImagePicker].
 *
 * @param image The raw image data as a [ByteArray].
 * @param contentDescription Text used by accessibility services.
 * @param modifier The [Modifier] to be applied to the container.
 * @param contentScale Scaling parameter for the image. Defaults to [ContentScale.Crop].
 * @param dispatcher The [CoroutineDispatcher] used for bitmap conversion. Defaults to
 *   [DefaultDP.default].
 */
@Composable
fun ImageDisplay(
    image: ByteArray?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    dispatcher: CoroutineDispatcher = DefaultDP.default
) {
  val imageBitmap by
      produceState<ImageBitmap?>(initialValue = null, key1 = image) {
        value = image?.toImageBitmap(dispatcher)
      }

  val targetBitmap = imageBitmap

  LiquidBox(modifier = modifier, enableLens = false, shape = CardShape,contentAlignment = Alignment.Center) {
    if (targetBitmap != null) {
      Image(
          bitmap = targetBitmap,
          contentDescription = contentDescription,
          contentScale = contentScale,
          modifier = Modifier.fillMaxSize())
    } else {
      Icon(
          imageVector = Icons.Outlined.Image,
          contentDescription = contentDescription,
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(Padding2XL))
    }
  }
}
