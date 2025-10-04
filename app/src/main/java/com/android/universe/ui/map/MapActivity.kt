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
 *
 * - XML path `res/layout/activity_map.xml`
 * - Business/UI logic in [MapViewModel]
 */
class MapActivity : AppCompatActivity() {

    private val viewModel: MapViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_fragment) as? TomTomMapFragment

        mapFragment?.getMapAsync { tomtomMap ->
            // React to ViewModel commands (camera moves, etc.)
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.cameraCommands.collect { camera ->
                        tomtomMap.moveCamera(camera)
                    }
                }
            }

            // Initial camera position
            // TODO replace with user location, if available
            viewModel.centerOnLausanne()
        }
    }
}
