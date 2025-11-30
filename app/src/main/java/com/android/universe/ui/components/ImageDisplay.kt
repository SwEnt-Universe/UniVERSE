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
import com.android.universe.ui.utils.toImageBitmap
import kotlinx.coroutines.Dispatchers

@Composable
fun ImageDisplay(
    image: ByteArray?,
    @DrawableRes defaultImageId: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
  val imageBitmap by
      produceState<ImageBitmap?>(initialValue = null, key1 = image) {
        value = image?.toImageBitmap(Dispatchers.Default)
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
