package com.android.universe.ui.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import com.android.universe.BuildConfig
import com.android.universe.ui.navigation.NavigationBottomMenu
import com.android.universe.ui.navigation.NavigationTestTags
import com.android.universe.ui.navigation.Tab
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.map.display.MapOptions
import com.tomtom.sdk.map.display.camera.CameraOptions
import com.tomtom.sdk.map.display.ui.MapView

/**
 * Composable that represents the main map screen of the application.
 *
 * This screen uses a [Scaffold] to provide a standard layout structure, including a bottom
 * navigation bar. The main content of the screen is the [TomTomMapView], which displays the map.
 *
 * @param onTabSelected A lambda function that is invoked when a tab in the bottom navigation menu
 *   is selected. It defaults to an empty lambda.
 */
@Composable
fun MapScreen(onTabSelected: (Tab) -> Unit = {}) {
  Scaffold(
      modifier = Modifier.fillMaxSize().testTag(NavigationTestTags.MAP_SCREEN),
      bottomBar = { NavigationBottomMenu(Tab.Map, onTabSelected) }) { paddingValues ->
        TomTomMapView(modifier = Modifier.padding(paddingValues))
      }
}

// TODO: (temp) put in Viewmodel when merging
var position = GeoPoint(latitude = 46.5181, longitude = 6.5668)
var zoom = 14.0

/**
 * A composable function that wraps the TomTom [MapView] in an [AndroidView], allowing the
 * traditional Android View-based map to be used within a Jetpack Compose UI.
 *
 * This function creates and remembers a `MapView` instance using the local context and the API key
 * from `BuildConfig`. It handles the lifecycle of the `MapView` (`onCreate`, `onStart`) within the
 * `AndroidView` factory.
 *
 * In the `update` block, it asynchronously gets the map, moves the camera to a predefined initial
 * position, and adds a listener to update the camera's position and zoom level as the user
 * interacts with the map.
 *
 * @param modifier The modifier to be applied to the layout. Defaults to [Modifier].
 */
@Composable
fun TomTomMapView(modifier: Modifier = Modifier) {
  val context = LocalContext.current
  val mapView = remember { MapView(context, MapOptions(BuildConfig.TOMTOM_API_KEY)) }
  AndroidView(
      modifier = modifier.fillMaxSize(),
      // called when the view is first initialized (example: after navigation)
      factory = { context ->
        mapView.also {
          it.onCreate(null)
          it.onStart()
        }
      },
      // called each time the composable is recomposed (example: after StateFlow change)
      update = {
        mapView.getMapAsync {
          it.moveCamera(CameraOptions(position, zoom)) // Example interaction,
        }
      })
}
