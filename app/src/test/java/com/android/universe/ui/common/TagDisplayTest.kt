package com.android.universe.ui.common

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
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
    private val manyTags =
        listOf(RUNNING, MUSIC, MUSIC, MUSIC, MUSIC, MUSIC, MUSIC, MUSIC, MUSIC, MUSIC, READING)
    private const val SELECTED = "Selected"
    private val multipleTags =
        listOf(
            Tag.METAL,
            Tag.HANDBALL,
            Tag.SAFARI,
            Tag.MACHINE_LEARNING,
            Tag.AI,
            Tag.CYCLING,
            Tag.BRAIN_GAMES,
            Tag.ONLINE_GAMES,
            Tag.FOOTBALL,
            Tag.MUSIC,
            Tag.BASKETBALL)
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
      TagGroup(name = "Test", tagList = multipleTags, selectedTags = emptyList())
    }

    val lastTag = Tag.BASKETBALL.displayName

    composeTestRule.onNodeWithText(lastTag, useUnmergedTree = true).assertIsNotDisplayed()

    composeTestRule.onNodeWithText(lastTag, useUnmergedTree = true).performScrollTo()

    composeTestRule.onNodeWithText(lastTag, useUnmergedTree = true).assertExists()
  }

  @Test
  fun tagElement_appliesUniqueTestTags() {
    composeTestRule.setContentWithStubBackdrop {
      TagGroup(
          name = "Test",
          tagList = sampleTags,
          selectedTags = emptyList(),
          tagElement = { t -> "Tag_${t.displayName}" })
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
          onTagSelect = { called = true })
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

  @Test
  fun clickingUnselectedTag_callsOnTagSelectForColumn() {
    var selectedTag: String? = null

    composeTestRule.setContentWithStubBackdrop {
      TagColumn(
          tags = sampleTags,
          onTagSelect = { tag -> selectedTag = tag.displayName },
          isSelected = { tag: Tag -> false },
          tagElement = { tag -> tag.displayName })
    }
    composeTestRule.onNodeWithText(RUNNING.displayName).performClick()
    assertEquals(RUNNING.displayName, selectedTag)
  }

  @Test
  fun clickingSelectedTag_callsOnTagReSelectColumn() {
    var reselectedTag: String? = null

    composeTestRule.setContentWithStubBackdrop {
      TagColumn(
          tags = sampleTags,
          onTagReSelect = { tag -> reselectedTag = tag.displayName },
          isSelected = { tag: Tag -> true },
          tagElement = { tag -> tag.displayName })
    }

    composeTestRule.onNodeWithText(RUNNING.displayName).performClick()
    assertEquals(RUNNING.displayName, reselectedTag)
  }

  @Test
  fun nonSelectableTag_doesNotTriggerCallbacksColumn() {
    var called = false

    composeTestRule.setContentWithStubBackdrop {
      TagColumn(
          tags = sampleTags,
          onTagSelect = { tag -> called = true },
          isSelected = { tag: Tag -> false },
          tagElement = { tag -> tag.displayName },
          isSelectable = false)
    }

    composeTestRule.onNodeWithText(READING.displayName).performClick()

    assertEquals(false, called)
  }

  @Test
  fun tagColumn_displaysAllTags() {
    composeTestRule.setContentWithStubBackdrop {
      TagColumn(tags = sampleTags, onTagSelect = {}, isSelected = { false })
    }

    sampleTags.forEach { tag ->
      composeTestRule.onNodeWithText(tag.displayName).assertIsDisplayed()
    }
  }

  @Test
  fun tagColumn_displaysFade() {
    composeTestRule.setContentWithStubBackdrop {
      TagColumn(tags = sampleTags, onTagSelect = {}, isSelected = { false }, fade = true)
    }

    composeTestRule.onNodeWithTag(TagGroupTestTag.BOTTOM_FADE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(TagGroupTestTag.TOP_FADE).assertIsDisplayed()
  }

  @Test
  fun tagColumn_displaysNoFadeWhenFalse() {
    composeTestRule.setContentWithStubBackdrop {
      TagColumn(tags = sampleTags, onTagSelect = {}, isSelected = { false }, fade = false)
    }

    composeTestRule.onNodeWithText(TagGroupTestTag.BOTTOM_FADE).assertIsNotDisplayed()
    composeTestRule.onNodeWithText(TagGroupTestTag.TOP_FADE).assertIsNotDisplayed()
  }

  @Test
  fun tagColumn_isScrollable() {
    composeTestRule.setContentWithStubBackdrop {
      TagColumn(
          tags = manyTags,
          onTagSelect = {},
          isSelected = { false },
          tagElement = { tag -> tag.displayName })
    }

    val lastTag = READING
    composeTestRule
        .onNodeWithTag(TagGroupTestTag.COLUMN)
        .performScrollToNode(hasTestTag(lastTag.displayName))
    composeTestRule.onNodeWithText(lastTag.displayName).assertIsDisplayed()
  }

  @Test
  fun tagColumn_multipleSelection() {
    val selectedTags = mutableStateListOf<Tag>()
    composeTestRule.setContentWithStubBackdrop {
      TagColumn(
          tags = sampleTags,
          isSelected = { it in selectedTags },
          onTagSelect = { selectedTags.add(it) },
          onTagReSelect = { selectedTags.remove(it) })
    }

    composeTestRule.onNodeWithText(sampleTags[0].displayName).performClick()
    composeTestRule.onNodeWithText(sampleTags[1].displayName).performClick()

    assertTrue(sampleTags[0] in selectedTags)
    assertTrue(sampleTags[1] in selectedTags)
  }

  @Test
  fun clickingUnselectedTag_callsOnTagSelectForRow() {
    var selectedTag: String? = null

    composeTestRule.setContentWithStubBackdrop {
      TagRow(
          tags = sampleTags,
          onTagSelect = { tag -> selectedTag = tag.displayName },
          isSelected = { tag: Tag -> false },
          tagElement = { tag -> tag.displayName })
    }
    composeTestRule.onNodeWithText(RUNNING.displayName).performClick()
    assertEquals(RUNNING.displayName, selectedTag)
  }

  @Test
  fun clickingSelectedTag_callsOnTagReSelectRow() {
    var reselectedTag: String? = null

    composeTestRule.setContentWithStubBackdrop {
      TagRow(
          tags = sampleTags,
          onTagReSelect = { tag -> reselectedTag = tag.displayName },
          isSelected = { tag: Tag -> true },
          tagElement = { tag -> tag.displayName })
    }

    composeTestRule.onNodeWithText(RUNNING.displayName).performClick()
    assertEquals(RUNNING.displayName, reselectedTag)
  }

  @Test
  fun nonSelectableTag_doesNotTriggerCallbacksRow() {
    var called = false

    composeTestRule.setContentWithStubBackdrop {
      TagRow(
          tags = sampleTags,
          onTagSelect = { tag -> called = true },
          isSelected = { tag: Tag -> false },
          tagElement = { tag -> tag.displayName },
          isSelectable = false)
    }

    composeTestRule.onNodeWithText(READING.displayName).performClick()

    assertEquals(false, called)
  }

  @Test
  fun tagRow_displaysAllTags() {
    composeTestRule.setContentWithStubBackdrop {
      TagRow(tags = sampleTags, onTagSelect = {}, isSelected = { false })
    }

    sampleTags.forEach { tag ->
      composeTestRule.onNodeWithText(tag.displayName).assertIsDisplayed()
    }
  }

  @Test
  fun tagRow_displaysAllTagsWith() {
    composeTestRule.setContentWithStubBackdrop {
      TagRow(tags = sampleTags, onTagSelect = {}, isSelected = { false })
    }

    sampleTags.forEach { tag ->
      composeTestRule.onNodeWithText(tag.displayName).assertIsDisplayed()
    }
  }

  @Test
  fun tagRow_displaysFade() {
    composeTestRule.setContentWithStubBackdrop {
      TagRow(tags = sampleTags, onTagSelect = {}, isSelected = { false }, fade = true)
    }

    composeTestRule.onNodeWithTag(TagGroupTestTag.RIGHT_FADE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(TagGroupTestTag.LEFT_FADE).assertIsDisplayed()
  }

  @Test
  fun tagRow_displaysNoFadeWhenFalse() {
    composeTestRule.setContentWithStubBackdrop {
      TagRow(tags = sampleTags, onTagSelect = {}, isSelected = { false }, fade = false)
    }

    composeTestRule.onNodeWithText(TagGroupTestTag.RIGHT_FADE).assertIsNotDisplayed()
    composeTestRule.onNodeWithText(TagGroupTestTag.LEFT_FADE).assertIsNotDisplayed()
  }

  @Test
  fun tagRow_isScrollable() {
    composeTestRule.setContentWithStubBackdrop {
      TagRow(
          tags = manyTags,
          onTagSelect = {},
          isSelected = { false },
          tagElement = { tag -> tag.displayName })
    }

    val lastTag = READING
    composeTestRule
        .onNodeWithTag(TagGroupTestTag.ROW)
        .performScrollToNode(hasTestTag(lastTag.displayName))
    composeTestRule.onNodeWithText(lastTag.displayName).assertIsDisplayed()
  }

  @Test
  fun tagRow_multipleSelection() {
    val selectedTags = mutableStateListOf<Tag>()
    composeTestRule.setContentWithStubBackdrop {
      TagRow(
          tags = sampleTags,
          isSelected = { it in selectedTags },
          onTagSelect = { selectedTags.add(it) },
          onTagReSelect = { selectedTags.remove(it) })
    }

    composeTestRule.onNodeWithText(sampleTags[0].displayName).performClick()
    composeTestRule.onNodeWithText(sampleTags[1].displayName).performClick()

    assertTrue(sampleTags[0] in selectedTags)
    assertTrue(sampleTags[1] in selectedTags)
  }
}
