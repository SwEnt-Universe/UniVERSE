package com.android.universe.model.user

import com.android.universe.model.Tag
import java.time.LocalDate
import kotlinx.coroutines.runBlocking

/**
 * Singleton provider for a [UserRepository] instance.
 *
 * This provider supplies a pre-populated [FakeUserRepository] that can be used for UI development
 * and testing purposes. It is intended to be a single shared repository instance across the
 * application.
 */
object UserRepositoryProvider {
  private val _repository: UserRepository = FakeUserRepository()
  private val tag = Tag.METAL
  private val tags = listOf(tag)

  init {
    val sampleUsers =
        listOf(
            UserProfile(
                username = "alice",
                firstName = "Alice",
                lastName = "Smith",
                country = "CH",
                description = "Hi, I'm Alice.",
                dateOfBirth = LocalDate.of(1990, 1, 1),
                tags = tags),
            UserProfile(
                username = "bob",
                firstName = "Bob",
                lastName = "Jones",
                country = "FR",
                description = "Hello, I'm Bob.",
                dateOfBirth = LocalDate.of(2000, 8, 11),
                tags = emptyList()),
            UserProfile(
                username = "john",
                firstName = "John",
                lastName = "Doe",
                country = "US",
                description = "Hello, I'm John from the US.",
                dateOfBirth = LocalDate.of(1800, 10, 30),
                tags = tags),
            UserProfile(
                username = "emily",
                firstName = "Emily",
                lastName = "Clark",
                country = "GB",
                dateOfBirth = LocalDate.of(1995, 3, 21),
                tags = tags),
            UserProfile(
                username = "michael",
                firstName = "Michael",
                lastName = "Brown",
                country = "CA",
                description = "Avid traveler.",
                dateOfBirth = LocalDate.of(1988, 12, 5),
                tags = tags),
            UserProfile(
                username = "sophia",
                firstName = "Sophia",
                lastName = "Lopez",
                country = "ES",
                description = "Food enthusiast.",
                dateOfBirth = LocalDate.of(1992, 7, 14),
                tags = tags),
            UserProfile(
                username = "daniel",
                firstName = "Daniel",
                lastName = "Kim",
                country = "KR",
                description = "Tech geek.",
                dateOfBirth = LocalDate.of(1997, 11, 9),
                tags = tags),
            UserProfile(
                username = "lisa",
                firstName = "Lisa",
                lastName = "Wang",
                country = "CN",
                description = "Nature lover.",
                dateOfBirth = LocalDate.of(2001, 5, 30),
                tags = tags),
            UserProfile(
                username = "paul",
                firstName = "Paul",
                lastName = "Müller",
                country = "DE",
                dateOfBirth = LocalDate.of(1985, 9, 2),
                tags = tags),
            UserProfile(
                username = "emma",
                firstName = "Emma",
                lastName = "Rossi",
                country = "IT",
                description = "Coffee aficionado.",
                dateOfBirth = LocalDate.of(1993, 6, 18),
                tags = tags),
            UserProfile(
                username = "lucas",
                firstName = "Lucas",
                lastName = "Silva",
                country = "BR",
                description = "Soccer fan.",
                dateOfBirth = LocalDate.of(1998, 4, 27),
                tags = tags),
            UserProfile(
                username = "mia",
                firstName = "Mia",
                lastName = "Nguyen",
                country = "VN",
                description = "Photographer.",
                dateOfBirth = LocalDate.of(1996, 2, 12),
                tags = tags),
            UserProfile(
                username = "noah",
                firstName = "Noah",
                lastName = "Patel",
                country = "IN",
                description = "Reader and writer.",
                dateOfBirth = LocalDate.of(1994, 10, 7),
                tags = tags),
            UserProfile(
                username = "chloe",
                firstName = "Chloe",
                lastName = "Dubois",
                country = "FR",
                dateOfBirth = LocalDate.of(2000, 1, 29),
                tags = tags),
            UserProfile(
                username = "ethan",
                firstName = "Ethan",
                lastName = "Smith",
                country = "AU",
                description = "Surfing enthusiast.",
                dateOfBirth = LocalDate.of(1991, 8, 16),
                tags = tags),
            UserProfile(
                username = "olivia",
                firstName = "Olivia",
                lastName = "Johnson",
                country = "US",
                description = "Loves painting.",
                dateOfBirth = LocalDate.of(1999, 12, 22),
                tags = tags))

    runBlocking { sampleUsers.forEach { _repository.addUser(it) } }
  }

  /** Public repository instance (read-only) */
  var repository: UserRepository = _repository
}
