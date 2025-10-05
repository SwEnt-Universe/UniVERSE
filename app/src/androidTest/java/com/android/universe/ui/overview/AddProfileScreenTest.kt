package com.android.universe.ui.overview

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.android.universe.model.user.FakeUserRepository
import com.android.universe.model.user.UserRepositoryProvider
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AddProfileScreenTest {
  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    UserRepositoryProvider.repository = FakeUserRepository()
    composeTestRule.setContent { AddProfileScreen() }
  }

  @Test
  fun displayAllComponents() {
    // Username
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.USERNAME_TEXT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.USERNAME_FIELD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.USERNAME_ERROR).assertIsNotDisplayed()

    // First name
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.FIRST_NAME_TEXT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.FIRST_NAME_FIELD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.FIRST_NAME_ERROR).assertIsNotDisplayed()

    // Last name
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.LAST_NAME_TEXT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.LAST_NAME_FIELD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.LAST_NAME_ERROR).assertIsNotDisplayed()

    // Description
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.DESCRIPTION_TEXT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.DESCRIPTION_FIELD).assertIsDisplayed()

    // Country
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.COUNTRY_TEXT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.COUNTRY_FIELD).assertIsDisplayed()
    composeTestRule
        .onAllNodesWithTag(AddProfileScreenTestTags.COUNTRY_DROPDOWN_ITEM_PREFIX)
        .assertCountEquals(0)

    // Date of birth
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.DATE_OF_BIRTH_TEXT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.DAY_FIELD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.DAY_ERROR_EMPTY).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.DAY_ERROR_NUMBER).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.MONTH_FIELD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.MONTH_ERROR_EMPTY).assertIsNotDisplayed()
    composeTestRule
        .onNodeWithTag(AddProfileScreenTestTags.MONTH_ERROR_NUMBER)
        .assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.YEAR_FIELD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.YEAR_ERROR_EMPTY).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.YEAR_ERROR_NUMBER).assertIsNotDisplayed()

    // Save button
    composeTestRule
        .onNodeWithTag(AddProfileScreenTestTags.SAVE_BUTTON)
        .performScrollTo()
        .assertIsDisplayed()
  }

  @Test
  fun canEnterUsername() {
    val text = "john_doe"

    // Enter text in the field
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.USERNAME_FIELD).performTextInput(text)

    // Assert that the field contains the entered text
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.USERNAME_FIELD).assertTextContains(text)

    // Assert that the error message is not displayed
    composeTestRule
        .onNodeWithTag(AddProfileScreenTestTags.USERNAME_ERROR, useUnmergedTree = true)
        .assertIsNotDisplayed()
  }

  @Test
  fun canEnterFirstName() {
    val text = "John"

    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.FIRST_NAME_FIELD).performTextInput(text)

    // Assert that the field contains the entered text
    composeTestRule
        .onNodeWithTag(AddProfileScreenTestTags.FIRST_NAME_FIELD)
        .assertTextContains(text)

    // Assert that the error message is not displayed
    composeTestRule
        .onNodeWithTag(AddProfileScreenTestTags.FIRST_NAME_ERROR, useUnmergedTree = true)
        .assertIsNotDisplayed()
  }

  @Test
  fun canEnterLastName() {
    val text = "Doe"

    // Enter text in the field
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.LAST_NAME_FIELD).performTextInput(text)

    // Assert that the field contains the entered text
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.LAST_NAME_FIELD).assertTextContains(text)

    // Assert that the error message is not displayed
    composeTestRule
        .onNodeWithTag(AddProfileScreenTestTags.LAST_NAME_ERROR, useUnmergedTree = true)
        .assertIsNotDisplayed()
  }

  @Test
  fun canEnterDescription() {
    val text = "I'm a default person"

    // Enter text in the field
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.DESCRIPTION_FIELD).performTextInput(text)

    // Assert that the field contains the entered text
    composeTestRule
        .onNodeWithTag(AddProfileScreenTestTags.DESCRIPTION_FIELD)
        .assertTextContains(text)
  }

  @Test
  fun countryFieldExpandsDropdown() {
    // Verify label and field are displayed
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.COUNTRY_TEXT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.COUNTRY_FIELD).assertIsDisplayed()

    // Initially, no dropdown items are visible
    composeTestRule
        .onAllNodesWithTag(AddProfileScreenTestTags.COUNTRY_DROPDOWN_ITEM_PREFIX)
        .assertCountEquals(0)

    // Click the country field to expand the dropdown
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.COUNTRY_FIELD).performClick()

    // Wait until the dropdown items appear (menu is expanded)
    composeTestRule.waitUntil(timeoutMillis = 2_000) {
      composeTestRule
          .onAllNodesWithTag(AddProfileScreenTestTags.COUNTRY_DROPDOWN_ITEM_PREFIX)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    // Assert that dropdown items are now visible
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.COUNTRY_TEXT).assertExists()
  }

  @Test
  fun canEnterDay() {
    val text = "1"

    // Enter text in the field
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.DAY_FIELD).performTextInput(text)

    // Assert that the field contains the entered text
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.DAY_FIELD).assertTextContains(text)

    // Assert that the error message is not displayed
    composeTestRule
        .onNodeWithTag(AddProfileScreenTestTags.DAY_ERROR_EMPTY, useUnmergedTree = true)
        .assertIsNotDisplayed()

    // Assert that the error message is not displayed
    composeTestRule
        .onNodeWithTag(AddProfileScreenTestTags.DAY_ERROR_NUMBER, useUnmergedTree = true)
        .assertIsNotDisplayed()
  }

  @Test
  fun canEnterMonth() {
    val text = "1"

    // Enter text in the field
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.MONTH_FIELD).performTextInput(text)

    // Assert that the field contains the entered text
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.MONTH_FIELD).assertTextContains(text)

    // Assert that the error message is not displayed
    composeTestRule
        .onNodeWithTag(AddProfileScreenTestTags.MONTH_ERROR_EMPTY, useUnmergedTree = true)
        .assertIsNotDisplayed()

    // Assert that the error message is not displayed
    composeTestRule
        .onNodeWithTag(AddProfileScreenTestTags.MONTH_ERROR_NUMBER, useUnmergedTree = true)
        .assertIsNotDisplayed()
  }

  @Test
  fun canEnterYear() {
    val text = "2025"

    // Enter text in the field
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.YEAR_FIELD).performTextInput(text)

    // Assert that the field contains the entered text
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.YEAR_FIELD).assertTextContains(text)

    // Assert that the error message is not displayed
    composeTestRule
        .onNodeWithTag(AddProfileScreenTestTags.YEAR_ERROR_EMPTY, useUnmergedTree = true)
        .assertIsNotDisplayed()

    // Assert that the error message is not displayed
    composeTestRule
        .onNodeWithTag(AddProfileScreenTestTags.YEAR_ERROR_NUMBER, useUnmergedTree = true)
        .assertIsNotDisplayed()
  }

  @Test
  fun showsUsernameErrorWhenEmpty() {
    // Focus the username field
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.USERNAME_FIELD).performClick()

    // Move focus away to trigger validation
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.FIRST_NAME_FIELD).performClick()

    // Assert that the error message is displayed
    composeTestRule
        .onNodeWithTag(AddProfileScreenTestTags.USERNAME_ERROR, useUnmergedTree = true)
        .assertExists()
  }

  @Test
  fun showsFirstNameErrorWhenEmpty() {
    // Focus the username field
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.FIRST_NAME_FIELD).performClick()

    // Move focus away to trigger validation
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.FIRST_NAME_FIELD).performClick()

    // Assert that the error message is displayed
    composeTestRule
        .onNodeWithTag(AddProfileScreenTestTags.FIRST_NAME_ERROR, useUnmergedTree = true)
        .assertExists()
  }

  @Test
  fun showsLastNameErrorWhenEmpty() {
    // Focus the username field
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.LAST_NAME_FIELD).performClick()

    // Move focus away to trigger validation
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.LAST_NAME_FIELD).performClick()

    // Assert that the error message is displayed
    composeTestRule
        .onNodeWithTag(AddProfileScreenTestTags.LAST_NAME_ERROR, useUnmergedTree = true)
        .assertExists()
  }

  @Test
  fun showsDayErrorWhenEmpty() {
    // Focus the username field
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.DAY_FIELD).performClick()

    // Move focus away to trigger validation
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.DAY_FIELD).performClick()

    // Assert that the error message is displayed
    composeTestRule
        .onNodeWithTag(AddProfileScreenTestTags.DAY_ERROR_EMPTY, useUnmergedTree = true)
        .assertExists()
  }

  @Test
  fun showsDayErrorWhenNotNumber() {
    val text = "hello"
    // Focus the username field
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.DAY_FIELD).performTextInput(text)

    // Move focus away to trigger validation
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.DAY_FIELD).performClick()

    // Assert that the error message is displayed
    composeTestRule
        .onNodeWithTag(AddProfileScreenTestTags.DAY_ERROR_NUMBER, useUnmergedTree = true)
        .assertExists()
  }

  @Test
  fun showsMonthErrorWhenEmpty() {
    // Focus the username field
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.MONTH_FIELD).performClick()

    // Move focus away to trigger validation
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.MONTH_FIELD).performClick()

    // Assert that the error message is displayed
    composeTestRule
        .onNodeWithTag(AddProfileScreenTestTags.MONTH_ERROR_EMPTY, useUnmergedTree = true)
        .assertExists()
  }

  @Test
  fun showsMonthErrorWhenNotNumber() {
    val text = "hello"
    // Focus the username field
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.MONTH_FIELD).performTextInput(text)

    // Move focus away to trigger validation
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.MONTH_FIELD).performClick()

    // Assert that the error message is displayed
    composeTestRule
        .onNodeWithTag(AddProfileScreenTestTags.MONTH_ERROR_NUMBER, useUnmergedTree = true)
        .assertExists()
  }

  @Test
  fun showsYearErrorWhenEmpty() {
    // Focus the username field
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.YEAR_FIELD).performClick()

    // Move focus away to trigger validation
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.YEAR_FIELD).performClick()

    // Assert that the error message is displayed
    composeTestRule
        .onNodeWithTag(AddProfileScreenTestTags.YEAR_ERROR_EMPTY, useUnmergedTree = true)
        .assertExists()
  }

  @Test
  fun showsYearErrorWhenNotNumber() {
    val text = "hello"
    // Focus the username field
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.YEAR_FIELD).performTextInput(text)

    // Move focus away to trigger validation
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.YEAR_FIELD).performClick()

    // Assert that the error message is displayed
    composeTestRule
        .onNodeWithTag(AddProfileScreenTestTags.YEAR_ERROR_NUMBER, useUnmergedTree = true)
        .assertExists()
  }

  @Test
  fun saveButtonEnabledOnlyWhenAllRequiredFieldsAreValid() {
    // Initially, the Save button should be disabled
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.SAVE_BUTTON).assertIsNotEnabled()

    // Fill all required fields with valid values
    composeTestRule
        .onNodeWithTag(AddProfileScreenTestTags.USERNAME_FIELD)
        .performTextInput("john_doe")
    composeTestRule
        .onNodeWithTag(AddProfileScreenTestTags.FIRST_NAME_FIELD)
        .performTextInput("John")
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.LAST_NAME_FIELD).performTextInput("Doe")
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.DAY_FIELD).performTextInput("1")
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.MONTH_FIELD).performTextInput("1")
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.YEAR_FIELD).performTextInput("2025")

    // After entering valid inputs, the Save button should be enabled
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.SAVE_BUTTON).assertIsEnabled()
  }
}
