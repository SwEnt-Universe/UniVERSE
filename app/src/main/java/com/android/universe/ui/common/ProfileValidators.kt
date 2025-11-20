package com.android.universe.ui.common

import java.time.DateTimeException
import java.time.LocalDate
import java.time.Period

/** Defines the input length and value constraints for user profile fields. */
object InputLimits {
  /** Maximum lengths for a username. */
  const val USERNAME = 25
  /** Maximum length for a first name. */
  const val FIRST_NAME = 25
  /** Maximum length for a last name. */
  const val LAST_NAME = 25
  /** Maximum length for a user's description. */
  const val DESCRIPTION = 100
  /** Maximum number of characters for the day part of a date. */
  const val DAY = 2
  /** Maximum number of characters for the month part of a date. */
  const val MONTH = 2
  /** Maximum number of characters for the year part of a date. */
  const val YEAR = 4
  /** Minimum required age for a user. */
  const val MIN_AGE = 13
  /** The earliest allowed birth year */
  const val MIN_BIRTH_YEAR = 1900
  /** Maximum length for an email address, as per standard specifications. */
  const val EMAIL_MAX_LENGTH = 254
  /** Minimum required length for a password. */
  const val PASSWORD_MIN_LENGTH = 6
}

/** Provides standardized error messages for input validation failures. */
object ErrorMessages {
  // Email
  /** Error message for an empty email field. */
  const val EMAIL_EMPTY = "Email cannot be empty"
  /** Error message for an email that exceeds the maximum length. */
  const val EMAIL_TOO_LONG = "Email cannot be longer than %d characters"
  /** Error message for an email that is not a valid @epfl.ch address. */
  const val EMAIL_NOT_EPFL = "Must be a valid @epfl.ch email address"

  // Username
  /** Error message for an empty username field. */
  const val USERNAME_EMPTY = "Username cannot be empty"
  /** Error message for a username that exceeds the maximum length. */
  const val USERNAME_TOO_LONG = "Username cannot be longer than %d characters"
  /** Error message for a username containing invalid characters. */
  const val USERNAME_INVALID_FORMAT = "Only letters, numbers, . _ - are allowed"
  /** Error message for a username that is already in use. */
  const val USERNAME_TAKEN = "This username is already taken"

  // First Name
  /** Error message for an empty first name field. */
  const val FIRSTNAME_EMPTY = "First name cannot be empty"
  /** Error message for a first name that exceeds the maximum length. */
  const val FIRSTNAME_TOO_LONG = "First name cannot be longer than %d characters"
  /** Error message for a first name containing invalid characters. */
  const val FIRSTNAME_INVALID_FORMAT = "Invalid characters in first name"

  // Last Name
  /** Error message for an empty last name field. */
  const val LASTNAME_EMPTY = "Last name cannot be empty"
  /** Error message for a last name that exceeds the maximum length. */
  const val LASTNAME_TOO_LONG = "Last name cannot be longer than %d characters"
  /** Error message for a last name containing invalid characters. */
  const val LASTNAME_INVALID_FORMAT = "Invalid characters in last name"

  // Password
  /** Error message for a password that is too short. */
  const val PASSWORD_TOO_SHORT = "Password must be at least %d characters"

  // Description
  /** Error message for a description that exceeds the maximum length. */
  const val DESCRIPTION_TOO_LONG = "Description cannot be longer than %d characters"

  // Country
  /** Error message for an empty country selection. */
  const val COUNTRY_EMPTY = "Country cannot be empty"
  /** Error message for an invalid country selection. */
  const val COUNTRY_INVALID = "Invalid country selected"

  // Day
  /** Error message for an empty day field. */
  const val DAY_EMPTY = "Day cannot be empty"
  /** Error message when the day is not a valid number. */
  const val DAY_INVALID_NUMBER = "Day must be a number"
  /** Error message for a day that is outside the valid range (1-31). */
  const val DAY_OUT_OF_RANGE = "Day must be between 1 and 31"

  // Month
  /** Error message for an empty month field. */
  const val MONTH_EMPTY = "Month cannot be empty"
  /** Error message when the month is not a valid number. */
  const val MONTH_INVALID_NUMBER = "Month must be a number"
  /** Error message for a month that is outside the valid range (1-12). */
  const val MONTH_OUT_OF_RANGE = "Month must be between 1 and 12"

  // Year
  /** Error message for an empty year field. */
  const val YEAR_EMPTY = "Year cannot be empty"
  /** Error message when the year is not a valid number. */
  const val YEAR_INVALID_NUMBER = "Year must be a number"
  /** Error message for a year that is outside the allowed range. */
  const val YEAR_OUT_OF_RANGE = "Year must be between %d and %d"

  // BirthDate
  /** Error message for a birth date that is in the future. */
  const val DATE_IN_FUTURE = "Date of birth cannot be in the future"
  /** Error message for a user who is younger than the minimum required age. */
  const val DATE_TOO_YOUNG = "You must be at least %d years old"
  /** Error message for a date that is logically impossible (e.g., April 31st). */
  const val DATE_INVALID_LOGICAL = "This date does not exist"
}

