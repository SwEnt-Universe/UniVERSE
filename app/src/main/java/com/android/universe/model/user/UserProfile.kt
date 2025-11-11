package com.android.universe.model.user

import com.android.universe.model.tag.Tag
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
 * @property profileImageUri the profile picture of the user.
 */
data class UserProfile(
    val uid: String,
    val username: String,
    val firstName: String,
    val lastName: String,
    val country: String,
    val description: String? = null,
    val dateOfBirth: LocalDate,
    val tags: Set<Tag>,
    val profileImageUri: ByteArray? = null
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as UserProfile

    if (uid != other.uid) return false
    if (username != other.username) return false
    if (firstName != other.firstName) return false
    if (lastName != other.lastName) return false
    if (country != other.country) return false
    if (description != other.description) return false
    if (dateOfBirth != other.dateOfBirth) return false
    if (tags != other.tags) return false
    if (!profileImageUri.contentEquals(other.profileImageUri)) return false

    return true
  }

  override fun hashCode(): Int {
    var result = uid.hashCode()
    result = 31 * result + username.hashCode()
    result = 31 * result + firstName.hashCode()
    result = 31 * result + lastName.hashCode()
    result = 31 * result + country.hashCode()
    result = 31 * result + (description?.hashCode() ?: 0)
    result = 31 * result + dateOfBirth.hashCode()
    result = 31 * result + tags.hashCode()
    result = 31 * result + (profileImageUri?.contentHashCode() ?: 0)
    return result
  }
}
