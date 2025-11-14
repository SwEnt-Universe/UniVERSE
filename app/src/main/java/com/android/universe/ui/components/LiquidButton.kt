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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceAtMost
import androidx.compose.ui.util.lerp
import com.android.universe.ui.utils.InteractiveHighlight
import com.kyant.backdrop.Backdrop
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
 * @param onClick The lambda function to be executed when the button is clicked.
 * @param backdrop The `Backdrop` instance from the `com.kyant.backdrop` library. This is the
 *   background layer that the component's glass effects will interact with.
 * @param modifier The [Modifier] to be applied to the component's container.
 * @param isInteractive Whether the component is interactive or not.
 * @param tint The color to tint the component with.
 * @param surfaceColor The color to fill the component with.
 * @param content The composable content for the button, typically a series of `Icon` or `Text`
 *   composables. This lambda is executed within a [RowScope].
 */
@Composable
fun LiquidButton(
    onClick: () -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    isInteractive: Boolean = true,
    tint: Color = Color.Unspecified,
    surfaceColor: Color = Color.Unspecified,
    content: @Composable RowScope.() -> Unit
) {
  val animationScope = rememberCoroutineScope()

  val interactiveHighlight =
      remember(animationScope) { InteractiveHighlight(animationScope = animationScope) }

  Row(
      modifier
          .drawBackdrop(
              backdrop = backdrop,
              shape = { RoundedCornerShape(percent = 50) },
              effects = {
                vibrancy()
                blur(2f.dp.toPx())
                lens(12f.dp.toPx(), 24f.dp.toPx())
              },
              layerBlock =
                  if (isInteractive) {
                    {
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
                  } else {
                    null
                  },
              onDrawSurface = {
                if (tint.isSpecified) {
                  drawRect(tint, blendMode = BlendMode.Hue)
                  drawRect(tint.copy(alpha = 0.75f))
                }
                if (surfaceColor.isSpecified) {
                  drawRect(surfaceColor)
                }
              })
          .clickable(
              interactionSource = null,
              indication = if (isInteractive) null else LocalIndication.current,
              role = Role.Button,
              onClick = onClick)
          .then(
              if (isInteractive) {
                Modifier.then(interactiveHighlight.modifier)
                    .then(interactiveHighlight.gestureModifier)
              } else {
                Modifier
              })
          .height(48f.dp)
          .padding(horizontal = 16f.dp),
      horizontalArrangement = Arrangement.spacedBy(8f.dp, Alignment.CenterHorizontally),
      verticalAlignment = Alignment.CenterVertically,
      content = content)
}
