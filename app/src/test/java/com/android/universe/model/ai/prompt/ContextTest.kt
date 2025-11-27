package com.android.universe.model.ai.prompt

import com.android.universe.model.tag.Tag
import com.android.universe.model.user.UserProfile
import java.time.LocalDate
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.*
import org.junit.Test

class ContextTest {

  // ----------------------------------------------------------------------
  // Helpers
  // ----------------------------------------------------------------------

  private fun dummyProfile() =
      UserProfile(
          uid = "u1",
          username = "johnny",
          firstName = "John",
          lastName = "Doe",
          country = "CH",
          description = "Example description",
          dateOfBirth = LocalDate.of(2000, 1, 1),
          tags = setOf(Tag.ROCK, Tag.MUSIC))

  // ----------------------------------------------------------------------
  // TaskConfig Tests
  // ----------------------------------------------------------------------

  @Test
  fun taskConfig_defaultValues() {
    val c = TaskConfig.Default

    assertNull(c.eventCount)
    assertTrue(c.requireRelevantTags)
  }

  @Test
  fun taskConfig_customValues() {
    val c = TaskConfig(eventCount = 5, requireRelevantTags = false)

    assertEquals(5, c.eventCount)
    assertFalse(c.requireRelevantTags)
  }

  @Test
  fun taskConfig_equalityAndHashCode() {
    val a = TaskConfig(3, false)
    val b = TaskConfig(3, false)

    assertEquals(a, b)
    assertEquals(a.hashCode(), b.hashCode())
  }

  // ----------------------------------------------------------------------
  // ContextConfig Tests
  // ----------------------------------------------------------------------

  @Test
  fun contextConfig_defaultValues() {
    val c = ContextConfig.Default

    assertEquals("Lausanne", c.location)
    assertNull(c.locationCoordinates)
    assertNull(c.radiusKm)
    assertEquals("today", c.timeFrame)
  }

  @Test
  fun contextConfig_customValues() {
    val coords = 46.5 to 6.6
    val c =
        ContextConfig(
            location = "Zurich",
            locationCoordinates = coords,
            radiusKm = 10,
            timeFrame = "tomorrow")

    assertEquals("Zurich", c.location)
    assertEquals(coords, c.locationCoordinates)
    assertEquals(10, c.radiusKm)
    assertEquals("tomorrow", c.timeFrame)
  }

  @Test
  fun contextConfig_equalityAndHashCode() {
    val a = ContextConfig("X", 1.0 to 2.0, 5, "week")
    val b = ContextConfig("X", 1.0 to 2.0, 5, "week")

    assertEquals(a, b)
    assertEquals(a.hashCode(), b.hashCode())
  }

  // ----------------------------------------------------------------------
  // EventQuery Tests
  // ----------------------------------------------------------------------

  @Test
  fun eventQuery_usesDefaults() {
    val profile = dummyProfile()
    val q = EventQuery(user = profile)

    assertEquals(TaskConfig.Default, q.task)
    assertEquals(ContextConfig.Default, q.context)
    assertEquals(profile, q.user)
  }

  @Test
  fun eventQuery_customValues() {
    val profile = dummyProfile()
    val task = TaskConfig(eventCount = 2)
    val context = ContextConfig(location = "Bern")

    val q = EventQuery(profile, task, context)

    assertEquals(profile, q.user)
    assertEquals(task, q.task)
    assertEquals(context, q.context)
  }

  // ----------------------------------------------------------------------
  // EventSchema Tests
  // ----------------------------------------------------------------------

  @Test
  fun eventSchema_jsonIsValid() {
    val parsed = Json.parseToJsonElement(EventSchema.json)
    assertTrue(parsed is kotlinx.serialization.json.JsonObject)
    assertNotNull(parsed.jsonObject["schema"])
  }

  @Test
  fun eventSchema_jsonObject_matchesParsedJson() {
    val parsed = Json.parseToJsonElement(EventSchema.json).jsonObject
    assertEquals(parsed, EventSchema.jsonObject)
  }

  // ----------------------------------------------------------------------
  // PromptBuilder Tests
  // ----------------------------------------------------------------------

  @Test
  fun promptBuilder_systemMessage_isValidStrictJson() {
    val txt = PromptBuilder.buildSystemMessage()
    val json = Json.parseToJsonElement(txt).jsonObject

    assertEquals("EventCuratorGPT", json["role"]!!.toString().trim('"'))
    assertTrue(json["rules"] != null)
    assertTrue(json["rules"].toString().contains("events"))
  }

  @Test
  fun promptBuilder_userMessage_containsTaskUserContext() {
    val profile = dummyProfile()
    val task = TaskConfig(eventCount = 3, requireRelevantTags = true)
    val ctx =
        ContextConfig(
            location = "Geneva",
            locationCoordinates = 47.0 to 8.0,
            radiusKm = 15,
            timeFrame = "this-week")

    val txt = PromptBuilder.buildUserMessage(profile, task, ctx)
    val root = Json.parseToJsonElement(txt).jsonObject

    // --- Task ---
    val taskObj = root["task"]!!.jsonObject
    assertEquals(
        "generate realistic public events matching the user's interests",
        taskObj["goal"]!!.toString().trim('"'))
    assertEquals(3, taskObj["eventsToGenerate"]!!.toString().toInt())
    assertEquals(true, taskObj["requireRelevantTags"]!!.toString().toBoolean())

    // --- User ---
    val userObj = root["user"]!!.jsonObject
    assertEquals(profile.uid, userObj["uid"]!!.toString().trim('"'))
    assertEquals("John Doe", userObj["name"]!!.toString().trim('"'))
    assertEquals("Example description", userObj["description"]!!.toString().trim('"'))
    assertTrue(userObj["interests"].toString().contains("Rock"))
    assertTrue(userObj["interests"].toString().contains("Music"))

    // --- Context ---
    val ctxObj = root["context"]!!.jsonObject
    assertEquals("Geneva", ctxObj["location"]!!.toString().trim('"'))

    val coords = ctxObj["coordinates"]!!.jsonObject
    assertEquals("47.0", coords["lat"]!!.toString().trim('"'))
    assertEquals("8.0", coords["lon"]!!.toString().trim('"'))

    assertEquals("15", ctxObj["radiusKm"]!!.toString().trim('"'))
    assertEquals("this-week", ctxObj["timeFrame"]!!.toString().trim('"'))
  }
}
