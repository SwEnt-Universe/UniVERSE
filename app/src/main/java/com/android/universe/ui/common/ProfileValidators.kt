package com.android.universe.ui.common

import java.time.DateTimeException
import java.time.LocalDate
import java.time.Period

object InputLimits {
  const val USERNAME = 25
  const val FIRST_NAME = 25
  const val LAST_NAME = 25
  const val DESCRIPTION = 100
  const val DAY = 2
  const val MONTH = 2
  const val YEAR = 4
  const val MIN_AGE = 13
  const val MIN_BIRTH_YEAR = 1900
  const val EMAIL_MAX_LENGTH = 254
  const val PASSWORD_MIN_LENGTH = 6
}

object ErrorMessages {
  // Email
  const val EMAIL_EMPTY = "Email cannot be empty"
  const val EMAIL_TOO_LONG = "Email cannot be longer than %d characters"
  const val EMAIL_NOT_EPFL = "Must be a valid @epfl.ch email address"

  // Username
  const val USERNAME_EMPTY = "Username cannot be empty"
  const val USERNAME_TOO_LONG = "Username cannot be longer than %d characters"
  const val USERNAME_INVALID_FORMAT = "Only letters, numbers, . _ - are allowed"

  // First Name
  const val FIRSTNAME_EMPTY = "First name cannot be empty"
  const val FIRSTNAME_TOO_LONG = "First name cannot be longer than %d characters"
  const val FIRSTNAME_INVALID_FORMAT = "Invalid characters in first name"

  // Last Name
  const val LASTNAME_EMPTY = "Last name cannot be empty"
  const val LASTNAME_TOO_LONG = "Last name cannot be longer than %d characters"
  const val LASTNAME_INVALID_FORMAT = "Invalid characters in last name"

  // Password
  const val PASSWORD_TOO_SHORT = "Password must be at least %d characters"

  // Description
  const val DESCRIPTION_TOO_LONG = "Description cannot be longer than %d characters"

  // Country
  const val COUNTRY_EMPTY = "Country cannot be empty"
  const val COUNTRY_INVALID = "Invalid country selected"

  // Day
  const val DAY_EMPTY = "Day cannot be empty"
  const val DAY_INVALID_NUMBER = "Day must be a number"
  const val DAY_OUT_OF_RANGE = "Day must be between 1 and 31"

  // Month
  const val MONTH_EMPTY = "Month cannot be empty"
  const val MONTH_INVALID_NUMBER = "Month must be a number"
  const val MONTH_OUT_OF_RANGE = "Month must be between 1 and 12"

  // Year
  const val YEAR_EMPTY = "Year cannot be empty"
  const val YEAR_INVALID_NUMBER = "Year must be a number"
  const val YEAR_OUT_OF_RANGE = "Year must be between %d and %d"

  // BirthDate
  const val DATE_IN_FUTURE = "Date of birth cannot be in the future"
  const val DATE_TOO_YOUNG = "You must be at least %d years old"
  const val DATE_INVALID_LOGICAL = "This date does not exist"
}

sealed class ValidationResult {
  data object Valid : ValidationResult()

  data class Invalid(val errorMessage: String) : ValidationResult()
}

private val emailRegex = "^[a-zA-Z0-9._%+-]+@epfl\\.ch$".toRegex()
private val usernameRegex = "^[A-Za-z0-9._-]+$".toRegex()
private val nameRegex = "^[\\p{L}\\p{M}' -]*$".toRegex()

fun validateEmail(email: String): ValidationResult {
  return when {
    email.isBlank() -> ValidationResult.Invalid(ErrorMessages.EMAIL_EMPTY)
    email.length > InputLimits.EMAIL_MAX_LENGTH ->
        ValidationResult.Invalid(ErrorMessages.EMAIL_TOO_LONG.format(InputLimits.EMAIL_MAX_LENGTH))
    !emailRegex.matches(email) -> ValidationResult.Invalid(ErrorMessages.EMAIL_NOT_EPFL)
    else -> ValidationResult.Valid
  }
}

fun validatePassword(password: String): ValidationResult {
  return when {
    password.length < InputLimits.PASSWORD_MIN_LENGTH ->
        ValidationResult.Invalid(
            ErrorMessages.PASSWORD_TOO_SHORT.format(InputLimits.PASSWORD_MIN_LENGTH))
    else -> ValidationResult.Valid
  }
}

