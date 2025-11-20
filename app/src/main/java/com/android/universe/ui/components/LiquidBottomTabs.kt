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
 * https://github.com/Kyant0/AndroidLiquidGlass/blob/master/catalog/src/main/java/com/kyant/backdrop/catalog/components/LiquidBottomTabs.kt
 * Date taken: 2025-11-13
 *
 * Description: This file was originally created by Kyant0 Minor modifications were made for
 * integration into UniVERSE
 */
package com.android.universe.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceIn
import androidx.compose.ui.util.fastRoundToInt
import androidx.compose.ui.util.lerp
import com.android.universe.ui.theme.CapsuleLarge
import com.android.universe.ui.theme.Dimensions
import com.android.universe.ui.theme.UniverseTheme
import com.android.universe.ui.utils.DampedDragAnimation
import com.android.universe.ui.utils.InteractiveHighlight
import com.android.universe.ui.utils.LocalLayerBackdrop
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberCombinedBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import kotlin.math.abs
import kotlin.math.sign
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

/**
 * A composable that renders a "liquid glass" bottom navigation bar.
 *
 * It displays a row of tabs (provided via [content]) and highlights the selected tab with a fluid,
 * animated, and draggable indicator. This component relies heavily on the `com.kyant.backdrop`
 * library and must be provided with a [Backdrop] to render its effects against.
 *
 * @param selectedTabIndex A lambda function that returns the index of the currently selected tab.
 *   This is used to control the component's state.
 * @param onTabSelected A callback invoked when a new tab is selected, typically after a drag or tap
 *   animation completes.
 * @param backdrop The `Backdrop` instance from the `com.kyant.backdrop` library. This is the
 *   background layer that the component's glass effects will interact with.
 * @param tabsCount The total number of tabs in the bar. This is crucial for calculating the width
 *   of each tab and the animation bounds.
 * @param modifier The [Modifier] to be applied to the component's container.
 * @param content The composable content for the tabs, typically a series of `Tab` or `Icon`
 *   composables. This lambda is executed within a [RowScope].
 */
