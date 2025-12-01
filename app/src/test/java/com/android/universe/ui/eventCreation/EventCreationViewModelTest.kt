package com.android.universe.ui.eventCreation

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.android.universe.model.event.FakeEventRepository
import com.android.universe.model.location.Location
import com.android.universe.model.tag.Tag
import com.android.universe.model.tag.TagLocalTemporaryRepository
import com.android.universe.model.tag.TagTemporaryRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EventCreationViewModelTest {
  private lateinit var eventRepository: FakeEventRepository
  private lateinit var viewModel: EventCreationViewModel
  private lateinit var tagRepository: TagTemporaryRepository
  private val testDispatcher = StandardTestDispatcher()

  /** Companion object to provides values for the tests. */
  companion object {
    const val SAMPLE_TITLE = "Sample Title"
    const val SAMPLE_DESCRIPTION = "Sample Description"
    const val SAMPLE_DATE_FORMAT = "12/12/2030"
    const val SAMPLE_TIME_FORMAT = "12:12"
    val SAMPLE_DATE = LocalDate.of(2030, 12, 12)
    val SAMPLE_TIME = LocalTime.of(12, 12)
    val sample_tags = setOf(Tag.METAL, Tag.HANDBALL)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    eventRepository = FakeEventRepository()
    tagRepository = TagLocalTemporaryRepository()
    viewModel =
        EventCreationViewModel(eventRepository = eventRepository, tagRepository = tagRepository)
  }

  @Test
  fun testSetEventName() {
    viewModel.setEventName(SAMPLE_TITLE)
    val state = viewModel.uiStateEventCreation.value
    assert(state.name == SAMPLE_TITLE)
  }

  @Test
  fun testSetEventDescription() {
    viewModel.setEventDescription(SAMPLE_DESCRIPTION)
    val state = viewModel.uiStateEventCreation.value
    assert(state.description == SAMPLE_DESCRIPTION)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun testSetEventTags() = runTest {
    val eventTags = setOf(Tag.METAL, Tag.KARATE)
    viewModel.setEventTags(eventTags)
    advanceUntilIdle()
    assert(viewModel.eventTags.value == eventTags)
  }

  @Test
  fun updateTagInRepoUpdateViewModel() = runTest {
    tagRepository.updateTags(sample_tags)
    testDispatcher.scheduler.advanceUntilIdle()
    assertEquals(sample_tags, viewModel.eventTags.value)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun testSaveEvent() = runTest {
    viewModel.setEventName(SAMPLE_TITLE)

    viewModel.setEventDescription(SAMPLE_DESCRIPTION)

    viewModel.setDate(SAMPLE_DATE)
    viewModel.setTime(SAMPLE_TIME)
    viewModel.setLocation(0.0,0.0)

    assertEquals(
        viewModel.formatTime(viewModel.uiStateEventCreation.value.time), SAMPLE_TIME_FORMAT)
    assertEquals(
        viewModel.formatDate(viewModel.uiStateEventCreation.value.date), SAMPLE_DATE_FORMAT)

    tagRepository.updateTags(sample_tags)
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.saveEvent(uid = "user123")
    testDispatcher.scheduler.advanceUntilIdle()
    val savedEvent = eventRepository.getAllEvents()
    assert(savedEvent.size == 1)
    val event = savedEvent[0]
    assert(event.title == SAMPLE_TITLE)
    assert(event.description == SAMPLE_DESCRIPTION)
    assert(event.creator == "user123")
    assert(event.participants == setOf("user123"))
    assert(event.location == Location(0.0, 0.0))
    assert(event.tags == sample_tags)

    val expectedDate = LocalDateTime.of(2030, 12, 12, 12, 12)

    assert(event.date == expectedDate)
    assertEquals(emptySet<Tag>(), tagRepository.getTags())
  }

  @Test
  fun setImageWithNullUriRemoveImagePicture() {
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    viewModel.setImage(context, null)
    assertEquals(null, viewModel.uiStateEventCreation.value.eventPicture)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }
}
