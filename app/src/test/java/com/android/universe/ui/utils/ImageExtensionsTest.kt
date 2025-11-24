package com.android.universe.ui.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class ImageExtensionsTest {

  @Before
  fun setup() {
    mockkStatic(BitmapFactory::class)
    mockkStatic(Log::class)

    // Mock the Compose extension function file where .asImageBitmap() lives.
    mockkStatic("androidx.compose.ui.graphics.AndroidImageBitmap_androidKt")

    // Stub Log.e to do nothing (return 0) so it doesn't crash the test,
    // but we won't verify it was called.
    every { Log.e(any(), any(), any()) } returns 0
  }

  @After
  fun tearDown() {
    unmockkAll()
  }

  @Test
  fun toImageBitmap_returnsImageBitmap_whenDecodingSucceeds() = runTest {
    val validBytes = byteArrayOf(0x00, 0x01, 0x02)
    val mockAndroidBitmap = mockk<Bitmap>()
    val mockComposeBitmap = mockk<ImageBitmap>()

    every { BitmapFactory.decodeByteArray(validBytes, 0, validBytes.size) } returns
        mockAndroidBitmap

    every { mockAndroidBitmap.asImageBitmap() } returns mockComposeBitmap

    val result = validBytes.toImageBitmap()

    assertEquals(mockComposeBitmap, result)
  }

  @Test
  fun toImageBitmap_returnsNull_whenDecodingFails() = runTest {
    val corruptBytes = byteArrayOf(0xFF.toByte())

    every { BitmapFactory.decodeByteArray(any(), any(), any()) } returns null

    val result = corruptBytes.toImageBitmap()

    assertNull(result)
  }

  @Test
  fun toImageBitmap_returnsNull_whenExceptionIsThrown() = runTest {
    val bytes = byteArrayOf(0x00)

    every { BitmapFactory.decodeByteArray(any(), any(), any()) } throws
        RuntimeException("Decoding crashed")

    val result = bytes.toImageBitmap()

    assertNull(result)
  }
}
