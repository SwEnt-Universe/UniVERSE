package com.android.universe.utils

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

  private val BaseEvent =
      Event(
          id = "0",
          title = "Morning Run at the Lake",
          description = "Join us for a casual 5km run around the lake followed by coffee.",
          date = DummyDate,
          tags = UserTestData.someTags,
          participants = emptySet(),
          creator = Alice,
          location = DummyLocation)

  val dummyEvent1 = BaseEvent.copy(id = "1", participants = setOf(Bob, Alice), creator = Bob)
  val dummyEvent2 =
      BaseEvent.copy(
          id = "2", title = "Tech Hackathon 2025", participants = setOf(Rocky), creator = Rocky)
  val dummyEvent3 =
      BaseEvent.copy(
          id = "3",
          title = "Art & Wine Evening",
          description = "Relaxed evening mixing painting, wine, and music.",
          participants = setOf(Alice),
      )
  val FullDescriptionEvent =
      BaseEvent.copy(
          id = "event-001",
          participants = setOf(userWithNullDescription, userWithManyTags),
          creator = userWithNullDescription)

  val EmptyDescriptionEvent =
      BaseEvent.copy(
          id = "event-002",
          title = "Morning Run at the Lake",
          description = "",
          participants = setOf(userWithNullDescription, userWithManyTags),
          creator = userWithNullDescription)
  val NullDescriptionEvent =
      BaseEvent.copy(
          id = "event-003",
          description = null,
          participants = setOf(userWithNullDescription, userWithManyTags),
          creator = userWithNullDescription)
  val NoParticipantEvent = BaseEvent.copy(id = "event-004", creator = userWithNullDescription)
  val NoTagsEvent =
      BaseEvent.copy(
          id = "event-005",
          tags = emptySet(),
          creator = userWithNullDescription,
      )

  val SomeTagsEvent =
      BaseEvent.copy(
          id = "event-006",
          tags = UserTestData.someTags,
      )
}
