package com.android.universe.ui.event

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.model.event.Event
import com.android.universe.model.event.FakeEventRepository
import com.android.universe.model.location.Location
import com.android.universe.model.tag.Tag
import com.android.universe.model.user.FakeUserRepository
import com.android.universe.model.user.UserProfile
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class EventViewModelTest {

  companion object {
    const val EVENT1TITLE = "Morning Run at the Lake"
    const val EVENT1DESC = "Join us for a casual 5km run around the lake followed by coffee."
    val SAMPLETAG = Tag.METAL
  }

  private lateinit var repository: FakeEventRepository
  private lateinit var userRepo: FakeUserRepository
  private lateinit var viewModel: EventViewModel

  private val futureDate = LocalDateTime.now().plusDays(10)
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

  val sampleEvents =
      listOf(
          Event(
              id = "event-001",
              title = EVENT1TITLE,
              description = EVENT1DESC,
              date = futureDate,
              tags = setOf(Tag.SCULPTURE, Tag.COUNTRY),
              participants = setOf(sampleUsers[0].uid, sampleUsers[1].uid),
              creator = sampleUsers[0].uid,
              location = Location(latitude = 46.5196535, longitude = 6.6322734),
              eventPicture = ByteArray(126 * 126) { index -> (index % 256).toByte() }),
          Event(
              id = "event-002",
              title = "Tech Hackathon 2025",
              date = futureDate,
              tags = setOf(Tag.TENNIS, Tag.AI, Tag.PROGRAMMING),
              participants = emptySet(),
              creator = sampleUsers[1].uid,
              location = Location(latitude = 46.5196535, longitude = 6.6322734),
              eventPicture = ByteArray(126 * 126) { index -> (index % 256).toByte() }))

  val thirdEvent =
      Event(
          id = "event-100",
          title = "Mega Tag Event",
          description = "Event with too many tags",
          date = futureDate,
          tags = setOf(Tag.TENNIS, Tag.AI, Tag.PROGRAMMING, Tag.RUNNING, Tag.MUSIC),
          participants = setOf(sampleUsers[0].uid, sampleUsers[1].uid),
          creator = sampleUsers[0].uid,
          location = Location(latitude = 46.5196535, longitude = 6.6322734),
          eventPicture = ByteArray(126 * 126) { index -> (index % 256).toByte() })

  @Before
  fun setup() {
    repository = FakeEventRepository()
    userRepo = FakeUserRepository()

    runTest {
      sampleEvents.forEach { repository.addEvent(it) }
      sampleUsers.forEach { userRepo.addUser(it) }
    }

    viewModel = EventViewModel(repository, null, userRepo)
    viewModel.storedUid = sampleUsers[0].uid
    runTest {
      viewModel.loadEvents()
      advanceUntilIdle()
    }
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
    assertEquals(futureDate, firstEvent.date)
    assertEquals(listOf(Tag.SCULPTURE, Tag.COUNTRY), firstEvent.tags)
    assertEquals("Alice Smith", firstEvent.creator)
    assertEquals(2, firstEvent.participants)
    assertArrayEquals(
        ByteArray(126 * 126) { index -> (index % 256).toByte() }, firstEvent.eventPicture)
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
  fun joinOrLeaveEventUpdatesParticipants() = runTest {
    advanceUntilIdle()
    val events = viewModel.eventsState.value
    assert(events.isNotEmpty())
    val first = events.first()

    val initialParticipants = first.participants
    assertEquals(true, first.joined)

    viewModel.joinOrLeaveEvent(first.index)
    advanceUntilIdle()

    val updated = viewModel.eventsState.value[first.index]
    assertEquals(false, updated.joined)
    assertEquals(initialParticipants - 1, updated.participants)
    assertEquals(viewModel.uiState.value.errormsg, null)
  }

  @Test
  fun joinOrLeaveEventHandlesUpdateError() {
    viewModel.setErrorMsg("No event $EVENT1TITLE found")
    assertEquals("No event $EVENT1TITLE found", viewModel.uiState.value.errormsg)
    viewModel.setErrorMsg(null)
    assertEquals(null, viewModel.uiState.value.errormsg)
  }

  @Test
  fun updateSearchQuery_updatesState() = runTest {
    assertEquals("", viewModel.searchQuery.value)

    viewModel.updateSearchQuery("run")
    advanceUntilIdle()

    assertEquals("run", viewModel.searchQuery.value)
  }

  @Test
  fun filteredEvents_returnsEmpty_whenNoMatch() = runTest {
    viewModel.updateSearchQuery("zzzz")
    advanceUntilIdle()

    val filtered = viewModel.filteredEvents.value
    assertEquals(0, filtered.size)
  }

  @Test
  fun filteredEvents_returnsAllEvents_whenQueryBlank() = runTest {
    val collected = mutableListOf<List<EventUIState>>()
    val job =
        launch(UnconfinedTestDispatcher(testScheduler)) {
          viewModel.filteredEvents.collect { collected.add(it) }
        }

    advanceUntilIdle()
    viewModel.updateSearchQuery("")
    advanceUntilIdle()

    assertEquals(2, collected.last().size)
    job.cancel()
  }

  @Test
  fun filteredEvents_filtersByTitleContains() = runTest {
    val collected = mutableListOf<List<EventUIState>>()
    val job =
        launch(UnconfinedTestDispatcher(testScheduler)) {
          viewModel.filteredEvents.collect { collected.add(it) }
        }

    advanceUntilIdle()
    viewModel.updateSearchQuery("run")
    advanceUntilIdle()

    val result = collected.last()
    assertEquals(2, result.size)
    assertEquals(EVENT1TITLE, result.first().title)

    job.cancel()
  }

  @Test
  fun categoriesUpdatesWork() {
    viewModel.selectCategory(SAMPLETAG.category)
    assertEquals(setOf(SAMPLETAG.category), viewModel.categories.value)
    viewModel.deselectCategory(SAMPLETAG.category)
    assertEquals(emptySet<Tag.Category>(), viewModel.categories.value)
  }
}
