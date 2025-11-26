package com.android.universe.ui.common

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import com.android.universe.ui.common.CommonBackground.BLURVALUE
import com.android.universe.ui.utils.LocalLayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import kotlinx.coroutines.delay

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
 * @param newBitmap The new bitmap to use as the background image.
 */
@Composable
fun UniverseBackground(newBitmap: ImageBitmap) {
  val backdrop = LocalLayerBackdrop.current

  // Duration must match the Crossfade tween
  val duration = 4000 // ms (adjust to taste)

  // The bitmap currently used as the blur source
  var activeBackdrop by remember { mutableStateOf(newBitmap) }

  // Triggered whenever the repository updates the bitmap
  // Drives the Crossfade (foreground animation)
  var fadingBitmap by remember { mutableStateOf(newBitmap) }

  // When the repo updates the snapshot:
  LaunchedEffect(newBitmap) {
    fadingBitmap = newBitmap // Start the crossfade animation
  }

  Box(Modifier.fillMaxSize()) {

    // --------------------------
    // 1) STATIC BACKDROP LAYER
    // --------------------------
    // This layer is underneath everything and blur is applied here.
    Image(
        bitmap = activeBackdrop,
        contentDescription = "Background",
        modifier =
            Modifier.fillMaxSize()
                .layerBackdrop(backdrop) // blur is applied here, using activeBackdrop
                .blur(BLURVALUE))

    // --------------------------
    // 2) FOREGROUND CROSSFADE LAYER
    // --------------------------
    // This layer fades smoothly between old and new bitmaps,
    Crossfade(
        targetState = fadingBitmap,
        animationSpec = tween(durationMillis = duration, easing = EaseInOutCubic),
        modifier = Modifier.fillMaxSize()) { image ->
          Image(
              bitmap = image,
              contentDescription = "Background Fade Layer",
              modifier = Modifier.fillMaxSize().blur(CommonBackground.BLURVALUE))
        }
  }

  // --------------------------
  // 3) AFTER ANIMATION COMPLETES
  // --------------------------
  // Switch the backdrop’s blur source to match the new image.
  LaunchedEffect(fadingBitmap) {
    delay((duration / 2).toLong())
    activeBackdrop = fadingBitmap // blur layer
  }
}
