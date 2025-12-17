package com.android.universe.ui.profile

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.model.event.FakeEventRepository
import com.android.universe.model.user.FakeUserRepository
import com.android.universe.model.user.UserProfile
import com.android.universe.utils.EventTestData
import com.android.universe.utils.MainCoroutineRule
import com.android.universe.utils.UserTestData
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class UserProfileViewModelTest {
  private lateinit var userRepository: FakeUserRepository
  private lateinit var eventRepository: FakeEventRepository
  private lateinit var viewModel: UserProfileViewModel

  @get:Rule val mainCoroutinesRule = MainCoroutineRule()

  @Before
  fun setup() {
    userRepository = FakeUserRepository()
    eventRepository = FakeEventRepository()
  }

  @Test
  fun loadsUserDataCorrectlyForExistingUser() = runTest {
    val profile =
        UserProfile(
            uid = "0",
            username = "alice",
            firstName = "Alice",
            lastName = "Smith",
            country = "CH",
            description = "Hi, I'm Alice.",
            dateOfBirth = LocalDate.of(1990, 1, 1),
            tags = emptySet())

    userRepository.addUser(profile)
    advanceUntilIdle()
    viewModel = UserProfileViewModel(profile.uid, "", userRepository, eventRepository)

    advanceUntilIdle()
    val state = viewModel.userState.value
    assertEquals("alice", state.userProfile.username)
    assertEquals("Alice", state.userProfile.firstName)
    assertEquals("Smith", state.userProfile.lastName)
    assertEquals("Hi, I'm Alice.", state.userProfile.description)
    assertEquals("CH", state.userProfile.country)
    assertEquals(1990, state.userProfile.dateOfBirth.year)
    assertEquals(1, state.userProfile.dateOfBirth.month.value)
    assertEquals(1, state.userProfile.dateOfBirth.dayOfMonth)
    assertEquals(0, state.userProfile.tags.size)
    assertNull(state.userProfile.profilePicture)
    assertNull(state.errorMsg)
  }

  @Test
  fun nonEmptyObserver_nonNullFollowerState() {
    runTest {
      userRepository.addUser(UserTestData.Bob)
      userRepository.addUser(UserTestData.Alice)
      val viewModel =
          UserProfileViewModel(
              UserTestData.Bob.uid, UserTestData.Alice.uid, userRepository, eventRepository)
      advanceUntilIdle()
      assertEquals(false, viewModel.userState.value.follower)
    }
  }

  @Test
  fun loadUser_splitsEventsCorrectlyIntoIncomingAndHistory() = runTest {
    val user = UserTestData.Alice
    userRepository.addUser(user)

    val now = LocalDateTime.now()

    val incomingEvent =
        EventTestData.dummyEvent1.copy(
            id = "incoming", date = now.plusDays(1), participants = setOf(user.uid))

    val historyEvent =
        EventTestData.dummyEvent2.copy(
            id = "history", date = now.minusDays(1), participants = setOf(user.uid))

    eventRepository.addEvent(incomingEvent)
    eventRepository.addEvent(historyEvent)
    advanceUntilIdle()

    viewModel = UserProfileViewModel(user.uid, "", userRepository, eventRepository)
    advanceUntilIdle()

    val state = viewModel.userState.value

    assertEquals("Should have 1 incoming event", 1, state.incomingEvents.size)
    assertEquals("Should have 1 history event", 1, state.historyEvents.size)

    val actualIncoming = state.incomingEvents.first()
    assertEquals(incomingEvent.id, actualIncoming.id)
    assertEquals(incomingEvent.title, actualIncoming.title)
    assertEquals(incomingEvent.date, actualIncoming.date)

    val actualHistory = state.historyEvents.first()
    assertEquals(historyEvent.id, actualHistory.id)
    assertEquals(historyEvent.title, actualHistory.title)
    assertEquals(historyEvent.date, actualHistory.date)
  }

  @Test
  fun loadUser_sortsEventsCorrectly() = runTest {
    val user = UserTestData.Bob
    userRepository.addUser(user)

    val now = LocalDateTime.now()

    val soonEvent =
        EventTestData.dummyEvent1.copy(
            id = "soon", date = now.plusDays(1), participants = setOf(user.uid))

    val laterEvent =
        EventTestData.dummyEvent2.copy(
            id = "later", date = now.plusDays(7), participants = setOf(user.uid))

    val recentHistory =
        EventTestData.dummyEvent3.copy(
            id = "recent", date = now.minusDays(1), participants = setOf(user.uid))

    val oldHistory =
        EventTestData.dummyEvent1.copy(
            id = "old", date = now.minusYears(1), participants = setOf(user.uid))

    eventRepository.addEvent(soonEvent)
    eventRepository.addEvent(laterEvent)
    eventRepository.addEvent(recentHistory)
    eventRepository.addEvent(oldHistory)

    viewModel = UserProfileViewModel(user.uid, "", userRepository, eventRepository)
    advanceUntilIdle()

    val state = viewModel.userState.value

    assertEquals(2, state.incomingEvents.size)
    assertEquals("Soonest event should be first", soonEvent.id, state.incomingEvents[0].id)
    assertEquals("Later event should be second", laterEvent.id, state.incomingEvents[1].id)

    assertEquals(2, state.historyEvents.size)
    assertEquals("Recent history should be first", recentHistory.id, state.historyEvents[0].id)
    assertEquals("Oldest history should be last", oldHistory.id, state.historyEvents[1].id)
  }
}
