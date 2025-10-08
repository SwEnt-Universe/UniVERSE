package com.android.universe

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * General smoke tests that verify:
 *  - Activity launches and renders the root container.
 *  - Home screen is present.
 *  - There's a clickable control to open the map (by tag), regardless of its text label or styling.
 *
 * These assertions avoid brittle checks (no hardcoded copy, no activity class names).
 */
@RunWith(AndroidJUnit4::class)
class MainActivityTest {

	@get:Rule
	val compose = createAndroidComposeRule<MainActivity>()

	@Test
	fun app_launches_and_shows_root_container() {
		compose
			.onNodeWithTag("main_screen_container", useUnmergedTree = true)
			.assertExists()
			.assertIsDisplayed()
	}

	@Test
	fun home_screen_is_present() {
		compose
			.onNodeWithTag("home_screen", useUnmergedTree = true)
			.assertExists()
			.assertIsDisplayed()
	}

	@Test
	fun has_clickable_control_to_open_map() {
		// Prefer the explicit control tag if it remains available
		compose
			.onNodeWithTag("open_map_button", useUnmergedTree = true)
			.assertExists()
			.assertIsDisplayed()
			.assertHasClickAction()
	}

	@Test
	fun home_screen_contains_a_clickable_descendant_even_if_button_tag_changes() {
		// Extra resilience: if you later rename/remove the specific button tag,
		// this still verifies the home screen exposes at least one clickable action.
		compose
			.onNode(
				hasTestTag("home_screen")
						and hasAnyDescendant(hasClickAction()),
				useUnmergedTree = true
			)
			.assertExists()
	}
}
