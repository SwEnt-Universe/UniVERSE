package com.android.universe.ui.utils

import androidx.compose.runtime.staticCompositionLocalOf
import com.kyant.backdrop.backdrops.LayerBackdrop

val LocalLayerBackdrop =
    staticCompositionLocalOf<LayerBackdrop> {
      error("No LayerBackdrop provided. Provide it in MainActivity's setContent block.")
    }
