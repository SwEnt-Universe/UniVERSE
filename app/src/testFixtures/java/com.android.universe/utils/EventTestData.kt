package com.android.universe.utils

import com.android.universe.model.Tag
import com.android.universe.model.event.Event
import com.android.universe.model.location.Location
import java.time.LocalDateTime

object EventTestData {
  private val userWithNullDescription = UserTestData.NullDescription
  private val userWithManyTags = UserTestData.ManyTagsUser
  private val Alice = UserTestData.Alice
  private val Bob = UserTestData.Bob
  private val Rocky = UserTestData.Rocky

  private val DummyLocation = Location(latitude = 46.5196535, longitude = 6.6322734)

  private val DummyDate = LocalDateTime.of(2025, 10, 15, 7, 30)

  val dummyEvent1 =
      Event(
          id = "1",
          title = "Morning Run at the Lake",
          description = "Join us for a casual 5km run around the lake followed by coffee.",
          date = LocalDateTime.of(2025, 10, 15, 7, 30),
          tags = setOf(Tag.JAZZ, Tag.COUNTRY),
          participants = setOf(UserTestData.Bob, UserTestData.Alice),
          creator = UserTestData.Bob,
          location = Location(latitude = 46.5196535, longitude = 6.6322734))
  val dummyEvent2 =
      Event(
          id = "2",
          title = "Tech Hackathon 2025",
          date = LocalDateTime.of(2025, 11, 3, 9, 0),
          tags = setOf(Tag.PROGRAMMING, Tag.ARTIFICIAL_INTELLIGENCE, Tag.BOAT),
          participants = setOf(Rocky),
          creator = Rocky,
          location = Location(latitude = 46.5196535, longitude = 6.6322734))
  val dummyEvent3 =
      Event(
          id = "3",
          title = "Art & Wine Evening",
          description = "Relaxed evening mixing painting, wine, and music.",
          date = LocalDateTime.of(2025, 10, 22, 19, 0),
          tags = setOf(Tag.SCULPTURE, Tag.MUSIC),
          participants = setOf(Alice),
          creator = Alice,
          location = Location(latitude = 46.5196535, longitude = 6.6322734))
  val FullDescriptionEvent =
      Event(
          id = "event-001",
          title = "Morning Run at the Lake",
          description = "Join us for a casual 5km run around the lake followed by coffee.",
          date = DummyDate,
          tags = setOf(Tag.SCULPTURE, Tag.COUNTRY),
          participants = setOf(userWithNullDescription, userWithManyTags),
          creator = userWithNullDescription,
          location = DummyLocation)

  val EmptyDescriptionEvent =
      Event(
          id = "event-002",
          title = "Morning Run at the Lake",
          description = "",
          date = DummyDate,
          tags = setOf(Tag.SCULPTURE, Tag.COUNTRY),
          participants = setOf(userWithNullDescription, userWithManyTags),
          creator = userWithNullDescription,
          location = DummyLocation)
  val NullDescriptionEvent =
      Event(
          id = "event-003",
          title = "Morning Run at the Lake",
          description = null,
          date = DummyDate,
          tags = setOf(Tag.SCULPTURE, Tag.COUNTRY),
          participants = setOf(userWithNullDescription, userWithManyTags),
          creator = userWithNullDescription,
          location = DummyLocation)

  val NoParticipantEvent =
      Event(
          id = "event-004",
          title = "Morning Run at the Lake",
          description = null,
          date = DummyDate,
          tags = setOf(Tag.SCULPTURE, Tag.COUNTRY),
          participants = emptySet(),
          creator = userWithNullDescription,
          location = DummyLocation)

  val NoTagsEvent =
      Event(
          id = "event-005",
          title = "Morning Run at the Lake",
          description = null,
          date = DummyDate,
          tags = emptySet(),
          participants = emptySet(),
          creator = userWithNullDescription,
          location = DummyLocation)

  val SomeTagsEvent =
      Event(
          id = "event-006",
          title = "Morning Run at the Lake",
          description = null,
          date = DummyDate,
          tags = UserTestData.someTags,
          participants = emptySet(),
          creator = userWithNullDescription,
          location = DummyLocation)
}
