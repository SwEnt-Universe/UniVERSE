package com.android.universe.model.user

import com.android.universe.model.Tag
import java.time.LocalDate

/**
 * Represents a user profile in the app.
 *
 * @property username unique identifier chosen by the user.
 * @property firstName given name of the user.
 * @property lastName family name of the user.
 * @property country country of the user (ISO 3166-1 alpha-2 code)
 * @property description optional short bio or "about me" text.
 * @property dateOfBirth date of birth of the user.
 * @property tags Set of tags representing the user's interests, means of transportation, or Swiss
 *   canton.
 */
data class UserProfile(
    val username: String,
    val firstName: String,
    val lastName: String,
    val country: String,
    val description: String? = null,
    val dateOfBirth: LocalDate,
    val tags: Set<Tag>
)
