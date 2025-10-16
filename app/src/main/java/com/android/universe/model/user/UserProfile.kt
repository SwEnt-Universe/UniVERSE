package com.android.universe.model.user

import com.android.universe.model.Tag
import java.time.LocalDate

/**
 * Represents a user profile in the app.
 *
 * @property uid The unique, system-generated identifier for the user.
 * @property username The user's unique identifier.
 * @property firstName The user's first name.
 * @property lastName The user's last name.
 * @property country The user's country, represented as an ISO 3166-1 alpha-2 code.
 * @property description An optional short bio or "about me" text.
 * @property dateOfBirth The user's date of birth.
 * @property tags A set of [Tag]s representing the user's interests.
 */
data class UserProfile(
    val uid: String,
    val username: String,
    val firstName: String,
    val lastName: String,
    val country: String,
    val description: String? = null,
    val dateOfBirth: LocalDate,
    val tags: Set<Tag>
)
