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

/**
 * A container that applies a "liquid" visual effect to its background, including vibrancy, blur,
 * and optional lens refraction.
 *
 * This composable creates a backdrop effect that samples the content behind it (using
 * [LocalLayerBackdrop]), applies blur and vibrancy effects, and optionally distorts the image to
 * simulate a lens or liquid surface. It also provides a new [LocalLayerBackdrop] to its children,
 * allowing nested backdrop effects.
 *
 * @param modifier The modifier to be applied to the layout.
 * @param shape The shape of the container. Defaults to [CapsuleLarge].
 * @param color The overlay color applied on top of the backdrop effects. Defaults to a
 *   semi-transparent surface color.
 * @param blurRadius The radius of the blur effect applied to the background. Defaults to 16.dp.
 * @param enableLens Whether to apply a lens refraction effect. Defaults to true.
 * @param refractionHeight The apparent "height" of the lens for calculating refraction. Defaults to
 *   24.dp.
 * @param refractionAmount The intensity of the refraction displacement. Defaults to 24.dp.
 * @param contentAlignment The default alignment inside the Box.
 * @param propagateMinConstraints Whether the incoming min constraints should be passed to content.
 * @param content The content to be displayed inside the box.
 */
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
