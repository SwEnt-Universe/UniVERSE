package com.android.universe.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ImageDisplayTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val validPngBytes =
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

  @Test
  fun imageDisplay_showsDefaultImage_whenImageIsNull() {
    val defaultImageId = android.R.drawable.ic_menu_camera
    val contentDesc = "Default Profile Picture"

    composeTestRule.setContent {
      ImageDisplay(image = null, defaultImageId = defaultImageId, contentDescription = contentDesc)
    }

    composeTestRule.onNodeWithContentDescription(contentDesc).assertIsDisplayed()
  }

  @Test
  fun imageDisplay_showsBitmap_whenImageIsNotNull() {
    val defaultImageId = android.R.drawable.ic_menu_camera
    val contentDesc = "User Profile Picture"

    composeTestRule.setContent {
      ImageDisplay(
          image = validPngBytes, defaultImageId = defaultImageId, contentDescription = contentDesc)
    }

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithContentDescription(contentDesc).assertIsDisplayed()
  }
}
