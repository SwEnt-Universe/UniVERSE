package com.android.universe.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag

object ScreenLayoutTestTags {
  const val TOP_BAR = "topBar"
  const val BOTTOM_BAR = "bottomBar"
}

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
@Composable
fun ScreenLayout(
    modifier: Modifier = Modifier,
    topBar: @Composable (() -> Unit)? = null,
    bottomBar: @Composable (() -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit
) {
  Box(modifier = modifier.fillMaxSize()) {
    var topBarHeight by remember { mutableIntStateOf(0) }
    var bottomBarHeight by remember { mutableIntStateOf(0) }
    // Main content fills the screen
    Box(modifier = Modifier.fillMaxSize().imePadding()) {
      content(
          PaddingValues(
              top = with(LocalDensity.current) { topBarHeight.toDp() },
              bottom = with(LocalDensity.current) { bottomBarHeight.toDp() }))
    }

    // Floating top bar
    if (topBar != null) {
      Box(
          modifier =
              Modifier.fillMaxWidth()
                  .onSizeChanged { topBarHeight = it.height }
                  .align(Alignment.TopCenter)
                  .testTag(ScreenLayoutTestTags.TOP_BAR)) {
            topBar()
          }
    }

    // Floating bottom bar
    if (bottomBar != null) {
      Box(
          modifier =
              Modifier.wrapContentSize()
                  .onSizeChanged { bottomBarHeight = it.height }
                  .align(Alignment.BottomCenter)
                  .testTag(ScreenLayoutTestTags.BOTTOM_BAR)) {
            bottomBar()
          }
    }
  }
}
