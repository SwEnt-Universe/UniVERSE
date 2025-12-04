package com.android.universe.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.dp
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
class TagDisplayTest {

  @get:Rule val composeTestRule = createComposeRule()

  companion object {
    private val READING = Tag.LITERATURE
    private val RUNNING = Tag.RUNNING
    private val MUSIC = Tag.MUSIC
    private val TOPICS = Category.TOPIC
    private val sampleTags = listOf(READING, RUNNING, MUSIC)
    private val manyTags =
        listOf(RUNNING, MUSIC, MUSIC, MUSIC, MUSIC, MUSIC, MUSIC, MUSIC, MUSIC, MUSIC, READING)
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
      TagGroup(title = TOPICS.displayName, tagList = sampleTags, selectedTags = emptyList())
    }

    composeTestRule.onNodeWithText(TOPICS.displayName).assertIsDisplayed()
  }

  @Test
  fun doesNotDisplayTitle_whenNameIsEmpty() {
    composeTestRule.setContentWithStubBackdrop {
      TagGroup(title = "", tagList = sampleTags, selectedTags = emptyList())
    }

    composeTestRule.onAllNodesWithText(TOPICS.displayName).assertCountEquals(0)
  }

  @Test
  fun clickingUnselectedTag_callsOnTagSelect() {
    var selectedTag: String? = null

    composeTestRule.setContentWithStubBackdrop {
      TagGroup(
          title = "Test",
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
          title = "Test",
          tagList = sampleTags,
          selectedTags = listOf(RUNNING),
          onTagReSelect = { tag -> reselectedTag = tag.displayName })
    }

    composeTestRule.onNodeWithText(RUNNING.displayName).performClick()
    assertEquals(RUNNING.displayName, reselectedTag)
  }

  @Test
  fun selectingAndDeselectingTag_triggersCorrectCallbacks() {
    val selectedTags = mutableStateListOf<Tag>()
    var lastSelected: Tag? = null
    var lastReselected: Tag? = null

    composeTestRule.setContentWithStubBackdrop {
      TagGroup(
          title = "Test",
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
      TagGroup(title = "Test", tagList = multipleTags, selectedTags = emptyList(), height = 100.dp)
    }

    val lastTag = Tag.BASKETBALL.displayName

    composeTestRule.onNodeWithText(lastTag, useUnmergedTree = true).assertIsNotDisplayed()

    composeTestRule.onNodeWithText(lastTag, useUnmergedTree = true).performScrollTo()

    composeTestRule.onNodeWithText(lastTag, useUnmergedTree = true).assertIsDisplayed()
  }

  @Test
  fun tagElement_appliesUniqueTestTags() {
    composeTestRule.setContentWithStubBackdrop {
      TagGroup(
          title = "Test",
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
          title = "Test",
          tagList = sampleTags,
          selectedTags = emptyList(),
          isSelectable = false,
          onTagSelect = { called = true })
    }

    composeTestRule.onNodeWithText(READING.displayName).performClick()

    assertEquals(false, called)
  }

  @Test
  fun clickingUnselectedTag_callsOnTagSelectForColumn() {
    var selectedTag: String? = null

    composeTestRule.setContentWithStubBackdrop {
      TagColumn(
          tags = sampleTags,
          onTagSelect = { tag -> selectedTag = tag.displayName },
          isSelected = { _: Tag -> false },
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
          isSelected = { _: Tag -> true },
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
          onTagSelect = { _ -> called = true },
          isSelected = { _: Tag -> false },
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
        .onNodeWithTag(TagGroupTestTag.tagColumn(manyTags))
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
          isSelected = { _: Tag -> false },
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
          isSelected = { _: Tag -> true },
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
          onTagSelect = { _ -> called = true },
          isSelected = { _: Tag -> false },
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
        .onNodeWithTag(TagGroupTestTag.tagRow(manyTags))
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

  @Test
  fun tagGroup_dynamicColumns_narrowWidth_singleColumn() {
    val tags = listOf(Tag.RUNNING, Tag.SWIMMING)

    composeTestRule.setContentWithStubBackdrop {
      Box(modifier = Modifier.width(150.dp)) {
        TagGroup(
            title = "Dynamic 1 Col",
            tagList = tags,
            selectedTags = emptyList(),
            tagElement = { "TAG_${it.name}" })
      }
    }

    val tag1Bounds =
        composeTestRule.onNodeWithTag("TAG_${tags[0].name}").fetchSemanticsNode().boundsInRoot
    val tag2Bounds =
        composeTestRule.onNodeWithTag("TAG_${tags[1].name}").fetchSemanticsNode().boundsInRoot

    assertTrue(
        "Tag 2 should be below Tag 1 in single column layout", tag2Bounds.top >= tag1Bounds.bottom)
  }

  @Test
  fun tagGroup_dynamicColumns_wideWidth_multiColumn() {
    val tags = listOf(Tag.RUNNING, Tag.SWIMMING)

    composeTestRule.setContentWithStubBackdrop {
      Box(modifier = Modifier.width(300.dp)) {
        TagGroup(
            title = "Dynamic 2 Col",
            tagList = tags,
            selectedTags = emptyList(),
            tagElement = { "TAG_${it.name}" })
      }
    }

    val tag1Bounds =
        composeTestRule.onNodeWithTag("TAG_${tags[0].name}").fetchSemanticsNode().boundsInRoot
    val tag2Bounds =
        composeTestRule.onNodeWithTag("TAG_${tags[1].name}").fetchSemanticsNode().boundsInRoot

    assertEquals("Tags should be in the same row", tag1Bounds.top, tag2Bounds.top, 5.0f)
    assertTrue("Tag 2 should be to the right of Tag 1", tag2Bounds.left >= tag1Bounds.right)
  }
}