/** Regex for validating EPFL email addresses. */
private val emailRegex = "^[a-zA-Z0-9._%+-]+@epfl\\.ch$".toRegex()
/** Regex for validating usernames (letters, numbers, '.', '_', '-'). */
private val usernameRegex = "^[A-Za-z0-9._-]+$".toRegex()
/** Regex for validating names (letters, marks, apostrophes, hyphens, spaces). */
private val nameRegex = "^[\\p{L}\\p{M}' -]*$".toRegex()

/**
 * Validates an email address. It must not be blank, must not exceed the maximum length, and must be
 * a valid @epfl.ch address.
 *
 * @param email The email address to validate.
 * @return [ValidationState.Valid] if the email is valid, otherwise [ValidationState.Invalid].
 */
fun validateEmail(email: String): ValidationState {
  return when {
    email.isBlank() -> ValidationState.Invalid(ErrorMessages.EMAIL_EMPTY)
    email.length > InputLimits.EMAIL_MAX_LENGTH ->
        ValidationState.Invalid(ErrorMessages.EMAIL_TOO_LONG.format(InputLimits.EMAIL_MAX_LENGTH))
    !emailRegex.matches(email) -> ValidationState.Invalid(ErrorMessages.EMAIL_NOT_EPFL)
    else -> ValidationState.Valid
  }
}

/**
 * Validates a password. It must meet the minimum length requirement.
 *
 * @param password The password to validate.
 * @return [ValidationState.Valid] if the password is valid, otherwise [ValidationState.Invalid].
 */
fun validatePassword(password: String): ValidationState {
  return when {
    password.length < InputLimits.PASSWORD_MIN_LENGTH ->
        ValidationState.Invalid(
            ErrorMessages.PASSWORD_TOO_SHORT.format(InputLimits.PASSWORD_MIN_LENGTH))
    else -> ValidationState.Valid
  }
}

/**
 * Validates a username. It must not be blank, must not exceed the maximum length, and must only
 * contain allowed characters.
 *
 * @param username The username to validate.
 * @return [ValidationState.Valid] if the username is valid, otherwise [ValidationState.Invalid].
 */
fun validateUsername(username: String): ValidationState {
  return when {
    username.isBlank() -> ValidationState.Invalid(ErrorMessages.USERNAME_EMPTY)
    username.length > InputLimits.USERNAME ->
        ValidationState.Invalid(ErrorMessages.USERNAME_TOO_LONG.format(InputLimits.USERNAME))
    !usernameRegex.matches(username) ->
        ValidationState.Invalid(ErrorMessages.USERNAME_INVALID_FORMAT)
    else -> ValidationState.Valid
  }
}

/**
 * Validates a first name. It must not be blank, must not exceed the maximum length, and must only
 * contain valid characters.
 *
 * @param firstName The first name to validate.
 * @return [ValidationState.Valid] if the first name is valid, otherwise [ValidationState.Invalid].
 */
fun validateFirstName(firstName: String): ValidationState {
  return when {
    firstName.isBlank() -> ValidationState.Invalid(ErrorMessages.FIRSTNAME_EMPTY)
    firstName.length > InputLimits.FIRST_NAME ->
        ValidationState.Invalid(ErrorMessages.FIRSTNAME_TOO_LONG.format(InputLimits.FIRST_NAME))
    !nameRegex.matches(firstName) -> ValidationState.Invalid(ErrorMessages.FIRSTNAME_INVALID_FORMAT)
    else -> ValidationState.Valid
  }
}

/**
 * Validates a last name. It must not be blank, must not exceed the maximum length, and must only
 * contain valid characters.
 *
 * @param lastName The last name to validate.
 * @return [ValidationState.Valid] if the last name is valid, otherwise [ValidationState.Invalid].
 */
fun validateLastName(lastName: String): ValidationState {
  return when {
    lastName.isBlank() -> ValidationState.Invalid(ErrorMessages.LASTNAME_EMPTY)
    lastName.length > InputLimits.LAST_NAME ->
        ValidationState.Invalid(ErrorMessages.LASTNAME_TOO_LONG.format(InputLimits.LAST_NAME))
    !nameRegex.matches(lastName) -> ValidationState.Invalid(ErrorMessages.LASTNAME_INVALID_FORMAT)
    else -> ValidationState.Valid
  }
}

/**
 * Validates a user description. It must not exceed the maximum length.
 *
 * @param description The description to validate.
 * @return [ValidationState.Valid] if the description is valid, otherwise [ValidationState.Invalid].
 */
fun validateDescription(description: String): ValidationState {
  return if (description.length > InputLimits.DESCRIPTION) {
    ValidationState.Invalid(ErrorMessages.DESCRIPTION_TOO_LONG.format(InputLimits.DESCRIPTION))
  } else {
    ValidationState.Valid
  }
}

/**
 * Validates a country selection. The country must not be blank and must exist as a key in the
 * provided map.
 *
 * @param country The selected country name.
 * @param countryMap A map of valid country names to their codes.
 * @return [ValidationState.Valid] if the country is valid, otherwise [ValidationState.Invalid].
 */
