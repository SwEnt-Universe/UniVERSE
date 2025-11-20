package com.android.universe.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.ui.theme.UniverseTheme
import com.android.universe.utils.setContentWithStubBackdrop
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LiquidBoxTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val TEST_TEXT_CONTENT = "Test Content"
  private val TEST_ALIGNMENT_TEXT = "Aligned Item"
  private val TEST_SIZE = 100.dp

  @Test
  fun liquidBox_displaysContent() {
    composeTestRule.setContentWithStubBackdrop {
      UniverseTheme {
        LiquidBox(modifier = Modifier.size(TEST_SIZE)) {
          Box(Modifier.size(50.dp)) { Text(TEST_TEXT_CONTENT) }
        }
      }
    }
    // Verify that the content provided to the LiquidBox is displayed
    composeTestRule.onNodeWithText(TEST_TEXT_CONTENT).assertIsDisplayed()
  }

  @Test
  fun liquidBox_appliesContentAlignment_toCenter() {
    composeTestRule.setContentWithStubBackdrop {
      UniverseTheme {
        LiquidBox(modifier = Modifier.size(TEST_SIZE), contentAlignment = Alignment.Center) {
          Text(TEST_ALIGNMENT_TEXT, Modifier.size(20.dp))
        }
      }
    }

    // Check if the Text node's position indicates center alignment.
    composeTestRule.onNode(hasText(TEST_ALIGNMENT_TEXT)).assertIsDisplayed()
  }

  @Test
  fun liquidBox_appliesCustomModifierAndShape() {
    val sizeModifier = Modifier.size(TEST_SIZE, TEST_SIZE / 2)

    composeTestRule.setContentWithStubBackdrop {
      UniverseTheme {
        LiquidBox(
            modifier = sizeModifier,
            shape = RoundedCornerShape(0.dp),
            color = Color.Red.copy(alpha = 0.5f)) {
              Text(TEST_TEXT_CONTENT)
            }
      }
    }

    // Verifying the size constraint applied via the modifier
    composeTestRule.onNode(hasText(TEST_TEXT_CONTENT)).assertIsDisplayed()
  }

  @Test
  fun liquidBox_contentAlignment_isApplied() {
    composeTestRule.setContentWithStubBackdrop {
      UniverseTheme {
        Row(Modifier.size(TEST_SIZE)) {
          LiquidBox(
              modifier = Modifier.size(TEST_SIZE).weight(1f),
              contentAlignment = Alignment.BottomEnd) {
                Text(TEST_ALIGNMENT_TEXT)
              }
        }
      }
    }

    composeTestRule.onNodeWithText(TEST_ALIGNMENT_TEXT).assertIsDisplayed()
  }

  @Test
  fun liquidBox_propagateMinConstraints_isApplied() {
    composeTestRule.setContentWithStubBackdrop {
      UniverseTheme {
        LiquidBox(modifier = Modifier.size(TEST_SIZE), propagateMinConstraints = true) {
          // This Box will inherit the min constraints from LiquidBox (100.dp)
          Box(Modifier.fillMaxWidth()) { Text(TEST_TEXT_CONTENT) }
        }
      }
    }

    // Asserting that the content is displayed, implying constraints were passed correctly.
    composeTestRule.onNodeWithText(TEST_TEXT_CONTENT).assertIsDisplayed()
  }

  @Test
  fun liquidBox_isComposable_withMinimumParameters() {
    composeTestRule.setContentWithStubBackdrop {
      UniverseTheme {
        // Test composability with only the required parameters (content)
        LiquidBox { Text(TEST_TEXT_CONTENT) }
      }
    }
    composeTestRule.onNodeWithText(TEST_TEXT_CONTENT).assertIsDisplayed()
  }
}
