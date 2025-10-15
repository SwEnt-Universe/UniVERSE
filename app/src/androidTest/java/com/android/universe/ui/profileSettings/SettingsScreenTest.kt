package com.android.universe.ui.profileSettings

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.android.universe.model.Tag
import com.android.universe.ui.profile.SettingsUiState
import org.junit.Rule
import org.junit.Test

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
        .assertTextEquals("Email address", "preview@example.com")
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
  fun testSettingsScreen_OpensEmailModal() {
    var fieldOpened: String? = null
    setUpScreen(onOpenField = { field -> fieldOpened = field })
    composeTestRule.onNodeWithTag(SettingsTestTags.EMAIL_BUTTON).performClick()
    assert(fieldOpened == "email") { "Expected email modal to open, but got $fieldOpened" }
  }

  @Test
  fun testSettingsScreen_ModalDisplaysCorrectly() {
    setUpScreen(
        uiState = sampleSettingsState(showModal = true, field = "email"),
        onUpdateTemp = { _, _ -> })
    composeTestRule.onNodeWithText("Edit Email", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(SettingsTestTags.MODAL_CANCEL_BUTTON, useUnmergedTree = true)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(SettingsTestTags.MODAL_SAVE_BUTTON, useUnmergedTree = true)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithText("preview@example.com", useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun testSettingsScreen_ModalCancelClosesModal() {
    var modalClosed = false
    setUpScreen(
        uiState = sampleSettingsState(showModal = true, field = "firstName"),
        onCloseModal = { modalClosed = true })
    composeTestRule
        .onNodeWithTag(SettingsTestTags.MODAL_CANCEL_BUTTON, useUnmergedTree = true)
        .performClick()
    assert(modalClosed) { "Modal should be closed on cancel" }
  }

  @Test
  fun testSettingsScreen_ModalSaveTriggersSave() {
    var saveTriggered = false
    setUpScreen(
        uiState = sampleSettingsState(showModal = true, field = "firstName"),
        onSaveModal = { saveTriggered = true })
    composeTestRule
        .onNodeWithTag(SettingsTestTags.MODAL_SAVE_BUTTON, useUnmergedTree = true)
        .performClick()
    assert(saveTriggered) { "Save action should be triggered" }
  }

  @Test
  fun testSettingsScreen_EmailInputInModal() {
    var updatedValue: String? = null
    setUpScreen(
        uiState = sampleSettingsState(showModal = true, field = "email"),
        onUpdateTemp = { key, value -> if (key == "tempValue") updatedValue = value })

    composeTestRule.onNodeWithText("Edit Email", useUnmergedTree = true).assertIsDisplayed()

    // Match only nodes that support setText (avoids grabbing labels/containers)
    composeTestRule
        .onNode(
            hasSetTextAction() and
                androidx.compose.ui.test.hasTestTag(SettingsTestTags.EMAIL_FIELD),
            useUnmergedTree = true)
        .performClick()
    composeTestRule
        .onNode(
            hasSetTextAction() and
                androidx.compose.ui.test.hasTestTag(SettingsTestTags.EMAIL_FIELD),
            useUnmergedTree = true)
        .performTextInput("newemail@example.com")

    assert(updatedValue == "newemail@example.com") {
      "Expected email update to newemail@example.com, but got $updatedValue"
    }
  }

  @Test
  fun testSettingsScreen_PasswordInputInModal() {
    var updatedValue: String? = null
    setUpScreen(
        uiState = sampleSettingsState(showModal = true, field = "password"),
        onUpdateTemp = { key, value -> if (key == "tempValue") updatedValue = value })

    composeTestRule.onNodeWithText("Edit Password", useUnmergedTree = true).assertIsDisplayed()

    composeTestRule
        .onNode(
            hasSetTextAction() and
                androidx.compose.ui.test.hasTestTag(SettingsTestTags.PASSWORD_FIELD),
            useUnmergedTree = true)
        .performClick()
    composeTestRule
        .onNode(
            hasSetTextAction() and
                androidx.compose.ui.test.hasTestTag(SettingsTestTags.PASSWORD_FIELD),
            useUnmergedTree = true)
        .performTextInput("newpassword123")

    assert(updatedValue == "newpassword123") {
      "Expected password update to newpassword123, but got $updatedValue"
    }
  }

  @Test
  fun testSettingsScreen_FirstNameInputInModal() {
    var updatedValue: String? = null
    setUpScreen(
        uiState = sampleSettingsState(showModal = true, field = "firstName"),
        onUpdateTemp = { key, value -> if (key == "tempValue") updatedValue = value })

    composeTestRule.onNodeWithText("Edit First Name", useUnmergedTree = true).assertIsDisplayed()

    composeTestRule
        .onNode(
            hasSetTextAction() and
                androidx.compose.ui.test.hasTestTag(SettingsTestTags.FIRST_NAME_FIELD),
            useUnmergedTree = true)
        .performClick()
    composeTestRule
        .onNode(
            hasSetTextAction() and
                androidx.compose.ui.test.hasTestTag(SettingsTestTags.FIRST_NAME_FIELD),
            useUnmergedTree = true)
        .performTextInput("Alice")

    assert(updatedValue == "Alice") { "Expected first name update to Alice, but got $updatedValue" }
  }

  @Test
  fun testSettingsScreen_LastNameInputInModal() {
    var updatedValue: String? = null
    setUpScreen(
        uiState = sampleSettingsState(showModal = true, field = "lastName"),
        onUpdateTemp = { key, value -> if (key == "tempValue") updatedValue = value })

    composeTestRule.onNodeWithText("Edit Last Name", useUnmergedTree = true).assertIsDisplayed()

    composeTestRule
        .onNode(
            hasSetTextAction() and
                androidx.compose.ui.test.hasTestTag(SettingsTestTags.LAST_NAME_FIELD),
            useUnmergedTree = true)
        .performClick()
    composeTestRule
        .onNode(
            hasSetTextAction() and
                androidx.compose.ui.test.hasTestTag(SettingsTestTags.LAST_NAME_FIELD),
            useUnmergedTree = true)
        .performTextInput("Smith")

    assert(updatedValue == "Smith") { "Expected last name update to Smith, but got $updatedValue" }
  }

  @Test
  fun testSettingsScreen_DescriptionInputInModal() {
    var updatedValue: String? = null
    setUpScreen(
        uiState = sampleSettingsState(showModal = true, field = "description"),
        onUpdateTemp = { key, value -> if (key == "tempValue") updatedValue = value })

    composeTestRule.onNodeWithText("Edit Description", useUnmergedTree = true).assertIsDisplayed()

    composeTestRule
        .onNode(
            hasSetTextAction() and
                androidx.compose.ui.test.hasTestTag(SettingsTestTags.DESCRIPTION_FIELD),
            useUnmergedTree = true)
        .performClick()
    composeTestRule
        .onNode(
            hasSetTextAction() and
                androidx.compose.ui.test.hasTestTag(SettingsTestTags.DESCRIPTION_FIELD),
            useUnmergedTree = true)
        .performTextInput("Loves coding and hiking.")

    assert(updatedValue == "Loves coding and hiking.") {
      "Expected description update to Loves coding and hiking., but got $updatedValue"
    }
  }

  @Test
  fun testSettingsScreen_ErrorDisplay() {
    setUpScreen(
        uiState =
            sampleSettingsState().copy(emailError = "Invalid email", dayError = "Invalid day"))
    composeTestRule.onNodeWithText("Invalid email").assertIsDisplayed()
    composeTestRule.onNodeWithText("Invalid day").assertIsDisplayed()
  }

  @Test
  fun testSettingsScreen_BackButton() {
    var backClicked = false
    setUpScreen(onBack = { backClicked = true })
    composeTestRule.onNodeWithContentDescription("Back").performClick()
    assert(backClicked) { "Back button should trigger onBack action" }
  }
}
