package com.android.universe.ui.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.utils.setContentWithStubBackdrop
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LiquidSearchBarTest {

  @get:Rule val composeRule = createComposeRule()

  @Test
  fun liquidSearchBar_allowsTyping() {
    composeRule.setContentWithStubBackdrop {
      var text by remember { mutableStateOf("") }

      LiquidSearchBar(
          query = text,
          onQueryChange = { text = it },
      )
    }

    composeRule.onNodeWithTag(LiquidSearchBarTestTags.SEARCH_INPUT).performTextInput("hello")

    composeRule.onNodeWithTag(LiquidSearchBarTestTags.SEARCH_INPUT).assertTextEquals("hello")
  }

  @Test
  fun placeholder_isVisible_whenQueryIsEmpty() {
    composeRule.setContentWithStubBackdrop {
      LiquidSearchBar(query = "", onQueryChange = {}, placeholder = "Search...")
    }

    composeRule.onNodeWithText("Search...").assertIsDisplayed()
  }
}
