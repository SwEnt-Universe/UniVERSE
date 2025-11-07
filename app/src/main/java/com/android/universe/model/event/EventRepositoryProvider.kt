package com.android.universe.model.event

import com.android.universe.model.Tag
import com.android.universe.model.location.Location
import com.android.universe.model.user.UserRepositoryProvider.sampleUsers
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import java.time.LocalDateTime

/**
 * Singleton provider for an [EventRepository].uid instance.
 *
 * This provider supplies a pre-populated [FakeEventRepository].uid that can be used for UI
 * development and testing purposes. It is intended to be a single shared repository instance across
 * the application.
 */
object EventRepositoryProvider {
  private val _repository: EventRepository by lazy { EventRepositoryFirestore(Firebase.firestore) }

  /** Public repository instance (read-only) */
  var repository: EventRepository = _repository

  val sampleEvents =
      listOf(
          Event(
              id = "event-001",
              title = "Morning Run at the Lake",
              description = "Join us for a casual 5km run around the lake followed by coffee.",
              date = LocalDateTime.of(2025, 10, 15, 7, 30),
              tags = setOf(Tag.JAZZ, Tag.COUNTRY),
              participants = setOf(sampleUsers[0].uid, sampleUsers[1].uid, sampleUsers[1].uid),
              creator = sampleUsers[0].uid,
              location = Location(latitude = 46.5196535, longitude = 6.6322734)),
          Event(
              id = "event-002",
              title = "Tech Hackathon 2025",
              date = LocalDateTime.of(2025, 11, 3, 9, 0),
              tags = setOf(Tag.PROGRAMMING, Tag.ARTIFICIAL_INTELLIGENCE, Tag.BOAT),
              participants = setOf(sampleUsers[2].uid, sampleUsers[6].uid, sampleUsers[10].uid),
              creator = sampleUsers[2].uid,
              location = Location(latitude = 37.423021, longitude = -122.086808)),
          Event(
              id = "event-003",
              title = "Art & Wine Evening",
              description = "Relaxed evening mixing painting, wine, and music.",
              date = LocalDateTime.of(2025, 10, 22, 19, 0),
              tags = setOf(Tag.SCULPTURE, Tag.MUSIC),
              creator = sampleUsers[3].uid,
              location = Location(latitude = 47.3769, longitude = 8.5417)),
          Event(
              id = "event-004",
              title = "Sunday Hiking Trip",
              description = "Exploring the Lavaux vineyards on foot.",
              date = LocalDateTime.of(2025, 10, 19, 9, 30),
              tags = setOf(Tag.COUNTRY, Tag.ASTRONOMY),
              participants = setOf(sampleUsers[7].uid, sampleUsers[8].uid, sampleUsers[0].uid),
              creator = sampleUsers[7].uid,
              location = Location(latitude = 46.4917, longitude = 6.7289)),
          Event(
              id = "event-005",
              title = "Live Jazz Concert",
              description = "An evening of smooth jazz with local artists.",
              date = LocalDateTime.of(2025, 10, 28, 20, 0),
              tags = setOf(Tag.MUSIC, Tag.SCULPTURE),
              participants = setOf(sampleUsers[9].uid, sampleUsers[11].uid),
              creator = sampleUsers[9].uid,
              location = Location(latitude = 47.5596, longitude = 7.5886)),
          Event(
              id = "event-006",
              title = "Board Games & Pizza Night",
              date = LocalDateTime.of(2025, 10, 25, 18, 30),
              tags = setOf(Tag.RUNNING, Tag.ASTRONOMY),
              creator = sampleUsers[5].uid,
              location = Location(latitude = 46.9481, longitude = 7.4474)),
          Event(
              id = "event-007",
              title = "Photography Workshop",
              description = "Learn portrait and outdoor photography techniques.",
              date = LocalDateTime.of(2025, 11, 7, 14, 0),
              tags = setOf(Tag.MUSIC, Tag.BOAT),
              participants = setOf(sampleUsers[11].uid, sampleUsers[5].uid),
              creator = sampleUsers[11].uid,
              location = Location(latitude = 47.0502, longitude = 8.3093)),
          Event(
              id = "event-008",
              title = "Kotlin for Beginners",
              description = "Hands-on workshop to learn Kotlin fundamentals.",
              date = LocalDateTime.of(2025, 11, 14, 10, 0),
              tags = setOf(Tag.PROGRAMMING, Tag.MEDITATION),
              participants = setOf(sampleUsers[6].uid, sampleUsers[2].uid, sampleUsers[10].uid),
              creator = sampleUsers[6].uid,
              location = Location(latitude = 47.3917, longitude = 8.0455)),
          Event(
              id = "event-009",
              title = "Yoga & Meditation Morning",
              description = "Start your day with guided yoga and mindfulness.",
              date = LocalDateTime.of(2025, 10, 30, 8, 0),
              tags = setOf(Tag.HANDBALL, Tag.WRITING),
              creator = sampleUsers[12].uid,
              location = Location(latitude = 46.8011, longitude = 7.1510)),
          Event(
              id = "event-010",
              title = "Startup Pitch Night",
              description = "Pitch your idea to local investors and mentors.",
              date = LocalDateTime.of(2025, 11, 9, 18, 0),
              tags = setOf(Tag.BOAT, Tag.NEUCHATEL),
              creator = sampleUsers[13].uid,
              location = Location(latitude = 46.992, longitude = 6.931)),
          Event(
              id = "event-011",
              title = "Cultural Food Festival",
              description = "Discover food from around the world.",
              date = LocalDateTime.of(2025, 11, 12, 12, 0),
              tags = setOf(Tag.FOOT, Tag.SCULPTURE),
              participants = setOf(sampleUsers[5].uid, sampleUsers[9].uid, sampleUsers[15].uid),
              creator = sampleUsers[5].uid,
              location = Location(latitude = 46.259, longitude = 7.535)),
          Event(
              id = "event-012",
              title = "Coding Interview Practice",
              date = LocalDateTime.of(2025, 10, 21, 17, 0),
              tags = setOf(Tag.PROGRAMMING, Tag.CAR),
              participants = setOf(sampleUsers[6].uid, sampleUsers[0].uid),
              creator = sampleUsers[6].uid,
              location = Location(latitude = 47.372, longitude = 8.538)),
          Event(
              id = "event-013",
              title = "Charity Marathon",
              description = "Run for a cause - all donations go to local NGOs.",
              date = LocalDateTime.of(2025, 11, 2, 8, 30),
              tags = setOf(Tag.BASKETBALL, Tag.NEUCHATEL),
              participants = setOf(sampleUsers[1].uid, sampleUsers[8].uid, sampleUsers[10].uid),
              creator = sampleUsers[1].uid,
              location = Location(latitude = 46.204, longitude = 6.143)),
          Event(
              id = "event-014",
              title = "Cooking Class: Italian Basics",
              description = "Learn to make authentic pasta and tiramisu.",
              date = LocalDateTime.of(2025, 11, 6, 17, 30),
              tags = setOf(Tag.FOOT, Tag.WRITING),
              participants = setOf(sampleUsers[9].uid, sampleUsers[5].uid),
              creator = sampleUsers[9].uid,
              location = Location(latitude = 47.176, longitude = 8.515)),
          Event(
              id = "event-015",
              title = "Evening Book Club",
              description = "Discussing 'Sapiens'.",
              date = LocalDateTime.of(2025, 11, 4, 19, 30),
              tags = setOf(Tag.CYCLING, Tag.PHILOSOPHY),
              participants = setOf(sampleUsers[12].uid, sampleUsers[0].uid, sampleUsers[15].uid),
              creator = sampleUsers[12].uid,
              location = Location(latitude = 46.806, longitude = 6.929)),
          Event(
              id = "event-016",
              title = "Surfing Weekend",
              description = "Two days of surfing and relaxation at the Atlantic coast.",
              date = LocalDateTime.of(2025, 11, 15, 10, 0),
              tags = setOf(Tag.BASKETBALL, Tag.TRAVELING),
              participants = setOf(sampleUsers[14].uid, sampleUsers[10].uid, sampleUsers[4].uid),
              creator = sampleUsers[14].uid,
              location = Location(latitude = 43.489, longitude = -1.558)))
}
