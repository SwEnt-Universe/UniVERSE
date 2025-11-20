package com.android.universe.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.model.tag.Tag
import com.android.universe.utils.setContentWithStubBackdrop
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TagItemTest {
  @get:Rule val composeTestRule = createComposeRule()
  val tag = Tag.METAL

  fun setUp(
      isSelectable: Boolean,
      isAlreadySelected: Boolean,
      onSelect: (Tag) -> Unit,
      onDeSelect: (Tag) -> Unit
  ) {
    composeTestRule.setContentWithStubBackdrop {
      TagItem(
          tag = tag,
          isSelectable = isSelectable,
          isAlreadySelected = isAlreadySelected,
          onSelect = onSelect,
          onDeSelect = onDeSelect)
    }
  }

  @Test
  fun notSelectedTagOnlyDisplayText() {
    setUp(isSelectable = true, isAlreadySelected = false, onSelect = {}, onDeSelect = {})
    composeTestRule.onNodeWithTag(TagItemTestTag.BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(TagItemTestTag.TEXT, useUnmergedTree = true).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(TagItemTestTag.ICON, useUnmergedTree = true)
        .assertIsNotDisplayed()
  }

  @Test
  fun selectedTagDisplayIcon() {
    setUp(isSelectable = true, isAlreadySelected = true, onSelect = {}, onDeSelect = {})
    composeTestRule.onNodeWithTag(TagItemTestTag.ICON, useUnmergedTree = true).assertIsDisplayed()
  }

  @Test
  fun tagCanBeSelected() {
    val tags = mutableListOf(Tag.DND)
    setUp(
        isSelectable = true,
        isAlreadySelected = false,
        onSelect = { tag -> tags.add(tag) },
        onDeSelect = {})
    composeTestRule.onNodeWithTag(TagItemTestTag.BUTTON).performClick()
    composeTestRule.onNodeWithTag(TagItemTestTag.ICON, useUnmergedTree = true).assertIsDisplayed()
    assertEquals(2, tags.size)
    assertEquals(Tag.DND, tags[0])
    assertEquals(Tag.METAL, tags[1])
  }

  @Test
  fun tagCanBeDeSelected() {
    val tags = mutableListOf(Tag.DND, Tag.METAL)
    setUp(
        isSelectable = true,
        isAlreadySelected = true,
        onSelect = {},
        onDeSelect = { tag -> tags.remove(tag) })
    composeTestRule.onNodeWithTag(TagItemTestTag.BUTTON).performClick()
    composeTestRule
        .onNodeWithTag(TagItemTestTag.ICON, useUnmergedTree = true)
        .assertIsNotDisplayed()
    assertEquals(1, tags.size)
    assertEquals(Tag.DND, tags[0])
  }
}
