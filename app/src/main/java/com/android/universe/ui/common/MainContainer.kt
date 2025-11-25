package com.android.universe.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import com.android.universe.ui.common.CommonBackground.BACKGROUNDTEXT
import com.android.universe.ui.common.CommonBackground.BLURVALUE
import com.android.universe.ui.utils.LocalLayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop

object CommonBackground {
  const val BACKGROUNDTEXT = "Background"
  val BLURVALUE = 8.dp
}

/**
 * A default container for the background image and content.
 *
 * @param bitmap The bitmap to use as the background image.
 * @param contentAlignment The alignment of the content within the container.
 * @param content The content to display inside the container.
 */
@Composable
fun UniverseBackgroundContainer(
    bitmap: ImageBitmap,
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable () -> Unit
) {
  Box(Modifier.fillMaxSize(), contentAlignment = contentAlignment) {
    UniverseBackground(bitmap)
    content()
  }
}

/**
 * A composable for the background image.
 *
 * @param bitmap The bitmap to use as the background image.
 * @param modifier The modifier to apply to the background image.
 */
@Composable
fun UniverseBackground(bitmap: ImageBitmap, modifier: Modifier = Modifier) {
  val backdrop = LocalLayerBackdrop.current

  Image(
      bitmap = bitmap,
      modifier = modifier.fillMaxSize().layerBackdrop(backdrop).blur(BLURVALUE),
      contentDescription = BACKGROUNDTEXT)
}
