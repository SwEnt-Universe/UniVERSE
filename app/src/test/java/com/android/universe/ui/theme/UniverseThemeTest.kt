package com.android.universe.ui.theme

import android.app.Application
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Robolectric-based unit tests for [UniverseTheme] and [tagColor].
 *
 * These tests are JVM-safe and achieve full line coverage by exercising:
 *  - light/dark + dynamic/static theme branches
 *  - all tag categories with isSelected/darkTheme variations
 *
 * No hardcoded color values are used — we only verify that valid, non-null colors are produced.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class UniverseThemeTest {

	@get:Rule
	val composeTestRule = createComposeRule()

	private val context = ApplicationProvider.getApplicationContext<Application>()

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
		composeTestRule.setContent {
			val light = tagColor(category = "INTEREST", isSelected = false, darkTheme = false)
			val dark = tagColor(category = "INTEREST", isSelected = false, darkTheme = true)
			assertNotEquals(light, dark)
		}
	}

	@Test
	fun tagColor_returnsSelectedColorWhenSelected() {
		composeTestRule.setContent {
			val normal = tagColor(category = "SPORT", isSelected = false, darkTheme = false)
			val selected = tagColor(category = "SPORT", isSelected = true, darkTheme = false)
			assertNotEquals(normal, selected)
		}
	}

	@Test
	fun tagColor_returnsColorsForAllCategoriesAndStates() {
		val categories = listOf("INTEREST", "SPORT", "MUSIC", "TRANSPORT", "CANTON", "UNKNOWN")
		composeTestRule.setContent {
			categories.forEach { category ->
				val lightNormal = tagColor(category, false, false)
				val darkNormal = tagColor(category, false, true)
				val lightSelected = tagColor(category, true, false)
				val darkSelected = tagColor(category, true, true)

				assertNotNull(lightNormal)
				assertNotNull(darkNormal)
				assertNotNull(lightSelected)
				assertNotNull(darkSelected)

				// all colors should be valid
				listOf(lightNormal, darkNormal, lightSelected, darkSelected).forEach {
					assertTrue(it != Color.Unspecified)
				}
			}
		}
	}
}
