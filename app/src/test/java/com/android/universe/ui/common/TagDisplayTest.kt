package com.android.universe.ui.common

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.model.tag.Tag
import com.android.universe.model.tag.Tag.Category
import com.android.universe.utils.setContentWithStubBackdrop
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
    private val multipleTags = listOf(Tag.METAL, Tag.HANDBALL, Tag.SAFARI, Tag.MACHINE_LEARNING, Tag.AI, Tag.CYCLING, Tag.BRAIN_GAMES, Tag.ONLINE_GAMES, Tag.FOOTBALL, Tag.MUSIC, Tag.BASKETBALL)
  }

  @Test
  fun displaysTitle_whenNameIsProvided() {
    composeTestRule.setContentWithStubBackdrop {
      TagGroup(name = TOPICS.displayName, tagList = sampleTags, selectedTags = emptyList())
    }

    composeTestRule.onNodeWithText(TOPICS.displayName).assertIsDisplayed()
  }

  @Test
  fun doesNotDisplayTitle_whenNameIsEmpty() {
    composeTestRule.setContentWithStubBackdrop {
      TagGroup(name = "", tagList = sampleTags, selectedTags = emptyList())
    }

    composeTestRule.onAllNodesWithText(TOPICS.displayName).assertCountEquals(0)
  }

  @Test
  fun clickingUnselectedTag_callsOnTagSelect() {
    var selectedTag: String? = null

    composeTestRule.setContentWithStubBackdrop {
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

    composeTestRule.setContentWithStubBackdrop {
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
    composeTestRule.setContentWithStubBackdrop {
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

    composeTestRule.setContentWithStubBackdrop {
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

  @Test
  fun tagList_isScrollable() {
    composeTestRule.setContentWithStubBackdrop {
      TagGroup(
        name = "Test",
        tagList = multipleTags,
        selectedTags = emptyList()
      )
    }

    val lastTag = Tag.BASKETBALL.displayName

    composeTestRule.onNodeWithText(lastTag, useUnmergedTree = true).assertIsNotDisplayed()

    composeTestRule.onNodeWithText(lastTag, useUnmergedTree = true)
      .performScrollTo()

    composeTestRule.onNodeWithText(lastTag, useUnmergedTree = true).assertExists()
  }

  @Test
  fun tagElement_appliesUniqueTestTags() {
    composeTestRule.setContentWithStubBackdrop {
      TagGroup(
        name = "Test",
        tagList = sampleTags,
        selectedTags = emptyList(),
        tagElement = { t -> "Tag_${t.displayName}" }
      )
    }

    sampleTags.forEach { tag ->
      composeTestRule.onNodeWithTag("Tag_${tag.displayName}").assertExists()
    }
  }

  @Test
  fun nonSelectableTag_doesNotTriggerCallbacks() {
    var called = false

    composeTestRule.setContentWithStubBackdrop {
      TagGroup(
        name = "Test",
        tagList = sampleTags,
        selectedTags = emptyList(),
        isSelectable = false,
        onTagSelect = { called = true }
      )
    }

    composeTestRule.onNodeWithText(READING.displayName).performClick()

    assertEquals(false, called)
  }

  @Test
  fun fadeBoxes_arePresent() {
    composeTestRule.setContentWithStubBackdrop {
      TagGroup(name = "Test", tagList = sampleTags, selectedTags = emptyList())
    }

    composeTestRule.onNodeWithTag(TagGroupTestTag.TOP_FADE).assertExists()
    composeTestRule.onNodeWithTag(TagGroupTestTag.BOTTOM_FADE).assertExists()
  }
}
