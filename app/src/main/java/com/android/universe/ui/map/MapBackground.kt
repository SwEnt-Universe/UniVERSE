package com.android.universe.ui.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun MapBackground(modifier: Modifier = Modifier, viewModel: MapViewModel) {
  val mapViewInstance by remember { mutableStateOf(viewModel.getMapInstance()) }
  val lifecycleOwner = LocalLifecycleOwner.current

  DisposableEffect(lifecycleOwner) {
    val observer = MapViewLifecycleObserver(mapViewInstance)
    lifecycleOwner.lifecycle.addObserver(observer)

    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }

  Surface(modifier.fillMaxSize()) {
    AndroidView(
        factory = {
          viewModel.decoupleFromParent()
          mapViewInstance
        },
        modifier = Modifier.testTag(MapScreenTestTags.MAP_VIEW))
  }
}