fun validateCountry(country: String, countryMap: Map<String, String>): ValidationState {
  return when {
    country.isBlank() -> ValidationState.Invalid(ErrorMessages.COUNTRY_EMPTY)
    !countryMap.containsKey(country) -> ValidationState.Invalid(ErrorMessages.COUNTRY_INVALID)
    else -> ValidationState.Valid
  }
}

/**
 * Validates the day part of a date. It must not be blank, must be a number, and must be between 1
 * and 31.
 *
 * @param day The day as a string.
 * @return [ValidationState.Valid] if the day is valid, otherwise [ValidationState.Invalid].
 */
fun validateDay(day: String): ValidationState {
  val dayInt = day.toIntOrNull()
  return when {
    day.isBlank() -> ValidationState.Invalid(ErrorMessages.DAY_EMPTY)
    dayInt == null -> ValidationState.Invalid(ErrorMessages.DAY_INVALID_NUMBER)
    dayInt !in 1..31 -> ValidationState.Invalid(ErrorMessages.DAY_OUT_OF_RANGE)
    else -> ValidationState.Valid
  }
}

/**
 * Validates the month part of a date. It must not be blank, must be a number, and must be between 1
 * and 12.
 *
 * @param month The month as a string.
 * @return [ValidationState.Valid] if the month is valid, otherwise [ValidationState.Invalid].
 */
fun validateMonth(month: String): ValidationState {
  val monthInt = month.toIntOrNull()
  return when {
    month.isBlank() -> ValidationState.Invalid(ErrorMessages.MONTH_EMPTY)
    monthInt == null -> ValidationState.Invalid(ErrorMessages.MONTH_INVALID_NUMBER)
    monthInt !in 1..12 -> ValidationState.Invalid(ErrorMessages.MONTH_OUT_OF_RANGE)
    else -> ValidationState.Valid
  }
}

/**
 * Validates the year part of a date. It must not be blank, must be a number, and must be within a
 * valid range from [InputLimits.MIN_BIRTH_YEAR] to the current year.
 *
 * @param year The year as a string.
 * @return [ValidationState.Valid] if the year is valid, otherwise [ValidationState.Invalid].
 */
fun validateYear(year: String): ValidationState {
  val yearInt = year.toIntOrNull()
  val currentYear = LocalDate.now().year
  return when {
    year.isBlank() -> ValidationState.Invalid(ErrorMessages.YEAR_EMPTY)
    yearInt == null -> ValidationState.Invalid(ErrorMessages.YEAR_INVALID_NUMBER)
    yearInt !in InputLimits.MIN_BIRTH_YEAR..currentYear ->
        ValidationState.Invalid(
            ErrorMessages.YEAR_OUT_OF_RANGE.format(InputLimits.MIN_BIRTH_YEAR, currentYear))
    else -> ValidationState.Valid
  }
}

/**
 * Validates a complete date of birth. Checks if the date is logically correct (e.g., not February
 * 30th), not in the future, and if the user meets the minimum age requirement.
 *
 * @param day The day of the month.
 * @param month The month of the year.
 * @param year The year.
 * @return [ValidationState.Valid] if the date is valid, otherwise [ValidationState.Invalid].
 */
fun validateBirthDate(day: Int, month: Int, year: Int): ValidationState {
  try {
    val dob = LocalDate.of(year, month, day)
    val today = LocalDate.now()

    if (dob.isAfter(today)) {
      return ValidationState.Invalid(ErrorMessages.DATE_IN_FUTURE)
    }

    val age = Period.between(dob, today).years
    if (age < InputLimits.MIN_AGE) {
      return ValidationState.Invalid(ErrorMessages.DATE_TOO_YOUNG.format(InputLimits.MIN_AGE))
    }

    return ValidationState.Valid
  } catch (_: DateTimeException) {
    return ValidationState.Invalid(ErrorMessages.DATE_INVALID_LOGICAL)
  }
}

/**
 * Sanitizes a string by replacing multiple whitespace characters with a single space and trimming
 * leading/trailing whitespace.
 *
 * @param s The string to sanitize.
 * @return The sanitized string.
 */
fun sanitize(s: String): String = s.replace(Regex("\\s+"), " ").trim()

/**
 * Returns a copy of this string with the first letter of each word capitalized and all other
 * letters converted to lowercase.
 *
 * Words are defined as sequences of characters separated by a single space (`" "`). Consecutive
 * spaces are treated as delimiters and will be collapsed in the result.
 *
 * "hello world".toTitleCase() // → "Hello World"
 *
 * This function is primarily intended for normalizing user-facing names and labels.
 */
fun String.toTitleCase(): String {
  return this.split(" ").joinToString(" ") { word ->
    when {
      word.isEmpty() -> word
      word.length == 1 -> word.uppercase()
      else -> word[0].uppercase() + word.substring(1).lowercase()
    }
  }
}

/**
 * Sanitizes a string by replacing multiple whitespace characters with a single space and trimming
 * leading whitespace.
 *
 * @param s The string to sanitize.
 * @return The sanitized string.
 */
fun sanitizeLead(s: String): String = s.replace(Regex("\\s+"), " ").trimStart()
