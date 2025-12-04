package com.android.universe.model.image

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.android.universe.di.DispatcherProvider
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import java.io.ByteArrayInputStream

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class ImageBitmapManagerTest {

  private lateinit var context: Context
  private lateinit var uri: Uri
  private lateinit var imageBitmapManager: ImageBitmapManager
  private lateinit var testDispatcher: TestDispatcher

  @Before
  fun setup() {
    context = ApplicationProvider.getApplicationContext()

    uri = Uri.parse("content://com.android.universe/test_image.png")

    testDispatcher = StandardTestDispatcher()

    val testDispatcherProvider =
        object : DispatcherProvider {
          override val main: CoroutineDispatcher
            get() = testDispatcher

          override val default: CoroutineDispatcher
            get() = testDispatcher

          override val io: CoroutineDispatcher
            get() = testDispatcher

          override val unconfined: CoroutineDispatcher
            get() = testDispatcher
        }

    imageBitmapManager = ImageBitmapManager(context, testDispatcherProvider)
  }

  @Test
  fun resizeAndCompressImage_returnsNull_whenInputStreamCannotBeOpened() =
      runTest(testDispatcher) {
        val missingUri = Uri.parse("file:///android_asset/does_not_exist.png")

        val result = imageBitmapManager.resizeAndCompressImage(missingUri)

        assertNull(result)
      }

  @Test
  fun resizeAndCompressImage_returnsByteArray_whenImageIsValid() =
      runTest(testDispatcher) {
        val validPngBytes =
            byteArrayOf(
                0x89.toByte(),
                0x50.toByte(),
                0x4E.toByte(),
                0x47.toByte(),
                0x0D.toByte(),
                0x0A.toByte(),
                0x1A.toByte(),
                0x0A.toByte(),
                0x00.toByte(),
                0x00.toByte(),
                0x00.toByte(),
                0x0D.toByte(),
                0x49.toByte(),
                0x48.toByte(),
                0x44.toByte(),
                0x52.toByte(),
                0x00.toByte(),
                0x00.toByte(),
                0x00.toByte(),
                0x01.toByte(),
                0x00.toByte(),
                0x00.toByte(),
                0x00.toByte(),
                0x01.toByte(),
                0x08.toByte(),
                0x02.toByte(),
                0x00.toByte(),
                0x00.toByte(),
                0x00.toByte(),
                0x90.toByte(),
                0x77.toByte(),
                0x53.toByte(),
                0xDE.toByte(),
                0x00.toByte(),
                0x00.toByte(),
                0x00.toByte(),
                0x0C.toByte(),
                0x49.toByte(),
                0x44.toByte(),
                0x41.toByte(),
                0x54.toByte(),
                0x08.toByte(),
                0xD7.toByte(),
                0x63.toByte(),
                0xF8.toByte(),
                0xCF.toByte(),
                0xC0.toByte(),
                0x00.toByte(),
                0x00.toByte(),
                0x03.toByte(),
                0x01.toByte(),
                0x01.toByte(),
                0x00.toByte(),
                0x18.toByte(),
                0xDD.toByte(),
                0x8D.toByte(),
                0xB0.toByte(),
                0x00.toByte(),
                0x00.toByte(),
                0x00.toByte(),
                0x00.toByte(),
                0x49.toByte(),
                0x45.toByte(),
                0x4E.toByte(),
                0x44.toByte(),
                0xAE.toByte(),
                0x42.toByte(),
                0x60.toByte(),
                0x82.toByte())

        val inputStream = ByteArrayInputStream(validPngBytes)
        Shadows.shadowOf(context.contentResolver).registerInputStream(uri, inputStream)

        val result = imageBitmapManager.resizeAndCompressImage(uri)

        assertNotNull("Result should not be null for valid image", result)
      }
}
