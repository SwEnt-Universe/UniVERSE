package com.android.universe.ui.event

import com.android.universe.model.Tag
import com.android.universe.model.event.Event
import com.android.universe.model.event.FakeEventRepository
import com.android.universe.model.user.UserProfile
import java.time.LocalDate
import java.time.LocalDateTime
import junit.framework.TestCase.assertEquals
import kotlin.test.Test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before

@OptIn(ExperimentalCoroutinesApi::class)
class EventViewModelTest {
  private lateinit var repository: FakeEventRepository
  private lateinit var viewModel: EventViewModel
  private val testDispatcher = StandardTestDispatcher()

  // Sample users for events
  private val sampleUsers =
      listOf(
          UserProfile(
              uid = "0",
              username = "alice_smith",
              firstName = "Alice",
              lastName = "Smith",
              country = "US",
              description = "Loves running",
              dateOfBirth = LocalDate.of(1990, 1, 1),
              tags = setOf(Tag.SCULPTURE)),
          UserProfile(
              uid = "1",
              username = "bob_johnson",
              firstName = "Bob",
              lastName = "Johnson",
              country = "US",
              dateOfBirth = LocalDate.of(1985, 5, 12),
              tags = setOf(Tag.TENNIS)),
          UserProfile(
              uid = "2",
              username = "charlie_brown",
              firstName = "Charlie",
              lastName = "Brown",
              country = "US",
              dateOfBirth = LocalDate.of(1992, 3, 22),
              tags = emptySet()))

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)

    repository = FakeEventRepository()
    val sampleEvents =
        listOf(
            Event(
                id = "event-001",
                title = "Morning Run at the Lake",
                description = "Join us for a casual 5km run around the lake followed by coffee.",
                date = LocalDateTime.of(2025, 10, 15, 7, 30),
                tags = setOf(Tag.SCULPTURE, Tag.COUNTRY),
                participants = setOf(sampleUsers[0], sampleUsers[1]),
                creator = sampleUsers[0]),
            Event(
                id = "event-002",
                title = "Tech Hackathon 2025",
                date = LocalDateTime.of(2025, 11, 3, 9, 0),
                tags = setOf(Tag.TENNIS, Tag.ARTIFICIAL_INTELLIGENCE, Tag.PROGRAMMING),
                participants = emptySet(),
                creator = sampleUsers[1]))

    runBlocking { sampleEvents.forEach { repository.addEvent(it) } }

    viewModel = EventViewModel(repository)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun initialStateIsLoaded() = runTest {
    val events = viewModel.eventsState.value
    assertEquals(2, events.size)
  }

  @Test
  fun firstEventIsMappedCorrectly() = runTest {
    val firstEvent = viewModel.eventsState.value.first()
    assertEquals("Morning Run at the Lake", firstEvent.title)
    assertEquals(
        "Join us for a casual 5km run around the lake followed by coffee.", firstEvent.description)
    assertEquals("15 Oct 07:30 AM", firstEvent.date)
    assertEquals(listOf("Sculpture", "Country"), firstEvent.tags)
    assertEquals("Alice Smith", firstEvent.creator)
    assertEquals(2, firstEvent.participants)
  }

  @Test
  fun eventWithNoDescriptionHasEmptyString() = runTest {
    val hackathonEvent = viewModel.eventsState.value.first { it.title == "Tech Hackathon 2025" }
    assertEquals("", hackathonEvent.description)
  }

  @Test
  fun participantCountIsCorrect() = runTest {
    val runEvent = viewModel.eventsState.value.first { it.title == "Morning Run at the Lake" }
    assertEquals(2, runEvent.participants)

    val hackathonEvent = viewModel.eventsState.value.first { it.title == "Tech Hackathon 2025" }
    assertEquals(0, hackathonEvent.participants)
  }

  @Test
  fun creatorIsFormattedCorrectly() = runTest {
    val runEvent = viewModel.eventsState.value.first { it.title == "Morning Run at the Lake" }
    assertEquals("Alice Smith", runEvent.creator)
  }

  @Test
  fun eventsWithMoreThanThreeTagsAreCropped() = runTest {
    val extraEvent =
        Event(
            id = "event-100",
            title = "Mega Tag Event",
            description = "Event with too many tags",
            date = LocalDateTime.of(2025, 12, 1, 10, 0),
            tags =
                setOf(
                    Tag.TENNIS,
                    Tag.ARTIFICIAL_INTELLIGENCE,
                    Tag.PROGRAMMING,
                    Tag.RUNNING,
                    Tag.MUSIC),
            participants = setOf(sampleUsers[0], sampleUsers[1]),
            creator = sampleUsers[0])

    runBlocking { repository.addEvent(extraEvent) }

    // Refresh events in the ViewModel
    viewModel.loadEvents()
    advanceUntilIdle()

    val megaTagEvent = viewModel.eventsState.value.first { it.title == "Mega Tag Event" }

    assertEquals(3, megaTagEvent.tags.size)
    assertEquals(listOf("Tennis", "Artificial intelligence", "Programming"), megaTagEvent.tags)
  }
}
