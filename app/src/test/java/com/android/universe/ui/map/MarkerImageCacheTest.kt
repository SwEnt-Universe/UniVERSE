package com.android.universe.ui.map

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.R
import com.android.universe.di.DefaultDP
import com.android.universe.utils.EventTestData
import com.tomtom.sdk.map.display.image.Image
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.reflect.Field

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class MarkerImageCacheTest {

  private val pins = EventTestData.categoryEvents.map { it.second }

  @Before
  fun setup() {
    // 1. Mock the Dispatcher Provider
    mockkObject(DefaultDP)
    every { DefaultDP.io } returns UnconfinedTestDispatcher()
    every { DefaultDP.default } returns UnconfinedTestDispatcher()

    mockkObject(MarkerImageCache)

    // 3. Reset the internal cache state before each test
    resetSingleton()
  }

  @After
  fun tearDown() {
    unmockkAll()
  }

  @Test
  fun `get initializes cache and returns correct image`() = runTest {
    val mockImage = mockk<Image>()
    var callCount = 0
    // Syntax: obj["privateMethodName"](args...) call function from object in mockk
    every { MarkerImageCache["fromResourceWrapper"](any<Int>()) } answers
        {
          callCount++
          mockImage
        }

    val result = MarkerImageCache.get(R.drawable.violet_pin_dark_mode)

    assertSame(mockImage, result)

    // Verify that the wrapper was called.
    // Note: loadImages() calls this for every pin in the map during initialization.
    // + 1 because the EventData doesnt load the basepin
    assertEquals(pins.size + 8, callCount)
  }

  @Test
  fun `get serves from cache on subsequent calls without recalling wrapper`() = runTest {
    val mockImage = mockk<Image>()
    var callCount = 0
    // Stub the private wrapper

    every { MarkerImageCache["fromResourceWrapper"](any<Int>()) } returns mockImage
    every { MarkerImageCache["fromResourceWrapper"](R.drawable.red_pin_dark_mode) } answers
        {
          callCount++
          mockImage
        }

    // Act 1: First call (Should hit"] }

    val result1 = MarkerImageCache.get(R.drawable.red_pin_dark_mode)

    // Act 2: Second call (Should hit cache -> NO wrapper call)
    val result2 = MarkerImageCache.get(R.drawable.red_pin_dark_mode)

    // Assert
    assertSame(mockImage, result1)
    assertSame(mockImage, result2)
    assertEquals(1, callCount)
  }

  @Test
  fun `get falls back to wrapper for unknown resource IDs`() = runTest {
    val unknownId = 99999
    var callCount = 0
    val fallbackImage = mockk<Image>()

    every { MarkerImageCache["fromResourceWrapper"](any<Int>()) } returns fallbackImage
    every { MarkerImageCache["fromResourceWrapper"](R.drawable.base_pin_light_mode) } answers
        {
          callCount++
          fallbackImage
        }

    // This will init the cache (if not done), check map (fail), and hit the Elvis operator ?:
    val result = MarkerImageCache.get(unknownId)

    assertSame(fallbackImage, result)

    // Verify it was called for the unknown ID
    assertEquals(2, callCount)
  }

  private fun resetSingleton() {
    val field: Field = MarkerImageCache::class.java.getDeclaredField("_cache")
    field.isAccessible = true
    field.set(MarkerImageCache, null)
  }
}
