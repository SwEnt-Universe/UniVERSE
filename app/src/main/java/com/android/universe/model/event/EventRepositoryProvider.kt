package com.android.universe.model.event

import com.android.universe.model.Tag
import com.android.universe.model.location.Location
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
                tags = setOf(Tag.JAZZ, Tag.COUNTRY),
                participants = setOf(users[0], users[1]),
                creator = users[0],
                location = Location(latitude = 46.5196535, longitude = 6.6322734)),
            Event(
                id = "event-002",
                title = "Tech Hackathon 2025",
                date = LocalDateTime.of(2025, 11, 3, 9, 0),
                tags = setOf(Tag.PROGRAMMING, Tag.ARTIFICIAL_INTELLIGENCE, Tag.BOAT),
                participants = setOf(users[2], users[6], users[10]),
                creator = users[2],
                location = Location(latitude = 37.423021, longitude = -122.086808)),
            Event(
                id = "event-003",
                title = "Art & Wine Evening",
                description = "Relaxed evening mixing painting, wine, and music.",
                date = LocalDateTime.of(2025, 10, 22, 19, 0),
                tags = setOf(Tag.SCULPTURE, Tag.MUSIC),
                creator = users[3],
                location = Location(latitude = 47.3769, longitude = 8.5417)),
            Event(
                id = "event-004",
                title = "Sunday Hiking Trip",
                description = "Exploring the Lavaux vineyards on foot.",
                date = LocalDateTime.of(2025, 10, 19, 9, 30),
                tags = setOf(Tag.COUNTRY, Tag.ASTRONOMY),
                participants = setOf(users[7], users[8], users[0]),
                creator = users[7],
                location = Location(latitude = 46.4917, longitude = 6.7289)),
            Event(
                id = "event-005",
                title = "Live Jazz Concert",
                description = "An evening of smooth jazz with local artists.",
                date = LocalDateTime.of(2025, 10, 28, 20, 0),
                tags = setOf(Tag.MUSIC, Tag.SCULPTURE),
                participants = setOf(users[9], users[11]),
                creator = users[9],
                location = Location(latitude = 47.5596, longitude = 7.5886)),
            Event(
                id = "event-006",
                title = "Board Games & Pizza Night",
                date = LocalDateTime.of(2025, 10, 25, 18, 30),
                tags = setOf(Tag.RUNNING, Tag.ASTRONOMY),
                creator = users[5],
                location = Location(latitude = 46.9481, longitude = 7.4474)),
            Event(
                id = "event-007",
                title = "Photography Workshop",
                description = "Learn portrait and outdoor photography techniques.",
                date = LocalDateTime.of(2025, 11, 7, 14, 0),
                tags = setOf(Tag.MUSIC, Tag.BOAT),
                participants = setOf(users[11], users[5]),
                creator = users[11],
                location = Location(latitude = 47.0502, longitude = 8.3093)),
            Event(
                id = "event-008",
                title = "Kotlin for Beginners",
                description = "Hands-on workshop to learn Kotlin fundamentals.",
                date = LocalDateTime.of(2025, 11, 14, 10, 0),
                tags = setOf(Tag.PROGRAMMING, Tag.MEDITATION),
                participants = setOf(users[6], users[2], users[10]),
                creator = users[6],
                location = Location(latitude = 47.3917, longitude = 8.0455)),
            Event(
                id = "event-009",
                title = "Yoga & Meditation Morning",
                description = "Start your day with guided yoga and mindfulness.",
                date = LocalDateTime.of(2025, 10, 30, 8, 0),
                tags = setOf(Tag.HANDBALL, Tag.WRITING),
                creator = users[12],
                location = Location(latitude = 46.8011, longitude = 7.1510)),
            Event(
                id = "event-010",
                title = "Startup Pitch Night",
                description = "Pitch your idea to local investors and mentors.",
                date = LocalDateTime.of(2025, 11, 9, 18, 0),
                tags = setOf(Tag.BOAT, Tag.NEUCHATEL),
                creator = users[13],
                location = Location(latitude = 46.992, longitude = 6.931)),
            Event(
                id = "event-011",
                title = "Cultural Food Festival",
                description = "Discover food from around the world.",
                date = LocalDateTime.of(2025, 11, 12, 12, 0),
                tags = setOf(Tag.FOOT, Tag.SCULPTURE),
                participants = setOf(users[5], users[9], users[15]),
                creator = users[5],
                location = Location(latitude = 46.259, longitude = 7.535)),
            Event(
                id = "event-012",
                title = "Coding Interview Practice",
                date = LocalDateTime.of(2025, 10, 21, 17, 0),
                tags = setOf(Tag.PROGRAMMING, Tag.CAR),
                participants = setOf(users[6], users[0]),
                creator = users[6],
                location = Location(latitude = 47.372, longitude = 8.538)),
            Event(
                id = "event-013",
                title = "Charity Marathon",
                description = "Run for a cause - all donations go to local NGOs.",
                date = LocalDateTime.of(2025, 11, 2, 8, 30),
                tags = setOf(Tag.BASKETBALL, Tag.NEUCHATEL),
                participants = setOf(users[1], users[8], users[10]),
                creator = users[1],
                location = Location(latitude = 46.204, longitude = 6.143)),
            Event(
                id = "event-014",
                title = "Cooking Class: Italian Basics",
                description = "Learn to make authentic pasta and tiramisu.",
                date = LocalDateTime.of(2025, 11, 6, 17, 30),
                tags = setOf(Tag.FOOT, Tag.WRITING),
                participants = setOf(users[9], users[5]),
                creator = users[9],
                location = Location(latitude = 47.176, longitude = 8.515)),
            Event(
                id = "event-015",
                title = "Evening Book Club",
                description = "Discussing 'Sapiens'.",
                date = LocalDateTime.of(2025, 11, 4, 19, 30),
                tags = setOf(Tag.CYCLING, Tag.PHILOSOPHY),
                participants = setOf(users[12], users[0], users[15]),
                creator = users[12],
                location = Location(latitude = 46.806, longitude = 6.929)),
            Event(
                id = "event-016",
                title = "Surfing Weekend",
                description = "Two days of surfing and relaxation at the Atlantic coast.",
                date = LocalDateTime.of(2025, 11, 15, 10, 0),
                tags = setOf(Tag.BASKETBALL, Tag.TRAVELING),
                participants = setOf(users[14], users[10], users[4]),
                creator = users[14],
                location = Location(latitude = 43.489, longitude = -1.558)))

    runBlocking { sampleEvents.forEach { _repository.addEvent(it) } }
  }

  /** Public repository instance (read-only) */
  var repository: EventRepository = _repository
}
