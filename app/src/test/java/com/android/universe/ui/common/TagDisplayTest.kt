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
import com.android.universe.model.Tag
import com.android.universe.model.Tag.Category
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
    private val READING = Tag.READING.displayName
    private val RUNNING = Tag.RUNNING.displayName
    private val MUSIC = Tag.MUSIC.displayName
    private val INTERESTS = Category.INTEREST.displayName
    private val sampleTags = listOf(READING, RUNNING, MUSIC)

    private const val SELECTED = "Selected"
  }

  @Test
  fun displaysTitle_whenNameIsProvided() {
    composeTestRule.setContent {
      TagGroup(name = INTERESTS, tagList = sampleTags, selectedTags = emptyList())
    }

    composeTestRule.onNodeWithText(INTERESTS).assertIsDisplayed()
  }

  @Test
  fun doesNotDisplayTitle_whenNameIsEmpty() {
    composeTestRule.setContent {
      TagGroup(name = "", tagList = sampleTags, selectedTags = emptyList())
    }

    composeTestRule.onAllNodesWithText(INTERESTS).assertCountEquals(0)
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

    composeTestRule.onNodeWithText(RUNNING).performClick()
    assertEquals(RUNNING, selectedTag)
  }

  @Test
  fun clickingSelectedTag_callsOnTagReSelect() {
    var reselectedTag: String? = null

    composeTestRule.setContent {
      TagGroup(
          name = "Test",
          tagList = sampleTags,
          selectedTags = listOf(RUNNING),
          onTagReSelect = { reselectedTag = it })
    }

    composeTestRule.onNodeWithText(RUNNING).performClick()
    assertEquals(RUNNING, reselectedTag)
  }

  @Test
  fun selectedTag_showsCheckIcon() {
    composeTestRule.setContent {
      TagGroup(name = "Test", tagList = sampleTags, selectedTags = listOf(READING))
    }

    // Verify that the icon appears for the selected tag
    composeTestRule.onNodeWithContentDescription(SELECTED).assertIsDisplayed()
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
    composeTestRule.onNodeWithText(READING).performClick()
    assertEquals(READING, lastSelected)
    assertTrue(READING in selectedTags)

    // Deselect "Reading"
    composeTestRule.onNodeWithText(READING).performClick()
    assertEquals(READING, lastReselected)
    assertTrue(READING !in selectedTags)
  }
}
