package com.android.universe.ui.profileSettings

import com.android.universe.ui.common.sanitize
import com.android.universe.ui.common.validateAll
import com.android.universe.ui.common.validateDateTriple
import com.android.universe.ui.common.validateDescription
import com.android.universe.ui.common.validateEmail
import com.android.universe.ui.common.validateName
import com.android.universe.ui.common.validateNonEmpty
import com.android.universe.ui.common.validatePassword
import java.time.LocalDate
import org.junit.Assert.*
import org.junit.Test

class ProfileValidatorsTest {

  @Test
  fun validateName_blank() {
    assertEquals("First name cannot be empty", validateName("First name", ""))
  }

  @Test
  fun validateName_invalidChars() {
    assertEquals("Invalid First name format", validateName("First name", "John123"))
  }

  @Test
  fun validateName_tooLong() {
    assertEquals("First name too long", validateName("First name", "A".repeat(26)))
  }

  @Test
  fun validateName_ok() {
    assertNull(validateName("Last name", "O'Connor"))
  }

  // --- validateEmail ---
  @Test
  fun validateEmail_blank() {
    assertEquals("Email cannot be empty", validateEmail(""))
  }

  @Test
  fun validateEmail_missingAt() {
    assertEquals("Invalid email format", validateEmail("user.example.com"))
  }

  @Test
  fun validateEmail_nonEPFL() {
    assertEquals("Not an EPFL email address", validateEmail("user@example.com"))
  }

  @Test
  fun validateEmail_ok() {
    assertNull(validateEmail("user@epfl.ch"))
  }

  // --- validatePassword ---
  @Test
  fun validatePassword_emptyAllowed() {
    assertNull(validatePassword(""))
  }

  @Test
  fun validatePassword_tooShort() {
    assertEquals("Password must be at least 6 characters", validatePassword("12345"))
  }

  @Test
  fun validatePassword_minOk() {
    assertNull(validatePassword("123456"))
  }

  // --- validateDescription ---
  @Test
  fun validateDescription_tooLong() {
    assertEquals("Description too long", validateDescription("x".repeat(201)))
  }

  @Test
  fun validateDescription_boundaryOk() {
    assertNull(validateDescription("x".repeat(100)))
  }

  // --- validateNonEmpty ---
  @Test
  fun validateNonEmpty_blank() {
    assertEquals("Field cannot be empty", validateNonEmpty("Field", ""))
  }

  @Test
  fun validateNonEmpty_ok() {
    assertNull(validateNonEmpty("Field", "value"))
  }

  // --- sanitize ---
  @Test
  fun sanitize_trimsAndSquashesWhitespace() {
    assertEquals("hello world again", sanitize("  hello   world \n again  "))
  }

  @Test
  fun sanitize_allWhitespaceToEmpty() {
    assertEquals("", sanitize(" \t \n "))
  }

  // --- validateDateTriple ---
  @Test
  fun validateDateTriple_empty() {
    val (d, m, y) = validateDateTriple("", "", "")
    assertEquals("Day cannot be empty", d)
    assertEquals("Month cannot be empty", m)
    assertEquals("Year cannot be empty", y)
  }

  @Test
  fun validateDateTriple_nonNumeric() {
    val (d, m, y) = validateDateTriple("aa", "bb", "cc")
    assertEquals("Invalid day", d)
    assertEquals("Invalid month", m)
    assertEquals("Invalid year", y)
  }

  @Test
  fun validateDateTriple_outOfRange() {
    val (d, m, y) = validateDateTriple("32", "13", "1800")
    assertEquals("Invalid day", d)
    assertEquals("Invalid month", m)
    assertEquals("Invalid year", y)
  }

  @Test
  fun validateDateTriple_invalidCalendar_april31() {
    val (d, m, y) = validateDateTriple("31", "4", "2000")
    assertEquals("Invalid day", d)
    assertEquals("Invalid month", m)
    assertEquals("Invalid year", y)
  }

  @Test
  fun validateDateTriple_nonLeapFeb29() {
    val (d, m, y) = validateDateTriple("29", "2", "2001")
    assertEquals("Invalid day", d)
    assertEquals("Invalid month", m)
    assertEquals("Invalid year", y)
  }

  @Test
  fun validateDateTriple_validLeapDay() {
    val (d, m, y) = validateDateTriple("29", "2", "2000")
    assertNull(d)
    assertNull(m)
    assertNull(y)
  }

  @Test
  fun validateDateTriple_under13() {
    val dob = LocalDate.now().minusYears(12)
    val (d, m, y) =
        validateDateTriple(
            dob.dayOfMonth.toString(), dob.monthValue.toString(), dob.year.toString())
    assertNull(d)
    assertNull(m)
    assertEquals("Must be at least 13 years old", y)
  }

  @Test
  fun validateDateTriple_exactly13_ok() {
    val dob = LocalDate.now().minusYears(13)
    val (d, m, y) =
        validateDateTriple(
            dob.dayOfMonth.toString(), dob.monthValue.toString(), dob.year.toString())
    assertNull(d)
    assertNull(m)
    assertNull(y)
  }

  // --- validateAll ---
  @Test
  fun validateAll_allValid() {
    val dob = LocalDate.now().minusYears(20)
    val e =
        validateAll(
            email = "a@epfl.ch",
            password = "secret",
            firstName = "Alice",
            lastName = "Smith",
            description = "Hi!",
            day = dob.dayOfMonth.toString(),
            month = dob.monthValue.toString(),
            year = dob.year.toString())
    assertNull(e.email)
    assertNull(e.password)
    assertNull(e.firstName)
    assertNull(e.lastName)
    assertNull(e.description)
    assertNull(e.day)
    assertNull(e.month)
    assertNull(e.year)
  }

  @Test
  fun validateAll_collectsErrors() {
    val e =
        validateAll(
            email = "not-an-email",
            password = "123",
            firstName = "",
            lastName = "Doe3",
            description = "x".repeat(201),
            day = "31",
            month = "4",
            year = "2005")
    assertEquals("Invalid email format", e.email)
    assertEquals("Password must be at least 6 characters", e.password)
    assertEquals("First name cannot be empty", e.firstName)
    assertEquals("Invalid Last name format", e.lastName)
    assertEquals("Description too long", e.description)
    assertEquals("Invalid day", e.day)
    assertEquals("Invalid month", e.month)
    assertEquals("Invalid year", e.year)
  }
}
