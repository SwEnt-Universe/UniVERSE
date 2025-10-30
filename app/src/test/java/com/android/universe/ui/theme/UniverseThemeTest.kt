package com.android.universe.ui.theme

import androidx.annotation.VisibleForTesting
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Robolectric-based unit tests for [UniverseTheme] and [tagColor].
 *
 * These tests are JVM-safe and achieve full line coverage by exercising:
 * - light/dark + dynamic/static theme branches
 * - all tag categories with isSelected/darkTheme variations
 *
 * No hardcoded color values are used — we only verify that valid, non-null colors are produced.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class UniverseThemeTest {

  @get:Rule val composeTestRule = createComposeRule()

  // ────────────────────────────────────────────────
  // Helper: Inject dark/light theme into composition
  // ────────────────────────────────────────────────

  @VisibleForTesting
  @Composable
  fun tagColorForTest(category: String, isSelected: Boolean, darkTheme: Boolean): Color {
    var color: Color = Color.Unspecified
    CompositionLocalProvider(LocalIsDarkTheme provides darkTheme) {
      color = tagColor(category, isSelected)
    }
    return color
  }

  // ────────────────────────────────────────────────
  // THEME TESTS
  // ────────────────────────────────────────────────

  @Test
  fun universeTheme_appliesLightThemeWithoutDynamicColors() {
    composeTestRule.setContent {
      UniverseTheme(darkTheme = false, dynamicColor = false) {
        val colors = MaterialTheme.colorScheme
        assertNotNull(colors)
        assertNotEquals(Color.Unspecified, colors.primary)
        assertNotEquals(Color.Unspecified, colors.background)
      }
    }
  }

  @Test
  fun universeTheme_appliesDarkThemeWithoutDynamicColors() {
    composeTestRule.setContent {
      UniverseTheme(darkTheme = true, dynamicColor = false) {
        val colors = MaterialTheme.colorScheme
        assertNotNull(colors)
        assertNotEquals(Color.Unspecified, colors.primary)
        assertNotEquals(Color.Unspecified, colors.background)
      }
    }
  }

  @Test
  fun universeTheme_appliesLightThemeWithDynamicColors() {
    composeTestRule.setContent {
      UniverseTheme(darkTheme = false, dynamicColor = true) {
        val colors = MaterialTheme.colorScheme
        assertNotNull(colors)
        assertNotEquals(Color.Unspecified, colors.primary)
      }
    }
  }

  @Test
  fun universeTheme_appliesDarkThemeWithDynamicColors() {
    composeTestRule.setContent {
      UniverseTheme(darkTheme = true, dynamicColor = true) {
        val colors = MaterialTheme.colorScheme
        assertNotNull(colors)
        assertNotEquals(Color.Unspecified, colors.primary)
      }
    }
  }

  // ────────────────────────────────────────────────
  // TAG COLOR TESTS
  // ────────────────────────────────────────────────

  @Test
  fun tagColor_returnsDifferentColorsForLightAndDarkThemes() {
    var light: Color? = null
    var dark: Color? = null

    composeTestRule.setContent {
      light = tagColorForTest(category = "INTEREST", isSelected = false, darkTheme = false)
      dark = tagColorForTest(category = "INTEREST", isSelected = false, darkTheme = true)
    }

    assertNotNull(light)
    assertNotNull(dark)
    assertNotEquals(light, dark)
  }

  @Test
  fun tagColor_returnsColorsForAllCategoriesAndStates() {
    val categories = listOf("INTEREST", "SPORT", "MUSIC", "TRANSPORT", "CANTON", "UNKNOWN")
    composeTestRule.setContent {
      categories.forEach { category ->
        val lightNormal = tagColorForTest(category, false, false)
        val darkNormal = tagColorForTest(category, false, true)
        val lightSelected = tagColorForTest(category, true, false)
        val darkSelected = tagColorForTest(category, true, true)

        assertNotNull(lightNormal)
        assertNotNull(darkNormal)
        assertNotNull(lightSelected)
        assertNotNull(darkSelected)

        listOf(lightNormal, darkNormal, lightSelected, darkSelected).forEach {
          assertTrue(it != Color.Unspecified)
        }
      }
    }
  }

  @Test
  fun tagColor_returnsSelectedColorWhenSelected() {
    var normal: Color? = null
    var selected: Color? = null

    composeTestRule.setContent {
      normal = tagColorForTest(category = "SPORT", isSelected = false, darkTheme = false)
      selected = tagColorForTest(category = "SPORT", isSelected = true, darkTheme = false)
    }

    assertNotNull(normal)
    assertNotNull(selected)
    assertNotEquals(normal, selected)
  }
}
