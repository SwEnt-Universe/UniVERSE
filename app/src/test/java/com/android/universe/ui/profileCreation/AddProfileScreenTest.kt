package com.android.universe.ui.profileCreation

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.ui.theme.UniverseTheme
import com.android.universe.utils.CustomSemanticsMatcher.hasTestTagPrefix
import com.android.universe.utils.FirestoreUserTest
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AddProfileScreenTest : FirestoreUserTest() {
  @get:Rule val composeTestRule = createComposeRule()
  val uid = "AddProfileScreenTest"
  private var onBackSpy: () -> Unit = {}

  @Before
  override fun setUp() {
    super.setUp()
    composeTestRule.setContent {
      UniverseTheme {
        AddProfileScreen(
            uid = uid,
            addProfileViewModel = viewModel(),
            navigateOnSave = {},
            onBack = { onBackSpy() })
      }
    }
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
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.DAY_ERROR).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.MONTH_FIELD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.MONTH_ERROR).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.YEAR_FIELD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.YEAR_ERROR).assertIsNotDisplayed()

    // Save button
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.SAVE_BUTTON).assertIsDisplayed()
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
        .onAllNodes(hasTestTagPrefix(AddProfileScreenTestTags.COUNTRY_DROPDOWN_ITEM_PREFIX))
        .assertCountEquals(0)

    // Click the country field to expand the dropdown
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.COUNTRY_FIELD).performClick()

    // Wait until the dropdown items appear (menu is expanded)
    composeTestRule.waitUntil(timeoutMillis = 2_000) {
      composeTestRule
          .onAllNodes(hasTestTagPrefix(AddProfileScreenTestTags.COUNTRY_DROPDOWN_ITEM_PREFIX))
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
        .onNodeWithTag(AddProfileScreenTestTags.DAY_ERROR, useUnmergedTree = true)
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
        .onNodeWithTag(AddProfileScreenTestTags.MONTH_ERROR, useUnmergedTree = true)
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
        .onNodeWithTag(AddProfileScreenTestTags.YEAR_ERROR, useUnmergedTree = true)
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
        .onNodeWithTag(AddProfileScreenTestTags.DAY_ERROR, useUnmergedTree = true)
        .assertExists()
  }

  @Test
  fun showsDayEmptyErrorWhenNonDigitsInput() {
    val text = "hello"
    // Focus the username field
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.DAY_FIELD).performTextInput(text)

    // Move focus away to trigger validation
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.DAY_FIELD).performClick()

    // Assert that the error message is displayed
    composeTestRule
        .onNodeWithTag(AddProfileScreenTestTags.DAY_ERROR, useUnmergedTree = true)
        .assertExists()
        .assertTextEquals("Day cannot be empty")
  }

  @Test
  fun showsDayErrorWhenErroneousInput() {
    val text = "32"
    // Focus the username field
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.DAY_FIELD).performTextInput(text)

    // Move focus away to trigger validation
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.DAY_FIELD).performClick()

    // Assert that the error message is displayed
    composeTestRule
        .onNodeWithTag(AddProfileScreenTestTags.DAY_ERROR, useUnmergedTree = true)
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
        .onNodeWithTag(AddProfileScreenTestTags.MONTH_ERROR, useUnmergedTree = true)
        .assertExists()
  }

  @Test
  fun showsMonthErrorWhenErroneousMonth() {
    val text = "13"
    // Focus the username field
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.MONTH_FIELD).performTextInput(text)

    // Move focus away to trigger validation
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.MONTH_FIELD).performClick()

    // Assert that the error message is displayed
    composeTestRule
        .onNodeWithTag(AddProfileScreenTestTags.MONTH_ERROR, useUnmergedTree = true)
        .assertExists()
  }

  @Test
  fun showsMonthEmptyErrorWhenNonDigitsInput() {
    val text = "hello"
    // Focus the username field
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.MONTH_FIELD).performTextInput(text)

    // Move focus away to trigger validation
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.MONTH_FIELD).performClick()

    // Assert that the error message is displayed
    composeTestRule
        .onNodeWithTag(AddProfileScreenTestTags.MONTH_ERROR, useUnmergedTree = true)
        .assertExists()
        .assertTextEquals("Month cannot be empty")
  }

  @Test
  fun showsYearErrorWhenEmpty() {
    // Focus the username field
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.YEAR_FIELD).performClick()

    // Move focus away to trigger validation
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.YEAR_FIELD).performClick()

    // Assert that the error message is displayed
    composeTestRule
        .onNodeWithTag(AddProfileScreenTestTags.YEAR_ERROR, useUnmergedTree = true)
        .assertExists()
  }

  @Test
  fun showsYearEmptyErrorWhenNonDigitsInput() {
    val text = "hello"
    // Focus the username field
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.YEAR_FIELD).performTextInput(text)

    // Move focus away to trigger validation
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.YEAR_FIELD).performClick()

    // Assert that the error message is displayed
    composeTestRule
        .onNodeWithTag(AddProfileScreenTestTags.YEAR_ERROR, useUnmergedTree = true)
        .assertExists()
        .assertTextEquals("Year cannot be empty")
  }

  @Test
  fun showsYearErrorWhenInvalidYear1() {
    val text = "1600"
    // Focus the username field
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.YEAR_FIELD).performTextInput(text)

    // Move focus away to trigger validation
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.YEAR_FIELD).performClick()

    // Assert that the error message is displayed
    composeTestRule
        .onNodeWithTag(AddProfileScreenTestTags.YEAR_ERROR, useUnmergedTree = true)
        .assertExists()
  }

  @Test
  fun showsYearErrorWhenInvalidYear2() {
    val text = "8000"
    // Focus the username field
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.YEAR_FIELD).performTextInput(text)

    // Move focus away to trigger validation
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.YEAR_FIELD).performClick()

    // Assert that the error message is displayed
    composeTestRule
        .onNodeWithTag(AddProfileScreenTestTags.YEAR_ERROR, useUnmergedTree = true)
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
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.YEAR_FIELD).performTextInput("2005")

    // After entering valid inputs, the Save button should be enabled
    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.SAVE_BUTTON).assertIsEnabled()
  }

  @Test
  fun showsUsernameErrorWhenTooLongAndCuts() {

    composeTestRule
        .onNodeWithTag(AddProfileScreenTestTags.USERNAME_FIELD)
        .performTextInput("A".repeat(InputLimits.USERNAME + 20))

    // Assert that the error message is displayed and right
    composeTestRule
        .onNodeWithTag(AddProfileScreenTestTags.USERNAME_ERROR, useUnmergedTree = true)
        .assertExists()
        .assertTextEquals("Username is too long")
  }

  @Test
  fun showsFirstnameErrorWhenTooLongAndCuts() {

    composeTestRule
        .onNodeWithTag(AddProfileScreenTestTags.FIRST_NAME_FIELD)
        .performTextInput("A".repeat(InputLimits.FIRST_NAME + 20))

    // Assert that the error message is displayed and right
    composeTestRule
        .onNodeWithTag(AddProfileScreenTestTags.FIRST_NAME_ERROR, useUnmergedTree = true)
        .assertExists()
        .assertTextEquals("First name too long")
  }

  @Test
  fun showsLastnameErrorWhenTooLongAndCuts() {

    composeTestRule
        .onNodeWithTag(AddProfileScreenTestTags.LAST_NAME_FIELD)
        .performTextInput("A".repeat(InputLimits.LAST_NAME + 20))

    // Assert that the error message is displayed and right
    composeTestRule
        .onNodeWithTag(AddProfileScreenTestTags.LAST_NAME_ERROR, useUnmergedTree = true)
        .assertExists()
        .assertTextEquals("Last name too long")
  }

  @Test
  fun showsDescriptionErrorWhenTooLongAndCuts() {

    composeTestRule
        .onNodeWithTag(AddProfileScreenTestTags.DESCRIPTION_FIELD)
        .performTextInput("A".repeat(InputLimits.DESCRIPTION + 20))

    // Assert that the error message is displayed and right
    composeTestRule
        .onNodeWithTag(AddProfileScreenTestTags.DESCRIPTION_ERROR, useUnmergedTree = true)
        .assertExists()
        .assertTextEquals("Description too long")
  }

  @Test
  fun showsUsernameErrorWhenForbiddenCharacters() {

    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.USERNAME_FIELD).performTextInput("a ")

    // Assert that the error message is displayed and right
    composeTestRule
        .onNodeWithTag(AddProfileScreenTestTags.USERNAME_ERROR, useUnmergedTree = true)
        .assertExists()
        .assertTextEquals(
            "Invalid username format, allowed characters are letters, numbers, dots, underscores, or dashes")
  }

  @Test
  fun showsFirstnameErrorWhenForbiddenCharacters() {

    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.FIRST_NAME_FIELD).performTextInput("10 ")

    // Assert that the error message is displayed and right
    composeTestRule
        .onNodeWithTag(AddProfileScreenTestTags.FIRST_NAME_ERROR, useUnmergedTree = true)
        .assertExists()
        .assertTextEquals("Invalid First name format")
  }

  @Test
  fun showsLastnameErrorWhenForbiddenCharacters() {

    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.LAST_NAME_FIELD).performTextInput("10 ")

    // Assert that the error message is displayed and right
    composeTestRule
        .onNodeWithTag(AddProfileScreenTestTags.LAST_NAME_ERROR, useUnmergedTree = true)
        .assertExists()
        .assertTextEquals("Invalid Last name format")
  }

  @Test
  fun backButtonInvokesOnBackCallback() {
    var wasCalled = false
    onBackSpy = { wasCalled = true }

    composeTestRule.onNodeWithTag(AddProfileScreenTestTags.BACK_BUTTON).performClick()

    assertTrue(wasCalled)
  }
}
