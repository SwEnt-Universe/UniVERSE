package com.android.universe.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import com.android.universe.ui.utils.LocalLayerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop

fun ComposeContentTestRule.setContentWithStubBackdrop(content: @Composable () -> Unit) {
    this.setContent {
        val stubBackdrop = rememberLayerBackdrop { drawRect(Color.Transparent) }

        CompositionLocalProvider(LocalLayerBackdrop provides stubBackdrop) { content() }
    }
}