package com.android.universe.model.ai.response

import com.android.universe.model.location.Location
import com.android.universe.model.tag.Tag
import java.time.LocalDateTime
import org.junit.Assert
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
              "date": "2069-04-12T20:00",
              "tags": ["Physics", "Mathematics"],
              "location": { "latitude": 46.52, "longitude": 6.63 }
            }
          ]
        }
        """
        .trimIndent()

    val events = ResponseParser.parseEvents(raw)

    Assert.assertEquals(1, events.size)

    val e = events.first()

    Assert.assertEquals("Physics Meetup", e.title)
    Assert.assertEquals("Quantum discussion", e.description)
    Assert.assertEquals(LocalDateTime.parse("2069-04-12T20:00"), e.date)

    Assert.assertTrue(e.tags.contains(Tag.PHYSICS))
    Assert.assertTrue(e.tags.contains(Tag.MATHEMATICS))

    Assert.assertEquals(Location(46.52, 6.63), e.location)

    Assert.assertEquals("OpenAI", e.creator)
    Assert.assertTrue(e.participants.isEmpty())
    Assert.assertEquals("", e.id) // parser sets id = ""
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

    Assert.assertEquals(2, events.size)

    Assert.assertEquals("Event A", events[0].title)
    Assert.assertEquals("Event B", events[1].title)
  }

  @Test
  fun `parseEvents gracefully returns empty list when events field is missing`() {
    val raw = """{"foo": "bar"}"""
    val events = ResponseParser.parseEvents(raw)
    Assert.assertTrue(events.isEmpty())
  }

  @Test
  fun `cleanJson removes json code fences`() {
    val raw =
      """
        ```json
        {
          "events": []
        }
        ```
        """
        .trimIndent()

    val events = ResponseParser.parseEvents(raw)

    Assert.assertTrue(events.isEmpty())
  }

  @Test
  fun `cleanJson removes backticks`() {
    val raw =
      """
        ```
        {"events": []}
        ```
        """
        .trimIndent()

    val events = ResponseParser.parseEvents(raw)

    Assert.assertTrue(events.isEmpty())
  }

  @Test
  fun `parseEvents ignores extra unknown fields`() {
    val raw =
      """
        {
          "events": [
            {
              "title": "Valid Event",
              "description": "Good",
              "date": "2030-01-01T10:00",
              "tags": ["Physics"],
              "location": { "latitude": 0.0, "longitude": 0.0 },
              "unexpected_field": "ignored",
              "extra": { "nested": "ignored" }
            }
          ]
        }
        """
        .trimIndent()

    val events = ResponseParser.parseEvents(raw)

    Assert.assertEquals(1, events.size)
    Assert.assertEquals("Valid Event", events.first().title)
  }
}
