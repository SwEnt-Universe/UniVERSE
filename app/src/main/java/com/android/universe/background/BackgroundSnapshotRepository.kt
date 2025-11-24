package com.android.universe.background

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.android.universe.R

/**
 * A lightweight in-memory repository responsible for storing and providing the background image
 * used across the app (e.g., blurred map snapshot).
 *
 * ## Purpose
 * The repository serves two roles:
 * 1. **Default background** When the user has not yet visited the Map screen, the UI needs a
 *    default fallback background. This is provided by loading a static image from resources, e.g.
 *    `R.drawable.map_snapshot2`.
 * 2. **Dynamic background from Map snapshots** Once the user interacts with the Map screen and
 *    navigates away, a snapshot is taken and stored here. Every screen observing this repository
 *    automatically updates to display the new snapshot as its backdrop.
 *
 * ## Why Compose State?
 * `currentSnapshot` is backed by `mutableStateOf`, meaning:
 * - All screens recomposing when this value changes update instantly.
 * - No manual state management or LiveData/Flow required.
 *
 * ## Persistence
 * This object **does not persist** snapshots to disk. It only keeps them in memory during the app's
 * lifecycle. The expectation is:
 * - If the app cold-starts → load fallback image.
 * - After Map screen updates snapshot → use the latest snapshot.
 *
 * If persistent storage is needed, this repository could be extended later.
 */
object BackgroundSnapshotRepository {

  /**
   * The current background snapshot exposed as a Compose state.
   * - `null` initially (before anything is loaded)
   * - Set to a default image via [loadInitialSnapshot] on first app launch
   * - Updated dynamically through [updateSnapshot] whenever the map generates a new snapshot
   *
   * Any composable reading this value will automatically recompose on change.
   */
  var currentSnapshot by mutableStateOf<ImageBitmap?>(null)

  /**
   * Loads the default background image **only if** a snapshot is not already set.
   *
   * Should typically be called once in `MainActivity` or early in app startup.
   *
   * @param context Application context, needed to decode drawable resources.
   */
  fun loadInitialSnapshot(context: Context) {
    if (currentSnapshot != null) return
    val bmp = BitmapFactory.decodeResource(context.resources, R.drawable.map_snapshot2)
    if (bmp != null) {
      currentSnapshot = bmp.asImageBitmap()
      return
    }
  }

  /**
   * Replaces the current snapshot with a new bitmap.
   *
   * This is called from the Map screen whenever a fresh snapshot is captured. The UI observing
   * [currentSnapshot] will recompose immediately.
   *
   * @param bitmap The new map snapshot to use as background.
   */
  fun updateSnapshot(bitmap: Bitmap) {
    currentSnapshot = bitmap.asImageBitmap()
  }
}
