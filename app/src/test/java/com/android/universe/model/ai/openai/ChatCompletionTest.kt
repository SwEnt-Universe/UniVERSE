package com.android.universe.model.ai.openai

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody
import org.junit.Assert
import org.junit.Test
import retrofit2.Response

class ChatCompletionTest {

  private val json = Json { ignoreUnknownKeys = true }

  @Test
  fun request_serializes_with_all_fields() {
    val req =
        ChatCompletionRequest(
            model = "gpt-xxx",
            messages = listOf(Message("user", "Hello")),
            temperature = 0.5,
            max_completion_tokens = 20,
            response_format = ResponseFormat("json_schema", null))

    val encoded = json.encodeToString(req)
    Assert.assertTrue(encoded.contains("\"model\":\"gpt-xxx\""))
    Assert.assertTrue(encoded.contains("\"temperature\":0.5"))
    Assert.assertTrue(encoded.contains("\"max_completion_tokens\":20"))
  }

  @Test
  fun request_serializes_when_optional_fields_null() {
    val req = ChatCompletionRequest(model = "m", messages = listOf(Message("system", "hi")))

    val encoded = json.encodeToString(req)
    // These MUST be omitted
    Assert.assertFalse(encoded.contains("temperature"))
    Assert.assertFalse(encoded.contains("max_completion_tokens"))
    Assert.assertFalse(encoded.contains("response_format"))
  }

  @Test
  fun response_deserializes_with_usage() {
    val raw =
        """
        {
          "id": "1",
          "created": 123,
          "model": "x",
          "choices": [
            {
              "index": 0,
              "message": { "role": "assistant", "content": "hi" },
              "finish_reason": "stop"
            }
          ],
          "usage": {
            "prompt_tokens": 10,
            "completion_tokens": 5,
            "total_tokens": 15
          }
        }
        """
            .trimIndent()

    val resp = json.decodeFromString<ChatCompletionResponse>(raw)

    Assert.assertEquals("1", resp.id)
    Assert.assertEquals(1, resp.choices.size)
    Assert.assertNotNull(resp.usage)
    Assert.assertEquals(15, resp.usage!!.total_tokens)
  }

  @Test
  fun response_deserializes_without_usage() {
    val raw =
        """
        {
          "id": "1",
          "created": 12,
          "model": "x",
          "choices": [
            {
              "index": 0,
              "message": { "role": "assistant", "content": "ok" }
            }
          ]
        }
        """
            .trimIndent()

    val resp = json.decodeFromString<ChatCompletionResponse>(raw)

    Assert.assertNull(resp.usage)
  }

  @Test
  fun empty_choices_is_valid_but_edge_case() {
    val raw =
        """
        {
          "id": "1",
          "created": 1,
          "model": "x",
          "choices": []
        }
        """
            .trimIndent()

    val resp = json.decodeFromString<ChatCompletionResponse>(raw)
    Assert.assertTrue(resp.choices.isEmpty())
  }

  @Test
  fun streaming_responsebody_is_wrapped_correctly() {
    val body = ResponseBody.Companion.create("text/plain".toMediaType(), "hello")
    val response = Response.success(body)
    Assert.assertEquals("hello", response.body()!!.string())
  }

  @Test
  fun response_deserializes_choice_with_null_finish_reason() {
    val raw =
        """
        {
          "id": "2",
          "created": 50,
          "model": "xyz",
          "choices": [
            {
              "index": 0,
              "message": { "role": "assistant", "content": "ok" },
              "finish_reason": null
            }
          ]
        }
        """
            .trimIndent()

    val resp = json.decodeFromString<ChatCompletionResponse>(raw)

    Assert.assertEquals("xyz", resp.model)
    Assert.assertNull(resp.choices.first().finish_reason)
  }

  @Test
  fun responseFormat_deserializes_with_schema() {
    val raw =
        """
        {
          "type": "json_schema",
          "json_schema": { "foo": "bar" }
        }
        """
            .trimIndent()

    val resp = json.decodeFromString<ResponseFormat>(raw)

    Assert.assertEquals("json_schema", resp.type)
    Assert.assertEquals("bar", resp.json_schema!!["foo"].toString().trim('"'))
  }

  @Test
  fun message_roles_are_serialized_and_deserialized() {
    val msgs = listOf(Message("system", "sys"), Message("user", "u"), Message("assistant", "a"))

    val encoded = json.encodeToString(msgs)
    val decoded = json.decodeFromString<List<Message>>(encoded)

    Assert.assertEquals(3, decoded.size)
    Assert.assertEquals("system", decoded[0].role)
    Assert.assertEquals("user", decoded[1].role)
    Assert.assertEquals("assistant", decoded[2].role)
  }
}
