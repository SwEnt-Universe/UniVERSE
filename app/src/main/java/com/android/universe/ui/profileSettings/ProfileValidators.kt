package com.android.universe.ui.profileSettings

import java.time.DateTimeException
import java.time.LocalDate
import java.time.Period

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

fun validateName(label: String, s: String, maxLength: Int = 25): String? =
    when {
      s.isBlank() -> "$label cannot be empty"
      !nameRegex.matches(s) -> "Invalid $label format"
      s.length > maxLength -> "$label too long"
      else -> null
    }

fun validateEmail(s: String) =
    when {
      s.isBlank() -> "Email cannot be empty"
      !s.contains('@') -> "Invalid email format"
      else -> null
    }

fun validatePassword(s: String) =
    when {
      s.isNotEmpty() && s.length < 6 -> "Password must be at least 6 characters"
      else -> null
    }

fun validateDescription(s: String, maxLength: Int = 200) =
    if (s.length > maxLength) "Description too long" else null

fun validateNonEmpty(label: String, s: String) = if (s.isBlank()) "$label cannot be empty" else null

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

fun sanitize(s: String): String = s.replace(Regex("\\s+"), " ").trim()

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
