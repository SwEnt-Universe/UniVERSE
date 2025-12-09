package com.android.universe.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.DrawerDefaults.scrimColor
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetDefaults
import androidx.compose.material3.ModalBottomSheetDefaults.properties
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.ScaffoldDefaults.contentWindowInsets
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A customized [ModalBottomSheet] implementation that applies a "liquid" or "glassmorphism" visual
 * effect to its background.
 *
 * Instead of a solid surface, this bottom sheet uses a [LiquidBox] to render a blurred, refracted,
 * and vibrant background behind the content. It handles the necessary nesting logic to ensure the
 * drag handle and content are properly layered over the liquid effect.
 *
 * @param isPresented Whether the bottom sheet is currently visible.
 * @param onDismissRequest The callback invoked when the user taps the scrim or drags the sheet
 *   away.
 * @param modifier The modifier to be applied to the bottom sheet layout.
 * @param sheetState The state of the bottom sheet. Defaults to [rememberModalBottomSheetState] with
 *   `skipPartiallyExpanded = true`.
 * @param sheetMaxWidth The maximum width of the bottom sheet.
 * @param shape The shape of the bottom sheet. This is applied to both the interaction bounds and
 *   the clipping of the liquid effect.
 * @param containerColor The tint color applied to the liquid glass effect. Note: The actual Sheet
 *   container is forced to [Color.Transparent] to allow the blur to render.
 * @param contentColor The default color for content inside the sheet.
 * @param tonalElevation The tonal elevation of the bottom sheet.
 * @param scrimColor The color of the scrim that obscures the screen behind the bottom sheet.
 * @param dragHandle The visual handle (pill) at the top of the sheet. Defaults to
 *   [CustomDragHandle]. Pass `null` to hide.
 * @param contentWindowInsets The window insets to be respected. Defaults to `WindowInsets(0, 0, 0,
 *   0)` to allow the sheet to extend to the screen edges.
 * @param properties [ModalBottomSheetProperties] for further customization.
 * @param blurRadius The radius of the blur effect applied to the background content. Defaults to
 *   16.dp.
 * @param refractionHeight The apparent height of the liquid lens, affecting the refraction
 *   calculation. Defaults to 24.dp.
 * @param refractionAmount The intensity of the displacement/refraction effect. Defaults to 24.dp.
 * @param bottomBar optional bottom bar displaying options
 * @param content The content to be displayed inside the bottom sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiquidBottomSheet(
    isPresented: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    sheetMaxWidth: Dp = BottomSheetDefaults.SheetMaxWidth,
    shape: Shape = BottomSheetDefaults.ExpandedShape,
    containerColor: Color = Color.Transparent,
    contentColor: Color = MaterialTheme.colorScheme.onBackground,
    tonalElevation: Dp = BottomSheetDefaults.Elevation,
    scrimColor: Color = BottomSheetDefaults.ScrimColor,
    dragHandle: @Composable (() -> Unit)? = { CustomDragHandle() },
    contentWindowInsets: @Composable () -> WindowInsets = { WindowInsets(0, 0, 0, 0) },
    properties: ModalBottomSheetProperties = ModalBottomSheetDefaults.properties,
    blurRadius: Dp = 16.dp,
    refractionHeight: Dp = 24.dp,
    refractionAmount: Dp = 24.dp,
    bottomBar: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
  if (isPresented) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        sheetMaxWidth = sheetMaxWidth,
        shape = shape,
        containerColor = Color.Transparent,
        contentColor = contentColor,
        tonalElevation = tonalElevation,
        scrimColor = scrimColor,
        dragHandle = null,
        contentWindowInsets = contentWindowInsets,
        properties = properties) {
          LiquidBox(
              modifier = Modifier.fillMaxWidth().clip(shape),
              shape = shape,
              color = containerColor,
              blurRadius = blurRadius,
              refractionHeight = refractionHeight,
              refractionAmount = refractionAmount) {
                Column(modifier = Modifier.fillMaxWidth()) {
                  if (dragHandle != null) {
                    Box(
                        modifier = Modifier.fillMaxWidth().align(Alignment.CenterHorizontally),
                        contentAlignment = Alignment.Center) {
                          dragHandle()
                        }
                  }

                  content()

                  bottomBar?.let {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                      it()
                    }
                  }
                }
              }
        }
  }
}

/**
 * A default drag handle (gripper) designed for the Liquid Bottom Sheet.
 *
 * It renders a small, semi-transparent pill shape. The color is derived from the Theme with reduced
 * alpha to blend seamlessly with the glass effect.
 */
@Composable
fun CustomDragHandle() {
  Row(
      modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 12.dp),
      horizontalArrangement = Arrangement.Center) {
        Surface(
            modifier = Modifier.size(width = 32.dp, height = 4.dp),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
            shape = MaterialTheme.shapes.extraSmall) {}
      }
}