@Composable
fun LiquidBottomTabs(
    selectedTabIndex: () -> Int,
    onTabSelected: (index: Int) -> Unit,
    tabsCount: Int,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
  val backdrop = LocalLayerBackdrop.current

  val isDarkTheme = UniverseTheme.isDark
  val containerColor = MaterialTheme.colorScheme.background.copy(0.4f)
  val accentColor = MaterialTheme.colorScheme.onBackground.copy(0.4f)

  val tabsBackdrop = rememberLayerBackdrop()

  BoxWithConstraints(
      modifier =
          modifier
              .padding(horizontal = Dimensions.PaddingExtraLarge)
              .padding(bottom = Dimensions.PaddingExtraLarge),
      contentAlignment = Alignment.CenterStart) {
        val density = LocalDensity.current
        val tabWidth = with(density) { (constraints.maxWidth.toFloat() - 8f.dp.toPx()) / tabsCount }

        val offsetAnimation = remember { Animatable(0f) }
        val panelOffset by
            remember(density) {
              derivedStateOf {
                val fraction = (offsetAnimation.value / constraints.maxWidth).fastCoerceIn(-1f, 1f)
                with(density) { 4f.dp.toPx() * fraction.sign * EaseOut.transform(abs(fraction)) }
              }
            }

        val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
        val animationScope = rememberCoroutineScope()
        var currentIndex by remember(selectedTabIndex) { mutableIntStateOf(selectedTabIndex()) }
        val dampedDragAnimation =
            remember(animationScope) {
              DampedDragAnimation(
                  animationScope = animationScope,
                  initialValue = selectedTabIndex().toFloat(),
                  valueRange = 0f..(tabsCount - 1).toFloat(),
                  visibilityThreshold = 0.001f,
                  initialScale = 1f,
                  pressedScale = 78f / 56f,
                  onDragStarted = {},
                  onDragStopped = {
                    val targetIndex = targetValue.fastRoundToInt().fastCoerceIn(0, tabsCount - 1)
                    currentIndex = targetIndex
                    animateToValue(targetIndex.toFloat())
                    animationScope.launch { offsetAnimation.animateTo(0f, spring(1f, 300f, 0.5f)) }
                  },
                  onDrag = { _, dragAmount ->
                    updateValue(
                        (targetValue + dragAmount.x / tabWidth * if (isLtr) 1f else -1f)
                            .fastCoerceIn(0f, (tabsCount - 1).toFloat()))
                    animationScope.launch {
                      offsetAnimation.snapTo(offsetAnimation.value + dragAmount.x)
                    }
                  })
            }
        LaunchedEffect(selectedTabIndex) {
          snapshotFlow { selectedTabIndex() }.collectLatest { index -> currentIndex = index }
        }
        LaunchedEffect(dampedDragAnimation) {
          snapshotFlow { currentIndex }
              .drop(1)
              .collectLatest { index ->
                dampedDragAnimation.animateToValue(index.toFloat())
                onTabSelected(index)
              }
        }
        val interactiveHighlight =
            remember(animationScope) {
              InteractiveHighlight(
                  animationScope = animationScope,
                  position = { size, _ ->
                    Offset(
                        if (isLtr) (dampedDragAnimation.value + 0.5f) * tabWidth + panelOffset
                        else
                            size.width - (dampedDragAnimation.value + 0.5f) * tabWidth +
                                panelOffset,
                        size.height / 2f)
                  })
            }

        Row(
            Modifier.graphicsLayer { translationX = panelOffset }
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { CapsuleLarge },
                    effects = {
                      vibrancy()
                      blur(8f.dp.toPx())
                      lens(24f.dp.toPx(), 24f.dp.toPx())
                    },
                    layerBlock = {
                      val progress = dampedDragAnimation.pressProgress
                      val scale = lerp(1f, 1f + 16f.dp.toPx() / size.width, progress)
                      scaleX = scale
                      scaleY = scale
                    },
                    onDrawSurface = { drawRect(containerColor) })
                .then(interactiveHighlight.modifier)
                .height(64f.dp)
                .fillMaxWidth()
                .padding(4f.dp)
                .graphicsLayer(colorFilter = ColorFilter.tint(accentColor)),
            verticalAlignment = Alignment.CenterVertically,
            content = content)

        CompositionLocalProvider(
            LocalLiquidBottomTabScale provides
                {
                  lerp(1f, 1.2f, dampedDragAnimation.pressProgress)
                }) {
              Row(
                  Modifier.clearAndSetSemantics {}
                      .alpha(0f)
                      .layerBackdrop(tabsBackdrop)
                      .graphicsLayer { translationX = panelOffset }
                      .drawBackdrop(
                          backdrop = backdrop,
                          shape = { CapsuleLarge },
                          effects = {
                            val progress = dampedDragAnimation.pressProgress
                            vibrancy()
                            blur(8f.dp.toPx())
                            lens(24f.dp.toPx() * progress, 24f.dp.toPx() * progress)
                          },
                          highlight = {
                            val progress = dampedDragAnimation.pressProgress
                            Highlight.Default.copy(alpha = progress)
                          },
                          onDrawSurface = { drawRect(containerColor) })
                      .then(interactiveHighlight.modifier)
                      .height(56f.dp)
                      .fillMaxWidth()
                      .padding(horizontal = 4.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  content = content)
            }

        Box(
            Modifier.padding(horizontal = 4f.dp)
                .graphicsLayer {
                  translationX =
                      if (isLtr) dampedDragAnimation.value * tabWidth + panelOffset
                      else size.width - (dampedDragAnimation.value + 1f) * tabWidth + panelOffset
                }
                .then(interactiveHighlight.gestureModifier)
                .then(dampedDragAnimation.modifier)
                .drawBackdrop(
                    backdrop = rememberCombinedBackdrop(backdrop, tabsBackdrop),
                    shape = { CapsuleLarge },
                    effects = {
                      val progress = dampedDragAnimation.pressProgress
                      lens(
                          10f.dp.toPx() * progress,
                          14f.dp.toPx() * progress,
                          chromaticAberration = true)
                    },
                    highlight = {
                      val progress = dampedDragAnimation.pressProgress
                      Highlight.Default.copy(alpha = progress)
                    },
                    shadow = {
                      val progress = dampedDragAnimation.pressProgress
                      Shadow(alpha = progress)
                    },
                    innerShadow = {
                      val progress = dampedDragAnimation.pressProgress
                      InnerShadow(radius = 8f.dp * progress, alpha = progress)
                    },
                    layerBlock = {
                      scaleX = dampedDragAnimation.scaleX
                      scaleY = dampedDragAnimation.scaleY
                      val velocity = dampedDragAnimation.velocity / 10f
                      scaleX /= 1f - (velocity * 0.75f).fastCoerceIn(-0.2f, 0.2f)
                      scaleY *= 1f - (velocity * 0.25f).fastCoerceIn(-0.2f, 0.2f)
                    },
                    onDrawSurface = {
                      val progress = dampedDragAnimation.pressProgress
                      drawRect(
                          if (!isDarkTheme) Color.Black.copy(0.1f) else Color.White.copy(0.1f),
                          alpha = 1f - progress)
                      drawRect(Color.Black.copy(alpha = 0.03f * progress))
                    })
                .height(56f.dp)
                .fillMaxWidth(1f / tabsCount))
      }
}
