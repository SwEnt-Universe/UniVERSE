package com.android.universe.background

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.scale
import com.android.universe.R
import com.android.universe.di.DefaultDP
import com.android.universe.ui.theme.Dimensions
import kotlinx.coroutines.withContext

/**
 * Holds the background image used across the app (typically a blurred map snapshot).
 *
 * This repository keeps the most recent background **in memory only**, exposed as a Compose state
 * so that all screens automatically recompose when the value changes.
 *
 * ## What this repository does
 * - Provides a **default background** when the app starts (from a drawable resource).
 * - Stores a **dynamic snapshot** taken from the Map screen when the user leaves it.
 * - Makes the background available to all screens through `currentSnapshot`.
 *
 * ## What this repository does not do
 * - It does not persist the snapshot to disk.
 * - It does not apply blur, callers may do so before passing the bitmap.
 *
 * ## Lifecycle expectations
 * - On cold start, `loadInitialSnapshot()` should be called once to set the fallback background.
 * - When leaving the map, `updateSnapshot(bitmap)` should be called to update the in-memory value.
 * - Any Composable that reads `currentSnapshot` will recompose automatically.
 */
object BackgroundSnapshotRepository {

  /**
   * The current background image.
   * - `null` until the app loads the fallback snapshot
   * - Updated when the Map screen provides a new snapshot
   *
   * Exposed as Compose state so screens recompose on change.
   */
  var currentSnapshot by mutableStateOf<ImageBitmap?>(null)

  /**
   * Loads the fallback background image from resources. Scaled Does nothing if a snapshot is
   * already set.
   *
   * Should be called once during app initialization.
   */
  fun loadInitialSnapshot(context: Context) {
    if (currentSnapshot != null) return
    val bmp = BitmapFactory.decodeResource(context.resources, R.drawable.map_snapshot2)
    if (bmp != null) {
      currentSnapshot =
          bmp.scale(
                  (bmp.width * Dimensions.ImageScale).toInt(),
                  (bmp.height * Dimensions.ImageScale).toInt())
              .asImageBitmap()
    }
  }

  /**
   * Updates the current in-memory background snapshot. The UI will recompose wherever
   * `currentSnapshot` is used.
   *
   * @param bitmap A snapshot created by the Map screen (may be pre-processed before calling).
   */
  suspend fun updateSnapshot(bitmap: Bitmap) {
    withContext(DefaultDP.default) {
      val processed = bitmap.asImageBitmap()

      withContext(DefaultDP.main) { currentSnapshot = processed }
    }
  }
}
