/*
 * Copyright 2024 The AndroidLiquidGlass Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Original source:
 * https://github.com/Kyant0/AndroidLiquidGlass/blob/master/catalog/src/main/java/com/kyant/backdrop/catalog/components/LiquidButton.kt
 * Date taken: 2025-11-14
 *
 * Description: This file was originally created by Kyant0 Minor modifications were made for
 * integration into UniVERSE
 */
package com.android.universe.ui.components

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceAtMost
import androidx.compose.ui.util.lerp
import com.android.universe.ui.theme.CapsuleLarge
import com.android.universe.ui.utils.InteractiveHighlight
import com.android.universe.ui.utils.LocalLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tanh

/**
 * A glass-style button that exhibits "liquid" properties, such as squashing, stretching, and
 * wobbling in response to user interaction.
 *
 * This button can be rendered in two ways:
 * 1. **With a backdrop:** It uses the `drawBackdrop` API to create a blurred, vibrant, and
 *    lens-distorted background effect. This is the default behavior.
 * 2. **Without a backdrop:** It renders as a simple colored surface, useful for environments where
 *    backdrop effects are unavailable or disabled. The liquid deformation is still applied via a
 *    `graphicsLayer`.
 *
 * The "liquid" effect is achieved through a combination of scaling and translation transformations
 * based on press and drag gestures.
 *
 * @param onClick The lambda function to be executed when the button is clicked.
 * @param modifier The [Modifier] to be applied to the button.
 * @param enabled Controls the enabled state of the button. When `false`, the button will not be
 *   clickable and interaction effects are disabled.
 * @param isInteractive If `true` and the button is `enabled`, liquid interaction effects (squash,
 *   stretch, wobble) will be active. If `false`, standard ripple effects are used instead.
 * @param tint An optional color to tint the button's surface. When used with a backdrop, it applies
 *   a hue shift and a translucent overlay.
 * @param disableBackdrop If `true`, the backdrop effect is disabled and a simple `surfaceColor` is
 *   drawn instead. Set this to `true` for better performance or when backdrop layers are not
 *   available.
 * @param height The height of the button.
 * @param width The width of the button.
 * @param surfaceColor The color of the button's surface when `disableBackdrop` is `true`, or the
 *   translucent overlay color when `disableBackdrop` is `false`.
 */
@Composable
fun LiquidButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isInteractive: Boolean = true,
    tint: Color = Color.Unspecified,
    disableBackdrop: Boolean = false,
    height: Float = 48f,
    width: Float = 192f,
    surfaceColor: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
    contentPadding: Dp = 16.dp,
    content: @Composable RowScope.() -> Unit
) {
  val animationScope = rememberCoroutineScope()
  val backdrop = LocalLayerBackdrop.current
  val interactiveHighlight =
      remember(animationScope) { InteractiveHighlight(animationScope = animationScope) }

  // Logic for the "Liquid" physical deformation (Squash & Stretch)
  // We define this once so it can be used by either the GraphicsLayer or the Backdrop
  val liquidDeformation: GraphicsLayerScope.() -> Unit = {
    val width = size.width
    val height = size.height

    val progress = interactiveHighlight.pressProgress
    val scale = lerp(1f, 1f + 4f.dp.toPx() / size.height, progress)

    val maxOffset = size.minDimension
    val initialDerivative = 0.05f
    val offset = interactiveHighlight.offset
    translationX = maxOffset * tanh(initialDerivative * offset.x / maxOffset)
    translationY = maxOffset * tanh(initialDerivative * offset.y / maxOffset)

    val maxDragScale = 4f.dp.toPx() / size.height
    val offsetAngle = atan2(offset.y, offset.x)
    scaleX =
        scale +
            maxDragScale *
                abs(cos(offsetAngle) * offset.x / size.maxDimension) *
                (width / height).fastCoerceAtMost(1f)
    scaleY =
        scale +
            maxDragScale *
                abs(sin(offsetAngle) * offset.y / size.maxDimension) *
                (height / width).fastCoerceAtMost(1f)
  }

  val backgroundModifier =
      if (disableBackdrop) {
        Modifier.then(
                if (enabled && isInteractive) {
                  Modifier.graphicsLayer(block = liquidDeformation)
                } else Modifier)
            .clip(CapsuleLarge)
            .drawBehind {
              if (tint.isSpecified) {
                drawRect(tint)
              }
              if (surfaceColor.isSpecified) {
                drawRect(surfaceColor)
              }
            }
      } else {
        Modifier.drawBackdrop(
            backdrop = backdrop,
            shape = { CapsuleLarge },
            effects = {
              vibrancy()
              blur(16f.dp.toPx())
              lens(24f.dp.toPx(), 24f.dp.toPx())
            },
            layerBlock = if (enabled && isInteractive) liquidDeformation else null,
            onDrawSurface = {
              if (tint.isSpecified) {
                drawRect(tint, blendMode = BlendMode.Hue)
                drawRect(tint.copy(alpha = 0.75f))
              }
              if (surfaceColor.isSpecified) {
                drawRect(surfaceColor)
              }
            })
      }

  Row(
      modifier
          .then(backgroundModifier)
          .clickable(
              interactionSource = null,
              enabled = enabled,
              indication = if (isInteractive) null else LocalIndication.current,
              role = Role.Button,
              onClick = onClick)
          .then(
              if (enabled && isInteractive) {
                Modifier.then(interactiveHighlight.modifier)
                    .then(interactiveHighlight.gestureModifier)
              } else {
                Modifier
              })
          .height(height.dp)
          .width(width.dp)
          .padding(horizontal = contentPadding),
      horizontalArrangement = Arrangement.spacedBy(8f.dp, Alignment.CenterHorizontally),
      verticalAlignment = Alignment.CenterVertically,
      content = content)
}

object LiquidButtonTestTags {
  const val LIQUID_BUTTON = "liquid_button"
}
