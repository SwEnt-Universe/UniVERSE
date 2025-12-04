package com.android.universe.ui.event

import com.android.universe.ui.search.SearchEngine
import io.mockk.every
import io.mockk.mockkObject
import org.junit.Assert.assertEquals
import org.junit.Test

class EventFilterTest {

  @Test
  fun `returns all when query blank`() {
    val events = listOf(EventUIState(title = "Run"), EventUIState(title = "Chess"))

    val result = filterEvents(events, "")
    assertEquals(2, result.size)
  }

  @Test
  fun `filters by contains ignoring case`() {
    val events = listOf(EventUIState(title = "Morning Run"), EventUIState(title = "Chess Battle"))

    val result = filterEvents(events, "run")
    assertEquals(1, result.size)
    assertEquals("Morning Run", result.first().title)
  }

  @Test
  fun `fuzzy match is used`() {
    mockkObject(SearchEngine)
    every { SearchEngine.fuzzyMatch("Meditation", "Medtation") } returns true

    val events = listOf(EventUIState(title = "Meditation"))
    val result = filterEvents(events, "Medtation")

    assertEquals(1, result.size)
  }
}
