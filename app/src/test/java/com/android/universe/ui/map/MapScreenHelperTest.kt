package com.android.universe.ui.map

import android.content.Context
import android.graphics.Bitmap
import android.view.TextureView
import android.view.View
import android.widget.FrameLayout
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tomtom.sdk.map.display.MapOptions
import com.tomtom.sdk.map.display.ui.MapView
import junit.framework.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

class TwoLayerTestViewGroup(context: Context) : FrameLayout(context) {

  val childView = View(context)
  val textureView = TextureView(context)

  init {
    // First arbitrary child
    addView(childView.apply { layoutParams = LayoutParams(100, 100) })

    // Final rendering surface
    addView(textureView.apply { layoutParams = LayoutParams(200, 200) })
  }
}

@RunWith(AndroidJUnit4::class)
class MapScreenHelperTest {

  val view =
      MapView(
          ApplicationProvider.getApplicationContext(),
          MapOptions(mapKey = "test", renderToTexture = true))

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
