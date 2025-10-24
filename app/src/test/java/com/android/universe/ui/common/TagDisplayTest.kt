package com.android.universe.ui.common

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalLayoutApi::class)
@RunWith(AndroidJUnit4::class)
class TagGroupTest {

  @get:Rule val composeTestRule = createComposeRule()

    companion object {
        private val sampleTags = listOf("Reading", "Running", "Music")
    }

  @Test
  fun displaysTitle_whenNameIsProvided() {
    composeTestRule.setContent {
      TagGroup(name = "Interests", tagList = sampleTags, selectedTags = emptyList())
    }

    composeTestRule.onNodeWithText("Interests").assertIsDisplayed()
  }

  @Test
  fun doesNotDisplayTitle_whenNameIsEmpty() {
    composeTestRule.setContent {
      TagGroup(name = "", tagList = sampleTags, selectedTags = emptyList())
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
          onTagSelect = { selectedTag = it })
    }

    composeTestRule.onNodeWithText("Running").performClick()
    assertEquals("Running", selectedTag)
  }

  @Test
  fun clickingSelectedTag_callsOnTagReSelect() {
    var reselectedTag: String? = null

    composeTestRule.setContent {
      TagGroup(
          name = "Test",
          tagList = sampleTags,
          selectedTags = listOf("Running"),
          onTagReSelect = { reselectedTag = it })
    }

    composeTestRule.onNodeWithText("Running").performClick()
    assertEquals("Running", reselectedTag)
  }

  @Test
  fun selectedTag_showsCheckIcon() {
    composeTestRule.setContent {
      TagGroup(name = "Test", tagList = sampleTags, selectedTags = listOf("Reading"))
    }

    // Verify that the icon appears for the selected tag
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
          })
    }

    // Select "Reading"
    composeTestRule.onNodeWithText("Reading").performClick()
    assertEquals("Reading", lastSelected )
    assertTrue("Reading" in selectedTags)

    // Deselect "Reading"
    composeTestRule.onNodeWithText("Reading").performClick()
    assertEquals("Reading", lastReselected )
    assertTrue("Reading" !in selectedTags)
  }
}
