package com.android.universe.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.utils.setContentWithStubBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LiquidButtonTest {

  companion object {
    const val RANDOM_STRING = "random_string"
  }

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun liquidButtonFullParams() {
    var bool = false
    composeTestRule.setContentWithStubBackdrop {
      MaterialTheme {
        LiquidButton(
            onClick = { bool = true },
            modifier = Modifier.height(100.dp).testTag(LiquidButtonTestTags.LIQUID_BUTTON),
            isInteractive = true,
            tint = MaterialTheme.colorScheme.primary,
            surfaceColor = MaterialTheme.colorScheme.secondary) {
              Text(RANDOM_STRING)
            }
      }
    }
    composeTestRule.onNodeWithText(RANDOM_STRING).assertExists()
    composeTestRule.onNodeWithTag(LiquidButtonTestTags.LIQUID_BUTTON).assertExists()
    composeTestRule.onNodeWithTag(LiquidButtonTestTags.LIQUID_BUTTON).assertIsEnabled()
    composeTestRule.onNodeWithTag(LiquidButtonTestTags.LIQUID_BUTTON).performClick()
    assertEquals(true, bool)
  }

  @Test
  fun liquidMinParams() {
    composeTestRule.setContentWithStubBackdrop {
      MaterialTheme {
        LiquidButton(onClick = {}, isInteractive = false, modifier = Modifier.testTag(
          LiquidButtonTestTags.LIQUID_BUTTON)) {
          Text(RANDOM_STRING)
        }
      }
    }
    composeTestRule.onNodeWithText(RANDOM_STRING).assertExists()
    composeTestRule.onNodeWithTag(LiquidButtonTestTags.LIQUID_BUTTON).assertExists()
  }
}
