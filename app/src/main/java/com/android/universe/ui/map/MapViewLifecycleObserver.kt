package com.android.universe.ui.map

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.Event.ON_PAUSE
import androidx.lifecycle.Lifecycle.Event.ON_RESUME
import androidx.lifecycle.Lifecycle.Event.ON_START
import androidx.lifecycle.Lifecycle.Event.ON_STOP
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.tomtom.sdk.map.display.ui.MapView

class MapViewLifecycleObserver(private val mapView: MapView) : LifecycleEventObserver {
  override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
    when (event) {
      ON_START -> mapView.onStart()
      ON_RESUME -> mapView.onResume()
      ON_PAUSE -> mapView.onPause()
      ON_STOP -> mapView.onStop()
      else -> {
        // DO NOTHING (managed manually)
      }
    }
  }
}
