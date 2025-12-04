package com.android.universe.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * A layout composable that arranges its children in a way that is common for screen UIs. It places
 * a main content area, a top bar, and a bottom bar. The top and bottom bars are measured and their
 * heights are provided as padding to the main content, allowing the content to be laid out
 * correctly under the bars.
 *
 * This is useful for creating screens with floating or overlapping top/bottom bars, where the
 * content needs to be aware of the space occupied by these bars to avoid being obscured.
 *
 * @param modifier The modifier to be applied to the layout.
 * @param topBar An optional composable lambda for the top bar. It will be placed at the top of the
 *   screen.
 * @param bottomBar An optional composable lambda for the bottom bar. It will be placed at the
 *   bottom of the screen.
 * @param content The main content of the screen. It receives [PaddingValues] that specify the space
 *   occupied by the top and bottom bars, which should be applied as padding to the content's
 *   container to prevent it from being drawn underneath the bars.
 */
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ScreenLayout(
    modifier: Modifier = Modifier,
    topBar: @Composable (() -> Unit) = {},
    bottomBar: @Composable (() -> Unit) = {},
    content: @Composable (PaddingValues) -> Unit
) {
  Scaffold(
      modifier = modifier,
      containerColor = Color.Transparent,
      topBar = topBar,
      bottomBar = bottomBar,
  ) { scaffoldPadding ->
    Box(modifier = Modifier.fillMaxSize()) { content(scaffoldPadding) }
  }
}
