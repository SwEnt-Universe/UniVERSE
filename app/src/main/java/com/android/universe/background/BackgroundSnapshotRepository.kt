package com.android.universe.background

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import com.android.universe.R

//TODO MAKE BETTER AND DOC
object BackgroundSnapshotRepository {

    // A Compose-friendly state. UI reacts automatically.
    var currentSnapshot by mutableStateOf<ImageBitmap?>(null)


    fun loadInitialSnapshot(context: Context) {
        if(currentSnapshot != null) return
        val bmp = BitmapFactory.decodeResource(context.resources, R.drawable.map_snapshot2)
        if (bmp != null) {
            currentSnapshot = bmp.asImageBitmap()
            return
        }
    }

    fun updateSnapshot(bitmap: Bitmap) {
        // Update in-memory
        currentSnapshot = bitmap.asImageBitmap()
    }
}
