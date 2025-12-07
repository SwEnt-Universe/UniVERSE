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
 * Date taken: 2025-12-07
 *
 * Description: This file was originally created by Kyant0 Minor modifications were made for
 * integration into UniVERSE
 */
package com.android.universe.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceIn
import androidx.compose.ui.util.lerp
import com.android.universe.ui.theme.CapsuleLarge
import com.android.universe.ui.theme.UniverseTheme
import com.android.universe.ui.utils.DampedDragAnimation
import com.android.universe.ui.utils.LocalLayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberBackdrop
import com.kyant.backdrop.backdrops.rememberCombinedBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import kotlinx.coroutines.flow.collectLatest

@Composable
fun LiquidToggle(
    selected: () -> Boolean,
    onSelect: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
  val accentColor = UniverseTheme.extendedColors.toggleActive
  val trackColor = UniverseTheme.extendedColors.toggleTrack

  val parentBackdrop = LocalLayerBackdrop.current

  val density = LocalDensity.current
  val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
  val dragWidth = with(density) { 20f.dp.toPx() }
  val animationScope = rememberCoroutineScope()
  var didDrag by remember { mutableStateOf(false) }
  var fraction by remember { mutableFloatStateOf(if (selected()) 1f else 0f) }
  val dampedDragAnimation =
      remember(animationScope) {
        DampedDragAnimation(
            animationScope = animationScope,
            initialValue = fraction,
            valueRange = 0f..1f,
            visibilityThreshold = 0.001f,
            initialScale = 1f,
            pressedScale = 1.5f,
            onDragStarted = {},
            onDragStopped = {
              if (didDrag) {
                fraction = if (targetValue >= 0.5f) 1f else 0f
                onSelect(fraction == 1f)
                didDrag = false
              } else {
                fraction = if (selected()) 0f else 1f
                onSelect(fraction == 1f)
              }
            },
            onDrag = { _, dragAmount ->
              if (!didDrag) {
                didDrag = dragAmount.x != 0f
              }
              val delta = dragAmount.x / dragWidth
              fraction =
                  if (isLtr) (fraction + delta).fastCoerceIn(0f, 1f)
                  else (fraction - delta).fastCoerceIn(0f, 1f)
            })
      }
  LaunchedEffect(dampedDragAnimation) {
    snapshotFlow { fraction }
        .collectLatest { fraction -> dampedDragAnimation.updateValue(fraction) }
  }
  LaunchedEffect(selected) {
    snapshotFlow { selected() }
        .collectLatest { isSelected ->
          val target = if (isSelected) 1f else 0f
          if (target != fraction) {
            fraction = target
            dampedDragAnimation.animateToValue(target)
          }
        }
  }

  val trackBackdrop = rememberLayerBackdrop()

  Box(
      modifier =
          modifier.toggleable(
              value = selected(),
              onValueChange = onSelect,
              role = Role.Switch,
              interactionSource = remember { MutableInteractionSource() },
              indication = null),
      contentAlignment = Alignment.CenterStart) {
        Box(
            Modifier.layerBackdrop(trackBackdrop)
                .clip(CapsuleLarge)
                .drawBehind {
                  val fraction = dampedDragAnimation.value
                  drawRect(lerp(trackColor, accentColor, fraction))
                }
                .size(64f.dp, 28f.dp))

        Box(
            Modifier.graphicsLayer {
                  val fraction = dampedDragAnimation.value
                  val padding = 2f.dp.toPx()
                  translationX =
                      if (isLtr) lerp(padding, padding + dragWidth, fraction)
                      else lerp(-padding, -(padding + dragWidth), fraction)
                }
                .semantics { role = Role.Switch }
                .then(dampedDragAnimation.modifier)
                .drawBackdrop(
                    backdrop =
                        rememberCombinedBackdrop(
                            parentBackdrop,
                            rememberBackdrop(trackBackdrop) { drawBackdrop ->
                              val progress = dampedDragAnimation.pressProgress
                              val scaleX = lerp(2f / 3f, 0.75f, progress)
                              val scaleY = lerp(0f, 0.75f, progress)
                              scale(scaleX, scaleY) { drawBackdrop() }
                            }),
                    shape = { CapsuleLarge },
                    effects = {
                      val progress = dampedDragAnimation.pressProgress
                      blur(8f.dp.toPx() * (1f - progress))
                      lens(
                          5f.dp.toPx() * progress,
                          10f.dp.toPx() * progress,
                          chromaticAberration = true)
                    },
                    highlight = {
                      val progress = dampedDragAnimation.pressProgress
                      Highlight.Ambient.copy(
                          width = Highlight.Ambient.width / 1.5f,
                          blurRadius = Highlight.Ambient.blurRadius / 1.5f,
                          alpha = progress)
                    },
                    shadow = { Shadow(radius = 4f.dp, color = Color.Black.copy(alpha = 0.05f)) },
                    innerShadow = {
                      val progress = dampedDragAnimation.pressProgress
                      InnerShadow(radius = 4f.dp * progress, alpha = progress)
                    },
                    layerBlock = {
                      scaleX = dampedDragAnimation.scaleX
                      scaleY = dampedDragAnimation.scaleY
                      val velocity = dampedDragAnimation.velocity / 50f
                      scaleX /= 1f - (velocity * 0.75f).fastCoerceIn(-0.2f, 0.2f)
                      scaleY *= 1f - (velocity * 0.25f).fastCoerceIn(-0.2f, 0.2f)
                    },
                    onDrawSurface = {
                      val progress = dampedDragAnimation.pressProgress
                      drawRect(Color.White.copy(alpha = 1f - progress))
                    })
                .size(40f.dp, 24f.dp))
      }
}
