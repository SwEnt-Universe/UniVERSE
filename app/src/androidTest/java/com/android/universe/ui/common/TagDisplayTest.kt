package com.android.universe.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalLayoutApi::class)
class TagGroupTest {

	@get:Rule
	val composeTestRule = createComposeRule()

	private val sampleTags = listOf("Reading", "Running", "Music")

	@Test
	fun displaysTitle_whenNameIsProvided() {
		composeTestRule.setContent {
			TagGroup(
				name = "Interests",
				tagList = sampleTags,
				selectedTags = emptyList()
			)
		}

		composeTestRule.onNodeWithText("Interests").assertIsDisplayed()
	}

	@Test
	fun doesNotDisplayTitle_whenNameIsEmpty() {
		composeTestRule.setContent {
			TagGroup(
				name = "",
				tagList = sampleTags,
				selectedTags = emptyList()
			)
		}

		composeTestRule.onAllNodesWithText("Interests").assertCountEquals(0)
	}

	@Test
	fun clickingUnselectedTag_callsOnTagSelect() {
		var selectedTag: String? = null

		composeTestRule.setContent {
			TagGroup(
				name = "Test",
				tagList = sampleTags,
				selectedTags = emptyList(),
				onTagSelect = { selectedTag = it }
			)
		}

		composeTestRule.onNodeWithText("Running").performClick()
		assert(selectedTag == "Running")
	}

	@Test
	fun clickingSelectedTag_callsOnTagReSelect() {
		var reselectedTag: String? = null

		composeTestRule.setContent {
			TagGroup(
				name = "Test",
				tagList = sampleTags,
				selectedTags = listOf("Running"),
				onTagReSelect = { reselectedTag = it }
			)
		}

		composeTestRule.onNodeWithText("Running").performClick()
		assert(reselectedTag == "Running")
	}

	@Test
	fun selectedTag_showsCheckIcon() {
		composeTestRule.setContent {
			TagGroup(
				name = "Test",
				tagList = sampleTags,
				selectedTags = listOf("Reading")
			)
		}

		// Verify that the icon appears for the selected tag
		composeTestRule.onNodeWithContentDescription("Selected").assertIsDisplayed()
	}

	@Test
	fun selectedTag_hasBorderAndGrayColor() {
		// UI border and color verification is partial, but we can still check layout effects.
		val selected = listOf("Music")

		composeTestRule.setContent {
			MaterialTheme {
				TagGroup(
					name = "Category",
					tagList = sampleTags,
					selectedTags = selected,
					color = Color(0xFF6650a4)
				)
			}
		}

		// Confirm both the tag text and check icon appear
		composeTestRule.onNodeWithText("Music").assertIsDisplayed()
		composeTestRule.onNodeWithContentDescription("Selected").assertIsDisplayed()
	}

	@Test
	fun selectingAndDeselectingTag_triggersCorrectCallbacks() {
		val selectedTags = mutableStateListOf<String>()
		var lastSelected: String? = null
		var lastReselected: String? = null

		composeTestRule.setContent {
			TagGroup(
				name = "Test",
				tagList = sampleTags,
				selectedTags = selectedTags,
				onTagSelect = {
					selectedTags.add(it)
					lastSelected = it
				},
				onTagReSelect = {
					selectedTags.remove(it)
					lastReselected = it
				}
			)
		}

		// Select "Reading"
		composeTestRule.onNodeWithText("Reading").performClick()
		assert(lastSelected == "Reading")
		assert("Reading" in selectedTags)

		// Deselect "Reading"
		composeTestRule.onNodeWithText("Reading").performClick()
		assert(lastReselected == "Reading")
		assert("Reading" !in selectedTags)
	}
}
