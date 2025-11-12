package com.android.universe.ui.profileSettings

import com.android.universe.ui.common.ErrorMessages
import com.android.universe.ui.common.InputLimits
import com.android.universe.ui.common.ValidationResult
import com.android.universe.ui.common.toTitleCase
import com.android.universe.ui.common.validateBirthDate
import com.android.universe.ui.common.validateCountry
import com.android.universe.ui.common.validateDay
import com.android.universe.ui.common.validateDescription
import com.android.universe.ui.common.validateEmail
import com.android.universe.ui.common.validateFirstName
import com.android.universe.ui.common.validateLastName
import com.android.universe.ui.common.validateMonth
import com.android.universe.ui.common.validatePassword
import com.android.universe.ui.common.validateUsername
import com.android.universe.ui.common.validateYear
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ProfileValidatorsTest {

  // A mock map to test country validation
  private val mockCountryMap = mapOf("Switzerland" to "CH", "France" to "FR", "Germany" to "DE")

  @Before
  fun setUp() {
  }

  // --- Email Validation ---

  @Test
  fun emailIsValid() {
    val email = "sample.email@epfl.ch"
    val result = validateEmail(email)

    assert(result is ValidationResult.Valid)
  }

  @Test
  fun emailIsEmpty() {
    val email = ""
    val result = validateEmail(email)

    assert(result is ValidationResult.Invalid)
    assertEquals(ErrorMessages.EMAIL_EMPTY, (result as ValidationResult.Invalid).errorMessage)
  }

  @Test
  fun emailIsBlank() {
    val email = "   "
    val result = validateEmail(email)

    assert(result is ValidationResult.Invalid)
    assertEquals(ErrorMessages.EMAIL_EMPTY, (result as ValidationResult.Invalid).errorMessage)
  }

  @Test
  fun emailIsTooLong() {
    val localPart = "a".repeat(InputLimits.EMAIL_MAX_LENGTH - "@epfl.ch".length + 1)
    val email = localPart + "@epfl.ch"
    val result = validateEmail(email)

    assert(result is ValidationResult.Invalid)
    assertEquals(
      ErrorMessages.EMAIL_TOO_LONG.format(InputLimits.EMAIL_MAX_LENGTH),
      (result as ValidationResult.Invalid).errorMessage
    )
  }

  @Test
  fun emailIsNotEPFL() {
    val email = "sample.email@gmail.ch"
    val result = validateEmail(email)

    assert(result is ValidationResult.Invalid)
    assertEquals(ErrorMessages.EMAIL_NOT_EPFL, (result as ValidationResult.Invalid).errorMessage)
  }

  @Test
  fun emailHasInvalidChars() {
    val email = "sample email@epfl.ch" // Contains a space
    val result = validateEmail(email)

    assert(result is ValidationResult.Invalid)
    assertEquals(ErrorMessages.EMAIL_NOT_EPFL, (result as ValidationResult.Invalid).errorMessage)
  }

  // --- Password Validation ---

  @Test
  fun passwordIsValid() {
    val password = "a".repeat(InputLimits.PASSWORD_MIN_LENGTH)
    val result = validatePassword(password)
    assert(result is ValidationResult.Valid)
  }

  @Test
  fun passwordIsValidLonger() {
    val password = "a".repeat(InputLimits.PASSWORD_MIN_LENGTH + 10)
    val result = validatePassword(password)
    assert(result is ValidationResult.Valid)
  }

  @Test
  fun passwordIsTooShort() {
    val password = "a".repeat(InputLimits.PASSWORD_MIN_LENGTH - 1)
    val result = validatePassword(password)
    assert(result is ValidationResult.Invalid)
    assertEquals(
      ErrorMessages.PASSWORD_TOO_SHORT.format(InputLimits.PASSWORD_MIN_LENGTH),
      (result as ValidationResult.Invalid).errorMessage
    )
  }

  // --- Username Validation ---

  @Test
  fun usernameIsValid() {
    val username = "valid.user-name_123"
    val result = validateUsername(username)
    assert(result is ValidationResult.Valid)
  }

  @Test
  fun usernameIsValidAtMaxLength() {
    val username = "a".repeat(InputLimits.USERNAME)
    val result = validateUsername(username)
    assert(result is ValidationResult.Valid)
  }

  @Test
  fun usernameIsEmpty() {
    val username = ""
    val result = validateUsername(username)
    assert(result is ValidationResult.Invalid)
    assertEquals(ErrorMessages.USERNAME_EMPTY, (result as ValidationResult.Invalid).errorMessage)
  }

  @Test
  fun usernameIsBlank() {
    val username = "   "
    val result = validateUsername(username)
    assert(result is ValidationResult.Invalid)
    assertEquals(ErrorMessages.USERNAME_EMPTY, (result as ValidationResult.Invalid).errorMessage)
  }

  @Test
  fun usernameIsTooLong() {
    val username = "a".repeat(InputLimits.USERNAME + 1)
    val result = validateUsername(username)
    assert(result is ValidationResult.Invalid)
    assertEquals(
      ErrorMessages.USERNAME_TOO_LONG.format(InputLimits.USERNAME),
      (result as ValidationResult.Invalid).errorMessage
    )
  }

  @Test
  fun usernameHasInvalidFormat() {
    val username = "invalid user!" // space and !
    val result = validateUsername(username)
    assert(result is ValidationResult.Invalid)
    assertEquals(
      ErrorMessages.USERNAME_INVALID_FORMAT, (result as ValidationResult.Invalid).errorMessage
    )
  }

  // --- First Name Validation ---

  @Test
  fun firstNameIsValid() {
    val name = "Valid-Name"
    val result = validateFirstName(name)
    assert(result is ValidationResult.Valid)
  }

  @Test
  fun firstNameIsValidWithApostrophe() {
    val name = "O'Malley"
    val result = validateFirstName(name)
    assert(result is ValidationResult.Valid)
  }

  @Test
  fun firstNameIsValidWithAccentsAndSpace() {
    val name = "José Müller"
    val result = validateFirstName(name)
    assert(result is ValidationResult.Valid)
  }

  @Test
  fun firstNameIsEmpty() {
    val name = ""
    val result = validateFirstName(name)
    assert(result is ValidationResult.Invalid)
    assertEquals(ErrorMessages.FIRSTNAME_EMPTY, (result as ValidationResult.Invalid).errorMessage)
  }

  @Test
  fun firstNameIsBlank() {
    val name = "  "
    val result = validateFirstName(name)
    assert(result is ValidationResult.Invalid)
    assertEquals(ErrorMessages.FIRSTNAME_EMPTY, (result as ValidationResult.Invalid).errorMessage)
  }

  @Test
  fun firstNameIsTooLong() {
    val name = "a".repeat(InputLimits.FIRST_NAME + 1)
    val result = validateFirstName(name)
    assert(result is ValidationResult.Invalid)
    assertEquals(
      ErrorMessages.FIRSTNAME_TOO_LONG.format(InputLimits.FIRST_NAME),
      (result as ValidationResult.Invalid).errorMessage
    )
  }

  @Test
  fun firstNameHasInvalidFormatNumbers() {
    val name = "Invalid123"
    val result = validateFirstName(name)
    assert(result is ValidationResult.Invalid)
    assertEquals(
      ErrorMessages.FIRSTNAME_INVALID_FORMAT, (result as ValidationResult.Invalid).errorMessage
    )
  }

  @Test
  fun firstNameHasInvalidFormatSymbols() {
    val name = "Invalid!"
    val result = validateFirstName(name)
    assert(result is ValidationResult.Invalid)
    assertEquals(
      ErrorMessages.FIRSTNAME_INVALID_FORMAT, (result as ValidationResult.Invalid).errorMessage
    )
  }

  // --- Last Name Validation ---

  @Test
  fun lastNameIsValid() {
    val name = "Valid-Name"
    val result = validateLastName(name)
    assert(result is ValidationResult.Valid)
  }

  @Test
  fun lastNameIsValidWithApostrophe() {
    val name = "O'Malley"
    val result = validateLastName(name)
    assert(result is ValidationResult.Valid)
  }

  @Test
  fun lastNameIsValidWithAccentsAndSpace() {
    val name = "José Müller"
    val result = validateLastName(name)
    assert(result is ValidationResult.Valid)
  }

  @Test
  fun lastNameIsEmpty() {
    val name = ""
    val result = validateLastName(name)
    assert(result is ValidationResult.Invalid)
    assertEquals(ErrorMessages.LASTNAME_EMPTY, (result as ValidationResult.Invalid).errorMessage)
  }

  @Test
  fun lastNameIsBlank() {
    val name = "  "
    val result = validateLastName(name)
    assert(result is ValidationResult.Invalid)
    assertEquals(ErrorMessages.LASTNAME_EMPTY, (result as ValidationResult.Invalid).errorMessage)
  }

  @Test
  fun lastNameIsTooLong() {
    val name = "a".repeat(InputLimits.LAST_NAME + 1)
    val result = validateLastName(name)
    assert(result is ValidationResult.Invalid)
    assertEquals(
      ErrorMessages.LASTNAME_TOO_LONG.format(InputLimits.LAST_NAME),
      (result as ValidationResult.Invalid).errorMessage
    )
  }

  @Test
  fun lastNameHasInvalidFormat() {
    val name = "Invalid123"
    val result = validateLastName(name)
    assert(result is ValidationResult.Invalid)
    assertEquals(
      ErrorMessages.LASTNAME_INVALID_FORMAT, (result as ValidationResult.Invalid).errorMessage
    )
  }

  // --- Description Validation ---

  @Test
  fun descriptionIsValid() {
    val description = "This is a valid description."
    val result = validateDescription(description)
    assert(result is ValidationResult.Valid)
  }

  @Test
  fun descriptionIsValidEmpty() {
    val description = ""
    val result = validateDescription(description)
    assert(result is ValidationResult.Valid)
  }

  @Test
  fun descriptionIsValidAtMaxLength() {
    val description = "a".repeat(InputLimits.DESCRIPTION)
    val result = validateDescription(description)
    assert(result is ValidationResult.Valid)
  }

  @Test
  fun descriptionIsTooLong() {
    val description = "a".repeat(InputLimits.DESCRIPTION + 1)
    val result = validateDescription(description)
    assert(result is ValidationResult.Invalid)
    assertEquals(
      ErrorMessages.DESCRIPTION_TOO_LONG.format(InputLimits.DESCRIPTION),
      (result as ValidationResult.Invalid).errorMessage
    )
  }

  // --- Country Validation ---

  @Test
  fun countryIsValid() {
    val country = "Switzerland"
    val result = validateCountry(country, mockCountryMap)
    assert(result is ValidationResult.Valid)
  }

  @Test
  fun countryIsEmpty() {
    val country = ""
    val result = validateCountry(country, mockCountryMap)
    assert(result is ValidationResult.Invalid)
    assertEquals(ErrorMessages.COUNTRY_EMPTY, (result as ValidationResult.Invalid).errorMessage)
  }

  @Test
  fun countryIsBlank() {
    val country = "  "
    val result = validateCountry(country, mockCountryMap)
    assert(result is ValidationResult.Invalid)
    assertEquals(ErrorMessages.COUNTRY_EMPTY, (result as ValidationResult.Invalid).errorMessage)
  }

  @Test
  fun countryIsInvalid() {
    val country = "Invalid Country" // Not in the mock map
    val result = validateCountry(country, mockCountryMap)
    assert(result is ValidationResult.Invalid)
    assertEquals(ErrorMessages.COUNTRY_INVALID, (result as ValidationResult.Invalid).errorMessage)
  }

  // --- Day Validation ---

  @Test
  fun dayIsValid() {
    val day = "15"
    val result = validateDay(day)
    assert(result is ValidationResult.Valid)
  }

  @Test
  fun dayIsValidLowerBound() {
    val day = "1"
    val result = validateDay(day)
    assert(result is ValidationResult.Valid)
  }

  @Test
  fun dayIsValidUpperBound() {
    val day = "31"
    val result = validateDay(day)
    assert(result is ValidationResult.Valid)
  }

  @Test
  fun dayIsEmpty() {
    val day = ""
    val result = validateDay(day)
    assert(result is ValidationResult.Invalid)
    assertEquals(ErrorMessages.DAY_EMPTY, (result as ValidationResult.Invalid).errorMessage)
  }

  @Test
  fun dayIsBlank() {
    val day = "  "
    val result = validateDay(day)
    assert(result is ValidationResult.Invalid)
    assertEquals(ErrorMessages.DAY_EMPTY, (result as ValidationResult.Invalid).errorMessage)
  }

  @Test
  fun dayIsInvalidNumber() {
    val day = "abc"
    val result = validateDay(day)
    assert(result is ValidationResult.Invalid)
    assertEquals(
      ErrorMessages.DAY_INVALID_NUMBER, (result as ValidationResult.Invalid).errorMessage
    )
  }

  @Test
  fun dayIsInvalidDecimal() {
    val day = "1.5"
    val result = validateDay(day)
    assert(result is ValidationResult.Invalid)
    assertEquals(
      ErrorMessages.DAY_INVALID_NUMBER, (result as ValidationResult.Invalid).errorMessage
    )
  }

  @Test
  fun dayIsOutOfRangeLow() {
    val day = "0"
    val result = validateDay(day)
    assert(result is ValidationResult.Invalid)
    assertEquals(ErrorMessages.DAY_OUT_OF_RANGE, (result as ValidationResult.Invalid).errorMessage)
  }

  @Test
  fun dayIsOutOfRangeHigh() {
    val day = "32"
    val result = validateDay(day)
    assert(result is ValidationResult.Invalid)
    assertEquals(ErrorMessages.DAY_OUT_OF_RANGE, (result as ValidationResult.Invalid).errorMessage)
  }

  // --- Month Validation ---

  @Test
  fun monthIsValid() {
    val month = "6"
    val result = validateMonth(month)
    assert(result is ValidationResult.Valid)
  }

  @Test
  fun monthIsValidLowerBound() {
    val month = "1"
    val result = validateMonth(month)
    assert(result is ValidationResult.Valid)
  }

  @Test
  fun monthIsValidUpperBound() {
    val month = "12"
    val result = validateMonth(month)
    assert(result is ValidationResult.Valid)
  }

  @Test
  fun monthIsEmpty() {
    val month = ""
    val result = validateMonth(month)
    assert(result is ValidationResult.Invalid)
    assertEquals(ErrorMessages.MONTH_EMPTY, (result as ValidationResult.Invalid).errorMessage)
  }

  @Test
  fun monthIsBlank() {
    val month = "   "
    val result = validateMonth(month)
    assert(result is ValidationResult.Invalid)
    assertEquals(ErrorMessages.MONTH_EMPTY, (result as ValidationResult.Invalid).errorMessage)
  }

  @Test
  fun monthIsInvalidNumber() {
    val month = "July"
    val result = validateMonth(month)
    assert(result is ValidationResult.Invalid)
    assertEquals(
      ErrorMessages.MONTH_INVALID_NUMBER, (result as ValidationResult.Invalid).errorMessage
    )
  }

  @Test
  fun monthIsOutOfRangeLow() {
    val month = "0"
    val result = validateMonth(month)
    assert(result is ValidationResult.Invalid)
    assertEquals(
      ErrorMessages.MONTH_OUT_OF_RANGE, (result as ValidationResult.Invalid).errorMessage
    )
  }

  @Test
  fun monthIsOutOfRangeHigh() {
    val month = "13"
    val result = validateMonth(month)
    assert(result is ValidationResult.Invalid)
    assertEquals(
      ErrorMessages.MONTH_OUT_OF_RANGE, (result as ValidationResult.Invalid).errorMessage
    )
  }

  // --- Year Validation ---

  @Test
  fun yearIsValid() {
    val year = "2000"
    val result = validateYear(year)
    assert(result is ValidationResult.Valid)
  }

  @Test
  fun yearIsValidLowerBound() {
    val year = InputLimits.MIN_BIRTH_YEAR.toString() // "1900"
    val result = validateYear(year)
    assert(result is ValidationResult.Valid)
  }

  @Test
  fun yearIsValidUpperBound() {
    val currentYear = LocalDate.now().year
    val year = currentYear.toString()
    val result = validateYear(year)
    assert(result is ValidationResult.Valid)
  }

  @Test
  fun yearIsEmpty() {
    val year = ""
    val result = validateYear(year)
    assert(result is ValidationResult.Invalid)
    assertEquals(ErrorMessages.YEAR_EMPTY, (result as ValidationResult.Invalid).errorMessage)
  }

  @Test
  fun yearIsBlank() {
    val year = "  "
    val result = validateYear(year)
    assert(result is ValidationResult.Invalid)
    assertEquals(ErrorMessages.YEAR_EMPTY, (result as ValidationResult.Invalid).errorMessage)
  }

  @Test
  fun yearIsInvalidNumber() {
    val year = "abc"
    val result = validateYear(year)
    assert(result is ValidationResult.Invalid)
    assertEquals(
      ErrorMessages.YEAR_INVALID_NUMBER, (result as ValidationResult.Invalid).errorMessage
    )
  }

  @Test
  fun yearIsOutOfRangeLow() {
    val year = (InputLimits.MIN_BIRTH_YEAR - 1).toString() // "1899"
    val result = validateYear(year)
    val currentYear = LocalDate.now().year
    assert(result is ValidationResult.Invalid)
    assertEquals(
      ErrorMessages.YEAR_OUT_OF_RANGE.format(InputLimits.MIN_BIRTH_YEAR, currentYear),
      (result as ValidationResult.Invalid).errorMessage
    )
  }

  @Test
  fun yearIsOutOfRangeHigh() {
    val currentYear = LocalDate.now().year
    val year = (currentYear + 1).toString()
    val result = validateYear(year)
    assert(result is ValidationResult.Invalid)
    assertEquals(
      ErrorMessages.YEAR_OUT_OF_RANGE.format(InputLimits.MIN_BIRTH_YEAR, currentYear),
      (result as ValidationResult.Invalid).errorMessage
    )
  }

  // --- BirthDate Validation ---

  @Test
  fun birthDateIsValid() {
    val today = LocalDate.now()
    val validDob = today.minusYears((InputLimits.MIN_AGE + 5).toLong()) // e.g., 18 years old
    val result = validateBirthDate(validDob.dayOfMonth, validDob.monthValue, validDob.year)
    assert(result is ValidationResult.Valid)
  }

  @Test
  fun birthDateIsValidOnMinAgeBirthday() {
    val today = LocalDate.now()
    val validDob = today.minusYears((InputLimits.MIN_AGE).toLong()) // Exactly 13 today
    val result = validateBirthDate(validDob.dayOfMonth, validDob.monthValue, validDob.year)
    assert(result is ValidationResult.Valid)
  }

  @Test
  fun birthDateIsValidLeapYear() {
    // Feb 29th on a leap year, assuming 2000 was > MIN_AGE years ago
    val result = validateBirthDate(29, 2, 2000)
    assert(result is ValidationResult.Valid)
  }

  @Test
  fun birthDateIsInvalidLogical() {
    // February 30th
    val result = validateBirthDate(30, 2, 2000)
    assert(result is ValidationResult.Invalid)
    assertEquals(
      ErrorMessages.DATE_INVALID_LOGICAL, (result as ValidationResult.Invalid).errorMessage
    )
  }

  @Test
  fun birthDateIsInvalidLogicalNonLeapYear() {
    // Feb 29th on a non-leap year
    val result = validateBirthDate(29, 2, 2001)
    assert(result is ValidationResult.Invalid)
    assertEquals(
      ErrorMessages.DATE_INVALID_LOGICAL, (result as ValidationResult.Invalid).errorMessage
    )
  }

  @Test
  fun birthDateIsInFuture() {
    val today = LocalDate.now()
    val futureDate = today.plusDays(1)
    val result = validateBirthDate(futureDate.dayOfMonth, futureDate.monthValue, futureDate.year)
    assert(result is ValidationResult.Invalid)
    assertEquals(ErrorMessages.DATE_IN_FUTURE, (result as ValidationResult.Invalid).errorMessage)
  }

  @Test
  fun birthDateIsTooYoung() {
    val today = LocalDate.now()
    // 1 day before 13th birthday (e.g., 12 years and 364 days old)
    val tooYoungDate = today.minusYears((InputLimits.MIN_AGE).toLong()).plusDays(1)
    val result =
      validateBirthDate(tooYoungDate.dayOfMonth, tooYoungDate.monthValue, tooYoungDate.year)
    assert(result is ValidationResult.Invalid)
    assertEquals(
      ErrorMessages.DATE_TOO_YOUNG.format(InputLimits.MIN_AGE),
      (result as ValidationResult.Invalid).errorMessage
    )
  }

  @Test
  fun toTitleCase_capitalizesSingleWord() {
    assertEquals("Hello", "hello".toTitleCase())
  }

  @Test
  fun toTitleCase_capitalizesMultipleWords() {
    assertEquals("Hello World", "hello world".toTitleCase())
  }

  @Test
  fun toTitleCase_convertsMixedCaseCorrectly() {
    assertEquals("Hello World", "hElLo wOrLd".toTitleCase())
  }

  @Test
  fun toTitleCase_handlesSingleLetterWords() {
    assertEquals("A B C", "a b c".toTitleCase())
  }
}
