package com.android.universe.ui.map

import com.android.universe.R
import com.android.universe.di.DefaultDP
import com.tomtom.sdk.map.display.image.Image
import com.tomtom.sdk.map.display.image.ImageFactory
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/** This object serves as cache for images, this way we don't have to recreate image each time */
object MarkerImageCache {
  @Volatile private var _cache: Map<Int, Image>? = null

  private val mutex = Mutex()

  /**
   * Create the cache the first time it asks, if the map returns null for now (the processing took
   * to long it default to processing the image itself)
   */
  suspend fun get(resId: Int): Image {
    val localCache = _cache
    if (localCache != null) {
      return localCache[resId] ?: fromResourceWrapper(resId)
    }

    mutex.withLock {
      val doubleCheckCache = _cache
      if (doubleCheckCache != null) {
        return doubleCheckCache[resId] ?: fromResourceWrapper(resId)
      }

      val newCache = withContext(DefaultDP.io) { loadImages() }

      _cache = newCache
      return newCache[resId] ?: fromResourceWrapper(resId)
    }
  }

  /* This function is used to add a level of indirection for testing purposes */
  private fun fromResourceWrapper(resId: Int): Image = ImageFactory.fromResource(resId)

  private fun loadImages() =
      mapOf(
          R.drawable.violet_pin_light_mode to fromResourceWrapper(R.drawable.violet_pin_light_mode),
          R.drawable.violet_pin_dark_mode to fromResourceWrapper(R.drawable.violet_pin_dark_mode),
          R.drawable.sky_blue_pin_dark_mode to
              fromResourceWrapper(R.drawable.sky_blue_pin_dark_mode),
          R.drawable.sky_blue_pin_light_mode to
              fromResourceWrapper(R.drawable.sky_blue_pin_light_mode),
          R.drawable.red_pin_dark_mode to fromResourceWrapper(R.drawable.red_pin_dark_mode),
          R.drawable.red_pin_light_mode to fromResourceWrapper(R.drawable.red_pin_light_mode),
          R.drawable.brown_pin_dark_mode to fromResourceWrapper(R.drawable.brown_pin_dark_mode),
          R.drawable.brown_pin_light_mode to fromResourceWrapper(R.drawable.brown_pin_light_mode),
          R.drawable.orange_pin_dark_mode to fromResourceWrapper(R.drawable.orange_pin_dark_mode),
          R.drawable.orange_pin_dark_mode to fromResourceWrapper(R.drawable.orange_pin_dark_mode),
          R.drawable.green_pin_dark_mode to fromResourceWrapper(R.drawable.green_pin_dark_mode),
          R.drawable.green_pin_light_mode to fromResourceWrapper(R.drawable.green_pin_light_mode),
          R.drawable.pink_pin_dark_mode to fromResourceWrapper(R.drawable.pink_pin_dark_mode),
          R.drawable.pink_pin_light_mode to fromResourceWrapper(R.drawable.pink_pin_light_mode),
          R.drawable.base_pin_dark_mode to fromResourceWrapper(R.drawable.base_pin_light_mode),
          R.drawable.base_pin_light_mode to fromResourceWrapper(R.drawable.base_pin_light_mode))
}
