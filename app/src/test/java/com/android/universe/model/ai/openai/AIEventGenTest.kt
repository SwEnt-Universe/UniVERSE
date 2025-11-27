package com.android.universe.model.ai.openai

import com.android.universe.model.ai.AIEventGen
import com.android.universe.model.ai.prompt.ContextConfig
import com.android.universe.model.ai.prompt.EventQuery
import com.android.universe.model.ai.prompt.TaskConfig
import com.android.universe.model.event.Event
import com.android.universe.model.tag.Tag
import com.android.universe.model.user.UserProfile
import java.time.LocalDate
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Response

// ======================================================================
// Top-level reusable test fixtures (shared across all tests)
// ======================================================================

private val TEST_USER =
    UserProfile(
        uid = "test",
        username = "tester",
        firstName = "Test",
        lastName = "User",
        country = "CH",
        description = "",
        dateOfBirth = LocalDate.of(2000, 1, 1),
        tags = emptySet())

private val TEST_CONTEXT =
    ContextConfig(
        location = "Lausanne", locationCoordinates = 46.5 to 6.5, radiusKm = 5, timeFrame = "today")

private fun minimalQuery() =
    EventQuery(
        user = TEST_USER,
        task = TaskConfig(eventCount = 1, requireRelevantTags = false),
        context = TEST_CONTEXT)

// ======================================================================
// TESTS
// ======================================================================

class AIEventGenTest {

  // --------------------------------------------------------------
  // Success case (your existing test)
  // --------------------------------------------------------------
  @Test
  fun generateEvents_parsesFakeJsonIntoEventObjects() = runTest {
    val service = OpenAIServiceFake()
    val generator: AIEventGen = OpenAIEventGen(service)

    val tags = setOf(Tag.ROCK, Tag.MUSIC)

    val profile =
        UserProfile(
            uid = "u123",
            username = "johnny",
            firstName = "John",
            lastName = "Doe",
            country = "CH",
            description = "Test user",
            dateOfBirth = LocalDate.of(2000, 8, 11),
            tags = tags)

    val query =
        EventQuery(
            user = profile,
            task = TaskConfig(eventCount = 1, requireRelevantTags = true),
            context =
                ContextConfig(
                    location = "Lausanne",
                    locationCoordinates = 46.52 to 6.63,
                    radiusKm = 5,
                    timeFrame = "today"))

    val events: List<Event> = generator.generateEvents(query)

    assertNotNull(events)
    assertEquals(1, events.size)

    val event = events.first()
    assertEquals("Fake Rock Concert", event.title)
    assertEquals("A generated test event", event.description)
    assertEquals(46.52, event.location.latitude, 0.0001)
    assertEquals(6.63, event.location.longitude, 0.0001)

    val tagNames = event.tags.map { it.displayName }.toSet()
    assertEquals(setOf("Rock", "Music"), tagNames)

    assertEquals("OpenAI", event.creator)
    assertEquals("2069-03-21T20:00", event.date.toString())
  }

  // --------------------------------------------------------------
  // Failure case 1: HTTP error status
  // --------------------------------------------------------------
  @Test
  fun generateEvents_throwsOnHttpError() = runTest {
    val failingService =
        object : OpenAIService {
          override suspend fun chatCompletion(request: ChatCompletionRequest) =
              Response.error<ChatCompletionResponse>(
                  500, ResponseBody.create("text/plain".toMediaTypeOrNull(), "server exploded"))

          override suspend fun chatCompletionStream(request: ChatCompletionRequest) =
              Response.success(ResponseBody.create(null, ""))
        }

    val gen = OpenAIEventGen(failingService)
    val query = minimalQuery()

    assertThrows(IllegalStateException::class.java) { runTest { gen.generateEvents(query) } }
  }

  // --------------------------------------------------------------
  // Failure case 2: body == null
  // --------------------------------------------------------------
  @Test
  fun generateEvents_throwsWhenBodyNull() = runTest {
    val service =
        object : OpenAIService {
          override suspend fun chatCompletion(request: ChatCompletionRequest) =
              Response.success<ChatCompletionResponse>(null)

          override suspend fun chatCompletionStream(request: ChatCompletionRequest) =
              Response.success(ResponseBody.create(null, ""))
        }

    val gen = OpenAIEventGen(service)

    assertThrows(IllegalStateException::class.java) {
      runTest { gen.generateEvents(minimalQuery()) }
    }
  }

  // --------------------------------------------------------------
  // Failure case 3: choices = empty list
  // --------------------------------------------------------------
  @Test
  fun generateEvents_throwsWhenNoChoices() = runTest {
    val service =
        object : OpenAIService {
          override suspend fun chatCompletion(request: ChatCompletionRequest) =
              Response.success(
                  ChatCompletionResponse(
                      id = "x", created = 0, model = "m", usage = null, choices = emptyList()))

          override suspend fun chatCompletionStream(request: ChatCompletionRequest) =
              Response.success(ResponseBody.create(null, ""))
        }

    val gen = OpenAIEventGen(service)

    assertThrows(IllegalStateException::class.java) {
      runTest { gen.generateEvents(minimalQuery()) }
    }
  }

  // --------------------------------------------------------------
  // Failure case 4: blank content
  // --------------------------------------------------------------
  @Test
  fun generateEvents_throwsOnBlankContent() = runTest {
    val service =
        object : OpenAIService {
          override suspend fun chatCompletion(request: ChatCompletionRequest) =
              Response.success(
                  ChatCompletionResponse(
                      id = "x",
                      created = 0,
                      model = "m",
                      usage = null,
                      choices =
                          listOf(
                              Choice(
                                  index = 0,
                                  message = Message("assistant", "   "), // blank content
                                  finish_reason = "stop"))))

          override suspend fun chatCompletionStream(request: ChatCompletionRequest) =
              Response.success(ResponseBody.create(null, ""))
        }

    val gen = OpenAIEventGen(service)

    assertThrows(IllegalStateException::class.java) {
      runTest { gen.generateEvents(minimalQuery()) }
    }
  }

  // --------------------------------------------------------------
  // Failure case 5: parsing failure (malformed JSON)
  // --------------------------------------------------------------
  @Test
  fun generateEvents_throwsOnParsingFailure() = runTest {
    val service =
        object : OpenAIService {
          override suspend fun chatCompletion(request: ChatCompletionRequest) =
              Response.success(
                  ChatCompletionResponse(
                      id = "x",
                      created = 0,
                      model = "m",
                      usage = null,
                      choices =
                          listOf(
                              Choice(
                                  index = 0,
                                  message =
                                      Message(role = "assistant", content = "{ not valid json "),
                                  finish_reason = "stop"))))

          override suspend fun chatCompletionStream(request: ChatCompletionRequest) =
              Response.success(ResponseBody.create(null, ""))
        }

    val gen = OpenAIEventGen(service)

    assertThrows(IllegalStateException::class.java) {
      runTest { gen.generateEvents(minimalQuery()) }
    }
  }

  // --------------------------------------------------------------
  // Streaming endpoint of fake service
  // --------------------------------------------------------------
  @Test
  fun chatCompletionStream_returnsBody() = runTest {
    val service = OpenAIServiceFake()
    val response =
        service.chatCompletionStream(ChatCompletionRequest(model = "m", messages = emptyList()))

    assertTrue(response.isSuccessful)
    assertNotNull(response.body())
  }
}
