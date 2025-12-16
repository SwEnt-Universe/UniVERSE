package com.android.universe.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.android.universe.ui.common.CommonBackground.BACKGROUND
import com.android.universe.ui.map.MapBackground
import com.android.universe.ui.map.MapViewModel
import com.android.universe.ui.utils.LocalLayerBackdrop
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop

object CommonBackground {
  const val BACKGROUND = "background"
}

/**
 * A default container for the background map
 *
 * @param contentAlignment The alignment of the content within the container.
 * @param content The content to display inside the container.
 */
@Composable
fun UniverseBackgroundContainer(
    mapViewModel: MapViewModel,
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable () -> Unit
) {
  Box(Modifier.fillMaxSize().testTag(BACKGROUND), contentAlignment = contentAlignment) {
    UniverseBackground(mapViewModel)
    content()
  }
}

/**
 * A composable for the background image.
 *
 * @param mapViewModel the mapView associated with the viewModel.
 */
@Composable
fun UniverseBackground(mapViewModel: MapViewModel) {
  val backdrop = LocalLayerBackdrop.current
  val uiState by mapViewModel.uiState.collectAsState()
  MapBackground(modifier = Modifier.layerBackdrop(backdrop), viewModel = mapViewModel)
  if (uiState.isLoading) {
    DefaultBackground(backdrop)
  }
}

@Composable
fun DefaultBackground(backdrop: LayerBackdrop) {
  Surface(Modifier.layerBackdrop(backdrop).fillMaxSize()) {}
}
