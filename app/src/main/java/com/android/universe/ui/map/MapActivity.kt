// MapActivity.kt
package com.android.universe.ui.map

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.android.universe.R
import com.tomtom.sdk.map.display.ui.MapFragment as TomTomMapFragment
import kotlinx.coroutines.launch

/**
 * Dedicated screen for the TomTom map (XML-hosted MapFragment).
 * - XML path `res/layout/activity_map.xml`
 * - Business/UI logic in [MapViewModel]
 *
 * The Activity observes and renders state only; it does not trigger logic like initial camera
 * moves.
 */
class MapActivity : AppCompatActivity() {

  private val viewModel: MapViewModel by viewModels()

  /**
   * Initializes the map screen and connects UI to the ViewModel.
   *
   * Flow:
   * 1) Inflate the XML layout (`activity_map`) and look up the TomTom `MapFragment` by ID.
   * 2) Call `getMapAsync` to obtain the `TomTomMap` when it’s ready (map creation is async).
   * 3) Once the map is available, launch a lifecycle-aware coroutine:
   *     - `repeatOnLifecycle(Lifecycle.State.STARTED)` collects `cameraCommands` only while the
   *       Activity is at least STARTED, and automatically stops collection when the Activity is
   *       STOPPED or DESTROYED, preventing leaks.
   *     - Each emitted camera command is applied to the map via `tomtomMap.moveCamera(...)`.
   *
   * Responsibility:
   * - The Activity only observes and renders state.
   */
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_map)

    val mapFragment =
        supportFragmentManager.findFragmentById(R.id.map_fragment) as? TomTomMapFragment
            ?: return // MapFragment not found

    mapFragment.getMapAsync { tomtomMap ->
      // Observe camera commands from the ViewModel and apply them to the map.
      lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
          viewModel.cameraCommands.collect { camera -> tomtomMap.moveCamera(camera) }
        }
      }
    }
  }
}
