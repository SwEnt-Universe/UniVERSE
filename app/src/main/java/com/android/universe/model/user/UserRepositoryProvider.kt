package com.android.universe.model.user

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

  init {
    val sampleUsers =
        listOf(
            UserProfile(
                username = "alice",
                firstName = "Alice",
                lastName = "Smith",
                country = "CH",
                description = "Hi, I'm Alice.",
                dateOfBirth = LocalDate.of(1990, 1, 1)),
            UserProfile(
                username = "bob",
                firstName = "Bob",
                lastName = "Jones",
                country = "FR",
                description = "Hello, I'm Bob.",
                dateOfBirth = LocalDate.of(2000, 8, 11)),
            UserProfile(
                username = "john",
                firstName = "John",
                lastName = "Doe",
                country = "US",
                description = "Hello, I'm John from the US.",
                dateOfBirth = LocalDate.of(1800, 10, 30)),
            UserProfile(
                username = "emily",
                firstName = "Emily",
                lastName = "Clark",
                country = "GB",
                description = "Music lover.",
                dateOfBirth = LocalDate.of(1995, 3, 21)),
            UserProfile(
                username = "michael",
                firstName = "Michael",
                lastName = "Brown",
                country = "CA",
                description = "Avid traveler.",
                dateOfBirth = LocalDate.of(1988, 12, 5)),
            UserProfile(
                username = "sophia",
                firstName = "Sophia",
                lastName = "Lopez",
                country = "ES",
                description = "Food enthusiast.",
                dateOfBirth = LocalDate.of(1992, 7, 14)),
            UserProfile(
                username = "daniel",
                firstName = "Daniel",
                lastName = "Kim",
                country = "KR",
                description = "Tech geek.",
                dateOfBirth = LocalDate.of(1997, 11, 9)),
            UserProfile(
                username = "lisa",
                firstName = "Lisa",
                lastName = "Wang",
                country = "CN",
                description = "Nature lover.",
                dateOfBirth = LocalDate.of(2001, 5, 30)),
            UserProfile(
                username = "paul",
                firstName = "Paul",
                lastName = "Müller",
                country = "DE",
                description = "Cycling enthusiast.",
                dateOfBirth = LocalDate.of(1985, 9, 2)),
            UserProfile(
                username = "emma",
                firstName = "Emma",
                lastName = "Rossi",
                country = "IT",
                description = "Coffee aficionado.",
                dateOfBirth = LocalDate.of(1993, 6, 18)),
            UserProfile(
                username = "lucas",
                firstName = "Lucas",
                lastName = "Silva",
                country = "BR",
                description = "Soccer fan.",
                dateOfBirth = LocalDate.of(1998, 4, 27)),
            UserProfile(
                username = "mia",
                firstName = "Mia",
                lastName = "Nguyen",
                country = "VN",
                description = "Photographer.",
                dateOfBirth = LocalDate.of(1996, 2, 12)),
            UserProfile(
                username = "noah",
                firstName = "Noah",
                lastName = "Patel",
                country = "IN",
                description = "Reader and writer.",
                dateOfBirth = LocalDate.of(1994, 10, 7)),
            UserProfile(
                username = "chloe",
                firstName = "Chloe",
                lastName = "Dubois",
                country = "FR",
                description = "Yoga practitioner.",
                dateOfBirth = LocalDate.of(2000, 1, 29)),
            UserProfile(
                username = "ethan",
                firstName = "Ethan",
                lastName = "Smith",
                country = "AU",
                description = "Surfing enthusiast.",
                dateOfBirth = LocalDate.of(1991, 8, 16)),
            UserProfile(
                username = "olivia",
                firstName = "Olivia",
                lastName = "Johnson",
                country = "US",
                description = "Loves painting.",
                dateOfBirth = LocalDate.of(1999, 12, 22)))

    runBlocking { sampleUsers.forEach { _repository.addUser(it) } }
  }

  /** Public repository instance (read-only) */
  var repository: UserRepository = _repository
}
