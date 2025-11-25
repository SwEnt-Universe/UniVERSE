package com.android.universe.ui.common

import android.graphics.Bitmap
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.utils.setContentWithStubBackdrop
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainContainerTest {

  @get:Rule val composeTestRule = createComposeRule()

  private fun createTestImageBitmap(): ImageBitmap {
    val bmp = Bitmap.createBitmap(300, 100, Bitmap.Config.ARGB_8888)
    return bmp.asImageBitmap()
  }

  companion object {
    const val HELLOTEXT = "Hello Universe"
    const val ALIGNEDITEM = "Aligned Item"
  }

  val bitmap = createTestImageBitmap()

  @Test
  fun universeBackground_displaysImage() {
    val img = bitmap

    composeTestRule.setContentWithStubBackdrop {
      UniverseBackground(bitmap = img, modifier = Modifier)
    }

    composeTestRule
        .onNodeWithContentDescription(CommonBackground.BACKGROUNDTEXT)
        .assertIsDisplayed()
  }

  @Test
  fun universeBackgroundContainer_displaysBackgroundAndContent() {
    val img = bitmap

    composeTestRule.setContentWithStubBackdrop {
      UniverseBackgroundContainer(bitmap = img, contentAlignment = Alignment.Center) {
        Text(HELLOTEXT)
      }
    }

    // Background is present
    composeTestRule
        .onNodeWithContentDescription(CommonBackground.BACKGROUNDTEXT)
        .assertIsDisplayed()

    // Inner content exists
    composeTestRule.onNodeWithText(HELLOTEXT).assertIsDisplayed()
  }

  @Test
  fun universeBackgroundContainer_customAlignmentDoesNotCrash() {
    val img = bitmap

    composeTestRule.setContentWithStubBackdrop {
      UniverseBackgroundContainer(bitmap = img, contentAlignment = Alignment.BottomEnd) {
        Text(ALIGNEDITEM)
      }
    }

    composeTestRule.onNodeWithText(ALIGNEDITEM).assertExists()
  }
}
