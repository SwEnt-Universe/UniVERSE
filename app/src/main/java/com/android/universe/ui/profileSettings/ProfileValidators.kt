package com.android.universe.ui.profileSettings

import java.time.DateTimeException
import java.time.LocalDate
import java.time.Period

/** Holds validation error messages for profile form fields. */
data class FormErrors(
    val email: String? = null,
    val password: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val description: String? = null,
    val day: String? = null,
    val month: String? = null,
    val year: String? = null,
)

private val nameRegex = "^[\\p{L}\\p{M}' -]*$".toRegex()

/**
 * Validates a user's first or last name.
 *
 * @param label The field name to display in error messages (e.g. "First name").
 * @param s The input string to validate.
 * @param maxLength The maximum allowed length (default = 25).
 * @return A human-readable error message, or null if valid.
 */
fun validateName(label: String, s: String, maxLength: Int = 25): String? =
    when {
      s.isBlank() -> "$label cannot be empty"
      !nameRegex.matches(s) -> "Invalid $label format"
      s.length > maxLength -> "$label too long"
      else -> null
    }

/**
 * Validates email address format.
 *
 * @param s The email string.
 * @return Error message or null if valid.
 */
fun validateEmail(s: String) =
    when {
      s.isBlank() -> "Email cannot be empty"
      !s.contains('@') -> "Invalid email format"
      else -> null
    }

/**
 * Validates password strength.
 *
 * @param s Password input string.
 * @return Error message if too short, or null if valid.
 */
fun validatePassword(s: String) =
    when {
      s.isNotEmpty() && s.length < 6 -> "Password must be at least 6 characters"
      else -> null
    }

/**
 * Validates profile description length.
 *
 * @param s The user description.
 * @param maxLength Maximum character limit (default = 200).
 * @return Error message or null.
 */
fun validateDescription(s: String, maxLength: Int = 200) =
    if (s.length > maxLength) "Description too long" else null

/**
 * Ensures that a string field is not empty.
 *
 * @param label Field name for display.
 * @param s Field value.
 */
fun validateNonEmpty(label: String, s: String) = if (s.isBlank()) "$label cannot be empty" else null

/**
 * Validates a date triple (day, month, year) for logical consistency and age constraint.
 * - Checks numeric format and valid ranges.
 * - Verifies that the date exists.
 * - Enforces minimum age of 13 years.
 *
 * @return Triple of day, month, and year error messages (null for valid).
 */
fun validateDateTriple(
    day: String,
    month: String,
    year: String
): Triple<String?, String?, String?> {
  val dayErr =
      when {
        day.isBlank() -> "Day cannot be empty"
        day.toIntOrNull() == null || day.toInt() !in 1..31 -> "Invalid day"
        else -> null
      }
  val monthErr =
      when {
        month.isBlank() -> "Month cannot be empty"
        month.toIntOrNull() == null || month.toInt() !in 1..12 -> "Invalid month"
        else -> null
      }
  val yearErr =
      when {
        year.isBlank() -> "Year cannot be empty"
        year.toIntOrNull() == null || year.toInt() !in 1900..LocalDate.now().year -> "Invalid year"
        else -> null
      }
  if (dayErr != null || monthErr != null || yearErr != null) {
    return Triple(dayErr, monthErr, yearErr)
  }
  try {
    val dob = LocalDate.of(year.toInt(), month.toInt(), day.toInt())
    if (Period.between(dob, LocalDate.now()).years < 13) {
      return Triple(null, null, "Must be at least 13 years old")
    }
  } catch (_: DateTimeException) {
    return Triple("Invalid day", "Invalid month", "Invalid year")
  }
  return Triple(null, null, null)
}

/** Normalizes whitespace and trims leading/trailing spaces. */
fun sanitize(s: String): String = s.replace(Regex("\\s+"), " ").trim()

/** Runs validation on all form fields and collects errors. */
fun validateAll(
    email: String,
    password: String,
    firstName: String,
    lastName: String,
    description: String,
    day: String,
    month: String,
    year: String
): FormErrors {
  val (dErr, mErr, yErr) = validateDateTriple(day, month, year)
  return FormErrors(
      email = validateEmail(email),
      password = validatePassword(password),
      firstName = validateName("First name", firstName),
      lastName = validateName("Last name", lastName),
      description = validateDescription(description),
      day = dErr,
      month = mErr,
      year = yErr)
}