fun validateUsername(username: String): ValidationResult {
  return when {
    username.isBlank() -> ValidationResult.Invalid(ErrorMessages.USERNAME_EMPTY)
    username.length > InputLimits.USERNAME ->
        ValidationResult.Invalid(ErrorMessages.USERNAME_TOO_LONG.format(InputLimits.USERNAME))
    !usernameRegex.matches(username) ->
        ValidationResult.Invalid(ErrorMessages.USERNAME_INVALID_FORMAT)
    else -> ValidationResult.Valid
  }
}

fun validateFirstName(firstName: String): ValidationResult {
  return when {
    firstName.isBlank() -> ValidationResult.Invalid(ErrorMessages.FIRSTNAME_EMPTY)
    firstName.length > InputLimits.FIRST_NAME ->
        ValidationResult.Invalid(ErrorMessages.FIRSTNAME_TOO_LONG.format(InputLimits.FIRST_NAME))
    !nameRegex.matches(firstName) ->
        ValidationResult.Invalid(ErrorMessages.FIRSTNAME_INVALID_FORMAT)
    else -> ValidationResult.Valid
  }
}

fun validateLastName(lastName: String): ValidationResult {
  return when {
    lastName.isBlank() -> ValidationResult.Invalid(ErrorMessages.LASTNAME_EMPTY)
    lastName.length > InputLimits.LAST_NAME ->
        ValidationResult.Invalid(ErrorMessages.LASTNAME_TOO_LONG.format(InputLimits.LAST_NAME))
    !nameRegex.matches(lastName) -> ValidationResult.Invalid(ErrorMessages.LASTNAME_INVALID_FORMAT)
    else -> ValidationResult.Valid
  }
}

fun validateDescription(description: String): ValidationResult {
  return if (description.length > InputLimits.DESCRIPTION) {
    ValidationResult.Invalid(ErrorMessages.DESCRIPTION_TOO_LONG.format(InputLimits.DESCRIPTION))
  } else {
    ValidationResult.Valid
  }
}

fun validateCountry(country: String, countryMap: Map<String, String>): ValidationResult {
  return when {
    country.isBlank() -> ValidationResult.Invalid(ErrorMessages.COUNTRY_EMPTY)
    !countryMap.containsKey(country) -> ValidationResult.Invalid(ErrorMessages.COUNTRY_INVALID)
    else -> ValidationResult.Valid
  }
}

fun validateDay(day: String): ValidationResult {
  val dayInt = day.toIntOrNull()
  return when {
    day.isBlank() -> ValidationResult.Invalid(ErrorMessages.DAY_EMPTY)
    dayInt == null -> ValidationResult.Invalid(ErrorMessages.DAY_INVALID_NUMBER)
    dayInt !in 1..31 -> ValidationResult.Invalid(ErrorMessages.DAY_OUT_OF_RANGE)
    else -> ValidationResult.Valid
  }
}

fun validateMonth(month: String): ValidationResult {
  val monthInt = month.toIntOrNull()
  return when {
    month.isBlank() -> ValidationResult.Invalid(ErrorMessages.MONTH_EMPTY)
    monthInt == null -> ValidationResult.Invalid(ErrorMessages.MONTH_INVALID_NUMBER)
    monthInt !in 1..12 -> ValidationResult.Invalid(ErrorMessages.MONTH_OUT_OF_RANGE)
    else -> ValidationResult.Valid
  }
}

fun validateYear(year: String): ValidationResult {
  val yearInt = year.toIntOrNull()
  val currentYear = LocalDate.now().year
  return when {
    year.isBlank() -> ValidationResult.Invalid(ErrorMessages.YEAR_EMPTY)
    yearInt == null -> ValidationResult.Invalid(ErrorMessages.YEAR_INVALID_NUMBER)
    yearInt !in InputLimits.MIN_BIRTH_YEAR..currentYear ->
        ValidationResult.Invalid(
            ErrorMessages.YEAR_OUT_OF_RANGE.format(InputLimits.MIN_BIRTH_YEAR, currentYear))
    else -> ValidationResult.Valid
  }
}

fun validateBirthDate(day: Int, month: Int, year: Int): ValidationResult {
  try {
    val dob = LocalDate.of(year, month, day)
    val today = LocalDate.now()

    if (dob.isAfter(today)) {
      return ValidationResult.Invalid(ErrorMessages.DATE_IN_FUTURE)
    }

    val age = Period.between(dob, today).years
    if (age < InputLimits.MIN_AGE) {
      return ValidationResult.Invalid(ErrorMessages.DATE_TOO_YOUNG.format(InputLimits.MIN_AGE))
    }

    return ValidationResult.Valid
  } catch (_: DateTimeException) {
    return ValidationResult.Invalid(ErrorMessages.DATE_INVALID_LOGICAL)
  }
}
