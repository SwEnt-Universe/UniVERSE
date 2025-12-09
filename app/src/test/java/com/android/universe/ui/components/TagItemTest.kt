package com.android.universe.ui.components

import androidx.compose.runtime.collectAsState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.model.tag.Tag
import com.android.universe.utils.setContentWithStubBackdrop
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TagItemTest {
  @get:Rule val composeTestRule = createComposeRule()

  companion object TestValuesTagItem {
    val tag = Tag.METAL
    const val TAG_BUTTON = "ButtonMetal"
    const val TAG_TEXT = "TextMetal"
  }

  fun setUp(
      isSelectable: Boolean,
      isSelected: Boolean,
      onSelect: (Tag) -> Unit,
      onDeSelect: (Tag) -> Unit
  ) {
    composeTestRule.setContentWithStubBackdrop {
      TagItem(
          tag = tag,
          isSelectable = isSelectable,
          isSelected = isSelected,
          onSelect = onSelect,
          onDeSelect = onDeSelect)
    }
  }

  @Test
  fun notSelectedTagOnlyDisplayText() {
    setUp(isSelectable = true, isSelected = false, onSelect = {}, onDeSelect = {})
    composeTestRule.onNodeWithTag(TAG_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(TAG_TEXT, useUnmergedTree = true).assertIsDisplayed()
  }

  @Test
  fun tagCanBeSelected() {
    val tags = mutableListOf(Tag.DND)
    setUp(
        isSelectable = true,
        isSelected = false,
        onSelect = { tag -> tags.add(tag) },
        onDeSelect = {})
    composeTestRule.onNodeWithTag(TAG_BUTTON).performClick()
    assertEquals(2, tags.size)
    assertEquals(Tag.DND, tags[0])
    assertEquals(Tag.METAL, tags[1])
  }

  @Test
  fun tagCanBeDeSelected() {
    val tags = mutableListOf(Tag.DND, Tag.METAL)
    setUp(
        isSelectable = true,
        isSelected = true,
        onSelect = {},
        onDeSelect = { tag -> tags.remove(tag) })
    composeTestRule.onNodeWithTag(TAG_BUTTON).performClick()
    assertEquals(1, tags.size)
    assertEquals(Tag.DND, tags[0])
  }

  @Test
  fun disabledTagIsNotClickable() {
    var callbackCalled = false
    setUp(
        isSelectable = false,
        isSelected = false,
        onSelect = { callbackCalled = true },
        onDeSelect = { callbackCalled = true })

    composeTestRule.onNodeWithTag(TAG_BUTTON).assertIsNotEnabled()

    composeTestRule.onNodeWithTag(TAG_BUTTON).performClick()
    assertEquals(false, callbackCalled)
  }

  @Test
  fun categorySelectionWorks() {
    val select = MutableStateFlow(false)
    composeTestRule.setContentWithStubBackdrop {
      TagItem(
          tag = tag,
          isSelectable = true,
          isSelected = select.collectAsState().value,
          onSelect = { select.value = true },
          onDeSelect = { select.value = false },
          isCategory = true)
    }
    composeTestRule
        .onNodeWithTag(CategoryItemTestTags.categoryText(tag.category), useUnmergedTree = true)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(CategoryItemTestTags.categoryButton(tag.category))
        .assertIsEnabled()
        .performClick()
    assertEquals(true, select.value)
    composeTestRule.onNodeWithTag(CategoryItemTestTags.categoryButton(tag.category)).performClick()
    assertEquals(false, select.value)
  }
}
