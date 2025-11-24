package com.android.universe.ui.map


import android.graphics.Bitmap
import android.os.Handler
import android.view.PixelCopy
import android.view.Surface
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLSurfaceView
import android.view.TextureView
import android.view.ViewGroup
import android.widget.FrameLayout
import com.tomtom.sdk.map.display.ui.MapView
import android.os.Looper
import android.view.SurfaceView
import android.view.View
import androidx.compose.ui.platform.LocalContext
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tomtom.sdk.map.display.MapOptions
import junit.framework.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

class TwoLayerTestViewGroup(context: Context) : FrameLayout(context) {

    val childView = View(context)
    val textureView = TextureView(context)

    init {
        // First arbitrary child
        addView(childView.apply {
            layoutParams = LayoutParams(100, 100)
        })

        // Final rendering surface
        addView(textureView.apply {
            layoutParams = LayoutParams(200, 200)
        })
    }

    fun setTextureSize(width: Int, height: Int) {
        textureView.layoutParams = LayoutParams(width, height)
        textureView.measure(
            MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        )
        textureView.layout(0, 0, width, height)
    }
}

@RunWith(AndroidJUnit4::class)
class MapScreenHelperTest {

    val view = MapView(ApplicationProvider.getApplicationContext(), MapOptions(mapKey = "test", renderToTexture = true))

    val frame = FrameLayout(ApplicationProvider.getApplicationContext())

    @Test
    fun `findRenderingView returns null when no TextureView exists`() {
        val emptyRoot = frame

        val result = emptyRoot.findRenderingView()
        val resultView = view.getRendererView()

        assertNull(result)
        assertNull(resultView)
    }


    @Test
    fun `findRenderingView finds TextureView inside nested group`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val root = TwoLayerTestViewGroup(context)

        val renderer = root.findRenderingView()

        assertNotNull(renderer)
        assertTrue(renderer is TextureView)
    }



    @Test
    fun `takeSnapshot returns null when renderer is missing`() {
        val fake = view
        fake.removeAllViews()

        var result: Bitmap? = null

        fake.takeSnapshot { result = it }

        assertNull(result)
    }

}