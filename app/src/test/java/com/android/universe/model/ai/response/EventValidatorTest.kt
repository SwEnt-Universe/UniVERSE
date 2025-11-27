package com.android.universe.model.ai.response

import com.android.universe.model.event.EventDTO
import com.android.universe.model.event.LocationDTO
import org.junit.Assert
import org.junit.Test

class EventValidatorTest {

  private fun validDto(): EventDTO =
      EventDTO(
          title = "Test Event",
          description = "Some description",
          date = "2030-01-01T10:00",
          tags = listOf("Physics"),
          location = LocationDTO(46.52, 6.63))

  @Test
  fun `validate accepts a correct DTO`() {
    // If no exception is thrown, the test passes
    EventValidator.validate(validDto())
  }

  @Test
  fun `validate fails when title is blank`() {
    val dto = validDto().copy(title = " ")
    Assert.assertThrows(IllegalArgumentException::class.java) { EventValidator.validate(dto) }
  }

  @Test
  fun `validate fails when description is blank`() {
    val dto = validDto().copy(description = "")
    Assert.assertThrows(IllegalArgumentException::class.java) { EventValidator.validate(dto) }
  }

  @Test
  fun `validate fails for invalid latitude`() {
    val dto = validDto().copy(location = LocationDTO(120.0, 6.63))
    Assert.assertThrows(IllegalArgumentException::class.java) { EventValidator.validate(dto) }
  }

  @Test
  fun `validate fails for invalid longitude`() {
    val dto = validDto().copy(location = LocationDTO(46.52, -200.0))
    Assert.assertThrows(IllegalArgumentException::class.java) { EventValidator.validate(dto) }
  }

  @Test
  fun `validate fails for invalid date format`() {
    val dto = validDto().copy(date = "not-a-date")
    Assert.assertThrows(IllegalArgumentException::class.java) { EventValidator.validate(dto) }
  }

  @Test
  fun `validate fails when event date is in the past`() {
    val dto = validDto().copy(date = "2000-01-01T00:00")

    Assert.assertThrows(IllegalArgumentException::class.java) {
      EventValidator.validate(dto)
    }
  }
}
