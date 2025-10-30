package com.android.universe.ui.profileSettings

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.model.Tag
import com.android.universe.ui.profile.SettingsUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private fun setUpScreen(
      uiState: SettingsUiState = sampleSettingsState(),
      onOpenField: (String) -> Unit = {},
      onUpdateTemp: (String, String) -> Unit = { _, _ -> },
      onToggleCountryDropdown: (Boolean) -> Unit = {},
      onAddTag: (Tag) -> Unit = {},
      onRemoveTag: (Tag) -> Unit = {},
      onCloseModal: () -> Unit = {},
      onSaveModal: () -> Unit = {},
      onBack: () -> Unit = {}
  ) {
    composeTestRule.setContent {
      MaterialTheme {
        SettingsScreenContent(
            uiState = uiState,
            onOpenField = onOpenField,
            onUpdateTemp = onUpdateTemp,
            onToggleCountryDropdown = onToggleCountryDropdown,
            onAddTag = onAddTag,
            onRemoveTag = onRemoveTag,
            onCloseModal = onCloseModal,
            onSaveModal = onSaveModal,
            onBack = onBack)
      }
    }
  }

  @Test
  fun testSettingsScreen_DisplaysGeneralSectionFields() {
    setUpScreen()
    composeTestRule
        .onNodeWithTag(SettingsTestTags.EMAIL_BUTTON)
        .assertIsDisplayed()
        .assertTextEquals("Email address", "preview@epfl.ch")
    composeTestRule
        .onNodeWithTag(SettingsTestTags.PASSWORD_BUTTON)
        .assertIsDisplayed()
        .assertTextEquals("Password", "Unchanged")
  }

  @Test
  fun testSettingsScreen_DisplaysProfileSectionFields() {
    setUpScreen()
    composeTestRule
        .onNodeWithTag(SettingsTestTags.FIRST_NAME_BUTTON)
        .assertIsDisplayed()
        .assertTextEquals("First Name", "Emma")
    composeTestRule
        .onNodeWithTag(SettingsTestTags.LAST_NAME_BUTTON)
        .assertIsDisplayed()
        .assertTextEquals("Last Name", "Prolapse")

    // Description is truncated to 30 chars + "..."
    composeTestRule
        .onNodeWithTag(SettingsTestTags.DESCRIPTION_BUTTON)
        .assertIsDisplayed()
        .assertTextEquals("Description", "Loves Kotlin, skiing, and fond...")

    composeTestRule
        .onNodeWithTag(SettingsTestTags.COUNTRY_BUTTON)
        .assertIsDisplayed()
        .assertTextEquals("Country", "Switzerland")
    composeTestRule
        .onNodeWithTag(SettingsTestTags.DATE_BUTTON)
        .assertIsDisplayed()
        .assertTextEquals("Date of Birth", "2000-01-05")
  }

  @Test
  fun testSettingsScreen_OpenVariousModals_viaButtons() {
    var opened: String? = null
    setUpScreen(onOpenField = { opened = it })

    composeTestRule.onNodeWithTag(SettingsTestTags.EMAIL_BUTTON).performClick()
    assertEquals("email", opened)

    composeTestRule.onNodeWithTag(SettingsTestTags.PASSWORD_BUTTON).performClick()
    assertEquals("password", opened)

    composeTestRule.onNodeWithTag(SettingsTestTags.FIRST_NAME_BUTTON).performClick()
    assertEquals("firstName", opened)

    composeTestRule.onNodeWithTag(SettingsTestTags.LAST_NAME_BUTTON).performClick()
    assertEquals("lastName", opened)

    composeTestRule.onNodeWithTag(SettingsTestTags.DESCRIPTION_BUTTON).performClick()
    assertEquals("description", opened)

    composeTestRule.onNodeWithTag(SettingsTestTags.COUNTRY_BUTTON).performClick()
    assertEquals("country", opened)

    composeTestRule.onNodeWithTag(SettingsTestTags.DATE_BUTTON).performClick()
    assertEquals("date", opened)
  }

  @Test
  fun testSettingsScreen_InterestsSection_ShowsCategoryChipsLines() {
    setUpScreen()

    // The interests section creates test tags literally like "SettingsTestTags.INTEREST_BUTTON"
    listOf(
            "SettingsTestTags.INTEREST_BUTTON",
            "SettingsTestTags.SPORT_BUTTON",
            "SettingsTestTags.MUSIC_BUTTON",
            "SettingsTestTags.TRANSPORT_BUTTON",
            "SettingsTestTags.CANTON_BUTTON")
        .forEach { tag -> composeTestRule.onNodeWithTag(tag).assertIsDisplayed() }
  }

  @Test
  fun testSettingsScreen_BackButton() {
    var backClicked = false
    setUpScreen(onBack = { backClicked = true })
    composeTestRule.onNodeWithContentDescription("Back").performClick()
    assert(backClicked) { "Back button should trigger onBack action" }
  }

  @Test
  fun testPasswordMaskingWhenNonEmpty() {
    val state = sampleSettingsState().copy(password = "secret")
    setUpScreen(uiState = state)
    composeTestRule
        .onNodeWithTag(SettingsTestTags.PASSWORD_BUTTON)
        .assertIsDisplayed()
        .assertTextEquals("Password", "********")
  }

  @Test
  fun testErrorTexts_AllThreeDateErrorsShown() {
    val state =
        sampleSettingsState()
            .copy(
                dayError = "Invalid day", monthError = "Invalid month", yearError = "Invalid year")
    setUpScreen(state)
    composeTestRule.onNodeWithText("Invalid day").assertIsDisplayed()
    composeTestRule.onNodeWithText("Invalid month").assertIsDisplayed()
    composeTestRule.onNodeWithText("Invalid year").assertIsDisplayed()
  }
}
