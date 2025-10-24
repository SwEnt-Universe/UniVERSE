package com.android.universe.utils

import com.android.universe.model.Tag
import com.android.universe.model.event.Event
import com.android.universe.model.location.Location
import java.time.LocalDateTime

object EventTestData {
  private val Alice = UserTestData.NullDescription
  private val Bob = UserTestData.ManyTagsUser

  private val DummyLocation = Location(latitude = 46.5196535, longitude = 6.6322734)

  private val DummyDate = LocalDateTime.of(2025, 10, 15, 7, 30)
  val FullDescriptionEvent =
      Event(
          id = "event-001",
          title = "Morning Run at the Lake",
          description = "Join us for a casual 5km run around the lake followed by coffee.",
          date = DummyDate,
          tags = setOf(Tag.SCULPTURE, Tag.COUNTRY),
          participants = setOf(Alice, Bob),
          creator = Alice,
          location = DummyLocation)

  val EmptyDescriptionEvent =
      Event(
          id = "event-002",
          title = "Morning Run at the Lake",
          description = "",
          date = DummyDate,
          tags = setOf(Tag.SCULPTURE, Tag.COUNTRY),
          participants = setOf(Alice, Bob),
          creator = Alice,
          location = DummyLocation)
  val NullDescriptionEvent =
      Event(
          id = "event-003",
          title = "Morning Run at the Lake",
          description = null,
          date = DummyDate,
          tags = setOf(Tag.SCULPTURE, Tag.COUNTRY),
          participants = setOf(Alice, Bob),
          creator = Alice,
          location = DummyLocation)

  val NoParticipantEvent =
      Event(
          id = "event-004",
          title = "Morning Run at the Lake",
          description = null,
          date = DummyDate,
          tags = setOf(Tag.SCULPTURE, Tag.COUNTRY),
          participants = emptySet(),
          creator = Alice,
          location = DummyLocation)

  val NoTagsEvent =
      Event(
          id = "event-005",
          title = "Morning Run at the Lake",
          description = null,
          date = DummyDate,
          tags = emptySet(),
          participants = emptySet(),
          creator = Alice,
          location = DummyLocation)

  val SomeTagsEvent =
      Event(
          id = "event-006",
          title = "Morning Run at the Lake",
          description = null,
          date = DummyDate,
          tags = UserTestData.someTags,
          participants = emptySet(),
          creator = Alice,
          location = DummyLocation)
}
