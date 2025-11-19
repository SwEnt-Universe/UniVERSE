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
import com.android.universe.model.tag.Tag
import com.android.universe.model.tag.Tag.Category
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
    private val READING = Tag.LITERATURE
    private val RUNNING = Tag.RUNNING
    private val MUSIC = Tag.MUSIC
    private val TOPICS = Category.TOPIC
    private val sampleTags = listOf(READING, RUNNING, MUSIC)

    private const val SELECTED = "Selected"
  }

  @Test
  fun displaysTitle_whenNameIsProvided() {
    composeTestRule.setContent {
      TagGroup(name = TOPICS.displayName, tagList = sampleTags, selectedTags = emptyList())
    }

    composeTestRule.onNodeWithText(TOPICS.displayName).assertIsDisplayed()
  }

  @Test
  fun doesNotDisplayTitle_whenNameIsEmpty() {
    composeTestRule.setContent {
      TagGroup(name = "", tagList = sampleTags, selectedTags = emptyList())
    }

    composeTestRule.onAllNodesWithText(TOPICS.displayName).assertCountEquals(0)
  }

  @Test
  fun clickingUnselectedTag_callsOnTagSelect() {
    var selectedTag: String? = null

    composeTestRule.setContent {
      TagGroup(
          name = "Test",
          tagList = sampleTags,
          selectedTags = emptyList(),
          onTagSelect = { tag -> selectedTag = tag.displayName })
    }

    composeTestRule.onNodeWithText(RUNNING.displayName).performClick()
    assertEquals(RUNNING.displayName, selectedTag)
  }

  @Test
  fun clickingSelectedTag_callsOnTagReSelect() {
    var reselectedTag: String? = null

    composeTestRule.setContent {
      TagGroup(
          name = "Test",
          tagList = sampleTags,
          selectedTags = listOf(RUNNING),
          onTagReSelect = { tag -> reselectedTag = tag.displayName })
    }

    composeTestRule.onNodeWithText(RUNNING.displayName).performClick()
    assertEquals(RUNNING.displayName, reselectedTag)
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
    val selectedTags = mutableStateListOf<Tag>()
    var lastSelected: Tag? = null
    var lastReselected: Tag? = null

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
    composeTestRule.onNodeWithText(READING.displayName).performClick()
    assertEquals(READING, lastSelected)
    assertTrue(READING in selectedTags)

    // Deselect "Reading"
    composeTestRule.onNodeWithText(READING.displayName).performClick()
    assertEquals(READING, lastReselected)
    assertTrue(READING !in selectedTags)
  }
}
