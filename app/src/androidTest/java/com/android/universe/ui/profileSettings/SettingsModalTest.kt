package com.android.universe.ui.profileSettings

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.android.universe.model.CountryData
import com.android.universe.model.Tag
import com.android.universe.ui.profile.SettingsUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SettingsModalTest {

  @get:Rule val composeTestRule = createComposeRule()

  private fun renderWith(
      ui: SettingsUiState,
      onUpdateTemp: (String, String) -> Unit = { _, _ -> },
      onToggleCountryDropdown: (Boolean) -> Unit = {},
      onAddTag: (Tag) -> Unit = {},
      onRemoveTag: (Tag) -> Unit = {},
      onCloseModal: () -> Unit = {},
      onSaveModal: () -> Unit = {}
  ) {
    composeTestRule.setContent {
      MaterialTheme {
        SettingsScreenContent(
            uiState = ui,
            onUpdateTemp = onUpdateTemp,
            onToggleCountryDropdown = onToggleCountryDropdown,
            onAddTag = onAddTag,
            onRemoveTag = onRemoveTag,
            onCloseModal = onCloseModal,
            onSaveModal = onSaveModal)
      }
    }
  }

  // ---------- Text fields ----------

  @Test
  fun emailModal_allowsEditing_and_buttonsVisible() {
    var last = ""
    val ui = sampleSettingsState(showModal = true, field = "email").copy(tempValue = "")

    renderWith(ui, onUpdateTemp = { k, v -> if (k == "tempValue") last = v })

    composeTestRule
        .onNodeWithTag(SettingsTestTags.MODAL_TITLE, useUnmergedTree = true)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(SettingsTestTags.MODAL_CANCEL_BUTTON, useUnmergedTree = true)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(SettingsTestTags.MODAL_SAVE_BUTTON, useUnmergedTree = true)
        .assertIsDisplayed()

    val field =
        composeTestRule.onNode(
            hasSetTextAction() and
                androidx.compose.ui.test.hasTestTag(SettingsTestTags.EMAIL_FIELD),
            useUnmergedTree = true)
    field.performClick()
    field.performTextClearance()
    field.performTextInput("newemail@example.com")

    assertEquals("newemail@example.com", last)
  }

  @Test
  fun passwordModal_allowsEditing() {
    var last = ""
    val ui = sampleSettingsState(showModal = true, field = "password").copy(tempValue = "")

    renderWith(ui, onUpdateTemp = { k, v -> if (k == "tempValue") last = v })

    val field =
        composeTestRule.onNode(
            hasSetTextAction() and
                androidx.compose.ui.test.hasTestTag(SettingsTestTags.PASSWORD_FIELD),
            useUnmergedTree = true)
    field.performClick()
    field.performTextClearance()
    field.performTextInput("passw0rd!")

    assertEquals("passw0rd!", last)
  }

  @Test
  fun firstNameModal_allowsEditing() {
    var updated = ""
    val ui = sampleSettingsState(showModal = true, field = "firstName").copy(tempValue = "")
    renderWith(ui, onUpdateTemp = { k, v -> if (k == "tempValue") updated = v })

    val field =
        composeTestRule.onNode(
            hasSetTextAction() and
                androidx.compose.ui.test.hasTestTag(SettingsTestTags.FIRST_NAME_FIELD),
            useUnmergedTree = true)
    field.performClick()
    field.performTextClearance()
    field.performTextInput("Alice")

    assertEquals("Alice", updated)
  }

  @Test
  fun lastNameModal_allowsEditing() {
    var updated = ""
    val ui = sampleSettingsState(showModal = true, field = "lastName").copy(tempValue = "")
    renderWith(ui, onUpdateTemp = { k, v -> if (k == "tempValue") updated = v })

    val field =
        composeTestRule.onNode(
            hasSetTextAction() and
                androidx.compose.ui.test.hasTestTag(SettingsTestTags.LAST_NAME_FIELD),
            useUnmergedTree = true)
    field.performClick()
    field.performTextClearance()
    field.performTextInput("Smith")

    assertEquals("Smith", updated)
  }

  @Test
  fun descriptionModal_multiline_and_errorShown() {
    var last = ""
    val ui =
        sampleSettingsState(showModal = true, field = "description")
            .copy(tempValue = "", modalError = "Too short")
    renderWith(ui, onUpdateTemp = { k, v -> if (k == "tempValue") last = v })

    composeTestRule.onNodeWithText("Too short").assertIsDisplayed()

    val field =
        composeTestRule.onNode(
            hasSetTextAction() and
                androidx.compose.ui.test.hasTestTag(SettingsTestTags.DESCRIPTION_FIELD),
            useUnmergedTree = true)
    field.performClick()
    field.performTextClearance()
    field.performTextInput("Loves coding and hiking.")

    assertEquals("Loves coding and hiking.", last)
  }

  // ---------- Country dropdown ----------

  @Test
  fun countryModal_dropdown_pickUpdates_andCloses() {
    var picked = ""
    var expandedState: Boolean? = null

    val ui =
        sampleSettingsState(showModal = true, field = "country")
            .copy(tempValue = "", showCountryDropdown = true)

    renderWith(
        ui,
        onUpdateTemp = { k, v -> if (k == "tempValue") picked = v },
        onToggleCountryDropdown = { expanded -> expandedState = expanded })

    // Click the FIRST option exposed by CountryData, by test tag, not by text.
    val firstOption = CountryData.allCountries.first()
    composeTestRule
        .onNodeWithTag(
            "${SettingsTestTags.COUNTRY_OPTION_PREFIX}$firstOption", useUnmergedTree = true)
        .performClick()

    assertEquals(firstOption, picked)
    assertEquals(false, expandedState)
  }

  @Test
  fun countryModal_tappingField_togglesExpansion() {
    var lastExpanded: Boolean? = null

    val ui =
        sampleSettingsState(showModal = true, field = "country")
            .copy(tempValue = "—", showCountryDropdown = false)
    renderWith(ui, onToggleCountryDropdown = { expanded -> lastExpanded = expanded })

    // Tapping the readOnly text field inside the ExposedDropdownMenuBox should request expansion.
    composeTestRule
        .onNodeWithTag(SettingsTestTags.COUNTRY_DROPDOWN_FIELD, useUnmergedTree = true)
        .performClick()

    // We only assert that we got a toggle request to 'true' (actual visual expansion is owned by
    // upstream state)
    assertEquals(true, lastExpanded)
  }

  // ---------- Date fields ----------

  @Test
  fun dateModal_inputsUpdate_and_showErrors() {
    var d = ""
    var m = ""
    var y = ""
    val ui =
        sampleSettingsState(showModal = true, field = "date")
            .copy(
                tempDay = "",
                tempMonth = "",
                tempYear = "",
                tempDayError = "Invalid day",
                tempMonthError = "Invalid month",
                tempYearError = "Invalid year")
    renderWith(
        ui,
        onUpdateTemp = { k, v ->
          when (k) {
            "tempDay" -> d = v
            "tempMonth" -> m = v
            "tempYear" -> y = v
          }
        })

    composeTestRule.onNodeWithText("Invalid day").assertIsDisplayed()
    composeTestRule.onNodeWithText("Invalid month").assertIsDisplayed()
    composeTestRule.onNodeWithText("Invalid year").assertIsDisplayed()

    val day = composeTestRule.onNodeWithTag(SettingsTestTags.DAY_FIELD, useUnmergedTree = true)
    day.performClick()
    day.performTextClearance()
    day.performTextInput("07")

    val month = composeTestRule.onNodeWithTag(SettingsTestTags.MONTH_FIELD, useUnmergedTree = true)
    month.performClick()
    month.performTextClearance()
    month.performTextInput("12")

    val year = composeTestRule.onNodeWithTag(SettingsTestTags.YEAR_FIELD, useUnmergedTree = true)
    year.performClick()
    year.performTextClearance()
    year.performTextInput("1999")

    assertEquals("07", d)
    assertEquals("12", m)
    assertEquals("1999", y)
  }

  // ---------- Save / Cancel ----------

  @Test
  fun saveAndCancel_invokeCallbacks() {
    var saved = false
    var closed = false
    val ui = sampleSettingsState(showModal = true, field = "firstName").copy(tempValue = "")
    renderWith(ui, onCloseModal = { closed = true }, onSaveModal = { saved = true })

    composeTestRule
        .onNodeWithTag(SettingsTestTags.MODAL_SAVE_BUTTON, useUnmergedTree = true)
        .performClick()
    composeTestRule
        .onNodeWithTag(SettingsTestTags.MODAL_CANCEL_BUTTON, useUnmergedTree = true)
        .performClick()

    assertTrue(saved)
    assertTrue(closed)
  }
}
