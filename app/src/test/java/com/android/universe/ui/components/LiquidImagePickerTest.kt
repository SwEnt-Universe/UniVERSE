package com.android.universe.ui.components

import android.graphics.Bitmap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.ui.components.LiquidImagePickerDefaults.AddImage
import com.android.universe.ui.components.LiquidImagePickerDefaults.DeleteImage
import com.android.universe.ui.components.LiquidImagePickerDefaults.EditImage
import com.android.universe.ui.components.LiquidImagePickerDefaults.NoImage
import com.android.universe.ui.components.LiquidImagePickerDefaults.SelectedImage
import com.android.universe.ui.theme.UniverseTheme
import com.android.universe.utils.setContentWithStubBackdrop
import java.io.ByteArrayOutputStream
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LiquidImagePickerTest {

  @get:Rule val composeTestRule = createComposeRule()

  @OptIn(ExperimentalCoroutinesApi::class) private val testDispatcher = UnconfinedTestDispatcher()

  @Test
  fun liquidImagePicker_displaysPlaceholder_whenImageBytesIsNull() {
    composeTestRule.setContentWithStubBackdrop {
      UniverseTheme {
        LiquidImagePicker(imageBytes = null, onPickImage = {}, dispatcher = testDispatcher)
      }
    }

    composeTestRule.onNodeWithContentDescription(NoImage).assertIsDisplayed()
    composeTestRule.onNodeWithContentDescription(AddImage).assertIsDisplayed()
    composeTestRule.onNodeWithContentDescription(DeleteImage).assertDoesNotExist()
  }

  @Test
  fun liquidImagePicker_displaysImage_whenImageBytesIsProvided() {
    val validBytes =
        ByteArrayOutputStream()
            .apply {
              val bitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
              bitmap.eraseColor(android.graphics.Color.RED)
              bitmap.compress(Bitmap.CompressFormat.PNG, 100, this)
            }
            .toByteArray()

    composeTestRule.setContentWithStubBackdrop {
      UniverseTheme {
        LiquidImagePicker(imageBytes = validBytes, onPickImage = {}, dispatcher = testDispatcher)
      }
    }

    composeTestRule.onNodeWithContentDescription(SelectedImage).assertIsDisplayed()
    composeTestRule.onNodeWithContentDescription(EditImage).assertIsDisplayed()
    composeTestRule.onNodeWithContentDescription(DeleteImage).assertIsDisplayed()
  }

  @Test
  fun liquidImagePicker_invokesCallback_whenClicker() {
    var isClicked = false

    composeTestRule.setContentWithStubBackdrop {
      UniverseTheme {
        LiquidImagePicker(
            imageBytes = null, onPickImage = { isClicked = true }, dispatcher = testDispatcher)
      }
    }

    composeTestRule.onNodeWithContentDescription(AddImage).performClick()
    assertTrue("Expected onPickImage callback to be triggered", isClicked)
  }

  @Test
  fun liquidImagePicker_invokesDeleteCallback_whenDeleteClicked() {
    var isDeleteClicked = false
    val validBytes =
        ByteArrayOutputStream()
            .apply {
              val bitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
              bitmap.eraseColor(android.graphics.Color.RED)
              bitmap.compress(Bitmap.CompressFormat.PNG, 100, this)
            }
            .toByteArray()

    composeTestRule.setContentWithStubBackdrop {
      UniverseTheme {
        LiquidImagePicker(
            imageBytes = validBytes,
            onPickImage = {},
            onDeleteImage = { isDeleteClicked = true },
            dispatcher = testDispatcher)
      }
    }

    composeTestRule.onNodeWithContentDescription(DeleteImage).performClick()
    assertTrue("Expected onDeleteImage callback to be triggered", isDeleteClicked)
  }
}
