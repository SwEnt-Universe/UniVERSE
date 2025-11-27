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
      return localCache[resId] ?: ImageFactory.fromResource(resId)
    }

    mutex.withLock {
      val doubleCheckCache = _cache
      if (doubleCheckCache != null) {
        return doubleCheckCache[resId] ?: ImageFactory.fromResource(resId)
      }

      val newCache = withContext(DefaultDP.io) { loadImages() }

      _cache = newCache
      return newCache[resId] ?: ImageFactory.fromResource(resId)
    }
  }

  private fun loadImages() =
      mapOf(
          R.drawable.violet_pin to ImageFactory.fromResource(R.drawable.violet_pin),
          R.drawable.sky_blue_pin to ImageFactory.fromResource(R.drawable.sky_blue_pin),
          R.drawable.yellow_pin to ImageFactory.fromResource(R.drawable.yellow_pin),
          R.drawable.red_pin to ImageFactory.fromResource(R.drawable.red_pin),
          R.drawable.brown_pin to ImageFactory.fromResource(R.drawable.brown_pin),
          R.drawable.orange_pin to ImageFactory.fromResource(R.drawable.orange_pin),
          R.drawable.grey_pin to ImageFactory.fromResource(R.drawable.grey_pin),
          R.drawable.pink_pin to ImageFactory.fromResource(R.drawable.pink_pin),
          R.drawable.base_pin to ImageFactory.fromResource(R.drawable.base_pin))
}
