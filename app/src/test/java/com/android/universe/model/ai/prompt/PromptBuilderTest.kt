package com.android.universe.model.ai.prompt

import com.android.universe.model.tag.Tag
import com.android.universe.model.user.UserProfile
import java.time.LocalDate
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Test

class PromptBuilderTest {

  private val DummyDate = LocalDate.of(2000, 8, 11)

  private val allTags =
    (Tag.Companion.getTagsForCategory(Tag.Category.TOPIC) +
        Tag.Companion.getTagsForCategory(Tag.Category.FOOD))
      .toSet()

  private val user =
    UserProfile(
      uid = "69",
      username = "ai_69",
      firstName = "AI",
      lastName = "Base",
      country = "CH",
      description = "Has all tags, country = Switzerland",
      dateOfBirth = DummyDate,
      tags = allTags)

  @Ignore("Developer debugging tool. Used to print/inspect PromptBuilder output")
  @Test
  fun printPrompt() {
    val task = TaskConfig(eventCount = 5, requireRelevantTags = true)

    val context =
      ContextConfig(
        location = "Lausanne",
        radiusKm = 5,
        timeFrame = "today",
        locationCoordinates = 46.5191 to 6.5668)

    val system = PromptBuilder.buildSystemMessage()
    val userMsg = PromptBuilder.buildUserMessage(user, task, context)

    println("SYSTEM MESSAGE:\n$system\n")
    println("USER MESSAGE:\n$userMsg")
  }

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

    // ------------------------------------------------------------
    // TASK
    // ------------------------------------------------------------
    val taskObj = root["task"]!!.jsonObject

    assertEquals(
      "generate public, drop-in, realistic events that match the environment and the user's interests when feasible.",
      taskObj["goal"]!!.jsonPrimitive.content)

    assertEquals(3, taskObj["eventsToGenerate"]!!.jsonPrimitive.int)
    assertEquals(true, taskObj["requireRelevantTags"]!!.jsonPrimitive.boolean)

    // ------------------------------------------------------------
    // USER
    // ------------------------------------------------------------
    val userObj = root["user"]!!.jsonObject

    assertEquals(profile.uid, userObj["uid"]!!.jsonPrimitive.content)
    assertEquals("John Doe", userObj["name"]!!.jsonPrimitive.content)
    assertEquals("Example description", userObj["description"]!!.jsonPrimitive.content)

    val interestsJson = userObj["interests"]!!.jsonArray.toString()
    assertTrue(interestsJson.contains("Rock"))
    assertTrue(interestsJson.contains("Music"))

    // ------------------------------------------------------------
    // CONTEXT
    // ------------------------------------------------------------
    val ctxObj = root["context"]!!.jsonObject

    assertEquals("Geneva", ctxObj["location"]!!.jsonPrimitive.content)

    val coords = ctxObj["coordinates"]!!.jsonObject
    assertEquals("47.0", coords["lat"]!!.jsonPrimitive.content)
    assertEquals("8.0", coords["lon"]!!.jsonPrimitive.content)

    assertEquals("15", ctxObj["radiusKm"]!!.jsonPrimitive.content)
    assertEquals("this-week", ctxObj["timeFrame"]!!.jsonPrimitive.content)

    assertEquals(ctx.currentDate.toString(), ctxObj["currentDate"]!!.jsonPrimitive.content)
  }
}
