package com.android.universe.model.event

import com.android.universe.model.Tag
import com.android.universe.model.user.UserRepositoryProvider
import java.time.LocalDateTime
import kotlinx.coroutines.runBlocking

/**
 * Singleton provider for an [EventRepository] instance.
 *
 * This provider supplies a pre-populated [FakeEventRepository] that can be used for UI development
 * and testing purposes. It is intended to be a single shared repository instance across the
 * application.
 */
object EventRepositoryProvider {
  private val _repository: EventRepository = FakeEventRepository()

  init {
    val users = runBlocking { UserRepositoryProvider.repository.getAllUsers() }

    val sampleEvents =
        listOf(
            Event(
                id = "event-001",
                title = "Morning Run at the Lake",
                description = "Join us for a casual 5km run around the lake followed by coffee.",
                date = LocalDateTime.of(2025, 10, 15, 7, 30),
                tags = setOf(Tag("Sport"), Tag("Outdoor")),
                participants = setOf(users[0], users[1])),
            Event(
                id = "event-002",
                title = "Tech Hackathon 2025",
                date = LocalDateTime.of(2025, 11, 3, 9, 0),
                tags = setOf(Tag("Tech"), Tag("AI"), Tag("Innovation")),
                participants = setOf(users[2], users[6], users[10])),
            Event(
                id = "event-003",
                title = "Art & Wine Evening",
                description = "Relaxed evening mixing painting, wine, and music.",
                date = LocalDateTime.of(2025, 10, 22, 19, 0),
                tags = setOf(Tag("Culture"), Tag("Social"))),
            Event(
                id = "event-004",
                title = "Sunday Hiking Trip",
                description = "Exploring the Lavaux vineyards on foot.",
                date = LocalDateTime.of(2025, 10, 19, 9, 30),
                tags = setOf(Tag("Outdoor"), Tag("Nature")),
                participants = setOf(users[7], users[8], users[0])),
            Event(
                id = "event-005",
                title = "Live Jazz Concert",
                description = "An evening of smooth jazz with local artists.",
                date = LocalDateTime.of(2025, 10, 28, 20, 0),
                tags = setOf(Tag("Music"), Tag("Culture")),
                participants = setOf(users[9], users[11])),
            Event(
                id = "event-006",
                title = "Board Games & Pizza Night",
                date = LocalDateTime.of(2025, 10, 25, 18, 30),
                tags = setOf(Tag("Fun"), Tag("Social"))),
            Event(
                id = "event-007",
                title = "Photography Workshop",
                description = "Learn portrait and outdoor photography techniques.",
                date = LocalDateTime.of(2025, 11, 7, 14, 0),
                tags = setOf(Tag("Art"), Tag("Workshop")),
                participants = setOf(users[11], users[5])),
            Event(
                id = "event-008",
                title = "Kotlin for Beginners",
                description = "Hands-on workshop to learn Kotlin fundamentals.",
                date = LocalDateTime.of(2025, 11, 14, 10, 0),
                tags = setOf(Tag("Tech"), Tag("Education")),
                participants = setOf(users[6], users[2], users[10])),
            Event(
                id = "event-009",
                title = "Yoga & Meditation Morning",
                description = "Start your day with guided yoga and mindfulness.",
                date = LocalDateTime.of(2025, 10, 30, 8, 0),
                tags = setOf(Tag("Health"), Tag("Wellness"))),
            Event(
                id = "event-010",
                title = "Startup Pitch Night",
                description = "Pitch your idea to local investors and mentors.",
                date = LocalDateTime.of(2025, 11, 9, 18, 0),
                tags = setOf(Tag("Business"), Tag("Networking"))),
            Event(
                id = "event-011",
                title = "Cultural Food Festival",
                description = "Discover food from around the world.",
                date = LocalDateTime.of(2025, 11, 12, 12, 0),
                tags = setOf(Tag("Food"), Tag("Culture")),
                participants = setOf(users[5], users[9], users[15])),
            Event(
                id = "event-012",
                title = "Coding Interview Practice",
                date = LocalDateTime.of(2025, 10, 21, 17, 0),
                tags = setOf(Tag("Tech"), Tag("Career")),
                participants = setOf(users[6], users[0])),
            Event(
                id = "event-013",
                title = "Charity Marathon",
                description = "Run for a cause - all donations go to local NGOs.",
                date = LocalDateTime.of(2025, 11, 2, 8, 30),
                tags = setOf(Tag("Sport"), Tag("Charity")),
                participants = setOf(users[1], users[8], users[10])),
            Event(
                id = "event-014",
                title = "Cooking Class: Italian Basics",
                description = "Learn to make authentic pasta and tiramisu.",
                date = LocalDateTime.of(2025, 11, 6, 17, 30),
                tags = setOf(Tag("Food"), Tag("Workshop")),
                participants = setOf(users[9], users[5])),
            Event(
                id = "event-015",
                title = "Evening Book Club",
                description = "Discussing 'Sapiens'.",
                date = LocalDateTime.of(2025, 11, 4, 19, 30),
                tags = setOf(Tag("Literature"), Tag("Social")),
                participants = setOf(users[12], users[0], users[15])),
            Event(
                id = "event-016",
                title = "Surfing Weekend",
                description = "Two days of surfing and relaxation at the Atlantic coast.",
                date = LocalDateTime.of(2025, 11, 15, 10, 0),
                tags = setOf(Tag("Sport"), Tag("Travel")),
                participants = setOf(users[14], users[10], users[4])))

    runBlocking { sampleEvents.forEach { _repository.addEvent(it) } }
  }

  /** Public repository instance (read-only) */
  var repository: EventRepository = _repository
}
