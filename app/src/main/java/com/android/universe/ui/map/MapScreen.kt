package com.android.universe.ui.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.universe.databinding.MapFragmentHostBinding
import com.android.universe.ui.navigation.NavigationBottomMenu
import com.android.universe.ui.navigation.Tab
import com.tomtom.sdk.map.display.ui.MapFragment as TomTomMapFragment
import kotlinx.coroutines.launch

@Composable
fun MapScreen(
    selectedTab: Tab,
    onTabSelected: (Tab) -> Unit,
    enableBottomBar: Boolean = true,
    testTag: String? = null,
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = viewModel()
) {
  val maybeFragmentActivity = LocalContext.current as? FragmentActivity
  val lifecycleOwner = LocalLifecycleOwner.current
  var fragment: TomTomMapFragment? by remember { mutableStateOf(null) }

  Scaffold(
      modifier = if (testTag != null) Modifier.testTag(testTag) else Modifier,
      bottomBar = { if (enableBottomBar) NavigationBottomMenu(selectedTab, onTabSelected) }) {
          padding ->
        AndroidViewBinding(
            factory = MapFragmentHostBinding::inflate,
            modifier = modifier.fillMaxSize().padding(padding)) {
              // Only try to resolve the fragment when we have a FragmentActivity
              fragment =
                  maybeFragmentActivity?.supportFragmentManager?.findFragmentById(mapFragment.id)
                      as? TomTomMapFragment
            }
      }

  // Only wire the map if we actually resolved a fragment and we have a FragmentActivity
  LaunchedEffect(fragment?.id, maybeFragmentActivity) {
    val activity = maybeFragmentActivity ?: return@LaunchedEffect
    fragment?.getMapAsync { tomtomMap ->
      activity.lifecycleScope.launch {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
          viewModel.cameraCommands.collect { cmd -> tomtomMap.moveCamera(cmd) }
        }
      }
    }
  }
}
