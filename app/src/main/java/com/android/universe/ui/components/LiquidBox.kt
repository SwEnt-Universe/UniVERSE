package com.android.universe.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android.universe.ui.theme.CapsuleLarge
import com.android.universe.ui.utils.LocalLayerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy

@Composable
fun LiquidBox(
    modifier: Modifier = Modifier,
    shape: Shape = CapsuleLarge,
    color: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
    blurRadius: Dp = 16.dp,
    enableLens: Boolean = true,
    refractionHeight: Dp = 24.dp,
    refractionAmount: Dp = 24.dp,
    contentAlignment: Alignment = Alignment.TopStart,
    propagateMinConstraints: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
  val parentBackdrop = LocalLayerBackdrop.current
  val liquidBackdrop = rememberLayerBackdrop()

  Box(
      modifier =
          modifier.drawBackdrop(
              backdrop = parentBackdrop,
              exportedBackdrop = liquidBackdrop,
              shape = { shape },
              effects = {
                vibrancy()
                blur(blurRadius.toPx())
                if (enableLens) {
                  lens(refractionHeight.toPx(), refractionAmount.toPx())
                }
              },
              onDrawSurface = { drawRect(color) }),
      contentAlignment = contentAlignment,
      propagateMinConstraints = propagateMinConstraints) {
        CompositionLocalProvider(LocalLayerBackdrop provides liquidBackdrop) { content() }
      }
}
