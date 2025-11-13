package com.android.universe.model.event

import com.android.universe.model.location.Location
import com.android.universe.model.tag.Tag
import com.android.universe.model.user.UserRepositoryProvider.sampleUsers
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDateTime

/**
 * Singleton provider for an [EventRepository].uid instance.
 *
 * This provider supplies a pre-populated [FakeEventRepository].uid that can be used for UI
 * development and testing purposes. It is intended to be a single shared repository instance across
 * the application.
 */
object EventRepositoryProvider {
  private val _repository: EventRepository by lazy {
    EventRepositoryFirestore(FirebaseFirestore.getInstance())
  }

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
      )
}
