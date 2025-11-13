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
 * https://github.com/Kyant0/AndroidLiquidGlass/blob/master/catalog/src/main/java/com/kyant/backdrop/catalog/components/LiquidBottomTab.kt
 * Date taken: 2025-11-13
 *
 * Description: This file was originally created by Kyant0 Minor modifications were made for
 * integration into UniVERSE
 */
package com.android.universe.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.NavigationBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

/**
 * A CompositionLocal that provides the current scale factor as a function reference [() -> Float]
 * for the Liquid Bottom Tab animation.
 *
 * This allows child composables (specifically [LiquidBottomTab]) to read the dynamic, animated
 * scale value (e.g., 0.9f when pressed, 1.0f when idle) without needing to pass the animation state
 * explicitly down through every function.
 *
 * The provided value is a lambda to ensure the reading of the scale factor happens inside the
 * high-performance [Modifier.graphicsLayer] block.
 *
 * The default value is a lambda that always returns 1.0f, representing no scale change.
 */
internal val LocalLiquidBottomTabScale = staticCompositionLocalOf { { 1f } }

/**
 * A custom, liquid-animated tab item designed for use within a horizontal layout, typically a
 * [NavigationBar].
 *
 * This composable enforces its placement within a [RowScope] (via the extension function syntax)
 * and uses a custom scale value provided by [LocalLiquidBottomTabScale] to achieve a dynamic
 * scaling animation (via [Modifier.graphicsLayer]) based on interaction or selection state.
 *
 * It applies a uniform weight of 1f to ensure all tabs within the parent Row/NavigationBar take up
 * equal horizontal space.
 *
 * @param onClick The lambda function to be executed when the tab is clicked.
 * @param modifier The [Modifier] to be applied to the tab container (the root [Column]).
 * @param content The composable content to display inside the tab (e.g., Icon and Text). The
 *   content is placed within a [ColumnScope], allowing it to use column-specific modifiers.
 */
@Composable
fun RowScope.LiquidBottomTab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
  val scale = LocalLiquidBottomTabScale.current
  Column(
      modifier
          .clip(RoundedCornerShape(100.dp))
          .clickable(
              interactionSource = null, // To disable the standard ripple effect
              indication = null, // To disable the standard ripple effect
              role = Role.Tab, // Accessibility information
              onClick = onClick)
          .fillMaxHeight()
          .weight(1f)
          .graphicsLayer { // Applies low level, high-performance visual transformations without
            // triggering a full layout pass.
            val scale = scale()
            scaleX = scale
            scaleY = scale
          },
      verticalArrangement = Arrangement.spacedBy(2f.dp, Alignment.CenterVertically),
      horizontalAlignment = Alignment.CenterHorizontally,
      content = content)
}
