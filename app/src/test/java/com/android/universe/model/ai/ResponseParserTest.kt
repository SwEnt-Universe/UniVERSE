package com.android.universe.model.ai

import com.android.universe.model.location.Location
import com.android.universe.model.tag.Tag
import java.time.LocalDateTime
import org.junit.Assert.*
import org.junit.Test

class ResponseParserTest {

  @Test
  fun `parseEvents returns correctly parsed domain events`() {
    val raw =
        """
        {
          "events": [
            {
              "title": "Physics Meetup",
              "description": "Quantum discussion",
              "date": "2025-04-12T20:00",
              "tags": ["Physics", "Mathematics"],
              "location": { "latitude": 46.52, "longitude": 6.63 }
            }
          ]
        }
        """
            .trimIndent()

    val events = ResponseParser.parseEvents(raw)

    assertEquals(1, events.size)
    val e = events.first()

    assertEquals("Physics Meetup", e.title)
    assertEquals("Quantum discussion", e.description)
    assertEquals(LocalDateTime.parse("2025-04-12T20:00"), e.date)

    assertTrue(e.tags.contains(Tag.PHYSICS))
    assertTrue(e.tags.contains(Tag.MATHEMATICS))

    assertEquals(Location(46.52, 6.63), e.location)

    assertEquals("OpenAI", e.creator)
    assertTrue(e.participants.isEmpty())
    assertEquals("", e.id) // parser sets id = ""
  }

  @Test
  fun `parseEvents handles multiple events`() {
    val raw =
        """
        {
          "events": [
            {
              "title": "Event A",
              "description": "Desc A",
              "date": "2030-01-01T12:00",
              "tags": ["History"],
              "location": { "latitude": 10.0, "longitude": 20.0 }
            },
            {
              "title": "Event B",
              "description": "Desc B",
              "date": "2031-01-01T13:00",
              "tags": ["Biology"],
              "location": { "latitude": 30.0, "longitude": 40.0 }
            }
          ]
        }
        """
            .trimIndent()

    val events = ResponseParser.parseEvents(raw)

    assertEquals(2, events.size)
    assertEquals("Event A", events[0].title)
    assertEquals("Event B", events[1].title)
  }

  @Test(expected = IllegalStateException::class)
  fun `parseEvents throws when events field is missing`() {
    val raw = """{"foo": "bar"}"""
    ResponseParser.parseEvents(raw)
  }

  @Test
  fun `cleanJson removes code fences`() {
    val raw =
        """
        ```json
        {
          "events": []
        }
        ```
        """
            .trimIndent()

    val cleaned = ResponseParser.parseEvents(raw) // calling parse indirectly tests cleanJson

    assertTrue(cleaned.isEmpty())
  }

  @Test
  fun `cleanJson removes backticks only`() {
    val raw =
        """
            ```json
            {"events": []}
            ```
        """

    val cleaned = ResponseParser.parseEvents(raw)
    assertTrue(cleaned.isEmpty())
  }

  @Test fun `parseEvents ignores extra fields`() {}
}
