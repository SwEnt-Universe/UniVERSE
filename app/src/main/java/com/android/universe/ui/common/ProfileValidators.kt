package com.android.universe.ui.common

import androidx.annotation.StringRes
import com.android.universe.R
import java.time.DateTimeException
import java.time.LocalDate
import java.time.Period

object InputLimits {
    const val USERNAME = 25
    const val FIRST_NAME = 25
    const val LAST_NAME = 25
    const val DESCRIPTION = 100
    const val MIN_AGE = 13
    const val MIN_BIRTH_YEAR = 1900
    const val EMAIL_MAX_LENGTH = 254
}

sealed class ValidationResult {
    data object Valid : ValidationResult()
    data class Invalid(
        @StringRes val errorResId: Int,
        val formatArgs: List<Any> = emptyList()
    ) : ValidationResult()
}

private val emailRegex = "^[a-zA-Z0-9._%+-]+@epfl\\.ch$".toRegex()
private val usernameRegex = "^[A-Za-z0-9._-]+$".toRegex()
private val nameRegex = "^[\\p{L}\\p{M}' -]*$".toRegex()

fun validateEmail(email: String): ValidationResult {
    return when {
        email.isBlank() -> ValidationResult.Invalid(R.string.error_email_empty)
        email.length > InputLimits.EMAIL_MAX_LENGTH -> ValidationResult.Invalid(
            R.string.error_email_too_long,
            listOf(InputLimits.EMAIL_MAX_LENGTH)
        )
        !emailRegex.matches(email) -> ValidationResult.Invalid(R.string.error_email_not_epfl)
        else -> ValidationResult.Valid
    }
}

fun validateUsername(username: String): ValidationResult {
    return when {
        username.isBlank() -> ValidationResult.Invalid(R.string.error_username_empty)
        username.length > InputLimits.USERNAME -> ValidationResult.Invalid(
            R.string.error_username_too_long,
            listOf(InputLimits.USERNAME)
        )
        !usernameRegex.matches(username) -> ValidationResult.Invalid(
            R.string.error_username_invalid_format
        )
        else -> ValidationResult.Valid
    }
}

fun validateFirstName(firstName: String): ValidationResult {
    return when {
        firstName.isBlank() -> ValidationResult.Invalid(R.string.error_firstname_empty)
        firstName.length > InputLimits.FIRST_NAME -> ValidationResult.Invalid(
            R.string.error_firstname_too_long,
            listOf(InputLimits.FIRST_NAME)
        )
        !nameRegex.matches(firstName) -> ValidationResult.Invalid(
            R.string.error_firstname_invalid_format
        )
        else -> ValidationResult.Valid
    }
}

fun validateLastName(lastName: String): ValidationResult {
    return when {
        lastName.isBlank() -> ValidationResult.Invalid(R.string.error_lastname_empty)
        lastName.length > InputLimits.LAST_NAME -> ValidationResult.Invalid(
            R.string.error_lastname_too_long,
            listOf(InputLimits.LAST_NAME)
        )
        !nameRegex.matches(lastName) -> ValidationResult.Invalid(
            R.string.error_lastname_invalid_format
        )
        else -> ValidationResult.Valid
    }
}

fun validateDescription(description: String): ValidationResult {
    return if (description.length > InputLimits.DESCRIPTION) {
        ValidationResult.Invalid(
            R.string.error_description_too_long,
            listOf(InputLimits.DESCRIPTION)
        )
    } else {
        ValidationResult.Valid
    }
}

fun validateCountry(country: String, countryMap: Map<String, String>): ValidationResult {
    return when {
        country.isBlank() -> ValidationResult.Invalid(R.string.error_country_empty)
        !countryMap.containsKey(country) -> ValidationResult.Invalid(R.string.error_country_invalid)
        else -> ValidationResult.Valid
    }
}

fun validateDay(day: String): ValidationResult {
    val dayInt = day.toIntOrNull()
    return when {
        day.isBlank() -> ValidationResult.Invalid(R.string.error_day_empty)
        dayInt == null -> ValidationResult.Invalid(R.string.error_day_invalid_number)
        dayInt !in 1..31 -> ValidationResult.Invalid(R.string.error_day_out_of_range)
        else -> ValidationResult.Valid
    }
}

fun validateMonth(month: String): ValidationResult {
    val monthInt = month.toIntOrNull()
    return when {
        month.isBlank() -> ValidationResult.Invalid(R.string.error_month_empty)
        monthInt == null -> ValidationResult.Invalid(R.string.error_month_invalid_number)
        monthInt !in 1..12 -> ValidationResult.Invalid(R.string.error_month_out_of_range)
        else -> ValidationResult.Valid
    }
}

fun validateYear(year: String): ValidationResult {
    val yearInt = year.toIntOrNull()
    val currentYear = LocalDate.now().year
    return when {
        year.isBlank() -> ValidationResult.Invalid(R.string.error_year_empty)
        yearInt == null -> ValidationResult.Invalid(R.string.error_year_invalid_number)
        yearInt !in InputLimits.MIN_BIRTH_YEAR..currentYear -> ValidationResult.Invalid(
            R.string.error_year_out_of_range,
            listOf(InputLimits.MIN_BIRTH_YEAR, currentYear)
        )
        else -> ValidationResult.Valid
    }
}

fun validateBirthDate(day: Int, month: Int, year: Int): ValidationResult {
    try {
        val dob = LocalDate.of(year, month, day)
        val today = LocalDate.now()

        if (dob.isAfter(today)) {
            return ValidationResult.Invalid(R.string.error_date_in_future)
        }

        val age = Period.between(dob, today).years
        if (age < InputLimits.MIN_AGE) {
            return ValidationResult.Invalid(
                R.string.error_date_too_young,
                listOf(InputLimits.MIN_AGE)
            )
        }

        return ValidationResult.Valid

    } catch (_: DateTimeException) {
        return ValidationResult.Invalid(R.string.error_date_invalid_logical)
    }
}

